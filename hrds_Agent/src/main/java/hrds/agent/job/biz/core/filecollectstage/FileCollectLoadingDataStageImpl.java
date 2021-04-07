package hrds.agent.job.biz.core.filecollectstage;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.utils.FileNameUtils;
import hrds.agent.job.biz.bean.AvroBean;
import hrds.agent.job.biz.bean.FileCollectParamBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.filecollectstage.methods.AvroBeanProcess;
import hrds.agent.job.biz.core.filecollectstage.methods.AvroOper;
import hrds.agent.job.biz.utils.PropertyParaUtil;
import hrds.agent.trans.biz.unstructuredfilecollect.FileCollectJob;
import hrds.commons.codes.DataSourceType;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.hadoop.readconfig.ConfigReader;
import hrds.commons.utils.MapDBHelper;
import hrds.commons.utils.jsch.FileProgressMonitor;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.jsch.SftpOperate;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

@DocClass(desc = "将avro文件读取出来存到大数据平台上", author = "zxz", createdate = "2019/11/5 10:12")
public class FileCollectLoadingDataStageImpl implements Callable<String> {

	private static final Logger logger = LogManager.getLogger();
	private final FileCollectParamBean fileCollectParamBean;
	//大文件文件夹路径
	private static final String BIGFILENAME = "bigFiles";
	private final ConcurrentMap<String, String> fileNameHTreeMap;
	//mapDB操作对象
	private final MapDBHelper mapDBHelper;

	FileCollectLoadingDataStageImpl(FileCollectParamBean paramBean, ConcurrentMap<String, String> fileNameHTreeMap,
	                                MapDBHelper mapDBHelper) {
		this.mapDBHelper = mapDBHelper;
		this.fileNameHTreeMap = fileNameHTreeMap;
		this.fileCollectParamBean = paramBean;
		String fileCollectHdfsPath = FileNameUtils.normalize(JobConstant.PREFIX + File.separator
			+ DataSourceType.DCL.getCode() + File.separator + paramBean.getFcs_id() + File.separator
			+ paramBean.getFile_source_id() + File.separator + BIGFILENAME, true);
		//如果有Hadoop环境,则创建HDFS文件目录
		if (JobConstant.FILE_COLLECTION_IS_WRITE_HADOOP) {
			//TODO 创建hdfs文件夹,这里agent需要制定参数，选择目的地的话应该从目的地取配置
			try (HdfsOperator operator = new HdfsOperator(
				System.getProperty("user.dir") + File.separator + "conf" + File.separator,
				PropertyParaUtil.getString("platform", ConfigReader.PlatformType.normal.toString()),
				PropertyParaUtil.getString("principle.name", "admin@HADOOP.COM"),
				PropertyParaUtil.getString("HADOOP_USER_NAME", "hyshf")
			)) {
				Path path = new Path(fileCollectHdfsPath);
				if (!operator.exists(path)) {
					operator.mkdir(path);
				}
			} catch (Exception e) {
				logger.error("检查当前环境是否配置集群客户端", e);
				throw new AppSystemException("初始化文件采集数据加载类失败");
			}
		}
	}

	@Override
	public String call() {
		String message = "";
		logger.info("Start FileCollectLoadingDataStageImpl Thread ...");
		ArrayBlockingQueue<String> queue = FileCollectJob.mapQueue.get(fileCollectParamBean.getFile_source_id());
		try {
			while (true) {
				//从队列中拿出信息，当队列中无信息时，该线程阻塞
				String queueMeta = queue.take();
				logger.info("queue.size: " + queue.size() + " ; queueMeta: " + queueMeta);
				JSONObject queueJb = JSONObject.parseObject(queueMeta);
				String sysDate = queueJb.getString("sys_date");
				//文件采集的id
				String avroFileAbsolutionPath = queueJb.getString("avroFileAbsolutionPath");
				String fileCollectHdfsPath = queueJb.getString("fileCollectHdfsPath");
				String jobRsId = queueJb.getString("job_rs_id");
				long watcherId = queueJb.getLong("watcher_id");
				//对其做处理（有Hadoop环境上传hdfs,没有Hadoop环境传输到服务器本地硬盘下）
				if (JobConstant.FILE_COLLECTION_IS_WRITE_HADOOP) {
					copyFileToHDFS(avroFileAbsolutionPath, fileCollectHdfsPath);
				} else {
					copyFileToRemote(avroFileAbsolutionPath, fileCollectHdfsPath);
				}
				//判断如果是大文件，则只做文件上传处理，大文件的信息记录在其他队列中，这里直接取下一个队列的数据
				if (IsFlag.Shi.getCode().equals(queueJb.getString("isBigFile"))) {
					continue;
				}
				//获取hdfs上的avro文件路径
				Path avroPath = new Path(fileCollectHdfsPath + FileNameUtils.getName(avroFileAbsolutionPath));
				//传输文件并入库，入hbase
				logger.info("处理  -->" + avroPath.getName());
				//不是hadoop版获取本地的卸数的Avro文件
				if (!JobConstant.FILE_COLLECTION_IS_WRITE_HADOOP) {
					avroPath = new Path(avroFileAbsolutionPath);
				}
				//获取AvroBeans
				List<AvroBean> avroBeans = AvroOper.getAvroBeans(avroPath);
				AvroBeanProcess abp = new AvroBeanProcess(fileCollectParamBean, sysDate, jobRsId);
				//TODO 目前只保存到solr和 Source_file_attribute
				// 判断是否入solr
				if (IsFlag.Shi.getCode().equals(fileCollectParamBean.getIs_solr())) {
					abp.saveInSolr(avroBeans);
				}
				//Source_file_attribute 保存采集文件的源信息,如果需要保存到HBase或者psql,获取saveMetaData的返回结果然后保存
				abp.saveMetaData(avroBeans, fileNameHTreeMap);
//				List<String[]> hbaseList = abp.saveMetaData(avroBeans, fileNameHTreeMap);
				//如果有Hadoop环境,则存入HBase,否则存Psql
//				if (JobConstant.HAS_HADOOP_ENV) {
//					//存入HBase
//					abp.saveInHbase(hbaseList);
//				} else {
//					abp.saveInPostgreSupersedeHbase(hbaseList);
//				}
				//保存到mapDB
				saveInMapDB(avroBeans);
				//处理完后 删除本地文件
				boolean delete = FileUtils.getFile(avroFileAbsolutionPath).delete();
				if (!delete) {
					logger.error("删除本地文件" + avroFileAbsolutionPath + "失败！");
				}
				//值为0代表当前拿到的值是队列最后一个,退出循环
				if (watcherId == AvroOper.LASTELEMENT) {
					logger.info("End FileCollectLoadingDataStageImpl Thread ...");
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Failed to process Avro file in hdfs FileSystem...", e);
			message = e.getMessage();
		}
		return message;
	}

	/**
	 * 保存文件信息到mapDB
	 */
	private void saveInMapDB(List<AvroBean> avroBeans) {
		for (AvroBean bean : avroBeans) {
			JSONObject object = new JSONObject();
			object.put("uuid", bean.getUuid());
			object.put("file_md5", bean.getFile_md5());
			fileNameHTreeMap.put(bean.getFile_scr_path(), object.toJSONString());
		}
		mapDBHelper.commit();
		logger.info("提交到mapDB");
	}

	@Method(desc = "执行上传文件到hdfs", logicStep = "1.调用hdfs操作类将本地文件上传到hdfs")
	@Param(name = "localPath", desc = "本地文件路径", range = "不可为空")
	@Param(name = "hdfsPath", desc = "hdfs文件路径", range = "不可为空")
	private static void copyFileToHDFS(String localPath, String hdfsPath) {
		// 1.调用hdfs操作类将本地文件上传到hdfs TODO
		try (HdfsOperator operator = new HdfsOperator(System.getProperty("user.dir") + File.separator + "conf" + File.separator,
			PropertyParaUtil.getString("platform", ConfigReader.PlatformType.normal.toString()),
			PropertyParaUtil.getString("principle.name", "admin@HADOOP.COM"),
			PropertyParaUtil.getString("HADOOP_USER_NAME", "hyshf"))) {
			operator.upLoad(localPath, hdfsPath, true);
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			} else {
				throw new AppSystemException(e);
			}
		}
	}

	@Method(
		desc = "执行上传文件到远程服务器",
		logicStep =
			"1.判断是否有hadoop环境，调用对应的方法" + "2.为了防止远程文件夹不存在，先创建文件夹" + "3.传输文件到远程服务器，并设置监控，打印传输文件百分比")
	@Param(name = "localPath", desc = "本地文件路径", range = "不可为空")
	@Param(name = "hdfsPath", desc = "hdfs文件路径", range = "不可为空")
	private static void copyFileToRemote(String localPath, String remotePath) {
		// 1.构建连接远程机器的对象
		SFTPDetails sftpDetails = new SFTPDetails();
		sftpDetails.setHost(PropertyParaUtil.getString("hyren_host", ""));
		sftpDetails.setUser_name(PropertyParaUtil.getString("hyren_user", ""));
		sftpDetails.setPwd(PropertyParaUtil.getString("hyren_pwd", ""));
		sftpDetails.setPort(Integer.parseInt(PropertyParaUtil.getString("sftp_port", "22")));
		try {
			// 初始化 sftpOperate 操作对象
			SftpOperate sftpOperate = new SftpOperate(sftpDetails);
			// 2.为了防止远程文件夹不存在，先创建文件夹
			sftpOperate.scpMkdir(remotePath);
			long fileSize = new File(localPath).length();
			// 3.传输文件到远程服务器，并设置监控，打印传输文件百分比
			sftpOperate.channelSftp.put(localPath, remotePath, new FileProgressMonitor(fileSize), ChannelSftp.OVERWRITE);
			sftpOperate.channelSftp.quit();
			sftpOperate.close();
		} catch (Exception e) {
			logger.error("拷贝本地文件到服务器: " + sftpDetails.getHost() + " 失败! 执行异常: ", e.getMessage());
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			} else {
				throw new AppSystemException(e);
			}
		}
	}
}