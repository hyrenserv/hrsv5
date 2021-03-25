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
 * 流数据管理消费目的地管理
 */
@Table(tableName = "sdm_consume_des")
public class Sdm_consume_des extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "sdm_consume_des";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 流数据管理消费目的地管理 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("sdm_des_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="sdm_des_id",value="配置id:",dataType = Long.class,required = true)
	private Long sdm_des_id;
	@DocBean(name ="sdm_thr_partition",value="消费线程与分区的关系(SdmThreadPartition):1-一对一<YiDuiYi> 2-一对多<YiDuiDuo> ",dataType = String.class,required = true)
	private String sdm_thr_partition;
	@DocBean(name ="sdm_cons_des",value="消费端目的地(ConsDirection):1-内部<NeiBu> 2-外部<WaiBu> ",dataType = String.class,required = true)
	private String sdm_cons_des;
	@DocBean(name ="remark",value="备注:",dataType = String.class,required = false)
	private String remark;
	@DocBean(name ="thread_num",value="线程数:",dataType = Integer.class,required = false)
	private Integer thread_num;
	@DocBean(name ="partition",value="分区:",dataType = String.class,required = false)
	private String partition;
	@DocBean(name ="sdm_bus_pro_cla",value="业务处理类:",dataType = String.class,required = false)
	private String sdm_bus_pro_cla;
	@DocBean(name ="sdm_consum_id",value="消费端配置id:",dataType = Long.class,required = true)
	private Long sdm_consum_id;
	@DocBean(name ="sdm_conf_describe",value="海云外部流数据管理消费端目的地(SdmConsumeDestination):1-数据库<ShuJuKu> 2-hbase<Hbase> 3-rest服务<RestFuWu> 4-文件<LiuWenJian> 5-二进制文件<ErJinZhiWenJian> 6-Kafka<Kafka> 7-自定义业务类<ZiDingYeWuLei> ",dataType = String.class,required = false)
	private String sdm_conf_describe;
	@DocBean(name ="hdfs_file_type",value="hdfs文件类型(HdfsFileType):1-csv<Csv> 2-parquet<Parquet> 3-avro<Avro> 4-orcfile<OrcFile> 5-sequencefile<SequenceFile> 6-其他<Other> ",dataType = String.class,required = false)
	private String hdfs_file_type;
	@DocBean(name ="des_class",value="目的地业务处理类:",dataType = String.class,required = false)
	private String des_class;
	@DocBean(name ="external_file_type",value="外部文件类型(HdfsFileType):1-csv<Csv> 2-parquet<Parquet> 3-avro<Avro> 4-orcfile<OrcFile> 5-sequencefile<SequenceFile> 6-其他<Other> ",dataType = String.class,required = false)
	private String external_file_type;
	@DocBean(name ="cus_des_type",value="自定义业务类类型(SdmCustomBusCla):0-None<NONE> 1-Java<Java> 2-JavaScript<JavaScript> ",dataType = String.class,required = true)
	private String cus_des_type;
	@DocBean(name ="descustom_buscla",value="目的地业务类类型(SdmCustomBusCla):0-None<NONE> 1-Java<Java> 2-JavaScript<JavaScript> ",dataType = String.class,required = true)
	private String descustom_buscla;
	@DocBean(name ="sdm_conf_para_id",value="sdm_conf_para_id:",dataType = Long.class,required = true)
	private Long sdm_conf_para_id;

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
	/** 取得：消费线程与分区的关系 */
	public String getSdm_thr_partition(){
		return sdm_thr_partition;
	}
	/** 设置：消费线程与分区的关系 */
	public void setSdm_thr_partition(String sdm_thr_partition){
		this.sdm_thr_partition=sdm_thr_partition;
	}
	/** 取得：消费端目的地 */
	public String getSdm_cons_des(){
		return sdm_cons_des;
	}
	/** 设置：消费端目的地 */
	public void setSdm_cons_des(String sdm_cons_des){
		this.sdm_cons_des=sdm_cons_des;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：线程数 */
	public Integer getThread_num(){
		return thread_num;
	}
	/** 设置：线程数 */
	public void setThread_num(Integer thread_num){
		this.thread_num=thread_num;
	}
	/** 设置：线程数 */
	public void setThread_num(String thread_num){
		if(!fd.ng.core.utils.StringUtil.isEmpty(thread_num)){
			this.thread_num=new Integer(thread_num);
		}
	}
	/** 取得：分区 */
	public String getPartition(){
		return partition;
	}
	/** 设置：分区 */
	public void setPartition(String partition){
		this.partition=partition;
	}
	/** 取得：业务处理类 */
	public String getSdm_bus_pro_cla(){
		return sdm_bus_pro_cla;
	}
	/** 设置：业务处理类 */
	public void setSdm_bus_pro_cla(String sdm_bus_pro_cla){
		this.sdm_bus_pro_cla=sdm_bus_pro_cla;
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
	/** 取得：海云外部流数据管理消费端目的地 */
	public String getSdm_conf_describe(){
		return sdm_conf_describe;
	}
	/** 设置：海云外部流数据管理消费端目的地 */
	public void setSdm_conf_describe(String sdm_conf_describe){
		this.sdm_conf_describe=sdm_conf_describe;
	}
	/** 取得：hdfs文件类型 */
	public String getHdfs_file_type(){
		return hdfs_file_type;
	}
	/** 设置：hdfs文件类型 */
	public void setHdfs_file_type(String hdfs_file_type){
		this.hdfs_file_type=hdfs_file_type;
	}
	/** 取得：目的地业务处理类 */
	public String getDes_class(){
		return des_class;
	}
	/** 设置：目的地业务处理类 */
	public void setDes_class(String des_class){
		this.des_class=des_class;
	}
	/** 取得：外部文件类型 */
	public String getExternal_file_type(){
		return external_file_type;
	}
	/** 设置：外部文件类型 */
	public void setExternal_file_type(String external_file_type){
		this.external_file_type=external_file_type;
	}
	/** 取得：自定义业务类类型 */
	public String getCus_des_type(){
		return cus_des_type;
	}
	/** 设置：自定义业务类类型 */
	public void setCus_des_type(String cus_des_type){
		this.cus_des_type=cus_des_type;
	}
	/** 取得：目的地业务类类型 */
	public String getDescustom_buscla(){
		return descustom_buscla;
	}
	/** 设置：目的地业务类类型 */
	public void setDescustom_buscla(String descustom_buscla){
		this.descustom_buscla=descustom_buscla;
	}
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
}
