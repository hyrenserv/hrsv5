package hrds.commons.utils.xlstoxml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.UnloadType;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.entity.Table_column;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.ExcelUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.w3c.dom.Element;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Xls2xml {

	public static XmlCreater xmlCreater = null;
	public static Element table = null;
	public static Element root = null;
	public static Element column = null;
	public static Element storage = null;
	public static Element handleType = null;
	private static final Log logger = LogFactory.getLog(Xls2xml.class);
	private static final String NONEED_ = "NONEED_";

	private static Map<String, Integer> colType = new HashMap<String, Integer>();

	static {
//		String jsonType = "";
//		try {
//			jsonType = PropertyUtil.getMessage("fixedType");
//		}
//		catch(Exception e) {}
//		if( !ObjectUtil.isEmpty(jsonType) ) {
//			JSONObject jsonTypeObj = JSON.parseObject(jsonType);
//			Set<String> keys = jsonTypeObj.keySet();
//			for(String key : keys) {
//				colType.put(key, jsonTypeObj.getInteger(key));
//			}
//		}
//		JSONObject jsonTypeObj = null;
//		try {
//			String jsonType = PropertyUtil.getMessage("fixedType");
//			jsonTypeObj = JSON.parseObject(jsonType);
//		} catch (Exception e) {
//			logger.info("配置文件中加载不到取默认值");
//		}
/*		if (null != jsonTypeObj) {
			Set<String> keys = jsonTypeObj.keySet();
			for (String key : keys) {
				colType.put(key, jsonTypeObj.getInteger(key));
			}
		} else*/
		{
			colType.put("INTEGER", 12);
			colType.put("BIGINT", 22);
			colType.put("SMALLINT", 8);
			colType.put("DOUBLE", 24);
			colType.put("REAL", 16);
			colType.put("TIMESTAMP", 14);
			colType.put("DATE", 8);
			colType.put("LONGVARCHAR", 4000);
			colType.put("CLOB", 4000);
			colType.put("BLOB", 4000);
			colType.put("DECFLOAT", 34);

			colType.put("VARCHAR", 0);
			colType.put("CHARACTER", 0);
			colType.put("DECIMAL", 0);
		}
	}

	public static String getheadCellValue(Cell cell) {

		String cellvalue = "";
		if (cell == null) {
			return cellvalue;
		}
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC: // 数字
				cellvalue = Double.toString(cell.getNumericCellValue()).trim();
				break;
			case Cell.CELL_TYPE_STRING: // 字符串
				cellvalue = cell.getStringCellValue().trim();
				break;
			case Cell.CELL_TYPE_BOOLEAN: // Boolean
				cellvalue = Boolean.toString(cell.getBooleanCellValue()).trim();
				break;
			case Cell.CELL_TYPE_FORMULA: // 公式
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cellvalue = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_BLANK: // 空值
				cellvalue = "";
				break;
			case Cell.CELL_TYPE_ERROR: // 故障
				cellvalue = "error";
				break;
			default:
				cellvalue = "unknown value";
				break;
		}
		return cellvalue;
	}

	public static void toXml(String db_path, String xml_path) {

		String path_cd = pathToUnEscape(db_path);
		File file = FileUtils.getFile(path_cd);
		if (file.exists()) {
			if (db_path.toUpperCase().endsWith("JSON") || db_path.endsWith("")) {
				jsonToXml(db_path, xml_path);
			} else if (db_path.toUpperCase().endsWith("XLS") || db_path.toUpperCase().endsWith("XLSX")) {
				XlsToXml(db_path, xml_path);
			} else {
				throw new BusinessException("请指定正确的数据字典文件！");
			}
		} else {
			throw new BusinessException("没有找到相应的数据字典定义文件！");
		}
	}

	public static void toXml2(String db_path, String xml_path) {

		db_path = pathToUnEscape(db_path + File.separator + "~dd_data.json");
		logger.info("采集文件路径：" + db_path);
		File file = FileUtils.getFile(db_path);
		if (file.exists()) {
			jsonToXml2(db_path, xml_path);
		} else {
			throw new BusinessException("没有找到相应的数据字典定义文件！");
		}
	}

	/**
	 * 通过json格式数据字典生成xml（半结构化采集）
	 *
	 * @param json_path json格式数据字典目录
	 * @param xml_path  生成xml文件目录
	 */
	public static void jsonToXml2(String json_path, String xml_path) {
		// 调用方法生成xml文件
		createXml(xml_path);
		BufferedReader br = null;
		try {
			StringBuilder result = new StringBuilder();
			// 构造一个BufferedReader类来读取文件
			br = new BufferedReader(new FileReader(json_path));
			String s;
			// 使用readLine方法，一次读一行并换行
			while ((s = br.readLine()) != null) {
				result.append('\n').append(s);
			}
			JSONArray jsonArray = JSONArray.parseArray(result.toString());
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				// 表名
				String table_name = json.getString("table_name");
				// 中文表名
				String table_ch_name = json.getString("table_ch_name");
				// 数据更新方式
				String updatetype = json.getString("updatetype");
				// 表信息处理
				addTable(table_name.toLowerCase(), table_ch_name, updatetype);
				JSONObject handleType = json.getJSONObject("handle_type");
				// 数据处理类型
				addHandleType(handleType.getString("insert"), handleType.getString("update"),
					handleType.getString("delete"));
				// 列信息
				JSONArray columns = json.getJSONArray("columns");
				for (int j = 0; j < columns.size(); j++) {
					JSONObject column = columns.getJSONObject(j);
					// 列ID
					String column_id = column.getString("column_id");
					// 字段名
					String column_name = column.getString("column_name").toLowerCase();
					// 字段中文名
					String column_ch_name = column.getString("column_ch_name");
					// 字段类型
					String column_type = column.getString("column_type");
					// 备注信息
					String column_remark = column.getString("column_remark");
					// 字段位置
					String columnposition = column.getString("columnposition");
					// 是否操作标识表
					String is_operate = column.getString("is_operate");
					int length = getLength(column_type);
					// 列信息封装
					addColumnToSemiStructuredCollect(column_id, column_name, column_ch_name, column_type,
						column_remark, columnposition, is_operate);
				}
			}
			// 生成xml文档
			xmlCreater.buildXmlFile();
		} catch (FileNotFoundException e) {
			throw new BusinessException("文件不存在," + e.getMessage());
		} catch (IOException e) {
			throw new BusinessException("读取文件失败," + e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	public static void addColumnToSemiStructuredCollect(String column_id, String column_name,
		String column_ch_name, String column_type,
		String column_remark, String columnposition,
		String is_operate) {

		column = xmlCreater.createElement(table, "columns");
		xmlCreater.createAttribute(column, "column_id", column_id);
		xmlCreater.createAttribute(column, "column_name", column_name);
		xmlCreater.createAttribute(column, "column_ch_name", column_ch_name);
		xmlCreater.createAttribute(column, "column_type", column_type);
		xmlCreater.createAttribute(column, "column_remark", column_remark);
		xmlCreater.createAttribute(column, "columnposition", columnposition);
		xmlCreater.createAttribute(column, "is_operate", is_operate);
	}

	public static void addHandleType(String insert, String update, String delete) {

		handleType = xmlCreater.createElement(table, "handle_type");
		xmlCreater.createAttribute(handleType, "insert", insert);
		xmlCreater.createAttribute(handleType, "update", update);
		xmlCreater.createAttribute(handleType, "delete", delete);
	}

	/**
	 * 根据系统将~修改成对应路径地址
	 *
	 * @param path 文件路径
	 * @return 转换后的路径
	 */
	private static String pathToUnEscape(String path) {

		if (SystemUtils.OS_NAME.toLowerCase().contains("win")) {
			return StringUtil.replace(path, "~", "\\");
		} else {
			return StringUtil.replace(path, "~", "/");
		}
	}

	public static void jsonToXml(String json_path, String xml_path) {

		InputStream xlsFileInputStream = null;
		createXml(xml_path);// 调用方法生成xml文件
		String info = "";
		BufferedReader br = null;
		try {
			StringBuilder result = new StringBuilder();
			br = new BufferedReader(new FileReader(json_path));//构造一个BufferedReader类来读取文件
			String s;
			while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
				result.append('\n').append(s);
			}
			JSONArray jsonArray = JSONArray.parseArray(result.toString());
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				String table_name = json.getString("table_name");//表名
				String table_cn_name = json.getString("table_ch_name");//中文表名
				String unload_type = json.getString("unload_type");//数据存储方式
				String insertColumnInfo = json.getString("insertColumnInfo");
				String updateColumnInfo = json.getString("updateColumnInfo");
				String deleteColumnInfo = json.getString("deleteColumnInfo");
				addTable(table_name, table_cn_name, unload_type, insertColumnInfo, updateColumnInfo, deleteColumnInfo);
				JSONArray columns = json.getJSONArray("columns");//列信息
				for (int j = 0; j < columns.size(); j++) {
					JSONObject column = columns.getJSONObject(j);
					Table_column table_column = JSON.toJavaObject(column, Table_column.class);
					table_column.setTc_remark(column.getString("column_remark"));//备注信息
					addColumn(table_column);
				}
				JSONArray storages = json.getJSONArray("storage");
				for (int j = 0; j < storages.size(); j++) {
					JSONObject storageJson = storages.getJSONObject(j);
					Data_extraction_def extraction_def = JSON.toJavaObject(storageJson, Data_extraction_def.class);
					addStorage(extraction_def);
				}
			}
			xmlCreater.buildXmlFile();// 生成xml文档
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) {
				//如果继续错误，将已经身
				File file = new File(xml_path);
				if (file.exists()) {
					file.delete();
				}
			}
			logger.info(info + "json定义错误数据错误");
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(xlsFileInputStream);
		}
	}

	/**
	 * 将xls转换成xml
	 *
	 * @param xls_path {@link String} xls的路径
	 * @param xml_path {@link String} 吐出xml的路径
	 */
	public static void XlsToXml(String xls_path, String xml_path) {

		createXml(xml_path);// 调用方法生成xml文件
		Workbook workbookFromExcel = null;
		try {
			File file = new File(xls_path);
			workbookFromExcel = ExcelUtil.getWorkbookFromExcel(file);
			//表的数据信息在第一个Sheet页,因为Excel的Sheet页码是从0开始的
			Sheet sheetAt = workbookFromExcel.getSheetAt(1);
			int lastRowNum = sheetAt.getLastRowNum();
			//遍历所有的有行数
			for (int i = 0; i < lastRowNum; i++) {
				//获取当前行对象
				Row row = sheetAt.getRow(i);
				//循环找出每个格子的数据信息,如果第一个格子的值是 "英文表名",说明是表的开始
				String cellValue = ExcelUtil.getValue(row.getCell(0)).toString();
				if (StringUtil.isNotBlank(cellValue)) {
					//匹配到英文表名标签,则开始写入xml的table信息
					if (cellValue.equals("英文表名")) {
						//下一行必定是参数信息
						writeTable2Xml(i + 1, sheetAt.getRow(i + 1));
					}
					//这里是列标签的开始,写入当前表的列信息
					if (cellValue.equals("序号")) {
						//下一行必定是列参数信息
						writeColumn2Xml(lastRowNum, sheetAt, i + 1);
					}
				}
			}
			xmlCreater.buildXmlFile();// 生成xml文档
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) {
				//如果继续错误，将已经生成的删除
				File file = new File(xml_path);
				if (file.exists()) {
					file.delete();
				}
			}
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (workbookFromExcel != null) {
					workbookFromExcel.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	static void writeTable2Xml(int rowNum, Row rowData) {
		//英文表名
		String table_name = ExcelUtil.getValue(rowData.getCell(0)).toString();
		Validator.notBlank(table_name, "数据字典( " + rowNum + " )行的英文表名为空,请检查");
		//中文表名
		String table_cn_name = ExcelUtil.getValue(rowData.getCell(1)).toString();
		Validator.notBlank(table_cn_name, "数据字典( " + rowNum + " )行的中文表名为空,请检查");
		//将表的信息写入Xml
		addTable(table_name.toLowerCase(), table_cn_name, UnloadType.QuanLiangXieShu.getCode());

		//数据抽取定义信息
		Data_extraction_def extraction_def = new Data_extraction_def();
		// 数据编码
		String database_code = ExcelUtil.getValue(rowData.getCell(2)).toString();
		Validator.notBlank(database_code, "数据字典( " + rowNum + " )行的数据编码为空,请检查");
		database_code = DataBaseCode.getCodeByValue(database_code);
		extraction_def.setDatabase_code(database_code);
		// 行分隔符
		String row_separator = ExcelUtil.getValue(rowData.getCell(3)).toString();
		Validator.notBlank(row_separator, "数据字典( " + rowNum + " )行的行分隔符为空,请检查");
		row_separator = StringUtil.string2Unicode(row_separator);
		extraction_def.setRow_separator(row_separator);
		// 是否有表头
		String is_header = ExcelUtil.getValue(rowData.getCell(4)).toString();
		Validator.notBlank(is_header, "数据字典( " + rowNum + " )行的是否有表头为空,请检查");
		is_header = IsFlag.getCodeByValue(is_header);
		extraction_def.setIs_header(is_header);
		// 数据文件格式
		String dbfile_format = ExcelUtil.getValue(rowData.getCell(5)).toString();
		Validator.notBlank(dbfile_format, "数据字典( " + rowNum + " )行的数据文件格式为空,请检查");
		dbfile_format = FileFormat.getCodeByValue(dbfile_format);
		extraction_def.setDbfile_format(dbfile_format);
		// 列分隔符
		String database_separatorr = ExcelUtil.getValue(rowData.getCell(6)).toString();
		Validator.notBlank(database_separatorr, "数据字典( " + rowNum + " )行的列分隔符为空,请检查");
		database_separatorr = StringUtil.string2Unicode(database_separatorr);
		extraction_def.setDatabase_separatorr(database_separatorr);
		// 数据文件存放路径
		String plane_url = ExcelUtil.getValue(rowData.getCell(7)).toString();
		Validator.notBlank(plane_url, "数据字典( " + rowNum + " )行的数据文件存放路径为空,请检查");
		extraction_def.setPlane_url(plane_url);
		//写入表的存储信息到Xml
		addStorage(extraction_def);
	}

	static void writeColumn2Xml(int lastRowNum, Sheet sheet, int rowNum) {

		for (int i = rowNum; i <= lastRowNum; i++) {
			Row rowData = sheet.getRow(i);
			String cellValue = ExcelUtil.getValue(rowData.getCell(0)).toString();
			//匹配到的标签为英文表名时,说明是新的表开始
			if (cellValue.equals("英文表名")) {
				break;
			}
			Table_column table_column = new Table_column();
			// 字段英文名
			String column_name = ExcelUtil.getValue(rowData.getCell(1)).toString();
			Validator.notBlank(column_name, "数据字典( " + i + " )行的字段英文名为空,请检查");
			table_column.setColumn_name(column_name);
			// 字段中文名
			String column_cn_name = ExcelUtil.getValue(rowData.getCell(2)).toString();
			Validator.notBlank(column_cn_name, "数据字典( " + i + " )行的字段中文名为空,请检查");
			table_column.setColumn_ch_name(column_cn_name);
			// 数据类型
			String column_type = ExcelUtil.getValue(rowData.getCell(3)).toString();
			Validator.notBlank(column_type, "数据字典( " + i + " )行的数据类型为空,请检查");
			table_column.setColumn_type(column_type);
			// 键值
			String is_primary_key = ExcelUtil.getValue(rowData.getCell(4)).toString();
			Validator.notBlank(is_primary_key, "数据字典( " + i + " )行的键值为空,请检查");
			is_primary_key = IsFlag.getCodeByValue(is_primary_key);
			table_column.setIs_primary_key(is_primary_key);
			// 空值
			String column_remark = ExcelUtil.getValue(rowData.getCell(5)).toString();
			Validator.notBlank(column_remark, "数据字典( " + i + " )行的空值信息为空,请检查");
			column_remark = IsFlag.getCodeByValue(column_remark);
			table_column.setTc_remark(column_remark);
			// 是否为拉链字段
			String is_zipper_field = ExcelUtil.getValue(rowData.getCell(6)).toString();
			Validator.notBlank(is_zipper_field, "数据字典( " + i + " )行的拉链字段为空,请检查");
			is_zipper_field = IsFlag.getCodeByValue(is_zipper_field);
			table_column.setIs_zipper_field(is_zipper_field);
			table_column.setIs_alive(IsFlag.Shi.getCode());
			table_column.setIs_get(IsFlag.Shi.getCode());
			table_column.setIs_new(IsFlag.Fou.getCode());
			//将列信息写入当前表的Xml下面
			addColumn(table_column);
		}
	}

	/**
	 * 从字符串首位开始获取指定字符串的指定长度的子串，不考虑中文情况造成的字符串长度变化问题
	 *
	 * @param sourceString 源字符串
	 * @param maxLength    截取最大长度
	 * @return 剪切后的结果，如果源字符串为null，返回空串
	 */
	public static String subString(String sourceString, int maxLength) {

		String innerSourceString = sourceString;
		if (null == sourceString) {//如果为null，返回空串

			innerSourceString = "";
		}
		String endString = "";
		int trueLength = innerSourceString.length();
		if (trueLength > maxLength) {//实际长度大于需要的长度
			endString = innerSourceString.substring(0, maxLength);
		} else {
			endString = innerSourceString;
		}
		return endString;
	}

	public static void createXml(String path) {

		xmlCreater = new XmlCreater(path);
		root = xmlCreater.createRootElement("database");
		xmlCreater.createAttribute(root, "xmlns", "http://db.apache.org/ddlutils/schema/1.1");
		xmlCreater.createAttribute(root, "name", "dict_params");
	}

	public static void addTable(String en_table_name, String cn_table_name, String unload_type) {

		table = xmlCreater.createElement(root, "table");
		xmlCreater.createAttribute(table, "table_name", en_table_name);
		xmlCreater.createAttribute(table, "table_ch_name", cn_table_name);
		xmlCreater.createAttribute(table, "unload_type", unload_type);
	}

	public static void addTable(String en_table_name, String cn_table_name, String unload_type
		, String insertColumnInfo, String updateColumnInfo, String deleteColumnInfo) {
		table = xmlCreater.createElement(root, "table");
		xmlCreater.createAttribute(table, "table_name", en_table_name);
		xmlCreater.createAttribute(table, "table_ch_name", cn_table_name);
		xmlCreater.createAttribute(table, "unload_type", unload_type);
		xmlCreater.createAttribute(table, "insertColumnInfo", insertColumnInfo);
		xmlCreater.createAttribute(table, "updateColumnInfo", updateColumnInfo);
		xmlCreater.createAttribute(table, "deleteColumnInfo", deleteColumnInfo);
	}

	public static void addColumn(Table_column table_column) {
		column = xmlCreater.createElement(table, "column");
		xmlCreater.createAttribute(column, "column_name", table_column.getColumn_name());
		xmlCreater.createAttribute(column, "column_ch_name", table_column.getColumn_ch_name());
		xmlCreater.createAttribute(column, "column_type", table_column.getColumn_type());
		xmlCreater.createAttribute(column, "is_primary_key", table_column.getIs_primary_key());
		xmlCreater.createAttribute(column, "column_remark", table_column.getTc_remark());
		xmlCreater.createAttribute(column, "is_get", table_column.getIs_get());
		xmlCreater.createAttribute(column, "is_alive", table_column.getIs_alive());
		xmlCreater.createAttribute(column, "is_new", table_column.getIs_new());
		xmlCreater.createAttribute(column, "is_zipper_field", table_column.getIs_zipper_field());
	}

	private static void addStorage(Data_extraction_def extraction_def) {
		storage = xmlCreater.createElement(table, "storage");
		xmlCreater.createAttribute(storage, "dbfile_format", extraction_def.getDbfile_format());
		xmlCreater.createAttribute(storage, "is_header", extraction_def.getIs_header());
		xmlCreater.createAttribute(storage, "row_separator", extraction_def.getRow_separator());
		xmlCreater.createAttribute(storage, "database_separatorr", extraction_def.getDatabase_separatorr());
		xmlCreater.createAttribute(storage, "plane_url", extraction_def.getPlane_url());
		xmlCreater.createAttribute(storage, "database_code", extraction_def.getDatabase_code());
	}

	/**
	 * 获取每个字典的长度
	 *
	 * @param column_type
	 * @return
	 */
	public static int getLength(String column_type) {
		column_type = column_type.trim();
		int length = colType.get(column_type.toUpperCase()) == null ? 0 : colType.get(column_type.toUpperCase());
		if (length == 0) {
			int start = column_type.indexOf("(");
			int end = column_type.indexOf(")");
			String substring = column_type.substring(start + 1, end);
			if (substring.indexOf(",") != -1) {
				return Integer.parseInt(StringUtil.split(substring, ",").get(0)) + 2;
			}
			return Integer.parseInt(substring);
		}
		return length;
	}

	public static void main(String[] args) {

		//System.out.println(getLength("varchar(50)"));
		jsonToXml("D:\\dd_data.json", "d:\\c11.xml");
	}
}
