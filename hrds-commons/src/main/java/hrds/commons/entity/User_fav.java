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
 * 我的收藏
 */
@Table(tableName = "user_fav")
public class User_fav extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "user_fav";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 我的收藏 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("fav_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="user_id",value="用户ID:",dataType = Long.class,required = true)
	private Long user_id;
	@DocBean(name ="fav_id",value="收藏ID:",dataType = Long.class,required = true)
	private Long fav_id;
	@DocBean(name ="fav_flag",value="是否有效(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String fav_flag;
	@DocBean(name ="original_name",value="原始文件名称:",dataType = String.class,required = true)
	private String original_name;
	@DocBean(name ="file_id",value="文件编号:",dataType = String.class,required = true)
	private String file_id;

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
	/** 取得：收藏ID */
	public Long getFav_id(){
		return fav_id;
	}
	/** 设置：收藏ID */
	public void setFav_id(Long fav_id){
		this.fav_id=fav_id;
	}
	/** 设置：收藏ID */
	public void setFav_id(String fav_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(fav_id)){
			this.fav_id=new Long(fav_id);
		}
	}
	/** 取得：是否有效 */
	public String getFav_flag(){
		return fav_flag;
	}
	/** 设置：是否有效 */
	public void setFav_flag(String fav_flag){
		this.fav_flag=fav_flag;
	}
	/** 取得：原始文件名称 */
	public String getOriginal_name(){
		return original_name;
	}
	/** 设置：原始文件名称 */
	public void setOriginal_name(String original_name){
		this.original_name=original_name;
	}
	/** 取得：文件编号 */
	public String getFile_id(){
		return file_id;
	}
	/** 设置：文件编号 */
	public void setFile_id(String file_id){
		this.file_id=file_id;
	}
}
