package hrds.producer.avro.file.bigFile;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.Validator;
import hrds.commons.codes.SdmPatitionWay;
import hrds.commons.exception.BusinessException;
import hrds.producer.common.CusClassLoader;
import hrds.producer.common.CustomerPartition;
import hrds.producer.common.JobParamsEntity;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.hadoop.io.AvroSerializer;
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
	 * @param json
	 * @param jobId
	 * @return
	 */
	public JobParamsEntity getMapParam(JSONObject json, String jobId) {

		JobParamsEntity jobParams = new JobParamsEntity();//实体
		jobParams.setJobId(jobId);
		Properties properties = null;
		try {
			String schemaString = "{\"type\":\"record\",\"name\":\"User\",\"namespace\":\"avro\",\"fields\":[{\"name\":\"line\",\"type\":[\"string\",\"null\"]},{\"name\":\"bytes\",\"type\":[\"bytes\", \"null\"]},{\"name\":\"lineNum\",\"type\":\"int\",\"order\":\"descending\"}]}";
			Schema schema = new Schema.Parser().parse(schemaString);
			jobParams.setSchema(schema);

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
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
			jobParams.setSync(jb.getString("sync"));
			logger.info("KafkaProducerWorker加载配置文件！！！");
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw new BusinessException(e.getMessage());
			}
			logger.error("生产者参数获取失败！！！", e);
		}
		Validator.notNull(properties, "生产者参数不能为空");
		KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(properties);
		jobParams.setProducer(producer);
		return jobParams;
	}

}
