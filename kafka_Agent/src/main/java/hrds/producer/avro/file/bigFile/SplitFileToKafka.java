package hrds.producer.avro.file.bigFile;

import com.alibaba.fastjson.JSONObject;
import hrds.agent.trans.biz.filecontentstream.FileContentStreamInfo;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.Constant;
import hrds.producer.common.JobParamsEntity;
import hrds.producer.common.KafkaProducerWorker;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SplitFileToKafka implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	public static final long READ_POSITION_LATEST = -2L;//从上次处理的位置开始读文件
	public static final long READ_POSITION_ASSIGN = 1L;//从指定位置开始读文件

	private String jobId;

	private JSONObject json;

	private String lineNums;

	private CountDownLatch countDownLatch;

	public SplitFileToKafka(String jobId, JSONObject json, CountDownLatch countDownLatch) {
		this.jobId = jobId;
		this.json = json;
		this.lineNums = null;
		this.countDownLatch = countDownLatch;
	}

	public SplitFileToKafka(String jobId, JSONObject json, String lineNums, CountDownLatch countDownLatch) {
		this.jobId = jobId;
		this.json = json;
		this.lineNums = lineNums;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {

		JSONObject jsonParams = json.getJSONObject("sdm_receive_conf");
		String fileAbsolute = jsonParams.getString("ra_file_path");
		if (FileContentStreamInfo.mapJob.containsKey(jobId)) {
			FileContentStreamInfo.mapJob.get(jobId).interrupt();

		}
		FileContentStreamInfo.mapJob.put(jobId, Thread.currentThread());
		ProducerOperatorBigFile producerOperatorBigFile = new ProducerOperatorBigFile();
		JobParamsEntity jobParams = producerOperatorBigFile.getMapParam(json, jobId);
		File srcFile = new File(fileAbsolute);
		if (!srcFile.exists()) {
			logger.info("文件不存在！！！");
			Thread.currentThread().interrupt();
		} else {

			long position = Long.parseLong(jsonParams.getString("file_initposition"));
			int filePointer = 0;
			RandomAccessFile rafFilePointer = null;
			InputStream in = null;
			BufferedInputStream bis = null;
			try {
				rafFilePointer = new RandomAccessFile(new File(fileAbsolute + ".rdp"), "rw");

				if (position == READ_POSITION_LATEST) {
					rafFilePointer.seek(0);
					filePointer = rafFilePointer.readInt();
				} else if (position == READ_POSITION_ASSIGN) {
					String file_read_num = jsonParams.getString("file_read_num");
					filePointer = Integer.parseInt(file_read_num);
				}
				long countSize = srcFile.length();
				long fileSize = jsonParams.getLongValue("messageSize");
				int num;
				if (countSize % fileSize == 0) {
					num = (int) (countSize / fileSize);
				} else {
					num = (int) (countSize / fileSize) + 1;
				}
				in = new FileInputStream(srcFile);
				bis = new BufferedInputStream(in);
				byte[] bytes = new byte[(int) fileSize];
				int len;
				int count = 0;
				KafkaProducerWorker kafkaProducerWorker = new KafkaProducerWorker();
				List<String> linuNumList = new ArrayList<>();
				if (!StringUtils.isBlank(lineNums)) {
					linuNumList = Arrays.asList(lineNums.split(","));
				}
				int i = 0;
				while ((len = bis.read(bytes)) != -1) {
					if (i >= filePointer && linuNumList.isEmpty() || linuNumList.contains(String.valueOf(i))) {
						//TODO Send to kafka -> len
						GenericRecord genericRecord = new GenericData.Record(jobParams.getSchema());
						genericRecord.put("line", String.valueOf(len));
						genericRecord.put("bytes", ByteBuffer.wrap(bytes, 0, len));
						genericRecord.put("lineNum", i);
						if (kafkaProducerWorker.sendToKafka(fileAbsolute, jobParams.getProducer(), genericRecord, jobParams.getTopic(),
							jobParams.getCustomerPartition(), jobParams.getBootstrapServers(), jobParams.getSync())) {
							rafFilePointer.seek(0);
							rafFilePointer.writeLong(i);
						}
						count += len;
						if (count >= countSize) {
							break;
						}
					}
					i++;
				}
				StringBuilder sb = new StringBuilder();
				String fileType = Files.probeContentType(srcFile.toPath());
				sb.append(num).append(",").append(srcFile.length()).append(",").append(fileAbsolute).append(",").append(fileType).append(",")
					.append(Constant.STREAM_HYREN_END).append(",").append(jobId);
				sb.append(",").append("md5");
				sb.append(",").append(jsonParams.getString("sdm_server_ip")).append(":").append(jsonParams.getString("sdm_rec_port"));
				GenericRecord genericRecord = new GenericData.Record(jobParams.getSchema());
				genericRecord.put("line", sb.toString());
				genericRecord.put("lineNum", 0);
				kafkaProducerWorker.sendToKafka(fileAbsolute, jobParams.getProducer(), genericRecord, jobParams.getTopic(),
					jobParams.getCustomerPartition(), jobParams.getBootstrapServers(), jobParams.getSync());
				logger.info("read file end:" + System.currentTimeMillis());
				if (this.countDownLatch != null) {
					this.countDownLatch.countDown();
				}
			} catch (Exception e) {
				logger.error("文件读取异常！！！", e);
				throw new BusinessException(e.getMessage());
			} finally {
				IOUtils.closeQuietly(rafFilePointer);
				IOUtils.closeQuietly(bis);
				IOUtils.closeQuietly(in);
			}
		}
	}
}
