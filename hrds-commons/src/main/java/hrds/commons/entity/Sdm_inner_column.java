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
 * 流数据海云内部字段信息登记表
 */
@Table(tableName = "sdm_inner_column")
public class Sdm_inner_column extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_inner_column";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据海云内部字段信息登记表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("field_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="field_id",value="字段Id:",dataType = Long.class,required = true)
	private Long field_id;
	@DocBean(name ="field_cn_name",value="字段中文名称:",dataType = String.class,required = false)
	private String field_cn_name;
	@DocBean(name ="field_en_name",value="字段英文名称:",dataType = String.class,required = true)
	private String field_en_name;
	@DocBean(name ="field_type",value="字段类型:",dataType = String.class,required = true)
	private String field_type;
	@DocBean(name ="field_desc",value="字段描述:",dataType = String.class,required = false)
	private String field_desc;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="table_id",value="表id:",dataType = Long.class,required = true)
	private Long table_id;
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;

	/** 取得：字段Id */
	public Long getField_id(){
		return field_id;
	}
	/** 设置：字段Id */
	public void setField_id(Long field_id){
		this.field_id=field_id;
	}
	/** 设置：字段Id */
	public void setField_id(String field_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(field_id)){
			this.field_id=new Long(field_id);
		}
	}
	/** 取得：字段中文名称 */
	public String getField_cn_name(){
		return field_cn_name;
	}
	/** 设置：字段中文名称 */
	public void setField_cn_name(String field_cn_name){
		this.field_cn_name=field_cn_name;
	}
	/** 取得：字段英文名称 */
	public String getField_en_name(){
		return field_en_name;
	}
	/** 设置：字段英文名称 */
	public void setField_en_name(String field_en_name){
		this.field_en_name=field_en_name;
	}
	/** 取得：字段类型 */
	public String getField_type(){
		return field_type;
	}
	/** 设置：字段类型 */
	public void setField_type(String field_type){
		this.field_type=field_type;
	}
	/** 取得：字段描述 */
	public String getField_desc(){
		return field_desc;
	}
	/** 设置：字段描述 */
	public void setField_desc(String field_desc){
		this.field_desc=field_desc;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：表id */
	public Long getTable_id(){
		return table_id;
	}
	/** 设置：表id */
	public void setTable_id(Long table_id){
		this.table_id=table_id;
	}
	/** 设置：表id */
	public void setTable_id(String table_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(table_id)){
			this.table_id=new Long(table_id);
		}
	}
	/** 取得：用户ID */
	public Long getUser_id(){
		return user_id;
	}
	/** 设置：用户ID */
	public void setUser_id(Long user_id){
		this.user_id=user_id;
	}
	/** 设置：用户ID */
	public void setUser_id(String user_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(user_id)){
			this.user_id=new Long(user_id);
		}
	}
}
