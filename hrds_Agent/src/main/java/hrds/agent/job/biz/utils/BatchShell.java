package hrds.agent.job.biz.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.hadoop_helper.HdfsOperator;
import hrds.commons.hadoop.readconfig.ConfigReader;
import hrds.commons.utils.jsch.FileProgressMonitor;
import hrds.commons.utils.jsch.SFTPChannel;
import hrds.commons.utils.jsch.SFTPDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

@DocClass(desc = "拼接执行命令的工具类", author = "zxz", createdate = "2019/11/1 10:18")
public class BatchShell {
	// 打印日志
	private static final Log logger = LogFactory.getLog(BatchShell.class);

	@Method(desc = "执行上传文件到远程服务器或者hdfs", logicStep = "1.判断是否有hadoop环境，调用对应的方法")
	@Param(name = "localPath", desc = "本地文件路径", range = "不可为空")
	@Param(name = "hdfsPath", desc = "hdfs文件路径", range = "不可为空")
	public static void execStationaryHDFSShell(
		String localPath, String hdfsPath, Boolean has_hadoop_env) {
		// TODO 这里上传到本地还是HDFS是页面上选的吗？
		// 1.判断是否有hadoop环境，调用对应的方法
		if (has_hadoop_env) {
			copyFileToHDFS(localPath, hdfsPath);
		} else {
			copyFileToRemote(localPath, hdfsPath);
		}
	}

	@Method(desc = "执行上传文件到hdfs", logicStep = "1.调用hdfs操作类将本地文件上传到hdfs")
	@Param(name = "localPath", desc = "本地文件路径", range = "不可为空")
	@Param(name = "hdfsPath", desc = "hdfs文件路径", range = "不可为空")
	private static void copyFileToHDFS(String localPath, String hdfsPath) {
		// 1.调用hdfs操作类将本地文件上传到hdfs TODO
		try (HdfsOperator operator = new HdfsOperator(System.getProperty("user.dir") + File.separator + "conf" + File.separator,
			PropertyParaUtil.getString("platform", ConfigReader.PlatformType.normal.toString()),
			PropertyParaUtil.getString("principle.name", "admin@HADOOP.COM"),
			PropertyParaUtil.getString("HADOOP_USER_NAME", "hyshf"))) {
			operator.upLoad(localPath, hdfsPath, true);
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			} else {
				throw new AppSystemException(e);
			}
		}
	}

	@Method(
		desc = "执行上传文件到远程服务器",
		logicStep =
			"1.判断是否有hadoop环境，调用对应的方法" + "2.为了防止远程文件夹不存在，先创建文件夹" + "3.传输文件到远程服务器，并设置监控，打印传输文件百分比")
	@Param(name = "localPath", desc = "本地文件路径", range = "不可为空")
	@Param(name = "hdfsPath", desc = "hdfs文件路径", range = "不可为空")
	private static void copyFileToRemote(String localPath, String remotePath) {
		try {
			// 1.构建连接远程机器的对象
			SFTPDetails sftpDetails = new SFTPDetails();
			sftpDetails.setHost(PropertyParaUtil.getString("hyren_host", ""));
			sftpDetails.setPort(Integer.parseInt(PropertyParaUtil.getString("sftp_port", "22")));
			sftpDetails.setUser_name(PropertyParaUtil.getString("hyren_user", ""));
			sftpDetails.setPwd(PropertyParaUtil.getString("hyren_pwd", ""));
			Session shellSession = SFTPChannel.getJSchSession(sftpDetails, 0);
			// 2.为了防止远程文件夹不存在，先创建文件夹
			String mkdir = "mkdir -p " + remotePath;
			String execCommandByJSch = SFTPChannel.execCommandByJSch(shellSession, mkdir);
			logger.info(execCommandByJSch);
			long fileSize = new File(localPath).length();
			// 3.传输文件到远程服务器，并设置监控，打印传输文件百分比
			SFTPChannel channel = new SFTPChannel();
			ChannelSftp chSftp = channel.getChannel(sftpDetails, 60000);
			chSftp.put(localPath, remotePath, new FileProgressMonitor(fileSize), ChannelSftp.OVERWRITE);
			chSftp.quit();
			channel.closeChannel();
		} catch (Exception e) {
			logger.error("在远程机器 " + PropertyParaUtil.getString("hyren_host", "") + " 执行命令异常 ", e);
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			} else {
				throw new AppSystemException(e);
			}
		}
	}
}
