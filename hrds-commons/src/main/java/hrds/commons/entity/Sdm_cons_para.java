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
 * 流数据管理消费端参数表
 */
@Table(tableName = "sdm_cons_para")
public class Sdm_cons_para extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_cons_para";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理消费端参数表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_conf_para_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sdm_conf_para_id",value="sdm_conf_para_id:",dataType = Long.class,required = true)
	private Long sdm_conf_para_id;
	@DocBean(name ="sdm_conf_para_na",value="参数名称:",dataType = String.class,required = false)
	private String sdm_conf_para_na;
	@DocBean(name ="sdm_cons_para_val",value="参数值:",dataType = String.class,required = false)
	private String sdm_cons_para_val;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="sdm_consum_id",value="消费端配置id:",dataType = Long.class,required = true)
	private Long sdm_consum_id;

	/** 取得：sdm_conf_para_id */
	public Long getSdm_conf_para_id(){
		return sdm_conf_para_id;
	}
	/** 设置：sdm_conf_para_id */
	public void setSdm_conf_para_id(Long sdm_conf_para_id){
		this.sdm_conf_para_id=sdm_conf_para_id;
	}
	/** 设置：sdm_conf_para_id */
	public void setSdm_conf_para_id(String sdm_conf_para_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_conf_para_id)){
			this.sdm_conf_para_id=new Long(sdm_conf_para_id);
		}
	}
	/** 取得：参数名称 */
	public String getSdm_conf_para_na(){
		return sdm_conf_para_na;
	}
	/** 设置：参数名称 */
	public void setSdm_conf_para_na(String sdm_conf_para_na){
		this.sdm_conf_para_na=sdm_conf_para_na;
	}
	/** 取得：参数值 */
	public String getSdm_cons_para_val(){
		return sdm_cons_para_val;
	}
	/** 设置：参数值 */
	public void setSdm_cons_para_val(String sdm_cons_para_val){
		this.sdm_cons_para_val=sdm_cons_para_val;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：消费端配置id */
	public Long getSdm_consum_id(){
		return sdm_consum_id;
	}
	/** 设置：消费端配置id */
	public void setSdm_consum_id(Long sdm_consum_id){
		this.sdm_consum_id=sdm_consum_id;
	}
	/** 设置：消费端配置id */
	public void setSdm_consum_id(String sdm_consum_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_consum_id)){
			this.sdm_consum_id=new Long(sdm_consum_id);
		}
	}
}
