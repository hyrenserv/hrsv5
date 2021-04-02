package hrds.h.biz.realloader;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import hrds.commons.codes.StoreLayerAdded;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.entity.Datatable_field_info;
import hrds.commons.exception.AppSystemException;
import hrds.h.biz.config.MarketConf;
import hrds.h.biz.config.MarketConfUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.annotation.meta.field;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static hrds.commons.utils.Constant.*;

/**
 * 一些共用的方法
 *
 * @Author: Mick Yuan
 * @Date:
 * @Since jdk1.8
 */
public class Utils {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * 获取字段名，字段类型组合 大概像这样： a string, b double, c long 这样的字符串
	 *
	 * @param conf       集市配置类实体
	 * @param isDatabase 是否是关系型数据库
	 * @return 字段名，字段类型组合
	 */
	public static String buildCreateTableColumnTypes(MarketConf conf, boolean isDatabase) {

		List<String> additionalAttrs = conf.getAddAttrColMap().get(StoreLayerAdded.ZhuJian.getCode());
		final StringBuilder columnTypes = new StringBuilder(300);
		List<Datatable_field_info> datatableFields = conf.getDatatableFields();
		for (int i = 0; i < datatableFields.size(); i++) {
			Datatable_field_info field = datatableFields.get(i);
			String fieldName = field.getField_en_name();
			columnTypes.append("\t")
				.append(fieldName)
				.append(" ")
				.append(field.getField_type());

			String fieldLength = field.getField_length();
			//写了精度，则添加精度
			if (StringUtil.isNotBlank(fieldLength)) {
				columnTypes
					.append("(").append(fieldLength).append(")");
			}
			//如果选择了主键，则添加主键
			if (isDatabase && additionalAttrs != null && additionalAttrs.contains(fieldName)) {
				columnTypes.append(" primary key");
			}
			if (i != datatableFields.size() - 1) {
				columnTypes.append(",");
			}
//			columnTypes.append(" -- " + field.getField_cn_name());
			columnTypes.append(System.lineSeparator());
		}

		//如果是database类型 则类型为定长char类型，否则为string类型（默认）
		if (isDatabase) {
			String str = MarketConfUtils.DEFAULT_STRING_TYPE;
			String s = columnTypes.toString();
			if (conf.isIncrement()) {
				s = StringUtil.replaceLast(s, str, "char(32)");
				s = StringUtil.replaceLast(s, str, "char(8)");
			}
			s = StringUtil.replaceLast(s, str, "char(8)");

			//多集市输入会多个字段
			if (conf.isMultipleInput()) {
				s = StringUtil.replaceLast(s, str, "char(18)");
			}
			if (conf.isGroup()) {
				s = StringUtil.replaceLast(s, str, "char(32)");
			}
			return s;
		}
		return columnTypes.toString();
	}

	/**
	 * 所有字段，以逗号隔开
	 *
	 * @return 所有字段，以逗号连接的字符串
	 */
	static String columns(List<Datatable_field_info> fields) {
		return fields
			.stream()
			.map(Datatable_field_info::getField_en_name)
			.collect(Collectors.joining(","));
	}

	/**
	 * 恢复关系型数据库的数据到上次跑批结果 z
	 *
	 * @param db DatabaseWrapper
	 */
	static void restoreDatabaseData(DatabaseWrapper db, String tableName,
		String etlDate, String datatableId, boolean isMultipleInput, boolean isIncrement) {
		if (isMultipleInput) {
			db.execute(String.format("DELETE FROM %s WHERE %s = '%s' AND %s = '%s'",
				tableName, SDATENAME, etlDate, TABLE_ID_NAME, datatableId));
			if (isIncrement) {
				db.execute(String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s' AND %s = '%s'",
					tableName, EDATENAME, MAXDATE, EDATENAME, etlDate, TABLE_ID_NAME, datatableId));
			}
		} else {
			db.execute(String.format("DELETE FROM %s WHERE %s = '%s'",
				tableName, SDATENAME, etlDate));
			if (isIncrement) {
				db.execute(String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s'",
					tableName, EDATENAME, MAXDATE, EDATENAME, etlDate));
			}
		}
	}

	/**
	 * 恢复关系型数据库的数据到上次跑批结果
	 *
	 * @param db DatabaseWrapper
	 */
	static boolean hasTodayData(DatabaseWrapper db, String tableName,
		String etlDate, String datatableId, boolean isMultipleInput, boolean isIncrement) throws SQLException {

		if (isMultipleInput) {
			ResultSet resultSet = db.queryPagedGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'",
				tableName, SDATENAME, etlDate, TABLE_ID_NAME, datatableId), 1, 1, false);
			if (resultSet.next()) {
				return true;
			}
			if (isIncrement) {
				resultSet = db.queryPagedGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'",
					tableName, EDATENAME, MAXDATE, TABLE_ID_NAME, datatableId), 1, 1, false);
				if (resultSet.next()) {
					return true;
				}
			}
		} else {
			ResultSet resultSet = db.queryPagedGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s'",
				tableName, SDATENAME, etlDate), 1, 2, false);
			if (resultSet.next()) {
				return true;
			}
			if (isIncrement) {
				resultSet = db.queryPagedGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s'",
					tableName, EDATENAME, MAXDATE), 1, 2, false);
				if (resultSet.next()) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean hasTodayDataLimit(DatabaseWrapper db, String tableName,
		String etlDate, String datatableId, boolean isMultipleInput, boolean isIncrement) throws SQLException {

		if (isMultipleInput) {
			ResultSet resultSet = db
				.queryGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s' LIMIT 1",
					tableName, SDATENAME, etlDate, TABLE_ID_NAME, datatableId));
			if (resultSet.next()) {
				return true;
			}
			if (isIncrement) {
				resultSet = db.queryGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s' LIMIT 1",
					tableName, EDATENAME, MAXDATE, TABLE_ID_NAME, datatableId));
				if (resultSet.next()) {
					return true;
				}
			}
		} else {
			ResultSet resultSet = db.queryGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' LIMIT 1",
				tableName, SDATENAME, etlDate));
			if (resultSet.next()) {
				return true;
			}
			db.execute(String.format("SELECT * FROM %s WHERE %s = '%s'",
				tableName, SDATENAME, etlDate));
			if (isIncrement) {
				resultSet = db.queryGetResultSet(String.format("SELECT * FROM %s WHERE %s = '%s' LIMIT 1",
					tableName, EDATENAME, MAXDATE));
				if (resultSet.next()) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * 创建表 如果表存在就报错
	 */
	static void createTable(DatabaseWrapper db, String tableName, String createTableColumnTypes) {
		String createSql;
		if (db.getDbtype() == Dbtype.TERADATA) {
			createSql = "CREATE MULTISET TABLE ";
		} else {
			createSql = "CREATE TABLE ";
		}
		logger.info("============================开始替换作业,创建临时表======================");
		createSql += tableName + " (" + createTableColumnTypes + ")";
		db.ExecDDL(createSql);
	}

	/**
	 * 创建表 如果表存在就删除掉
	 */
	static void forceCreateTable(DatabaseWrapper db, String tableName, String createTableColumnTypes) {

		if (db.isExistTable(tableName)) {
			logger.info("============================开始替换作业,删除临时表,在重建======================");
			db.ExecDDL("DROP TABLE " + tableName);
		}
		createTable(db, tableName, createTableColumnTypes);
	}

	/**
	 * 创建表 如果表不存在就创建
	 */
	static void softCreateTable(DatabaseWrapper db, String tableName, String createTableColumnTypes) {
		if (!db.isExistTable(tableName)) {
			createTable(db, tableName, createTableColumnTypes);
		}
	}

	static Optional<List<String>> getFinalWorkSqls(String sqls) {
		if (StringUtil.isBlank(sqls)) {
			logger.info("无后置作业需要执行！");
			return Optional.empty();
		}
		List<String> sqlList = Arrays.stream(sqls.split(";;"))
			.filter(StringUtil::isNotBlank)
			.collect(Collectors.toList());
		if (sqlList.isEmpty()) {
			logger.info("无后置作业需要执行！");
			return Optional.empty();
		}
		return Optional.of(sqlList);
	}

	static Optional<List<String>> getPreWorkSqls(String sqls) {
		if (StringUtil.isBlank(sqls)) {
			logger.info("无前置作业需要执行！");
			return Optional.empty();
		}
		List<String> sqlList = Arrays.stream(sqls.split(";;"))
			.filter(StringUtil::isNotBlank)
			.collect(Collectors.toList());
		if (sqlList.isEmpty()) {
			logger.info("无前置作业需要执行！");
			return Optional.empty();
		}
		return Optional.of(sqlList);
	}

	/**
	 * 后置作业为多个sql 不支持数据库级别事务回滚
	 *
	 * @param sqlsJoined 多个sql，以 ;; 隔开
	 * @param db         db对象
	 */
	static void finalWorkWithoutTrans(String sqlsJoined, DatabaseWrapper db) {
		//把sql字符串转换成sql的list
		Optional<List<String>> OptionSqls = getFinalWorkSqls(sqlsJoined);
		if (OptionSqls.isPresent()) {
			for (String sql : OptionSqls.get()) {
				db.execute(sql);
			}
		}
	}

	/**
	 * 后置作业为多个sql 不支持数据库级别事务回滚
	 *
	 * @param sqlsJoined 多个sql，以 ;; 隔开
	 * @param db         db对象
	 */
	static void preWorkWithoutTrans(String sqlsJoined, DatabaseWrapper db) {
		//把sql字符串转换成sql的list
		Optional<List<String>> OptionSqls = getPreWorkSqls(sqlsJoined);
		if (OptionSqls.isPresent()) {
			for (String sql : OptionSqls.get()) {
				db.execute(sql);
			}
		}
	}

	/**
	 * 后置作业为多个sql 支持数据库级别事务回滚
	 *
	 * @param sqlsJoined 多个sql，以 ;; 隔开
	 * @param dbConf     db配置map
	 */
	static void finalWorkWithinTrans(String sqlsJoined, Map<String, String> dbConf) {
		//把sql字符串转换成sql的list
		Optional<List<String>> OptionSqls = getFinalWorkSqls(sqlsJoined);
		if (OptionSqls.isPresent()) {
			DatabaseWrapper db = null;
			try {
				db = ConnectionTool.getDBWrapper(dbConf);
				db.beginTrans();
				for (String sql : OptionSqls.get()) {
					db.execute(sql);
				}
				db.commit();
			} catch (Exception e) {
				if (db != null) {
					db.rollback();
				}
				throw e;
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}

	}

	static void softDropTable(DatabaseWrapper db, String tableName) {
		try {
			if (db.isExistTable(tableName)) {
				db.ExecDDL("DROP TABLE " + tableName);
			}
		} catch (Exception e) {
			logger.warn("删除临时表 " + tableName + " 失败");
		}
	}

	static void dropTable(DatabaseWrapper db, String tableName) {
		try {
			if (db.isExistTable(tableName)) {
				logger.info("============================开始替换作业,删除最终表======================");
				db.ExecDDL("DROP TABLE " + tableName);
			}
		} catch (Exception e) {
			throw new AppSystemException("删除表失败：" + tableName, e);
		}
	}

	/**
	 * @param db
	 * @param srcTableName  : 临时表名称
	 * @param destTableName : 最终表名称
	 */
	static void renameTable(DatabaseWrapper db, String srcTableName, String destTableName) {
		String renameSql;
		//检查临时表是否存在
		if (!db.isExistTable(srcTableName)) {
			throw new AppSystemException("表" + srcTableName + "不存在，无法重命名成" + destTableName);
		}

		logger.info("==================================表: " + srcTableName + " 是否存在: " + db.isExistTable(srcTableName));
		//检查最终表是否存在
		if (db.isExistTable(destTableName)) {
			throw new AppSystemException("表" + destTableName + "已存在，无法重命名成" + destTableName);
		}

		if (db.getDbtype() == Dbtype.DB2V1 || db.getDbtype() == Dbtype.DB2V2) {
			renameSql = "RENAME " + srcTableName + " TO " + destTableName;
		} else if (db.getDbtype() == Dbtype.TERADATA) {
			renameSql = "RENAME TABLE " + srcTableName + " TO " + destTableName;
		} else if (db.getDbtype() == Dbtype.KINGBASE) {
			renameSql = "ALTER TABLE " + db.getDatabaseName() + "." + srcTableName + " RENAME TO " + destTableName;
		} else {
			renameSql = "ALTER TABLE " + srcTableName + " RENAME TO " + destTableName;
		}

		db.ExecDDL(renameSql);
	}

	/**
	 * 获取 hive 表大小统计信息，触发优化 broadcast 等优化方式
	 *
	 * @param db
	 * @param tableName
	 */
	static void computeStatistics(DatabaseWrapper db, String tableName) {
		db.execute("ANALYZE TABLE " + tableName + " COMPUTE STATISTICS NOSCAN");
	}

	public static void softCreateIndex(DatabaseWrapper db, MarketConf conf, String tableName) {
		//oracle数据库创建索引
		if (Dbtype.ORACLE == db.getDbtype()) {
			List<String> additionalAttrs = conf.getAddAttrColMap().get(StoreLayerAdded.SuoYinLie.getCode());
			if (additionalAttrs != null && additionalAttrs.size() > 0) {
				for (int i = 0; i < additionalAttrs.size(); i++) {
					String indexName = "idx_" + tableName + "_" + i;
					if (isExistIndex(indexName, db)) {
						db.ExecDDL("drop index " + indexName);
					}
					db.ExecDDL("create index  " + indexName + " on " +
						tableName + "(" + additionalAttrs.get(i) + ")");
				}
			}
		}
	}

	/**
	 * 拼接Oracle判断索引是否存在，存在则返回true
	 */
	public static boolean isExistIndex(String indexName, DatabaseWrapper db) {
		try {
			ResultSet resultSet = db.queryGetResultSet("SELECT * FROM user_objects where  lower(object_name) = '"
				+ indexName.toLowerCase() + "'");
			return resultSet.next();
		} catch (SQLException throwables) {
			throw new AppSystemException("查找是否存在索引报错");
		}
	}

	public static void main(String[] args) {
		try (DatabaseWrapper db = new DatabaseWrapper.Builder().dbname("kingbase").create()) {
			renameTable(db, "TEST2_TABLE", "TEST2_TABLE2222");
//			renameTable(db, "TEST2_TABLE2222", "TEST2_TABLE");
//			Result result = SqlOperator.queryResult(db, "SELECT * FROM TESTDB1.TEST2_TABLE");
//			System.out.println(result.toJSON());
		}
	}
}
