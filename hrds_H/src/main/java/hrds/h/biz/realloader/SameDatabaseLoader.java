package hrds.h.biz.realloader;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.ProcessType;
import hrds.commons.codes.StoreLayerAdded;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.entity.Datatable_field_info;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import hrds.commons.utils.DruidParseQuerySql;
import hrds.commons.utils.PropertyParaValue;
import hrds.commons.utils.StorageTypeKey;
import hrds.h.biz.config.MarketConf;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static hrds.commons.utils.Constant.*;

/**
 * @Author: Mick Yuan
 * @Date:
 * @Since jdk1.8
 */
public class SameDatabaseLoader extends AbstractRealLoader {

	// TODO 注释
	private final boolean MARKET_INCREMENT_TMPTABLE_DELETE =
			PropertyParaValue.getBoolean("market.increment.tmptable.delete", true);
	private final DatabaseWrapper db;
	private final String sql;
	private final String columns;
	private final DatabaseType databaseType;
	private final String currentTableName;
	private final String validTableName;
	private final String invalidTableName;
	private final String createTableColumnTypes;

	SameDatabaseLoader(MarketConf conf) {
		super(conf);
		sql = conf.getCompleteSql();
		columns = Utils.columns(conf.getDatatableFields());
		createTableColumnTypes = Utils.buildCreateTableColumnTypes(conf, true);
		databaseType = DatabaseType.ofEnumByCode(tableLayerAttrs.get(StorageTypeKey.database_type));
		currentTableName = "cur_" + tableName;
		validTableName = "vid_" + tableName;
		invalidTableName = "ivd_" + tableName;

		db = ConnectionTool.getDBWrapper(tableLayerAttrs);
		//开启事务，在 this.rollback() 中进行回滚,在 this.finalWork() 中 commit()
		db.beginTrans();
	}

	@Override
	public void ensureRelation() {
		if (versionManager.isVersionExpire()) {
			if (!db.isExistTable(versionManager.getRenameTableName()) && db.isExistTable(tableName)) {
				Utils.renameTable(db, tableName, versionManager.getRenameTableName());
			}
		}
		Utils.softCreateTable(db, tableName, createTableColumnTypes);
		//创建索引
		Utils.softCreateIndex(db, conf, tableName);
	}

	@Override
	public void append() {
		insertData(tableName);
	}

	/**
	 * 1.创建临时表
	 * 2.把数据导入到临时表
	 * 3.删除最终表
	 * 4.把临时表重命名成最终表
	 */
	@Override
	public void replace() {
		String replaceTempTable = tableName + "_hyr";
		forceCreateTable(replaceTempTable);
		insertData(replaceTempTable);
		Utils.dropTable(db, tableName);
		Utils.renameTable(db, replaceTempTable, tableName);
	}

	@Override
	public void increment() {
		//1.创建当前表
		forceCreateTable(currentTableName);
		//2.将执行sql后的结果集插入当前表中
		insertData(currentTableName);
		//3.创建增量表1,装载有效数据
		forceCreateTable(validTableName);
		//3.创建增量表2,装载失效数据
		forceCreateTable(invalidTableName);
		//4.当前表与最终表中的有效数据做比较，执行增量算法计算出 新增的数据 并插入到增量表中，action 字段值为 I
		computeValidDataAndInsert();
		//5.当前表与最终表中的有效数据做比较，执行增量算法计算出 关链的数据 并插入到增量表中，action 字段值为 D
		computeInvalidDataAndInsert();
		//6.根据增量表中存储的拉链结果来更新最终表
		// 开链数据
		db.execute(String.format("INSERT INTO %s SELECT * FROM %s",
				tableName, validTableName));
		// 关链数据
		String sql = String.format("UPDATE %s SET %s = '%s' WHERE " +
						"HYREN_MD5_VAL IN ( SELECT HYREN_MD5_VAL FROM %s )",
				tableName, EDATENAME, conf.getEtlDate(), invalidTableName);
		if (isMultipleInput) {
			sql = sql + " AND " + TABLE_ID_NAME + " = '" + datatableId + "'";
		}
		db.execute(sql);

		if (MARKET_INCREMENT_TMPTABLE_DELETE) {
			dropTempTable();
		}

	}

	private void computeValidDataAndInsert() {
		String validDataSql = String.format("INSERT INTO %s select * from %s WHERE NOT EXISTS " +
						"( SELECT 1 from %s WHERE %s.%s = %s.%s AND %s.%s = '%s')"
				, validTableName, currentTableName, tableName, currentTableName,
				MD5NAME, tableName, MD5NAME, tableName, EDATENAME, MAXDATE);
		if (isMultipleInput) {
			validDataSql = validDataSql.replace(")", " AND " + TABLE_ID_NAME
					+ " = '" + datatableId + "')");
		}
		db.execute(validDataSql);
	}

	private void computeInvalidDataAndInsert() {

		String invalidDataSql = String.format("INSERT INTO %s select * from %s WHERE NOT EXISTS " +
						"( SELECT 1 from %s WHERE %s.%s = %s.%s) AND %s.%s = '%s'",
				invalidTableName, tableName, currentTableName,
				tableName, MD5NAME, currentTableName, MD5NAME, tableName, EDATENAME, MAXDATE);
		if (isMultipleInput) {
			invalidDataSql = invalidDataSql.replace(")", " AND " + TABLE_ID_NAME
					+ " = '" + datatableId + "')");
		}
		db.execute(invalidDataSql);
	}

	@Override
	public void restore() throws SQLException {
		if (Utils.hasTodayData(db, tableName, etlDate,
				datatableId, isMultipleInput, conf.isIncrement())) {
			Utils.restoreDatabaseData(db, tableName, conf.getEtlDate(), conf.getDatatableId(),
					isMultipleInput, conf.isIncrement());
		}
	}

	@Override
	public void handleException() {
		db.rollback();
		versionManager.rollBack();
		if (versionManager.isVersionExpire()) {
			if (db.isExistTable(versionManager.getRenameTableName())) {
				Utils.dropTable(db, tableName);
				Utils.renameTable(db, versionManager.getRenameTableName(), tableName);
			}
		}
	}

	@Override
	public void preWork() {
		Utils.preWorkWithoutTrans(preSql, db);
	}

	@Override
	public void finalWork() {
		Utils.finalWorkWithoutTrans(finalSql, db);
		versionManager.updateSqlVersion();
		if (versionManager.isVersionExpire()) {
			versionManager.updateFieldVersion();
		}
		versionManager.commit();
		db.commit();
	}

	@Override
	public void close() {
		if (db != null) {
			db.close();
		}
		super.close();
	}

	private void dropTempTable() {
		Utils.softDropTable(db, currentTableName);
		Utils.softDropTable(db, validTableName);
		Utils.softDropTable(db, invalidTableName);
	}

	/**
	 * 创建表
	 * 如果表存在就删除掉
	 */
	private void forceCreateTable(String tableName) {
		Utils.forceCreateTable(db, tableName, createTableColumnTypes);
	}

	/**
	 * 插入数据
	 *
	 * @param tableName
	 */
	private void insertData(String tableName) {
		db.execute(String.format("INSERT INTO %s ( %s ) SELECT %s FROM ( %s ) a",
				tableName, columns, realSelectExpr(), sql));
	}

	private String lineMd5Expr(String columnsJoin) {
		List<String> columnList = StringUtil.split(columnsJoin, ",");
		if (columnList == null || columnList.isEmpty()) {
			throw new AppSystemException("采集作业信息不能为空");
		}
		//2、遍历list集合，获取每一个ColumnCleanResult对象的columnName
		List<String> columnNames = new ArrayList<>();
		for (String column : columnList) {
			columnNames.add(column);
		}
		return ConnectionTool.getDbType(databaseType.getCode()).ofColMd5(db, columnNames);
		/*return Utils.registerMd5Function(db, databaseType) +
				"(''||" + columnsJoin.replace(",", "||") + ")";*/
	}


	private List<String> getColumnSequence() {
		return new DruidParseQuerySql(sql).parseSelectAliasField();
	}

	/**
	 * 返回需要从sql中需要查询的表达式
	 * 包括，字段名称，自增函数，定制，hyren自定义字段等
	 */
	private String realSelectExpr() {
		final StringBuilder selectExpr = new StringBuilder(120);
		//只有映射字段做MD5
		final StringBuilder md5Cols = new StringBuilder(120);
		/**
		 * field.getProcess_para()
		 * 1.如果是映射，则返回的是改字段对应sql中的真实查询出来的字段序号
		 * 2.如果是定值，则返回的是定值的值
		 */
		conf.getDatatableFields()
				.stream()
				.filter(field -> !Constant.HYRENFIELD.contains(field.getField_en_name().toUpperCase()))
				.forEach(field -> {
					String processCode = field.getField_process();
					if (ProcessType.ZiZeng.getCode().equals(processCode)) {
						selectExpr.append(autoIncreasingExpr());
					} else if (ProcessType.YingShe.getCode().equals(processCode)
							|| ProcessType.FenZhuYingShe.getCode().equals(processCode)
							|| ProcessType.HanShuYingShe.getCode().equals(processCode)
							|| ProcessType.DingZhi.getCode().equals(processCode)) {
//                        int mappingIndex = Integer.parseInt(field.getProcess_mapping());
//                        selectExpr.append(colSeq.get(mappingIndex));
//                        md5Cols.append(field.getField_en_name()).append(',');
						selectExpr.append(field.getField_en_name());
						md5Cols.append(field.getField_en_name()).append(',');
					} else {
						throw new AppSystemException("不支持处理方式码：" + processCode);
					}
					selectExpr.append(',');
				});
		//添加自定义字段
		if (isMultipleInput) {
			selectExpr
					.append(conf.getDatatableId())
					.append(',');
		}
		selectExpr.append(conf.getEtlDate());

		if (conf.isIncrement()) {
			selectExpr.append(',').append(MAXDATE).append(',')
					.append(lineMd5Expr(md5Cols.deleteCharAt(md5Cols.length() - 1).toString()));
		}
		return selectExpr.toString();
	}

	/**
	 * 返回自增函数表达式
	 */
	private String autoIncreasingExpr() {
		//使用开窗函数 row_number() over ()做自增列
		//如果选择了主键，使用主键做开窗排序列
		List<String> additionalAttrs = conf.getAddAttrColMap().get(StoreLayerAdded.ZhuJian.getCode());
		String orderByCol = "";
		if (additionalAttrs != null) {
			orderByCol = additionalAttrs.get(0);
		} else {
			//随机选择一个普通列作为排序列
			List<Datatable_field_info> datatableCreateFields = conf.getDatatableFields();
			for (Datatable_field_info field_info : datatableCreateFields) {
				if (!field_info.getField_en_name().startsWith("hyren_") &&
						!ProcessType.ZiZeng.getCode().equals(field_info.getField_process())) {
					orderByCol = field_info.getField_en_name();
					break;
				}
			}
		}
		if (StringUtil.isBlank(orderByCol)) {
			throw new AppSystemException("找不到自增函数的排序列");
		}
		return " row_number() over (order by " + orderByCol + ")";
	}
}
