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
 * 错误信息表
 */
@Table(tableName = "error_info")
public class Error_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "error_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 错误信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("error_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="error_id",value="错误ID:",dataType = Long.class,required = true)
	private Long error_id;
	@DocBean(name ="error_msg",value="error_msg:",dataType = String.class,required = false)
	private String error_msg;
	@DocBean(name ="job_rs_id",value="作业执行结果ID:",dataType = String.class,required = true)
	private String job_rs_id;

	/** 取得：错误ID */
	public Long getError_id(){
		return error_id;
	}
	/** 设置：错误ID */
	public void setError_id(Long error_id){
		this.error_id=error_id;
	}
	/** 设置：错误ID */
	public void setError_id(String error_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(error_id)){
			this.error_id=new Long(error_id);
		}
	}
	/** 取得：error_msg */
	public String getError_msg(){
		return error_msg;
	}
	/** 设置：error_msg */
	public void setError_msg(String error_msg){
		this.error_msg=error_msg;
	}
	/** 取得：作业执行结果ID */
	public String getJob_rs_id(){
		return job_rs_id;
	}
	/** 设置：作业执行结果ID */
	public void setJob_rs_id(String job_rs_id){
		this.job_rs_id=job_rs_id;
	}
}
