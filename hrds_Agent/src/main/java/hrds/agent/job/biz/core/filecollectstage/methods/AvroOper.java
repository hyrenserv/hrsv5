package hrds.agent.job.biz.core.filecollectstage.methods;

import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.summary.TextRankSentence;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.MD5Util;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.AvroBean;
import hrds.agent.job.biz.bean.FileCollectParamBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.utils.PropertyParaUtil;
import hrds.agent.trans.biz.unstructuredfilecollect.FileCollectJob;
import hrds.commons.codes.DataSourceType;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.AppSystemException;
import hrds.commons.hadoop.readconfig.ConfigReader;
import hrds.commons.utils.ReadFileUtil;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DocClass(desc = "卸数生成Avro文件到本地目录的程序", author = "zxz", createdate = "2019/11/14 15:32")
public class AvroOper {
	//打印日志
	private static final Logger logger = LogManager.getLogger();
	//写Avro文件的结构
	private static final String SCHEMA_JSON = "{\"type\": \"record\",\"name\": \"SmallFilesTest\", " + "\"fields\": [" + "{\"name\":\""
			+ "file_name" + "\",\"type\":\"string\"}," + "{\"name\":\"" + "file_scr_path" + "\", \"type\":\"string\"}," + "{\"name\":\""
			+ "file_size" + "\",\"type\":\"string\"}," + "{\"name\":\"" + "file_time" + "\",\"type\":\"string\"}," + "{\"name\":\""
			+ "file_summary" + "\",\"type\":\"string\"}," + "{\"name\":\"" + "file_text" + "\",\"type\":\"string\"}," + "{\"name\":\""
			+ "file_md5" + "\",\"type\":\"string\"}," + "{\"name\":\"" + "file_avro_path" + "\",\"type\":\"string\"}," + "{\"name\":\""
			+ "file_avro_block" + "\",\"type\":\"long\"}," + "{\"name\":\"" + "is_big_file" + "\",\"type\":\"string\"}," + "{\"name\":\""
			+ "file_contents" + "\",\"type\":\"bytes\"}," + "{\"name\":\"" + "uuid" + "\",\"type\":\"string\"}," + "{\"name\":\""
			+ "is_increasement" + "\",\"type\":\"string\"}," + "{\"name\":\"" + "is_cache" + "\",\"type\":\"string\"}" + "]}";
	//Avro文件的SCHEMA
	private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);
	//文件采集传递参数的实体
	private final FileCollectParamBean fileCollectParamBean;
	//文件采集的监听器
	private final CollectionWatcher collectionWatcher;
	//大文件文件夹路径
	private static final String BIGFILENAME = "bigFiles";
	//文件上传到hdfs文件的路径
	private final String fileCollectHdfsPath;
	//大文件上传到hdfs上的路径
	private final String bigFileCollectHdfsPath;
	//当前线程的消费队列
	private final ArrayBlockingQueue<String> queue;
	//mapDB对象
	private final ConcurrentMap<String, String> fileNameHTreeMap;
	//最后一个卸数文件的标识
	public static final long LASTELEMENT = 0L;

	public AvroOper(FileCollectParamBean fileCollectParamBean, ConcurrentMap<String, String> fileNameHTreeMap) {
		this.fileCollectParamBean = fileCollectParamBean;
		this.fileNameHTreeMap = fileNameHTreeMap;
		//XXX 下面这个方法改造，不能直接对数据库进行操作
		collectionWatcher = new CollectionWatcher(fileCollectParamBean);
		fileCollectHdfsPath = FileNameUtils.normalize(JobConstant.PREFIX + File.separator +
				DataSourceType.DCL.getCode() + File.separator + fileCollectParamBean.getFcs_id() + File.separator
				+ fileCollectParamBean.getFile_source_id() + File.separator, true);
		bigFileCollectHdfsPath = FileNameUtils.normalize(fileCollectHdfsPath +
				File.separator + BIGFILENAME + File.separator, true);
		queue = FileCollectJob.mapQueue.get(fileCollectParamBean.getFile_source_id());
	}

	public CollectionWatcher getCollectionWatcher() {
		return collectionWatcher;
	}

	/**
	 * 将一个文件追加到avro
	 *
	 * @param avroHdfsPath 追加到Avro文件的路径，可以是LOCAL路径也可以是HDFS路径
	 * @param writer       由于写一个Avro需要用一个DataFileWriter对象，故传入该对象，为同时写入多个小文件。
	 * @param isSelect     是否增量
	 */
	private long putOneFile2Avro(String filePath, long avroFileTotalSize, String avroHdfsPath,
								 DataFileWriter<Object> writer, boolean isSelect) throws IOException {
		GenericRecord record = new GenericData.Record(SCHEMA);
		try {
			long syncBlock = writer.sync();
			File file = new File(filePath);
			String fileName = file.getName();
			//TODO 这里之前有一个查询is_cache的程序，不知道干啥的，占时去掉
			record.put("is_cache", "");
			//文件需要查询是否增量
			if (isSelect) {
				//增量文件说明mapDB里面可以查询到数据，使用原有的uuid
				record.put("uuid", JSONObject.parseObject(fileNameHTreeMap.get(filePath)).getString("uuid"));
				record.put("is_increasement", IsFlag.Shi.getCode());
			} else {
				record.put("uuid", UUID.randomUUID().toString());
				record.put("is_increasement", IsFlag.Fou.getCode());
			}
			record.put("file_name", fileName);
			record.put("file_scr_path", filePath);
			record.put("file_size", String.valueOf(file.length()));
			record.put("file_time", String.valueOf(file.lastModified()));
			if (JobConstant.FILECHANGESTYPEMD5) {
				record.put("file_md5", MD5Util.md5File(filePath));
			} else {
				record.put("file_md5", String.valueOf(new File(filePath).lastModified()));
			}
			//记录该文件在Avro中的块号，为今后方便查找该文件
			record.put("file_avro_block", syncBlock);
			if (file.length() > JobConstant.THRESHOLD_FILE_SIZE) {
				String bigFileHdfs = bigFileCollectHdfsPath + fileName;
				//如果是大文件，就是大文件所在的路径加文件名称
				record.put("file_avro_path", bigFileHdfs);
				record.put("is_big_file", IsFlag.Shi.getCode());
				//XXX 如果是大文件，不记录流，记录文件在hdfs上的实际位置，大文件不卸数，直接由源文件上传hdfs
				record.put("file_contents", ByteBuffer.wrap(bigFileHdfs.getBytes()));
				record.put("file_summary", "");
				record.put("file_text", "");
				//大文件只记录文件基本信息，这里记做1 byte
				avroFileTotalSize++;
				//大文件单独写一个队列，用于上传大文件到hdfs
				putIntoQueue(file.getAbsolutePath(), fileCollectParamBean, false, true);
			} else {
				//小文件直接记录卸数的位置
				record.put("file_avro_path", fileCollectHdfsPath + FileNameUtils.getName(avroHdfsPath));
				// 记录文件类型，判断是否为大小文件
				record.put("is_big_file", IsFlag.Fou.getCode());
				//TODO 异步方法不能用，用其他方式
				record.put("file_contents", ByteBuffer.wrap(FileUtils.readFileToByteArray(file)));
				String text = ReadFileUtil.file2String(file);
				try {
					record.put("file_summary", normalizeSummary(TextRankSentence.
							getTopSentenceList(text, JobConstant.SUMMARY_VOLUMN).toString()));
				} catch (OutOfMemoryError e) {
					record.put("file_summary", file.getName());
				}
				//文件读出的文本
				text = normalizeText(text);
				record.put("file_text", text);
				avroFileTotalSize += file.length();
			}
			writer.append(record);
			/*
			TODO 这里判断是图片文件，调用神经网络建立索引，索引需要记录在Avro中的块号、avro文件的路径、文件名使用`做分隔符
						List<String> list = FileTypeUtil.getTypeFileList(FileTypeUtil.TuPian);
						if( list.contains(FileUtil.getFileExtName(file.getName()).replace(".", "")) ) {//判断只有图片建索引
							String indexName = sync_block + "`" + avroHdfsPath + "`" + file_name;
							RpcIndex index = new RpcIndex(indexName);
							String localPictureIndex = index.localPictureIndex(file);
							if( "0".equals(localPictureIndex) ) {
								throw new BusinessException("以图搜图建立索引失败");
							}
						}
			*/
			return avroFileTotalSize;
		} catch (IOException e) {
			logger.error("Failed to putOneFile2Avro ... " + filePath, e);
			throw e;
		}
	}

	private static String replaceMatcher(String str, String matcher, String replaceStr) {

		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile(matcher);
			Matcher m = p.matcher(str);
			dest = m.replaceAll(replaceStr);
		}
		return dest;
	}

	private static final int LIMIT_LENGTH_IN_SOLR_FIELD = 30000;

	private String normalizeText(String text) {
		text = replaceMatcher(text, "(\r|\n){2,}", "<br><br>");
		text = replaceMatcher(text, "\r|\n", "<br>");
		text = replaceMatcher(text, "\t", "");
		if (text.length() > LIMIT_LENGTH_IN_SOLR_FIELD) {
			text = text.substring(0, LIMIT_LENGTH_IN_SOLR_FIELD);
		}
		return text;
	}

	private String normalizeSummary(String text) {
		text = replaceMatcher(text, "\\s*|\t|\r|\n", "");
		if (text.length() > LIMIT_LENGTH_IN_SOLR_FIELD) {
			text = text.substring(0, LIMIT_LENGTH_IN_SOLR_FIELD);
		}
		return text;
	}

	/**
	 * @param files 所有需要写入Avro文件的List<file>
	 */
	public void putAllFiles2Avro(List<String> files, boolean isSelect, boolean isLast) {
		String unLoadPath = fileCollectParamBean.getUnLoadPath();
		//文件采集卸数到本地的目录不存在要创建
		File file2 = new File(unLoadPath);
		if (!file2.exists()) {
			boolean mkdirs = file2.mkdirs();
			if (!mkdirs) {
				throw new AppSystemException("创建文件夹" + file2.getAbsolutePath() + "失败");
			}
		}
		//生成avro文件，每生成一个，立即放入队列，交给消费线程去处理
		DataFileWriter<Object> writer = null;
		OutputStream outputStream = null;
		String avroFileAbsolutionPath = "";
		long avroFileTotalSize = 0L;
		try {
			if (files.size() > 0) {
				for (String filePath : files) {
					//控制avro文件大小第一次运行或者avro文件大小已经达到阈值会进入此条件
					if (avroFileTotalSize > JobConstant.SINGLE_AVRO_SIZE || avroFileTotalSize == 0L) {
						avroFileTotalSize = 0L;
					/*关闭writer和outputStream，每写一个SINGLE_AVRO_SIZE大小的avro
					文件需要重新创建一个writer和outputStream对象。*/
						if (writer != null) {
							writer.close();
						}
						if (outputStream != null) {
							outputStream.close();
						}
						//当avro文件达到阈值，且不是第一次（即已经生成了一个SINGLE_AVRO_SIZE大小的avro）会进入此块
						if (!StringUtil.isBlank(avroFileAbsolutionPath)) {
							//将信息放入队列
							putIntoQueue(avroFileAbsolutionPath, fileCollectParamBean, false, false);
						}
						//生成的avro的本地路径
						avroFileAbsolutionPath = unLoadPath + "avro_" + UUID.randomUUID();
						logger.info("Ready to generate avro file: " + avroFileAbsolutionPath);
						outputStream = new FileOutputStream(avroFileAbsolutionPath);
						writer = new DataFileWriter<>(new GenericDatumWriter<>());
						writer.setCodec(CodecFactory.snappyCodec());
						writer.create(SCHEMA, outputStream);
					}
					avroFileTotalSize = putOneFile2Avro(filePath, avroFileTotalSize, avroFileAbsolutionPath,
							writer, isSelect);
				}
				//是最后一批文件，则给结束标识
				//avro文件写完之后就立刻将该条avro文件信息放入队列，交于消费线程处理
				putIntoQueue(avroFileAbsolutionPath, fileCollectParamBean, isLast, false);
			}
		} catch (IOException e) {
			logger.error("Failed to create Avro File...", e);
			throw new AppSystemException(e.getMessage());
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void putIntoQueue(String avroFileAbsolutionPath, FileCollectParamBean
			fileCollectParamBean, boolean isLastElement, boolean isBigFile) {
		try {
			JSONObject queueJb = new JSONObject();
			queueJb.put("source_id", fileCollectParamBean.getSource_id());
			queueJb.put("agent_id", fileCollectParamBean.getAgent_id());
			queueJb.put("sys_date", fileCollectParamBean.getSysDate());
			queueJb.put("collect_set_id", fileCollectParamBean.getFcs_id());
			queueJb.put("avroFileAbsolutionPath", avroFileAbsolutionPath);
			queueJb.put("job_rs_id", collectionWatcher.getJob_rs_id());
			if (isBigFile) {
				queueJb.put("isBigFile", IsFlag.Shi.getCode());
				queueJb.put("fileCollectHdfsPath", bigFileCollectHdfsPath);
			} else {
				queueJb.put("isBigFile", IsFlag.Fou.getCode());
				queueJb.put("fileCollectHdfsPath", fileCollectHdfsPath);
			}

			//任务结束与否的标识应该看最后一个任务队列元素是否完成
			if (isLastElement) {
				//是最后一个
				queueJb.put("watcher_id", LASTELEMENT);
			} else {
				//不是最后一个
				queueJb.put("watcher_id", System.currentTimeMillis());
			}
			String queueJbString = queueJb.toJSONString();
			logger.info("Put queueJbString: " + queueJbString + " into queue!");
			//avro文件已经上传到生成，上传，删除本地，均成功，则将此信息放入到队列中，交给消费线程做处理
			queue.put(queueJbString);
		} catch (Exception e) {
			logger.error("Failed to upload the avro file or put information into queue ...", e);
			throw new AppSystemException(e.getMessage());
		}
	}

	/**
	 * 把avro文件数据读出 为实体（hdfs）
	 *
	 * @param avroFilePath avro文件在hdfs上的路径
	 * @return avroBean实体的list
	 */
	public static List<AvroBean> getAvroBeans(Path avroFilePath) throws IOException {

		List<AvroBean> avroBeans = new ArrayList<>();
		int i = 0;
		FileSystem fs = null;
		InputStream is = null;
		DataFileStream<Object> reader = null;
		try {
			if (JobConstant.FILE_COLLECTION_IS_WRITE_HADOOP) {
				//TODO
				fs = FileSystem.get(ConfigReader.getConfiguration(
						System.getProperty("user.dir") + File.separator + "conf" + File.separator,
						PropertyParaUtil.getString("platform", ConfigReader.PlatformType.normal.toString()),
						PropertyParaUtil.getString("principle.name", "admin@HADOOP.COM"),
						PropertyParaUtil.getString("HADOOP_USER_NAME", "hyshf")
				));
				is = fs.open(avroFilePath);
			} else {
				is = new FileInputStream(avroFilePath.toString());
			}
			reader = new DataFileStream<>(is, new GenericDatumReader<>());
			AvroBean avroBean;
			for (Object obj : reader) {
				GenericRecord r = (GenericRecord) obj;
				avroBean = new AvroBean();
				if (i % 1000 == 0) {
					logger.info("[info]读取第" + i + "个文件");
				}
				avroBean.setUuid(r.get("uuid").toString());
				avroBean.setFile_name(r.get("file_name").toString());
				avroBean.setFile_scr_path(r.get("file_scr_path").toString());
				avroBean.setFile_size(r.get("file_size").toString());
				avroBean.setFile_time(r.get("file_time").toString());
				avroBean.setFile_summary(r.get("file_summary").toString().replace(" ", ""));
				avroBean.setFile_text(r.get("file_text").toString());
				avroBean.setFile_md5(r.get("file_md5").toString());
				avroBean.setFile_avro_path(r.get("file_avro_path").toString());
				avroBean.setFile_avro_block(r.get("file_avro_block").toString());
				avroBean.setIs_big_file(r.get("is_big_file").toString());
				avroBean.setIs_increasement(r.get("is_increasement").toString());
				avroBean.setIs_cache(r.get("is_cache").toString());
				// 将一个bean加入list中去。
				avroBeans.add(avroBean);
				i++;
			}
			logger.info("读取 avro 文件完毕：一共读取了 " + i + " 个文件");

		} catch (Exception e) {
			logger.error("Failed to getAvroBeans...", e);
			throw e;
		} finally {
			try {
				if (null != reader)
					reader.close();
				if (null != is)
					is.close();
				if (null != fs)
					fs.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return avroBeans;
	}

}
