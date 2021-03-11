package hrds.commons.utils;

import fd.ng.core.annotation.DocClass;

@DocClass(desc = "项目中经常用到的变量值", author = "BY-HLL", createdate = "2020/1/7 0007 上午 10:38")
public class CommonVariables {

	/**
	 * 是否有大数据环境
	 */
	public static final boolean HAS_HADOOP_ENV = "HD".equalsIgnoreCase(PropertyParaValue.getString("ver_type", "DB"));
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
	 * solr的collection's name
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
	public static final String OCR_SERVER_ADDRESS = PropertyParaValue.getString("ocr_rpc_cpu", "");
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
	public static final String SFTP_PORT = PropertyParaValue.getString("sftp_port", "22");
}
