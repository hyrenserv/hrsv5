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
 * REST数据库数据信息表
 */
@Table(tableName = "sdm_rest_database")
public class Sdm_rest_database extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_rest_database";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** REST数据库数据信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("ssd_info_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ssd_info_id",value="数据库信息表id:",dataType = Long.class,required = true)
	private Long ssd_info_id;
	@DocBean(name ="ssd_database_type",value="数据库类型(DatabaseType):01-MYSQL<MYSQL> 02-Oracle9i及一下<Oracle9i> 03-Oracle10g及以上<Oracle10g> 04-SQLSERVER2000<SqlServer2000> 05-SQLSERVER2005<SqlServer2005> 06-DB2<DB2> 07-SybaseASE12.5及以上<SybaseASE125> 08-Informatic<Informatic> 09-H2<H2> 10-ApacheDerby<ApacheDerby> 11-Postgresql<Postgresql> 12-GBase<GBase> 13-TeraData<TeraData> 14-Hive<Hive> 15-Odps<Odps> 16-KingBase<KingBase> ",dataType = String.class,required = true)
	private String ssd_database_type;
	@DocBean(name ="ssd_database_drive",value="数据库驱动:",dataType = String.class,required = true)
	private String ssd_database_drive;
	@DocBean(name ="ssd_database_name",value="数据库名称:",dataType = String.class,required = false)
	private String ssd_database_name;
	@DocBean(name ="ssd_ip",value="数据库ip:",dataType = String.class,required = true)
	private String ssd_ip;
	@DocBean(name ="ssd_port",value="端口:",dataType = String.class,required = true)
	private String ssd_port;
	@DocBean(name ="ssd_user_name",value="数据库用户名:",dataType = String.class,required = false)
	private String ssd_user_name;
	@DocBean(name ="ssd_user_password",value="用户密码:",dataType = String.class,required = true)
	private String ssd_user_password;
	@DocBean(name ="ssd_table_name",value="表名称:",dataType = String.class,required = false)
	private String ssd_table_name;
	@DocBean(name ="ssd_jdbc_url",value="数据库jdbc连接的url:",dataType = String.class,required = true)
	private String ssd_jdbc_url;
	@DocBean(name ="rs_id",value="Rest Id:",dataType = Long.class,required = false)
	private Long rs_id;

	/** 取得：数据库信息表id */
	public Long getSsd_info_id(){
		return ssd_info_id;
	}
	/** 设置：数据库信息表id */
	public void setSsd_info_id(Long ssd_info_id){
		this.ssd_info_id=ssd_info_id;
	}
	/** 设置：数据库信息表id */
	public void setSsd_info_id(String ssd_info_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(ssd_info_id)){
			this.ssd_info_id=new Long(ssd_info_id);
		}
	}
	/** 取得：数据库类型 */
	public String getSsd_database_type(){
		return ssd_database_type;
	}
	/** 设置：数据库类型 */
	public void setSsd_database_type(String ssd_database_type){
		this.ssd_database_type=ssd_database_type;
	}
	/** 取得：数据库驱动 */
	public String getSsd_database_drive(){
		return ssd_database_drive;
	}
	/** 设置：数据库驱动 */
	public void setSsd_database_drive(String ssd_database_drive){
		this.ssd_database_drive=ssd_database_drive;
	}
	/** 取得：数据库名称 */
	public String getSsd_database_name(){
		return ssd_database_name;
	}
	/** 设置：数据库名称 */
	public void setSsd_database_name(String ssd_database_name){
		this.ssd_database_name=ssd_database_name;
	}
	/** 取得：数据库ip */
	public String getSsd_ip(){
		return ssd_ip;
	}
	/** 设置：数据库ip */
	public void setSsd_ip(String ssd_ip){
		this.ssd_ip=ssd_ip;
	}
	/** 取得：端口 */
	public String getSsd_port(){
		return ssd_port;
	}
	/** 设置：端口 */
	public void setSsd_port(String ssd_port){
		this.ssd_port=ssd_port;
	}
	/** 取得：数据库用户名 */
	public String getSsd_user_name(){
		return ssd_user_name;
	}
	/** 设置：数据库用户名 */
	public void setSsd_user_name(String ssd_user_name){
		this.ssd_user_name=ssd_user_name;
	}
	/** 取得：用户密码 */
	public String getSsd_user_password(){
		return ssd_user_password;
	}
	/** 设置：用户密码 */
	public void setSsd_user_password(String ssd_user_password){
		this.ssd_user_password=ssd_user_password;
	}
	/** 取得：表名称 */
	public String getSsd_table_name(){
		return ssd_table_name;
	}
	/** 设置：表名称 */
	public void setSsd_table_name(String ssd_table_name){
		this.ssd_table_name=ssd_table_name;
	}
	/** 取得：数据库jdbc连接的url */
	public String getSsd_jdbc_url(){
		return ssd_jdbc_url;
	}
	/** 设置：数据库jdbc连接的url */
	public void setSsd_jdbc_url(String ssd_jdbc_url){
		this.ssd_jdbc_url=ssd_jdbc_url;
	}
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
}
