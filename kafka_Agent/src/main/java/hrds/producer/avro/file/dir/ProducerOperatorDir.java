package hrds.producer.avro.file.dir;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.StringUtil;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.SdmPatitionWay;
import hrds.commons.exception.BusinessException;
import hrds.producer.common.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.hadoop.io.AvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.Invocable;
import java.util.*;

public class ProducerOperatorDir {

	private static final Logger logger = LogManager.getLogger();
	private static final String spritName = "{\"name\":\"";
	private static final String spritTypeString = "\",\"type\":[\"string\",\"null\"]},";
	private static final String spritTypeByte = "\",\"type\":[\"bytes\",\"null\"]},";

	/**
	 * @param json
	 * @param jobId
	 * @return
	 */
	public JobParamsEntity getMapParam(String is_data_partition, JSONObject json, String jobId) {

		JobParamsEntity jobParams = new JobParamsEntity();//实体
		jobParams.setJobId(jobId);
		Properties properties = null;
		try {
			StringBuilder schemaString = new StringBuilder("{\"type\":\"record\",\"name\":\"User\",\"namespace\":\"avro\",\"fields\":[");
			StringBuilder schemaStringLine = new StringBuilder("{\"type\":\"record\",\"name\":\"User\",\"namespace\":\"avro\",\"fields\":[{\"name\":\"line\",\"type\":[\"string\",\"null\"]},");
			JSONArray columesJson = json.getJSONArray("msg_info_ls");//获取列信息
			List<String> listColumn = new ArrayList<>();
			Map<Integer, String> mapSchema = new TreeMap<>(Integer::compareTo);
			for (Object columnParam : columesJson) {
				JSONObject columnJson = JSONObject.parseObject(columnParam.toString());
				int num = columnJson.getIntValue("number") - 1;
				String cloumeName = columnJson.getString("sdm_var_name_en");
				String type = columnJson.getString("sdm_var_type").toLowerCase();
				String sdm_is_send = columnJson.getString("sdm_is_send").trim();
				if (type.contains("byte")) {
					if (IsFlag.Fou == IsFlag.ofEnumByCode(sdm_is_send)) {
						mapSchema.put(num, spritName + cloumeName + spritTypeByte);
					}
				} else {
					if (IsFlag.Fou == IsFlag.ofEnumByCode(sdm_is_send)) {
						mapSchema.put(num, spritName + cloumeName + spritTypeString);
					}
				}
				listColumn.add(num, cloumeName + "`" + sdm_is_send);
			}
			jobParams.setListColumn(listColumn);

			String schemaMessage;
			if (columesJson.size() > 0 && IsFlag.Fou == IsFlag.ofEnumByCode(is_data_partition)) {
				for (int key : mapSchema.keySet()) {
					schemaString.append(mapSchema.get(key));
				}
				schemaMessage = schemaString.substring(0, schemaString.length() - 1) + "]}";
			} else {
				for (int key : mapSchema.keySet()) {
					schemaStringLine.append(mapSchema.get(key));
				}
				schemaMessage = schemaStringLine.substring(0, schemaStringLine.length() - 1) + "]}";
			}
			Schema schema = new Schema.Parser().parse(schemaMessage);
			jobParams.setSchema(schema);

			//获取并加载kafka信息
			JSONObject jb = json.getJSONObject("kafka_params");
			jobParams.setTopic(jb.getString("topic"));
			//获取自定义业务处理类
			JSONObject businessJson = json.getJSONObject("business_class");
			String sdm_bus_pro_cla = businessJson.getString("sdm_bus_pro_cla");
			if (StringUtil.isNotBlank(sdm_bus_pro_cla)) {
				String cus_des_type = businessJson.getString("cus_des_type");
				jobParams.setCusDesType(cus_des_type);
				if (IsFlag.Fou == IsFlag.ofEnumByCode(cus_des_type)) {
					CusClassLoader classLoader = new CusClassLoader();
					Class<?> clazz = classLoader.getURLClassLoader().loadClass(sdm_bus_pro_cla);
					jobParams.setBusinessProcess((BusinessProcess) clazz.newInstance());
				} else {
					CustomJavaScript customJavaScript = new CustomJavaScript();
					Invocable invocable = customJavaScript.getInvocable(sdm_bus_pro_cla);
					jobParams.setInvocable(invocable);
				}
			}
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
		KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(properties);
		jobParams.setProducer(producer);
		return jobParams;

	}

}
