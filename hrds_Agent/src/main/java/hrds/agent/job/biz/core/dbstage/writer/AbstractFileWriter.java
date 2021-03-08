package hrds.agent.job.biz.core.dbstage.writer;

import com.alibaba.druid.DbType;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.MD5Util;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.Dbtype;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.exception.AppSystemException;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

@DocClass(desc = "数据库直连采集以指定的格式将数据卸到指定的数据文件，接口适配器，抽象类", author = "WangZhengcheng")
public abstract class AbstractFileWriter implements FileWriterInterface {
	//打印日志
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String SCHEMA_JSON = "{\"type\": \"record\",\"name\": \"BigFilesTest\", " + "\"fields\": [" + "{\"name\":\"" + "currValue"
			+ "\",\"type\":\"string\"}," + "{\"name\":\"" + "readerToByte" + "\", \"type\":\"bytes\"}" + "]}";//avro schema

	private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);
	private static final GenericRecord record = new GenericData.Record(SCHEMA);
	protected ResultSet resultSet;
	protected CollectTableBean collectTableBean;
	protected int pageNum;
	protected TableBean tableBean;
	protected Data_extraction_def data_extraction_def;
	protected Dbtype db_type;
	//操作日期
	protected String operateDate;
	//操作时间
	protected String operateTime;
	//操作人
	protected String user_id;

	public AbstractFileWriter(ResultSet resultSet, CollectTableBean collectTableBean, int pageNum,
							  TableBean tableBean, Data_extraction_def data_extraction_def) {
		this.resultSet = resultSet;
		this.collectTableBean = collectTableBean;
		this.pageNum = pageNum;
		this.tableBean = tableBean;
		this.data_extraction_def = data_extraction_def;
		this.operateDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		this.operateTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		this.user_id = String.valueOf(collectTableBean.getUser_id());
		this.db_type = collectTableBean.getDb_type();
	}

	@Method(desc = "把Blob类型转换为byte字节数组, 用于写Avro，在抽象类中实现，请子类不要覆盖这个方法"
			, logicStep = "1、以流的形式获取此Blob实例指定的BLOB值,并获取BufferedInputStream实例" +
			"              2、构建用于保存结果的字节数组" +
			"              3、从流中读数据并保存到字节数组中")
	@Param(name = "blob", desc = "采集得到的Blob类型的列的值", range = "不为空")
	@Return(desc = "采集得到的Blob类型的列的值转换得到的字节数组", range = "不会为null")
	private byte[] blobToBytes(Blob blob) {
		BufferedInputStream is = null;
		try {
			//1、以流的形式获取此Blob实例指定的BLOB值,并获取BufferedInputStream实例
			is = new BufferedInputStream(blob.getBinaryStream());
			//2、构建用于保存结果的字节数组
			byte[] bytes = new byte[(int) blob.length()];
			int len = bytes.length;
			int offset = 0;
			int read;
			//3、从流中读数据并保存到字节数组中
			while (offset < len && (read = is.read(bytes, offset, len - offset)) >= 0) {
				offset += read;
			}
			return bytes;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AppSystemException("jdbc获取blob的数据转为byte异常");
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * 解析result一行的值
	 */
	protected String getOneColumnValue(DataFileWriter<Object> avroWriter, long lineCounter, int pageNum, ResultSet resultSet,
									   int type, StringBuilder sb_, String column_name, String hbase_name, String midName)
			throws SQLException, IOException {
		String lobs_file_name = "";
		String reader2String = "";
		byte[] readerToByte = null;
		if (type == java.sql.Types.BLOB || type == Types.LONGVARBINARY) {
			Blob blob = resultSet.getBlob(column_name);
			if (null != blob) {
				readerToByte = blobToBytes(blob);
				if (readerToByte.length > 0) {
					lobs_file_name = "LOBs_" + hbase_name + "_" + column_name + "_" + pageNum + "_"
							+ lineCounter + "_BLOB_" + avroWriter.sync();
					sb_.append(lobs_file_name);
					reader2String = new String(readerToByte);
				}
			}
		} else if (type == Types.VARBINARY) {
			InputStream binaryStream = resultSet.getBinaryStream(column_name);
			if (null != binaryStream) {
				readerToByte = IOUtils.toByteArray(binaryStream);
				if (readerToByte != null && readerToByte.length > 0) {
					lobs_file_name = "LOBs_" + hbase_name + "_" + column_name + "_" + pageNum + "_"
							+ lineCounter + "_VARBINARY_" + avroWriter.sync();
					sb_.append(lobs_file_name);
					reader2String = new String(readerToByte);
				}
			}
		} else if (type == Types.CLOB) {
			Object obj = resultSet.getClob(column_name);
			if (null != obj) {
				Reader characterStream = resultSet.getClob(column_name).getCharacterStream();
				reader2String = readerStreamToString(characterStream);
				//清理不能处理的数据  TODO 这里其实修改了数据，需要讨论
				reader2String = clearIrregularData(reader2String);
			}
			sb_.append(reader2String);
		} else {
			Object oj = resultSet.getObject(column_name);
			if (null != oj) {
				if (type == java.sql.Types.TIMESTAMP) {
					String dateStr = resultSet.getTimestamp(column_name).toString();
					if (dateStr.endsWith(".0")) {
						dateStr = dateStr.replace(".0", "");
					}
					reader2String = dateStr;
				} else if (type == java.sql.Types.DATE) {
					Date date = resultSet.getDate(column_name);
					reader2String = date.toString();
				} else if (type == java.sql.Types.TIME) {
					Date date = resultSet.getTime(column_name);
					reader2String = date.toString();
				} else if (type == java.sql.Types.CHAR || type == java.sql.Types.VARCHAR
						|| type == java.sql.Types.NVARCHAR || type == java.sql.Types.BINARY
						|| type == java.sql.Types.LONGVARCHAR) {
					reader2String = oj.toString();

				} else {
					reader2String = oj.toString();
				}
				//清理数据  TODO 这里其实修改了数据，需要讨论
				reader2String = clearIrregularData(reader2String);
			}
			sb_.append(reader2String);
		}
		if (readerToByte != null && readerToByte.length > 0) {
			record.put("currValue", lobs_file_name);
			record.put("readerToByte", ByteBuffer.wrap(readerToByte));
			avroWriter.append(record);//往avro文件中写入信息（每行）
			//写lobs小文件
			writeLobsFileToOracle(midName + "LOBS", lobs_file_name, readerToByte);
		}
		return reader2String;
	}


	/**
	 * 解析result一行的值
	 */
	protected String getOneColumnValue(DataFileWriter<Object> avroWriter, long lineCounter, int pageNum, ResultSet resultSet,
									   int type, StringBuilder sb_, String column_name, String hbase_name, String midName,int i)
			throws SQLException, IOException {
		String lobs_file_name = "";
		String reader2String = "";
		byte[] readerToByte = null;
		if (type == java.sql.Types.BLOB || type == Types.LONGVARBINARY) {
			Blob blob = resultSet.getBlob(i);
			if (null != blob) {
				readerToByte = blobToBytes(blob);
				if (readerToByte.length > 0) {
					lobs_file_name = "LOBs_" + hbase_name + "_" + column_name + "_" + pageNum + "_"
							+ lineCounter + "_BLOB_" + avroWriter.sync();
					sb_.append(lobs_file_name);
					reader2String = new String(readerToByte);
				}
			}
		} else if (type == Types.VARBINARY) {
			InputStream binaryStream = resultSet.getBinaryStream(i);
			if (null != binaryStream) {
				readerToByte = IOUtils.toByteArray(binaryStream);
				if (readerToByte != null && readerToByte.length > 0) {
					lobs_file_name = "LOBs_" + hbase_name + "_" + column_name + "_" + pageNum + "_"
							+ lineCounter + "_VARBINARY_" + avroWriter.sync();
					sb_.append(lobs_file_name);
					reader2String = new String(readerToByte);
				}
			}
		} else if (type == Types.CLOB) {
			Object obj = resultSet.getClob(i);
			if (null != obj) {
				Reader characterStream = resultSet.getClob(i).getCharacterStream();
				reader2String = readerStreamToString(characterStream);
				//清理不能处理的数据  TODO 这里其实修改了数据，需要讨论
				reader2String = clearIrregularData(reader2String);
			}
			sb_.append(reader2String);
		} else {
			Object oj = resultSet.getObject(i);
			if (null != oj) {
				if (type == java.sql.Types.TIMESTAMP) {
					String dateStr = resultSet.getTimestamp(i).toString();
					if (dateStr.endsWith(".0")) {
						dateStr = dateStr.replace(".0", "");
					}
					reader2String = dateStr;
				} else if (type == java.sql.Types.DATE) {
					Date date = resultSet.getDate(i);
					reader2String = date.toString();
				} else if (type == java.sql.Types.TIME) {
					Date date = resultSet.getTime(i);
					reader2String = date.toString();
				} else if (type == java.sql.Types.CHAR || type == java.sql.Types.VARCHAR
						|| type == java.sql.Types.NVARCHAR || type == java.sql.Types.BINARY
						|| type == java.sql.Types.LONGVARCHAR) {
					reader2String = oj.toString();

				} else {
					reader2String = oj.toString();
				}
				//清理数据  TODO 这里其实修改了数据，需要讨论
				reader2String = clearIrregularData(reader2String);
			}
			sb_.append(reader2String);
		}
		if (readerToByte != null && readerToByte.length > 0) {
			record.put("currValue", lobs_file_name);
			record.put("readerToByte", ByteBuffer.wrap(readerToByte));
			avroWriter.append(record);//往avro文件中写入信息（每行）
			//写lobs小文件
			writeLobsFileToOracle(midName + "LOBS", lobs_file_name, readerToByte);
		}
		return reader2String;
	}

	/**
	 * 清理掉不规则的数据
	 *
	 * @param columnData 单列的数据
	 * @return 清理之后的数据
	 */
	public static String clearIrregularData(String columnData) {
		//TODO 目前针对换行符的问题，经过测试，可以通过自定义hive的TextInputFormat能解决自定义表的换行符，
		//TODO 但是如果页面自定义填写换行符，就导致需要每一个不同的换行符都需要对应一个自定义hive的
		//TODO TextInputFormat，难以实现，因此需要使用默认的行分隔符，或者提前实现几个TextInputFormat供选择
		//TODO 下面几行是使用默认的行分隔符，需要替换到数据本身的换行符，这里应该替换成特殊字符串，以便于还原
		if (columnData.contains("\r")) {
			columnData = columnData.replace('\r', ' ');
		}
		if (columnData.contains("\n")) {
			columnData = columnData.replace('\n', ' ');
		}
		if (columnData.contains("\r\n")) {
			columnData = StringUtil.replace(columnData, "\r\n", " ");
		}
		return columnData;
	}

	/**
	 * 获取数据库中clob的值，将其转为字符串
	 */
	private String readerStreamToString(Reader characterStream) throws IOException {
		BufferedReader br = new BufferedReader(characterStream);
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = br.readLine()) != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * 将大字段写一个小文件
	 *
	 * @param lobs_path      大字段文件路径
	 * @param lobs_file_name 大字段文件名称
	 * @param readerToByte   数据
	 */
	private void writeLobsFileToOracle(String lobs_path, String lobs_file_name, byte[] readerToByte) {
		FileOutputStream fos = null;
		BufferedOutputStream output = null;
		try {
			File file = new File(FileNameUtils.normalize(lobs_path + File.separator
					+ lobs_file_name, true));
			fos = new FileOutputStream(file);
			// 实例化OutputStream 对象
			output = new BufferedOutputStream(fos);
			output.write(readerToByte, 0, readerToByte.length);
		} catch (Exception e) {
			throw new AppSystemException("大字段输出文件流时抛异常，filePath={}");
		} finally {
			try {
				if (output != null)
					output.close();
				if (fos != null)
					fos.close();
			} catch (IOException e0) {
				LOGGER.error("关闭流异常", e0);
			}
		}
	}

	/**
	 * 源表包含BLOB、VARBINARY类型获取写一个avro文件的文件输出流
	 *
	 * @param typeArray  所有采集字段的类型的数组
	 * @param hbase_name 采集到目的地的表名
	 * @param midName    采集特殊字段合成arvo文件生成的目录
	 * @param pageNum    线程编号
	 * @return 文件输出流
	 * @throws IOException 获取avro文件输出流异常
	 */
	protected DataFileWriter<Object> getAvroWriter(int[] typeArray, String hbase_name,
												   String midName, long pageNum) throws IOException {
		DataFileWriter<Object> avroWriter = null;
		for (int type : typeArray) {
			//TODO 哪些是大字段这里要支持配置
			if (type == Types.BLOB || type == Types.VARBINARY || type == Types.LONGVARBINARY) {
				createLobsDir(midName);
				// 生成Avro文件到local
				OutputStream outputStream = new FileOutputStream(midName + "LOB" + File.separator
						+ "avro_" + hbase_name + pageNum);
				avroWriter = new DataFileWriter<>(new GenericDatumWriter<>()).setSyncInterval(100);
				avroWriter.setCodec(CodecFactory.snappyCodec());
				avroWriter.create(SCHEMA, outputStream);
				break;
			}
		}
		return avroWriter;
	}

	/**
	 * 创建LOB文件夹路径
	 *
	 * @param midName 文件夹路径
	 */
	private void createLobsDir(String midName) {
		//创建文件夹,写Avro文件
		File file = new File(midName + "LOB");
		if (!file.exists()) {
			boolean mkdirs = file.mkdirs();
			LOGGER.info("创建文件夹" + midName + "LOB" + mkdirs);
		}
		//创建文件夹,写多个lobs文件
		File file2 = new File(midName + "LOBS");
		if (!file2.exists()) {
			boolean mkdirs = file2.mkdirs();
			LOGGER.info("创建文件夹" + midName + "LOBS" + mkdirs);
		}
	}

	/**
	 * 源表包含BLOB、VARBINARY类型获取写一个avro文件的文件输出流
	 *
	 * @param typeMap    所有采集字段的类型的集合
	 * @param hbase_name 采集到目的地的表名
	 * @param midName    采集特殊字段合成arvo文件生成的目录
	 * @param pageNum    线程编号
	 * @return 文件输出流
	 * @throws IOException 获取avro文件输出流异常
	 */
	protected DataFileWriter<Object> getAvroWriter(Map<String, Integer> typeMap, String hbase_name,
												   String midName, long pageNum) throws IOException {
		DataFileWriter<Object> avroWriter = null;
		for (String key : typeMap.keySet()) {
			Integer type = typeMap.get(key);
			if (type == Types.BLOB || type == Types.VARBINARY || type == Types.LONGVARBINARY) {
				createLobsDir(midName);
				// 生成Avro文件到local
				OutputStream outputStream = new FileOutputStream(midName + "LOB" + File.separator
						+ "avro_" + hbase_name + pageNum);
				avroWriter = new DataFileWriter<>(new GenericDatumWriter<>()).setSyncInterval(100);
				avroWriter.setCodec(CodecFactory.snappyCodec());
				avroWriter.create(SCHEMA, outputStream);
				break;
			}
		}
		return avroWriter;
	}

	/**
	 * toMD5:生成MD5值. <br/>
	 *
	 * @author Cool Yu
	 * @since JDK 1.6
	 */
	protected String toMD5(String plainText) {
		return MD5Util.md5String(plainText);
	}

	/**
	 * 字段变为定长
	 */
	public static String columnToFixed(String columnValue, int length, String database_code, String column_name) {
		StringBuilder sb;
		try {
			byte[] bytes = columnValue.getBytes(DataBaseCode.ofValueByCode(database_code));
			int columnValueLength = bytes.length;
			sb = new StringBuilder();
			sb.append(columnValue);
			if (length >= columnValueLength) {
				for (int j = 0; j < length - columnValueLength; j++) {
					sb.append(' ');
				}
			} else {
				throw new AppSystemException(column_name + "字段定长指定的长度小于源数据长度；字段自定长度为：" + length
						+ ",实际值长度为：" + columnValueLength);
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new AppSystemException("定长文件卸数编码读取错误", e);
		}
	}

	/**
	 * 将string的集合转为int集合
	 */
	protected List<Integer> stringToIntegerList(List<String> list) {
		List<Integer> integerList = new ArrayList<>();
		for (String string : list) {
			integerList.add(Integer.parseInt(string));
		}
		return integerList;
	}

	/**
	 * 查询是否选择了拉链字段，如果有，则不做任何操作，没有，则全部key的值变为true
	 */
	protected Map<String, Boolean> transMd5ColMap(Map<String, Boolean> md5ColMap) {
		Map<String, Boolean> map = new HashMap<>();
		boolean flag = true;
		for (String key : md5ColMap.keySet()) {
			if (md5ColMap.get(key)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			for (String key : md5ColMap.keySet()) {
				map.put(key, true);
			}
		} else {
			map = md5ColMap;
		}
		return map;
	}
}
