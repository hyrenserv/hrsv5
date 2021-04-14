package hrds.agent.trans.biz.util;

import fd.ng.core.annotation.DocClass;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.exception.BusinessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@DocClass(desc = "编写生产者参数", author = "dhw", createdate = "2021/4/13 13:49")
public class WriterParam extends AgentBaseAction {

	private static final Logger logger = LogManager.getLogger();
	private static final String folder = System.getProperty("user.dir");

	public void writeProducerParam(String jobId, String param) {

		String fileName = jobId + ".txt";
		File file = new File(folder + File.separator + fileName);
		FileWriter fileWriter = null;
		try {
			if (!file.exists()) {
				if (!file.createNewFile()) {
					throw new BusinessException("创建文件失败：" + file.getPath());
				}
			}
			//覆盖方式写入
			fileWriter = new FileWriter(file, false);
			fileWriter.write(param);
		} catch (IOException e) {
			throw new BusinessException("写入producer配置文件失败！！！" + e.getMessage());
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

}
