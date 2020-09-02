package hrds.commons.collection;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import hrds.commons.codes.*;
import hrds.commons.collection.bean.LayerBean;
import hrds.commons.collection.bean.LayerTypeBean;
import hrds.commons.entity.*;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.DruidParseQuerySql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DocClass(desc = "数据处理类，获取表的存储等信息", author = "xchao", createdate = "2020年3月31日 16:32:43")
public abstract class ProcessingData {


	@Method(desc = "根据sql存储层下的数据表数据",
			logicStep = "根据sql存储层下的数据表数据")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Return(desc = "查询出来的rs", range = "数据")
	public List<String> getDataLayer(String sql, DatabaseWrapper db) {
		return getPageDataLayer(sql, db, 0, 0, false);
	}

	@Method(desc = "根据sql和指定存储层信息获取指定存储层下的数据表数据",
			logicStep = "根据sql和指定存储层信息获取指定存储层下的数据表数据")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Param(name = "dsl_id", desc = "存储层配置id", range = "long类型")
	@Param(name = "intoLayerBean", desc = "指定存储层定义对象", range = "LayerBean实体对象")
	@Return(desc = "查询出来的rs", range = "数据")
	public List<String> getDataLayer(String sql, DatabaseWrapper db, long dsl_id) {
		return getPageDataLayer(sql, db, 0, 0, dsl_id, false);
	}

	@Method(desc = "根据sql存储层下的数据表数据",
			logicStep = "根据sql存储层下的数据表数据")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Param(name = "begin", desc = "开始条数", range = "int类型,大于等于1")
	@Param(name = "end", desc = "结束条数", range = "int类型,大于1")
	@Return(desc = "查询出来的rs", range = "数据")
	public void getPageDataLayer(String sql, DatabaseWrapper db, int begin, int end) {
		getPageDataLayer(sql, db, begin, end, false);
	}

	@Method(desc = "根据sql和指定存储层信息获取指定存储层下的数据表数据",
			logicStep = "根据sql和指定存储层信息获取指定存储层下的数据表数据")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Param(name = "begin", desc = "开始条数", range = "int类型,大于等于1")
	@Param(name = "end", desc = "结束条数", range = "int类型,大于1")
	@Param(name = "dsl_id", desc = "存储层配置id", range = "long类型")
	@Return(desc = "查询出来的rs", range = "数据")
	public void getPageDataLayer(String sql, DatabaseWrapper db, int begin, int end, long dsl_id) {
		getPageDataLayer(sql, db, begin, end, dsl_id, false);
	}

	@Method(desc = "根据sql和指定存储层信息获取指定存储层下的数据表数据",
			logicStep = "根据sql和指定存储层信息获取指定存储层下的数据表数据")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Param(name = "begin", desc = "开始条数", range = "int类型,大于等于1")
	@Param(name = "end", desc = "结束条数", range = "int类型,大于1")
	@Param(name = "dsl_id", desc = "存储层配置id", range = "long类型")
	@Param(name = "isCountTotal", desc = "是否count总条数", range = "是否count总条数")
	@Return(desc = "查询出来的rs", range = "数据")
	public List<String> getPageDataLayer(String sql, DatabaseWrapper db, int begin, int end, long dsl_id,
	                                     boolean isCountTotal) {
		return getResultSet(sql, db, dsl_id, begin, end, isCountTotal);
	}

	@Method(desc = "根据表名获取该表相应的存储信息", logicStep = "1")
	@Param(name = "sql", desc = "查询sql", range = "查询sql")
	@Param(name = "db", desc = "数据库db操作对象", range = "不可为空")
	@Param(name = "begin", desc = "开始条数", range = "int类型,大于等于1")
	@Param(name = "end", desc = "结束条数", range = "int类型,大于1")
	@Param(name = "isCountTotal", desc = "是否count总条数", range = "int类型,大于1")
	@Return(desc = "查询出来的rs", range = "数据")
	public List<String> getPageDataLayer(String sql, DatabaseWrapper db, int begin, int end, boolean isCountTotal) {

		LayerTypeBean ltb = getAllTableIsLayer(sql, db);
		String ofSql = getdStoreReg(sql, db);
		//只有一个存储，且是jdbc的方式
		if (ltb.getConnType() == LayerTypeBean.ConnType.oneJdbc) {
			Long dsl_id = ltb.getLayerBean().getDsl_id();
			return getResultSet(ofSql, db, dsl_id, begin, end, isCountTotal);
		}
		//只有一种存储，是什么，可以使用ltb.getLayerBean().getStore_type(),进行判断
		else if (ltb.getConnType() == LayerTypeBean.ConnType.oneOther) {
			//ltb.getLayerBean().getStore_type();
			//TODO 数据在一种存介质中，但不是jdbc
		}
		//有多种存储，但都支持JDBC，是否可以使用dblink的方式
		else if (ltb.getConnType() == LayerTypeBean.ConnType.moreJdbc) {
			//List<LayerBean> layerBeanList = ltb.getLayerBeanList();
			//TODO 数据都在关系型数据库，也就说都可以使用jdbc的方式的实现方式
			return getMoreJdbcResult(ofSql, begin, end, isCountTotal);
		}
		// 其他，包含了不同的存储，如jdbc、hbase、solr等不同给情况
		else if (ltb.getConnType() == LayerTypeBean.ConnType.moreOther) {
			//List<LayerBean> layerBeanList = ltb.getLayerBeanList();
			// TODO 混搭模式
		}
		return null;
	}

	private static String getdStoreReg(String sql, DatabaseWrapper db) {
		/*
		 * 判斷存储方式是不是 贴源登记，如果是，需要将表名修改为原始表名 ******************************start
		 */
		Map<String, String> map = new HashMap<>();
		List<String> listTable = DruidParseQuerySql.parseSqlTableToList(sql);
		for (String tableName : listTable) {
			Map<String, Object> objectMap = SqlOperator.queryOneObject(db,
					"select a.collect_type,a.table_name from " + Data_store_reg.TableName + " a join" +
							" " + Database_set.TableName + " b on a.database_id = b.database_id " +
							" where a.collect_type in (?,?) and lower(hyren_name) = ? and b.collect_type = ?",
					AgentType.DBWenJian.getCode(), AgentType.ShuJuKu.getCode(), tableName.toLowerCase(),
					CollectType.TieYuanDengJi.getCode());
			if (objectMap.size() != 0) {
				Pattern p = Pattern.compile("([\\s*|\\t|\\r|\\n+])" + tableName.toUpperCase());
				Matcher m = p.matcher(sql.toUpperCase());
				sql = m.replaceAll(" " + objectMap.get("table_name").toString().toUpperCase());
			}
		}
		return sql;
		/*
		 * 判斷存储方式是不是 贴源登记，如果是，需要将表名修改为原始表名 *********************************end
		 */
	}

	@Method(desc = "获取表的存储位置", logicStep = "获取表的存储位置")
	@Param(name = "tableName", desc = "表名", range = "取值范围说明")
	@Param(name = "db", desc = "DatabaseWrapper对象", range = "DatabaseWrapper对象")
	@Return(desc = "表的存储位置", range = "表的存储位置")
	private static List<LayerBean> getTableLayer(String tableName, DatabaseWrapper db) {
		//初始化表的存储位置
		List<LayerBean> tableStorageLayerBeans = new ArrayList<>();
		//查询贴元表信息，也就是通过数据采集过来的数据表
		Optional<Data_store_reg> dsr_optional = SqlOperator.queryOneObject(db, Data_store_reg.class,
				"select * from " + Data_store_reg.TableName + " where collect_type in (?,?) and lower(hyren_name) = ?",
				AgentType.DBWenJian.getCode(), AgentType.ShuJuKu.getCode(), tableName.toLowerCase());
		//如果登记表存在数据,则设置改表登记的存储层信息
		if (dsr_optional.isPresent()) {
			Data_store_reg dsr = dsr_optional.get();
			List<LayerBean> dcl_layerBeans = SqlOperator.queryList(db, LayerBean.class,
					"select dsl.*,'" + DataSourceType.DCL.getCode() + "' as dst from " + Table_storage_info.TableName + " tsi" +
							" join " + Dtab_relation_store.TableName + " dtrs on tsi.storage_id = dtrs.tab_id" +
							" join " + Data_store_layer.TableName + " dsl on dtrs.dsl_id = dsl.dsl_id" +
							" where tsi.table_id = ? and dtrs.data_source in (?,?)",
					dsr.getTable_id(), StoreLayerDataSource.DB.getCode(), StoreLayerDataSource.DBA.getCode());
			//记录数据表所在存储层信息
			tableStorageLayerBeans.addAll(dcl_layerBeans);
		}
		//查询集市表(DML)信息，通过数据集市产生的数表
		List<LayerBean> dml_layerBeans = SqlOperator.queryList(db, LayerBean.class,
				"select dsl.*,'" + DataSourceType.DML.getCode() + "' as dst from " + Dm_datatable.TableName + " dd" +
						" join  " + Dtab_relation_store.TableName + " dtrs on dd.datatable_id = dtrs.tab_id" +
						" join " + Data_store_layer.TableName + " dsl on dtrs.dsl_id = dsl.dsl_id" +
						" where lower(datatable_en_name) = ? and dtrs.data_source = ?",
				tableName.toLowerCase(), StoreLayerDataSource.DM.getCode());
		//记录数据表所在存储层信息
		tableStorageLayerBeans.addAll(dml_layerBeans);
		//查询数据管控层(DQC)表信息, 通过规则校验保存结果3生成的数表
		List<LayerBean> dqc_layerBeans = SqlOperator.queryList(db, LayerBean.class,
				"select dsl.*,'" + DataSourceType.DQC.getCode() + "' as dst from " + Dq_index3record.TableName + " di3" +
						" join " + Data_store_layer.TableName + " dsl on di3.dsl_id=dsl.dsl_id" +
						" where lower(di3.table_name) = ?",
				tableName.toLowerCase());
		//记录数据表所在存储层信息
		tableStorageLayerBeans.addAll(dqc_layerBeans);
		//查询自定义层(UDL)表信息, 通过数据管控创建的数表
		List<LayerBean> udl_layerBeans = SqlOperator.queryList(db, LayerBean.class,
				"select dsl.*,'" + DataSourceType.UDL.getCode() + "' as dst from " + Dq_table_info.TableName + " dqti" +
						" join  " + Dtab_relation_store.TableName + " dtrs on dqti.table_id = dtrs.tab_id" +
						" join " + Data_store_layer.TableName + " dsl on dtrs.dsl_id = dsl.dsl_id" +
						" where lower(dqti.table_name) = ? and dtrs.data_source = ?",
				tableName.toLowerCase(), StoreLayerDataSource.UD.getCode());
		//记录数据表所在存储层信息
		tableStorageLayerBeans.addAll(udl_layerBeans);
		if (tableStorageLayerBeans.size() != 0) {
			for (LayerBean layerBean : tableStorageLayerBeans) {
				layerBean.setLayerAttr(ConnectionTool.getLayerMap(db, layerBean.getDsl_id()));
			}
		}
		//TODO 这里以后需要添加加工数据、机器学习、流数据、系统管理维护的表、系统管理等
		if (tableStorageLayerBeans.isEmpty()) {
			throw new BusinessException("表: " + tableName + " 未在任何存储层中存在!");
		}
		return tableStorageLayerBeans;
	}

	/**
	 * 根据表名获取存储层的信息
	 *
	 * @param tableName 表名
	 * @param db        DatabaseWrapper对象
	 */
	public static List<LayerBean> getLayerByTable(String tableName, DatabaseWrapper db) {
		return getTableLayer(tableName, db);
	}

	/**
	 * 根据表名获取存储层的信息
	 *
	 * @param tableNameList 表名List
	 * @param db            DatabaseWrapper对象
	 */
	public static Map<String, List<LayerBean>> getLayerByTable(List<String> tableNameList, DatabaseWrapper db) {
		Map<String, List<LayerBean>> laytable = new HashMap<>();
		for (String tableName : tableNameList) {
			List<LayerBean> layerByTable = getLayerByTable(tableName, db);
			laytable.put(tableName, layerByTable);
		}
		return laytable;
	}

	/**
	 * 获取所有的表是不是在同一个存储层，且是jdbc或其他
	 *
	 * @param sql {@link String} sql语句
	 * @param db  {@link DatabaseWrapper} db
	 * @return layerTypeBean  存储信息
	 */
	public static LayerTypeBean getAllTableIsLayer(String sql, DatabaseWrapper db) {
		List<String> listTable = DruidParseQuerySql.parseSqlTableToList(sql);
		return getAllTableIsLayer(listTable, db);
	}

	/**
	 * 获取所有的表是不是在同一个存储层，且是jdbc或其他
	 *
	 * @param allTableList {@link List} sql语句
	 * @param db           {@link DatabaseWrapper} db
	 * @return layerTypeBean  存储信息
	 * <p>
	 * 一、判断所有的表是不是使用了同一个存储层，且是jdbc 返回 oneJdbc
	 * 二、判断所有的表是不是都使用的jdbc的方式，且是多个jdbc，返回 morejdbc
	 * 三、判断如果只有一个存储层，且不是jdbc，返回oneother
	 * 四、判断有多个存储层，且不是jdbc，返回 moreother
	 */

	public static LayerTypeBean getAllTableIsLayer(List<String> allTableList, DatabaseWrapper db) {
		/*
		 * 1、使用存储ID为key记录每个存储层下面有多少张表
		 * 获取所有表的存储层，记录所有表的存储层ID
		 */
		Map<String, LayerBean> allTableLayer = new HashMap<>();
		for (String tableName : allTableList) {
			List<LayerBean> tableLayer = getLayerByTable(tableName, db);
			//更加table获取每张表不同的存储信息，有可能一张表存储不同的目的地，所以这里是list
			for (LayerBean objectMap : tableLayer) {
				String layer_id = String.valueOf(objectMap.getDsl_id());
				LayerBean layerBean = allTableLayer.get(layer_id) == null ? objectMap : allTableLayer.get(layer_id);
				/*
				 * 根据id获取map中是否有存储层信息，如果没有，直接添加表的list将存储层信息添加
				 * 如果有，将表表的信息添加的list中就ok
				 */
				Set<String> tableNameList = layerBean.getTableNameList() == null ? new HashSet<>() : layerBean.getTableNameList();
				tableNameList.add(tableName);
				layerBean.setTableNameList(tableNameList);
				allTableLayer.put(layer_id, layerBean);
			}
		}
		/*
		 * 2、计算所有的包的存储目的地，是否是可以通用等方式
		 * 这里开始判断每个表在哪里存储
		 */
		LayerTypeBean layerTypeBean = new LayerTypeBean();
		List<LayerBean> list = new ArrayList<>();
		Iterator<String> iter = allTableLayer.keySet().iterator();
		Set<LayerTypeBean.ConnType> setconn = new HashSet<>();
		while (iter.hasNext()) {
			String key = iter.next();
			LayerBean objectMap = allTableLayer.get(key);
			String store_type = objectMap.getStore_type();
			Set<String> tableNameList = objectMap.getTableNameList();
			/*
			 * 1、如果有一个list中的表个数和解析的表个数一样，也就是说所有的表都在一个存储层中存在，所有直接用这个存储层的信息即可
			 * 2、如果不一样，所有的都是jdbc
			 */
			if (tableNameList.size() == allTableList.size()) {
				if (Store_type.DATABASE == Store_type.ofEnumByCode(store_type) || Store_type.HIVE == Store_type.ofEnumByCode(store_type))
					layerTypeBean.setConnType(LayerTypeBean.ConnType.oneJdbc);
				else
					layerTypeBean.setConnType(LayerTypeBean.ConnType.oneOther);
				layerTypeBean.setLayerBean(objectMap);
				return layerTypeBean;
			}
			//2、判断是不是都支持jdbc，是否可以使用dblink的方式进行使用
			if (Store_type.DATABASE == Store_type.ofEnumByCode(store_type) || Store_type.HIVE == Store_type.ofEnumByCode(store_type)) {
				setconn.add(LayerTypeBean.ConnType.moreJdbc);
			} else {
				setconn.add(LayerTypeBean.ConnType.moreOther);
			}
			list.add(objectMap);
			layerTypeBean.setLayerBeanList(list);
		}
		//set有一个，且是morejdbc，就是有过的且全部是jdbc的方式
		if (setconn.size() == 1 && setconn.contains(LayerTypeBean.ConnType.moreJdbc))
			layerTypeBean.setConnType(LayerTypeBean.ConnType.moreJdbc);
		else
			layerTypeBean.setConnType(LayerTypeBean.ConnType.moreOther);
		return layerTypeBean;
	}

	//************************************************具体的实现********************************************************/

	/**
	 * 实现数据库查询的方式
	 *
	 * @param sql    {@like String} 查询的sql语句
	 * @param db     {@link DatabaseWrapper} db
	 * @param dsl_id 存储层id
	 */
	private List<String> getResultSet(String sql, DatabaseWrapper db, long dsl_id, int begin, int end, boolean isCountTotal) {
		try (DatabaseWrapper dbDataConn = ConnectionTool.getDBWrapper(db, dsl_id)) {
			return getSQLData(sql, dbDataConn, begin, end, isCountTotal);
		} catch (Exception e) {
			throw new AppSystemException("sql查询出错: " + e.getMessage());
		}
	}


	private List<String> getMoreJdbcResult(String sql, int begin, int end, boolean isCountTotal) {
		try (DatabaseWrapper db = new DatabaseWrapper.Builder().dbname("Hive").create()) {
			return getSQLData(sql, db, begin, end, isCountTotal);
		} catch (Exception e) {
			throw new AppSystemException("sql查询出错: " + e.getMessage());
		}
	}

	private List<String> getSQLData(String sql, DatabaseWrapper db, int begin, int end, boolean isCountTotal) throws
			Exception {
		List<String> colArray = new ArrayList<>();//获取数据的列信息，存放到list中
		ResultSet rs;
		if (begin == 0 && end == 0) {
			rs = db.queryGetResultSet(sql);
		} else {
			rs = db.queryPagedGetResultSet(sql, begin, end, isCountTotal);
		}
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		for (int i = 0; i < cols; i++) {
			String colName = meta.getColumnName(i + 1).toLowerCase();
			colArray.add(colName);
		}
		while (rs.next()) {
			Map<String, Object> result = new HashMap<>();
			for (String col : colArray) {
				result.put(col, rs.getObject(col));
			}
			dealLine(result);
		}
		return colArray;
	}

	public abstract void dealLine(Map<String, Object> map) throws Exception;
	//public abstract void dealColHead(Map<String, Object> map) throws Exception;
}
