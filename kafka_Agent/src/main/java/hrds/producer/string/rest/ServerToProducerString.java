package hrds.producer.string.rest;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.CodecUtil;
import hrds.producer.common.JobParamsEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@DocClass(desc = "", author = "dhw", createdate = "2021/4/13 14:03")
public class ServerToProducerString extends AbstractHandler {

	private static final transient Logger logger = LogManager.getLogger();
	private static ServerToProducerString cI = null;
	private static final Map<String, Map<ServerConnector, Server>> mapServer = new HashMap<>();
	private JobParamsEntity jobParams;
	RecvDataControllerString recvDataController = new RecvDataControllerString();

	public static ServerToProducerString getInstance() {

		logger.info("已加载server");
		cI = new ServerToProducerString();
		return cI;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {

		response.setCharacterEncoding(CodecUtil.UTF8_STRING);
		response.setContentType("text/html");
		response.setCharacterEncoding(CodecUtil.UTF8_STRING);
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
		//获取jobId对应的producer
		ProducerOperatorString producerOperatorString = new ProducerOperatorString();
		jobParams = producerOperatorString.getMapParam(json);
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
			ServerConnector oldHttp = map.keySet().iterator().next();
			server = map.get(oldHttp);
			http = getHttp(sdm_server_ip, sdm_rec_port, server);
			server.removeConnector(oldHttp);
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

}
