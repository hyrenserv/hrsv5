package hrds.producer.string.file.bigFile;

import com.alibaba.fastjson.JSONObject;
import hrds.commons.codes.SdmPatitionWay;
import hrds.commons.exception.BusinessException;
import hrds.producer.common.CusClassLoader;
import hrds.producer.common.CustomerPartition;
import hrds.producer.common.JobParamsEntity;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ProducerOperatorBigFile {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * @param json  生产者配置参数
	 * @param jobId 任务ID
	 * @return 返回任务参数实体对象
	 */
	public JobParamsEntity getMapParam(JSONObject json, String jobId) {

		JobParamsEntity jobParams = new JobParamsEntity();//实体
		jobParams.setJobId(jobId);
		Properties properties = null;
		try {
			//获取并加载kafka信息
			JSONObject jb = json.getJSONObject("kafka_params");
			jobParams.setTopic(jb.getString("topic"));
			if (SdmPatitionWay.Key == SdmPatitionWay.ofEnumByCode(jb.getString("sdm_partition"))) {
				CusClassLoader classLoader = new CusClassLoader();
				Class<?> clazz = classLoader.getURLClassLoader().loadClass(jb.getString("sdm_partition_name"));
				CustomerPartition cp = (CustomerPartition) clazz.newInstance();
				jobParams.setCustomerPartition(cp);
			}

			// producer 参数配置
			String bootstrapServers = jb.getString("bootstrap.servers");
			jobParams.setBootstrapServers(bootstrapServers);
			properties = new Properties();
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
			properties.put(ProducerConfig.ACKS_CONFIG, jb.getString("acks"));
			properties.put(ProducerConfig.RETRIES_CONFIG, jb.getString("retries"));
			properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, jb.getString("max.request.size"));
			properties.put(ProducerConfig.BATCH_SIZE_CONFIG, jb.getString("batch.size"));
			properties.put(ProducerConfig.LINGER_MS_CONFIG, jb.getString("linger.ms"));
			properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, jb.getString("buffer.memory"));
			properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, jb.getString("compression.type"));
			String interceptor = jb.getString("interceptor.classes");
			if (interceptor != null && !interceptor.isEmpty()) {
				List<String> interceptors = new ArrayList<>();
				Collections.addAll(interceptors, interceptor.split(","));
				properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);
			}
			if (SdmPatitionWay.FenQu.toString().equals(jb.getString("sdm_partition"))) {
				properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, jb.getString("sdm_partition_name"));
			}
			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			jobParams.setSync(jb.getString("sync"));
			logger.info("KafkaProducerWorker加载配置文件！！！");
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw new BusinessException(e.getMessage());
			}
			logger.error("生产者参数获取失败！！！", e);
		}
		if (properties != null) {
			KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
			jobParams.setProducerString(producer);
		}
		return jobParams;

	}

}
