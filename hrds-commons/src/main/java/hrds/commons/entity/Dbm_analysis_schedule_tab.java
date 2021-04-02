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
 * 数据对标分析进度表
 */
@Table(tableName = "dbm_analysis_schedule_tab")
public class Dbm_analysis_schedule_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_analysis_schedule_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标分析进度表 */
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
	@DocBean(name ="feature_sche",value="字段特征分析进度:",dataType = String.class,required = false)
	private String feature_sche;
	@DocBean(name ="feature_start_date",value="字段特征分析开始时间:",dataType = String.class,required = false)
	private String feature_start_date;
	@DocBean(name ="feature_end_date",value="字段特征分析结束时间:",dataType = String.class,required = false)
	private String feature_end_date;
	@DocBean(name ="fd_sche",value="函数依赖分析进度:",dataType = String.class,required = false)
	private String fd_sche;
	@DocBean(name ="fd_start_date",value="函数依赖分析开始时间:",dataType = String.class,required = false)
	private String fd_start_date;
	@DocBean(name ="fd_end_date",value="函数依赖分析结束时间:",dataType = String.class,required = false)
	private String fd_end_date;
	@DocBean(name ="pk_sche",value="主键分析进度:",dataType = String.class,required = false)
	private String pk_sche;
	@DocBean(name ="pk_start_date",value="主键分析开始时间:",dataType = String.class,required = false)
	private String pk_start_date;
	@DocBean(name ="pk_end_date",value="主键分析结束时间:",dataType = String.class,required = false)
	private String pk_end_date;
	@DocBean(name ="fk_sche",value="外键分析进度:",dataType = String.class,required = false)
	private String fk_sche;
	@DocBean(name ="fk_start_date",value="外键分析开始时间:",dataType = String.class,required = false)
	private String fk_start_date;
	@DocBean(name ="fk_end_date",value="外键分析结束时间:",dataType = String.class,required = false)
	private String fk_end_date;
	@DocBean(name ="fd_check_sche",value="函数依赖验证进度:",dataType = String.class,required = false)
	private String fd_check_sche;
	@DocBean(name ="fd_check_start_date",value="函数依赖验证开始时间:",dataType = String.class,required = false)
	private String fd_check_start_date;
	@DocBean(name ="fd_check_end_date",value="函数依赖验证结束时间:",dataType = String.class,required = false)
	private String fd_check_end_date;
	@DocBean(name ="joint_fk_sche",value="联合外键分析进度:",dataType = String.class,required = false)
	private String joint_fk_sche;
	@DocBean(name ="joint_fk_start_date",value="联合外键分析开始时间:",dataType = String.class,required = false)
	private String joint_fk_start_date;
	@DocBean(name ="joint_fk_end_date",value="联合外键分析结束时间:",dataType = String.class,required = false)
	private String joint_fk_end_date;
	@DocBean(name ="dim_sche",value="维度划分进度:",dataType = String.class,required = false)
	private String dim_sche;
	@DocBean(name ="dim_start_date",value="维度划分进度开始时间:",dataType = String.class,required = false)
	private String dim_start_date;
	@DocBean(name ="dim_end_date",value="维度划分进度结束时间:",dataType = String.class,required = false)
	private String dim_end_date;
	@DocBean(name ="incre_to_full_sche",value="表转为全量进度:",dataType = String.class,required = false)
	private String incre_to_full_sche;
	@DocBean(name ="incre_to_full_start_date",value="表转为全量开始时间:",dataType = String.class,required = false)
	private String incre_to_full_start_date;
	@DocBean(name ="incre_to_full_end_date",value="表转为全量结束时间:",dataType = String.class,required = false)
	private String incre_to_full_end_date;

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
	/** 取得：字段特征分析进度 */
	public String getFeature_sche(){
		return feature_sche;
	}
	/** 设置：字段特征分析进度 */
	public void setFeature_sche(String feature_sche){
		this.feature_sche=feature_sche;
	}
	/** 取得：字段特征分析开始时间 */
	public String getFeature_start_date(){
		return feature_start_date;
	}
	/** 设置：字段特征分析开始时间 */
	public void setFeature_start_date(String feature_start_date){
		this.feature_start_date=feature_start_date;
	}
	/** 取得：字段特征分析结束时间 */
	public String getFeature_end_date(){
		return feature_end_date;
	}
	/** 设置：字段特征分析结束时间 */
	public void setFeature_end_date(String feature_end_date){
		this.feature_end_date=feature_end_date;
	}
	/** 取得：函数依赖分析进度 */
	public String getFd_sche(){
		return fd_sche;
	}
	/** 设置：函数依赖分析进度 */
	public void setFd_sche(String fd_sche){
		this.fd_sche=fd_sche;
	}
	/** 取得：函数依赖分析开始时间 */
	public String getFd_start_date(){
		return fd_start_date;
	}
	/** 设置：函数依赖分析开始时间 */
	public void setFd_start_date(String fd_start_date){
		this.fd_start_date=fd_start_date;
	}
	/** 取得：函数依赖分析结束时间 */
	public String getFd_end_date(){
		return fd_end_date;
	}
	/** 设置：函数依赖分析结束时间 */
	public void setFd_end_date(String fd_end_date){
		this.fd_end_date=fd_end_date;
	}
	/** 取得：主键分析进度 */
	public String getPk_sche(){
		return pk_sche;
	}
	/** 设置：主键分析进度 */
	public void setPk_sche(String pk_sche){
		this.pk_sche=pk_sche;
	}
	/** 取得：主键分析开始时间 */
	public String getPk_start_date(){
		return pk_start_date;
	}
	/** 设置：主键分析开始时间 */
	public void setPk_start_date(String pk_start_date){
		this.pk_start_date=pk_start_date;
	}
	/** 取得：主键分析结束时间 */
	public String getPk_end_date(){
		return pk_end_date;
	}
	/** 设置：主键分析结束时间 */
	public void setPk_end_date(String pk_end_date){
		this.pk_end_date=pk_end_date;
	}
	/** 取得：外键分析进度 */
	public String getFk_sche(){
		return fk_sche;
	}
	/** 设置：外键分析进度 */
	public void setFk_sche(String fk_sche){
		this.fk_sche=fk_sche;
	}
	/** 取得：外键分析开始时间 */
	public String getFk_start_date(){
		return fk_start_date;
	}
	/** 设置：外键分析开始时间 */
	public void setFk_start_date(String fk_start_date){
		this.fk_start_date=fk_start_date;
	}
	/** 取得：外键分析结束时间 */
	public String getFk_end_date(){
		return fk_end_date;
	}
	/** 设置：外键分析结束时间 */
	public void setFk_end_date(String fk_end_date){
		this.fk_end_date=fk_end_date;
	}
	/** 取得：函数依赖验证进度 */
	public String getFd_check_sche(){
		return fd_check_sche;
	}
	/** 设置：函数依赖验证进度 */
	public void setFd_check_sche(String fd_check_sche){
		this.fd_check_sche=fd_check_sche;
	}
	/** 取得：函数依赖验证开始时间 */
	public String getFd_check_start_date(){
		return fd_check_start_date;
	}
	/** 设置：函数依赖验证开始时间 */
	public void setFd_check_start_date(String fd_check_start_date){
		this.fd_check_start_date=fd_check_start_date;
	}
	/** 取得：函数依赖验证结束时间 */
	public String getFd_check_end_date(){
		return fd_check_end_date;
	}
	/** 设置：函数依赖验证结束时间 */
	public void setFd_check_end_date(String fd_check_end_date){
		this.fd_check_end_date=fd_check_end_date;
	}
	/** 取得：联合外键分析进度 */
	public String getJoint_fk_sche(){
		return joint_fk_sche;
	}
	/** 设置：联合外键分析进度 */
	public void setJoint_fk_sche(String joint_fk_sche){
		this.joint_fk_sche=joint_fk_sche;
	}
	/** 取得：联合外键分析开始时间 */
	public String getJoint_fk_start_date(){
		return joint_fk_start_date;
	}
	/** 设置：联合外键分析开始时间 */
	public void setJoint_fk_start_date(String joint_fk_start_date){
		this.joint_fk_start_date=joint_fk_start_date;
	}
	/** 取得：联合外键分析结束时间 */
	public String getJoint_fk_end_date(){
		return joint_fk_end_date;
	}
	/** 设置：联合外键分析结束时间 */
	public void setJoint_fk_end_date(String joint_fk_end_date){
		this.joint_fk_end_date=joint_fk_end_date;
	}
	/** 取得：维度划分进度 */
	public String getDim_sche(){
		return dim_sche;
	}
	/** 设置：维度划分进度 */
	public void setDim_sche(String dim_sche){
		this.dim_sche=dim_sche;
	}
	/** 取得：维度划分进度开始时间 */
	public String getDim_start_date(){
		return dim_start_date;
	}
	/** 设置：维度划分进度开始时间 */
	public void setDim_start_date(String dim_start_date){
		this.dim_start_date=dim_start_date;
	}
	/** 取得：维度划分进度结束时间 */
	public String getDim_end_date(){
		return dim_end_date;
	}
	/** 设置：维度划分进度结束时间 */
	public void setDim_end_date(String dim_end_date){
		this.dim_end_date=dim_end_date;
	}
	/** 取得：表转为全量进度 */
	public String getIncre_to_full_sche(){
		return incre_to_full_sche;
	}
	/** 设置：表转为全量进度 */
	public void setIncre_to_full_sche(String incre_to_full_sche){
		this.incre_to_full_sche=incre_to_full_sche;
	}
	/** 取得：表转为全量开始时间 */
	public String getIncre_to_full_start_date(){
		return incre_to_full_start_date;
	}
	/** 设置：表转为全量开始时间 */
	public void setIncre_to_full_start_date(String incre_to_full_start_date){
		this.incre_to_full_start_date=incre_to_full_start_date;
	}
	/** 取得：表转为全量结束时间 */
	public String getIncre_to_full_end_date(){
		return incre_to_full_end_date;
	}
	/** 设置：表转为全量结束时间 */
	public void setIncre_to_full_end_date(String incre_to_full_end_date){
		this.incre_to_full_end_date=incre_to_full_end_date;
	}
}
