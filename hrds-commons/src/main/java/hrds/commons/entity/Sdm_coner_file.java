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
 * 数据消费至二进制文件
 */
@Table(tableName = "sdm_coner_file")
public class Sdm_coner_file extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_coner_file";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据消费至二进制文件 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("file_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="file_name",value="文件名:",dataType = String.class,required = true)
	private String file_name;
	@DocBean(name ="file_path",value="文件路径:",dataType = String.class,required = true)
	private String file_path;
	@DocBean(name ="file_id",value="二进制文件ID:",dataType = Long.class,required = true)
	private Long file_id;
	@DocBean(name ="time_interval",value="获取不到数据重发时间:",dataType = String.class,required = true)
	private String time_interval;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="sdm_des_id",value="配置id:",dataType = Long.class,required = true)
	private Long sdm_des_id;

	/** 取得：文件名 */
	public String getFile_name(){
		return file_name;
	}
	/** 设置：文件名 */
	public void setFile_name(String file_name){
		this.file_name=file_name;
	}
	/** 取得：文件路径 */
	public String getFile_path(){
		return file_path;
	}
	/** 设置：文件路径 */
	public void setFile_path(String file_path){
		this.file_path=file_path;
	}
	/** 取得：二进制文件ID */
	public Long getFile_id(){
		return file_id;
	}
	/** 设置：二进制文件ID */
	public void setFile_id(Long file_id){
		this.file_id=file_id;
	}
	/** 设置：二进制文件ID */
	public void setFile_id(String file_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(file_id)){
			this.file_id=new Long(file_id);
		}
	}
	/** 取得：获取不到数据重发时间 */
	public String getTime_interval(){
		return time_interval;
	}
	/** 设置：获取不到数据重发时间 */
	public void setTime_interval(String time_interval){
		this.time_interval=time_interval;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：配置id */
	public Long getSdm_des_id(){
		return sdm_des_id;
	}
	/** 设置：配置id */
	public void setSdm_des_id(Long sdm_des_id){
		this.sdm_des_id=sdm_des_id;
	}
	/** 设置：配置id */
	public void setSdm_des_id(String sdm_des_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_des_id)){
			this.sdm_des_id=new Long(sdm_des_id);
		}
	}
}
