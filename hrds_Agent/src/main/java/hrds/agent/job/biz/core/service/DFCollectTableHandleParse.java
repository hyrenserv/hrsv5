package hrds.agent.job.biz.core.service;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.StorageType;
import hrds.commons.entity.Column_split;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import hrds.commons.utils.xlstoxml.Xls2xml;
import hrds.commons.utils.xlstoxml.util.ColumnMeta;

import java.util.List;
import java.util.Map;

@DocClass(desc = "根据页面所选的表和字段对jdbc所返回的meta信息进行解析", author = "zxz", createdate = "2019/12/4 11:17")
public class DFCollectTableHandleParse extends AbstractCollectTableHandle {

	@SuppressWarnings("unchecked")
	@Method(desc = "根据数据源信息和采集表信息得到卸数元信息", logicStep = "" +
			"1、根据数据源信息和采集表信息抽取SQL" +
			"2、根据数据源信息和抽取SQL，执行SQL，获取")
	@Param(name = "sourceDataConfBean", desc = "数据库采集,DB文件采集数据源配置信息", range = "不为空")
	@Param(name = "collectTableBean", desc = "数据库采集表配置信息", range = "不为空")
	@Return(desc = "卸数阶段元信息", range = "不为空")
	public TableBean generateTableInfo(SourceDataConfBean sourceDataConfBean,
	                                   CollectTableBean collectTableBean) {
		TableBean tableBean = new TableBean();
		String plane_url = sourceDataConfBean.getPlane_url();
		//获取数据字典所在目录文件，根据数据字典计算xml文件名称
		String xmlName = Math.abs(plane_url.hashCode()) + ".xml";
		//2.DB文件采集将数据字典dd_data.xls转为xml
		toXml(plane_url, Constant.XMLPATH + xmlName);
		//获取页面选择的需要采集的文件格式
		Data_extraction_def sourceData_extraction_def = collectTableBean.getSourceData_extraction_def();
		tableBean.setFile_format(sourceData_extraction_def.getDbfile_format());
		tableBean.setIs_header(sourceData_extraction_def.getIs_header());
		tableBean.setRow_separator(sourceData_extraction_def.getRow_separator());
		tableBean.setColumn_separator(sourceData_extraction_def.getDatabase_separatorr());
		tableBean.setRoot_path(sourceData_extraction_def.getPlane_url());
		tableBean.setFile_code(sourceData_extraction_def.getDatabase_code());
		StringBuilder columnMetaInfo = new StringBuilder();//生成的元信息列名
		StringBuilder allColumns = new StringBuilder();//要采集的列名
		StringBuilder colTypeMetaInfo = new StringBuilder();//生成的元信息列类型
		StringBuilder allType = new StringBuilder();//要采集的列类型
		StringBuilder colLengthInfo = new StringBuilder();//生成的元信息列长度
		//3.读取xml获取数据字典下所有的表信息,找到当前线程对应需要采集表的数据字典，获取表结构(注数据字典中的是有序的)
		List<String> cols = ColumnMeta.getColumnList(collectTableBean.getTable_name(),
				Constant.XMLPATH + xmlName);
		//遍历集合
		for (String col : cols) {
			List<String> colList = StringUtil.split(col, STRSPLIT);
			allType.append(colList.get(3)).append(STRSPLIT);
			colTypeMetaInfo.append(colList.get(3)).append(STRSPLIT);
			allColumns.append(colList.get(0)).append(STRSPLIT);
			columnMetaInfo.append(colList.get(0)).append(STRSPLIT);
			colLengthInfo.append(colList.get(2)).append(STRSPLIT);
		}
		if (colLengthInfo.length() > 0) {
			colLengthInfo.delete(colLengthInfo.length() - 1, colLengthInfo.length());
			allType.delete(allType.length() - 1, allType.length());
			allColumns.delete(allColumns.length() - 1, allColumns.length());
			colTypeMetaInfo.delete(colTypeMetaInfo.length() - 1, colTypeMetaInfo.length());
			columnMetaInfo.delete(columnMetaInfo.length() - 1, columnMetaInfo.length());
		}
		//根据清洗规则，进行表结构重组
		//清洗配置
		Map<String, Object> parseJson = parseJson(collectTableBean);
		Map<String, Map<String, Column_split>> splitIng = (Map<String, Map<String, Column_split>>) parseJson.get("splitIng");//字符拆分
		Map<String, String> mergeIng = (Map<String, String>) parseJson.get("mergeIng");//字符合并
		//更新拆分和合并的列信息
		String colMeta = updateColumn(mergeIng, splitIng, columnMetaInfo, colTypeMetaInfo, colLengthInfo);
		columnMetaInfo.delete(0, columnMetaInfo.length()).append(colMeta);
		//仅登记，海云不加任何字段
		if (IsFlag.Fou.getCode().equals(collectTableBean.getIs_register())) {
			//这里是根据不同存储目的地会有相同的拉链方式，则这新增拉链字段在这里增加
			columnMetaInfo.append(STRSPLIT).append(Constant.SDATENAME);
			colTypeMetaInfo.append(STRSPLIT).append("char(8)");
			colLengthInfo.append(STRSPLIT).append("8");
			//增量进数方式
			if (StorageType.ZengLiang.getCode().equals(collectTableBean.getStorage_type())) {
				columnMetaInfo.append(STRSPLIT).append(Constant.EDATENAME).append(STRSPLIT).append(Constant.MD5NAME);
				colTypeMetaInfo.append(STRSPLIT).append("char(8)").append(STRSPLIT).append("char(32)");
				colLengthInfo.append(STRSPLIT).append("8").append(STRSPLIT).append("32");
			}
		}
		// 页面定义的清洗格式进行卸数
		tableBean.setAllColumns(allColumns.toString());
		tableBean.setAllType(allType.toString());
		tableBean.setColLengthInfo(colLengthInfo.toString());
		tableBean.setColTypeMetaInfo(colTypeMetaInfo.toString());
		tableBean.setColumnMetaInfo(columnMetaInfo.toString().toUpperCase());
		tableBean.setParseJson(parseJson);
		//返回表结构信息
		return tableBean;
	}

	/**
	 * 将数据字典文件格式转为xml格式
	 *
	 * @param plane_url 数据字典文件全路径
	 * @param xmlName   xml文件名称
	 */
	private void toXml(String plane_url, String xmlName) {
		//根据文件全路径，判断是json还是excel格式，转为xml,进行读取
		if (plane_url.endsWith("json")) {
			Xls2xml.jsonToXml(plane_url, xmlName);
		} else if (plane_url.endsWith("xls")) {
			Xls2xml.XlsToXml(plane_url, xmlName);
		} else {
			throw new AppSystemException("数据字典的文件格式不正确");
		}
	}

	public static void main(String[] args) {
		String p = "D:\\data\\dd_data.json";
		//获取数据字典所在目录文件，根据数据字典计算xml文件名称
		String xmlName = Math.abs(p.hashCode()) + ".xml";
		//2.DB文件采集将数据字典dd_data.xls转为xml
		new DFCollectTableHandleParse().toXml(p, Constant.XMLPATH + xmlName);
		//3.读取xml获取数据字典下所有的表信息,找到当前线程对应需要采集表的数据字典，获取表结构
		List<String> cols = ColumnMeta.getColumnList("agent_info", Constant.XMLPATH + xmlName);
		System.out.println(cols.size());
	}
}