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
 * 数据对标字段信息表
 */
@Table(tableName = "dbm_mmm_field_info_tab")
public class Dbm_mmm_field_info_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_mmm_field_info_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标字段信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_schema");
		__tmpPKS.add("table_code");
		__tmpPKS.add("col_num");
		__tmpPKS.add("col_code");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编号:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_schema",value="表所属schema:",dataType = String.class,required = true)
	private String table_schema;
	@DocBean(name ="table_code",value="表编号:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="col_num",value="字段编号:",dataType = Integer.class,required = true)
	private Integer col_num;
	@DocBean(name ="col_code",value="字段英文名:",dataType = String.class,required = true)
	private String col_code;
	@DocBean(name ="col_name",value="字段中文名:",dataType = String.class,required = false)
	private String col_name;
	@DocBean(name ="col_comment",value="字段注释:",dataType = String.class,required = false)
	private String col_comment;
	@DocBean(name ="col_type",value="字段类型:",dataType = String.class,required = true)
	private String col_type;
	@DocBean(name ="col_type_judge_rate",value="字段类型判断比例:",dataType = BigDecimal.class,required = false)
	private BigDecimal col_type_judge_rate;
	@DocBean(name ="col_length",value="字段长度:",dataType = String.class,required = false)
	private String col_length;
	@DocBean(name ="col_nullable",value="字段是否可为空(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String col_nullable;
	@DocBean(name ="col_pk",value="字段是否为主键(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String col_pk;
	@DocBean(name ="is_std",value="是否按标(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_std;
	@DocBean(name ="cdval_no",value="码值编号:",dataType = String.class,required = false)
	private String cdval_no;
	@DocBean(name ="col_check",value="约束条件:",dataType = String.class,required = false)
	private String col_check;
	@DocBean(name ="coltra",value="转换类型:",dataType = String.class,required = false)
	private String coltra;
	@DocBean(name ="colformat",value="字段格式:",dataType = String.class,required = false)
	private String colformat;
	@DocBean(name ="tratype",value="转换后数据类型:",dataType = String.class,required = false)
	private String tratype;
	@DocBean(name ="st_tm",value="开始时间:",dataType = String.class,required = true)
	private String st_tm;
	@DocBean(name ="end_tm",value="结束时间:",dataType = String.class,required = true)
	private String end_tm;
	@DocBean(name ="data_src",value="数据来源:",dataType = String.class,required = false)
	private String data_src;
	@DocBean(name ="col_autoincre",value="字段值是否为自增序列(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String col_autoincre;
	@DocBean(name ="col_defult",value="字段值是否是默认值(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String col_defult;

	/** 取得：系统分类编号 */
	public String getSys_class_code(){
		return sys_class_code;
	}
	/** 设置：系统分类编号 */
	public void setSys_class_code(String sys_class_code){
		this.sys_class_code=sys_class_code;
	}
	/** 取得：表所属schema */
	public String getTable_schema(){
		return table_schema;
	}
	/** 设置：表所属schema */
	public void setTable_schema(String table_schema){
		this.table_schema=table_schema;
	}
	/** 取得：表编号 */
	public String getTable_code(){
		return table_code;
	}
	/** 设置：表编号 */
	public void setTable_code(String table_code){
		this.table_code=table_code;
	}
	/** 取得：字段编号 */
	public Integer getCol_num(){
		return col_num;
	}
	/** 设置：字段编号 */
	public void setCol_num(Integer col_num){
		this.col_num=col_num;
	}
	/** 设置：字段编号 */
	public void setCol_num(String col_num){
		if(!fd.ng.core.utils.StringUtil.isEmpty(col_num)){
			this.col_num=new Integer(col_num);
		}
	}
	/** 取得：字段英文名 */
	public String getCol_code(){
		return col_code;
	}
	/** 设置：字段英文名 */
	public void setCol_code(String col_code){
		this.col_code=col_code;
	}
	/** 取得：字段中文名 */
	public String getCol_name(){
		return col_name;
	}
	/** 设置：字段中文名 */
	public void setCol_name(String col_name){
		this.col_name=col_name;
	}
	/** 取得：字段注释 */
	public String getCol_comment(){
		return col_comment;
	}
	/** 设置：字段注释 */
	public void setCol_comment(String col_comment){
		this.col_comment=col_comment;
	}
	/** 取得：字段类型 */
	public String getCol_type(){
		return col_type;
	}
	/** 设置：字段类型 */
	public void setCol_type(String col_type){
		this.col_type=col_type;
	}
	/** 取得：字段类型判断比例 */
	public BigDecimal getCol_type_judge_rate(){
		return col_type_judge_rate;
	}
	/** 设置：字段类型判断比例 */
	public void setCol_type_judge_rate(BigDecimal col_type_judge_rate){
		this.col_type_judge_rate=col_type_judge_rate;
	}
	/** 设置：字段类型判断比例 */
	public void setCol_type_judge_rate(String col_type_judge_rate){
		if(!fd.ng.core.utils.StringUtil.isEmpty(col_type_judge_rate)){
			this.col_type_judge_rate=new BigDecimal(col_type_judge_rate);
		}
	}
	/** 取得：字段长度 */
	public String getCol_length(){
		return col_length;
	}
	/** 设置：字段长度 */
	public void setCol_length(String col_length){
		this.col_length=col_length;
	}
	/** 取得：字段是否可为空 */
	public String getCol_nullable(){
		return col_nullable;
	}
	/** 设置：字段是否可为空 */
	public void setCol_nullable(String col_nullable){
		this.col_nullable=col_nullable;
	}
	/** 取得：字段是否为主键 */
	public String getCol_pk(){
		return col_pk;
	}
	/** 设置：字段是否为主键 */
	public void setCol_pk(String col_pk){
		this.col_pk=col_pk;
	}
	/** 取得：是否按标 */
	public String getIs_std(){
		return is_std;
	}
	/** 设置：是否按标 */
	public void setIs_std(String is_std){
		this.is_std=is_std;
	}
	/** 取得：码值编号 */
	public String getCdval_no(){
		return cdval_no;
	}
	/** 设置：码值编号 */
	public void setCdval_no(String cdval_no){
		this.cdval_no=cdval_no;
	}
	/** 取得：约束条件 */
	public String getCol_check(){
		return col_check;
	}
	/** 设置：约束条件 */
	public void setCol_check(String col_check){
		this.col_check=col_check;
	}
	/** 取得：转换类型 */
	public String getColtra(){
		return coltra;
	}
	/** 设置：转换类型 */
	public void setColtra(String coltra){
		this.coltra=coltra;
	}
	/** 取得：字段格式 */
	public String getColformat(){
		return colformat;
	}
	/** 设置：字段格式 */
	public void setColformat(String colformat){
		this.colformat=colformat;
	}
	/** 取得：转换后数据类型 */
	public String getTratype(){
		return tratype;
	}
	/** 设置：转换后数据类型 */
	public void setTratype(String tratype){
		this.tratype=tratype;
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
	/** 取得：字段值是否为自增序列 */
	public String getCol_autoincre(){
		return col_autoincre;
	}
	/** 设置：字段值是否为自增序列 */
	public void setCol_autoincre(String col_autoincre){
		this.col_autoincre=col_autoincre;
	}
	/** 取得：字段值是否是默认值 */
	public String getCol_defult(){
		return col_defult;
	}
	/** 设置：字段值是否是默认值 */
	public void setCol_defult(String col_defult){
		this.col_defult=col_defult;
	}
}
