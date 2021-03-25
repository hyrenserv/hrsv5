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
 * 流数据海云内部消费信息登记表
 */
@Table(tableName = "sdm_inner_table")
public class Sdm_inner_table extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_inner_table";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据海云内部消费信息登记表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("table_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="table_id",value="表id:",dataType = Long.class,required = true)
	private Long table_id;
	@DocBean(name ="table_cn_name",value="表中文名称:",dataType = String.class,required = false)
	private String table_cn_name;
	@DocBean(name ="table_en_name",value="表英文名称:",dataType = String.class,required = true)
	private String table_en_name;
	@DocBean(name ="create_date",value="创建日期:",dataType = String.class,required = true)
	private String create_date;
	@DocBean(name ="create_time",value="创建时间:",dataType = String.class,required = true)
	private String create_time;
	@DocBean(name ="hyren_consumedes",value="海云内部消费目的地:",dataType = String.class,required = true)
	private String hyren_consumedes;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="execute_state",value="运行状态(JobExecuteState):100-等待<DengDai> 101-运行<YunXing> 102-暂停<ZanTing> 103-中止<ZhongZhi> 104-完成<WanCheng> 105-失败<ShiBai> ",dataType = String.class,required = false)
	private String execute_state;
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;

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
	/** 取得：表中文名称 */
	public String getTable_cn_name(){
		return table_cn_name;
	}
	/** 设置：表中文名称 */
	public void setTable_cn_name(String table_cn_name){
		this.table_cn_name=table_cn_name;
	}
	/** 取得：表英文名称 */
	public String getTable_en_name(){
		return table_en_name;
	}
	/** 设置：表英文名称 */
	public void setTable_en_name(String table_en_name){
		this.table_en_name=table_en_name;
	}
	/** 取得：创建日期 */
	public String getCreate_date(){
		return create_date;
	}
	/** 设置：创建日期 */
	public void setCreate_date(String create_date){
		this.create_date=create_date;
	}
	/** 取得：创建时间 */
	public String getCreate_time(){
		return create_time;
	}
	/** 设置：创建时间 */
	public void setCreate_time(String create_time){
		this.create_time=create_time;
	}
	/** 取得：海云内部消费目的地 */
	public String getHyren_consumedes(){
		return hyren_consumedes;
	}
	/** 设置：海云内部消费目的地 */
	public void setHyren_consumedes(String hyren_consumedes){
		this.hyren_consumedes=hyren_consumedes;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：运行状态 */
	public String getExecute_state(){
		return execute_state;
	}
	/** 设置：运行状态 */
	public void setExecute_state(String execute_state){
		this.execute_state=execute_state;
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
