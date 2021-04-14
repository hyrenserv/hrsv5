package hrds.producer.common;

import com.alibaba.fastjson.JSONObject;
import hrds.commons.codes.IsFlag;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;

public class GetFileParams {

	private static Log logger = LogFactory.getLog(GetFileParams.class);

	public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

	public GenericRecord getRealGenericRecordAvro(JobParamsEntity jobParams, StringBuilder lineBuffer, String charset, List<String> listColumn,
	                                              GenericRecord genericRecord) throws Exception {
		String message = new String(lineBuffer.toString().getBytes("ISO-8859-1"), charset);
		//单行数据分隔is_data_partition 0是 1否
		String sdmDatelimiter = jobParams.getSdmDatelimiter();
		if (!StringUtils.isBlank(sdmDatelimiter)) {
			String[] columns = StringUtils.splitPreserveAllTokens(message, sdmDatelimiter);
			int i = 0;
			for (String column : columns) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(listColumn.get(i).split("`")[1])) {
					genericRecord.put(listColumn.get(i).split("`")[0], column);
				}
				i++;
			}
		} else {
			genericRecord.put("line", message);
		}
		String cusDesType = jobParams.getCusDesType();
		if (!StringUtils.isBlank(cusDesType)) {
			if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
				genericRecord = jobParams.getBusinessProcess().process(listColumn, genericRecord);
			} else {
				//			var recordFunction = function(genericRecord){var avroecord = genericRecord;}
				genericRecord = (GenericRecord) jobParams.getInvocable().invokeFunction("recordFunction", genericRecord);
			}
		}
		return genericRecord;
	}

	public GenericRecord getRealGenericRecordAvro(JobParamsEntity jobParams, StringBuilder lineBuffer, String charset, List<String> listColumn,
	                                              GenericRecord genericRecord, String read_mode) throws Exception {
		String message = new String(lineBuffer.toString().getBytes("ISO-8859-1"), charset);
		if ("1".equals(read_mode)) {
			//单行数据分隔is_data_partition 0是 1否
			String sdmDatelimiter = jobParams.getSdmDatelimiter();
			if (!StringUtils.isBlank(sdmDatelimiter)) {
				String[] columns = StringUtils.splitPreserveAllTokens(message, sdmDatelimiter);
				int i = 0;
				for (String column : columns) {
					if (IsFlag.Fou == IsFlag.ofEnumByCode(listColumn.get(i).split("`")[1])) {
						genericRecord.put(listColumn.get(i).split("`")[0], column);
					}
					i++;
				}
			} else {
				genericRecord.put("line", message);
			}
		} else {//按对象解析
			boolean flag = true;
			JSONObject object = JSONObject.parseObject(message);
			for (String column : listColumn) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
					flag = false;
					genericRecord.put(column.split("`")[0], object.getString(column.split("`")[0]));
				}
			}
			if (flag) {
				genericRecord.put("line", message);
			}
		}
		String cusDesType = jobParams.getCusDesType();
		if (!StringUtils.isBlank(cusDesType)) {
			if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
				genericRecord = jobParams.getBusinessProcess().process(listColumn, genericRecord);
			} else {
				//			var recordFunction = function(genericRecord){var avroecord = genericRecord;}
				genericRecord = (GenericRecord) jobParams.getInvocable().invokeFunction("recordFunction", genericRecord);
			}
		}
		return genericRecord;
	}

	public GenericRecord getParmGenericRecordAvro(List<String> listColumn, File file, GenericRecord genericRecord) {

		if (listColumn.contains("file_attr_ip")) {
			String iplocal = getIp();
			genericRecord.put("file_attr_ip", iplocal);
		}
		if (listColumn.contains("file_name")) {
			genericRecord.put("file_name", file.getName());
		}
		if (listColumn.contains("file_size")) {
			genericRecord.put("file_size", String.valueOf(file.length()));
		}
		if (listColumn.contains("file_time")) {
			genericRecord.put("file_time", sdf.format(file.lastModified()));
		}
		if (listColumn.contains("full_path")) {
			genericRecord.put("full_path", file.getParent());
		}

		return genericRecord;
	}

	public JSONObject getRealJson(JobParamsEntity jobParams, StringBuilder lineBuffer, String charset, List<String> listColumn,
	                              JSONObject json, String read_mode) throws Exception {

		JSONObject jsonReal = new JSONObject();
		jsonReal.putAll(json);
		String message = new String(lineBuffer.toString().getBytes("ISO-8859-1"), charset);
		if ("1".equals(read_mode)) {//按行解析
			String sdmDatelimiter = jobParams.getSdmDatelimiter();
			if (!StringUtils.isBlank(sdmDatelimiter)) {
				String[] columns = StringUtils.splitPreserveAllTokens(message, sdmDatelimiter);
				int i = 0;
				for (String column : columns) {
					if (IsFlag.Fou == IsFlag.ofEnumByCode(listColumn.get(i).split("`")[1])) {
						jsonReal.put(listColumn.get(i).split("`")[0], column);
					}
					i++;
				}
			} else {
				jsonReal.put("line", message);
			}
		} else {//按对象解析
			boolean flag = true;
			JSONObject object = JSONObject.parseObject(message);
			for (String column : listColumn) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
					flag = false;
					jsonReal.put(column.split("`")[0], object.getString(column.split("`")[0]));
				}
			}
			if (flag) {
				jsonReal.put("line", message);
			}
		}
		String cusDesType = jobParams.getCusDesType();
		if (!StringUtils.isBlank(cusDesType)) {
			if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
				jsonReal = jobParams.getBusinessProcess().process(listColumn, jsonReal);
			} else {
				jsonReal = (JSONObject) jobParams.getInvocable().invokeFunction("recordFunction", jsonReal);
			}
		}
		return jsonReal;
	}

	public JSONObject getRealJson(JobParamsEntity jobParams, StringBuilder lineBuffer, String charset, List<String> listColumn,
	                              JSONObject json) throws Exception {

		JSONObject jsonReal = new JSONObject();
		jsonReal.putAll(json);
		String message = new String(lineBuffer.toString().getBytes("ISO-8859-1"), charset);
		String sdmDatelimiter = jobParams.getSdmDatelimiter();
		if (!StringUtils.isBlank(sdmDatelimiter)) {
			String[] columns = StringUtils.splitPreserveAllTokens(message, sdmDatelimiter);
			int i = 0;
			for (String column : columns) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(listColumn.get(i).split("`")[1])) {
					jsonReal.put(listColumn.get(i).split("`")[0], column);
				}
				i++;
			}
		} else {
			jsonReal.put("line", message);
		}
		String cusDesType = jobParams.getCusDesType();
		if (!StringUtils.isBlank(cusDesType)) {
			if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
				jsonReal = jobParams.getBusinessProcess().process(listColumn, jsonReal);
			} else {
				jsonReal = (JSONObject) jobParams.getInvocable().invokeFunction("recordFunction", jsonReal);
			}
		}
		return jsonReal;
	}

	public JSONObject getParmJson(List<String> listColumn, File file) {

		JSONObject json = new JSONObject();
		if (listColumn.contains("file_attr_ip")) {
			String iplocal = getIp();
			json.put("file_attr_ip", iplocal);
		}
		if (listColumn.contains("file_name")) {
			json.put("file_name", file.getName());
		}
		if (listColumn.contains("file_size")) {
			json.put("file_size", String.valueOf(file.length()));
		}
		if (listColumn.contains("file_time")) {
			json.put("file_time", String.valueOf(file.lastModified()));
		}
		if (listColumn.contains("full_path")) {
			json.put("full_path", file.getParent());
		}

		return json;
	}

	private String getIp() {

		InetAddress addr;
		String iplocal = null;
		try {
			addr = InetAddress.getLocalHost();
			iplocal = addr.getHostAddress();
		} catch (UnknownHostException e1) {
			logger.error("获取主机ip异常：", e1);
			System.exit(-2);
		}
		return iplocal;
	}

	public static void main(String[] args) {

		String aa = "a||s|||||c";
		System.out.println(StringUtils.splitPreserveAllTokens(aa, "|").length);
	}
}
