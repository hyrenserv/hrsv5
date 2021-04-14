package hrds.commons.utils;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.FileUtil;
import hrds.commons.codes.CleanType;
import hrds.commons.codes.FileFormat;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "项目中经常用到的常量值", author = "zxz", createdate = "2019/12/25 0025 下午 04:28")
public class Constant {

	public static final String SDATENAME = "HYREN_S_DATE";
	public static final String EDATENAME = "HYREN_E_DATE";
	public static final String MD5NAME = "HYREN_MD5_VAL";
	public static final String HYREN_OPER_DATE = "HYREN_OPER_DATE";
	public static final String HYREN_OPER_TIME = "HYREN_OPER_TIME";
	public static final String HYREN_OPER_PERSON = "HYREN_OPER_PERSON";
	public static final String TABLE_ID_NAME = "HYREN_TABLE_ID";
	/**
	 * 海云建表时的一些字段信息, 防止集市在查询时使用此类字段. 所以这里进行字段的统计提供给集市过滤使用
	 */
	private static final String[] HYRENCOLUMN = {SDATENAME, EDATENAME, MD5NAME, HYREN_OPER_DATE, HYREN_OPER_TIME,
		HYREN_OPER_PERSON, TABLE_ID_NAME};
	public static final List<String> HYRENFIELD = Arrays.asList(HYRENCOLUMN);
	/**
	 * 数据有效日期
	 */
	public static final String MAXDATE = "99991231";
	/**
	 * 数据有效日期
	 */
	public static final String INVDATE = "99999999";
	/**
	 * 数据初始化日期
	 */
	public static final String INITDATE = "00000000";
	/**
	 * Hbase的默认列族
	 */
	public static final byte[] HBASE_COLUMN_FAMILY = "F".getBytes();
	public static final String HBASE_ROW_KEY = "hyren_key";
	/**
	 * 当前程序运行的目录
	 */
	private static final String USER_DIR = System.getProperty("user.dir");
	/**
	 * jobInfo文件存放的顶层目录
	 */
	public static final String JOBINFOPATH = USER_DIR + File.separator + "jobInfo" + File.separator;
	/**
	 * agent读取数据字典转换为xml保存的文件目录
	 */
	public static final String XMLPATH = USER_DIR + File.separator + "xmlPath" + File.separator;
	/**
	 * job运行程序信息存储文件名称
	 */
	public static final String JOBFILENAME = "jobInfo.json";
	/**
	 * mapDB文件存放的顶层目录
	 */
	public static final String MAPDBPATH = USER_DIR + File.separator + "mapDb" + File.separator;
	//TODO 文件采集卸数文件存放的顶层目录,这里应该是根据系统参数来的
	/**
	 * 文件采集卸数文件存放的顶层目录,这里应该是根据系统参数来的
	 */
	public static final String FILEUNLOADFOLDER = USER_DIR + File.separator + "dirFile" + File.separator;
	//TODO DB文件采集卸数文件存放的顶层目录,这里应该是根据系统参数来的
	/**
	 * DB文件采集卸数文件存放的顶层目录,这里应该是根据系统参数来的
	 */
	public static final String DBFILEUNLOADFOLDER = USER_DIR + File.separator + "dbFile" + File.separator;
	//TODO 存储层上传的配置文件存放的顶层目录
	/**
	 * 存储层上传的配置文件存放的顶层目录
	 */
	public static final String STORECONFIGPATH = USER_DIR + File.separator + "storeConfigPath" + File.separator;
	//TODO 存储文件上传脚本的顶层目录
	/**
	 * 存储文件上传脚本的顶层目录
	 */
	public static final String HDFSSHELLFILE = USER_DIR + File.separator + "hdfsShellFile" + File.separator;
	//通信异常存储目录
	public static final String COMMUNICATIONERRORFOLDER = USER_DIR + File.separator
		+ "CommunicationError" + File.separator;
	//定义并行抽取SQL开始条数占位符
	/**
	 * 定义并行抽取SQL开始条数占位符
	 */
	public static final String PARALLEL_SQL_START = "#{hy_start}";
	//定义并行抽取SQL结束条数占位符
	/**
	 * 定义并行抽取SQL结束条数占位符
	 */
	public static final String PARALLEL_SQL_END = "#{hy_end}";

	/**
	 * 贴源层下批量数据 01:批量类型数据
	 */
	public static final String DCL_BATCH = "dcl_batch";
	/**
	 * 贴源层下实时数据 02:实时类型数据
	 */
	public static final String DCL_REALTIME = "dcl_realtime";
	/**
	 * 系统数据表 sys_data_table:系统数据表
	 */
	public static final String SYS_DATA_TABLE = "sys_data_table";
	/**
	 * 系统数据备份 sys_data_bak:系统数据备份
	 */
	public static final String SYS_DATA_BAK = "sys_data_bak";
	/**
	 * 数据管控结果3表名前缀标记
	 */
	public static final String DQC_TABLE = "dqc_";
	/**
	 * 数据管控失效标记前缀 dqc_invalid_table_
	 */
	public static final String DQC_INVALID_TABLE = "dit_";
	/**
	 * 数据管控表操作类型 set_invalid:设置为无效,restore:恢复
	 */
	public static final String DM_SET_INVALID_TABLE = "set_invalid";
	public static final String DM_RESTORE_TABLE = "restore";
	/**
	 * solr自定义的handler
	 */
	public static final String HANDLER = "/reloadDictionary";
	/**
	 * solr自定义的分隔符
	 */
	public static final char SOLR_DATA_DELIMITER = '\001';
	// 日志审查需要加入日志管理的功能
	public static final String USERLOGIN = "用户登入";
	public static final String USERSIGNOUT = "用户登出";
	public static final String INVALIDUSERINFO = "用户信息失效";
	public static final String DELETEDBTASK = "采集数据库直连任务的删除";
	public static final String DELETEDFTASK = "采集db文件任务的删除记录";
	public static final String DELETEHALFSTRUCTTASK = "采集半结构任务的删除记录";
	public static final String DELETENONSTRUCTTASK = "采集非结构任务的删除记录";
	public static final String DELETEFTPTASK = "采集ftp任务的删除记录";
	public static final String ADDDMDATATABLE = "集市新建数据表";
	public static final String DELETEDMDATATABLE = "集市删除数据表";
	/**
	 * 数据库采集，获取元数据信息拼接的分隔符
	 */
	public static final String METAINFOSPLIT = "^";
	/**
	 * 自定义sql或者自定义并行抽取sql时使用的分隔符
	 */
	public static final String SQLDELIMITER = "`@^";
	/**
	 * DB文件转存默认的行分隔符
	 */
	public static final String DEFAULTLINESEPARATOR = "\n";
	/**
	 * DB文件转存默认的行分隔符的字符串形式
	 */
	public static final String DEFAULTLINESEPARATORSTR = "\\n";
	//拼接sql字段的分隔符
//	public static final String COLUMN_SEPARATOR = ",";
	/**
	 * 此分隔符为拼接MD5的默认分隔符
	 */
	public static final String DATADELIMITER = "`@^";
	/**
	 * 此分隔符为卸数成SequenceFile时的默认分隔符，SequenceFile不允许页面自定义
	 */
	public static final String SEQUENCEDELIMITER = String.valueOf('\001');
	//mysql建表语句的转义符
//	public static final String MYSQL_ESCAPES = "`";
	/**
	 * 作业程序参数默认拼接分隔符
	 */
	public static final String ETLPARASEPARATOR = "@";

	/**
	 * 数据库抽取不同文件格式对应的默认路径名称的映射
	 */
	public static final Map<String, String> fileFormatMap = new HashMap<>();

	/**
	 * 采集程序的默认脚本名称
	 */
	public static final String SHELLCOMMAND = "shellCommand.sh";

	/**
	 * 采集定义启动方式默认配置
	 */
	// 默认前一天跑批日期
	public static final String BATCH_DATE = "#{txdate_pre}";
	// 默认增加一个资源类型
	public static final String RESOURCE_THRESHOLD = "XS_ZT";
	// 资源类默认的阈值
	public static final int RESOURCE_NUM = 15;
	// 单个作业的所需资源数
	public static final int JOB_RESOURCE_NUM = 1;
	// 程序目录的工程系统参数名
	public static final String HYRENBIN = "!{HYSHELLBIN}";
	// 程序日志的工程系统参数名
	public static final String HYRENLOG = "!{HYLOG}";
	// 作业调度系统参数定义程序变量名称
	public static final String PARA_HYRENBIN = "!HYSHELLBIN";
	// 作业调度系统参数定义日志变量名称
	public static final String PARA_HYRENLOG = "!HYLOG";
	// 作业名称/描述之间的分割符
	public static final String SPLITTER = "_";

	/**
	 * Agent启动脚本名称
	 */
	public static final String START_AGENT = "agent_operation.sh";
	/**
	 * 表默认清洗顺序
	 */
	public static final JSONObject DEFAULT_TABLE_CLEAN_ORDER = new JSONObject(true);
	/**
	 * 列默认清洗顺序
	 */
	public static final JSONObject DEFAULT_COLUMN_CLEAN_ORDER = new JSONObject(true);
	/**
	 * 列默认清洗顺序
	 */
	public static final JSONObject DATABASE_CLEAN = new JSONObject(true);
	/*
	 * 数据库抽取卸数下来文件格式对应路径的关系
	 */

	static {
		fileFormatMap.put(FileFormat.FeiDingChang.getCode(), "NONFIXEDFILE");
		fileFormatMap.put(FileFormat.DingChang.getCode(), "FIXEDFILE");
		fileFormatMap.put(FileFormat.CSV.getCode(), FileFormat.CSV.getValue());
		fileFormatMap.put(FileFormat.PARQUET.getCode(), FileFormat.PARQUET.getValue());
		fileFormatMap.put(FileFormat.ORC.getCode(), FileFormat.ORC.getValue());
		fileFormatMap.put(FileFormat.SEQUENCEFILE.getCode(), FileFormat.SEQUENCEFILE.getValue());

		DEFAULT_TABLE_CLEAN_ORDER.put(CleanType.ZiFuBuQi.getCode(), 1);
		DEFAULT_TABLE_CLEAN_ORDER.put(CleanType.ZiFuTiHuan.getCode(), 2);
		DEFAULT_TABLE_CLEAN_ORDER.put(CleanType.ZiFuHeBing.getCode(), 3);
		DEFAULT_TABLE_CLEAN_ORDER.put(CleanType.ZiFuTrim.getCode(), 4);

		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.ZiFuBuQi.getCode(), 1);
		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.ZiFuTiHuan.getCode(), 2);
		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.ShiJianZhuanHuan.getCode(), 3);
		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.MaZhiZhuanHuan.getCode(), 4);
		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.ZiFuChaiFen.getCode(), 5);
		DEFAULT_COLUMN_CLEAN_ORDER.put(CleanType.ZiFuTrim.getCode(), 6);

		DATABASE_CLEAN.put(CleanType.ZiFuBuQi.getCode(), 1);
		DATABASE_CLEAN.put(CleanType.ZiFuTiHuan.getCode(), 2);
		DATABASE_CLEAN.put(CleanType.ShiJianZhuanHuan.getCode(), 3);
		DATABASE_CLEAN.put(CleanType.MaZhiZhuanHuan.getCode(), 4);
		DATABASE_CLEAN.put(CleanType.ZiFuHeBing.getCode(), 5);
		DATABASE_CLEAN.put(CleanType.ZiFuChaiFen.getCode(), 6);
		DATABASE_CLEAN.put(CleanType.ZiFuTrim.getCode(), 7);
	}

	// 项目bin目录路径 E:\hrsv5\bin
	public static final String PROJECT_BIN_DIR = System.getProperty("user.dir") + File.separator + "bin";
	/**
	 * 数据管控创建表的默认表空间
	 */
	public static final String DEFAULT_TABLE_SPACE = "hyshf";
	/**
	 * 空格字符
	 */
	public static final String SPACE = " ";
	/**
	 * 左小括号
	 */
	public static final String LXKH = "(";
	/**
	 * 右小括号
	 */
	public static final String RXKH = ")";

	public static final String COLLECTOKFILE = "hyren_collect_flag.ok";

	// 集市分类分隔符
	public static final String MARKETDELIMITER = "-->";

	//hive映射HBase的rowkey的列名
	public static final String HIVEMAPPINGROWKEY = "rowkey_hyren";

	//自定义静态常量
	public static final String CUSTOMIZE = "customize";
	// 仪表盘发布接口名称
	public static final String DASHBOARDINTERFACENAME = "dashboardRelease";
	/**
	 * 数据对标配置类实体序列化路径前缀
	 */
	public static final String ALGORITHMS_CONF_SERIALIZE_PATH = FileUtil.TEMP_DIR_NAME +
		"algorithms-conf-serialize" + FileUtil.PATH_SEPARATOR_CHAR;
	public static final String HYUCC_RESULT_PATH_NAME = "HyUccOut";
	public static final String HYFD_RESULT_PATH_NAME = "HyFdOut";

	/**
	 * 流数据管理
	 */
	public static final String STREAM_HYREN_END = "hyren_end_end_end";

}
