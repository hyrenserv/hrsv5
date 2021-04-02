package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import hrds.commons.entity.fdentity.ProjectTableEntity;
import fd.ng.db.entity.anno.Table;
import fd.ng.core.annotation.DocBean;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * StreamingPro流数据信息表
 */
@Table(tableName = "sdm_sp_stream")
public class Sdm_sp_stream extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_stream";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro流数据信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sss_stream_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sss_stream_id",value="流数据信息表id:",dataType = Long.class,required = true)
	private Long sss_stream_id;
	@DocBean(name ="sss_kafka_version",value="kafka版本(SdmSpStreamVer):1-kafka8<KAFKA8> 2-kafka9<KAFKA9> 3-kafka<KAFKA> ",dataType = String.class,required = true)
	private String sss_kafka_version;
	@DocBean(name ="sss_topic_name",value="主题:",dataType = String.class,required = true)
	private String sss_topic_name;
	@DocBean(name ="sss_bootstrap_server",value="流服务主机:",dataType = String.class,required = true)
	private String sss_bootstrap_server;
	@DocBean(name ="sss_consumer_offset",value="偏移量设置:",dataType = String.class,required = true)
	private String sss_consumer_offset;
	@DocBean(name ="sdm_info_id",value="作业输入信息表id:",dataType = Long.class,required = true)
	private Long sdm_info_id;

	/** 取得：流数据信息表id */
	public Long getSss_stream_id(){
		return sss_stream_id;
	}
	/** 设置：流数据信息表id */
	public void setSss_stream_id(Long sss_stream_id){
		this.sss_stream_id=sss_stream_id;
	}
	/** 设置：流数据信息表id */
	public void setSss_stream_id(String sss_stream_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sss_stream_id)){
			this.sss_stream_id=new Long(sss_stream_id);
		}
	}
	/** 取得：kafka版本 */
	public String getSss_kafka_version(){
		return sss_kafka_version;
	}
	/** 设置：kafka版本 */
	public void setSss_kafka_version(String sss_kafka_version){
		this.sss_kafka_version=sss_kafka_version;
	}
	/** 取得：主题 */
	public String getSss_topic_name(){
		return sss_topic_name;
	}
	/** 设置：主题 */
	public void setSss_topic_name(String sss_topic_name){
		this.sss_topic_name=sss_topic_name;
	}
	/** 取得：流服务主机 */
	public String getSss_bootstrap_server(){
		return sss_bootstrap_server;
	}
	/** 设置：流服务主机 */
	public void setSss_bootstrap_server(String sss_bootstrap_server){
		this.sss_bootstrap_server=sss_bootstrap_server;
	}
	/** 取得：偏移量设置 */
	public String getSss_consumer_offset(){
		return sss_consumer_offset;
	}
	/** 设置：偏移量设置 */
	public void setSss_consumer_offset(String sss_consumer_offset){
		this.sss_consumer_offset=sss_consumer_offset;
	}
	/** 取得：作业输入信息表id */
	public Long getSdm_info_id(){
		return sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(Long sdm_info_id){
		this.sdm_info_id=sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(String sdm_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_info_id)){
			this.sdm_info_id=new Long(sdm_info_id);
		}
	}
}
