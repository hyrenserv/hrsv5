package hrds.producer.avro.file.dir;

import hrds.commons.utils.MapDBHelper;
import hrds.producer.common.FileDataValidator;
import hrds.producer.common.GetFileParams;
import hrds.producer.common.JobParamsEntity;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ReaderFileAvro implements Runnable {

	private JobParamsEntity jobParams;
	private List<String> listColumn;
	private File file;
	private File fileRename;
	private String readMode;
	private ConcurrentMap<String, String> htMap;
	private String charset;
	private String beforeLine;
	private ConcurrentMap<String, String> htMapThread;
	private FileDataValidator fileDataValidator;
	private MapDBHelper mapDBHelper;
	private GetFileParams getFileParams = new GetFileParams();

	public ReaderFileAvro(MapDBHelper mapDBHelper, ConcurrentMap<String, String> htmap, ConcurrentMap<String,
		String> htMapThread, String readMode, JobParamsEntity jobParams, File file, File fileRename,
	                      String charset, String beforeLine, FileDataValidator fileDataValidator) {
		this.mapDBHelper = mapDBHelper;
		this.jobParams = jobParams;
		this.listColumn = jobParams.getListColumn();
		this.file = file;
		this.fileRename = fileRename;
		this.readMode = readMode;
		this.htMap = htmap;
		this.charset = charset;
		this.beforeLine = beforeLine;
		this.htMapThread = htMapThread;
		this.fileDataValidator = fileDataValidator;
	}

	@Override
	public void run() {

		GenericRecord genericRecord = new GenericData.Record(jobParams.getSchema());
		genericRecord = getFileParams.getParmGenericRecordAvro(listColumn, file, genericRecord);
		ReadFileProcessor readFileProcessor = new ReadFileProcessor();
		if (readMode.equals("1")) {
			//按行读取
			readFileProcessor.lineProcessor(getFileParams, mapDBHelper, htMap, htMapThread, file, listColumn,
				jobParams, fileRename, charset, beforeLine, genericRecord, fileDataValidator);
		} else {
			//按类型读取
			readFileProcessor.objectProcessor(mapDBHelper, htMap, htMapThread, file, listColumn,
				jobParams, fileRename, charset, genericRecord);
		}
	}

}
