package hrds.commons.utils.ocr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.summary.TextRankSentence;
import fd.ng.core.utils.StringUtil;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.hadoop.solr.ISolrOperator;
import hrds.commons.hadoop.solr.SolrFactory;
import hrds.commons.utils.CommonVariables;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OCR 文本提取类
 *
 * @author BY-HLL
 * @create: 2021年4月9日09:27:07
 */
public class OcrExtractText implements Closeable {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * OCR 可以提取文本的预期扩展名列表
	 */
	private static final List<String> OCR_EXPECTED_EXTENSION_S = Arrays.asList("pdf", "bmp", "png", "tif", "jpg", "");

	/**
	 * avro 的 SCHEMA
	 */
	public static Schema SCHEMA;

	/**
	 * OCR 线程池大小
	 */
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(CommonVariables.OCR_THREAD_POOL);

	/**
	 * 图片提取对象
	 */
	private static PictureTextExtract pictureTextExtract;

	/**
	 * hdfs 操作对象
	 */
	private static HdfsOperator hdfsOperator;

	/**
	 * AVRO 文件块的父目录
	 */
	private String avroParentDir;

	/*
	 * 类加载初始化配置
	 */
	static {
		try {
			//初始化 avro 的 SCHEMA
			JSONObject SCHEMA_JSON = new JSONObject();
			SCHEMA_JSON.put("type", "record");
			SCHEMA_JSON.put("name", "solrAvroFile");
			JSONArray field_info_s = new JSONArray();
			JSONObject field_info = new JSONObject();
			field_info.put("name", "uuid");
			field_info.put("type", "string");
			field_info_s.add(field_info);
			field_info = new JSONObject();
			field_info.put("name", "file_text");
			field_info.put("type", "string");
			field_info_s.add(field_info);
			field_info = new JSONObject();
			field_info.put("name", "file_summary");
			field_info.put("type", "string");
			field_info_s.add(field_info);
			SCHEMA_JSON.put("fields", field_info_s);
			SCHEMA = new Schema.Parser().parse(String.valueOf(SCHEMA_JSON));
			//使用的是OCR RPC服务,初始化图片文本提取对象
			if (CommonVariables.USE_OCR_RPC) {
				pictureTextExtract = new PictureTextExtract();
			}
			hdfsOperator = new HdfsOperator();
			hdfsOperator.conf.set("fs.hdfs.impl.disable.cache", "false");
		} catch (Exception e) {
			logger.error("OCR 提取文字类初始化失败! " + e.getMessage());
		}
	}

	/**
	 * 有参构造
	 *
	 * @param avroParentDir AVRO 文件块的父目录
	 */
	public OcrExtractText(String avroParentDir) {

		this.avroParentDir = avroParentDir;
	}

	/**
	 * 利用ocr将avro中存储的文件提取文本和摘要，存放到solr中，并生成一一对应的的avro文件存储起来
	 */
	public void OCR2AvroAndSolr() {
		logger.info("OCR 提取文件的文本摘要并保存到Solr和Avro,开始");
		try {
			List<Path> paths = hdfsOperator.listFiles(avroParentDir, false);
			//循环遍历每一个avro文件
			for (Path path : paths) {
				fixedThreadPool.execute(new OcrTextTask(path));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取目录: " + avroParentDir + " 下的avro文件失败!", e.getMessage());
		} finally {
			try {
				fixedThreadPool.shutdown();
				fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				logger.error("关闭线程池失败! 异常" + e);
			}
		}
	}

	/**
	 * 判断该文件是否能被ocr识别
	 *
	 * @param fileName 带后缀的文件名
	 * @return 是否为ocr能识别的文件
	 */
	public static boolean isOcrFile(String fileName) {
		return OCR_EXPECTED_EXTENSION_S.contains(FilenameUtils.getExtension(fileName));
	}

	@Override
	public void close() {
		if (hdfsOperator != null) {
			hdfsOperator.close();
		}
	}

	/**
	 * OCR 文本提取跑批类
	 */
	class OcrTextTask implements Runnable {
		/**
		 * 获取工具对象
		 */
		private ITesseract instance = new Tesseract();

		/**
		 * avro 文件路径
		 */
		private Path avroPath;

		public OcrTextTask(Path avroPath) {
			this.avroPath = avroPath;
			//tessdata目录放在jar包同级目录
			instance.setDatapath(System.getProperty("user.dir"));
			//设置识别语言，默认简体中文(chi_sim)
			instance.setLanguage(CommonVariables.OCR_RECOGNITION_LANGUAGE);
		}

		/**
		 * 从一个 avro文件中提取数据读取文件，并提取成文本摘要
		 *
		 * @param fs FileSystem
		 * @return 读取并转换成的信息放入到一个list里面
		 */
		public List<GenericRecord> readRecordFromAvro(FileSystem fs) {

			List<GenericRecord> recordList = new ArrayList<>();
			try (InputStream is = fs.open(avroPath);
			     DataFileStream<Object> reader = new DataFileStream<>(is, new GenericDatumReader<>())) {
				logger.info("reader.size: " + reader.getMetaKeys().size());
				//循环遍历每一个
				while (reader.hasNext()) {
					GenericRecord r = (GenericRecord) reader.next();
					recordList.add(r);
				}
				return recordList;
			} catch (IOException e) {
				logger.error("Failed to get record from avro file ...", e);
				return null;
			}
		}

		/**
		 * 从记录列表中提取每一条记录的文本
		 *
		 * @param recordList 记录列表中
		 */
		public void extractTextFromRecord(List<GenericRecord> recordList) {

			//循环记录的每一条记录处理
			for (GenericRecord r : recordList) {
				String file_name = r.get("file_name").toString();
				logger.debug("待提取的文件名: " + file_name);
				//判断是否是 OCR 识别支持的文件类型,不是直接下一位
				if (!isOcrFile(file_name)) {
					continue;
				}
				//获取avro文件中的文件流 转化为对象，用ocr提取出文本文件
				ByteBuffer byteBuffer = (ByteBuffer) r.get("file_contents");
				byte[] bytes = byteBuffer.array();
				String filePath = Bytes.toString(bytes);
				logger.info("" + bytes.length);
				String result;
				ImageIO.scanForPlugins();
				if (r.get("is_big_file").toString().equals("2")) {//非大文件
					if (file_name.endsWith(".pdf")) {//pdf文件只能写到本地再调用ocr
						try {
							File file = new File(FileUtils.getTempDirectoryPath() + File.separator + file_name);
							FileUtils.writeByteArrayToFile(file, bytes);
							result = extractText(file);
							boolean wasSuccessful = file.delete();
							if (!wasSuccessful) {
								logger.warn("文件: " + file.getName() + " ,删除失败!");
							}
						} catch (IOException e) {
							logger.error("文件: " + file_name + " ,提取文本失败! 异常: " + e);
							throw new BusinessException("文件: " + file_name + " ,提取文本失败! 异常: " + e.getMessage());
						}
					} else {
						if (CommonVariables.USE_OCR_RPC) {//这里决定是否使用跑批方式
							result = pictureTextExtract.byteToStr(bytes);//rpc
						} else {
							result = extractText(bytes, file_name);//图片可以直接在内存中调用ocr (apache)
						}
					}
				} else if (r.get("is_big_file").toString().equals("1")) {//是大文件
					result = extractText(FileUtils.getFile(filePath));
				} else {
					throw new BusinessException("Neither a large file nor a small file, check the file type!");
				}
				//有换行无法入solr,替换掉换行
				result = result.replaceAll("[\r\n]", " ");
				//获取摘要前几行,默认取3行
				String summary = TextRankSentence.getTopSentenceList(result, CommonVariables.SUMMARY_VOLUMN).toString();
				//设置文本摘要到记录列表中
				r.put("file_text", result);
				r.put("file_summary", summary);
				//记录提取文件个数
				safeAdd();
			}
		}

		/**
		 * 记录提取的文件个数
		 */
		private void safeAdd() {

			AtomicInteger aci = new AtomicInteger(0);
			int alreadyProccessNumber = aci.incrementAndGet();
			if (alreadyProccessNumber % 100 == 0) {
				logger.info("已提取文本：" + alreadyProccessNumber + " 个！！！");
			}

		}

		/**
		 * 文件的二进制流,提取文本
		 *
		 * @param fileBytes 文件的二进制流
		 * @param fileName  文件
		 * @return 提取的文本信息, 如果发生异常, 提取文本为 ""
		 */
		public String extractText(byte[] fileBytes, String fileName) {

			try (InputStream is = new ByteArrayInputStream(fileBytes)) {
				//将bb作为输入流,将in作为输入流，读取图片存入image中，而这里in可以为ByteArrayInputStream();
				BufferedImage image = ImageIO.read(is);
				return instance.doOCR(image);
			} catch (Exception e) {
				logger.error("提取文件： " + fileName + " 错误！", e);
				return "";
			}
		}

		/**
		 * 图片,提取文本
		 *
		 * @param picture 图片
		 * @return 提取的文本信息, 如果发生异常, 提取文本为 ""
		 */
		public String extractText(File picture) {

			try {
				return instance.doOCR(picture);
			} catch (Exception e) {
				logger.error("提取文件： " + picture.getName() + " 错误！", e);
				return "";
			}
		}

		/**
		 * 新的记录列表写入新的avro
		 *
		 * @param recordList  写入avro文件的记录集合
		 * @param genAvroPath 生成的avro文件路径
		 */
		public void putIntoAvro(List<GenericRecord> recordList, String genAvroPath, FileSystem fs) {

			try (OutputStream outputStream = fs.create(new Path(genAvroPath));
			     DataFileWriter<Object> writer = new DataFileWriter<>(new GenericDatumWriter<>()).setSyncInterval(100)) {

				writer.setCodec(CodecFactory.snappyCodec());
				writer.create(SCHEMA, outputStream);
				for (GenericRecord genericRecord : recordList) {
					GenericRecord newGenericRecord = new GenericData.Record(SCHEMA);
					newGenericRecord.put("uuid", genericRecord.get("uuid").toString());
					newGenericRecord.put("file_text", genericRecord.get("file_text").toString());
					newGenericRecord.put("file_summary", genericRecord.get("file_summary").toString());
					writer.append(newGenericRecord);
				}
			} catch (Exception e) {
				logger.error("Failed to write to avro file ...", e);
			}
		}

		/**
		 * Description: 提取的文本摘要覆盖到已有的solr记录中
		 *
		 * @param recordList 写入avro文件的记录集合，此处要将这些记录写入solr
		 */
		public void putIntoSolr(List<GenericRecord> recordList) {

			try (ISolrOperator os = SolrFactory.getInstance()) {
				SolrClient server = os.getServer();
				List<SolrInputDocument> docs = new ArrayList<>();
				SolrInputDocument doc;
				for (GenericRecord r : recordList) {
					String id = r.get("uuid").toString();
					logger.info("id: " + id);
					String text = r.get("file_text").toString().replace("\n", "\\n");//文本处理
					if (StringUtil.isBlank(text)) {
						continue;
					}
					doc = new SolrInputDocument();
					doc.addField("id", r.get("uuid").toString());
					doc.addField("tf-file_name", r.get("file_name").toString());
					doc.addField("tf-file_scr_path", r.get("file_scr_path").toString());
					doc.addField("tf-file_size", r.get("file_size").toString());
					doc.addField("tf-file_time", r.get("file_time").toString());
					doc.addField("tf-file_summary", r.get("file_summary").toString());
					doc.addField("tf-file_text", text);
					doc.addField("tf-file_md5", r.get("file_md5").toString());
					doc.addField("tf-file_avro_path", r.get("file_avro_path").toString());
					doc.addField("tf-file_avro_block", r.get("file_avro_block").toString());
					doc.addField("tf-is_big_file", r.get("is_big_file").toString());
					docs.add(doc);
				}
				// 提交到solr
				if (docs.size() != 0) {
					server.add(docs);
					server.commit();
				}
			} catch (Exception e) {
				logger.error("Failed to write to solr ...", e);
			}

		}

		@Override
		public void run() {

			String genAvroPath = avroParentDir + "/ocravro/" + avroPath.getName() + "_zw";
			logger.info("正在读取" + avroPath + " , ocr识别到------>" + genAvroPath);
			//源avro和ocr识别后的信息存储的avro文件一一对应
			try {
				//如果已经有识别过的文件,删除
				if (hdfsOperator.fileSystem.exists(new Path(genAvroPath))) {
					hdfsOperator.fileSystem.delete(new Path(genAvroPath), false);
					logger.info("删除已有文件：" + genAvroPath);
				}
				//读取源Avro文件中的数据
				List<GenericRecord> recordList = readRecordFromAvro(hdfsOperator.fileSystem);
				//重新识别并写入,提取结果记录列表中每条记录的文本
				extractTextFromRecord(recordList);
				//写入avro
				putIntoAvro(recordList, genAvroPath, hdfsOperator.fileSystem);
				//写入solr
				putIntoSolr(recordList);
			} catch (Exception e) {
				logger.error("识别文件: " + avroPath + " 到 " + genAvroPath + "失败! 异常:" + e);
			}
		}
	}

	public static void main(String[] args) {

		logger.info("Start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		OcrExtractText oet = new OcrExtractText("/hrds/DCL/828989695240179712/828989754677661696");
		oet.OCR2AvroAndSolr();
		logger.info("End~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

}
