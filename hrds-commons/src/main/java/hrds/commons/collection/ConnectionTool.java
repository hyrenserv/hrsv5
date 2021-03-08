package hrds.commons.collection;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.ConnWay;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.Store_type;
import hrds.commons.collection.bean.DbConfBean;
import hrds.commons.entity.Data_store_layer;
import hrds.commons.entity.Data_store_layer_attr;
import hrds.commons.entity.Database_set;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.StorageTypeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "数据库直连采集获取数据库连接", author = "WangZhengcheng", createdate = "2019/10/28 14:26")
public class ConnectionTool {
	//打印日志
	private static Logger logger = LogManager.getLogger();

	@Method(desc = "根据数据库配置信息获取数据库连接", logicStep = "" +
			"1、将SourceDataConfBean对象中的内容封装到dbInfo中" +
			"2、获取数据库类型" +
			"3、根据数据库类型获取对应数据库的数据库连接")
	@Param(name = "dbConfigBean", desc = "该对象封装了海云应用服务端发过来的数据库采集连接数据库的信息",
			range = "DBConfigBean类型对象")
	@Return(desc = "项目中常用的DatabaseWrapper对象", range = "DatabaseWrapper类型对象")
	public static DatabaseWrapper getDBWrapper(DbConfBean dbConfBean) {
		return getDBWrapper(dbConfBean.getDatabase_drive(), dbConfBean.getJdbc_url(), dbConfBean.getUser_name(),
				dbConfBean.getDatabase_pad(), dbConfBean.getDatabase_type(), dbConfBean.getDatabase_name());
	}

	public static DatabaseWrapper getDBWrapper(String database_drive, String jdbc_url, String user_name,
											   String database_pad, String database_type) {
		return getDBWrapper(database_drive, jdbc_url, user_name, database_pad, database_type, 0);
	}

	public static DatabaseWrapper getDBWrapper(String database_drive, String jdbc_url, String user_name,
											   String database_pad, String database_type, String database_name) {
		return getDBWrapper(database_drive, jdbc_url, user_name, database_pad, database_type, database_name, 0);
	}

	public static DatabaseWrapper getDBWrapper(String database_drive, String jdbc_url, String user_name,
											   String database_pad, String database_type, int fetch_size) {
		return getDBWrapper(database_drive, jdbc_url, user_name, database_pad, database_type,
				"", fetch_size);
	}

	public static DatabaseWrapper getDBWrapper(String database_drive, String jdbc_url, String user_name,
											   String database_pad, String database_type, String database_name, int fetch_size) {
		Map<String, String> dbConfig = new HashMap<>();
		dbConfig.put(StorageTypeKey.database_driver, database_drive);
		dbConfig.put(StorageTypeKey.jdbc_url, jdbc_url);
		dbConfig.put(StorageTypeKey.user_name, user_name);
		dbConfig.put(StorageTypeKey.database_pwd, database_pad);
		dbConfig.put(StorageTypeKey.database_type, database_type);
		dbConfig.put(StorageTypeKey.database_name, database_name);
		return getDBWrapper(dbConfig, fetch_size);
	}

	/**
	 * 直接通过数据库查询出来的List数据
	 */
	public static DatabaseWrapper getDBWrapper(List<Map<String, Object>> dbConfig) {
		return getDBWrapper(getLayerMap(dbConfig));
	}

	@Method(desc = "根据存储层配置Map信息获取DbConfBean", logicStep = "根据存储层配置Map信息获取DbConfBean")
	public static DbConfBean getDbConfBean(DatabaseWrapper db, long dsl_id) {
		//获取存储层配置Map信息
		Map<String, String> dbConfigMap = getLayerMap(getLayerList(db, dsl_id));
		//根据存储层配置Map信息设置 DbConfBean
		DbConfBean dbConfBean = new DbConfBean();
		dbConfBean.setDatabase_drive(dbConfigMap.get(StorageTypeKey.database_driver));
		dbConfBean.setJdbc_url(dbConfigMap.get(StorageTypeKey.jdbc_url));
		dbConfBean.setUser_name(dbConfigMap.get(StorageTypeKey.user_name));
		dbConfBean.setDatabase_pad(dbConfigMap.get(StorageTypeKey.database_pwd));
		if (Store_type.HIVE.getCode().equals(dbConfigMap.get("store_type"))
				|| Store_type.CARBONDATA.getCode().equals(dbConfigMap.get("store_type"))) {
			dbConfBean.setDatabase_type(DatabaseType.Hive.getCode());
		} else if (Store_type.HBASE.getCode().equals(dbConfigMap.get("store_type"))) {
			if ("hive".equals(dbConfigMap.get(StorageTypeKey.increment_engine))) {
				dbConfBean.setDatabase_type(DatabaseType.Hive.getCode());
			} else if ("phoenix".equals(dbConfigMap.get(StorageTypeKey.increment_engine))) {
				//TODO
				throw new AppSystemException("暂时没支持");
			} else {
				throw new AppSystemException("hbase的增量引擎不能为空");
			}
		} else {
			dbConfBean.setDatabase_type(dbConfigMap.get(StorageTypeKey.database_type));
		}
		dbConfBean.setDatabase_name(dbConfigMap.get(StorageTypeKey.database_name));
		return dbConfBean;
	}

	public static DatabaseWrapper getDBWrapper(Map<String, String> dbConfig) {
		return getDBWrapper(dbConfig, 0);
	}

	public static DatabaseWrapper getDBWrapper(Map<String, String> dbConfig, int fetch_size) {
		logger.info("获取到连接的配置为：===============================" + dbConfig.toString());
		//1、将SourceDataConfBean对象中的内容封装到dbInfo中
		DbinfosConf.Dbinfo dbInfo = new DbinfosConf.Dbinfo();
		dbInfo.setName(DbinfosConf.DEFAULT_DBNAME);
		dbInfo.setDriver(dbConfig.get(StorageTypeKey.database_driver));
		dbInfo.setUrl(dbConfig.get(StorageTypeKey.jdbc_url));
		dbInfo.setUsername(dbConfig.get(StorageTypeKey.user_name));
		dbInfo.setPassword(dbConfig.get(StorageTypeKey.database_pwd));
		dbInfo.setWay(ConnWay.JDBC);
		//2、获取数据库类型
		Dbtype dbType = getDbType(dbConfig.get(StorageTypeKey.database_type));
		if (dbType == Dbtype.HIVE) {
			dbInfo.setAutoCommit(false);
		}
		if (fetch_size != 0) {
			dbInfo.setFetch_size(fetch_size);
			if (dbType == Dbtype.POSTGRESQL) {
				dbInfo.setAutoCommit(false);
			}
		}
		if (!StringUtil.isEmpty(dbConfig.get(StorageTypeKey.database_name))) {
			dbInfo.setDataBaseName(dbConfig.get(StorageTypeKey.database_name));
		}
		dbInfo.setDbtype(dbType);
		dbInfo.setShow_conn_time(true);
		dbInfo.setShow_sql(true);
		//打印创建连接的参数
		logger.info("创建连接的参数为：==============" + dbInfo.toString());
		//3、根据数据库类型获取对应数据库的数据库连接
		DatabaseWrapper db = new DatabaseWrapper.Builder().dbconf(dbInfo).create();
		if (db.getDbtype() == Dbtype.TERADATA && db.getDatabaseName() != null) {
			db.execute("DATABASE " + db.getDatabaseName().toUpperCase());
		}
		if (db.getDbtype() == Dbtype.HIVE && db.getDatabaseName() != null) {
			db.execute("USE " + db.getDatabaseName().toUpperCase());
		}
		return db;
	}

//	public static DatabaseWrapper getDBWrapper(Map<String, String> dbConfig, int fetch_size) {
//		//1、将SourceDataConfBean对象中的内容封装到dbInfo中
//		DbinfosConf.Dbinfo dbInfo = new DbinfosConf.Dbinfo();
//		if (dbConfig.get(StorageTypeKey.name) != null) {
//			dbInfo.setName(dbConfig.get(StorageTypeKey.name));
//		} else {
//			dbInfo.setName(DbinfosConf.DEFAULT_DBNAME);
//		}
//		dbInfo.setDriver(dbConfig.get(StorageTypeKey.database_driver));
//		dbInfo.setUrl(dbConfig.get(StorageTypeKey.jdbc_url));
//		dbInfo.setUsername(dbConfig.get(StorageTypeKey.user_name));
//		dbInfo.setPassword(dbConfig.get(StorageTypeKey.database_pwd));
//		if (dbConfig.get(StorageTypeKey.conn_way) == null || "jdbc".equalsIgnoreCase(dbConfig
//				.get(StorageTypeKey.conn_way))) {
//			dbInfo.setWay(ConnWay.JDBC);
//		} else if ("pool".equalsIgnoreCase(dbConfig.get(StorageTypeKey.conn_way))) {
//			dbInfo.setWay(ConnWay.POOL);
//		} else if ("JNDI".equalsIgnoreCase(dbConfig.get(StorageTypeKey.conn_way))) {
//			dbInfo.setWay(ConnWay.JNDI);
//		}
//		if (!StringUtil.isEmpty(dbConfig.get(StorageTypeKey.minPoolSize))) {
//			dbInfo.setMinPoolSize(Integer.parseInt(dbConfig.get(StorageTypeKey.minPoolSize)));
//		}
//		if (!StringUtil.isEmpty(dbConfig.get(StorageTypeKey.maxPoolSize))) {
//			dbInfo.setMaxPoolSize(Integer.parseInt(dbConfig.get(StorageTypeKey.maxPoolSize)));
//		}
//		if (!StringUtil.isEmpty(dbConfig.get(StorageTypeKey.fetch_size))) {
//			dbInfo.setFetch_size(Integer.parseInt(dbConfig.get(StorageTypeKey.fetch_size)));
//		}
//		//2、获取数据库类型
//		Dbtype dbType = getDbType(dbConfig.get(StorageTypeKey.database_type));
//		if (dbType == Dbtype.HIVE) {
//			dbInfo.setAutoCommit(false);
//		}
//		if (fetch_size != 0) {
//			dbInfo.setFetch_size(fetch_size);
//			if (dbType == Dbtype.POSTGRESQL) {
//				dbInfo.setAutoCommit(false);
//			}
//		}
//		if (!StringUtil.isEmpty(dbConfig.get(StorageTypeKey.database_name))) {
//			dbInfo.setDataBaseName(dbConfig.get(StorageTypeKey.database_name));
//		}
//		dbInfo.setDbtype(dbType);
//		dbInfo.setShow_conn_time(true);
//		dbInfo.setShow_sql(true);
//		//3、根据数据库类型获取对应数据库的数据库连接
//		logger.info("建立db连接的参数为：" + dbConfig.toString());
//		DatabaseWrapper db = new DatabaseWrapper.Builder().dbconf(dbInfo).create();
//		if (db.getDbtype() == Dbtype.TERADATA && db.getDatabaseName() != null) {
//			db.execute("DATABASE " + db.getDatabaseName().toUpperCase());
//		}
//		if (db.getDbtype() == Dbtype.HIVE && db.getDatabaseName() != null) {
//			db.execute("USE " + db.getDatabaseName().toUpperCase());
//		}
//		return db;
//	}

	@Method(desc = "根据数据库配置信息获取数据库连接", logicStep = "" +
			"1、将database_set对象中的内容封装到dbInfo中" +
			"2、获取数据库类型" +
			"3、根据数据库类型获取对应数据库的数据库连接")
	@Param(name = "database_set", desc = "该对象封装了海云应用服务端发过来的数据库采集连接数据库的信息",
			range = "database_set")
	@Return(desc = "项目中常用的DatabaseWrapper对象", range = "DatabaseWrapper类型对象")
	public static DatabaseWrapper getDBWrapper(Database_set database_set) {
		return getDBWrapper(database_set.getDatabase_drive(), database_set.getJdbc_url(), database_set.getUser_name(),
				database_set.getDatabase_pad(), database_set.getDatabase_type(), database_set.getDatabase_name());
	}

	@Method(desc = "根据定义的存储层id返回存储层连接", logicStep = "根据定义的存储层id返回存储层连接")
	public static DatabaseWrapper getDBWrapper(DatabaseWrapper db, long dsl_id) {
		//根据数据层获取存储层配置信息
		DbConfBean dbConfBean = getDbConfBean(db, dsl_id);
		//返回DBWrapper
		return getDBWrapper(dbConfBean.getDatabase_drive(), dbConfBean.getJdbc_url(), dbConfBean.getUser_name(),
				dbConfBean.getDatabase_pad(), dbConfBean.getDatabase_type(), dbConfBean.getDatabase_name());
	}

	@Method(desc = "使用数据库信息，返回一个存储层的信息，以list的方式返回存储层配置信息",
			logicStep = "返回一个存储层的信息")
	public static List<Map<String, Object>> getLayerList(DatabaseWrapper db, long dsl_id) {
		//根据存储层id获取存储层配置信息列表

		return SqlOperator.queryList(db, "select t1.*,store_type from " + Data_store_layer_attr.TableName + " t1 " +
				"left join " + Data_store_layer.TableName + " t2 on t1.dsl_id = t2.dsl_id where t1.dsl_id = ?", dsl_id);
	}

	@Method(desc = "使用数据库信息，返回一个存储层的信息，以map的方式返回，key为用户输入的key，val为val",
			logicStep = "返回一个存储层的信息")
	public static Map<String, String> getLayerMap(DatabaseWrapper db, long dsl_id) {
		//根据存储层id获取存储层配置信息列表,处理配置信息,以map的方式返回，key为用户输入的key，val为val
		return getLayerMap(getLayerList(db, dsl_id));
	}

	@Method(desc = "使用数据库信息，返回一个存储层的信息，以map的方式返回，key为用户输入的key，val为val",
			logicStep = "返回一个存储层的信息")
	private static Map<String, String> getLayerMap(List<Map<String, Object>> dbConfig) {
		//处理配置信息,以map的方式返回，key为用户输入的key，val为val
		Map<String, String> dbConfigMap = new HashMap<>();
		for (Map<String, Object> dbMap : dbConfig) {
			String key = dbMap.get("storage_property_key").toString();
			String val = dbMap.get("storage_property_val").toString();
			dbConfigMap.put(key, val);
		}
		dbConfigMap.put("store_type", dbConfig.get(0).get("store_type").toString());
		return dbConfigMap;
	}

	public static Dbtype getDbType(String database_type) {
		//获取数据库类型
		DatabaseType typeConstant = DatabaseType.ofEnumByCode(database_type);
		if (typeConstant == DatabaseType.MYSQL) {
			return Dbtype.MYSQL;
		} else if (typeConstant == DatabaseType.Oracle9i) {
			throw new AppSystemException("系统不支持Oracle9i及以下");
		} else if (typeConstant == DatabaseType.Oracle10g) {
			return Dbtype.ORACLE;
		} else if (typeConstant == DatabaseType.SqlServer2000) {
			return Dbtype.SQLSERVER;
		} else if (typeConstant == DatabaseType.SqlServer2005) {
			return Dbtype.SQLSERVER;
		} else if (typeConstant == DatabaseType.DB2) {
			//TODO 使用db2v1还是db2v2
			return Dbtype.DB2V1;
		} else if (typeConstant == DatabaseType.SybaseASE125) {
			return Dbtype.SYBASE;
		} else if (typeConstant == DatabaseType.Informatic) {
			//Informatic
			throw new AppSystemException("系统不支持该数据库类型");
		} else if (typeConstant == DatabaseType.H2) {
			//H2
			throw new AppSystemException("系统不支持该数据库类型");
		} else if (typeConstant == DatabaseType.ApacheDerby) {
			//ApacheDerBy
			throw new AppSystemException("系统不支持该数据库类型");
		} else if (typeConstant == DatabaseType.Postgresql) {
			return Dbtype.POSTGRESQL;
		} else if (typeConstant == DatabaseType.GBase) {
			return Dbtype.GBASE;
		} else if (typeConstant == DatabaseType.TeraData) {
			return Dbtype.TERADATA;
		} else if (typeConstant == DatabaseType.Hive) {
			return Dbtype.HIVE;
		} else if (typeConstant == DatabaseType.Odps) {
			return Dbtype.ODPS;
		} else if (typeConstant == DatabaseType.KingBase) {
			return Dbtype.KINGBASE;
		} else {
			throw new AppSystemException("系统不支持该数据库类型");
		}
	}
}
