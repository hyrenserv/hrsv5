package hrds.producer.string.file.file;

import com.alibaba.fastjson.JSONObject;
import fd.exception.BusinessException;
import hmfms.service.Constant;
import hmfms.util.Debug;
import hrds.agent.tranbiz.Tran9003;
import hrds.producer.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileReadAllroundString implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	private static final KafkaProducerError KAFKA_PRODUCER_ERROR = new KafkaProducerError();

	public static final Map<String, Thread> mapJob = new HashMap<>();

	public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

	public String jobId;

	public static final long READ_POSITION_HEAD = 0L;//从头开始读文件
	public static final long READ_POSITION_TAIL = -1L;//从最后开始读文件
	public static final long READ_POSITION_LATEST = -2L;//从上次处理的位置开始读文件
	public static final long READ_POSITION_ASSIGN = 1L;//从指定位置开始读文件

	private ExecutorService executor;
	private ArtisanRunnable runner;
	private Future<?> runnerFuture;

	private String sourceFilepath;
	private long position;
	private String sourceFileCharset;
	private FileDataValidator filedataValidator;
	private String file_read_num;
	private JobParamsEntity jobParams;
	private boolean readeOpinion;
	private String is_data_partition;
	private JSONObject json;
	private CountDownLatch countDownLatch;
	private String read_mode;
	private String is_obj;

	public FileReadAllroundString(String jobId, JSONObject json, CountDownLatch countDownLatch) {

		this.jobId = jobId;
		this.json = json;
		this.countDownLatch = countDownLatch;
		executor = Tran9003.mapExec.get(jobId);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {

				quit();
				Debug.error(logger, "FileReadAllround shutdown hook down." + System.currentTimeMillis());
			}
		});
	}

	public void config(String is_data_partition, boolean readeOpinion, String filepath, long position, String sourceFileCharset,
	                   FileDataValidator filedataValidator, String file_read_num, JobParamsEntity jobParams, String read_mode, String is_obj) {

		this.is_data_partition = is_data_partition;
		this.sourceFilepath = filepath;
		this.position = position;
		this.sourceFileCharset = sourceFileCharset;
		this.filedataValidator = filedataValidator;
		this.jobParams = jobParams;
		this.file_read_num = file_read_num;
		this.readeOpinion = readeOpinion;
		this.read_mode = read_mode;
		this.is_obj = is_obj;
	}

	/**
	 * 开始处理文件。读取的起始位置由参数position决定。可以是READ_POSITION_HEAD等3个位置，或者自己指定确切的位置。
	 * 
	 * @param position
	 */
	public void startup() {

		Debug.debug(logger, "FileReadAllround startup.");

		//TODO 传入线程参数，测试参数是否生效
		executor = Executors.newSingleThreadExecutor();
		try {
			runner = new ArtisanRunnable(this.is_data_partition, this.readeOpinion, this.sourceFilepath, this.position, this.sourceFileCharset,
							this.filedataValidator, this.file_read_num, this.jobParams, this.countDownLatch, this.read_mode, this.is_obj);
			Thread thread = mapJob.get(jobId);
			if( thread != null ) {
				thread.interrupt();
				runnerFuture = executor.submit(runner);
			}
			else {
				runnerFuture = executor.submit(runner);
			}
		}
		catch(Exception e) {
			Debug.exception(logger, "startup failed!", e);
			quit();
			throw new BusinessException(e);
		}
		//以上可修改为读取一个目录下所有符合要求的文件，并启动多个线程并行处理。
	}

	public void quit() {

		Debug.debug(logger, "FileReadAllround quit beginning on {} " + System.currentTimeMillis());
		if( runner != null ) {
			runner.kill();
		}
		Debug.debug(logger, "FileReadAllround quit step 1 runner killed.");
		if( runnerFuture != null ) {
			runnerFuture.cancel(true);
		}
		Debug.debug(logger, "FileReadAllround quit step 2 runnerFuture canceled.");
		executor.shutdown();
		Debug.debug(logger, "FileReadAllround quit step 3 executor shutdown.");
		while( !executor.isTerminated() ) {
			try {
				executor.awaitTermination(500, TimeUnit.MILLISECONDS);
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		Debug.debug(logger, "FileReadAllround quit over on {}. " + System.currentTimeMillis());
	}

	//Start
	@Override
	public void run() {

		try {
			JSONObject jsonParams = json.getJSONObject("sdm_receive_conf");
			String readType = jsonParams.getString("read_type");//读取类型0：单次读取 1：实时读取
			if( "0".equals(readType) ) {
				this.readeOpinion = false;
			}
			else {
				this.readeOpinion = true;
			}
			this.jobId = jsonParams.getString("sdm_receive_id");
			ProducerOperatorFileString producerOperator = new ProducerOperatorFileString();
			JobParamsEntity jobParams = producerOperator.getMapParam(json, jobId);

			String sourceFilepath = jsonParams.getString("ra_file_path");
			String fileValidatorClassname = jsonParams.getString("file_handle");
			FileDataValidator filedataValidator = null;
			if( !StringUtils.isBlank(fileValidatorClassname) ) {
				CusClassLoader classLoader = new CusClassLoader();
				Class<?> clazz = classLoader.getURLClassLoader().loadClass(fileValidatorClassname);
				filedataValidator = (FileDataValidator)clazz.newInstance();
			}
			String position = jsonParams.getString("file_initposition");
			String sourceFileCharset = jsonParams.getString("code");
			String file_read_num = jsonParams.getString("file_read_num");
			String is_data_partition = jsonParams.getString("is_data_partition");
			String read_mode = jsonParams.getString("read_mode");
			String is_obj = jsonParams.getString("is_obj");
			if( "0".equals(is_data_partition) ) {
				jobParams.setSdmDatelimiter(jsonParams.getString("sdm_dat_delimiter"));
			}
			config(is_data_partition, readeOpinion, sourceFilepath, Long.parseLong(position), sourceFileCharset, filedataValidator, file_read_num,
							jobParams, read_mode, is_obj);
			startup();
		}
		catch(Exception e) {
			Debug.exception(logger, e.getMessage(), e);
			quit();
		}
	}

	private class ArtisanRunnable implements Runnable {

		private String filepath;//读取的文件全路径
		private File file;//读取的文件
		private long readSeekPos;//开始读取的位置。0：从头读；-1：从尾读，1：从该位置读
		private String charset;//字符集
		private String file_read_num;
		private FileDataValidator filedataValidator;
		private KafkaProducer<String, String> producer;
		private String topic;
		private CustomerPartition cp;
		private String bootstrapServers;
		private String jobId;
		private boolean readeOpinion;
		private List<String> listColumn;
		private boolean reading;
		private String is_data_partition;
		private String sync;
		private CountDownLatch countDownLatch;
		private String read_mode;
		private String is_obj;

		public ArtisanRunnable(String is_data_partition, boolean readeOpinion, String filepath, long readSeekPos, String charset,
		                       FileDataValidator filedataValidator, String file_read_num, JobParamsEntity jobParams, CountDownLatch countDownLatch,
		                       String read_mode, String is_obj) throws Exception {

			this.filepath = filepath;
			this.file = new File(filepath);
			this.readSeekPos = readSeekPos;
			this.charset = charset;
			this.reading = true;
			this.filedataValidator = filedataValidator;
			this.file_read_num = file_read_num;
			this.producer = jobParams.getProducerString();
			this.topic = jobParams.getTopic();
			this.cp = jobParams.getCustomerPartition();
			this.bootstrapServers = jobParams.getBootstrapServers();
			this.jobId = jobParams.getJobId();
			this.readeOpinion = readeOpinion;
			this.listColumn = jobParams.getListColumn();
			this.is_data_partition = is_data_partition;
			this.sync = jobParams.getSync();
			this.countDownLatch = countDownLatch;
			this.read_mode = read_mode;
			this.is_obj = is_obj;
		}

		@Override
		public void run() {

			//TODO 多线程
			mapJob.put(jobId, Thread.currentThread());
			if( !this.file.exists() ) {
				Debug.error(logger, "file [{}] is not exist! " + filepath);
				return;
			}
			//logger.debug("file length is {}", this.file.length());

			long filePointer = 0;//文件读取的位置
			long lineNum = 0;
			if( this.readSeekPos == READ_POSITION_TAIL ) {
				filePointer = this.file.length();
			}
			else if( this.readSeekPos == READ_POSITION_HEAD )
				filePointer = 0;
			else if( this.readSeekPos == READ_POSITION_LATEST ) {
				try (RandomAccessFile rafFilePointer = new RandomAccessFile(new File(filepath + ".rdp"), "r")) {
					rafFilePointer.seek(0);
					filePointer = rafFilePointer.readLong();
				}
				catch(Exception e) {
					if( e instanceof FileNotFoundException )
						filePointer = 0;//存文件读取位置的rdp文件不存在，则默认从头开始读
					else {
						Debug.exception(logger, "read rdp file [" + this.filepath + "] failed!", e);
						return;
					}
				}
			}
			else if( this.readSeekPos == READ_POSITION_ASSIGN )
				lineNum = Long.valueOf(this.file_read_num);
			else {
				Debug.error(logger, "file read position[{}] is wrong! " + this.readSeekPos);
				return;
			}

			RandomAccessFile rafSourceFile = null;
			KafkaProducerWorker kafkaProducerWorkerString = new KafkaProducerWorker();
			try (RandomAccessFile rafFilePointer = new RandomAccessFile(new File(filepath + ".rdp"), "rw")) {
				rafSourceFile = new RandomAccessFile(file, "r");
				StringBuilder lineBuffer = new StringBuilder(1024);//存储符合业务意义的一行数据，比如日志中的Exception文本。初始1024，避免16的默认长度
				GetFileParams getFileParams = new GetFileParams();
				JSONObject json = getFileParams.getParmJson(listColumn, file);
				if( lineNum > 0 ) {
					for(int i = 1; i < lineNum; i++) {
						rafSourceFile.readLine();
					}
				}
				else {
					rafSourceFile.seek(filePointer);
				}
				while( this.reading ) {
					this.reading = readeOpinion;
					long curfileLength = file.length();
					//文件目前的长度小于起始读取的位置，说明文件被更新过，比如日志在晚上日切
					if( curfileLength < filePointer || (curfileLength == 0 && filePointer == 0) ) {
						if( rafSourceFile != null ) {
							rafSourceFile.close();
						}
						rafSourceFile = new RandomAccessFile(file, "r");
						filePointer = 0;
						rafFilePointer.seek(0);
						rafFilePointer.writeLong(0L);
					}
					while( true ) {
						String curLineString = rafSourceFile.readLine();
						//如果已经读完了文件，那么需要把存储在lineBuffer中的数据送出。
						if( curLineString == null ) {
							if( lineBuffer.length() > 0 ) {
								// 发送上一行数据（lineBuffer），给kafka
								json = getFileParams.getRealJson(jobParams, lineBuffer, charset, listColumn, json, read_mode);
								if( kafkaProducerWorkerString.sendToKafka(filepath, producer, json.toString(), topic, cp, bootstrapServers, sync) ) {
									filePointer = saveReadingPointer(rafSourceFile, rafFilePointer);
									lineBuffer.delete(0, lineBuffer.length());
								}
								else {
									Debug.error(logger, "数据发送失败，失败数据为：" + lineBuffer.toString());
									KAFKA_PRODUCER_ERROR.sendToKafka(bootstrapServers, topic, json.toString());
									break;//遇到错误则退出，等待处理后再继续
								}
							}
							TimeUnit.MILLISECONDS.sleep(500);//休息一会再看是否能读到数据，避免死循环狂读。
							break;
						}

						if( isNewLine(curLineString) ) {
							if( lineBuffer.length() < 1 ) {//第一次读到的一行数据，先缓存起来
								lineBuffer.append(curLineString);
							}
							else {//读到了新的一行，而且buffer中有数据，则把buffer中的数据发送出去。即上一行的数据发送出去
									// 发送上一行数据（lineBuffer），给kafka
								json = getFileParams.getRealJson(jobParams, lineBuffer, charset, listColumn, json, read_mode);
								if( kafkaProducerWorkerString.sendToKafka(filepath, producer, json.toString(), topic, cp, bootstrapServers, sync) ) {
									filePointer = saveReadingPointer(rafSourceFile, rafFilePointer, curLineString);
									lineBuffer.delete(0, lineBuffer.length());
									lineBuffer.append(curLineString);
								}
								else {
									Debug.error(logger, "数据发送失败！！！");
									break;//遇到错误则退出，等待处理后再继续
								}
							}
						}
						else {
							lineBuffer.append('\n').append(curLineString);
							//							filePointer = saveReadingPointer(rafSourceFile, rafFilePointer);
						}

					}
				}
				json = getParmJson2(listColumn, file, Constant.STREAM_HYREN_END);
				kafkaProducerWorkerString.sendToKafka(filepath, producer, json.toString(), topic, cp, bootstrapServers, sync);
				if( this.countDownLatch != null ) {
					this.countDownLatch.countDown();
				}
				if( producer != null ) {
					producer.close();
				}

			}
			catch(Exception e) {
				if( e instanceof InterruptedException ) {
					Thread.currentThread().interrupt();
				}
				else {
					e.printStackTrace();
					throw new BusinessException(e);
				}
			}
			finally {
				try {
					if( rafSourceFile != null ) {
						rafSourceFile.close();
					}
				}
				catch(Exception e) {}
			}
		}
		
		/**
		 * 这时最后一行读到数据为null，将缓存的数据发送出去，同时记下文件最后的位置
		 * @param rafSource
		 * @param rafPointer
		 * @return
		 * @throws IOException
		 */
		private long saveReadingPointer(RandomAccessFile rafSource, RandomAccessFile rafPointer) throws IOException {

			long pointer = rafSource.getFilePointer();
			rafPointer.seek(0);
			rafPointer.writeLong(pointer);//(String.valueOf(filePointer));
			return pointer;
		}

		/**
		 * 不是最后一行时，文件读到缓存的下一行，这时要把记录数的这一行的长度去掉，再去掉换行符的2个长度
		 * @param rafSource
		 * @param rafPointer
		 * @param curLineString
		 * @return
		 * @throws IOException
		 */
		private long saveReadingPointer(RandomAccessFile rafSource, RandomAccessFile rafPointer, String curLineString) throws IOException {

			long pointer = rafSource.getFilePointer();
			pointer = pointer - curLineString.length() - 2;
			rafPointer.seek(0);
			rafPointer.writeLong(pointer);//(String.valueOf(filePointer));
			return pointer;
		}

		/**
		 * 从业务的角度上，判断什么是新的一行。
		 * @param str
		 * @return
		 */
		private boolean isNewLine(String lineText) {

			if( filedataValidator != null ) {
				return this.filedataValidator.isNewLine(lineText);
			}
			else {
				return true;
			}
		}

		/**
		 * 是否跳过当前行
		 * @param str
		 * @return
		 */
		@SuppressWarnings("unused")
		private boolean isSkipLine(String lineText) {

			return this.filedataValidator.isSkipLine(lineText);
		}

		public void kill() {

			this.reading = false;
			synchronized( this.getClass() ) {
				//在这里进行各种清理工作
			}
		}

		private JSONObject getParmJson2(List<String> listColumn, File file, String message) {

			JSONObject json = new JSONObject();
			if( "1".equals(read_mode) ) {//按行解析
				if( "0".equals(is_data_partition) ) {
					for(String column : listColumn) {
						if( "0".equals(column.split("`")[1]) ) {
							json.put(column.split("`")[0], message);
						}
					}
				}
				else {
					json.put("line", message);
				}
			}
			else {//按对象解析
				if( "0".equals(is_obj) ) {
					for(String column : listColumn) {
						if( "0".equals(column.split("`")[1]) ) {
							json.put(column.split("`")[0], message);
						}
					}
				}
				else {
					json.put("line", message);
				}
			}
			return json;
		}

	}

}