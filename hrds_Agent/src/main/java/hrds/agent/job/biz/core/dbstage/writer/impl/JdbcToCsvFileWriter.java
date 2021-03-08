package hrds.agent.job.biz.core.dbstage.writer.impl;

import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.conf.Dbtype;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.dbstage.writer.AbstractFileWriter;
import hrds.agent.job.biz.dataclean.Clean;
import hrds.agent.job.biz.dataclean.CleanFactory;
import hrds.agent.job.biz.dataclean.DataCleanInterface;
import hrds.agent.job.biz.utils.WriterFile;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Column_split;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import org.apache.avro.file.DataFileWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.io.CsvListWriter;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JdbcToCsvFileWriter date: 2019/12/6 17:19 author: zxz
 */
public class JdbcToCsvFileWriter extends AbstractFileWriter {

	//打印日志
	private static final Logger log = LogManager.getLogger();

	public JdbcToCsvFileWriter(ResultSet resultSet, CollectTableBean collectTableBean, int pageNum,
		TableBean tableBean, Data_extraction_def data_extraction_def) {
		super(resultSet, collectTableBean, pageNum, tableBean, data_extraction_def);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String writeFiles() {
		String eltDate = collectTableBean.getEtlDate();
		StringBuilder fileInfo = new StringBuilder(1024);
		String hbase_name = collectTableBean.getHbase_name();
		//数据抽取指定的目录
		String plane_url = data_extraction_def.getPlane_url();
		String midName = plane_url + File.separator + eltDate + File.separator + collectTableBean.getTable_name()
			+ File.separator + Constant.fileFormatMap.get(FileFormat.CSV.getCode()) + File.separator;
		midName = FileNameUtils.normalize(midName, true);
		DataFileWriter<Object> avroWriter = null;
		CsvListWriter writer;
		long counter = 0;
		int index = 0;
		WriterFile writerFile = null;
		try {
			avroWriter = getAvroWriter(tableBean.getTypeArray(), hbase_name, midName, pageNum);
			//卸数文件名为hbase_name加线程唯一标识加此线程创建文件下标
			String fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
			String dataDelimiter = data_extraction_def.getDatabase_separatorr();
			fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
			writerFile = new WriterFile(fileName);
			writer = writerFile.getCsvWriter(DataBaseCode.ofValueByCode(data_extraction_def.getDatabase_code()));
			//清洗配置
			final DataCleanInterface allclean = CleanFactory.getInstance().getObjectClean("clean_database");
			//获取所有字段的名称，包括列分割和列合并出来的字段名称
			List<String> columnMetaInfoList = StringUtil.split(tableBean.getColumnMetaInfo(), Constant.METAINFOSPLIT);
			//写表头
			writeHeader(writer, columnMetaInfoList);
			//获取所有查询的字段的名称，不包括列分割和列合并出来的字段名称
			List<String> selectColumnList = StringUtil.split(tableBean.getAllColumns(), Constant.METAINFOSPLIT);
			Map<String, Object> parseJson = tableBean.getParseJson();
			//字符合并
			Map<String, String> mergeIng = (Map<String, String>) parseJson.get("mergeIng");
			//字符拆分
			Map<String, Map<String, Column_split>> splitIng = (Map<String, Map<String, Column_split>>)
				parseJson.get("splitIng");
			Clean cl = new Clean(parseJson, allclean);
			//获取所有列的值用来做列合并
			StringBuilder mergeStringTmp = new StringBuilder(1024 * 1024);
			//获取页面选择列算拉链时算md5的列，当没有选择拉链字段，默认使用全字段算md5
			Map<String, Boolean> md5Col = transMd5ColMap(tableBean.getIsZipperFieldInfo());
			StringBuilder md5StringTmp = new StringBuilder(1024 * 1024);
			List<Object> sb = new ArrayList<>(columnMetaInfoList.size());//用来写一行数据
			StringBuilder sb_ = new StringBuilder();//用来写临时数据
			List<String> typeList = StringUtil.split(tableBean.getAllType(),
				Constant.METAINFOSPLIT);
			int numberOfColumns = selectColumnList.size();
			log.info("type : " + typeList.size() + "  colName " + numberOfColumns);
			String currValue;
			int[] typeArray = tableBean.getTypeArray();
			while (resultSet.next()) {
				// Count it
				counter++;
				md5StringTmp.delete(0, md5StringTmp.length());
				mergeStringTmp.delete(0, mergeStringTmp.length());
				// Write columns
				for (int i = 0; i < numberOfColumns; i++) {
					//获取原始值来计算 MD5
					sb_.delete(0, sb_.length());
					log.info("开始处理列: " + selectColumnList.get(i) + " 列类型是: " + typeArray[i] + " 数据库类型是: " + collectTableBean
						.getDb_type());
					if (collectTableBean.getDb_type() != Dbtype.ODPS) {
						mergeStringTmp.append(getOneColumnValue(avroWriter, counter, pageNum, resultSet, typeArray[i],
							sb_, selectColumnList.get(i), hbase_name, midName));
					} else {
						mergeStringTmp.append(getOneColumnValue(avroWriter, counter, pageNum, resultSet, typeArray[i],
							sb_, selectColumnList.get(i), hbase_name, midName, i + 1));
					}
					// Add DELIMITER if not last value
					if (i < numberOfColumns - 1) {
						mergeStringTmp.append(Constant.DATADELIMITER);
					}
					currValue = sb_.toString();
					log.info("处理列: " + selectColumnList.get(i) + " 数据是: " + currValue);
					//判断是否是算md5的列，算md5
					if (md5Col.get(selectColumnList.get(i)) != null && md5Col.get(selectColumnList.get(i))) {
						md5StringTmp.append(currValue);
					}
					//清洗操作
					currValue = cl.cleanColumn(currValue, selectColumnList.get(i).toUpperCase(), null,
						typeList.get(i), FileFormat.CSV.getCode(), sb,
						data_extraction_def.getDatabase_code(), dataDelimiter);
					if (splitIng.get(selectColumnList.get(i).toUpperCase()) == null
						|| splitIng.get(selectColumnList.get(i).toUpperCase()).size() == 0) {
						sb.add(currValue);
					}
				}
				//如果有列合并处理合并信息
				if (!mergeIng.isEmpty()) {
					List<String> arrColString = StringUtil.split(mergeStringTmp.toString(),
						Constant.DATADELIMITER);
					allclean.merge(mergeIng, arrColString.toArray(new String[0]), selectColumnList.toArray
							(new String[0]), null, sb,
						FileFormat.CSV.getCode(), data_extraction_def.getDatabase_code(), dataDelimiter);
				}
				sb.add(eltDate);
				//根据是否算MD5判断是否追加结束日期和MD5两个字段
				if (IsFlag.Shi.getCode().equals(collectTableBean.getIs_md5())) {
					String md5 = toMD5(md5StringTmp.toString());
					sb.add(Constant.MAXDATE);
					sb.add(md5);
				}
				//拼接操作时间、操作日期、操作人
				appendOperateInfo(sb);
				if (JobConstant.WriteMultipleFiles) {
					long messageSize = sb.toString().length();
					long singleFileSize = new File(fileName).length();
					if (singleFileSize + messageSize > JobConstant.FILE_BLOCKSIZE) {
						//当文件满足阈值时 ，然后关闭当前流，并创建新的数据流
						writerFile.csvClose();
						index++;
						fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
						writerFile = new WriterFile(fileName);
						writer = writerFile.getCsvWriter(DataBaseCode.ofValueByCode(
							data_extraction_def.getDatabase_code()));
						//写表头
						writeHeader(writer, columnMetaInfoList);
						fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
					}
				}
				writer.write(sb);
				if (counter % JobConstant.BUFFER_ROW == 0) {
					log.info("正在写入文件，已写入" + counter + "行");
					writer.flush();
				}
				sb.clear();
			}
			writer.flush();
		} catch (Exception e) {
			log.error("卸数失败", e);
			throw new AppSystemException("数据库采集卸数Csv文件失败", e);
		} finally {
			try {
				if (writerFile != null) {
					writerFile.csvClose();
				}
				if (avroWriter != null) {
					avroWriter.close();
				}
			} catch (IOException e) {
				log.error(e);
			}
		}
		fileInfo.append(counter);
		//返回卸数一个或者多个文件名全路径和总的文件行数
		return fileInfo.toString();
	}

	/**
	 * 添加操作日期、操作时间、操作人
	 */
	private void appendOperateInfo(List<Object> sb) {
		if (JobConstant.ISADDOPERATEINFO) {
			sb.add(operateDate);
			sb.add(operateTime);
			sb.add(user_id);
		}
	}

	/**
	 * 根据页面传过来的参数，决定是否写表头
	 *
	 * @param csvListWriter      csv的写文件的输出流
	 * @param columnMetaInfoList 所有字段的列
	 */
	private void writeHeader(CsvListWriter csvListWriter, List<String> columnMetaInfoList) throws Exception {
		if (IsFlag.Shi.getCode().equals(data_extraction_def.getIs_header())) {
			csvListWriter.write(columnMetaInfoList);
		}
	}

}
