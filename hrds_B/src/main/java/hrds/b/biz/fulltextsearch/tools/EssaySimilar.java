package hrds.b.biz.fulltextsearch.tools;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.utils.ocr.OcrExtractText;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.mapred.FsInput;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@DocClass(desc = "文章相似查询类", author = "BY-HLL", createdate = "2019/10/10 0010 上午 10:03")
public class EssaySimilar {

	private static final Logger logger = LogManager.getLogger();

	@Method(desc = "从solr中获取相似的文章", logicStep = "从solr中获取相似的文章")
	@Param(name = "filePath", desc = "文件在HDFS上的路径", range = "HDFS文件全路径")
	@Param(name = "similarityRate", desc = "文章相似率", range = "String类型的字符串0-1", valueIfNull = "1")
	@Param(name = "isFlag", desc = "是否返回检索到文章的文本内容", range = "IsFlag代码项")
	@Return(desc = "solr获取到的相似文章的结果集", range = "无限制")
	public Result getDocumentSimilarFromSolr(String filePath, String similarityRate, IsFlag is_return_text) {
		//记录文件在hdfs上的地址
		double dblRate = NumberUtils.toDouble(similarityRate);
		if (NumberUtils.compare(dblRate, 1) == 1) {
			dblRate = 1;
		}
		if (NumberUtils.compare(dblRate, 0) == -1) {
			dblRate = 0;
		}
		if (is_return_text == IsFlag.Shi) {
			logger.debug("返回检索到的文本内容!");
		}
		if (!StringUtil.isBlank(filePath)) {
			throw new BusinessException("从solr中获取相似的文章的方法未实现！");
		}
		return null;
	}

	@Method(desc = "从Avro获取文件摘要", logicStep = "从Avro获取文件摘要")
	@Param(name = "filePath", desc = "文件avro文件存储路径", range = "HDFS文件全路径，不为空")
	@Param(name = "blockId", desc = "avro文件存储的文件blockId", range = "numeric 长度15 不为空")
	@Param(name = "fileId", desc = "文件唯一id", range = "字符串类型 String 长度40 不为空")
	@Return(desc = "solr获取到的相似文章的结果集", range = "无限制")
	public String getFileSummaryFromAvro(String filePath, String blockId, String fileId) {
		String summary = "";
		if (!StringUtil.isBlank(filePath) && !StringUtil.isBlank(blockId) && !StringUtil.isBlank(fileId)) {
			//TODO hadoop平台暂未实现多存储层,目前取conf目录下的配置文件对应的集群
			try (HdfsOperator hdfsOperator = new HdfsOperator()) {
				Path path = new Path(filePath);
				SeekableInput in = new FsInput(path, hdfsOperator.conf);
				DatumReader<GenericRecord> reader = new GenericDatumReader<>();
				DataFileReader<GenericRecord> fileReader = new DataFileReader<>(in, reader);
				GenericRecord record = new GenericData.Record(fileReader.getSchema());
				//指定block的id
				fileReader.seek(Long.parseLong(blockId));
				//遍历每个地址中的块
				if (fileReader.hasNext()) {
					GenericRecord grnext = fileReader.next(record);
					String name = grnext.get("file_name").toString();
					//判断是否为ocr能识别的文件类型,如果是,获取ocr识别后的文本和摘要,不是获取file_summary
					if (OcrExtractText.isOcrFile(name)) {
						String ocrpath = path.getParent() + "/ocravro/" + path.getName() + "_zw";
						SeekableInput ocrin = new FsInput(new Path(ocrpath), hdfsOperator.conf);
						DatumReader<GenericRecord> ocrreader = new GenericDatumReader<>();
						DataFileReader<GenericRecord> ocrfileReader = new DataFileReader<>(ocrin, ocrreader);
						GenericRecord ocrrecord = new GenericData.Record(ocrfileReader.getSchema());
						while (ocrfileReader.hasNext()) {
							GenericRecord ocrgrnext = ocrfileReader.next(ocrrecord);
							if (ocrgrnext.get("uuid").toString().equals(fileId)) {
								summary = ocrgrnext.get("file_summary").toString();
								break;
							}
						}
					} else {
						summary = grnext.get("file_summary").toString();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new BusinessException("获取文件摘要失败!" + e.getMessage());
			}
		}
		return summary;
	}
}
