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
 * StreamProRest配置信息表
 */
@Table(tableName = "streampro_setting")
public class Streampro_setting extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "streampro_setting";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** StreamProRest配置信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("rs_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="rs_id",value="Rest Id:",dataType = Long.class,required = true)
	private Long rs_id;
	@DocBean(name ="rs_url",value="RestUrl地址:",dataType = String.class,required = true)
	private String rs_url;
	@DocBean(name ="rs_processing",value="返回值处理(SdmSpRsType):0-忽略<HuLue> 1-数据库<ShuJuKu> 2-kafka<KafKa> ",dataType = String.class,required = false)
	private String rs_processing;
	@DocBean(name ="rs_type",value="请求类型:",dataType = String.class,required = false)
	private String rs_type;
	@DocBean(name ="rs_para",value="Rest请求参数:",dataType = String.class,required = true)
	private String rs_para;
	@DocBean(name ="sdm_info_id",value="作业输入信息表id:",dataType = Long.class,required = true)
	private Long sdm_info_id;

	/** 取得：Rest Id */
	public Long getRs_id(){
		return rs_id;
	}
	/** 设置：Rest Id */
	public void setRs_id(Long rs_id){
		this.rs_id=rs_id;
	}
	/** 设置：Rest Id */
	public void setRs_id(String rs_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(rs_id)){
			this.rs_id=new Long(rs_id);
		}
	}
	/** 取得：RestUrl地址 */
	public String getRs_url(){
		return rs_url;
	}
	/** 设置：RestUrl地址 */
	public void setRs_url(String rs_url){
		this.rs_url=rs_url;
	}
	/** 取得：返回值处理 */
	public String getRs_processing(){
		return rs_processing;
	}
	/** 设置：返回值处理 */
	public void setRs_processing(String rs_processing){
		this.rs_processing=rs_processing;
	}
	/** 取得：请求类型 */
	public String getRs_type(){
		return rs_type;
	}
	/** 设置：请求类型 */
	public void setRs_type(String rs_type){
		this.rs_type=rs_type;
	}
	/** 取得：Rest请求参数 */
	public String getRs_para(){
		return rs_para;
	}
	/** 设置：Rest请求参数 */
	public void setRs_para(String rs_para){
		this.rs_para=rs_para;
	}
	/** 取得：作业输入信息表id */
	public Long getSdm_info_id(){
		return sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(Long sdm_info_id){
		this.sdm_info_id=sdm_info_id;
	}
	/** 设置：作业输入信息表id */
	public void setSdm_info_id(String sdm_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_info_id)){
			this.sdm_info_id=new Long(sdm_info_id);
		}
	}
}
