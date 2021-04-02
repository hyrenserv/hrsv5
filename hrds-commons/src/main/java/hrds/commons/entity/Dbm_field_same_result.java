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
 * 数据对标类别划分分组结果表
 */
@Table(tableName = "dbm_field_same_result")
public class Dbm_field_same_result extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_field_same_result";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标类别划分分组结果表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_code");
		__tmpPKS.add("col_code");
		__tmpPKS.add("category_same");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编码:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_code",value="表编码:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="col_code",value="字段编码:",dataType = String.class,required = true)
	private String col_code;
	@DocBean(name ="category_same",value="类别编号:",dataType = Integer.class,required = true)
	private Integer category_same;
	@DocBean(name ="diff_flg",value="区别标识:",dataType = Integer.class,required = false)
	private Integer diff_flg;
	@DocBean(name ="dim_order",value="同类别字段排序编号:",dataType = Integer.class,required = false)
	private Integer dim_order;
	@DocBean(name ="ana_time",value="分析时间:",dataType = String.class,required = false)
	private String ana_time;

	/** 取得：系统分类编码 */
	public String getSys_class_code(){
		return sys_class_code;
	}
	/** 设置：系统分类编码 */
	public void setSys_class_code(String sys_class_code){
		this.sys_class_code=sys_class_code;
	}
	/** 取得：表编码 */
	public String getTable_code(){
		return table_code;
	}
	/** 设置：表编码 */
	public void setTable_code(String table_code){
		this.table_code=table_code;
	}
	/** 取得：字段编码 */
	public String getCol_code(){
		return col_code;
	}
	/** 设置：字段编码 */
	public void setCol_code(String col_code){
		this.col_code=col_code;
	}
	/** 取得：类别编号 */
	public Integer getCategory_same(){
		return category_same;
	}
	/** 设置：类别编号 */
	public void setCategory_same(Integer category_same){
		this.category_same=category_same;
	}
	/** 设置：类别编号 */
	public void setCategory_same(String category_same){
		if(!fd.ng.core.utils.StringUtil.isEmpty(category_same)){
			this.category_same=new Integer(category_same);
		}
	}
	/** 取得：区别标识 */
	public Integer getDiff_flg(){
		return diff_flg;
	}
	/** 设置：区别标识 */
	public void setDiff_flg(Integer diff_flg){
		this.diff_flg=diff_flg;
	}
	/** 设置：区别标识 */
	public void setDiff_flg(String diff_flg){
		if(!fd.ng.core.utils.StringUtil.isEmpty(diff_flg)){
			this.diff_flg=new Integer(diff_flg);
		}
	}
	/** 取得：同类别字段排序编号 */
	public Integer getDim_order(){
		return dim_order;
	}
	/** 设置：同类别字段排序编号 */
	public void setDim_order(Integer dim_order){
		this.dim_order=dim_order;
	}
	/** 设置：同类别字段排序编号 */
	public void setDim_order(String dim_order){
		if(!fd.ng.core.utils.StringUtil.isEmpty(dim_order)){
			this.dim_order=new Integer(dim_order);
		}
	}
	/** 取得：分析时间 */
	public String getAna_time(){
		return ana_time;
	}
	/** 设置：分析时间 */
	public void setAna_time(String ana_time){
		this.ana_time=ana_time;
	}
}
