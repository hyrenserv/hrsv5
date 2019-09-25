package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import fd.ng.db.entity.TableEntity;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import fd.ng.core.docannotation.DocBean;
import hrds.commons.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * 列清洗参数信息
 */
@Table(tableName = "column_clean")
public class Column_clean extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "column_clean";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 列清洗参数信息 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("c_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="c_id",value="清洗参数编号",dataType = Long.class,required = true)
	private Long c_id;
	@DocBean(name ="clean_type",value="清洗方式",dataType = String.class,required = true)
	private String clean_type;
	@DocBean(name ="character_filling",value="补齐字符",dataType = String.class,required = false)
	private String character_filling;
	@DocBean(name ="filling_length",value="补齐长度",dataType = Long.class,required = false)
	private Long filling_length;
	@DocBean(name ="field",value="原字段",dataType = String.class,required = false)
	private String field;
	@DocBean(name ="replace_feild",value="替换字段",dataType = String.class,required = false)
	private String replace_feild;
	@DocBean(name ="filling_type",value="补齐方式",dataType = String.class,required = false)
	private String filling_type;
	@DocBean(name ="convert_format",value="转换格式",dataType = String.class,required = false)
	private String convert_format;
	@DocBean(name ="old_format",value="原始格式",dataType = String.class,required = false)
	private String old_format;
	@DocBean(name ="column_id",value="字段ID",dataType = Long.class,required = true)
	private Long column_id;
	@DocBean(name ="codename",value="码值名称",dataType = String.class,required = false)
	private String codename;
	@DocBean(name ="codesys",value="码值所属系统",dataType = String.class,required = false)
	private String codesys;

	/** 取得：清洗参数编号 */
	public Long getC_id(){
		return c_id;
	}
	/** 设置：清洗参数编号 */
	public void setC_id(Long c_id){
		this.c_id=c_id;
	}
	/** 设置：清洗参数编号 */
	public void setC_id(String c_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(c_id)){
			this.c_id=new Long(c_id);
		}
	}
	/** 取得：清洗方式 */
	public String getClean_type(){
		return clean_type;
	}
	/** 设置：清洗方式 */
	public void setClean_type(String clean_type){
		this.clean_type=clean_type;
	}
	/** 取得：补齐字符 */
	public String getCharacter_filling(){
		return character_filling;
	}
	/** 设置：补齐字符 */
	public void setCharacter_filling(String character_filling){
		this.character_filling=character_filling;
	}
	/** 取得：补齐长度 */
	public Long getFilling_length(){
		return filling_length;
	}
	/** 设置：补齐长度 */
	public void setFilling_length(Long filling_length){
		this.filling_length=filling_length;
	}
	/** 设置：补齐长度 */
	public void setFilling_length(String filling_length){
		if(!fd.ng.core.utils.StringUtil.isEmpty(filling_length)){
			this.filling_length=new Long(filling_length);
		}
	}
	/** 取得：原字段 */
	public String getField(){
		return field;
	}
	/** 设置：原字段 */
	public void setField(String field){
		this.field=field;
	}
	/** 取得：替换字段 */
	public String getReplace_feild(){
		return replace_feild;
	}
	/** 设置：替换字段 */
	public void setReplace_feild(String replace_feild){
		this.replace_feild=replace_feild;
	}
	/** 取得：补齐方式 */
	public String getFilling_type(){
		return filling_type;
	}
	/** 设置：补齐方式 */
	public void setFilling_type(String filling_type){
		this.filling_type=filling_type;
	}
	/** 取得：转换格式 */
	public String getConvert_format(){
		return convert_format;
	}
	/** 设置：转换格式 */
	public void setConvert_format(String convert_format){
		this.convert_format=convert_format;
	}
	/** 取得：原始格式 */
	public String getOld_format(){
		return old_format;
	}
	/** 设置：原始格式 */
	public void setOld_format(String old_format){
		this.old_format=old_format;
	}
	/** 取得：字段ID */
	public Long getColumn_id(){
		return column_id;
	}
	/** 设置：字段ID */
	public void setColumn_id(Long column_id){
		this.column_id=column_id;
	}
	/** 设置：字段ID */
	public void setColumn_id(String column_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(column_id)){
			this.column_id=new Long(column_id);
		}
	}
	/** 取得：码值名称 */
	public String getCodename(){
		return codename;
	}
	/** 设置：码值名称 */
	public void setCodename(String codename){
		this.codename=codename;
	}
	/** 取得：码值所属系统 */
	public String getCodesys(){
		return codesys;
	}
	/** 设置：码值所属系统 */
	public void setCodesys(String codesys){
		this.codesys=codesys;
	}
}
