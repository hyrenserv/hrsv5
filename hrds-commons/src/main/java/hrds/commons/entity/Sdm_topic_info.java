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
 * 流数据管理topic信息表
 */
@Table(tableName = "sdm_topic_info")
public class Sdm_topic_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_topic_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理topic信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("topic_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="topic_id",value="topic_id:",dataType = Long.class,required = true)
	private Long topic_id;
	@DocBean(name ="sdm_top_name",value="topic英文名称:",dataType = String.class,required = true)
	private String sdm_top_name;
	@DocBean(name ="sdm_top_value",value="topic描述:",dataType = String.class,required = false)
	private String sdm_top_value;
	@DocBean(name ="sdm_zk_host",value="ZK主机:",dataType = String.class,required = true)
	private String sdm_zk_host;
	@DocBean(name ="sdm_partition",value="分区数:",dataType = Long.class,required = true)
	private Long sdm_partition;
	@DocBean(name ="sdm_replication",value="副本值个数:",dataType = Long.class,required = true)
	private Long sdm_replication;
	@DocBean(name ="create_date",value="创建日期:",dataType = String.class,required = true)
	private String create_date;
	@DocBean(name ="create_time",value="创建时间:",dataType = String.class,required = true)
	private String create_time;
	@DocBean(name ="sdm_top_cn_name",value="topic中文名称:",dataType = String.class,required = true)
	private String sdm_top_cn_name;
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;

	/** 取得：topic_id */
	public Long getTopic_id(){
		return topic_id;
	}
	/** 设置：topic_id */
	public void setTopic_id(Long topic_id){
		this.topic_id=topic_id;
	}
	/** 设置：topic_id */
	public void setTopic_id(String topic_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(topic_id)){
			this.topic_id=new Long(topic_id);
		}
	}
	/** 取得：topic英文名称 */
	public String getSdm_top_name(){
		return sdm_top_name;
	}
	/** 设置：topic英文名称 */
	public void setSdm_top_name(String sdm_top_name){
		this.sdm_top_name=sdm_top_name;
	}
	/** 取得：topic描述 */
	public String getSdm_top_value(){
		return sdm_top_value;
	}
	/** 设置：topic描述 */
	public void setSdm_top_value(String sdm_top_value){
		this.sdm_top_value=sdm_top_value;
	}
	/** 取得：ZK主机 */
	public String getSdm_zk_host(){
		return sdm_zk_host;
	}
	/** 设置：ZK主机 */
	public void setSdm_zk_host(String sdm_zk_host){
		this.sdm_zk_host=sdm_zk_host;
	}
	/** 取得：分区数 */
	public Long getSdm_partition(){
		return sdm_partition;
	}
	/** 设置：分区数 */
	public void setSdm_partition(Long sdm_partition){
		this.sdm_partition=sdm_partition;
	}
	/** 设置：分区数 */
	public void setSdm_partition(String sdm_partition){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_partition)){
			this.sdm_partition=new Long(sdm_partition);
		}
	}
	/** 取得：副本值个数 */
	public Long getSdm_replication(){
		return sdm_replication;
	}
	/** 设置：副本值个数 */
	public void setSdm_replication(Long sdm_replication){
		this.sdm_replication=sdm_replication;
	}
	/** 设置：副本值个数 */
	public void setSdm_replication(String sdm_replication){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_replication)){
			this.sdm_replication=new Long(sdm_replication);
		}
	}
	/** 取得：创建日期 */
	public String getCreate_date(){
		return create_date;
	}
	/** 设置：创建日期 */
	public void setCreate_date(String create_date){
		this.create_date=create_date;
	}
	/** 取得：创建时间 */
	public String getCreate_time(){
		return create_time;
	}
	/** 设置：创建时间 */
	public void setCreate_time(String create_time){
		this.create_time=create_time;
	}
	/** 取得：topic中文名称 */
	public String getSdm_top_cn_name(){
		return sdm_top_cn_name;
	}
	/** 设置：topic中文名称 */
	public void setSdm_top_cn_name(String sdm_top_cn_name){
		this.sdm_top_cn_name=sdm_top_cn_name;
	}
	/** 取得：用户ID */
	public Long getUser_id(){
		return user_id;
	}
	/** 设置：用户ID */
	public void setUser_id(Long user_id){
		this.user_id=user_id;
	}
	/** 设置：用户ID */
	public void setUser_id(String user_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(user_id)){
			this.user_id=new Long(user_id);
		}
	}
}
