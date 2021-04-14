package hrds.producer.common;

import hrds.commons.exception.BusinessException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * 获取信息，并往kafka发送消息
 */
public class KafkaProducerError {

	private static final Logger logger = LogManager.getLogger();

	private static final String sign = "_inner";

	private static final String errorFilePath = "/var/log/hyrenserv/kafkaError";

	/**
	 * kafka发消息方法
	 *
	 * @param message 发送的消息
	 */
	public void sendToKafka(String bootstrapServers, String topic, String message) {

		Properties properties = prop(bootstrapServers);
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
		final ProducerRecord<String, String> record = new ProducerRecord<>(topic + sign, message);
		producer.send(record, (paramRecordMetadata, e) -> {
			if (e != null) {
				String topic1 = record.topic();
				String message1 = record.value();
				logger.error("Error生产者发送失败数据失败！！！", e);
				String errorFile = errorFilePath + File.separator + topic1;
				//TODO 任务再失败错误信息写文件（路径操作系统盘，/var/log/hyrenserv/kafka）
				File file = new File(errorFile);
				if (!file.exists()) {
					if (file.mkdirs()) {
						throw new BusinessException("创建错误信息文件目录失败");
					}
				}
				// 将错误信息写入文件保存
				saveWriteFile(errorFile, message1);
			}
		});
		producer.close();
	}

	public static void saveWriteFile(String fileName, String content) {
		FileWriter fw = null;
		PrintWriter out = null;
		try {
			fw = new FileWriter(fileName, true);
			out = new PrintWriter(fw);
			out.println(content);
		} catch (IOException e) {
			logger.error(e);
			throw new BusinessException(e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error(e);
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception e) {
					logger.error(e);
				}
			}

		}

	}

	public static Properties prop(String bootstrapServers) {
		// producer 参数配置
		logger.info("KafkaProducerError加载配置文件！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
		Properties properties = new Properties();
		properties.put("bootstrap.servers", bootstrapServers);
		properties.put("acks", "all");
		properties.put("retries", "1");
		properties.put("max.request.size", "1048576");
		properties.put("batch.size", "16384");
		properties.put("linger.ms", "1");
		properties.put("key.serializer", StringSerializer.class.getName());
		properties.put("value.serializer", StringSerializer.class.getName());

		return properties;
	}
}
