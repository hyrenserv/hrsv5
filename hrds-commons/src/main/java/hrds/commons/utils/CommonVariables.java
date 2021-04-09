package hrds.commons.utils;

import fd.ng.core.annotation.DocClass;
import hrds.commons.codes.IsFlag;

@DocClass(desc = "项目中经常用到的变量值", author = "BY-HLL", createdate = "2020/1/7 0007 上午 10:38")
public class CommonVariables {

	/**
	 * 非结构化,文件采集是否
	 */
	public static final boolean FILE_COLLECTION_IS_WRITE_HADOOP = IsFlag.Shi.getCode().equals(
		PropertyParaValue.getString("file_collection_is_write_hadoop", IsFlag.Fou.getCode()));
	/**
	 * 开启kerberos认证后,服务认证实例名
	 */
	public static final String PRINCIPLE_NAME = PropertyParaValue.getString("principle.name",
		"hyshf@beyondsoft.com");
	/**
	 * 初始化solr具体实现类全名
	 */
	public static final String SOLR_IMPL_CLASS_NAME = PropertyParaValue.getString("solrclassname",
		"hrds.commons.hadoop.solr.impl.SolrOperatorImpl5_3_1");
	/**
	 * solr的collection's name 非结构化,文件采集
	 */
	public static final String SOLR_COLLECTION = PropertyParaValue.getString("collection",
		"HrdsFullTextIndexing");
	/**
	 * solr创建索引批量提交数
	 */
	public static final int SOLR_BULK_SUBMISSIONS_NUM = PropertyParaValue.getInt("solr_bulk_submissions_num",
		50000);
	/**
	 * solr的ZKHOST地址
	 */
	public static final String ZK_HOST = PropertyParaValue.getString("zkHost",
		"hdp001.beyondsoft.com:2181,hdp002.beyondsoft.com:2181,hdp003.beyondsoft.com:2181/solr");
	/**
	 * 对于SQL的字段是否使用字段验证
	 */
	public static final String AUTHORITY = PropertyParaValue.getString("restAuthority", "");
	/**
	 * 接口数据文件的存放路径
	 */
	public static final String RESTFILEPATH = PropertyParaValue.getString("restFilePath", "");
	/**
	 * 数据对标，表函数依赖和主键分析输出结果的根目录
	 */
	public static final String ALGORITHMS_RESULT_ROOT_PATH = PropertyParaValue.getString(
		"algorithms_result_root_path", "");
	/**
	 * 部署时服务器的默认端口
	 */
	public static final int SFTP_PORT = PropertyParaValue.getInt("sftp_port", 22);
	/**
	 * OCR 服务请求地址
	 */
	public static final String OCR_RPC_ADDRESS = PropertyParaValue.getString("ocr_rpc_address",
		"127.0.0.1:18101");
	/**
	 * 是否使用的是OCR RPC服务,还是Apache OCR, 默认1 OCR RPC
	 */
	public static final boolean USE_OCR_RPC = IsFlag.Shi.getCode().equals(PropertyParaValue.getString(
		"use_ocr_rpc", "0"));
	/**
	 * OCR 跑批线程池大小
	 */
	public static final int OCR_THREAD_POOL = PropertyParaValue.getInt("ocr_thread_pool", 4);
	/**
	 * OCR 识别语言，默认简体中文(chi_sim)
	 */
	public static final String OCR_RECOGNITION_LANGUAGE = PropertyParaValue.getString(
		"ocr_recognition_language", "chi_sim");
	/**
	 * OCR 提取的摘要行数,默认 3 行
	 */
	public static final int SUMMARY_VOLUMN = PropertyParaValue.getInt("summary_volumn", 3);
}
