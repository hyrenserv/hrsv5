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
 * 数据对标字段特征表
 */
@Table(tableName = "dbm_feature_tab")
public class Dbm_feature_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_feature_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标字段特征表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_schema");
		__tmpPKS.add("table_code");
		__tmpPKS.add("col_code");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编号:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_schema",value="表所属schema:",dataType = String.class,required = true)
	private String table_schema;
	@DocBean(name ="table_code",value="表编号:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="col_code",value="字段编号:",dataType = String.class,required = true)
	private String col_code;
	@DocBean(name ="col_records",value="字段所在表中的记录数:",dataType = Integer.class,required = true)
	private Integer col_records;
	@DocBean(name ="col_distinct",value="字段取值去重数目:",dataType = Integer.class,required = false)
	private Integer col_distinct;
	@DocBean(name ="max_len",value="字段值最大长度:",dataType = Integer.class,required = false)
	private Integer max_len;
	@DocBean(name ="min_len",value="字段值最小长度:",dataType = Integer.class,required = false)
	private Integer min_len;
	@DocBean(name ="avg_len",value="字段值平均长度:",dataType = BigDecimal.class,required = false)
	private BigDecimal avg_len;
	@DocBean(name ="skew_len",value="字段值平均长度偏度:",dataType = BigDecimal.class,required = false)
	private BigDecimal skew_len;
	@DocBean(name ="kurt_len",value="字段值平均长度峰度:",dataType = BigDecimal.class,required = false)
	private BigDecimal kurt_len;
	@DocBean(name ="median_len",value="字段值平均长度中位数:",dataType = BigDecimal.class,required = false)
	private BigDecimal median_len;
	@DocBean(name ="var_len",value="字段值平均长度方差:",dataType = BigDecimal.class,required = false)
	private BigDecimal var_len;
	@DocBean(name ="字段值是否包含中文",value="字段值是否包含中文(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = false)
	private String 字段值是否包含中文;
	@DocBean(name ="tech_cate",value="字段技术分类:",dataType = String.class,required = false)
	private String tech_cate;

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
	public String getCol_code(){
		return col_code;
	}
	/** 设置：字段编号 */
	public void setCol_code(String col_code){
		this.col_code=col_code;
	}
	/** 取得：字段所在表中的记录数 */
	public Integer getCol_records(){
		return col_records;
	}
	/** 设置：字段所在表中的记录数 */
	public void setCol_records(Integer col_records){
		this.col_records=col_records;
	}
	/** 设置：字段所在表中的记录数 */
	public void setCol_records(String col_records){
		if(!fd.ng.core.utils.StringUtil.isEmpty(col_records)){
			this.col_records=new Integer(col_records);
		}
	}
	/** 取得：字段取值去重数目 */
	public Integer getCol_distinct(){
		return col_distinct;
	}
	/** 设置：字段取值去重数目 */
	public void setCol_distinct(Integer col_distinct){
		this.col_distinct=col_distinct;
	}
	/** 设置：字段取值去重数目 */
	public void setCol_distinct(String col_distinct){
		if(!fd.ng.core.utils.StringUtil.isEmpty(col_distinct)){
			this.col_distinct=new Integer(col_distinct);
		}
	}
	/** 取得：字段值最大长度 */
	public Integer getMax_len(){
		return max_len;
	}
	/** 设置：字段值最大长度 */
	public void setMax_len(Integer max_len){
		this.max_len=max_len;
	}
	/** 设置：字段值最大长度 */
	public void setMax_len(String max_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(max_len)){
			this.max_len=new Integer(max_len);
		}
	}
	/** 取得：字段值最小长度 */
	public Integer getMin_len(){
		return min_len;
	}
	/** 设置：字段值最小长度 */
	public void setMin_len(Integer min_len){
		this.min_len=min_len;
	}
	/** 设置：字段值最小长度 */
	public void setMin_len(String min_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(min_len)){
			this.min_len=new Integer(min_len);
		}
	}
	/** 取得：字段值平均长度 */
	public BigDecimal getAvg_len(){
		return avg_len;
	}
	/** 设置：字段值平均长度 */
	public void setAvg_len(BigDecimal avg_len){
		this.avg_len=avg_len;
	}
	/** 设置：字段值平均长度 */
	public void setAvg_len(String avg_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(avg_len)){
			this.avg_len=new BigDecimal(avg_len);
		}
	}
	/** 取得：字段值平均长度偏度 */
	public BigDecimal getSkew_len(){
		return skew_len;
	}
	/** 设置：字段值平均长度偏度 */
	public void setSkew_len(BigDecimal skew_len){
		this.skew_len=skew_len;
	}
	/** 设置：字段值平均长度偏度 */
	public void setSkew_len(String skew_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(skew_len)){
			this.skew_len=new BigDecimal(skew_len);
		}
	}
	/** 取得：字段值平均长度峰度 */
	public BigDecimal getKurt_len(){
		return kurt_len;
	}
	/** 设置：字段值平均长度峰度 */
	public void setKurt_len(BigDecimal kurt_len){
		this.kurt_len=kurt_len;
	}
	/** 设置：字段值平均长度峰度 */
	public void setKurt_len(String kurt_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(kurt_len)){
			this.kurt_len=new BigDecimal(kurt_len);
		}
	}
	/** 取得：字段值平均长度中位数 */
	public BigDecimal getMedian_len(){
		return median_len;
	}
	/** 设置：字段值平均长度中位数 */
	public void setMedian_len(BigDecimal median_len){
		this.median_len=median_len;
	}
	/** 设置：字段值平均长度中位数 */
	public void setMedian_len(String median_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(median_len)){
			this.median_len=new BigDecimal(median_len);
		}
	}
	/** 取得：字段值平均长度方差 */
	public BigDecimal getVar_len(){
		return var_len;
	}
	/** 设置：字段值平均长度方差 */
	public void setVar_len(BigDecimal var_len){
		this.var_len=var_len;
	}
	/** 设置：字段值平均长度方差 */
	public void setVar_len(String var_len){
		if(!fd.ng.core.utils.StringUtil.isEmpty(var_len)){
			this.var_len=new BigDecimal(var_len);
		}
	}
	/** 取得：字段值是否包含中文 */
	public String get字段值是否包含中文(){
		return 字段值是否包含中文;
	}
	/** 设置：字段值是否包含中文 */
	public void set字段值是否包含中文(String 字段值是否包含中文){
		this.字段值是否包含中文=字段值是否包含中文;
	}
	/** 取得：字段技术分类 */
	public String getTech_cate(){
		return tech_cate;
	}
	/** 设置：字段技术分类 */
	public void setTech_cate(String tech_cate){
		this.tech_cate=tech_cate;
	}
}
