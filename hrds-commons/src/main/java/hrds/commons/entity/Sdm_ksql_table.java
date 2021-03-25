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
 * 任务/topic映射表
 */
@Table(tableName = "sdm_ksql_table")
public class Sdm_ksql_table extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_ksql_table";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 任务/topic映射表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_ksql_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="stram_table",value="表名:",dataType = String.class,required = true)
	private String stram_table;
	@DocBean(name ="create_time",value="创建时间:",dataType = String.class,required = true)
	private String create_time;
	@DocBean(name ="create_date",value="创建日期:",dataType = String.class,required = true)
	private String create_date;
	@DocBean(name ="table_type",value="数据表格式（流、表、目的地）:",dataType = String.class,required = true)
	private String table_type;
	@DocBean(name ="table_remark",value="备注:",dataType = String.class,required = false)
	private String table_remark;
	@DocBean(name ="sdm_top_name",value="topic名称:",dataType = String.class,required = false)
	private String sdm_top_name;
	@DocBean(name ="sdm_ksql_id",value="映射表主键:",dataType = Long.class,required = true)
	private Long sdm_ksql_id;
	@DocBean(name ="execute_sql",value="执行的sql:",dataType = String.class,required = true)
	private String execute_sql;
	@DocBean(name ="is_create_sql",value="是否基于映射表创建(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_create_sql;
	@DocBean(name ="consumer_name",value="消费名称:",dataType = String.class,required = true)
	private String consumer_name;
	@DocBean(name ="job_desc",value="任务描述:",dataType = String.class,required = false)
	private String job_desc;
	@DocBean(name ="auto_offset",value="数据拉取初始位置:",dataType = String.class,required = false)
	private String auto_offset;
	@DocBean(name ="sdm_receive_id",value="流数据管理:",dataType = Long.class,required = true)
	private Long sdm_receive_id;

	/** 取得：表名 */
	public String getStram_table(){
		return stram_table;
	}
	/** 设置：表名 */
	public void setStram_table(String stram_table){
		this.stram_table=stram_table;
	}
	/** 取得：创建时间 */
	public String getCreate_time(){
		return create_time;
	}
	/** 设置：创建时间 */
	public void setCreate_time(String create_time){
		this.create_time=create_time;
	}
	/** 取得：创建日期 */
	public String getCreate_date(){
		return create_date;
	}
	/** 设置：创建日期 */
	public void setCreate_date(String create_date){
		this.create_date=create_date;
	}
	/** 取得：数据表格式（流、表、目的地） */
	public String getTable_type(){
		return table_type;
	}
	/** 设置：数据表格式（流、表、目的地） */
	public void setTable_type(String table_type){
		this.table_type=table_type;
	}
	/** 取得：备注 */
	public String getTable_remark(){
		return table_remark;
	}
	/** 设置：备注 */
	public void setTable_remark(String table_remark){
		this.table_remark=table_remark;
	}
	/** 取得：topic名称 */
	public String getSdm_top_name(){
		return sdm_top_name;
	}
	/** 设置：topic名称 */
	public void setSdm_top_name(String sdm_top_name){
		this.sdm_top_name=sdm_top_name;
	}
	/** 取得：映射表主键 */
	public Long getSdm_ksql_id(){
		return sdm_ksql_id;
	}
	/** 设置：映射表主键 */
	public void setSdm_ksql_id(Long sdm_ksql_id){
		this.sdm_ksql_id=sdm_ksql_id;
	}
	/** 设置：映射表主键 */
	public void setSdm_ksql_id(String sdm_ksql_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_ksql_id)){
			this.sdm_ksql_id=new Long(sdm_ksql_id);
		}
	}
	/** 取得：执行的sql */
	public String getExecute_sql(){
		return execute_sql;
	}
	/** 设置：执行的sql */
	public void setExecute_sql(String execute_sql){
		this.execute_sql=execute_sql;
	}
	/** 取得：是否基于映射表创建 */
	public String getIs_create_sql(){
		return is_create_sql;
	}
	/** 设置：是否基于映射表创建 */
	public void setIs_create_sql(String is_create_sql){
		this.is_create_sql=is_create_sql;
	}
	/** 取得：消费名称 */
	public String getConsumer_name(){
		return consumer_name;
	}
	/** 设置：消费名称 */
	public void setConsumer_name(String consumer_name){
		this.consumer_name=consumer_name;
	}
	/** 取得：任务描述 */
	public String getJob_desc(){
		return job_desc;
	}
	/** 设置：任务描述 */
	public void setJob_desc(String job_desc){
		this.job_desc=job_desc;
	}
	/** 取得：数据拉取初始位置 */
	public String getAuto_offset(){
		return auto_offset;
	}
	/** 设置：数据拉取初始位置 */
	public void setAuto_offset(String auto_offset){
		this.auto_offset=auto_offset;
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
