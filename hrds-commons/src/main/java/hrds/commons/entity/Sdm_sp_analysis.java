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
 * StreamingPro作业分析信息表
 */
@Table(tableName = "sdm_sp_analysis")
public class Sdm_sp_analysis extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_analysis";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro作业分析信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("ssa_info_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ssa_info_id",value="分析信息表id:",dataType = Long.class,required = true)
	private Long ssa_info_id;
	@DocBean(name ="analysis_table_name",value="输出表名:",dataType = String.class,required = true)
	private String analysis_table_name;
	@DocBean(name ="analysis_sql",value="分析sql:",dataType = String.class,required = true)
	private String analysis_sql;
	@DocBean(name ="analysis_number",value="序号:",dataType = Long.class,required = true)
	private Long analysis_number;
	@DocBean(name ="ssj_job_id",value="作业id:",dataType = Long.class,required = true)
	private Long ssj_job_id;

	/** 取得：分析信息表id */
	public Long getSsa_info_id(){
		return ssa_info_id;
	}
	/** 设置：分析信息表id */
	public void setSsa_info_id(Long ssa_info_id){
		this.ssa_info_id=ssa_info_id;
	}
	/** 设置：分析信息表id */
	public void setSsa_info_id(String ssa_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ssa_info_id)){
			this.ssa_info_id=new Long(ssa_info_id);
		}
	}
	/** 取得：输出表名 */
	public String getAnalysis_table_name(){
		return analysis_table_name;
	}
	/** 设置：输出表名 */
	public void setAnalysis_table_name(String analysis_table_name){
		this.analysis_table_name=analysis_table_name;
	}
	/** 取得：分析sql */
	public String getAnalysis_sql(){
		return analysis_sql;
	}
	/** 设置：分析sql */
	public void setAnalysis_sql(String analysis_sql){
		this.analysis_sql=analysis_sql;
	}
	/** 取得：序号 */
	public Long getAnalysis_number(){
		return analysis_number;
	}
	/** 设置：序号 */
	public void setAnalysis_number(Long analysis_number){
		this.analysis_number=analysis_number;
	}
	/** 设置：序号 */
	public void setAnalysis_number(String analysis_number){
		if(!fd.ng.core.utils.StringUtil.isEmpty(analysis_number)){
			this.analysis_number=new Long(analysis_number);
		}
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
