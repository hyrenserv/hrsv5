package hrds.producer.common;

import com.alibaba.fastjson.JSONObject;
import hrds.commons.codes.IsFlag;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaProducerWorker {

	private static final Logger logger = LogManager.getLogger();

	private KafkaProducerError KAFKA_PRODUCER_ERROR = new KafkaProducerError();

	/**
	 * kafka发消息方法
	 *
	 * @param producer kafka生产者对象
	 * @param topic    kafka主题对象
	 * @param msg      发送的消息
	 * @return 返回发送kafka消息是否成功标志
	 */
	public boolean sendToKafka(final String path, KafkaProducer<String, GenericRecord> producer, GenericRecord msg, final String topic,
	                           CustomerPartition cp, final String bootstrapServers, String sync) {

		try {
			final ProducerRecord<String, GenericRecord> record;
			if (cp != null) {
				String partitionKey = cp.getPartitionKey(msg);
				record = new ProducerRecord<>(topic, partitionKey, msg);
			} else {
				record = new ProducerRecord<>(topic, msg);
			}
			if (IsFlag.Fou == IsFlag.ofEnumByCode(sync)) {
				producer.send(record, (metadata, e) -> {
					if (e != null) {
						String message = record.value().toString();
						logger.error("生产者数据发送失败！！！", e);
						setKafkaProduceErrorInfo(path, topic, bootstrapServers, e, message);
					}
				}).get();
			} else {
				producer.send(record, (metadata, e) -> {

					if (e != null) {
						String message = record.value().toString();
						logger.error("生产者数据发送失败。原始消息为[" + message + "]", e);
						setKafkaProduceErrorInfo(path, topic, bootstrapServers, e, message);
					}
				});
			}
			return true;
		} catch (Exception e) {
			logger.error("错误数据为：" + msg + ";", e);
			return false;
		}
	}

	public boolean sendToKafka(final String path, KafkaProducer<String, String> producer, String msg, final String topic, CustomerPartition cp,
	                           final String bootstrapServers, String sync) {

		try {
			final ProducerRecord<String, String> record;
			if (cp != null) {
				String partitionKey = cp.getPartitionKey(msg);
				record = new ProducerRecord<>(topic, partitionKey, msg);
			} else {
				record = new ProducerRecord<>(topic, msg);
			}
			if (IsFlag.Fou == IsFlag.ofEnumByCode(sync)) {
				producer.send(record, (metadata, e) -> {
					if (e != null) {
						String message = record.value();
						setKafkaProduceErrorInfo(path, topic, bootstrapServers, e, message);
					}
				}).get();
			} else {
				producer.send(record, (metadata, e) -> {
					if (e != null) {
						String message = record.value();
						setKafkaProduceErrorInfo(path, topic, bootstrapServers, e, message);
					}
				});
			}
			return true;
		} catch (Exception e) {
			logger.error("错误数据为：" + msg + ";", e);
			return false;
		}
	}

	private void setKafkaProduceErrorInfo(String path, String topic, String bootstrapServers,
	                                      Exception e, String message) {
		logger.error("生产者数据发送失败。", e);
		JSONObject json = new JSONObject();
		json.put("time", System.currentTimeMillis());
		json.put("topic", topic);
		json.put("message", message);
		json.put("error", e);
		json.put("path", path);
		KAFKA_PRODUCER_ERROR.sendToKafka(bootstrapServers, topic, json.toString());
	}
}
