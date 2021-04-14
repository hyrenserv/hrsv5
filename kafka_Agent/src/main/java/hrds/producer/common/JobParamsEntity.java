package hrds.producer.common;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.script.Invocable;
import java.util.List;

public class JobParamsEntity {

	private BusinessProcess businessProcess;//自定义业务处理
	private Invocable invocable;//自定义业务处理(javaScript)
	private String cusDesType;//自定义业务处理类型(0：BusinessProcess;1:JavaScript)
	private String topic;//topic
	private String msgType;//消息类型(rest)
	private String msgHeader;//消息头(rest)
	private List<String> listColumn;//列信息
	private CustomerPartition customerPartition;//自定义分区类(key)
	private String bootstrapServers;//kafka ip及端口
	private String jobId;//任务Id
	private KafkaProducer<String, GenericRecord> producer;//producer
	private KafkaProducer<String, String> producerString;//producer
	private Schema schema;//avro schema
	private String sdmDateLimiter;
	private String isObj;
	private String sync;

	public BusinessProcess getBusinessProcess() {

		return businessProcess;
	}

	public void setBusinessProcess(BusinessProcess businessProcess) {

		this.businessProcess = businessProcess;
	}

	public Invocable getInvocable() {

		return invocable;
	}

	public void setInvocable(Invocable invocable) {

		this.invocable = invocable;
	}

	public String getCusDesType() {

		return cusDesType;
	}

	public void setCusDesType(String cusDesType) {

		this.cusDesType = cusDesType;
	}

	public String getTopic() {

		return topic;
	}

	public void setTopic(String topic) {

		this.topic = topic;
	}

	public String getMsgType() {

		return msgType;
	}

	public void setMsgType(String msgType) {

		this.msgType = msgType;
	}

	public String getMsgHeader() {

		return msgHeader;
	}

	public void setMsgHeader(String msgHeader) {

		this.msgHeader = msgHeader;
	}

	public List<String> getListColumn() {

		return listColumn;
	}

	public void setListColumn(List<String> listColumn) {

		this.listColumn = listColumn;
	}

	public CustomerPartition getCustomerPartition() {

		return customerPartition;
	}

	public void setCustomerPartition(CustomerPartition customerPartition) {

		this.customerPartition = customerPartition;
	}

	public String getBootstrapServers() {

		return bootstrapServers;
	}

	public void setBootstrapServers(String bootstrapServers) {

		this.bootstrapServers = bootstrapServers;
	}

	public String getJobId() {

		return jobId;
	}

	public void setJobId(String jobId) {

		this.jobId = jobId;
	}

	public KafkaProducer<String, GenericRecord> getProducer() {

		return producer;
	}

	public void setProducer(KafkaProducer<String, GenericRecord> producer) {

		this.producer = producer;
	}

	public KafkaProducer<String, String> getProducerString() {

		return producerString;
	}

	public void setProducerString(KafkaProducer<String, String> producerString) {

		this.producerString = producerString;
	}

	public Schema getSchema() {

		return schema;
	}

	public void setSchema(Schema schema) {

		this.schema = schema;
	}

	public String getSdmDatelimiter() {

		return sdmDateLimiter;
	}

	public void setSdmDatelimiter(String sdmDatelimiter) {

		this.sdmDateLimiter = sdmDatelimiter;
	}

	public String getIsObj() {

		return isObj;
	}

	public void setIsObj(String isObj) {

		this.isObj = isObj;
	}

	public String getSync() {

		return sync;
	}

	public void setSync(String sync) {

		this.sync = sync;
	}


}
