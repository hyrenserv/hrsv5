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
 * StreamingPro作业信息表
 */
@Table(tableName = "sdm_sp_jobinfo")
public class Sdm_sp_jobinfo extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_jobinfo";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro作业信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("ssj_job_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ssj_job_name",value="作业名称:",dataType = String.class,required = true)
	private String ssj_job_name;
	@DocBean(name ="ssj_job_id",value="作业id:",dataType = Long.class,required = true)
	private Long ssj_job_id;
	@DocBean(name ="ssj_job_desc",value="作业描述:",dataType = String.class,required = false)
	private String ssj_job_desc;
	@DocBean(name ="ssj_strategy",value="作业执行策略:",dataType = String.class,required = true)
	private String ssj_strategy;
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;

	/** 取得：作业名称 */
	public String getSsj_job_name(){
		return ssj_job_name;
	}
	/** 设置：作业名称 */
	public void setSsj_job_name(String ssj_job_name){
		this.ssj_job_name=ssj_job_name;
	}
	/** 取得：作业id */
	public Long getSsj_job_id(){
		return ssj_job_id;
	}
	/** 设置：作业id */
	public void setSsj_job_id(Long ssj_job_id){
		this.ssj_job_id=ssj_job_id;
	}
	/** 设置：作业id */
	public void setSsj_job_id(String ssj_job_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ssj_job_id)){
			this.ssj_job_id=new Long(ssj_job_id);
		}
	}
	/** 取得：作业描述 */
	public String getSsj_job_desc(){
		return ssj_job_desc;
	}
	/** 设置：作业描述 */
	public void setSsj_job_desc(String ssj_job_desc){
		this.ssj_job_desc=ssj_job_desc;
	}
	/** 取得：作业执行策略 */
	public String getSsj_strategy(){
		return ssj_strategy;
	}
	/** 设置：作业执行策略 */
	public void setSsj_strategy(String ssj_strategy){
		this.ssj_strategy=ssj_strategy;
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
