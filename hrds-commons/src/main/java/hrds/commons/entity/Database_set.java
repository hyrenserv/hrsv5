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
 * 源系统数据库设置
 */
@Table(tableName = "database_set")
public class Database_set extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "database_set";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 源系统数据库设置 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("database_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="agent_id",value="Agent_id:",dataType = Long.class,required = false)
	private Long agent_id;
	@DocBean(name ="database_id",value="数据库设置id:",dataType = Long.class,required = true)
	private Long database_id;
	@DocBean(name ="task_name",value="数据库采集任务名称:",dataType = String.class,required = false)
	private String task_name;
	@DocBean(name ="database_name",value="数据库名称:",dataType = String.class,required = false)
	private String database_name;
	@DocBean(name ="database_pad",value="数据库密码:",dataType = String.class,required = false)
	private String database_pad;
	@DocBean(name ="database_drive",value="数据库驱动:",dataType = String.class,required = false)
	private String database_drive;
	@DocBean(name ="database_type",value="数据库类型(DatabaseType):01-MYSQL<MYSQL> 02-Oracle9i及一下<Oracle9i> 03-Oracle10g及以上<Oracle10g> 04-SQLSERVER2000<SqlServer2000> 05-SQLSERVER2005<SqlServer2005> 06-DB2<DB2> 07-SybaseASE12.5及以上<SybaseASE125> 08-Informatic<Informatic> 09-H2<H2> 10-ApacheDerby<ApacheDerby> 11-Postgresql<Postgresql> 12-GBase<GBase> 13-TeraData<TeraData> 14-Hive<Hive> 15-Odps<Odps> 16-KingBase<KingBase> ",dataType = String.class,required = false)
	private String database_type;
	@DocBean(name ="user_name",value="用户名称:",dataType = String.class,required = false)
	private String user_name;
	@DocBean(name ="database_ip",value="数据库服务器IP:",dataType = String.class,required = false)
	private String database_ip;
	@DocBean(name ="database_port",value="数据库端口:",dataType = String.class,required = false)
	private String database_port;
	@DocBean(name ="host_name",value="主机名:",dataType = String.class,required = false)
	private String host_name;
	@DocBean(name ="system_type",value="操作系统类型:",dataType = String.class,required = false)
	private String system_type;
	@DocBean(name ="is_sendok",value="是否设置完成并发送成功(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_sendok;
	@DocBean(name ="database_number",value="数据库设置编号:",dataType = String.class,required = false)
	private String database_number;
	@DocBean(name ="db_agent",value="是否DB文件数据采集(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String db_agent;
	@DocBean(name ="plane_url",value="DB文件数据字典位置:",dataType = String.class,required = false)
	private String plane_url;
	@DocBean(name ="database_separatorr",value="数据采用分隔符:",dataType = String.class,required = false)
	private String database_separatorr;
	@DocBean(name ="row_separator",value="数据行分隔符:",dataType = String.class,required = false)
	private String row_separator;
	@DocBean(name ="classify_id",value="分类id:",dataType = Long.class,required = true)
	private Long classify_id;
	@DocBean(name ="cp_or",value="清洗顺序:",dataType = String.class,required = false)
	private String cp_or;
	@DocBean(name ="jdbc_url",value="数据库连接地址:",dataType = String.class,required = false)
	private String jdbc_url;
	@DocBean(name ="collect_type",value="数据库采集方式(CollectType):1-贴元登记<TieYuanDengJi> 2-数据库抽数<ShuJuKuChouShu> 3-数据库采集<ShuJuKuCaiJi> ",dataType = String.class,required = true)
	private String collect_type;
	@DocBean(name ="dsl_id",value="存储层配置ID:",dataType = Long.class,required = false)
	private Long dsl_id;

	/** 取得：Agent_id */
	public Long getAgent_id(){
		return agent_id;
	}
	/** 设置：Agent_id */
	public void setAgent_id(Long agent_id){
		this.agent_id=agent_id;
	}
	/** 设置：Agent_id */
	public void setAgent_id(String agent_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(agent_id)){
			this.agent_id=new Long(agent_id);
		}
	}
	/** 取得：数据库设置id */
	public Long getDatabase_id(){
		return database_id;
	}
	/** 设置：数据库设置id */
	public void setDatabase_id(Long database_id){
		this.database_id=database_id;
	}
	/** 设置：数据库设置id */
	public void setDatabase_id(String database_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(database_id)){
			this.database_id=new Long(database_id);
		}
	}
	/** 取得：数据库采集任务名称 */
	public String getTask_name(){
		return task_name;
	}
	/** 设置：数据库采集任务名称 */
	public void setTask_name(String task_name){
		this.task_name=task_name;
	}
	/** 取得：数据库名称 */
	public String getDatabase_name(){
		return database_name;
	}
	/** 设置：数据库名称 */
	public void setDatabase_name(String database_name){
		this.database_name=database_name;
	}
	/** 取得：数据库密码 */
	public String getDatabase_pad(){
		return database_pad;
	}
	/** 设置：数据库密码 */
	public void setDatabase_pad(String database_pad){
		this.database_pad=database_pad;
	}
	/** 取得：数据库驱动 */
	public String getDatabase_drive(){
		return database_drive;
	}
	/** 设置：数据库驱动 */
	public void setDatabase_drive(String database_drive){
		this.database_drive=database_drive;
	}
	/** 取得：数据库类型 */
	public String getDatabase_type(){
		return database_type;
	}
	/** 设置：数据库类型 */
	public void setDatabase_type(String database_type){
		this.database_type=database_type;
	}
	/** 取得：用户名称 */
	public String getUser_name(){
		return user_name;
	}
	/** 设置：用户名称 */
	public void setUser_name(String user_name){
		this.user_name=user_name;
	}
	/** 取得：数据库服务器IP */
	public String getDatabase_ip(){
		return database_ip;
	}
	/** 设置：数据库服务器IP */
	public void setDatabase_ip(String database_ip){
		this.database_ip=database_ip;
	}
	/** 取得：数据库端口 */
	public String getDatabase_port(){
		return database_port;
	}
	/** 设置：数据库端口 */
	public void setDatabase_port(String database_port){
		this.database_port=database_port;
	}
	/** 取得：主机名 */
	public String getHost_name(){
		return host_name;
	}
	/** 设置：主机名 */
	public void setHost_name(String host_name){
		this.host_name=host_name;
	}
	/** 取得：操作系统类型 */
	public String getSystem_type(){
		return system_type;
	}
	/** 设置：操作系统类型 */
	public void setSystem_type(String system_type){
		this.system_type=system_type;
	}
	/** 取得：是否设置完成并发送成功 */
	public String getIs_sendok(){
		return is_sendok;
	}
	/** 设置：是否设置完成并发送成功 */
	public void setIs_sendok(String is_sendok){
		this.is_sendok=is_sendok;
	}
	/** 取得：数据库设置编号 */
	public String getDatabase_number(){
		return database_number;
	}
	/** 设置：数据库设置编号 */
	public void setDatabase_number(String database_number){
		this.database_number=database_number;
	}
	/** 取得：是否DB文件数据采集 */
	public String getDb_agent(){
		return db_agent;
	}
	/** 设置：是否DB文件数据采集 */
	public void setDb_agent(String db_agent){
		this.db_agent=db_agent;
	}
	/** 取得：DB文件数据字典位置 */
	public String getPlane_url(){
		return plane_url;
	}
	/** 设置：DB文件数据字典位置 */
	public void setPlane_url(String plane_url){
		this.plane_url=plane_url;
	}
	/** 取得：数据采用分隔符 */
	public String getDatabase_separatorr(){
		return database_separatorr;
	}
	/** 设置：数据采用分隔符 */
	public void setDatabase_separatorr(String database_separatorr){
		this.database_separatorr=database_separatorr;
	}
	/** 取得：数据行分隔符 */
	public String getRow_separator(){
		return row_separator;
	}
	/** 设置：数据行分隔符 */
	public void setRow_separator(String row_separator){
		this.row_separator=row_separator;
	}
	/** 取得：分类id */
	public Long getClassify_id(){
		return classify_id;
	}
	/** 设置：分类id */
	public void setClassify_id(Long classify_id){
		this.classify_id=classify_id;
	}
	/** 设置：分类id */
	public void setClassify_id(String classify_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(classify_id)){
			this.classify_id=new Long(classify_id);
		}
	}
	/** 取得：清洗顺序 */
	public String getCp_or(){
		return cp_or;
	}
	/** 设置：清洗顺序 */
	public void setCp_or(String cp_or){
		this.cp_or=cp_or;
	}
	/** 取得：数据库连接地址 */
	public String getJdbc_url(){
		return jdbc_url;
	}
	/** 设置：数据库连接地址 */
	public void setJdbc_url(String jdbc_url){
		this.jdbc_url=jdbc_url;
	}
	/** 取得：数据库采集方式 */
	public String getCollect_type(){
		return collect_type;
	}
	/** 设置：数据库采集方式 */
	public void setCollect_type(String collect_type){
		this.collect_type=collect_type;
	}
	/** 取得：存储层配置ID */
	public Long getDsl_id(){
		return dsl_id;
	}
	/** 设置：存储层配置ID */
	public void setDsl_id(Long dsl_id){
		this.dsl_id=dsl_id;
	}
	/** 设置：存储层配置ID */
	public void setDsl_id(String dsl_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(dsl_id)){
			this.dsl_id=new Long(dsl_id);
		}
	}
}
