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
 * 数据对标类别划分详细记录表
 */
@Table(tableName = "dbm_field_same_detail")
public class Dbm_field_same_detail extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_field_same_detail";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标类别划分详细记录表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("left_sys_class_code");
		__tmpPKS.add("left_table_code");
		__tmpPKS.add("left_col_code");
		__tmpPKS.add("fk_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="left_sys_class_code",value="相等关系左部字段所属系统分类编码:",dataType = String.class,required = true)
	private String left_sys_class_code;
	@DocBean(name ="left_table_code",value="相等关系左部字段所属表编码:",dataType = String.class,required = true)
	private String left_table_code;
	@DocBean(name ="left_col_code",value="相等关系左部字段编码:",dataType = String.class,required = true)
	private String left_col_code;
	@DocBean(name ="right_sys_class_code",value="相等关系右部字段所属系统分类编码:",dataType = String.class,required = true)
	private String right_sys_class_code;
	@DocBean(name ="right_table_code",value="相等关系右部字段所属表编码:",dataType = String.class,required = false)
	private String right_table_code;
	@DocBean(name ="right_col_code",value="相等关系右部字段编码:",dataType = String.class,required = false)
	private String right_col_code;
	@DocBean(name ="fk_id",value="关联外键编号:",dataType = String.class,required = true)
	private String fk_id;
	@DocBean(name ="fk_type",value="关联外键类型:",dataType = String.class,required = false)
	private String fk_type;
	@DocBean(name ="分析时间",value="分析时间:",dataType = String.class,required = false)
	private String 分析时间;

	/** 取得：相等关系左部字段所属系统分类编码 */
	public String getLeft_sys_class_code(){
		return left_sys_class_code;
	}
	/** 设置：相等关系左部字段所属系统分类编码 */
	public void setLeft_sys_class_code(String left_sys_class_code){
		this.left_sys_class_code=left_sys_class_code;
	}
	/** 取得：相等关系左部字段所属表编码 */
	public String getLeft_table_code(){
		return left_table_code;
	}
	/** 设置：相等关系左部字段所属表编码 */
	public void setLeft_table_code(String left_table_code){
		this.left_table_code=left_table_code;
	}
	/** 取得：相等关系左部字段编码 */
	public String getLeft_col_code(){
		return left_col_code;
	}
	/** 设置：相等关系左部字段编码 */
	public void setLeft_col_code(String left_col_code){
		this.left_col_code=left_col_code;
	}
	/** 取得：相等关系右部字段所属系统分类编码 */
	public String getRight_sys_class_code(){
		return right_sys_class_code;
	}
	/** 设置：相等关系右部字段所属系统分类编码 */
	public void setRight_sys_class_code(String right_sys_class_code){
		this.right_sys_class_code=right_sys_class_code;
	}
	/** 取得：相等关系右部字段所属表编码 */
	public String getRight_table_code(){
		return right_table_code;
	}
	/** 设置：相等关系右部字段所属表编码 */
	public void setRight_table_code(String right_table_code){
		this.right_table_code=right_table_code;
	}
	/** 取得：相等关系右部字段编码 */
	public String getRight_col_code(){
		return right_col_code;
	}
	/** 设置：相等关系右部字段编码 */
	public void setRight_col_code(String right_col_code){
		this.right_col_code=right_col_code;
	}
	/** 取得：关联外键编号 */
	public String getFk_id(){
		return fk_id;
	}
	/** 设置：关联外键编号 */
	public void setFk_id(String fk_id){
		this.fk_id=fk_id;
	}
	/** 取得：关联外键类型 */
	public String getFk_type(){
		return fk_type;
	}
	/** 设置：关联外键类型 */
	public void setFk_type(String fk_type){
		this.fk_type=fk_type;
	}
	/** 取得：分析时间 */
	public String get分析时间(){
		return 分析时间;
	}
	/** 设置：分析时间 */
	public void set分析时间(String 分析时间){
		this.分析时间=分析时间;
	}
}
