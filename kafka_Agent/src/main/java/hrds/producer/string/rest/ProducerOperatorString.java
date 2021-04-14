package hrds.producer.string.rest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.StringUtil;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.SdmPatitionWay;
import hrds.commons.exception.BusinessException;
import hrds.producer.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.Invocable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ProducerOperatorString {

	private static final transient Logger logger = LogManager.getLogger();

	public JobParamsEntity getMapParam(JSONObject json) {

		JobParamsEntity jobParams = new JobParamsEntity();
		Properties properties = null;
		try {
			//			//获取消息列信息(类型，列名）
			JSONObject msgtypeJson = json.getJSONObject("msgtype");
			jobParams.setMsgType(msgtypeJson.getString("type"));
			String header = msgtypeJson.getString("header");
			if (header != null) {
				jobParams.setMsgHeader(msgtypeJson.getString("header"));
			}
			JSONArray columnsJson = json.getJSONArray("msg_info_ls");
			List<String> listColumn = new ArrayList<>();
			for (Object columnParam : columnsJson) {
				JSONObject columnJson = JSONObject.parseObject(columnParam.toString());
				String columnName = columnJson.getString("sdm_var_name_en");
				String sdm_is_send = columnJson.getString("sdm_is_send").trim();
				listColumn.add(columnJson.getIntValue("number") - 1, columnName + "`" + sdm_is_send);
			}
			jobParams.setListColumn(listColumn);
			//获取并加载kafka信息
			JSONObject jb = json.getJSONObject("kafka_params");
			jobParams.setTopic(jb.getString("topic"));
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
			if (!StringUtils.isBlank(interceptor)) {
				List<String> interceptors = new ArrayList<>();
				Collections.addAll(interceptors, interceptor.split(","));
				properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);
			}
			if (SdmPatitionWay.FenQu == SdmPatitionWay.ofEnumByCode(jb.getString("sdm_partition"))) {
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
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
		jobParams.setProducerString(producer);
		return jobParams;
	}

}
