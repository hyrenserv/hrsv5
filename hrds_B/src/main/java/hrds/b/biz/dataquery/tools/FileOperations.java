package hrds.b.biz.dataquery.tools;

import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;
import fd.ng.web.util.Dbo;
import hrds.commons.codes.ApplyType;
import hrds.commons.codes.AuthType;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Source_file_attribute;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.hadoop.readconfig.ConfigReader;
import hrds.commons.utils.CommonVariables;
import hrds.commons.utils.DboExecute;
import hrds.commons.utils.FileTypeUtil;
import hrds.commons.utils.PathUtil;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.mapred.FsInput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class FileOperations {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * <p>方法名: getFileBytesFromAvro</p>
	 * <p>方法说明: 在hdfs上获取文件信息； 占位方法，方法写完后删除</p>
	 */
	@Method(desc = "根据文件id获取文件信息",
		logicStep = "1.检查文件是否存在" +
			"2.文件不在avro文件中，还在服务器本地,获取文件的byte并返回" +
			"3.已经采集到avro并上传到hdfs上，需要查看是大文件还是小文件" +
			"4.是否为大文件：1是 2否" +
			"4-1.是大文件file_contents存储文件的实际存储路径,然后根据从hdfs获取的文件路径获取文件二进制流" +
			"4-2.不是大文件file_contents存储的是文件数据,直接获取文件二进制流")
	@Param(name = "fileId", desc = "文件id", range = "自动生成的id")
	@Return(desc = "返回值说明", range = "返回值取值范围")
	public static byte[] getFileBytesFromAvro(String fileId) {
		//1.检查文件是否存在
		Optional<Source_file_attribute> sourceFileAttribute = Dbo.queryOneObject(Source_file_attribute.class,
			"SELECT * FROM source_file_attribute WHERE file_id=?", fileId);
		if (!sourceFileAttribute.isPresent()) {
			throw new BusinessException("申请的文件不存在！fileId=" + fileId);
		}
		//如果is_cache为是，则表示这个文件是单文件上传接口上传的文件，且还缓存在服务器本地
		String isCache = sourceFileAttribute.get().getIs_cache();
		if (StringUtil.isNotBlank(isCache) && IsFlag.Shi.getValue().equals(isCache)) {
			//2.文件不在avro文件中，还在服务器本地,获取文件的byte并返回
			try {
				String sourcePath = sourceFileAttribute.get().getSource_path();
				return FileUtils.readFileToByteArray(new File(sourcePath));
			} catch (IOException e) {
				e.printStackTrace();
				throw new BusinessException("Failed to get the byte of the local file!" + fileId);
			}
		}
		//3.已经采集到avro并上传到hdfs上，需要查看是大文件还是小文件
		Long fileAvroBlock = sourceFileAttribute.get().getFile_avro_block();
		String fileAvroPath = sourceFileAttribute.get().getFile_avro_path();
		GenericRecord avroRecord = getAvroRecord(fileAvroBlock, fileAvroPath);
		//4.是否为大文件：1是 2否
		String isBigFile = avroRecord.get("is_big_file").toString();
		if (IsFlag.Shi.getCode().equals(isBigFile)) {
			//4-1.是大文件file_contents存储文件的实际存储路径,然后根据从hdfs获取的文件路径获取文件二进制流
			String filePath = new String(((ByteBuffer) avroRecord.get("file_contents")).array(), CodecUtil.UTF8_CHARSET);
			String fileHdfsPath = PathUtil.convertLocalPathToHDFSPath(filePath);
			try (HdfsOperator operator = new HdfsOperator()) {
				if (!operator.exists(fileHdfsPath)) {
					throw new BusinessException("大文件 " + fileHdfsPath + " 不存在");
				}
				return IOUtils.toByteArray(operator.open(fileHdfsPath));
			} catch (IOException e) {
				e.printStackTrace();
				throw new BusinessException("Failed to get the byte of the hdfs file!" + fileId);
			}
		} else {
			//4-2.不是大文件file_contents存储的是文件数据,直接获取文件二进制流
			return ((ByteBuffer) avroRecord.get("file_contents")).array();
		}
	}

	@Method(desc = "根据文件id获取文件信息",
		logicStep = "1.检查文件是否存在" +
			"2.获取文件属性信息" +
			"3.设置返回数据信息")
	@Param(name = "fileId", desc = "文件id", range = "自动生成的id")
	@Return(desc = "返回值说明", range = "返回值取值范围")
	public static Map<String, String> getFileInfoByFileId(String fileId) {
		return getFileInfoByFileId(fileId, false);
	}

	@Method(desc = "根据文件id获取文件信息",
		logicStep = "1.检查文件是否存在" +
			"2.获取文件属性信息" +
			"3.设置返回数据信息")
	@Param(name = "fileId", desc = "文件id", range = "自动生成的id")
	@Param(name = "ocrText", desc = "是否需要ocr识别", range = "自动生成的id")
	@Return(desc = "返回值说明", range = "返回值取值范围")
	public static Map<String, String> getFileInfoByFileId(String fileId, boolean ocrText) {
		//初始化文件信息
		Map<String, String> fileInfoMap = new HashMap<>();
		//1.检查文件是否存在
		Source_file_attribute source_file_attribute = new Source_file_attribute();
		source_file_attribute.setFile_id(fileId);
		Optional<Source_file_attribute> fileRs = Dbo.queryOneObject(Source_file_attribute.class,
			"SELECT * FROM source_file_attribute WHERE file_id=?", source_file_attribute.getFile_id());
		if (!fileRs.isPresent()) {
			throw new BusinessException("申请的文件不存在！fileId=" + fileId);
		} else {
			//2.获取文件属性信息
			Source_file_attribute sfa = fileRs.get();
			long fileAvroBlock = sfa.getFile_avro_block();
			String fileAvroPath = sfa.getFile_avro_path();
			String originalName = sfa.getOriginal_name();
			String storageDate = sfa.getStorage_date();
			String storageTime = sfa.getStorage_time();
			String originalUpdateDate = sfa.getOriginal_update_date();
			String originalUpdateTime = sfa.getOriginal_update_time();
			String fileSuffix = sfa.getFile_suffix();
			GenericRecord avroRecord = getAvroRecord(fileAvroBlock, fileAvroPath);
			//3.设置返回数据信息
			fileInfoMap.put("original_name", originalName);
			fileInfoMap.put("storage_date", storageDate);
			fileInfoMap.put("storage_time", storageTime);
			fileInfoMap.put("original_update_date", originalUpdateDate);
			fileInfoMap.put("original_update_time", originalUpdateTime);
			String fileText = avroRecord.get("file_text").toString();
			List<String> list = FileTypeUtil.getTypeFileList(FileTypeUtil.TuPian);
			//如果是图片，就将图片流转换成base64，以便传输到页面展示
			assert list != null;
			if (list.contains(fileSuffix)) {
				ByteBuffer bb = (ByteBuffer) avroRecord.get("file_contents");
				fileText = Base64.getEncoder().encode(bb).toString();
				//如果是ture的话，就是只要文本，如果是fase的话就要图片的二进制流，其他情况用的时候在使用
				if (ocrText) {
					fileText = getOcrText(fileAvroPath, originalName, fileId);
				}
			}
			fileInfoMap.put("file_content", fileText);
			fileInfoMap.put("file_id", fileId);
			fileInfoMap.put("file_suffix", fileSuffix);
		}
		return fileInfoMap;
	}

	private static String getOcrText(String file_avro_path, String name, String uuid) {
		return "getOcrText";
	}

	@Method(desc = "根据文件id检查文件是否已经存在",
		logicStep = "1.根据文件id检查文件是否存在")
	@Param(name = "fileId", desc = "文件id", range = "自动生成的id")
	@Return(desc = "文件是否已经存在", range = "true: 存在, false: 不存在")
	private static boolean checkFileIsExist(String fileId) {
		if (StringUtil.isBlank(fileId)) {
			throw new BusinessException("待检查文件id为空！fileId=" + fileId);
		}
		//1.根据文件id检查文件是否存在
		return Dbo.queryNumber("SELECT COUNT(1) FROM " + Source_file_attribute.TableName + " WHERE file_id=?",
			fileId).orElseThrow(() -> new BusinessException("检查文件是否存在的SQL编写错误")) == 1;
	}

	@Method(desc = "更新文件查看权限",
		logicStep = "1.更新文件查看权限,如果查看的权限是一次" +
			"2.查看后，删除这次申请")
	@Param(name = "fileId", desc = "文件id", range = "自动生成的id")
	public static void updateViewFilePermissions(String fileId) {
		//1.更新文件查看权限,如果查看的权限是一次
		Result dataAuthRs = Dbo.queryResult(
			"select * from data_auth where file_id=? and apply_type = ? and auth_type = ?",
			fileId, ApplyType.ChaKan.getCode(), AuthType.YiCi.getCode());
		//2.查看后，删除这次申请
		if (!dataAuthRs.isEmpty()) {
			DboExecute.deletesOrThrow(1, "权限更新失败!",
				"delete from data_auth where file_id=? and apply_type = ? and auth_type = ?",
				fileId, ApplyType.ChaKan.getCode(), AuthType.YiCi.getCode());
		}
	}

	@Method(desc = "获取文件所在Avro记录",
		logicStep = "1.根据文件所在的block块id和文件Avro地址获取Avro记录")
	@Param(name = "file_avro_block", desc = "文件block块id", range = "自动生成的id")
	@Param(name = "file_avro_path", desc = "文件Avro地址", range = "自动生成的地址")
	private static GenericRecord getAvroRecord(Long file_avro_block, String file_avro_path) {
		DataFileReader<GenericRecord> fileReader = null;
		Path path = new Path(file_avro_path);
		DatumReader<GenericRecord> reader = new GenericDatumReader<>();
		//判断是否有Hadoop环境如果有,则查询HDFS上文件
		if (CommonVariables.FILE_COLLECTION_IS_WRITE_HADOOP) {
			Configuration conf = ConfigReader.getConfiguration();
			try (SeekableInput in = new FsInput(path, conf)) {
				fileReader = new DataFileReader<>(in, reader);
				GenericRecord record = new GenericData.Record(fileReader.getSchema());
				//指定block号
				fileReader.seek(file_avro_block);
				if (fileReader.hasNext()) {
					return fileReader.next(record);
				} else {
					throw new BusinessException("This block has no record in avro");
				}
			} catch (Exception e) {
				throw new BusinessException("FsInput data failure!");
			} finally {
				try {
					if (fileReader != null) {
						fileReader.close();
					}
				} catch (IOException ioe) {
					throw new BusinessException("DataFileReader close exception!");
				}
			}
		}
		//不是Hadoop环境
		else {
			try {
				File avro_file = new File(path.toString());
				if (!avro_file.exists()) {
					throw new BusinessException("本地文件: " + path + " ,已经不存在!");
				}
				logger.info("读取本地文件:" + path);
				fileReader = new DataFileReader<>(avro_file, reader);
				GenericRecord record = new GenericData.Record(fileReader.getSchema());
				//指定block号
				fileReader.seek(file_avro_block);
				if (fileReader.hasNext()) {
					return fileReader.next(record);
				} else {
					throw new BusinessException("This block has no record in avro");
				}
			} catch (IOException e) {
				throw new BusinessException("File data failure!");
			}
		}
	}
}
