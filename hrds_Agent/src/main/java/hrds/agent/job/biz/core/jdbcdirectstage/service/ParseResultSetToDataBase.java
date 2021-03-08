package hrds.agent.job.biz.core.jdbcdirectstage.service;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.MD5Util;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.DataStoreConfBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.dfstage.fileparser.FileParserAbstract;
import hrds.agent.job.biz.core.dfstage.service.ReadFileToDataBase;
import hrds.commons.codes.IsFlag;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@DocClass(desc = "解析ResultSet数据到数据库", author = "zxz", createdate = "2019/12/17 15:43")
public class ParseResultSetToDataBase {

	//打印日志
	private static final Logger LOGGER = LogManager.getLogger();
	//采集数据的结果集
	private final ResultSet resultSet;
	//数据采集表对应的存储的所有信息
	private final CollectTableBean collectTableBean;
	//数据库采集表对应的meta信息
	private final TableBean tableBean;
	//直连采集对应存储的目的地信息
	private final DataStoreConfBean dataStoreConfBean;
	//操作日期
	protected String operateDate;
	//操作时间
	protected String operateTime;
	//操作人
	protected String user_id;
	//是否拉链存储
	private final boolean is_zipper_flag;

	/**
	 * 读取文件到数据库构造方法
	 *
	 * @param resultSet         ResultSet 含义：采集数据的结果集
	 * @param tableBean         TableBean 含义：文件对应的表结构信息
	 * @param collectTableBean  CollectTableBean 含义：文件对应的卸数信息
	 * @param dataStoreConfBean DataStoreConfBean 含义：文件需要上传到表对应的存储信息
	 */
	public ParseResultSetToDataBase(ResultSet resultSet, TableBean tableBean, CollectTableBean collectTableBean,
		DataStoreConfBean dataStoreConfBean) {
		this.resultSet = resultSet;
		this.collectTableBean = collectTableBean;
		this.dataStoreConfBean = dataStoreConfBean;
		this.tableBean = tableBean;
		this.operateDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		this.operateTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		this.user_id = String.valueOf(collectTableBean.getUser_id());
		this.is_zipper_flag = IsFlag.Shi.getCode().equals(collectTableBean.getIs_zipper());
	}

	public long parseResultSet() {
		//获取所有字段的名称，包括列分割和列合并出来的字段名称
		List<String> columnMetaInfoList = StringUtil.split(tableBean.getColumnMetaInfo(), Constant.METAINFOSPLIT);
		Map<String, Boolean> isZipperFieldInfo = FileParserAbstract.transMd5ColMap(tableBean.getIsZipperFieldInfo());
		StringBuilder md5Value = new StringBuilder();
		String etlDate = collectTableBean.getEtlDate();
		String batchSql = ReadFileToDataBase.getBatchSql(columnMetaInfoList,
			collectTableBean.getHbase_name() + "_" + 1);
		long counter = 0;
		try (DatabaseWrapper db = ConnectionTool.getDBWrapper(dataStoreConfBean.getData_store_connect_attr())) {
			LOGGER.info("连接配置为：" + dataStoreConfBean.getData_store_connect_attr().toString());
			PreparedStatement pst = db.getConnection().prepareStatement(batchSql);
			//获取所有查询的字段的名称，不包括列分割和列合并出来的字段名称
			List<String> selectColumnList = StringUtil.split(tableBean.getAllColumns(), Constant.METAINFOSPLIT);
			//用来batch提交
			List<String> typeList = StringUtil.split(tableBean.getAllType(),
				Constant.METAINFOSPLIT);
			int[] typeArray = tableBean.getTypeArray();
			int numberOfColumns = selectColumnList.size();
			boolean isodps = collectTableBean.getDb_type() == Dbtype.ODPS;
			LOGGER.info(
				"type : " + typeList.size() + "  colName " + numberOfColumns + "   是否为ODPS " + isodps);
			while (resultSet.next()) {
				//用来写一行数据
				counter++;
				for (int i = 0; i < numberOfColumns; i++) {
					int type = typeArray[i];
//					LOGGER.info("字段类型: " + type + " 字段名称: " + selectColumnList.get(i));
					//判断类型
					if (type == Types.BLOB || type == Types.LONGVARBINARY) {
						Blob blob = !isodps ? resultSet.getBlob(selectColumnList.get(i)) : resultSet.getBlob(i + 1);
						if (blob == null) {
							pst.setBinaryStream(i + 1, null);
						} else {
							pst.setBinaryStream(i + 1, blob.getBinaryStream());
						}
					} else if (type == Types.CLOB) {
						Clob clob = !isodps ? resultSet.getClob(selectColumnList.get(i)) : resultSet.getClob(i + 1);
						if (clob == null) {
							pst.setCharacterStream(i + 1, null);
						} else {
							pst.setCharacterStream(i + 1, clob.getCharacterStream());
						}
					} else if (type == Types.VARBINARY) {
						pst.setBinaryStream(i + 1,
							!isodps ? resultSet.getBinaryStream(selectColumnList.get(i)) : resultSet.getBinaryStream(i + 1));
					} else {
						//获取值
						Object obj = !isodps ? resultSet.getObject(selectColumnList.get(i)) : resultSet.getObject(i + 1);
						if (obj == null) {
							pst.setNull(i + 1, Types.NULL);
						} else {
							if (type == Types.DATE) {
								pst.setObject(i + 1, !isodps ? resultSet.getDate(selectColumnList.get(i)).toString()
									: resultSet.getDate(i + 1).toString());
							} else if (type == Types.TIME) {
								pst.setObject(i + 1, !isodps ? resultSet.getTime(selectColumnList.get(i)).toString()
									: resultSet.getTime(i + 1).toString());
							} else if (type == Types.TIMESTAMP) {
								pst.setObject(i + 1, !isodps ? resultSet.getTimestamp(selectColumnList.get(i)).toString()
									: resultSet.getTimestamp(i + 1).toString());
							} else if (type == Types.STRUCT) {
								pst.setObject(i + 1, !isodps ? resultSet.getObject(selectColumnList.get(i)).toString()
									: resultSet.getObject(i + 1).toString());
							} else {
								pst.setObject(i + 1, !isodps ? resultSet.getObject(selectColumnList.get(i))
									: resultSet.getObject(i + 1).toString());
							}
						}
					}
					//TODO 这里要放到上面，因为流的取值不能只能toString
					if (is_zipper_flag && isZipperFieldInfo.get(selectColumnList.get(i))) {
						md5Value.append(!isodps ? resultSet.getObject(selectColumnList.get(i)) : resultSet.getObject(i + 1));
					}
				}
				pst.setString(numberOfColumns + 1, etlDate);
				//判断是否拼接md5，结束日期
				if (is_zipper_flag) {
					pst.setString(numberOfColumns + 2, Constant.MAXDATE);
					pst.setString(numberOfColumns + 3, MD5Util.md5String(md5Value.toString()));
					md5Value.delete(0, md5Value.length());
				}
				//拼接操作时间、操作日期、操作人
				appendOperateInfo(pst, numberOfColumns);
				pst.addBatch();
				if (counter % JobConstant.BUFFER_ROW == 0) {
					LOGGER.info("正在入库，已batch插入" + counter + "行");
					pst.executeBatch();
				}
			}
			if (counter != 0 && counter % JobConstant.BUFFER_ROW != 0) {
				pst.executeBatch();
			}
			pst.close();
		} catch (Exception e) {
			LOGGER.error("batch入库失败", e);
			throw new AppSystemException("数据库直连采集batch入库失败", e);
		}
		//返回batch插入数据的条数
		return counter;
	}

	/**
	 * 添加操作日期、操作时间、操作人
	 */
	private void appendOperateInfo(PreparedStatement pst, int numberOfColumns) throws Exception {
		if (JobConstant.ISADDOPERATEINFO) {
			if (is_zipper_flag) {
				pst.setString(numberOfColumns + 4, operateDate);
				pst.setString(numberOfColumns + 5, operateTime);
				pst.setString(numberOfColumns + 6, user_id);
			} else {
				pst.setString(numberOfColumns + 2, operateDate);
				pst.setString(numberOfColumns + 3, operateTime);
				pst.setString(numberOfColumns + 4, user_id);
			}
		}
	}

}
