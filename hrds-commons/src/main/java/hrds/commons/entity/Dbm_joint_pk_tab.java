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
 * 数据对标联合主键关系表
 */
@Table(tableName = "dbm_joint_pk_tab")
public class Dbm_joint_pk_tab extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_joint_pk_tab";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标联合主键关系表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sys_class_code");
		__tmpPKS.add("table_code");
		__tmpPKS.add("col_code");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sys_class_code",value="系统分类编码:",dataType = String.class,required = true)
	private String sys_class_code;
	@DocBean(name ="table_code",value="表编码:",dataType = String.class,required = true)
	private String table_code;
	@DocBean(name ="group_code",value="联合主键分组编码:",dataType = String.class,required = true)
	private String group_code;
	@DocBean(name ="col_code",value="主键字段编码:",dataType = String.class,required = true)
	private String col_code;

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
	/** 取得：联合主键分组编码 */
	public String getGroup_code(){
		return group_code;
	}
	/** 设置：联合主键分组编码 */
	public void setGroup_code(String group_code){
		this.group_code=group_code;
	}
	/** 取得：主键字段编码 */
	public String getCol_code(){
		return col_code;
	}
	/** 设置：主键字段编码 */
	public void setCol_code(String col_code){
		this.col_code=col_code;
	}
}
