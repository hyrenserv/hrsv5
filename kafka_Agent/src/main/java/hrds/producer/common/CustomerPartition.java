package hrds.producer.common;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Partitioner;

public interface CustomerPartition extends Partitioner {
	/**
	 * 获取用户用来做分区的key
	 *
	 * @param param 用户要处理的流
	 * @return 从处理的流里处理获取用来分区的key
	 */
	String getPartitionKey(GenericRecord param);

	String getPartitionKey(String param);
}
