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
 * 数据对标维度划分结果表
 */
@Table(tableName = "dbm_field_cate_result")
public class Dbm_field_cate_result extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_field_cate_result";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标维度划分结果表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_code");
		__tmpPKS.add("字段编码");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编码:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_code",value="表编码:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="字段编码",value="字段编码:",dataType = String.class,required = true)
	private String 字段编码;
	@DocBean(name ="所属维度节点编号",value="所属维度节点编号:",dataType = String.class,required = false)
	private String 所属维度节点编号;
	@DocBean(name ="origin_dim",value="原始所属维度节点编号:",dataType = String.class,required = false)
	private String origin_dim;
	@DocBean(name ="relation_type",value="关系类型:",dataType = String.class,required = false)
	private String relation_type;
	@DocBean(name ="category_same",value="同维度类别编号:",dataType = Integer.class,required = false)
	private Integer category_same;
	@DocBean(name ="区别标识",value="区别标识:",dataType = Integer.class,required = false)
	private Integer 区别标识;
	@DocBean(name ="dim_order",value="同维度下同类别字段排序编号:",dataType = Integer.class,required = false)
	private Integer dim_order;
	@DocBean(name ="del_flag",value="子集关系删除标识(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = false)
	private String del_flag;

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
	public String get字段编码(){
		return 字段编码;
	}
	/** 设置：字段编码 */
	public void set字段编码(String 字段编码){
		this.字段编码=字段编码;
	}
	/** 取得：所属维度节点编号 */
	public String get所属维度节点编号(){
		return 所属维度节点编号;
	}
	/** 设置：所属维度节点编号 */
	public void set所属维度节点编号(String 所属维度节点编号){
		this.所属维度节点编号=所属维度节点编号;
	}
	/** 取得：原始所属维度节点编号 */
	public String getOrigin_dim(){
		return origin_dim;
	}
	/** 设置：原始所属维度节点编号 */
	public void setOrigin_dim(String origin_dim){
		this.origin_dim=origin_dim;
	}
	/** 取得：关系类型 */
	public String getRelation_type(){
		return relation_type;
	}
	/** 设置：关系类型 */
	public void setRelation_type(String relation_type){
		this.relation_type=relation_type;
	}
	/** 取得：同维度类别编号 */
	public Integer getCategory_same(){
		return category_same;
	}
	/** 设置：同维度类别编号 */
	public void setCategory_same(Integer category_same){
		this.category_same=category_same;
	}
	/** 设置：同维度类别编号 */
	public void setCategory_same(String category_same){
		if(!fd.ng.core.utils.StringUtil.isEmpty(category_same)){
			this.category_same=new Integer(category_same);
		}
	}
	/** 取得：区别标识 */
	public Integer get区别标识(){
		return 区别标识;
	}
	/** 设置：区别标识 */
	public void set区别标识(Integer 区别标识){
		this.区别标识=区别标识;
	}
	/** 设置：区别标识 */
	public void set区别标识(String 区别标识){
		if(!fd.ng.core.utils.StringUtil.isEmpty(区别标识)){
			this.区别标识=new Integer(区别标识);
		}
	}
	/** 取得：同维度下同类别字段排序编号 */
	public Integer getDim_order(){
		return dim_order;
	}
	/** 设置：同维度下同类别字段排序编号 */
	public void setDim_order(Integer dim_order){
		this.dim_order=dim_order;
	}
	/** 设置：同维度下同类别字段排序编号 */
	public void setDim_order(String dim_order){
		if(!fd.ng.core.utils.StringUtil.isEmpty(dim_order)){
			this.dim_order=new Integer(dim_order);
		}
	}
	/** 取得：子集关系删除标识 */
	public String getDel_flag(){
		return del_flag;
	}
	/** 设置：子集关系删除标识 */
	public void setDel_flag(String del_flag){
		this.del_flag=del_flag;
	}
}
