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
 * 流数据用户消费申请表
 */
@Table(tableName = "sdm_user_permission")
public class Sdm_user_permission extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_user_permission";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据用户消费申请表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("app_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="app_id",value="申请id:",dataType = Long.class,required = true)
	private Long app_id;
	@DocBean(name ="application_status",value="流数据申请状态(FlowApplyStatus):1-未申请<WeiShenQing> 2-申请中<ShenQingZhong> 3-申请通过<ShenQingTongGuo> 4-申请不通过<ShenQingBuTongGuo> ",dataType = String.class,required = true)
	private String application_status;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="produce_user",value="用户ID:",dataType = Long.class,required = true)
	private Long produce_user;
	@DocBean(name ="consume_user",value="用户ID:",dataType = Long.class,required = true)
	private Long consume_user;
	@DocBean(name ="topic_id",value="topic_id:",dataType = Long.class,required = true)
	private Long topic_id;
	@DocBean(name ="sdm_receive_id",value="流数据管理:",dataType = Long.class,required = true)
	private Long sdm_receive_id;

	/** 取得：申请id */
	public Long getApp_id(){
		return app_id;
	}
	/** 设置：申请id */
	public void setApp_id(Long app_id){
		this.app_id=app_id;
	}
	/** 设置：申请id */
	public void setApp_id(String app_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(app_id)){
			this.app_id=new Long(app_id);
		}
	}
	/** 取得：流数据申请状态 */
	public String getApplication_status(){
		return application_status;
	}
	/** 设置：流数据申请状态 */
	public void setApplication_status(String application_status){
		this.application_status=application_status;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：用户ID */
	public Long getProduce_user(){
		return produce_user;
	}
	/** 设置：用户ID */
	public void setProduce_user(Long produce_user){
		this.produce_user=produce_user;
	}
	/** 设置：用户ID */
	public void setProduce_user(String produce_user){
		if(!fd.ng.core.utils.StringUtil.isEmpty(produce_user)){
			this.produce_user=new Long(produce_user);
		}
	}
	/** 取得：用户ID */
	public Long getConsume_user(){
		return consume_user;
	}
	/** 设置：用户ID */
	public void setConsume_user(Long consume_user){
		this.consume_user=consume_user;
	}
	/** 设置：用户ID */
	public void setConsume_user(String consume_user){
		if(!fd.ng.core.utils.StringUtil.isEmpty(consume_user)){
			this.consume_user=new Long(consume_user);
		}
	}
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
	/** 取得：流数据管理 */
	public Long getSdm_receive_id(){
		return sdm_receive_id;
	}
	/** 设置：流数据管理 */
	public void setSdm_receive_id(Long sdm_receive_id){
		this.sdm_receive_id=sdm_receive_id;
	}
	/** 设置：流数据管理 */
	public void setSdm_receive_id(String sdm_receive_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_receive_id)){
			this.sdm_receive_id=new Long(sdm_receive_id);
		}
	}
}
