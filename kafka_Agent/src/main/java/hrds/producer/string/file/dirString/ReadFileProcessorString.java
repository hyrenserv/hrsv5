package hrds.producer.string.file.dirString;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.MapDBHelper;
import hrds.producer.common.FileDataValidator;
import hrds.producer.common.GetFileParams;
import hrds.producer.common.JobParamsEntity;
import hrds.producer.common.KafkaProducerWorker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ReadFileProcessorString {

	private static final Logger logger = LogManager.getLogger();

	public void lineProcessor(GetFileParams getFileParams, MapDBHelper mapDBHelper,
	                          ConcurrentMap<String, String> htmap, ConcurrentMap<String, String> htMapThread,
	                          File file, List<String> listColumn, JobParamsEntity jobParams, File fileRename,
	                          String charset, String beforeLine, JSONObject json, FileDataValidator fileDataValidator) {

		RandomAccessFile readFile;
		try {
			readFile = new RandomAccessFile(file, "r");
			StringBuilder lineBuffer = new StringBuilder();
			if (beforeLine != null) {
				readFile.seek(Long.parseLong(beforeLine));
			}
			KafkaProducerWorker kafkaProducerWorkerString = new KafkaProducerWorker();
			long number = 0;
			while (true) {
				number++;
				String line = readFile.readLine();
				if (line != null) {
					if (isNewLine(line, fileDataValidator)) {
						if (lineBuffer.length() < 1) {
							lineBuffer.append(line);
						} else {
							json = getFileParams.getRealJson(jobParams, lineBuffer, charset, listColumn, json);
							if (kafkaProducerWorkerString.sendToKafka(file.getAbsolutePath(), jobParams.getProducerString(), json.toString(),
								jobParams.getTopic(), jobParams.getCustomerPartition(), jobParams.getBootstrapServers(),
								jobParams.getSync())) {
								htmap.put(file.getName(), String.valueOf(readFile.getFilePointer()));
								lineBuffer.delete(0, lineBuffer.length());
								lineBuffer.append(line);
								if (number % 5000 == 0) {
									mapDBHelper.commit();
								}
							} else {
								logger.error("数据发送失败！！！");
								break;//遇到错误则退出，等待处理后再继续
							}
						}
					} else {
						lineBuffer.append("\n").append(line);
					}
				} else {
					if (lineBuffer.length() > 0) {
						json = getFileParams.getRealJson(jobParams, lineBuffer, charset, listColumn, json);
						kafkaProducerWorkerString.sendToKafka(file.getAbsolutePath(), jobParams.getProducerString(), json.toString(),
							jobParams.getTopic(), jobParams.getCustomerPartition(), jobParams.getBootstrapServers(), jobParams.getSync());
					}
					IOUtils.closeQuietly(readFile);
					htmap.put(file.getName(), "all");
					mapDBHelper.commit();
					try {
						File fileOld = new File((fileRename.getAbsolutePath() + File.separator + file.getName()));
						if (FileUtils.directoryContains(fileRename, fileOld)) {
							if (fileOld.delete()) {
								throw new BusinessException("删除旧文件失败：" + fileOld.getPath());
							}
						}
						FileUtils.moveToDirectory(file, fileRename, true);
						htMapThread.remove(file.getName());
					} catch (Exception e) {
						logger.error("文件移动失败！！！失败文件为：" + file.getAbsolutePath(), e);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e);
			throw new BusinessException(e.getMessage());
		} finally {
			mapDBHelper.commit();
		}
	}

	/**
	 * @param htMap       //读取位置存储信息表
	 * @param htMapThread
	 * @param file        //采集 文件
	 * @param listColumn  //列集合
	 * @param jobParams   //任务参数实体
	 * @param fileRename  //转移目的文件夹
	 * @param charset     //编码类型
	 */
	public void objectProcessor(MapDBHelper mapDBHelper, ConcurrentMap<String, String> htMap,
	                            ConcurrentMap<String, String> htMapThread, File file, List<String> listColumn,
	                            JobParamsEntity jobParams, File fileRename,
	                            String charset, JSONObject json) {

		try {
			String message = readParam(file, charset);
			KafkaProducerWorker kafkaProducerWorkerString = new KafkaProducerWorker();
			JSONArray jsonArray = JSONObject.parseArray(message);
			if (jsonArray != null) {
				for (Object obj : jsonArray) {
					if (IsFlag.Shi == IsFlag.ofEnumByCode(jobParams.getIsObj())) {
						json.put("line", obj.toString());
					} else {
						JSONObject jsonMessage = JSONObject.parseObject(obj.toString());
						for (String column : listColumn) {
							if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
								String columnValue = jsonMessage.getString(column.split("`")[0]);
								if (columnValue != null) {
									json.put(column.split("`")[0], columnValue);
								}
							}
						}
					}
					String cusDesType = jobParams.getCusDesType();
					if (!StringUtils.isBlank(cusDesType)) {
						if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
							json = jobParams.getBusinessProcess().process(listColumn, json);
						} else {
							json = (JSONObject) jobParams.getInvocable().invokeFunction("recordFunction", json);
						}
					}
					kafkaProducerWorkerString.sendToKafka(file.getAbsolutePath(), jobParams.getProducerString(), json.toString(),
						jobParams.getTopic(), jobParams.getCustomerPartition(), jobParams.getBootstrapServers(), jobParams.getSync());
				}
			} else {
				logger.error("数据格式错误，为非JsonArray数据！！！ 文件为：" + file.getAbsolutePath());
			}

			htMap.put(file.getName(), "all");
			mapDBHelper.commit();
		} catch (Exception e) {
			logger.error("file:" + file.getAbsolutePath(), e);
		}
		try {
			File fileOld = new File((fileRename.getAbsolutePath() + File.separator + file.getName()));
			if (FileUtils.directoryContains(fileRename, fileOld)) {
				if (!fileOld.delete()) {
					throw new BusinessException("删除旧文件失败！！" + fileOld.getPath());
				}
			}
			FileUtils.moveToDirectory(file, fileRename, true);
			htMapThread.remove(file.getName());
		} catch (IOException e) {
			logger.error("文件移动失败！！！ 失败文件为：" + file.getAbsolutePath(), e);
		}

	}

	public String readParam(File file, String charset) {

		String lineTxt;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(file));
			while ((lineTxt = bf.readLine()) != null) {
				stringBuilder.append(lineTxt);
			}
		} catch (FileNotFoundException e) {
			logger.error("文件不存在！！！失败文件为：" + file.getAbsolutePath(), e);
			System.exit(-2);
		} catch (IOException e1) {
			throw new BusinessException(e1.getMessage());
		} finally {
			IOUtils.closeQuietly(bf);
		}
		String message = null;
		try {
			message = new String(stringBuilder.toString().getBytes("ISO-8859-1"), charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("数据类型转换失败！！！失败文件为：" + file.getAbsolutePath(), e);
		}
		return message;
	}

	private boolean isNewLine(String line, FileDataValidator fileDataValidator) {

		if (fileDataValidator != null) {
			return fileDataValidator.isNewLine(line);
		} else {
			return true;
		}

	}
}