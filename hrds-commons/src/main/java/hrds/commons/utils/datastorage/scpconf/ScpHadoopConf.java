package hrds.commons.utils.datastorage.scpconf;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import fd.ng.core.annotation.DocClass;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Data_store_layer;
import hrds.commons.entity.Data_store_layer_attr;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.jsch.AgentDeploy;
import hrds.commons.utils.jsch.SftpOperate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

@DocClass(desc = "将存储层的配置文件cp到Agent目录", author = "Mr.Lee", createdate = "2020-02-17 13:31")
public class ScpHadoopConf {

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * agent需要的各种配置文件目录
	 */
	private static final String STORE_CONFIG_PATH = "storeConfigPath";

	/**
	 * 1 : 将配置目下的配置文件使用SCP的方式复制到Agent目录下 2 : 将配置文件复制到agent路径下后,将配置文件修改为Hadoop固定的配置文件名称
	 *
	 * @param targetPath  : 目标路径
	 * @param sftpOperate : sftp操作对象
	 */
	public static void scpConfToAgent(String targetPath, SftpOperate sftpOperate) {

		// FIXME 这里上传的存储文件地址如果是固定的,则取一次即可...待确定
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			List<Map<String, Object>> list =
				SqlOperator.queryList(
					db,
					"select t1.dsl_name,t2.storage_property_key,t2.storage_property_val from "
						+ Data_store_layer.TableName
						+ " t1 "
						+ "join "
						+ Data_store_layer_attr.TableName
						+ " t2 on t1.dsl_id = t2.dsl_id where t2.is_file = ?",
					IsFlag.Shi.getCode());

			String targetDir =
				targetPath + AgentDeploy.SEPARATOR + STORE_CONFIG_PATH + AgentDeploy.SEPARATOR;
			list.forEach(
				item -> {
					String dsl_name = ((String) item.get("dsl_name")).trim();
					String orginalFileName = ((String) item.get("storage_property_key")).trim();
					String localFilePath = ((String) item.get("storage_property_val")).trim();

					// 将要在远程机器创建的目录
					String targetMachineConf = targetDir + dsl_name;

					// 如果不含有当前目录,则在目标Agent目录下创建,反之直接将本地文件使用 SFTP方式传输到指定位置
					try {
						//检查当前本地文件是否存在,如果不存在则不做任何操作
						if (!new File(localFilePath).exists()) {
							LOGGER.info("本地文件: " + localFilePath + " 不存在,跳过!!!");
						} else {
							// 检查目录是否存在,不存在就创建目录
							sftpOperate.execCommandByJSchNoRs("mkdir -p " + targetMachineConf);
							LOGGER.info("开始传输集群XML: " + localFilePath + " 到目录 :" + targetMachineConf);
							// 将本地文件 sftp到远程目录下
							sftpOperate.channelSftp.put(localFilePath, targetMachineConf);
						}

						// 修改传输完成后的文件名称,传输过去的文件名称为md5文件名称
//						SFTPChannel.execCommandByJSch(
//							shellSession,
//							"mv "
//								+ targetMachineConf
//								+ AgentDeploy.SEPARATOR
//								+ new File(localFilePath).getName()
//								+ " "
//								+ targetMachineConf
//								+ AgentDeploy.SEPARATOR
//								+ orginalFileName);
					} catch (SftpException e) {
						LOGGER.error(e);
						throw new BusinessException("创建远程目录  " + targetMachineConf + "  失败!!!");
					} catch (Exception e) {
						LOGGER.error(e);
						throw new BusinessException(e.getMessage());
					}
				});
		}
	}

	private static boolean isExistDir(String path, ChannelSftp sftp) {
		boolean isExist = false;
		try {
			SftpATTRS sftpATTRS = sftp.lstat(path);
			isExist = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				isExist = false;
			}
		}
		return isExist;
	}
}
