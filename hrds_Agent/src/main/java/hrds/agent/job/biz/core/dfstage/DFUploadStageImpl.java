package hrds.agent.job.biz.core.dfstage;

import com.jcraft.jsch.ChannelSftp;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.SystemUtil;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.*;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.constant.RunStatusConstant;
import hrds.agent.job.biz.constant.StageConstant;
import hrds.agent.job.biz.core.AbstractJobStage;
import hrds.agent.job.biz.core.dfstage.incrementfileprocess.TableProcessInterface;
import hrds.agent.job.biz.core.dfstage.incrementfileprocess.impl.MppTableProcessImpl;
import hrds.agent.job.biz.core.dfstage.service.ReadFileToDataBase;
import hrds.agent.job.biz.core.dfstage.service.ReadFileToSolr;
import hrds.agent.job.biz.utils.DataTypeTransform;
import hrds.agent.job.biz.utils.FileUtil;
import hrds.agent.job.biz.utils.JobStatusInfoUtil;
import hrds.commons.codes.*;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.exception.AppSystemException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.hadoop.solr.ISolrOperator;
import hrds.commons.hadoop.solr.SolrFactory;
import hrds.commons.hadoop.solr.SolrParam;
import hrds.commons.utils.Constant;
import hrds.commons.utils.StorageTypeKey;
import hrds.commons.utils.jsch.FileProgressMonitor;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.jsch.SftpOperate;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@DocClass(desc = "数据文件采集，数据上传阶段实现", author = "WangZhengcheng")
public class DFUploadStageImpl extends AbstractJobStage {
	//打印日志
	private static final Logger LOGGER = LogManager.getLogger();
	private final CollectTableBean collectTableBean;

	/**
	 * 数据文件采集，数据上传阶段实现
	 *
	 * @param collectTableBean 文件采集对应的表信息
	 */
	public DFUploadStageImpl(CollectTableBean collectTableBean) {
		this.collectTableBean = collectTableBean;
	}

	@Method(desc = "数据文件采集，数据上传阶段实现，处理完成后，无论成功还是失败，" +
		"将相关状态信息封装到StageStatusInfo对象中返回", logicStep = "")
	@Return(desc = "StageStatusInfo是保存每个阶段状态信息的实体类", range = "不会为null,StageStatusInfo实体类对象")
	@Override
	public StageParamInfo handleStage(StageParamInfo stageParamInfo) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("------------------表" + collectTableBean.getHbase_name()
			+ "DB文件采集上传阶段开始------------------");
		//1、创建卸数阶段状态信息，更新作业ID,阶段名，阶段开始时间
		StageStatusInfo statusInfo = new StageStatusInfo();
		JobStatusInfoUtil.startStageStatusInfo(statusInfo, collectTableBean.getTable_id(),
			StageConstant.UPLOAD.getCode());
		try {
			//判断是全量采集还是增量采集
			if (UnloadType.ZengLiangXieShu.getCode().equals(collectTableBean.getUnload_type())) {
				//增量采集
				incrementCollect(stageParamInfo);
			} else if (UnloadType.QuanLiangXieShu.getCode().equals(collectTableBean.getUnload_type())) {
				//全量采集
				fullAmountCollect(stageParamInfo);
			} else {
				throw new AppSystemException("表" + collectTableBean.getHbase_name()
					+ "DB文件采集指定的数据抽取卸数方式类型不正确");
			}
			LOGGER.info("------------------表" + collectTableBean.getHbase_name()
				+ "DB文件全量上传阶段成功------------------执行时间为："
				+ (System.currentTimeMillis() - startTime) / 1000 + "，秒");
			JobStatusInfoUtil.endStageStatusInfo(statusInfo, RunStatusConstant.SUCCEED.getCode(), "执行成功");
		} catch (Exception e) {
			JobStatusInfoUtil.endStageStatusInfo(statusInfo, RunStatusConstant.FAILED.getCode(), e.getMessage());
			LOGGER.error("表" + collectTableBean.getHbase_name()
				+ "DB文件采集上传阶段失败：", e);
		}
		//结束给stageParamInfo塞值
		JobStatusInfoUtil.endStageParamInfo(stageParamInfo, statusInfo, collectTableBean
			, AgentType.DBWenJian.getCode());
		return stageParamInfo;
	}

	private void incrementCollect(StageParamInfo stageParamInfo) {
		TableProcessInterface processInterface = null;
		try {
			List<DataStoreConfBean> dataStoreConfBeanList = collectTableBean.getDataStoreConfBean();
			for (DataStoreConfBean dataStoreConfBean : dataStoreConfBeanList) {
				//这边做一个接口多实现，目前只实现传统数据库的增量更新接口
				if (Store_type.DATABASE.getCode().equals(dataStoreConfBean.getStore_type())) {
					//关系型数据库
					processInterface = new MppTableProcessImpl(stageParamInfo.getTableBean(),
						collectTableBean, dataStoreConfBean);
				} else {
					throw new AppSystemException("增量采集目前没有实现其他数据库的增量更新代码");
				}
				for (String readFile : stageParamInfo.getFileArr()) {
					processInterface.parserFileToTable(readFile);
				}
			}
		} catch (Exception e) {
			throw new AppSystemException("表" + collectTableBean.getHbase_name()
				+ "db文件采集增量上传失败", e);
		} finally {
			if (processInterface != null) {
				processInterface.close();
			}
		}
	}

	private void fullAmountCollect(StageParamInfo stageParamInfo) {
		//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
		//TODO Runtime.getRuntime().availableProcessors()此处不能用这个,因为可能同时有多个数据库采集同时进行
		//这里多个文件，使用多线程读取文件，batch进外部数据库。
		ExecutorService executor = null;
		try {
			List<DataStoreConfBean> dataStoreConfBeanList = collectTableBean.getDataStoreConfBean();
			for (DataStoreConfBean dataStoreConfBean : dataStoreConfBeanList) {
				//根据存储类型上传到目的地
				if (Store_type.DATABASE.getCode().equals(dataStoreConfBean.getStore_type())) {
					//数据库类型
					if (IsFlag.Shi.getCode().equals(dataStoreConfBean.getIs_hadoopclient())) {
						//支持外部表的方式
						execSftpToDbServer(dataStoreConfBean, stageParamInfo.getFileArr(), collectTableBean);
					} else if (IsFlag.Fou.getCode().equals(dataStoreConfBean.getIs_hadoopclient())) {
						//不支持外部表的方式
						executor = Executors.newFixedThreadPool(JobConstant.AVAILABLEPROCESSORS);
						exeBatch(dataStoreConfBean, executor, stageParamInfo.getFileArr(),
							stageParamInfo.getTableBean());
					} else {
						throw new AppSystemException("错误的是否标识");
					}
				} else if (Store_type.HIVE.getCode().equals(dataStoreConfBean.getStore_type())) {
					//设置hive的默认类型
					dataStoreConfBean.getData_store_connect_attr().put(StorageTypeKey.database_type,
						DatabaseType.Hive.getCode());
					if (IsFlag.Shi.getCode().equals(dataStoreConfBean.getIs_hadoopclient())) {
						//有hadoop客户端，通过直接上传hdfs，映射外部表的方式进hive
						execHDFSShell(dataStoreConfBean, stageParamInfo.getFileArr(), collectTableBean);
					} else if (IsFlag.Fou.getCode().equals(dataStoreConfBean.getIs_hadoopclient())) {
						//没有hadoop客户端
						executor = Executors.newFixedThreadPool(JobConstant.AVAILABLEPROCESSORS);
						exeBatch(dataStoreConfBean, executor, stageParamInfo.getFileArr(),
							stageParamInfo.getTableBean());
					} else {
						throw new AppSystemException("错误的是否标识");
					}
				} else if (Store_type.HBASE.getCode().equals(dataStoreConfBean.getStore_type())) {
//					LOGGER.warn("DB文件采集数据上传进HBASE没有实现");
					//数据进hbase加载使用BulkLoad加载hdfs上的文件，所以这里必须有hdfs的操作权限，上传hdfs
					execHDFSShell(dataStoreConfBean, stageParamInfo.getFileArr(), collectTableBean);
				} else if (Store_type.SOLR.getCode().equals(dataStoreConfBean.getStore_type())) {
					clearSolrData(dataStoreConfBean);
					//数据进solr
					executor = Executors.newFixedThreadPool(JobConstant.AVAILABLEPROCESSORS);
					exeBatchSolr(dataStoreConfBean, executor, stageParamInfo.getFileArr(),
						stageParamInfo.getTableBean());
				} else if (Store_type.ElasticSearch.getCode().equals(dataStoreConfBean.getStore_type())) {
					LOGGER.warn("DB文件采集数据上传进ElasticSearch没有实现");
				} else if (Store_type.MONGODB.getCode().equals(dataStoreConfBean.getStore_type())) {
					LOGGER.warn("DB文件采集数据上传进MONGODB没有实现");
				} else {
					//TODO 上面的待补充。
					throw new AppSystemException("不支持的存储类型");
				}
			}
		} catch (Exception e) {
			throw new AppSystemException("表" + collectTableBean.getHbase_name()
				+ "db文件采集全量上传失败", e);
		} finally {
			if (executor != null)
				executor.shutdown();
		}
	}

	/**
	 * 追加或者替换清理solr中的数据
	 *
	 * @param dataStoreConfBean 存储层配置
	 */
	private void clearSolrData(DataStoreConfBean dataStoreConfBean) {
		String configPath = FileNameUtils.normalize(Constant.STORECONFIGPATH
			+ dataStoreConfBean.getDsl_name() + File.separator, true);
		//获取solr的配置
		Map<String, String> data_store_connect_attr = dataStoreConfBean.getData_store_connect_attr();
		SolrParam solrParam = new SolrParam();
		solrParam.setSolrZkUrl(data_store_connect_attr.get(StorageTypeKey.solr_zk_url));
		solrParam.setCollection(data_store_connect_attr.get(StorageTypeKey.collection));
		try (ISolrOperator os = SolrFactory.getInstance(JobConstant.SOLRCLASSNAME, solrParam, configPath);
		     SolrClient server = os.getServer()) {
			//判断是追加或者增量先清理表的数据
			if (StorageType.ZhuiJia.getCode().equals(collectTableBean.getStorage_type())) {
				String query = Constant.SDATENAME + ":" + collectTableBean.getEtlDate() +
					" and table-name:" + collectTableBean.getHbase_name();
				server.deleteByQuery(query);
			} else if (StorageType.TiHuan.getCode().equals(collectTableBean.getStorage_type())) {
				String query = "table-name:" + collectTableBean.getHbase_name();
				server.deleteByQuery(query);
			} else {
				throw new AppSystemException("数据进solr不支持增量");
			}
		} catch (Exception e) {
			throw new AppSystemException("清理solr无效数据失败", e);
		}
	}

	/**
	 * batch将数据导入到solr
	 *
	 * @param dataStoreConfBean 存储层配置信息
	 * @param executor          线程池的执行器
	 * @param fileArr           需要采集的DB文件的全路径的数组
	 * @param tableBean         文件的结构化信息
	 */
	private void exeBatchSolr(DataStoreConfBean dataStoreConfBean, ExecutorService executor,
	                          String[] fileArr, TableBean tableBean) {
		long count = 0;
		List<Future<Long>> list = new ArrayList<>();
		try {
			//读取文件进solr
			for (String fileAbsolutePath : fileArr) {
				ReadFileToSolr readFileToSolr = new ReadFileToSolr(fileAbsolutePath, tableBean,
					collectTableBean, dataStoreConfBean);
				//TODO 这个状态是不是可以在这里
				Future<Long> submit = executor.submit(readFileToSolr);
				list.add(submit);
			}
			for (Future<Long> future : list) {
				count += future.get();
			}
			if (count < 0) {
				throw new AppSystemException("数据进" + dataStoreConfBean.getDsl_name() + "异常");
			}
			LOGGER.info("数据成功进" + dataStoreConfBean.getDsl_name() + ",总计进数" + count + "条");
		} catch (Exception e) {
			throw new AppSystemException("多线程读取文件batch进" + dataStoreConfBean.getDsl_name() + "异常", e);
		}
	}

	/**
	 * 上传卸数的文件到hdfs
	 */
	public static void execHDFSShell(DataStoreConfBean dataStoreConfBean, String[] localFiles,
	                                 CollectTableBean collectTableBean) throws Exception {
		//STORECONFIGPATH上传hdfs需要读取的配置文件顶层目录
		String hdfsPath = getUploadHdfsPath(collectTableBean);
		//TODO 需要加一个key为hadoop_user_name的键值对，作为普通属性
		//TODO 需要加一个key为platform的键值对，作为普通属性
		Map<String, String> data_store_connect_attr = dataStoreConfBean.getData_store_connect_attr();
		//TODO 这里需要在部署agent的时候要将目的地属于配置文件的属性部署到这边指定的目录下。
		try (HdfsOperator operator = new HdfsOperator(FileNameUtils.normalize(Constant.STORECONFIGPATH
			+ dataStoreConfBean.getDsl_name() + File.separator, true)
			, data_store_connect_attr.get(StorageTypeKey.platform),
			data_store_connect_attr.get(StorageTypeKey.prncipal_name),
			data_store_connect_attr.get(StorageTypeKey.hadoop_user_name))) {
			//创建hdfs表的文件夹
			if (operator.exists(hdfsPath)) {
				if (!operator.deletePath(hdfsPath)) {
					throw new AppSystemException("删除hdfs文件夹" + hdfsPath + "失败");
				}
			}
			if (!operator.mkdir(hdfsPath)) {
				throw new AppSystemException("创建hdfs文件夹" + hdfsPath + "失败");
			}
			if (SystemUtil.OS_NAME.toLowerCase().contains("windows")) {
				//windows机器上使用api上传hdfs
				for (String localFilePath : localFiles) {
					LOGGER.info("开始上传文件到hdfs");
					if (!operator.upLoad(localFilePath, hdfsPath, true)) {
						throw new AppSystemException("上传文件" + localFilePath + "到hdfs文件夹" + hdfsPath + "失败");
					}
					LOGGER.info("上传文件" + localFilePath + "到hdfs文件夹" + hdfsPath + "结束");
				}
			} else {
				//这里只支持windows和linux，其他机器不支持，linux下使用命令上传hdfs
				StringBuilder fsSql = new StringBuilder();
				fsSql.append("source /etc/profile;source ~/.bashrc;");
				//拼接认证
				//TODO 有认证需要加认证文件key必须为keytab_file，需要加认证用户，key必须为keytab_user
				if (!StringUtil.isEmpty(dataStoreConfBean.getData_store_layer_file().get(StorageTypeKey.keytab_file))) {
					fsSql.append("kinit -k -t ").append(dataStoreConfBean.getData_store_layer_file()
						.get(StorageTypeKey.keytab_file)).append(" ").append(data_store_connect_attr
						.get(StorageTypeKey.keytab_user)).append(" ").append(System.lineSeparator());
				}
				//拼接上传hdfs命令
				for (String localFilePath : localFiles) {
					fsSql.append("hadoop fs -put -f ").append(localFilePath).append(" ").append(hdfsPath)
						.append(" ").append(System.lineSeparator());
				}
				String hdfsShellFile = FileNameUtils.normalize(Constant.HDFSSHELLFILE + collectTableBean
					.getHbase_name() + ".sh", true);
				//写脚本文件
				FileUtil.createFile(hdfsShellFile, fsSql.toString());
				String command = "sh " + hdfsShellFile;
				LOGGER.info("开始运行(HDFS上传)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + command);
				final CommandLine cmdLine = new CommandLine("sh");
				cmdLine.addArgument(hdfsShellFile);
				DefaultExecutor executor = new DefaultExecutor();
				ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
				executor.setWatchdog(watchdog);
				executor.execute(cmdLine);
				LOGGER.info("上传文件到" + hdfsPath + "结束");
			}
		}
	}

	/**
	 * 执行sftp程序将文件上传到服务器所在机器
	 */
	public static void execSftpToDbServer(DataStoreConfBean dataStoreConfBean, String[] localFiles,
	                                      CollectTableBean collectTableBean) {
		//XXX 这个map需要额外有数据库所在机器的用户名、密码、ip地址、sftp端口(没有取默认值 22)
		Map<String, String> data_store_connect_attr = dataStoreConfBean.getData_store_connect_attr();
		try (SftpOperate sftpOperate = new SftpOperate(
			new SFTPDetails(data_store_connect_attr.get(StorageTypeKey.sftp_host),
				data_store_connect_attr.get(StorageTypeKey.sftp_user), data_store_connect_attr.
				get(StorageTypeKey.sftp_pwd), data_store_connect_attr.get(StorageTypeKey.sftp_port)),
			0)) {
			// 上传文件
			LOGGER.info("==========文件上传开始=========");
			String database_type = data_store_connect_attr.get(StorageTypeKey.database_type);
			String targetPath;
			if (DatabaseType.Oracle10g.getCode().equals(database_type) ||
				DatabaseType.Oracle9i.getCode().equals(database_type)) {
				//oracle数据库采集外部表文件所在目录
				targetPath = data_store_connect_attr.get(StorageTypeKey.external_root_path);
				//使用Oracle则不能删除再创建
				String mkdirShell = "mkdir -p " + targetPath;
				sftpOperate.execCommandByJSch(mkdirShell);
				LOGGER.info("==========上传文件的目标地址,创建目录：targetDir==========" + targetPath);
				//赋权限
				String chmodShell = "chmod 777 " + targetPath;
				sftpOperate.execCommandByJSch(chmodShell);
				//上传大字段文件到服务器所在目录
				//XXX 传统数据库目前只实现了oracle数据库的外部表支持大字段
				uploadLobsFileToOracle(localFiles[0], sftpOperate, targetPath, collectTableBean.getHbase_name());
			} else if (DatabaseType.Postgresql.getCode().equals(database_type)) {
				//获取需要上传到服务器的目录
				targetPath = getUploadServerPath(collectTableBean,
					data_store_connect_attr.get(StorageTypeKey.external_root_path));
				if (FileUtil.isSysDir(targetPath)) {
					throw new AppSystemException("未知的异常或配置导致需要删除的目录是系统目录");
				}
				String deleteDirShell = "rm -rf " + targetPath;
				sftpOperate.execCommandByJSch(deleteDirShell);
				LOGGER.info("==========上传文件的目标地址,目录存在先删除：targetDir==========" + deleteDirShell);
				String mkdirShell = "mkdir -p " + targetPath;
				sftpOperate.execCommandByJSch(mkdirShell);
				LOGGER.info("==========上传文件的目标地址,创建目录：targetDir==========" + mkdirShell);
			} else {
				//其他支持外部表的数据库 TODO 这里的逻辑后面可能需要不断补充
				throw new AppSystemException(dataStoreConfBean.getDsl_name() + "数据库暂不支持外部表的形式入库");
			}
			//上传数据文件
			for (String localFilePath : localFiles) {
				File file = new File(localFilePath);
				long fileSize = file.length();
				LOGGER.info("上传文件本地文件" + localFilePath + "到服务器" + targetPath);
				sftpOperate.channelSftp.put(localFilePath, targetPath, new FileProgressMonitor(fileSize),
					ChannelSftp.OVERWRITE);
			}
			LOGGER.info("上传数据完成");
		} catch (Exception e) {
			throw new AppSystemException("上传文件失败", e);
		}
	}

	public static void uploadLobsFileToOracle(String absolutePath, SftpOperate sftpOperate,
	                                          String targetDir, String unload_hbase_name) throws Exception {
		String LOBs = FileNameUtils.getFullPath(absolutePath) + "LOBS" + File.separator;
		String[] fileNames = new File(LOBs).list();
		//XXX 大字段文件这里目前只支持oracle数据库
		if (SystemUtil.OS_NAME.toLowerCase().contains("windows")) {//卸数的agent在linux系统下，使用命令压缩文件
			if (fileNames != null && fileNames.length > 0) {
				for (String f : fileNames) {
					//传输到服务器上
					sftpOperate.channelSftp.put(LOBs + f, targetDir, ChannelSftp.OVERWRITE); // 代码段2
				}
			}
		} else {
			if (fileNames != null && fileNames.length > 0) {
				String zipFileName = LOBs + unload_hbase_name + ".zip";
				//防止重跑压缩报错，服务器上的LOBs文件没有被删
				File zip = new File(zipFileName);
				if (zip.exists()) {
					if (!zip.delete()) {
						throw new AppSystemException("删除压缩文件失败");
					}
				}
				String lobs_file = "find " + targetDir + " -name \"LOBs_" + unload_hbase_name
					+ "_*\" | xargs rm -rf 'LOBs_" + unload_hbase_name + "_*'";
				if (FileUtil.isSysDir(targetDir)) {
					throw new AppSystemException("异常导致需要删除的目录是系统目录");
				}
				sftpOperate.execCommandByJSch(lobs_file);
				//find /home/hyshf/xxxx/LOBs/ -name 'LOBs_xxxx*' -print | zip -qj /home/hyshf/xxxx/LOBs/xxxx.zip -@
				//开始压缩上传文件到数据库服务器，解压
				String zip_shell = "find " + LOBs + " -name '*' -print | zip -qj " + zipFileName + " -@";
				sftpOperate.executeLocalShell(zip_shell);
				sftpOperate.channelSftp.put(zipFileName, targetDir, ChannelSftp.OVERWRITE); // 代码段2
				sftpOperate.execCommandByJSch("unzip " + targetDir
					+ unload_hbase_name + ".zip" + " -d " + targetDir);
				//删除zip压缩包
				sftpOperate.execCommandByJSch("rm -rf " + targetDir + unload_hbase_name + ".zip");
			}
		}
	}

	/**
	 * 使用batch方式进数
	 */
	private void exeBatch(DataStoreConfBean dataStoreConfBean, ExecutorService executor,
	                      String[] localFiles, TableBean tableBean) {
		long count = 0;
		List<Future<Long>> list = new ArrayList<>();
		String todayTableName = collectTableBean.getHbase_name() + "_" + 1;
		DatabaseWrapper db = null;
		try {
			//获取连接
			db = ConnectionTool.getDBWrapper(dataStoreConfBean.getData_store_connect_attr());
			//备份表上次执行进数的数据
			backupToDayTable(todayTableName, db);
			//先创建表，再多线程batch数据入库，根据数据保留天数做相应调整，成功则删除最早一次进数保留的数据
			createTodayTable(tableBean, todayTableName, dataStoreConfBean, db);
			for (String fileAbsolutePath : localFiles) {
				ReadFileToDataBase readFileToDataBase = new ReadFileToDataBase(fileAbsolutePath, tableBean,
					collectTableBean, dataStoreConfBean);
				//TODO 这个状态是不是可以在这里
				Future<Long> submit = executor.submit(readFileToDataBase);
				list.add(submit);
			}
			for (Future<Long> future : list) {
				if (future.get() < 0) {
					throw new AppSystemException("数据Batch提交到库" + dataStoreConfBean.getDsl_name() + "异常");
				}
				count += future.get();
			}
			//根据表存储期限备份每张表存储期限内进数的数据
			backupPastTable(collectTableBean, db);
			LOGGER.info("数据成功进入库" + dataStoreConfBean.getDsl_name() + "下的表" + collectTableBean.getHbase_name()
				+ ",总计进数" + count + "条");
		} catch (Exception e) {
			if (db != null) {
				//执行失败，恢复上次进数的数据
				recoverBackupToDayTable(todayTableName, db);
			}
			throw new AppSystemException("多线程读取文件batch进库" + dataStoreConfBean.getDsl_name() + "下的表"
				+ collectTableBean.getHbase_name() + "异常", e);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	/**
	 * 创建当天执行成功进数的表
	 *
	 * @param tableBean         表结构信息
	 * @param todayTableName    当天表进数成功的表名
	 * @param dataStoreConfBean 存储目的地配置信息
	 * @param db                数据库连接方式
	 */
	public static void createTodayTable(TableBean tableBean, String todayTableName, DataStoreConfBean dataStoreConfBean,
	                                    DatabaseWrapper db) {
		List<String> columns = StringUtil.split(tableBean.getColumnMetaInfo(), Constant.METAINFOSPLIT);
		List<String> types = DataTypeTransform.tansform(StringUtil.split(tableBean.getColTypeMetaInfo(),
			Constant.METAINFOSPLIT), dataStoreConfBean.getDsl_name());
		//拼接建表语句
		StringBuilder sql = new StringBuilder(120); //拼接创表sql语句
		if (db.getDbtype() == Dbtype.TERADATA) {
			sql.append("CREATE MULTISET TABLE ");
		} else {
			sql.append("CREATE TABLE ");
		}
		sql.append(todayTableName);
		sql.append("(");
		for (int i = 0; i < columns.size(); i++) {
			sql.append(columns.get(i)).append(" ").append(types.get(i)).append(",");
		}
		//将最后的逗号删除
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		//执行建表语句
		db.execute(sql.toString());
	}

	/**
	 * 获取上传到hdfs的文件夹路径
	 */
	public static String getUploadHdfsPath(CollectTableBean collectTableBean) {
		return FileNameUtils.normalize(JobConstant.PREFIX + File.separator + collectTableBean.getDatabase_id()
			+ File.separator + collectTableBean.getHbase_name() + File.separator, true);
	}

	/**
	 * 获取上传到服务器所在机器的目录
	 */
	static String getUploadServerPath(CollectTableBean collectTableBean, String rootPath) {
		return FileNameUtils.normalize(rootPath + collectTableBean.getDatabase_id()
			+ File.separator + collectTableBean.getHbase_name() + File.separator, true);
	}

	@Override
	public int getStageCode() {
		return StageConstant.UPLOAD.getCode();
	}
}
