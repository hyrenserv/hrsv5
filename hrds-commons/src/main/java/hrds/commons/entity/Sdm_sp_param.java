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
 * StreamingPro作业启动参数表
 */
@Table(tableName = "sdm_sp_param")
public class Sdm_sp_param extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_param";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro作业启动参数表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("ssp_param_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ssp_param_id",value="作业启动参数表id:",dataType = Long.class,required = true)
	private Long ssp_param_id;
	@DocBean(name ="ssp_param_key",value="参数key:",dataType = String.class,required = true)
	private String ssp_param_key;
	@DocBean(name ="ssp_param_value",value="参数值:",dataType = String.class,required = false)
	private String ssp_param_value;
	@DocBean(name ="is_customize",value="是否是自定义参数(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_customize;
	@DocBean(name ="ssj_job_id",value="作业id:",dataType = Long.class,required = true)
	private Long ssj_job_id;

	/** 取得：作业启动参数表id */
	public Long getSsp_param_id(){
		return ssp_param_id;
	}
	/** 设置：作业启动参数表id */
	public void setSsp_param_id(Long ssp_param_id){
		this.ssp_param_id=ssp_param_id;
	}
	/** 设置：作业启动参数表id */
	public void setSsp_param_id(String ssp_param_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ssp_param_id)){
			this.ssp_param_id=new Long(ssp_param_id);
		}
	}
	/** 取得：参数key */
	public String getSsp_param_key(){
		return ssp_param_key;
	}
	/** 设置：参数key */
	public void setSsp_param_key(String ssp_param_key){
		this.ssp_param_key=ssp_param_key;
	}
	/** 取得：参数值 */
	public String getSsp_param_value(){
		return ssp_param_value;
	}
	/** 设置：参数值 */
	public void setSsp_param_value(String ssp_param_value){
		this.ssp_param_value=ssp_param_value;
	}
	/** 取得：是否是自定义参数 */
	public String getIs_customize(){
		return is_customize;
	}
	/** 设置：是否是自定义参数 */
	public void setIs_customize(String is_customize){
		this.is_customize=is_customize;
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
}
