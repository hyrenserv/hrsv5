package hrds.agent.job.biz.core.jdbcdirectstage.service;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.DataStoreConfBean;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.exception.AppSystemException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

@DocClass(desc = "多线程采集线程类，子线程向主线程返回的有生成的文件路径，当前线程采集到的ResultSet，" +
		"当前线程采集到的数据量", author = "zxz")
public class CollectPage implements Callable<Long> {
	private final SourceDataConfBean sourceDataConfBean;
	private final CollectTableBean collectTableBean;
	private final TableBean tableBean;
	private final String sql;
	private final int start;
	private final int end;
	//直连采集对应存储的目的地信息
	private final DataStoreConfBean dataStoreConfBean;

	public CollectPage(SourceDataConfBean sourceDataConfBean, CollectTableBean collectTableBean,
					   TableBean tableBean, int start, int end, DataStoreConfBean dataStoreConfBean) {
		this.sourceDataConfBean = sourceDataConfBean;
		this.collectTableBean = collectTableBean;
		this.tableBean = tableBean;
		this.start = start;
		this.end = end;
		this.sql = tableBean.getCollectSQL();
		this.dataStoreConfBean = dataStoreConfBean;
	}

	public CollectPage(SourceDataConfBean sourceDataConfBean, CollectTableBean collectTableBean,
					   TableBean tableBean, int start, int end, DataStoreConfBean dataStoreConfBean, String sql) {
		this.sourceDataConfBean = sourceDataConfBean;
		this.collectTableBean = collectTableBean;
		this.tableBean = tableBean;
		this.start = start;
		this.end = end;
		this.sql = sql;
		this.dataStoreConfBean = dataStoreConfBean;
	}

	@Method(desc = "多线程数据库直连采集执行方法", logicStep = "" +
			"1、执行查询，获取ResultSet" +
			"2、解析ResultSet，并batch入库" +
			"3、返回值为batch插入数据库的条数")
	@Override
	public Long call() {
		ResultSet resultSet = null;
		try (DatabaseWrapper db = ConnectionTool.getDBWrapper(sourceDataConfBean.getDatabase_drive(),
				sourceDataConfBean.getJdbc_url(), sourceDataConfBean.getUser_name(),
				sourceDataConfBean.getDatabase_pad(), sourceDataConfBean.getDatabase_type(),
				sourceDataConfBean.getDatabase_name(), 50)) {
			long rowCount = 0L;
			//1、执行查询，获取ResultSet
			resultSet = getPageData(db);
			//放入DBType类型
			collectTableBean.setDb_type(db.getDbtype());
			if (resultSet != null) {
				//2、解析ResultSet，并batch入库
				ParseResultSetToDataBase parser = new ParseResultSetToDataBase(resultSet, tableBean,
						collectTableBean, dataStoreConfBean);
				//3、返回值为batch插入数据库的条数
				rowCount = parser.parseResultSet();
			}
			return rowCount;
		} catch (Exception e) {
			throw new AppSystemException("执行分页卸数程序失败", e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Method(desc = "根据分页SQL获取ResultSet", logicStep = "" +
			"1、将DBConfigBean对象传入工具类ConnectionTool，得到DatabaseWrapper" +
			"2、将采集SQL，当前页的start，end转换通过strategy转为分页SQL" +
			"3、调用方法获得当前线程的分页数据并返回")
	@Param(name = "strategy", desc = "数据库方言策略实例", range = "不为空，DataBaseDialectStrategy接口实例")
	@Param(name = "strSql", desc = "数据库直连采集作业SQL语句", range = "不为空")
	@Param(name = "start", desc = "当前分页开始条数", range = "不限")
	@Param(name = "end", desc = "当前分页结束条数", range = "不限")
	@Return(desc = "当前线程执行分页SQL查询得到的结果集", range = "不会为null")
	private ResultSet getPageData(DatabaseWrapper db) {
		//如果开始为1且结束为最大值，则表示不分页
		if (start == 1 && end == Integer.MAX_VALUE) {
			return db.queryGetResultSet(sql);
		} else {
			return db.queryPagedGetResultSet(sql, start, end, false);
		}
	}
}
