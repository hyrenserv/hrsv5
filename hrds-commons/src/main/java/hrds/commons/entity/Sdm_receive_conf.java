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
 * 流数据管理接收端配置表
 */
@Table(tableName = "sdm_receive_conf")
public class Sdm_receive_conf extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_receive_conf";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理接收端配置表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_receive_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="ra_file_path",value="文本流文件路径:",dataType = String.class,required = true)
	private String ra_file_path;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="sdm_partition",value="分区方式(SdmPatitionWay):1-随机分布<SuiJiFenBu> 2-key<Key> 3-分区<FenQu> ",dataType = String.class,required = true)
	private String sdm_partition;
	@DocBean(name ="sdm_partition_name",value="分区方式类名:",dataType = String.class,required = false)
	private String sdm_partition_name;
	@DocBean(name ="is_file_attr_ip",value="是否包含文件属性（所在主机ip）(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_file_attr_ip;
	@DocBean(name ="is_full_path",value="是否包含文件属性（全路径）(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_full_path;
	@DocBean(name ="is_file_name",value="是否包含文件属性（文件名）(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_file_name;
	@DocBean(name ="is_data_partition",value="是否行数据分割(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_data_partition;
	@DocBean(name ="sdm_dat_delimiter",value="流数据管理数据分割符:",dataType = String.class,required = false)
	private String sdm_dat_delimiter;
	@DocBean(name ="create_date",value="创建日期:",dataType = String.class,required = true)
	private String create_date;
	@DocBean(name ="create_time",value="创建时间:",dataType = String.class,required = true)
	private String create_time;
	@DocBean(name ="sdm_receive_name",value="任务名称:",dataType = String.class,required = true)
	private String sdm_receive_name;
	@DocBean(name ="sdm_rec_des",value="任务描述:",dataType = String.class,required = false)
	private String sdm_rec_des;
	@DocBean(name ="msgtype",value="消息类型:",dataType = String.class,required = false)
	private String msgtype;
	@DocBean(name ="msgheader",value="消息头:",dataType = String.class,required = false)
	private String msgheader;
	@DocBean(name ="file_handle",value="文件处理类:",dataType = String.class,required = false)
	private String file_handle;
	@DocBean(name ="code",value="编码(DataBaseCode):1-UTF-8<UTF_8> 2-GBK<GBK> 3-UTF-16<UTF_16> 4-GB2312<GB2312> 5-ISO-8859-1<ISO_8859_1> ",dataType = String.class,required = false)
	private String code;
	@DocBean(name ="file_initposition",value="文件初始位置:",dataType = String.class,required = false)
	private String file_initposition;
	@DocBean(name ="file_read_num",value="自定义读取行数:",dataType = String.class,required = false)
	private String file_read_num;
	@DocBean(name ="is_file_time",value="是否包含文件时间(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_file_time;
	@DocBean(name ="is_file_size",value="是否包含文件大小(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_file_size;
	@DocBean(name ="read_mode",value="对象采集方式(ObjectCollectType):1-行采集<HangCaiJi> 2-对象采集<DuiXiangCaiJi> ",dataType = String.class,required = true)
	private String read_mode;
	@DocBean(name ="monitor_type",value="监听类型(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String monitor_type;
	@DocBean(name ="thread_num",value="线程数:",dataType = Long.class,required = true)
	private Long thread_num;
	@DocBean(name ="file_match_rule",value="文件匹配规则:",dataType = String.class,required = false)
	private String file_match_rule;
	@DocBean(name ="sdm_bus_pro_cla",value="自定义业务类:",dataType = String.class,required = false)
	private String sdm_bus_pro_cla;
	@DocBean(name ="read_type",value="读取方式(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String read_type;
	@DocBean(name ="is_obj",value="是否包含对象属性(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_obj;
	@DocBean(name ="file_readtype",value="文本读取方式:",dataType = String.class,required = false)
	private String file_readtype;
	@DocBean(name ="sdm_email",value="警告发送email:",dataType = String.class,required = false)
	private String sdm_email;
	@DocBean(name ="check_cycle",value="警告发送周期:",dataType = Integer.class,required = false)
	private Integer check_cycle;
	@DocBean(name ="snmp_ip",value="snmp主机p:",dataType = String.class,required = false)
	private String snmp_ip;
	@DocBean(name ="fault_alarm_mode",value="错误提醒方式:",dataType = String.class,required = false)
	private String fault_alarm_mode;
	@DocBean(name ="is_line_num",value="是否包含文件行数(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_line_num;
	@DocBean(name ="cus_des_type",value="自定义业务类类型(SdmCustomBusCla):0-None<NONE> 1-Java<Java> 2-JavaScript<JavaScript> ",dataType = String.class,required = true)
	private String cus_des_type;
	@DocBean(name ="run_way",value="启动方式(ExecuteWay):1-按时启动<AnShiQiDong> 2-命令触发<MingLingChuFa> 3-信号文件触发<QianZhiTiaoJian> ",dataType = String.class,required = true)
	private String run_way;
	@DocBean(name ="sdm_agent_id",value="流数据管理agent_id:",dataType = Long.class,required = true)
	private Long sdm_agent_id;
	@DocBean(name ="sdm_receive_id",value="流数据管理:",dataType = Long.class,required = true)
	private Long sdm_receive_id;
	@DocBean(name ="sdm_rec_port",value="流数据管理接收端口号:",dataType = String.class,required = false)
	private String sdm_rec_port;

	/** 取得：文本流文件路径 */
	public String getRa_file_path(){
		return ra_file_path;
	}
	/** 设置：文本流文件路径 */
	public void setRa_file_path(String ra_file_path){
		this.ra_file_path=ra_file_path;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：分区方式 */
	public String getSdm_partition(){
		return sdm_partition;
	}
	/** 设置：分区方式 */
	public void setSdm_partition(String sdm_partition){
		this.sdm_partition=sdm_partition;
	}
	/** 取得：分区方式类名 */
	public String getSdm_partition_name(){
		return sdm_partition_name;
	}
	/** 设置：分区方式类名 */
	public void setSdm_partition_name(String sdm_partition_name){
		this.sdm_partition_name=sdm_partition_name;
	}
	/** 取得：是否包含文件属性（所在主机ip） */
	public String getIs_file_attr_ip(){
		return is_file_attr_ip;
	}
	/** 设置：是否包含文件属性（所在主机ip） */
	public void setIs_file_attr_ip(String is_file_attr_ip){
		this.is_file_attr_ip=is_file_attr_ip;
	}
	/** 取得：是否包含文件属性（全路径） */
	public String getIs_full_path(){
		return is_full_path;
	}
	/** 设置：是否包含文件属性（全路径） */
	public void setIs_full_path(String is_full_path){
		this.is_full_path=is_full_path;
	}
	/** 取得：是否包含文件属性（文件名） */
	public String getIs_file_name(){
		return is_file_name;
	}
	/** 设置：是否包含文件属性（文件名） */
	public void setIs_file_name(String is_file_name){
		this.is_file_name=is_file_name;
	}
	/** 取得：是否行数据分割 */
	public String getIs_data_partition(){
		return is_data_partition;
	}
	/** 设置：是否行数据分割 */
	public void setIs_data_partition(String is_data_partition){
		this.is_data_partition=is_data_partition;
	}
	/** 取得：流数据管理数据分割符 */
	public String getSdm_dat_delimiter(){
		return sdm_dat_delimiter;
	}
	/** 设置：流数据管理数据分割符 */
	public void setSdm_dat_delimiter(String sdm_dat_delimiter){
		this.sdm_dat_delimiter=sdm_dat_delimiter;
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
	/** 取得：任务名称 */
	public String getSdm_receive_name(){
		return sdm_receive_name;
	}
	/** 设置：任务名称 */
	public void setSdm_receive_name(String sdm_receive_name){
		this.sdm_receive_name=sdm_receive_name;
	}
	/** 取得：任务描述 */
	public String getSdm_rec_des(){
		return sdm_rec_des;
	}
	/** 设置：任务描述 */
	public void setSdm_rec_des(String sdm_rec_des){
		this.sdm_rec_des=sdm_rec_des;
	}
	/** 取得：消息类型 */
	public String getMsgtype(){
		return msgtype;
	}
	/** 设置：消息类型 */
	public void setMsgtype(String msgtype){
		this.msgtype=msgtype;
	}
	/** 取得：消息头 */
	public String getMsgheader(){
		return msgheader;
	}
	/** 设置：消息头 */
	public void setMsgheader(String msgheader){
		this.msgheader=msgheader;
	}
	/** 取得：文件处理类 */
	public String getFile_handle(){
		return file_handle;
	}
	/** 设置：文件处理类 */
	public void setFile_handle(String file_handle){
		this.file_handle=file_handle;
	}
	/** 取得：编码 */
	public String getCode(){
		return code;
	}
	/** 设置：编码 */
	public void setCode(String code){
		this.code=code;
	}
	/** 取得：文件初始位置 */
	public String getFile_initposition(){
		return file_initposition;
	}
	/** 设置：文件初始位置 */
	public void setFile_initposition(String file_initposition){
		this.file_initposition=file_initposition;
	}
	/** 取得：自定义读取行数 */
	public String getFile_read_num(){
		return file_read_num;
	}
	/** 设置：自定义读取行数 */
	public void setFile_read_num(String file_read_num){
		this.file_read_num=file_read_num;
	}
	/** 取得：是否包含文件时间 */
	public String getIs_file_time(){
		return is_file_time;
	}
	/** 设置：是否包含文件时间 */
	public void setIs_file_time(String is_file_time){
		this.is_file_time=is_file_time;
	}
	/** 取得：是否包含文件大小 */
	public String getIs_file_size(){
		return is_file_size;
	}
	/** 设置：是否包含文件大小 */
	public void setIs_file_size(String is_file_size){
		this.is_file_size=is_file_size;
	}
	/** 取得：对象采集方式 */
	public String getRead_mode(){
		return read_mode;
	}
	/** 设置：对象采集方式 */
	public void setRead_mode(String read_mode){
		this.read_mode=read_mode;
	}
	/** 取得：监听类型 */
	public String getMonitor_type(){
		return monitor_type;
	}
	/** 设置：监听类型 */
	public void setMonitor_type(String monitor_type){
		this.monitor_type=monitor_type;
	}
	/** 取得：线程数 */
	public Long getThread_num(){
		return thread_num;
	}
	/** 设置：线程数 */
	public void setThread_num(Long thread_num){
		this.thread_num=thread_num;
	}
	/** 设置：线程数 */
	public void setThread_num(String thread_num){
		if(!fd.ng.core.utils.StringUtil.isEmpty(thread_num)){
			this.thread_num=new Long(thread_num);
		}
	}
	/** 取得：文件匹配规则 */
	public String getFile_match_rule(){
		return file_match_rule;
	}
	/** 设置：文件匹配规则 */
	public void setFile_match_rule(String file_match_rule){
		this.file_match_rule=file_match_rule;
	}
	/** 取得：自定义业务类 */
	public String getSdm_bus_pro_cla(){
		return sdm_bus_pro_cla;
	}
	/** 设置：自定义业务类 */
	public void setSdm_bus_pro_cla(String sdm_bus_pro_cla){
		this.sdm_bus_pro_cla=sdm_bus_pro_cla;
	}
	/** 取得：读取方式 */
	public String getRead_type(){
		return read_type;
	}
	/** 设置：读取方式 */
	public void setRead_type(String read_type){
		this.read_type=read_type;
	}
	/** 取得：是否包含对象属性 */
	public String getIs_obj(){
		return is_obj;
	}
	/** 设置：是否包含对象属性 */
	public void setIs_obj(String is_obj){
		this.is_obj=is_obj;
	}
	/** 取得：文本读取方式 */
	public String getFile_readtype(){
		return file_readtype;
	}
	/** 设置：文本读取方式 */
	public void setFile_readtype(String file_readtype){
		this.file_readtype=file_readtype;
	}
	/** 取得：警告发送email */
	public String getSdm_email(){
		return sdm_email;
	}
	/** 设置：警告发送email */
	public void setSdm_email(String sdm_email){
		this.sdm_email=sdm_email;
	}
	/** 取得：警告发送周期 */
	public Integer getCheck_cycle(){
		return check_cycle;
	}
	/** 设置：警告发送周期 */
	public void setCheck_cycle(Integer check_cycle){
		this.check_cycle=check_cycle;
	}
	/** 设置：警告发送周期 */
	public void setCheck_cycle(String check_cycle){
		if(!fd.ng.core.utils.StringUtil.isEmpty(check_cycle)){
			this.check_cycle=new Integer(check_cycle);
		}
	}
	/** 取得：snmp主机p */
	public String getSnmp_ip(){
		return snmp_ip;
	}
	/** 设置：snmp主机p */
	public void setSnmp_ip(String snmp_ip){
		this.snmp_ip=snmp_ip;
	}
	/** 取得：错误提醒方式 */
	public String getFault_alarm_mode(){
		return fault_alarm_mode;
	}
	/** 设置：错误提醒方式 */
	public void setFault_alarm_mode(String fault_alarm_mode){
		this.fault_alarm_mode=fault_alarm_mode;
	}
	/** 取得：是否包含文件行数 */
	public String getIs_line_num(){
		return is_line_num;
	}
	/** 设置：是否包含文件行数 */
	public void setIs_line_num(String is_line_num){
		this.is_line_num=is_line_num;
	}
	/** 取得：自定义业务类类型 */
	public String getCus_des_type(){
		return cus_des_type;
	}
	/** 设置：自定义业务类类型 */
	public void setCus_des_type(String cus_des_type){
		this.cus_des_type=cus_des_type;
	}
	/** 取得：启动方式 */
	public String getRun_way(){
		return run_way;
	}
	/** 设置：启动方式 */
	public void setRun_way(String run_way){
		this.run_way=run_way;
	}
	/** 取得：流数据管理agent_id */
	public Long getSdm_agent_id(){
		return sdm_agent_id;
	}
	/** 设置：流数据管理agent_id */
	public void setSdm_agent_id(Long sdm_agent_id){
		this.sdm_agent_id=sdm_agent_id;
	}
	/** 设置：流数据管理agent_id */
	public void setSdm_agent_id(String sdm_agent_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(sdm_agent_id)){
			this.sdm_agent_id=new Long(sdm_agent_id);
		}
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
	/** 取得：流数据管理接收端口号 */
	public String getSdm_rec_port(){
		return sdm_rec_port;
	}
	/** 设置：流数据管理接收端口号 */
	public void setSdm_rec_port(String sdm_rec_port){
		this.sdm_rec_port=sdm_rec_port;
	}
}
