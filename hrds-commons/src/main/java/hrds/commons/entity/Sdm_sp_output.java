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
 * StreamingPro作业输出信息表
 */
@Table(tableName = "sdm_sp_output")
public class Sdm_sp_output extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_sp_output";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamingPro作业输出信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_info_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sdm_info_id",value="作业输出信息表id:",dataType = Long.class,required = true)
	private Long sdm_info_id;
	@DocBean(name ="output_type",value="输出类型(SdmSpInputOutputType):1-文本文件<WENBENWENJIAN> 2-数据库表<SHUJUKUBIAO> 3-消费主题<XIAOFEIZHUTI> 4-内部表<NEIBUBIAO> ",dataType = String.class,required = true)
	private String output_type;
	@DocBean(name ="output_mode",value="输出模式(SdmSpOutputMode):1-Append<APPEND> 2-Update<UPDATE> 3-Complete<COMPLETE> ",dataType = String.class,required = true)
	private String output_mode;
	@DocBean(name ="output_table_name",value="输入表名称:",dataType = String.class,required = true)
	private String output_table_name;
	@DocBean(name ="output_number",value="序号:",dataType = Long.class,required = true)
	private Long output_number;
	@DocBean(name ="stream_tablename",value="输出到流表的表名:",dataType = String.class,required = false)
	private String stream_tablename;
	@DocBean(name ="ssj_job_id",value="作业id:",dataType = Long.class,required = true)
	private Long ssj_job_id;

	/** 取得：作业输出信息表id */
	public Long getSdm_info_id(){
		return sdm_info_id;
	}
	/** 设置：作业输出信息表id */
	public void setSdm_info_id(Long sdm_info_id){
		this.sdm_info_id=sdm_info_id;
	}
	/** 设置：作业输出信息表id */
	public void setSdm_info_id(String sdm_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_info_id)){
			this.sdm_info_id=new Long(sdm_info_id);
		}
	}
	/** 取得：输出类型 */
	public String getOutput_type(){
		return output_type;
	}
	/** 设置：输出类型 */
	public void setOutput_type(String output_type){
		this.output_type=output_type;
	}
	/** 取得：输出模式 */
	public String getOutput_mode(){
		return output_mode;
	}
	/** 设置：输出模式 */
	public void setOutput_mode(String output_mode){
		this.output_mode=output_mode;
	}
	/** 取得：输入表名称 */
	public String getOutput_table_name(){
		return output_table_name;
	}
	/** 设置：输入表名称 */
	public void setOutput_table_name(String output_table_name){
		this.output_table_name=output_table_name;
	}
	/** 取得：序号 */
	public Long getOutput_number(){
		return output_number;
	}
	/** 设置：序号 */
	public void setOutput_number(Long output_number){
		this.output_number=output_number;
	}
	/** 设置：序号 */
	public void setOutput_number(String output_number){
		if(!fd.ng.core.utils.StringUtil.isEmpty(output_number)){
			this.output_number=new Long(output_number);
		}
	}
	/** 取得：输出到流表的表名 */
	public String getStream_tablename(){
		return stream_tablename;
	}
	/** 设置：输出到流表的表名 */
	public void setStream_tablename(String stream_tablename){
		this.stream_tablename=stream_tablename;
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
