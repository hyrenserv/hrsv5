package hrds.agent.job.biz.constant;


import hrds.agent.job.biz.utils.PropertyParaUtil;
import hrds.commons.codes.IsFlag;

import java.io.File;

public class JobConstant {
	//写文件时缓存的行数
	public static final int BUFFER_ROW = 5000;
	//TODO 下面这些是从配置文件取，需要放到agent下面
	//XXX 这里文件采集上传到本地还是HDFS如果是页面上选的，下面这个参数就不需要了
	public static final boolean FILE_COLLECTION_IS_WRITE_HADOOP = IsFlag.Shi.getCode().equals(PropertyParaUtil.getString(
		"file_collection_is_write_hadoop", IsFlag.Fou.getCode()));
	public static final long FILE_BLOCKSIZE = Long.parseLong(PropertyParaUtil.getString("file_blocksize",
		"1024")) * 1024 * 1024L;//卸数写文件大小默认1024M file_blocksize
	//    public static final String SYS_DATEFORMAT = PropertyParaUtil.getString("SYS_DATEFORMAT", "yyyyMMdd");
	//是否写多文件
	public static final boolean WriteMultipleFiles = IsFlag.Shi.getCode().equals(PropertyParaUtil.getString(
		"writemultiplefiles", IsFlag.Fou.getCode()));
	//判断文件变化的类型是否是MD5
	public static final boolean FILECHANGESTYPEMD5 = "MD5".equals(PropertyParaUtil.getString(
		"determineFileChangesType", ""));
	//文件采集进solr摘要获取行数，默认获取前3行
	public static final int SUMMARY_VOLUMN = Integer.parseInt(PropertyParaUtil.getString("summary_volumn",
		"3"));
	//文件采集设定单个Avro文件带下的阈值 默认为128M
	public static final long SINGLE_AVRO_SIZE = Long.parseLong(PropertyParaUtil.getString("singleAvroSize",
		"134217728"));
	//文件采集大文件的阈值，超过此大小则认为是大文件，默认为25M
	public static final long THRESHOLD_FILE_SIZE = Long.parseLong(PropertyParaUtil.getString("thresholdFileSize",
		"26214400"));
	//文件上传到hdfs的顶层目录
	public static final String PREFIX = PropertyParaUtil.getString("pathprefix", "/hrds");
	//是否添加isAddOperateInfo
	public static final boolean ISADDOPERATEINFO = Boolean.parseBoolean(PropertyParaUtil.
		getString("isAddOperateInfo", "false"));
	//是否添加isWriteDictionary
	public static final boolean ISWRITEDICTIONARY = Boolean.parseBoolean(PropertyParaUtil.
		getString("isWriteDictionary", "false"));
	//多线程指定线程池的默认线程数
	public static final int AVAILABLEPROCESSORS = Integer.parseInt(PropertyParaUtil.getString("availableProcessors",
		String.valueOf(Runtime.getRuntime().availableProcessors())));
	//solr进数的实现类
	public static final String SOLRCLASSNAME = PropertyParaUtil.getString("solrclassname", "");
	//XXX 文件采集进solr没做选择存储层临时使用
	public static final String SOLRZKHOST = PropertyParaUtil.getString("zkHost", "");
	//XXX 文件采集进solr没做选择存储层临时使用
	public static final String SOLRCOLLECTION = PropertyParaUtil.getString("collection", "");
	//数据进hbase,bulklode临时文件产生区
	public static final String TMPDIR = PREFIX + File.separator + "TMP";//临时文件产生区
	//agent接受页面配置文件和数据字典存放的顶层目录
	public static final String agentConfigPath = PropertyParaUtil.getString("agentConfigPath", "");
	/**
	 * 数据库采集数据字典存放的顶层目录
	 */
	public static final String DICTIONARY = agentConfigPath + File.separator + "dictionary" + File.separator;
	/**
	 * 存放采集页面配置的数据文件
	 */
	public static final String MESSAGEFILE = agentConfigPath + File.separator + "messageFile" + File.separator;
}

