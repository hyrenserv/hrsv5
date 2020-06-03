package hrds.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import hrds.commons.codes.DatabaseType;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 标 题: 海云数服务
 *
 * <p>描 述: 获取数据看连接
 *
 * <p>版 权: Copyright (c) 2016
 *
 * <p>公 司: 上海泓智信息科技有限公司
 *
 * <p>创建时间: 2015-12-30 上午10:08:05
 *
 * <p>@author mashimaro
 *
 * <p>@version 1.0
 *
 * <p>ConnUtil.java
 *
 * <p>
 */
public class ConnUtil {

	private static final Log logger = LogFactory.getLog(ConnUtil.class);

	@SuppressWarnings("unused")
	public static JSONObject getConn_url(String type, String... database_port) {

		JSONObject jdbcObj = new JSONObject(true);
		String conn_url = "";
		if (DatabaseType.MYSQL.getCode().equals(type)) {
			//			conn_url = "jdbc:mysql://" + database_ip + ":" + database_port + "/" + database_name
			//							+ "?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";
			jdbcObj.put("jdbcPrefix", "jdbc:mysql://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull");
		} else if (DatabaseType.Oracle9i.getCode().equals(type)
			|| DatabaseType.Oracle10g.getCode().equals(type)) {
			//			conn_url = "jdbc:oracle:thin:@" + database_ip + ":" + database_port + ":" +
			// database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:oracle:thin:@");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", ':');
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.DB2.getCode().equals(type)) {
			//			conn_url = "jdbc:db2://" + database_ip + ":" + database_port + "/" + database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:db2://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.SqlServer2005.getCode().equals(type)) {
			//			conn_url = "jdbc:sqlserver://" + database_ip + ":" + database_port + ";DatabaseName=" +
			// database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:sqlserver://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", ";DatabaseName=");
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.SqlServer2000.getCode().equals(type)) {
			//			conn_url = "jdbc:sqlserver://" + database_ip + ":" + database_port + ";DatabaseName=" +
			// database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:sqlserver://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", ";DatabaseName=");
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.Postgresql.getCode().equals(type)) {
			//			conn_url = "jdbc:postgresql://" + database_ip + ":" + database_port + "/" +
			// database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:postgresql://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.SybaseASE125.getCode().equals(type)) {
			//			conn_url = "jdbc:sybase:Tds:" + database_ip + ":" + database_port + "/" + database_name;
			jdbcObj.put("jdbcPrefix", "jdbc:sybase:Tds:");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.ApacheDerby.getCode().equals(type)) {
			//			conn_url = "jdbc:derby://" + database_ip + ":" + database_port + "/" + database_name +
			// ";create=true";
			jdbcObj.put("jdbcPrefix", "jdbc:derby://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", ";create=true");
		} else if (DatabaseType.GBase.getCode().equals(type)) {
			//			conn_url = "jdbc:gbase://" + database_ip + ":" + database_port + "/" + database_name +
			// "";
			jdbcObj.put("jdbcPrefix", "jdbc:gbase://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "");
		} else if (DatabaseType.TeraData.getCode().equals(type)) {
			for (int i = 0; i < database_port.length; i++) {
				if (database_port.length > -1 || !"".equals(database_port[i].trim())) {
					//					conn_url = "jdbc:teradata://" + database_ip +
					// "/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=" + database_name
					//									+ ",lob_support=off,DBS_PORT=" + database_port;
					jdbcObj.put("jdbcPrefix", "jdbc:teradata://");
					jdbcObj.put("jdbcIp", "/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=");
					jdbcObj.put("jdbcBase", ",lob_support=off,DBS_PORT=");
					jdbcObj.put("jdbcPort", "");
				} else {
					//					conn_url = "jdbc:teradata://" + database_ip +
					// "/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=" + database_name
					//									+ ",lob_support=off";
					jdbcObj.put("jdbcPrefix", "jdbc:teradata://");
					jdbcObj.put("jdbcIp", "/TMODE=TERA,CHARSET=ASCII,CLIENT_CHARSET=cp936,DATABASE=");
					jdbcObj.put("jdbcBase", ",lob_support=off");
					jdbcObj.put("jdbcPort", "");
				}
			}

		} else if (DatabaseType.Informatic.getCode().equals(type)) {
			//
			//	jdbc:informix-sqli://127.0.0.1:1533/testDB:INFORMIXSERVER=myserver;user=testuser;password=testpassword"
			jdbcObj.put("jdbcPrefix", "jdbc:informix-sqli://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", ":INFORMIXSERVER=myserver");
		} else if (DatabaseType.H2.getCode().equals(type)) {
			//			jdbc:h2:tcp://localhost:port/databaseName
			jdbcObj.put("jdbcPrefix", "jdbc:h2:tcp://");
			jdbcObj.put("jdbcIp", ':');
			jdbcObj.put("jdbcPort", '/');
			jdbcObj.put("jdbcBase", "");
		} else {
			logger.info("目前不支持该数据，请联系管理员");
		}
		jdbcObj.put("jdbcDriver", getJDBCDriver(type));
		return jdbcObj;
	}

	/**
	 * 获取jdbc连接
	 *
	 * @param database_drive 驱动
	 * @param jdbc_url       jdbc连接url
	 * @param user_name      用户名
	 * @param database_pad   密码
	 * @return jdbc连接
	 */
	public static Connection getConnection(
		String database_drive, String jdbc_url, String user_name, String database_pad) {
		Connection conn;
		try {
			logger.info("开始连接 :" + jdbc_url);
			Class.forName(database_drive);
			conn = DriverManager.getConnection(jdbc_url, user_name, database_pad);
		} catch (Exception e) {
			logger.debug(e.getMessage());
			throw new AppSystemException("创建数据库连接失败", e);
		}
		return conn;
	}

	/**
	 * 获取connection
	 *
	 * @return
	 * @throws Exception
	 */
	public static void close(Connection conn) {

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 返回JDBCURL中的IP,端口,dataBase
	 *
	 * @param jdbcUrl
	 * @return
	 */
	public static List<String> getJDBCUrlInfo(String jdbcUrl, String jdbcType) {

		String[] regx = {
			"(\\d+\\.\\d+\\.\\d+\\.\\d+|\\w+):\\d+",
			".*:(\\d+)",
			"DBS_PORT=(\\d+)",
			"\\d+:\\d+:(.*)",
			";DatabaseName=(.*)",
			":\\d+/(\\w+)",
			"DATABASE=(\\w+)"
		};
		List<String> list = new ArrayList<>();
		if (DatabaseType.TeraData.getCode().equals(jdbcType)) {
			String teraDaraFromUrl = getTeraDaraFromUrl(jdbcUrl);
			list.add(teraDaraFromUrl);
		}
		Pattern pattern = null;
		Matcher m = null;
		for (int i = 0; i < regx.length; i++) {
			pattern = Pattern.compile(regx[i]); // 匹配的模式
			m = pattern.matcher(jdbcUrl);
			while (m.find()) {
				list.add(m.group(1));
				break;
			}
		}

		if (DatabaseType.TeraData.getCode().equals(jdbcType) && list.size() == 2) {
			list.add(1, "");
		}
		return list;
	}

	public static String getJDBCDriver(String type) {

		String jdbcDriver = "";
		if (DatabaseType.MYSQL.getCode().equals(type)) {
			jdbcDriver = "com.mysql.jdbc.Driver";
		} else if (DatabaseType.Oracle9i.getCode().equals(type)) {
			jdbcDriver = "oracle.jdbc.driver.OracleDriver";
		} else if (DatabaseType.Oracle10g.getCode().equals(type)) {
			jdbcDriver = "oracle.jdbc.OracleDriver";
		} else if (DatabaseType.SqlServer2000.getCode().equals(type)) {
			jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (DatabaseType.SqlServer2005.getCode().equals(type)) {
			jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (DatabaseType.DB2.getCode().equals(type)) {
			jdbcDriver = "com.ibm.db2.jcc.DB2Driver";
		} else if (DatabaseType.SybaseASE125.getCode().equals(type)) {
			jdbcDriver = "com.sybase.jdbc2.jdbc.SybDriver";
		} else if (DatabaseType.Informatic.getCode().equals(type)) {
			jdbcDriver = "com.informix.jdbc.IfxDriver";
		} else if (DatabaseType.H2.getCode().equals(type)) {
			jdbcDriver = "org.h2.Driver";
		} else if (DatabaseType.ApacheDerby.getCode().equals(type)) {
			jdbcDriver = "org.apache.derby.jdbc.EmbeddedDriver";
		} else if (DatabaseType.GBase.getCode().equals(type)) {
			jdbcDriver = "com.gbase.jdbc.Driver";
		} else if (DatabaseType.TeraData.getCode().equals(type)) {
			jdbcDriver = "com.teradata.jdbc.TeraDriver";
		} else {
			jdbcDriver = "org.postgresql.Driver";
		}
		return jdbcDriver;
	}

	/**
	 * 获取database
	 *
	 * @param database_ip
	 * @param database_port
	 * @param database_name
	 * @param user_name
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
  /*public static Database getDataBase(String database_drive, String database_ip, String database_port, String database_name, String user_name,
  				String database_pad, String type) throws ClassNotFoundException, SQLException {

  	String conn_url = getConn_url(type, database_ip, database_port, database_name);
  	Connection conn = getConnection(database_drive, database_ip, database_port, database_name, user_name, database_pad, type);
  	conn.setAutoCommit(false);
  	Platform platform = PlatformFactory.createNewPlatformInstance(database_drive, conn_url);
  	String[] _defaultTableTypes = { "TABLE" };
  	Database db = platform.readModelFromDatabase(conn, user_name, database_name, user_name, _defaultTableTypes);
  	return db;
  }*/
	public static String getDataBaseFile(
		String database_ip, String database_port, String database_name, String user_name) {

		return Math.abs((database_name + database_ip + database_port + user_name).hashCode()) + ".xml";
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getColumnByTable(String filename, String tablename) {
		List<Map<String, String>> columnList = new ArrayList<>();
		Map<?, ?> retMap = loadStoreInfo(filename);
		Map<String, Element> mapCol = (Map<String, Element>) retMap.get("mapCol");
		Element colList = mapCol.get(tablename);
		// 当xml树中没有节点的时候跳出
		if (null == colList) {
			return columnList;
		}
		List<?> acctTypes = XMLUtil.getChildElements(colList, "column");
		for (Object object : acctTypes) {
			Element type = (Element) object;
			Map<String, String> hashMap = new HashMap<>();
			hashMap.put("column_name", type.getAttribute("column_name"));
			hashMap.put("is_primary_key", type.getAttribute("is_primary_key"));
			hashMap.put("column_ch_name", type.getAttribute("column_ch_name"));
			hashMap.put("column_type", type.getAttribute("column_type"));
			hashMap.put("column_remark", type.getAttribute("column_remark"));
			hashMap.put("is_get", type.getAttribute("is_get"));
			hashMap.put("is_alive", type.getAttribute("is_alive"));
			hashMap.put("is_new", type.getAttribute("is_new"));
			columnList.add(hashMap);
		}
		return columnList;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Map<String, String>>> getColumnByXml(String xmlName) {
		Map<String, List<Map<String, String>>> columnMap = new HashMap<>();
		Map<?, ?> retMap = loadStoreInfo(xmlName);
		Map<String, Element> mapCol = (Map<String, Element>) retMap.get("mapCol");
		// 当xml树中没有节点的时候跳出
		if (null == mapCol) {
			return columnMap;
		}
		for (String table_name : mapCol.keySet()) {
			List<Map<String, String>> columnList = new ArrayList<>();
			List<?> acctTypes = XMLUtil.getChildElements(mapCol.get(table_name), "column");
			for (Object object : acctTypes) {
				Element type = (Element) object;
				Map<String, String> hashMap = new HashMap<>();
				hashMap.put("column_name", type.getAttribute("column_name"));
				hashMap.put("is_primary_key", type.getAttribute("is_primary_key"));
				hashMap.put("column_ch_name", type.getAttribute("column_ch_name"));
				hashMap.put("column_type", type.getAttribute("column_type"));
				hashMap.put("column_remark", type.getAttribute("column_remark"));
				hashMap.put("is_get", type.getAttribute("is_get"));
				hashMap.put("is_alive", type.getAttribute("is_alive"));
				hashMap.put("is_new", type.getAttribute("is_new"));
				columnList.add(hashMap);
			}
			columnMap.put(table_name, columnList);
		}
		return columnMap;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getStorageByXml(String xmlName) {
		List<Map<String, Object>> allStorageList = new ArrayList<>();
		Map<?, ?> retMap = loadStoreInfo(xmlName);
		Map<String, Element> mapCol = (Map<String, Element>) retMap.get("mapCol");
		Map<String, String> tableNameMap = (Map<String, String>) retMap.get("tableNameMap");
		// 当xml树中没有节点的时候跳出
		if (null == mapCol) {
			return allStorageList;
		}
		for (String table_name : mapCol.keySet()) {
			Map<String, Object> storageMap = new HashMap<>();
			List<Map<String, String>> storageList = new ArrayList<>();
			List<?> acctTypes = XMLUtil.getChildElements(mapCol.get(table_name), "storage");
			for (Object object : acctTypes) {
				Element type = (Element) object;
				Map<String, String> hashMap = new HashMap<>();
				hashMap.put("dbfile_format", type.getAttribute("dbfile_format"));
				hashMap.put("is_header", type.getAttribute("is_header"));
				hashMap.put("row_separator", type.getAttribute("row_separator"));
				hashMap.put("database_separatorr", type.getAttribute("database_separatorr"));
				hashMap.put("plane_url", type.getAttribute("plane_url"));
				hashMap.put("database_code", type.getAttribute("database_code"));
				storageList.add(hashMap);
			}
			storageMap.put("storage", storageList);
			storageMap.put("table_name", table_name);
			storageMap.put("table_ch_name", tableNameMap.get(table_name));
			allStorageList.add(storageMap);
		}
		return allStorageList;
	}

	/**
	 * 获取表信息
	 *
	 * @param filename 文件名
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getTable(String filename, String searchTableName) {
		//读取xml的表数据信息
		Map<String, Object> retMap = loadStoreInfo(filename);
		//获取的表名称集合
		List<Object> xmlTableNameList = (List<Object>) retMap.get("tableNameList");
		/*
		 *如果搜索的表名存在,将表名进行分割,然后从xml的结果集中删除掉和搜索表名不一致的表
		 */
		if (StringUtil.isNotBlank(searchTableName)) {
			//处理的表数据信息集合
			List<Object> processTableList = new ArrayList<>();
			List<String> searchTableNames = StringUtil.split(searchTableName, "|");
			for (String searchTable : searchTableNames) {
				for (Object xmlTable : xmlTableNameList) {
					if (xmlTable.toString().contains(searchTable)) {
						//防止有重复的表名出现
						if (!processTableList.contains(xmlTable)) {
							processTableList.add(xmlTable);
						}
					}
				}
			}
			return processTableList;
		}
		return xmlTableNameList;
	}

	private static Map<String, Object> loadStoreInfo(String filename) {
		List<?> tableList = getXmlToList(filename);
		Map<String, Element> mapCol = new HashMap<String, Element>();
		List<String> tableNameList = new ArrayList<>();
		Map<String, String> tableNameMap = new HashMap<>();
		for (Object element : tableList) {
			Element table = (Element) element;
			String tableName = table.getAttribute("table_name");
			String table_cn_name = table.getAttribute("table_ch_name");
			mapCol.put(tableName, table);
			tableNameList.add(tableName);
			tableNameMap.put(tableName, table_cn_name);
		}
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("tableNameMap", tableNameMap);
		retMap.put("tableNameList", tableNameList);
		retMap.put("mapCol", mapCol);
		return retMap;
	}

	private static List<Object> loadStoreInfoXML(String filename) {
		List<?> tableList = getXmlToList(filename);
		List<Object> tables = new ArrayList<>();
		for (Object element : tableList) {
			Element table = (Element) element;
			Map<String, String> map = new HashMap<>();
			map.put("table_name", table.getAttribute("table_name"));
			map.put("table_ch_name", table.getAttribute("table_ch_name"));
			map.put("unload_type", table.getAttribute("unload_type"));
			tables.add(map);
		}
		return tables;
	}

	private static List<?> getXmlToList(String filename) {
		try {
			File f = new File(filename);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new BusinessException("创建文档管理器失败" + e.getMessage());
			}
			Document doc = db.parse(f);
			Element root = (Element) doc.getElementsByTagName("database").item(0);
			return XMLUtil.getChildElements(root, "table");
		} catch (Exception ex) {
			throw new BusinessException("加载信息异常！ " + ex.getMessage());
		}
	}

	public static List<Object> getTableToXML(String filename) {

		return loadStoreInfoXML(filename);
	}

	public static String getTeraDaraFromUrl(String jdbcUrl) {

		String cleanURI = jdbcUrl.substring(5);
		URI uri = URI.create(cleanURI);
		return uri.getHost();
	}

	public static JSONObject getTableToXML2(String filename) {

		return loadStoreInfoXML2(filename);
	}

	private static JSONObject loadStoreInfoXML2(String filename) {

		File f = new File(filename);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new BusinessException("创建文档管理器失败");
			}
			Document doc = db.parse(f);
			Element root = (Element) doc.getElementsByTagName("database").item(0);
			JSONObject jsonTable = new JSONObject();
			JSONArray jsonArrayTable = new JSONArray();
			List<?> tableList = XMLUtil.getChildElements(root, "table");
			for (int b = 0; b < tableList.size(); b++) {
				Element table = (Element) tableList.get(b);
				JSONObject tableJson = new JSONObject();
				tableJson.put("table_name", table.getAttribute("table_name"));
				tableJson.put("table_ch_name", table.getAttribute("table_ch_name"));
				tableJson.put("updatetype", table.getAttribute("unload_type"));
				List<?> columnList = XMLUtil.getChildElements(table, "column");
				JSONArray columnJsonArray = new JSONArray();
				for (int c = 0; c < columnList.size(); c++) {
					Element column = (Element) columnList.get(c);
					JSONObject columnJson = new JSONObject();
					String columnname = column.getAttribute("name");
					String columntype = column.getAttribute("column_type");
					String is_key = column.getAttribute("is_key");
					String is_hbase = column.getAttribute("is_hbase");
					String is_rowkey = column.getAttribute("is_rowkey");
					String is_solr = column.getAttribute("is_solr");
					String is_operate = column.getAttribute("is_operate");
					String columnposition = column.getAttribute("columnposition");
					columnJson.put("columnname", columnname);
					columnJson.put("columntype", columntype);
					columnJson.put("is_key", is_key);
					columnJson.put("is_hbase", is_hbase);
					columnJson.put("is_rowkey", is_rowkey);
					columnJson.put("is_solr", is_solr);
					columnJson.put("is_operate", is_operate);
					columnJson.put("columnposition", columnposition);
					columnJsonArray.add(columnJson);
				}
				tableJson.put("column", columnJsonArray);
				JSONObject handleTypejson = new JSONObject();
				Element handleType = XMLUtil.getChildElement(table, "handletype");
				handleTypejson.put("insert", handleType.getAttribute("insert"));
				handleTypejson.put("update", handleType.getAttribute("update"));
				handleTypejson.put("delete", handleType.getAttribute("delete"));
				tableJson.put("handletype", handleTypejson);
				jsonArrayTable.add(tableJson);
			}
			jsonTable.put("tablename", jsonArrayTable);
			return jsonTable;
		} catch (SAXException e) {
			throw new BusinessException("解析文件异常," + e.getMessage());
		} catch (IOException e) {
			throw new BusinessException("加载信息异常," + e.getMessage());
		}
	}

	@Method(desc = "没有数据字典时读取数据文件", logicStep = "")
	@Param(desc = "文件路径", name = "dbpath", range = "无限制")
	@Param(desc = "数据日期", name = "data_date", range = "无限制")
	@Param(desc = "文件后缀名", name = "filesuffix", range = "无限制")
	@Return(desc = "半结构化采集查看表时，对于不提供数据字典的情况，解析返回表名", range = "无限制")
	public static JSONObject getTableFromJson(String dbpath, String data_date, String filesuffix) {
		if (!dbpath.endsWith(File.separator)) {
			dbpath += File.separator;
		}
		JSONObject resultobject = new JSONObject();
		File dbpathfile = new File(dbpath);
		File[] dbpathfilelist = Objects.requireNonNull(dbpathfile.listFiles());
		List<String> dbpathfilelistname = new ArrayList<>();
		for (File everyfile : dbpathfilelist) {
			dbpathfilelistname.add(everyfile.getName());
		}
		if (!dbpathfilelistname.contains(data_date)) {
			resultobject.put("ErrorMessage", "文件路径不存在，请检查:" + dbpath + " 下是否存在 " + data_date + " 文件");
			throw new BusinessException(resultobject.getString("ErrorMessage"));
		}
		List<String> tablenamelsit = new ArrayList<String>();
		String filepath = dbpath + data_date;
		File targetFileDirectory = new File(filepath);
		if (!targetFileDirectory.isDirectory()) {
			resultobject.put("ErrorMessage", "文件路径：" + filepath + " 不是一个文件夹，请检查");
			throw new BusinessException(resultobject.getString("ErrorMessage"));
		}
		List<File> files =
			new ArrayList<File>(Arrays.asList(Objects.requireNonNull(new File(filepath).listFiles())));
		if (files.size() == 0) {
			resultobject.put("ErrorMessage", "数据路径的日期目录：" + filepath + " 下没有文件");
			throw new BusinessException(resultobject.getString("ErrorMessage"));
		}
		JSONArray jsonarray = new JSONArray();
		for (File everyfile : files) {
			String filename = everyfile.getName();
			if (filename.endsWith("_" + data_date + "." + filesuffix)) {
				String tablename = filename.split("_" + data_date + "." + filesuffix)[0];
				if (!tablenamelsit.contains(tablename)) {
					try {
						BufferedReader reader = new BufferedReader(new FileReader(everyfile));
						String readLine = reader.readLine();
						tablenamelsit.add(tablename);
						JSONObject everyobject = new JSONObject();
						everyobject.put("table_name", tablename);
						everyobject.put("table_ch_name", tablename);
						everyobject.put("everyline", readLine);
						jsonarray.add(everyobject);
					} catch (Exception e) {
						throw new BusinessException("没有数据字典时读取数据文件失败！");
					}
				}
			}
		}
		if (jsonarray.size() == 0) {
			resultobject.put("ErrorMessage", "数据路径的日期目录下没有后缀名为：" + filesuffix + "的文件");
			throw new BusinessException(resultobject.getString("ErrorMessage"));
		}
		resultobject.put("tablename", jsonarray);
		return resultobject;
	}

}
