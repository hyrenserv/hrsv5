package hrds.producer.string.file.dirString;

import com.alibaba.fastjson.JSONObject;
import hrds.agent.trans.biz.filecontentstream.FileContentStreamInfo;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.Constant;
import hrds.commons.utils.MapDBHelper;
import hrds.producer.common.CusClassLoader;
import hrds.producer.common.FileDataValidator;
import hrds.producer.common.JobParamsEntity;
import hrds.producer.common.KafkaProducerWorker;
import jodd.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirListenerReaderString implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	public static final String folder = System.getProperty("user.dir");
	private ExecutorService executor;
	public boolean reading = true;
	private String jobId;
	private MapDBHelper mapDBHelper;//创建mapDB数据库
	private ConcurrentMap<String, String> htMap;//读取位置信息存储表
	private ConcurrentMap<String, String> htMapThread;//线程安全维护表
	private JSONObject json;
	private CountDownLatch countDownLatch;

	public DirListenerReaderString(String jobId, JSONObject json, CountDownLatch countDownLatch) {

		executor = FileContentStreamInfo.mapExec.get(jobId);
		if (executor != null && !executor.isShutdown()) {
			FileContentStreamInfo.mapStop.put(jobId, false);
			executor.shutdown();
			try {
				executor.awaitTermination(1, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				logger.error(e);
			}
			mapDBHelper = FileContentStreamInfo.mapMapDb.get(jobId);
			if (mapDBHelper != null) {
				mapDBHelper.close();
			}
		}
		this.json = json;
		this.jobId = jobId;
		FileContentStreamInfo.mapStop.put(jobId, true);
		this.mapDBHelper = new MapDBHelper(folder + File.separator + "mapDB" + File.separator + jobId, "Map.db");
		FileContentStreamInfo.mapMapDb.put(jobId, mapDBHelper);
		this.htMap = mapDBHelper.htMap("FileParm", 24 * 60);
		this.htMapThread = mapDBHelper.htMap("FileThreadSign", 24 * 60);
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {

		try {
			JSONObject jsonConf = json.getJSONObject("sdm_receive_conf");
			String readType = jsonConf.getString("read_type");//读取类型0：单次读取 1：实时读取
			boolean readeOpinion;
			readeOpinion = IsFlag.Fou != IsFlag.ofEnumByCode(readType);
			String file_handle = jsonConf.getString("file_handle");//按行读取时整行判断类
			FileDataValidator fileDataValidator = null;
			if (StringUtil.isNotBlank(file_handle)) {
				CusClassLoader classLoader = new CusClassLoader();
				Class<?> clazz = classLoader.getURLClassLoader().loadClass(file_handle);
				fileDataValidator = (FileDataValidator) clazz.newInstance();
			}
			String charset = jsonConf.getString("code");//编码类型
			String readMode = jsonConf.getString("read_mode");//1按行读取，2按对象读取
			this.executor = Executors.newFixedThreadPool(jsonConf.getIntValue("thread_num"));//执行线程数
			FileContentStreamInfo.mapExec.put(jobId, executor);
			//将producer的配置写入文件
			ProducerOperatorDirString producerOperator = new ProducerOperatorDirString();
			//加载Producer配置
			JobParamsEntity jobParams = producerOperator.getMapParam(json, jobId);
			String filePath = jsonConf.getString("ra_file_path");//监控文件
			String pathRename = filePath + File.separator + "rename";//转移目录
			File fileRename = new File(pathRename);
			if (!fileRename.exists()) {
				if (!fileRename.mkdirs()) {
					throw new BusinessException("创建目录失败：" + pathRename);
				}
			}

			String isDataPartition = jsonConf.getString("is_data_partition");
			String isObj = jsonConf.getString("is_obj");
			jobParams.setIsObj(isObj);
			//单行数据分隔is_data_partition 0是 1否
			if (isDataPartition.equals("0")) {
				jobParams.setSdmDatelimiter(jsonConf.getString("sdm_dat_delimiter"));
			}
			htMapThread.clear();//启动时先清空

			String matchRule = jsonConf.getString("file_match_rule");//文件匹配
			final Pattern pattern;
			if (StringUtils.isBlank(matchRule)) {
				pattern = null;
			} else {
				pattern = Pattern.compile(matchRule);
			}
			FilenameFilter fileNameFilter = (dir, name) -> {
				if (pattern != null) {
					Matcher matcher = pattern.matcher(name);
					return matcher.matches() && !htMapThread.containsKey(name);
				} else {
					return !htMapThread.containsKey(name);
				}
			};
			while (this.reading) {
				this.reading = readeOpinion;
				if (!FileContentStreamInfo.mapStop.get(jobId)) {
					break;
				}
				File root = new File(filePath);
				File[] files = root.listFiles(fileNameFilter);
				if (files != null && files.length > 0) {
					for (File file : files) {
						if (!FileContentStreamInfo.mapStop.get(jobId)) {
							break;
						}
						if (!file.isDirectory() && !htMapThread.containsKey(file.getName())) {
							String fileName = file.getName();
							if (!htMap.containsKey(fileName)) {
								ReaderFileString readerFile = new ReaderFileString(mapDBHelper, htMap,
									htMapThread, readMode, jobParams, file,
									fileRename, charset, null, fileDataValidator);
								executor.submit(readerFile);
								htMapThread.put(fileName, "start");
							} else if (!htMap.get(fileName).equals("all")) {
								String position = jsonConf.getString("file_initposition");
								if (position.equals("0")) {
									ReaderFileString readerFile = new ReaderFileString(mapDBHelper, htMap,
										htMapThread, readMode, jobParams, file,
										fileRename, charset, null, fileDataValidator);
									executor.submit(readerFile);
									htMapThread.put(fileName, "start");
								} else if (position.equals("-2")) {
									String beforeLine = htMap.get(fileName);
									ReaderFileString readerFile = new ReaderFileString(mapDBHelper, htMap,
										htMapThread, readMode, jobParams, file,
										fileRename, charset, beforeLine, fileDataValidator);
									executor.submit(readerFile);
									htMapThread.put(fileName, "start");
								} else {
									logger.error("此采集方式只支持重新开始和从上次位置开始！！！");
								}
							} else {
								try {
									File fileOld = new File((fileRename.getAbsolutePath() + File.separator + file.getName()));
									if (FileUtils.directoryContains(fileRename, fileOld)) {
										if (!fileOld.delete()) {
											throw new BusinessException("删除旧文件失败！！" + fileOld.getPath());
										}
									}
									FileUtils.moveToDirectory(file, fileRename, true);
								} catch (IOException e) {
									logger.error("文件移动失败！！！", e);
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ignored) {
					}
				}
			}
			while (!htMapThread.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
			mapDBHelper.close();
			KafkaProducer<String, String> producer = jobParams.getProducerString();
			JSONObject json = getParamJson2(jobParams.getListColumn(), isDataPartition);
			KafkaProducerWorker kafkaProducerWorker = new KafkaProducerWorker();
			kafkaProducerWorker.sendToKafka(filePath, producer, json.toString(), jobParams.getTopic(), jobParams.getCustomerPartition(),
				jobParams.getBootstrapServers(), jobParams.getSync());
			producer.close();
			if (this.countDownLatch != null) {
				this.countDownLatch.countDown();
			}
		} catch (Exception e) {
			logger.error("文件流启动失败！！！", e);
			throw new BusinessException("文件流启动失败:" + e.getMessage());
		}

	}

	private JSONObject getParamJson2(List<String> listColumn, String is_data_partition) {

		JSONObject json = new JSONObject();
		if (IsFlag.Fou == IsFlag.ofEnumByCode(is_data_partition)) {
			for (String column : listColumn) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
					json.put(column.split("`")[0], Constant.STREAM_HYREN_END);
				}
			}
		} else {
			json.put("line", Constant.STREAM_HYREN_END);
		}
		return json;
	}

}