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
 * 流数据管理Agent信息表
 */
@Table(tableName = "sdm_agent_info")
public class Sdm_agent_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_agent_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理Agent信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_agent_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sdm_agent_id",value="流数据管理agent_id:",dataType = Long.class,required = true)
	private Long sdm_agent_id;
	@DocBean(name ="sdm_agent_name",value="流数据管理agent名称:",dataType = String.class,required = true)
	private String sdm_agent_name;
	@DocBean(name ="sdm_agent_type",value="流数据管理agent类别(SdmAgentType):1-文本流Agent<WenBenLiu> 2-Rest接收Agent<RestJieShou> ",dataType = String.class,required = true)
	private String sdm_agent_type;
	@DocBean(name ="sdm_agent_ip",value="流数据管理agent所在服务器ip:",dataType = String.class,required = true)
	private String sdm_agent_ip;
	@DocBean(name ="sdm_agent_port",value="流数据管理agent服务器端口:",dataType = String.class,required = true)
	private String sdm_agent_port;
	@DocBean(name ="create_date",value="创建日期:",dataType = String.class,required = true)
	private String create_date;
	@DocBean(name ="create_time",value="创建时间:",dataType = String.class,required = true)
	private String create_time;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;
	@DocBean(name ="sdm_agent_status",value="流数据管理agent状态(AgentStatus):1-已连接<YiLianJie> 2-未连接<WeiLianJie> 3-正在运行<ZhengZaiYunXing> ",dataType = String.class,required = true)
	private String sdm_agent_status;
	@DocBean(name ="create_id",value="创建用户ID:",dataType = Long.class,required = true)
	private Long create_id;
	@DocBean(name ="sdm_source_id",value="数据源ID:",dataType = Long.class,required = true)
	private Long sdm_source_id;

	/** 取得：流数据管理agent_id */
	public Long getSdm_agent_id(){
		return sdm_agent_id;
	}
	/** 设置：流数据管理agent_id */
	public void setSdm_agent_id(Long sdm_agent_id){
		this.sdm_agent_id=sdm_agent_id;
	}
	/** 设置：流数据管理agent_id */
	public void setSdm_agent_id(String sdm_agent_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_agent_id)){
			this.sdm_agent_id=new Long(sdm_agent_id);
		}
	}
	/** 取得：流数据管理agent名称 */
	public String getSdm_agent_name(){
		return sdm_agent_name;
	}
	/** 设置：流数据管理agent名称 */
	public void setSdm_agent_name(String sdm_agent_name){
		this.sdm_agent_name=sdm_agent_name;
	}
	/** 取得：流数据管理agent类别 */
	public String getSdm_agent_type(){
		return sdm_agent_type;
	}
	/** 设置：流数据管理agent类别 */
	public void setSdm_agent_type(String sdm_agent_type){
		this.sdm_agent_type=sdm_agent_type;
	}
	/** 取得：流数据管理agent所在服务器ip */
	public String getSdm_agent_ip(){
		return sdm_agent_ip;
	}
	/** 设置：流数据管理agent所在服务器ip */
	public void setSdm_agent_ip(String sdm_agent_ip){
		this.sdm_agent_ip=sdm_agent_ip;
	}
	/** 取得：流数据管理agent服务器端口 */
	public String getSdm_agent_port(){
		return sdm_agent_port;
	}
	/** 设置：流数据管理agent服务器端口 */
	public void setSdm_agent_port(String sdm_agent_port){
		this.sdm_agent_port=sdm_agent_port;
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
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
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
	/** 取得：流数据管理agent状态 */
	public String getSdm_agent_status(){
		return sdm_agent_status;
	}
	/** 设置：流数据管理agent状态 */
	public void setSdm_agent_status(String sdm_agent_status){
		this.sdm_agent_status=sdm_agent_status;
	}
	/** 取得：创建用户ID */
	public Long getCreate_id(){
		return create_id;
	}
	/** 设置：创建用户ID */
	public void setCreate_id(Long create_id){
		this.create_id=create_id;
	}
	/** 设置：创建用户ID */
	public void setCreate_id(String create_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(create_id)){
			this.create_id=new Long(create_id);
		}
	}
	/** 取得：数据源ID */
	public Long getSdm_source_id(){
		return sdm_source_id;
	}
	/** 设置：数据源ID */
	public void setSdm_source_id(Long sdm_source_id){
		this.sdm_source_id=sdm_source_id;
	}
	/** 设置：数据源ID */
	public void setSdm_source_id(String sdm_source_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_source_id)){
			this.sdm_source_id=new Long(sdm_source_id);
		}
	}
}
