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
 * 流数据管理消息信息表
 */
@Table(tableName = "sdm_mess_info")
public class Sdm_mess_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_mess_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理消息信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("mess_info_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="mess_info_id",value="mess_info_id:",dataType = Long.class,required = true)
	private Long mess_info_id;
	@DocBean(name ="sdm_var_name_en",value="英文变量名:",dataType = String.class,required = true)
	private String sdm_var_name_en;
	@DocBean(name ="sdm_var_name_cn",value="中文变量名:",dataType = String.class,required = true)
	private String sdm_var_name_cn;
	@DocBean(name ="sdm_describe",value="含义:",dataType = String.class,required = false)
	private String sdm_describe;
	@DocBean(name ="sdm_var_type",value="变量类型(SdmVariableType):1-整数<ZhengShu> 2-字符串<ZiFuChuan> 3-浮点数<FuDianShu> 4-字节数组<ZiJieShuZu> ",dataType = String.class,required = true)
	private String sdm_var_type;
	@DocBean(name ="sdm_is_send",value="是否发送(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String sdm_is_send;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="num",value="变量序号:",dataType = String.class,required = true)
	private String num;
	@DocBean(name ="sdm_receive_id",value="流数据管理:",dataType = Long.class,required = true)
	private Long sdm_receive_id;

	/** 取得：mess_info_id */
	public Long getMess_info_id(){
		return mess_info_id;
	}
	/** 设置：mess_info_id */
	public void setMess_info_id(Long mess_info_id){
		this.mess_info_id=mess_info_id;
	}
	/** 设置：mess_info_id */
	public void setMess_info_id(String mess_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(mess_info_id)){
			this.mess_info_id=new Long(mess_info_id);
		}
	}
	/** 取得：英文变量名 */
	public String getSdm_var_name_en(){
		return sdm_var_name_en;
	}
	/** 设置：英文变量名 */
	public void setSdm_var_name_en(String sdm_var_name_en){
		this.sdm_var_name_en=sdm_var_name_en;
	}
	/** 取得：中文变量名 */
	public String getSdm_var_name_cn(){
		return sdm_var_name_cn;
	}
	/** 设置：中文变量名 */
	public void setSdm_var_name_cn(String sdm_var_name_cn){
		this.sdm_var_name_cn=sdm_var_name_cn;
	}
	/** 取得：含义 */
	public String getSdm_describe(){
		return sdm_describe;
	}
	/** 设置：含义 */
	public void setSdm_describe(String sdm_describe){
		this.sdm_describe=sdm_describe;
	}
	/** 取得：变量类型 */
	public String getSdm_var_type(){
		return sdm_var_type;
	}
	/** 设置：变量类型 */
	public void setSdm_var_type(String sdm_var_type){
		this.sdm_var_type=sdm_var_type;
	}
	/** 取得：是否发送 */
	public String getSdm_is_send(){
		return sdm_is_send;
	}
	/** 设置：是否发送 */
	public void setSdm_is_send(String sdm_is_send){
		this.sdm_is_send=sdm_is_send;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：变量序号 */
	public String getNum(){
		return num;
	}
	/** 设置：变量序号 */
	public void setNum(String num){
		this.num=num;
	}
	/** 取得：流数据管理 */
	public Long getSdm_receive_id(){
		return sdm_receive_id;
	}
	/** 设置：流数据管理 */
	public void setSdm_receive_id(Long sdm_receive_id){
		this.sdm_receive_id=sdm_receive_id;
	}
	/** 设置：流数据管理 */
	public void setSdm_receive_id(String sdm_receive_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_receive_id)){
			this.sdm_receive_id=new Long(sdm_receive_id);
		}
	}
}
