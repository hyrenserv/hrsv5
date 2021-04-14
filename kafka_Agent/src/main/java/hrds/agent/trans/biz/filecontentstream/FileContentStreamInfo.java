package hrds.agent.trans.biz.filecontentstream;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import hrds.agent.trans.biz.util.WriterParam;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.codes.ExecuteWay;
import hrds.commons.codes.IsFlag;
import hrds.commons.utils.MapDBHelper;
import hrds.producer.avro.file.bigFile.SplitFileToKafka;
import hrds.producer.avro.file.dir.DirListenerReader;
import hrds.producer.avro.file.file.FileReadAllRound;
import hrds.producer.string.file.bigFile.SplitFileToKafkaString;
import hrds.producer.string.file.dirString.DirListenerReaderString;
import hrds.producer.string.file.file.FileReadAllroundString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

@DocClass(desc = "文件内容流kafka启动信息", author = "dhw", createdate = "2021/4/13 14:48")
public class FileContentStreamInfo extends AgentBaseAction {

	private static final Logger logger = LogManager.getLogger();
	public static volatile ConcurrentMap<String, ExecutorService> mapExec = new ConcurrentHashMap<>();
	public static volatile ConcurrentMap<String, MapDBHelper> mapMapDb = new ConcurrentHashMap<>();
	public static volatile ConcurrentMap<String, Thread> mapJob = new ConcurrentHashMap<>();
	public static volatile ConcurrentMap<String, ExecutorService> mapExecS = new ConcurrentHashMap<>();
	public volatile static ConcurrentMap<String, Boolean> mapStop = new ConcurrentHashMap<>();

	@Method(desc = "数据消息流kafka启动", logicStep = "")
	@Param(name = "kafkaParams", desc = "数据消息流kafka启动参数", range = "无限制")
	public void execute(String kafkaParams) throws InterruptedException {
		JSONObject json = JSONObject.parseObject(kafkaParams);
		JSONObject jsonConf = json.getJSONObject("sdm_receive_conf");
		String jobId = jsonConf.getString("sdm_receive_id");
		String run_way = jsonConf.getString("run_way");//启动方式
		if (ExecuteWay.AnShiQiDong == ExecuteWay.ofEnumByCode(run_way)) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			long startTime = System.currentTimeMillis();
			logger.info("beginTime：" + startTime);
			String serializerType = json.getJSONObject("kafka_params").getString("value.serializer");
			String readType = jsonConf.getString("file_readtype");
			//字节读取
			if (IsFlag.Shi == IsFlag.ofEnumByCode(readType)) {
				WriterParam writerParam = new WriterParam();
				writerParam.writeProducerParam(jobId, json.toString());
				if ("Avro".equals(serializerType)) {
					SplitFileToKafka splitFileToKafka = new SplitFileToKafka(jobId, json, null);
					executor.execute(splitFileToKafka);
				} else {
					SplitFileToKafkaString splitFileToKafka = new SplitFileToKafkaString(jobId, json, null);
					executor.execute(splitFileToKafka);
				}
			}
			//内容读取
			else {
				String fileType = jsonConf.getString("monitor_type");
				if ("Avro".equals(serializerType)) {
					//文件采集
					if (IsFlag.ofEnumByCode(fileType) == IsFlag.Fou) {
						FileReadAllRound fileReadAllround = new FileReadAllRound(jobId, json, null);
						executor.submit(fileReadAllround);
					}
					//文件夹采集
					else if (IsFlag.ofEnumByCode(fileType) == IsFlag.Shi) {
						DirListenerReader dirListenerReader = new DirListenerReader(jobId, json, null);
						executor.execute(dirListenerReader);
					}
				}
				//String
				else {
					//文件采集
					if (IsFlag.ofEnumByCode(fileType) == IsFlag.Fou) {
						FileReadAllroundString fileReadAllroundString = new FileReadAllroundString(jobId, json, null);
						executor.execute(fileReadAllroundString);
					}
					//文件夹采集
					else if (IsFlag.ofEnumByCode(fileType) == IsFlag.Shi) {
						DirListenerReaderString dirListenerReaderString = new DirListenerReaderString(jobId, json, null);
						executor.execute(dirListenerReaderString);
					}
				}

			}
			if (mapExecS.get(jobId) != null) {
				ExecutorService executorService = mapExecS.get(jobId);
				executorService.shutdown();
				executorService.awaitTermination(1, TimeUnit.HOURS);
			}
			mapExecS.put(jobId, executor);
		} else {//命令触发
			WriterParam writerParam = new WriterParam();
			writerParam.writeProducerParam(jobId, json.toString());
		}
	}
}
