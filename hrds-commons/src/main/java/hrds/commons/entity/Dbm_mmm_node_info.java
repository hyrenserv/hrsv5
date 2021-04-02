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
 * 数据对标节点信息表
 */
@Table(tableName = "dbm_mmm_node_info")
public class Dbm_mmm_node_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dbm_mmm_node_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据对标节点信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("node_code");
		__tmpPKS.add("node_name");
		__tmpPKS.add("node_type");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="node_code",value="节点编码:",dataType = String.class,required = true)
	private String node_code;
	@DocBean(name ="node_name",value="节点名称:",dataType = String.class,required = true)
	private String node_name;
	@DocBean(name ="node_type",value="节点类型:",dataType = String.class,required = true)
	private String node_type;
	@DocBean(name ="parent_node_code",value="父节点编码:",dataType = String.class,required = false)
	private String parent_node_code;
	@DocBean(name ="sys_class_code",value="系统分类编码:",dataType = String.class,required = false)
	private String sys_class_code;
	@DocBean(name ="data_src",value="数据来源:",dataType = String.class,required = false)
	private String data_src;

	/** 取得：节点编码 */
	public String getNode_code(){
		return node_code;
	}
	/** 设置：节点编码 */
	public void setNode_code(String node_code){
		this.node_code=node_code;
	}
	/** 取得：节点名称 */
	public String getNode_name(){
		return node_name;
	}
	/** 设置：节点名称 */
	public void setNode_name(String node_name){
		this.node_name=node_name;
	}
	/** 取得：节点类型 */
	public String getNode_type(){
		return node_type;
	}
	/** 设置：节点类型 */
	public void setNode_type(String node_type){
		this.node_type=node_type;
	}
	/** 取得：父节点编码 */
	public String getParent_node_code(){
		return parent_node_code;
	}
	/** 设置：父节点编码 */
	public void setParent_node_code(String parent_node_code){
		this.parent_node_code=parent_node_code;
	}
	/** 取得：系统分类编码 */
	public String getSys_class_code(){
		return sys_class_code;
	}
	/** 设置：系统分类编码 */
	public void setSys_class_code(String sys_class_code){
		this.sys_class_code=sys_class_code;
	}
	/** 取得：数据来源 */
	public String getData_src(){
		return data_src;
	}
	/** 设置：数据来源 */
	public void setData_src(String data_src){
		this.data_src=data_src;
	}
}
