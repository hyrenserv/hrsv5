package hrds.agent.job.biz.core.dfstage.service;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.DataStoreConfBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.constant.DataTypeConstant;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.exception.AppSystemException;
import hrds.commons.hadoop.readconfig.ConfigReader;
import hrds.commons.utils.Constant;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.mapred.OrcMapredRecordReader;
import org.apache.orc.mapred.OrcStruct;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@DocClass(desc = "读取卸数文件到数据库", author = "zxz", createdate = "2019/12/17 15:43")
public class ReadFileToDataBase implements Callable<Long> {

	//打印日志
	private static final Logger LOGGER = LogManager.getLogger();
	//卸数到本地的文件绝对路径
	private final String fileAbsolutePath;
	//数据采集表对应的存储的所有信息
	private final CollectTableBean collectTableBean;
	//数据库采集表对应的meta信息
	private final TableBean tableBean;
	//文件对应存储的目的地信息
	private final DataStoreConfBean dataStoreConfBean;
	//本地文件BLOB类型的问价流存放的文件夹 LOBS
	private final String lobsFileAbsolutePath;

	/**
	 * 读取文件到数据库构造方法
	 *
	 * @param fileAbsolutePath  String 含义：本地文件绝对路径
	 * @param tableBean         TableBean 含义：文件对应的表结构信息
	 * @param collectTableBean  CollectTableBean 含义：文件对应的卸数信息
	 * @param dataStoreConfBean DataStoreConfBean 含义：文件需要上传到表对应的存储信息
	 */
	public ReadFileToDataBase(String fileAbsolutePath, TableBean tableBean, CollectTableBean collectTableBean,
		DataStoreConfBean dataStoreConfBean) {
		this.fileAbsolutePath = fileAbsolutePath;
		this.collectTableBean = collectTableBean;
		this.dataStoreConfBean = dataStoreConfBean;
		this.tableBean = tableBean;
		this.lobsFileAbsolutePath = FileNameUtils.getFullPath(fileAbsolutePath) + "LOBS" + File.separator;
	}

	@Method(desc = "执行读取文件batch提交到数据库的方法", logicStep = "")
	@Override
	public Long call() {
		long count;
		try (DatabaseWrapper db = ConnectionTool.getDBWrapper(dataStoreConfBean.getData_store_connect_attr())) {
			//1.获取数据库的连接
			List<String> columnList = StringUtil.split(tableBean.getColumnMetaInfo(), Constant.METAINFOSPLIT);
			//2.取值的应该根据原来的类型来
			List<String> sourceTypeList = StringUtil.split(tableBean.getColTypeMetaInfo(), Constant.METAINFOSPLIT);
			//转换之后的类型用于建表语句
//			List<String> typeList = DataTypeTransform.tansform(StringUtil.split(tableBean.getColTypeMetaInfo(),
//					Constant.METAINFOSPLIT), dataStoreConfBean.getDsl_name());
			//3.拼接batch插入数据库的sql
			String batchSql = getBatchSql(columnList, collectTableBean.getHbase_name() + "_"
				+ 1);
			//4.根据卸数问价类型读取文件插入到数据库
			//文件编码
			String file_code = tableBean.getFile_code();
			//文件类型
			String file_format = tableBean.getFile_format();
			//列分隔符
			String column_separator = tableBean.getColumn_separator();
			//是否包含表头
			String is_header = tableBean.getIs_header();
			if (FileFormat.CSV.getCode().equals(file_format)) {
				count = readCsvToDataBase(db, columnList, sourceTypeList, batchSql, file_code, is_header);
			} else if (FileFormat.PARQUET.getCode().equals(file_format)) {
				count = readParquetToDataBase(db, columnList, sourceTypeList, batchSql);
			} else if (FileFormat.ORC.getCode().equals(file_format)) {
				count = readOrcToDataBase(db, sourceTypeList, batchSql);
			} else if (FileFormat.SEQUENCEFILE.getCode().equals(file_format)) {
				count = readSequenceToDataBase(db, columnList, sourceTypeList, batchSql);
			} else if (FileFormat.DingChang.getCode().equals(file_format)) {
				//分隔符为空
				if (StringUtil.isEmpty(column_separator)) {
					count = readDingChangToDataBase(db, columnList, sourceTypeList, batchSql, file_code, is_header);
				} else {
					count = readFeiDingChangToDataBase(db, columnList, sourceTypeList, batchSql,
						column_separator, file_code, is_header);
				}
			} else if (FileFormat.FeiDingChang.getCode().equals(file_format)) {
				count = readFeiDingChangToDataBase(db, columnList, sourceTypeList, batchSql,
					column_separator, file_code, is_header);
			} else {
				throw new AppSystemException("不支持的卸数文件格式");
			}
		} catch (Exception e) {
			count = -1L;
			LOGGER.error("数据库采集读文件上传到数据库异常", e);
		}
		//6.返回batch插入数据库的数据量
		return count;
	}

	private long readFeiDingChangToDataBase(DatabaseWrapper db, List<String> columnList,
		List<String> typeList, String batchSql, String dataDelimiter,
		String database_code, String is_header) {
		long num = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileAbsolutePath),
			DataBaseCode.ofValueByCode(database_code)))) {
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			String line;
			if (IsFlag.Shi.getCode().equals(is_header)) {
				//判断包含表头，先读取表头
				line = reader.readLine();
				if (line != null) {
					LOGGER.info("读取到表头为：" + line);
				}
			}
			Object[] objs;
			while ((line = reader.readLine()) != null) {
				num++;
				objs = new Object[columnList.size()];// 存储全量插入信息的list
				List<String> valueList = StringUtil.split(line, dataDelimiter);
				if (valueList.size() != columnList.size()) {
					throw new AppSystemException("取到数据的列的数量跟数据字典定义的列的长度不一致，请检查数据是否有问题：========"
						+ valueList.toString());
				}
				for (int j = 0; j < columnList.size(); j++) {
					objs[j] = getValue(typeList.get(j), valueList.get(j), db.getDbtype());
				}
				pool.add(objs);
				if (num % JobConstant.BUFFER_ROW == 0) {
					doBatch(batchSql, pool, num, db);
				}
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
		} catch (Exception e) {
			throw new AppSystemException("bash插入数据库失败:", e);
		}
		return num;
	}

	public static List<String> getDingChangValueList(String line, List<Integer> lengthList, String database_code)
		throws Exception {
		List<String> valueList = new ArrayList<>();
		byte[] bytes = line.getBytes(database_code);
		int begin = 0;
		for (int length : lengthList) {
			byte[] byteTmp = new byte[length];
			System.arraycopy(bytes, begin, byteTmp, 0, length);
			begin += length;
			valueList.add(new String(byteTmp, database_code));
		}
		return valueList;
	}

	private long readDingChangToDataBase(DatabaseWrapper db, List<String> columnList, List<String> typeList,
		String batchSql, String database_code, String is_header) {
		database_code = DataBaseCode.ofValueByCode(database_code);
		long num = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileAbsolutePath),
			database_code))) {
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			String line;
			Object[] objs;
			List<String> lengthStrList = StringUtil.split(tableBean.getColLengthInfo(),
				Constant.METAINFOSPLIT);
			List<Integer> lengthList = new ArrayList<>();
			for (String lengthStr : lengthStrList) {
				lengthList.add(Integer.parseInt(lengthStr));
			}
			if (IsFlag.Shi.getCode().equals(is_header)) {
				//判断包含表头，先读取表头
				line = reader.readLine();
				if (line != null) {
					LOGGER.info("读取到表头为：" + line);
				}
			}
			while ((line = reader.readLine()) != null) {
				num++;
				objs = new Object[columnList.size()];// 存储全量插入信息的list
				List<String> valueList = getDingChangValueList(line, lengthList, database_code);
				if (valueList.size() != columnList.size()) {
					throw new AppSystemException("取到数据的列的数量跟数据字典定义的列的长度不一致，请检查数据是否有问题：========"
						+ valueList.toString());
				}
				for (int j = 0; j < columnList.size(); j++) {
					objs[j] = getValue(typeList.get(j), valueList.get(j), db.getDbtype());
				}
				pool.add(objs);
				if (num % JobConstant.BUFFER_ROW == 0) {
					doBatch(batchSql, pool, num, db);
				}
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
		} catch (Exception e) {
			throw new AppSystemException("bash插入数据库失败", e);
		}
		return num;
	}

	private long readSequenceToDataBase(DatabaseWrapper db, List<String> columnList, List<String> typeList,
		String batchSql) throws Exception {
		//TODO 这里是不是修改？？
		Configuration conf = ConfigReader.getConfiguration();
		conf.setBoolean("fs.hdfs.impl.disable.cache", true);
		conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
		conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
		conf.set("fs.defaultFS", "file:///");
		long num = 0L;
		SequenceFile.Reader sfr = null;
		try {
			SequenceFile.Reader.Option optionFile = SequenceFile.Reader.file((new Path(fileAbsolutePath)));
			sfr = new SequenceFile.Reader(conf, optionFile);
			NullWritable key = NullWritable.get();
			Text value = new Text();
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			Object[] objs;
			while (sfr.next(key, value)) {
				String str = value.toString();
				//XXX SequenceFile不指定分隔符，页面也不允许其指定分隔符，使用hive默认的\001隐藏字符做分隔符
				//XXX 这样只要创建hive映射外部表时使用store as sequencefile hive会自动解析。batch方式使用默认的去解析
				List<String> valueList = StringUtil.split(str, Constant.SEQUENCEDELIMITER);
				if (valueList.size() != columnList.size()) {
					throw new AppSystemException("取到数据的列的数量跟数据字典定义的列的长度不一致，请检查数据是否有问题：========"
						+ valueList.toString());
				}
				objs = new Object[columnList.size()];// 存储全量插入信息的list
				for (int j = 0; j < columnList.size(); j++) {
					objs[j] = getValue(typeList.get(j), valueList.get(j), db.getDbtype());
				}
				num++;
				pool.add(objs);
				if (num % JobConstant.BUFFER_ROW == 0) {
					doBatch(batchSql, pool, num, db);
				}
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
		} finally {
			if (sfr != null) {
				sfr.close();
			}
		}
		return num;
	}

	private long readOrcToDataBase(DatabaseWrapper db, List<String> typeList,
		String batchSql) throws Exception {
		RecordReader rows = null;
		long num = 0L;
		try {
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			Object[] objs;
			Reader reader = OrcFile.createReader(new Path(fileAbsolutePath), OrcFile.readerOptions(
				ConfigReader.getConfiguration()));
			rows = reader.rows();
			TypeDescription schema = reader.getSchema();
			List<TypeDescription> children = schema.getChildren();
			VectorizedRowBatch batch = schema.createRowBatch();
			int numberOfChildren = children.size();
			while (rows.nextBatch(batch)) {
				for (int r = 0; r < batch.size; r++) {
					OrcStruct result = new OrcStruct(schema);
					for (int i = 0; i < numberOfChildren; ++i) {
						OrcMapredRecordReader.nextValue(batch.cols[i], r,
							children.get(i), result.getFieldValue(i));
						result.setFieldValue(i, OrcMapredRecordReader.nextValue(batch.cols[i], r,
							children.get(i), result.getFieldValue(i)));
					}
					num++;
					objs = new Object[result.getNumFields()];// 存储全量插入信息的list
					for (int i = 0; i < result.getNumFields(); i++) {
						objs[i] = getValue(typeList.get(i), result.getFieldValue(i), db.getDbtype());
					}
					pool.add(objs);
//					LOGGER.info(objs.toString());
//					LOGGER.info(batchSql);
				}
				doBatch(batchSql, pool, num, db);
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
		} finally {
			if (rows != null) {
				rows.close();
			}
		}
		return num;
	}

	private long readParquetToDataBase(DatabaseWrapper db, List<String> columnList, List<String> typeList,
		String batchSql) {
		ParquetReader<Group> build = null;
		try {
			long num = 0;
			GroupReadSupport readSupport = new GroupReadSupport();
			ParquetReader.Builder<Group> reader = ParquetReader.builder(readSupport, new Path(fileAbsolutePath));
			build = reader.build();
			Group line;
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			Object[] objs;
			while ((line = build.read()) != null) {
				objs = new Object[columnList.size()];// 存储全量插入信息的list
				for (int j = 0; j < columnList.size(); j++) {
					objs[j] = getParquetValue(typeList.get(j), line, columnList.get(j));
				}
				num++;
				pool.add(objs);
				if (num % JobConstant.BUFFER_ROW == 0) {
					doBatch(batchSql, pool, num, db);
				}
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
			return num;
		} catch (Exception e) {
			throw new AppSystemException("读取parquet文件失败", e);
		} finally {
			try {
				if (build != null) {
					build.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Object getParquetValue(String type, Group line, String column) throws FileNotFoundException {
		Object str;
		type = type.toLowerCase();
		if (type.contains(DataTypeConstant.BOOLEAN.getMessage())) {
			// 如果取出的值为null则给空字符串
			str = line.getBoolean(column, 0);
		} else if (type.contains(DataTypeConstant.INT8.getMessage())
			|| type.contains(DataTypeConstant.BIGINT.getMessage())
			|| type.contains(DataTypeConstant.LONG.getMessage())) {
			str = line.getLong(column, 0);
		} else if (type.contains(DataTypeConstant.INT.getMessage())) {
			str = line.getInteger(column, 0);
		} else if (type.contains(DataTypeConstant.FLOAT.getMessage())) {
			str = line.getFloat(column, 0);
		} else if (type.contains(DataTypeConstant.DOUBLE.getMessage())
			|| type.contains(DataTypeConstant.DECIMAL.getMessage())
			|| type.contains(DataTypeConstant.NUMERIC.getMessage())) {
			str = line.getDouble(column, 0);
		} else if (DataTypeConstant.CLOB.getMessage().equalsIgnoreCase(type)) {
			str = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
				line.getString(column, 0).getBytes())));
		} else if (DataTypeConstant.BLOB.getMessage().equalsIgnoreCase(type)) {
			String tmpValue = line.getString(column, 0);
			if (tmpValue == null || StringUtil.isEmpty(tmpValue)) {
				str = null;
			} else {
				str = new BufferedInputStream(new FileInputStream(lobsFileAbsolutePath +
					tmpValue));
			}
		} else {
			// 如果取出的值为null则给空字符串
			if ((str = line.getString(column, 0)) == null) {
				str = "";
			}
			//TODO 这里应该有好多类型需要支持，然后在else里面报错
		}
		return str;
	}

	private long readCsvToDataBase(DatabaseWrapper db, List<String> columnList, List<String> typeList,
		String batchSql, String database_code, String is_header) {
		//TODO 分隔符应该使用传进来的，懒得找了，测试的时候一起改吧
		long num = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileAbsolutePath),
			DataBaseCode.ofValueByCode(database_code)));
			CsvListReader csvReader = new CsvListReader(reader,
				CsvPreference.EXCEL_PREFERENCE)) {
			List<Object[]> pool = new ArrayList<>();// 存储全量插入信息的list
			List<String> lineList;
			Object[] objs;
			if (IsFlag.Shi.getCode().equals(is_header)) {
				//判断包含表头，先读取表头
				lineList = csvReader.read();
				if (lineList != null) {
					LOGGER.info("读取到表头为：" + lineList.toString());
				}
			}
			while ((lineList = csvReader.read()) != null) {
				objs = new Object[columnList.size()];// 存储全量插入信息的list
				for (int j = 0; j < columnList.size(); j++) {
					objs[j] = getValue(typeList.get(j), lineList.get(j), db.getDbtype());
				}
				num++;
				pool.add(objs);
				if (num % JobConstant.BUFFER_ROW == 0) {
					doBatch(batchSql, pool, num, db);
				}
			}
			if (pool.size() != 0) {
				doBatch(batchSql, pool, num, db);
			}
		} catch (Exception e) {
			throw new AppSystemException("bash插入数据库失败", e);
		}
		return num;
	}

	private Object getValue(String type, WritableComparable tmpValue, Dbtype dbtype)
		throws FileNotFoundException {
		Object str;
		type = type.toLowerCase();
		if (type.contains(DataTypeConstant.BOOLEAN.getMessage())) {
			// 如果取出的值为null则给空字符串
			str = tmpValue == null ? null : Boolean.parseBoolean(tmpValue.toString().trim());
		} else if (type.contains(DataTypeConstant.LONG.getMessage())
			|| type.contains(DataTypeConstant.INT.getMessage())
			|| type.contains(DataTypeConstant.FLOAT.getMessage())
			|| type.contains(DataTypeConstant.DOUBLE.getMessage())
			|| type.contains(DataTypeConstant.DECIMAL.getMessage())
			|| type.contains(DataTypeConstant.NUMERIC.getMessage())) {
			// 如果取出的值为null则给空字符串
			str = tmpValue == null ? null : new BigDecimal(tmpValue.toString().trim());
		} else if (DataTypeConstant.CLOB.getMessage().equalsIgnoreCase(type)) {
			String clobValue = tmpValue == null ? "" : tmpValue.toString();
			str = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
				clobValue.getBytes())));
		} else if (DataTypeConstant.BLOB.getMessage().equalsIgnoreCase(type)) {
			if (tmpValue == null || StringUtil.isEmpty(tmpValue.toString())) {
				str = null;
			} else {
				str = new BufferedInputStream(new FileInputStream(lobsFileAbsolutePath +
					tmpValue.toString()));
			}
		} else {
			// 如果取出的值为null则给空字符串
			str = tmpValue == null ? "" : tmpValue.toString();
			//TODO 这里应该有好多类型需要支持，然后在else里面报错
		}
		if (Dbtype.TERADATA == dbtype) {
			if (str != null) {
				str = String.valueOf(str);
			}
		}
		return str;
	}

	private Object getValue(String type, String tmpValue, Dbtype dbtype) throws
		FileNotFoundException {
		Object str;
		type = type.toLowerCase();
		if (type.contains(DataTypeConstant.BOOLEAN.getMessage())) {
			// 如果取出的值为null则给空字符串
			str = StringUtil.isBlank(tmpValue) ? null : Boolean.parseBoolean(tmpValue.trim());
		} else if (type.contains(DataTypeConstant.LONG.getMessage())
			|| type.contains(DataTypeConstant.INT.getMessage())
			|| type.contains(DataTypeConstant.FLOAT.getMessage())
			|| type.contains(DataTypeConstant.DOUBLE.getMessage())
			|| type.contains(DataTypeConstant.DECIMAL.getMessage())
			|| type.contains(DataTypeConstant.NUMERIC.getMessage())) {
			// 如果取出的值为null则给空字符串
			str = StringUtil.isBlank(tmpValue) ? null : new BigDecimal(tmpValue.trim());
		} else if (DataTypeConstant.CLOB.getMessage().equalsIgnoreCase(type)) {
			String clobValue = tmpValue == null ? "" : tmpValue;
			str = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
				clobValue.getBytes())));
		} else if (DataTypeConstant.BLOB.getMessage().equalsIgnoreCase(type)) {
			if (tmpValue == null || StringUtil.isEmpty(tmpValue)) {
				str = null;
			} else {
				str = new BufferedInputStream(new FileInputStream(lobsFileAbsolutePath +
					tmpValue));
			}
		} else if (type.contains(DataTypeConstant.TIMESTAMP.getMessage())) {
			str = Timestamp.valueOf(tmpValue);
		} else {
			// 如果取出的值为null则给空字符串
			str = StringUtil.isBlank(tmpValue) ? "" : tmpValue.trim();
			//TODO 这里应该有好多类型需要支持，然后在else里面报错
		}
		if (Dbtype.TERADATA == dbtype) {
			if (str != null) {
				str = String.valueOf(str);
			}
		}
		return str;
	}

	private void doBatch(String batchSql, List<Object[]> pool, long num, DatabaseWrapper db) {
		int[] ints = db.execBatch(batchSql, pool);
//		for (int i : ints) {
//		XXX Oracle数据库batch插入，这边返回值是-2
//			if (i != 1) {
//				throw new AppSystemException("批量插入数据出现错误,退出");
//			}
//			LOGGER.info("batch插入的返回值为" + i);
//		}
		LOGGER.info("本次batch插入" + ints.length);
		LOGGER.info("数据库已插入" + num + "条！");
		pool.clear();// 插入成功，清空集合
	}

	public static String getBatchSql(List<String> columns, String todayTableName) {
		//拼接插入的sql
		StringBuilder sbAdd = new StringBuilder();
		sbAdd.append("insert into ").append(todayTableName).append("(");
		for (String column : columns) {
			sbAdd.append(column).append(",");
		}
		sbAdd.deleteCharAt(sbAdd.length() - 1);
		sbAdd.append(") values(");
		for (int i = 0; i < columns.size(); i++) {
			if (i != columns.size() - 1) {
				sbAdd.append("?").append(",");
			} else {
				sbAdd.append("?");
			}
		}
		sbAdd.append(")");
		return sbAdd.toString();
	}

	public static void main(String[] args) {
		//自己使用别的库,建表语句create table aaa(a varchar(100),b int,c varchar(100))
		List<Object[]> arr = new ArrayList<>();
		Object[] aa = new Object[3];
		aa[0] = "aaaa,ccc,sssdddeee";
		aa[1] = null;
		aa[2] = "cccc";
		arr.add(aa);
		try (DatabaseWrapper dbWrapper = ConnectionTool.getDBWrapper("oracle.jdbc.OracleDriver",
			"jdbc:oracle:thin:@47.103.83.1:1521:hyshf",
			"hyshf", "hyshf", DatabaseType.Oracle10g.getCode())) {
//		try (DatabaseWrapper dbWrapper = ConnectionTool.getDBWrapper("org.postgresql.Driver",
//				"jdbc:postgresql://47.103.83.1:32001/hrsdxg",
//				"hrsdxg", "hrsdxg", DatabaseType.Postgresql.getCode())) {
//			String batchSql = "insert into z001_fff_item_1(i_item_sk,i_item_id,i_rec_start_date,i_rec_end_date,i_item_desc,i_current_price,i_wholesale_cost,i_brand_id,i_brand,i_class_id,i_class,i_category_id,i_category,i_manufact_id,i_manufact,i_size,i_formulation,i_color,i_units,i_container,i_manager_id,i_product_name,remark,HYREN_S_DATE) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String batchSql = "insert into aaa(a,b,c) values (?,?,?)";
			int[] ints = dbWrapper.execBatch(batchSql, arr);
			System.out.println("插入" + ints.length + "条");
		}
	}
}
