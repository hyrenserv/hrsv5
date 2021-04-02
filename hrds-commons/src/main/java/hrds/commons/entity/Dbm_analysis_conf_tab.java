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
 * 数据对标分析配置表
 */
@Table(tableName = "dbm_analysis_conf_tab")
public class Dbm_analysis_conf_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_analysis_conf_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }
	/** 数据对标分析配置表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("ori_table_code");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编号:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="ori_table_code",value="原始表编号:",dataType = String.class,required = true)
	private String ori_table_code;
	@DocBean(name ="trans_table_code",value="转换后表编号:",dataType = String.class,required = false)
	private String trans_table_code;
	@DocBean(name ="etl_date",value="数据时间:",dataType = String.class,required = false)
	private String etl_date;
	@DocBean(name ="date_offset",value="时间偏移量:",dataType = String.class,required = false)
	private String date_offset;
	@DocBean(name ="feature_flag",value="是否进行字段特征分析:",dataType = String.class,required = true)
	private String feature_flag;
	@DocBean(name ="fd_flag",value="是否进行函数依赖分析:",dataType = String.class,required = true)
	private String fd_flag;
	@DocBean(name ="pk_flag",value="是否进行主键分析:",dataType = String.class,required = true)
	private String pk_flag;
	@DocBean(name ="fk_flag",value="是否进行单一外键分析:",dataType = String.class,required = true)
	private String fk_flag;
	@DocBean(name ="joint_fk_flag",value="是否进行联合外键分析:",dataType = String.class,required = true)
	private String joint_fk_flag;
	@DocBean(name ="fd_check_flag",value="是否进行函数依赖验证:",dataType = String.class,required = true)
	private String fd_check_flag;
	@DocBean(name ="dim_flag",value="是否进行维度划分:",dataType = String.class,required = true)
	private String dim_flag;
	@DocBean(name ="incre_to_full_flag",value="是否将表转为全量:",dataType = String.class,required = true)
	private String incre_to_full_flag;
	@DocBean(name ="ana_alg",value="分析算法:",dataType = String.class,required = true)
	private String ana_alg;
	@DocBean(name ="fd_sample_count",value="函数依赖分析采样量:",dataType = String.class,required = false)
	private String fd_sample_count;
	@DocBean(name ="to_ana_tb_pk",value="待分析表主键:",dataType = String.class,required = false)
	private String to_ana_tb_pk;
	@DocBean(name ="fk_ana_mode",value="外键分析模式:",dataType = String.class,required = false)
	private String fk_ana_mode;
	@DocBean(name ="joint_fk_ana_mode",value="联合外键分析模式:",dataType = String.class,required = false)
	private String joint_fk_ana_mode;
	@DocBean(name ="etl_date_filter",value="跑批日期过滤条件:",dataType = String.class,required = false)
	private String etl_date_filter;
	@DocBean(name ="end_date_filter",value="结束日期过滤条件:",dataType = String.class,required = false)
	private String end_date_filter;

	/** 取得：系统分类编号 */
	public String getSys_class_code(){
		return sys_class_code;
	}
	/** 设置：系统分类编号 */
	public void setSys_class_code(String sys_class_code){
		this.sys_class_code=sys_class_code;
	}
	/** 取得：原始表编号 */
	public String getOri_table_code(){
		return ori_table_code;
	}
	/** 设置：原始表编号 */
	public void setOri_table_code(String ori_table_code){
		this.ori_table_code=ori_table_code;
	}
	/** 取得：转换后表编号 */
	public String getTrans_table_code(){
		return trans_table_code;
	}
	/** 设置：转换后表编号 */
	public void setTrans_table_code(String trans_table_code){
		this.trans_table_code=trans_table_code;
	}
	/** 取得：数据时间 */
	public String getEtl_date(){
		return etl_date;
	}
	/** 设置：数据时间 */
	public void setEtl_date(String etl_date){
		this.etl_date=etl_date;
	}
	/** 取得：时间偏移量 */
	public String getDate_offset(){
		return date_offset;
	}
	/** 设置：时间偏移量 */
	public void setDate_offset(String date_offset){
		this.date_offset=date_offset;
	}
	/** 取得：是否进行字段特征分析 */
	public String getFeature_flag(){
		return feature_flag;
	}
	/** 设置：是否进行字段特征分析 */
	public void setFeature_flag(String feature_flag){
		this.feature_flag=feature_flag;
	}
	/** 取得：是否进行函数依赖分析 */
	public String getFd_flag(){
		return fd_flag;
	}
	/** 设置：是否进行函数依赖分析 */
	public void setFd_flag(String fd_flag){
		this.fd_flag=fd_flag;
	}
	/** 取得：是否进行主键分析 */
	public String getPk_flag(){
		return pk_flag;
	}
	/** 设置：是否进行主键分析 */
	public void setPk_flag(String pk_flag){
		this.pk_flag=pk_flag;
	}
	/** 取得：是否进行单一外键分析 */
	public String getFk_flag(){
		return fk_flag;
	}
	/** 设置：是否进行单一外键分析 */
	public void setFk_flag(String fk_flag){
		this.fk_flag=fk_flag;
	}
	/** 取得：是否进行联合外键分析 */
	public String getJoint_fk_flag(){
		return joint_fk_flag;
	}
	/** 设置：是否进行联合外键分析 */
	public void setJoint_fk_flag(String joint_fk_flag){
		this.joint_fk_flag=joint_fk_flag;
	}
	/** 取得：是否进行函数依赖验证 */
	public String getFd_check_flag(){
		return fd_check_flag;
	}
	/** 设置：是否进行函数依赖验证 */
	public void setFd_check_flag(String fd_check_flag){
		this.fd_check_flag=fd_check_flag;
	}
	/** 取得：是否进行维度划分 */
	public String getDim_flag(){
		return dim_flag;
	}
	/** 设置：是否进行维度划分 */
	public void setDim_flag(String dim_flag){
		this.dim_flag=dim_flag;
	}
	/** 取得：是否将表转为全量 */
	public String getIncre_to_full_flag(){
		return incre_to_full_flag;
	}
	/** 设置：是否将表转为全量 */
	public void setIncre_to_full_flag(String incre_to_full_flag){
		this.incre_to_full_flag=incre_to_full_flag;
	}
	/** 取得：分析算法 */
	public String getAna_alg(){
		return ana_alg;
	}
	/** 设置：分析算法 */
	public void setAna_alg(String ana_alg){
		this.ana_alg=ana_alg;
	}
	/** 取得：函数依赖分析采样量 */
	public String getFd_sample_count(){
		return fd_sample_count;
	}
	/** 设置：函数依赖分析采样量 */
	public void setFd_sample_count(String fd_sample_count){
		this.fd_sample_count=fd_sample_count;
	}
	/** 取得：待分析表主键 */
	public String getTo_ana_tb_pk(){
		return to_ana_tb_pk;
	}
	/** 设置：待分析表主键 */
	public void setTo_ana_tb_pk(String to_ana_tb_pk){
		this.to_ana_tb_pk=to_ana_tb_pk;
	}
	/** 取得：外键分析模式 */
	public String getFk_ana_mode(){
		return fk_ana_mode;
	}
	/** 设置：外键分析模式 */
	public void setFk_ana_mode(String fk_ana_mode){
		this.fk_ana_mode=fk_ana_mode;
	}
	/** 取得：联合外键分析模式 */
	public String getJoint_fk_ana_mode(){
		return joint_fk_ana_mode;
	}
	/** 设置：联合外键分析模式 */
	public void setJoint_fk_ana_mode(String joint_fk_ana_mode){
		this.joint_fk_ana_mode=joint_fk_ana_mode;
	}
	/** 取得：跑批日期过滤条件 */
	public String getEtl_date_filter() {
		return etl_date_filter;
	}
	/** 设置：跑批日期过滤条件 */
	public void setEtl_date_filter(String etl_date_filter) {
		this.etl_date_filter = etl_date_filter;
	}
	/** 取得：结束日期过滤条件 */
	public String getEnd_date_filter() {
		return end_date_filter;
	}
	/** 设置：结束日期过滤条件 */
	public void setEnd_date_filter(String end_date_filter) {
		this.end_date_filter = end_date_filter;
	}
}
