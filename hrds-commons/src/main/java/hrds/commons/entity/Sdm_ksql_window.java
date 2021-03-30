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
 * 窗口信息登记表
 */
@Table(tableName = "sdm_ksql_window")
public class Sdm_ksql_window extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_ksql_window";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 窗口信息登记表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_win_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sdm_ksql_id",value="映射表主键:",dataType = Long.class,required = true)
	private Long sdm_ksql_id;
	@DocBean(name ="sdm_win_id",value="窗口信息登记id:",dataType = Long.class,required = true)
	private Long sdm_win_id;
	@DocBean(name ="window_type",value="窗口类别:",dataType = String.class,required = false)
	private String window_type;
	@DocBean(name ="window_size",value="窗口大小:",dataType = Long.class,required = true)
	private Long window_size;
	@DocBean(name ="advance_interval",value="窗口滑动间隔:",dataType = Long.class,required = true)
	private Long advance_interval;
	@DocBean(name ="window_remark",value="备注:",dataType = String.class,required = false)
	private String window_remark;

	/** 取得：映射表主键 */
	public Long getSdm_ksql_id(){
		return sdm_ksql_id;
	}
	/** 设置：映射表主键 */
	public void setSdm_ksql_id(Long sdm_ksql_id){
		this.sdm_ksql_id=sdm_ksql_id;
	}
	/** 设置：映射表主键 */
	public void setSdm_ksql_id(String sdm_ksql_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_ksql_id)){
			this.sdm_ksql_id=new Long(sdm_ksql_id);
		}
	}
	/** 取得：窗口信息登记id */
	public Long getSdm_win_id(){
		return sdm_win_id;
	}
	/** 设置：窗口信息登记id */
	public void setSdm_win_id(Long sdm_win_id){
		this.sdm_win_id=sdm_win_id;
	}
	/** 设置：窗口信息登记id */
	public void setSdm_win_id(String sdm_win_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_win_id)){
			this.sdm_win_id=new Long(sdm_win_id);
		}
	}
	/** 取得：窗口类别 */
	public String getWindow_type(){
		return window_type;
	}
	/** 设置：窗口类别 */
	public void setWindow_type(String window_type){
		this.window_type=window_type;
	}
	/** 取得：窗口大小 */
	public Long getWindow_size(){
		return window_size;
	}
	/** 设置：窗口大小 */
	public void setWindow_size(Long window_size){
		this.window_size=window_size;
	}
	/** 设置：窗口大小 */
	public void setWindow_size(String window_size){
		if(!fd.ng.core.utils.StringUtil.isEmpty(window_size)){
			this.window_size=new Long(window_size);
		}
	}
	/** 取得：窗口滑动间隔 */
	public Long getAdvance_interval(){
		return advance_interval;
	}
	/** 设置：窗口滑动间隔 */
	public void setAdvance_interval(Long advance_interval){
		this.advance_interval=advance_interval;
	}
	/** 设置：窗口滑动间隔 */
	public void setAdvance_interval(String advance_interval){
		if(!fd.ng.core.utils.StringUtil.isEmpty(advance_interval)){
			this.advance_interval=new Long(advance_interval);
		}
	}
	/** 取得：备注 */
	public String getWindow_remark(){
		return window_remark;
	}
	/** 设置：备注 */
	public void setWindow_remark(String window_remark){
		this.window_remark=window_remark;
	}
}
