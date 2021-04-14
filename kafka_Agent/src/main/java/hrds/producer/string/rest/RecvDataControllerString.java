package hrds.producer.string.rest;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.CodecUtil;
import fd.ng.web.fileupload.FileItem;
import fd.ng.web.fileupload.FileUploadException;
import fd.ng.web.fileupload.disk.DiskFileItemFactory;
import fd.ng.web.fileupload.servlet.ServletFileUpload;
import hrds.commons.codes.IsFlag;
import hrds.producer.common.JobParamsEntity;
import hrds.producer.common.KafkaProducerWorker;
import hrds.producer.common.Response_result;
import hrds.producer.common.StatusDefine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class RecvDataControllerString {

	private static final Log logger = LogFactory.getLog(RecvDataControllerString.class);
	private KafkaProducerWorker kafkaProducerWorkerString = new KafkaProducerWorker();

	/**
	 * rest接收数据端
	 *
	 * @param request
	 * @param jobParams 任务参数
	 * @return
	 */
	public String acceptorService(HttpServletRequest request, JobParamsEntity jobParams) {
		Response_result resp_json = new Response_result();
		String backData = null;
		String message = null;
		List<String> listColumn = null;
		JSONObject json = new JSONObject();
		try {
			// 把httpservlet请求转换为多表单请求
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				json = getFileStream(json, request);
			} else {
				listColumn = jobParams.getListColumn();
				if (jobParams.getMsgType().equals("1")) {
					String value = request.getParameter(jobParams.getMsgHeader());
					JSONObject jsonValue = JSONObject.parseObject(value);
					for (String column : listColumn) {
						if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
							String parameter = jsonValue.getString(column.split("`")[0]);
							json.put(column.split("`")[0], parameter);
						}
					}
				} else {
					for (String column : listColumn) {
						if (IsFlag.Fou == IsFlag.ofEnumByCode(column.split("`")[1])) {
							Object parameter = request.getParameter(column.split("`")[0]);
							json.put(column.split("`")[0], parameter);
						}
					}
				}
			}
			//自定义业务处理类
			String cusDesType = jobParams.getCusDesType();
			if (!StringUtils.isBlank(cusDesType)) {
				if (IsFlag.Fou == IsFlag.ofEnumByCode(cusDesType)) {
					json = jobParams.getBusinessProcess().process(listColumn, json);
				} else {
					json = (JSONObject) jobParams.getInvocable().invokeFunction("recordFunction", json);
				}
			}
			if (!kafkaProducerWorkerString.sendToKafka(request.getRequestURL().toString(), jobParams.getProducerString(), json.toString(),
				jobParams.getTopic(), jobParams.getCustomerPartition(), jobParams.getBootstrapServers(), jobParams.getSync())) {
				logger.error("进kafka失败！！！");
				message = "kafka缓存时出现错误！！！";
				resp_json.setMessage(message);
				return resp_json.toString();
			}

			StatusDefine.success(resp_json, backData, message);
		} catch (JSONException e) {
			logger.error("结构化数据不能转换为json字符串，请使用json字符串上传结构化数据", e);
			message = "结构化数据不能转换为json字符串，请使用json字符串上传结构化数据。";
			StatusDefine.error(resp_json, e, message);
			return resp_json.toString();
		} catch (ClassCastException e) {
			logger.error("不是多表单请求，请使用多表单请求上传文件", e);
			message = "不是多表单请求，请使用多表单请求上传文件。";
			StatusDefine.error(resp_json, e, message);
			return resp_json.toString();
		} catch (NullPointerException e) {
			logger.error("未知的关键字为空，请优先确认jobKey是否存在", e);
			message = "未知的关键字为空，请优先确认jobKey是否存在";
			StatusDefine.error(resp_json, e, message);
			return resp_json.toString();
		} catch (Exception e) {
			logger.error("未知的服务端错误", e);
			message = "未知的服务端错误";
			StatusDefine.fail(resp_json, e, message);
			return resp_json.toString();
		}
		return resp_json.toString();
	}

	private static JSONObject getFileStream(JSONObject json, HttpServletRequest request) {

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		// 指定单个上传文件的最大尺寸,单位:字节，这里设为50Mb
		upload.setFileSizeMax(50 * 1024 * 1024);

		// 指定一次上传多个文件的总尺寸,单位:字节，这里设为50Mb
		upload.setSizeMax(50 * 1024 * 1024);
		upload.setHeaderEncoding(CodecUtil.UTF8_STRING);
		try {
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem fileItem : list) {
				String fieldName = fileItem.getFieldName();
				if (!fileItem.isFormField()) {
					InputStream inputStream = fileItem.getInputStream();
					ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
					byte[] b = new byte[1000];
					int n;
					while ((n = inputStream.read(b)) != -1) {
						bos.write(b, 0, n);
					}
					bos.close();
					byte[] byteArray = bos.toByteArray();
					json.put(fieldName, ByteBuffer.wrap(byteArray));
				} else {
					String msg = fileItem.getString();
					json.put(fieldName, msg);
				}
			}
		} catch (FileUploadException | IOException e) {
			logger.error(e.getMessage(), e);
		}
		return json;
	}

}