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
 * 数据对标外键信息表
 */
@Table(tableName = "dbm_fk_info_tab")
public class Dbm_fk_info_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_fk_info_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标外键信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("fk_sys_class_code");
		__tmpPKS.add("fk_name");
		__tmpPKS.add("fk_table_owner");
		__tmpPKS.add("fk_table_code");
		__tmpPKS.add("fk_col_code");
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_schema");
		__tmpPKS.add("table_code");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="fk_sys_class_code",value="主表所在系统分类编码:",dataType = String.class,required = true)
	private String fk_sys_class_code;
	@DocBean(name ="fk_name",value="外键名称:",dataType = String.class,required = true)
	private String fk_name;
	@DocBean(name ="fk_table_owner",value="主表所属者:",dataType = String.class,required = true)
	private String fk_table_owner;
	@DocBean(name ="fk_table_code",value="主表表编码:",dataType = String.class,required = true)
	private String fk_table_code;
	@DocBean(name ="fk_col_code",value="主键字段编码:",dataType = String.class,required = true)
	private String fk_col_code;
	@DocBean(name ="sys_class_code",value="从表所在系统分类编码:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_schema",value="从表所在schema:",dataType = String.class,required = true)
	private String table_schema;
	@DocBean(name ="table_code",value="从表编码:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="col_code",value="外键字段编码:",dataType = String.class,required = false)
	private String col_code;
	@DocBean(name ="st_tm",value="开始时间:",dataType = String.class,required = false)
	private String st_tm;
	@DocBean(name ="end_tm",value="结束时间:",dataType = String.class,required = false)
	private String end_tm;
	@DocBean(name ="data_src",value="数据来源:",dataType = String.class,required = false)
	private String data_src;
	@DocBean(name ="id",value="外键信息表主键UUID存储:",dataType = String.class,required = false)
	private String id;

	/** 取得：主表所在系统分类编码 */
	public String getFk_sys_class_code(){
		return fk_sys_class_code;
	}
	/** 设置：主表所在系统分类编码 */
	public void setFk_sys_class_code(String fk_sys_class_code){
		this.fk_sys_class_code=fk_sys_class_code;
	}
	/** 取得：外键名称 */
	public String getFk_name(){
		return fk_name;
	}
	/** 设置：外键名称 */
	public void setFk_name(String fk_name){
		this.fk_name=fk_name;
	}
	/** 取得：主表所属者 */
	public String getFk_table_owner(){
		return fk_table_owner;
	}
	/** 设置：主表所属者 */
	public void setFk_table_owner(String fk_table_owner){
		this.fk_table_owner=fk_table_owner;
	}
	/** 取得：主表表编码 */
	public String getFk_table_code(){
		return fk_table_code;
	}
	/** 设置：主表表编码 */
	public void setFk_table_code(String fk_table_code){
		this.fk_table_code=fk_table_code;
	}
	/** 取得：主键字段编码 */
	public String getFk_col_code(){
		return fk_col_code;
	}
	/** 设置：主键字段编码 */
	public void setFk_col_code(String fk_col_code){
		this.fk_col_code=fk_col_code;
	}
	/** 取得：从表所在系统分类编码 */
	public String getSys_class_code(){
		return sys_class_code;
	}
	/** 设置：从表所在系统分类编码 */
	public void setSys_class_code(String sys_class_code){
		this.sys_class_code=sys_class_code;
	}
	/** 取得：从表所在schema */
	public String getTable_schema(){
		return table_schema;
	}
	/** 设置：从表所在schema */
	public void setTable_schema(String table_schema){
		this.table_schema=table_schema;
	}
	/** 取得：从表编码 */
	public String getTable_code(){
		return table_code;
	}
	/** 设置：从表编码 */
	public void setTable_code(String table_code){
		this.table_code=table_code;
	}
	/** 取得：外键字段编码 */
	public String getCol_code(){
		return col_code;
	}
	/** 设置：外键字段编码 */
	public void setCol_code(String col_code){
		this.col_code=col_code;
	}
	/** 取得：开始时间 */
	public String getSt_tm(){
		return st_tm;
	}
	/** 设置：开始时间 */
	public void setSt_tm(String st_tm){
		this.st_tm=st_tm;
	}
	/** 取得：结束时间 */
	public String getEnd_tm(){
		return end_tm;
	}
	/** 设置：结束时间 */
	public void setEnd_tm(String end_tm){
		this.end_tm=end_tm;
	}
	/** 取得：数据来源 */
	public String getData_src(){
		return data_src;
	}
	/** 设置：数据来源 */
	public void setData_src(String data_src){
		this.data_src=data_src;
	}
	/** 取得：外键信息表主键UUID存储 */
	public String getId(){
		return id;
	}
	/** 设置：外键信息表主键UUID存储 */
	public void setId(String id){
		this.id=id;
	}
}
