package hrds.agent.trans.biz.datamessagestream;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import hrds.agent.trans.biz.util.WriterParam;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.codes.ExecuteWay;
import hrds.commons.exception.BusinessException;
import hrds.producer.avro.rest.ServerToProducer;
import hrds.producer.string.rest.ServerToProducerString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DocClass(desc = "数据消息流kafka启动信息", author = "dhw", createdate = "2021/4/13 10:51")
public class DataMessageStreamInfo extends AgentBaseAction {

	private static final Logger logger = LogManager.getLogger();

	@Method(desc = "数据消息流kafka启动", logicStep = "")
	@Param(name = "kafkaParams", desc = "数据消息流kafka启动参数", range = "无限制")
	public void execute(String kafkaParams) {
		JSONObject json = JSONObject.parseObject(kafkaParams);
		JSONObject jsonConf = json.getJSONObject("sdm_receive_conf");
		String sdm_receive_id = jsonConf.getString("sdm_receive_id");//任务id
		String run_way = jsonConf.getString("run_way");//启动方式
		if (ExecuteWay.AnShiQiDong == ExecuteWay.ofEnumByCode(run_way)) {
			String serializerType = json.getJSONObject("kafka_params").getString("value.serializer");
			long startTime = System.currentTimeMillis();
			logger.info("beginTime：" + startTime);
			try {
				if ("Avro".equals(serializerType)) {
					ServerToProducer instance = ServerToProducer.getInstance();
					instance.server(json);
				} else {
					ServerToProducerString instance = ServerToProducerString.getInstance();
					instance.server(json);
				}
			} catch (Exception e) {
				logger.error(e);
				throw new BusinessException("启动kafka我服务失败" + e.getMessage());
			}
		} else {//命令触发，写文件
			WriterParam writerParam = new WriterParam();
			writerParam.writeProducerParam(sdm_receive_id, kafkaParams);
		}
	}
}
