package hrds.producer.avro.rest;

import com.alibaba.fastjson.JSONObject;
import hrds.commons.exception.BusinessException;
import hrds.producer.common.JobParamsEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerToProducer extends AbstractHandler {

	private static final transient Logger logger = LogManager.getLogger();
	private static ServerToProducer cI = null;
	private static final String folder = System.getProperty("user.dir");
	private static final Map<String, Map<ServerConnector, Server>> mapServer = new HashMap<>();
	private JobParamsEntity jobParams;
	RecvDataController recvDataController = new RecvDataController();

	public static ServerToProducer getInstance() {

		logger.info("已加载server");
		cI = new ServerToProducer();
		return cI;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		if (!"/favicon.ico".equals(request.getRequestURI())) {//去掉浏览器/favicon.ico的请求
			recvDataController.acceptorService(request, jobParams);
		}
		baseRequest.setHandled(true);
	}

	public void server(JSONObject json) throws Exception {

		//获取ip及端口信息
		JSONObject sdm_receive_conf = json.getJSONObject("sdm_receive_conf");
		String sdm_rec_port = sdm_receive_conf.getString("sdm_rec_port");
		String sdm_server_ip = sdm_receive_conf.getString("sdm_server_ip");
		String sdm_receive_id = sdm_receive_conf.getString("sdm_receive_id");

		//将producer的配置写入文件
		writeProducerParam(sdm_receive_id, json.toString());
		//获取jobId对应的producer
		ProducerOperatorAvro producerOperator = new ProducerOperatorAvro();
		jobParams = producerOperator.getMapParam(json);
		Server server;
		ServerConnector http;
		if (mapServer.isEmpty() || !mapServer.containsKey(sdm_receive_id)) {
			server = new Server();
			http = getHttp(sdm_server_ip, sdm_rec_port, server);
			// 启动服务
			logger.info("server.isRunning()1; " + server.isRunning());
			logger.info("server.isStarted()1; " + server.isStarted());
			server.addConnector(http);
			logger.info("server.isStopped(); " + server.isStopped());
			server.setHandler(cI);
			Map<ServerConnector, Server> map = new ConcurrentHashMap<>();
			map.put(http, server);
			mapServer.put(sdm_receive_id, map);
		} else {
			Map<ServerConnector, Server> map = mapServer.get(sdm_receive_id);
			ServerConnector oldhttp = map.keySet().iterator().next();
			server = map.get(oldhttp);
			http = getHttp(sdm_server_ip, sdm_rec_port, server);
			server.removeConnector(oldhttp);
			server.addConnector(http);
			server.stop();
			logger.info("server.isStarted(); " + server.isStarted());
			logger.info("server.isStopped(); " + server.isStopped());
		}
		logger.info("server是  " + server);
		server.start();
		logger.info("启动成功！" + sdm_rec_port);
//		server.join();
	}

	private ServerConnector getHttp(String ip, String port, Server server) {

		ServerConnector http = new ServerConnector(server);
		if (StringUtils.isBlank(port)) {
			http.setPort(8800);
		} else {
			http.setPort(Integer.parseInt(port));
		}
		//ip 从获取
		if (StringUtils.isBlank(ip)) {
			http.setHost("0.0.0.0");
		} else {
			http.setHost(ip);
		}
		return http;
	}

	public static void writeProducerParam(String jobId, String param) {

		String fileName = jobId + ".txt";
		File file = new File(folder + File.separator + fileName);
		FileWriter fileWriter = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			//覆盖方式写入
			fileWriter = new FileWriter(file, false);
			fileWriter.write(param);
		} catch (IOException e) {
			logger.error(e);
			throw new BusinessException("写入配置文件失败！！！" + e.getMessage());
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}

	}

	public static void main(String[] args) throws Exception {

		//String json = "{\"port\":\"8080\",\"ip\":\"0.0.0.0\"}";
		String json = "{\"msg_info_ls\":[{\"sdm_is_send\":\"0\",\"sdm_var_type\":\"3\",\"sdm_describe\":\"ddd\",\"sdm_var_name_en\":\"ddd\",\"sdm_var_name_cn\":\"dddddd\"}],\"sdm_receive_conf\":{\"sdm_server_ip\":\"172.168.0.203\",\"sdm_partition\":\"1\",\"sdm_receive_id\":\"1001\",	\"sdm_receive_name\":\"rest1\",\"sdm_rec_port\":\"9999\"},\"kafka_params\":{\"topic\":\"5122\",\"bootstrap.servers\":\"178.168.0.95:9092\",\"acks\":\"1\",\"retries\":\"0\",\"max.request.size\":\"1048576\",\"batch.size\":\"16384\",\"linger.ms\":\"1\",\"buffer.memory\":\"33554432\",\"key.serializer\":\"String\",\"value.serializer\":\"String\"}}";
		ServerToProducer s = new ServerToProducer();
		s.server(JSONObject.parseObject(json));

	}

}
