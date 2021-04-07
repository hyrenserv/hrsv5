package hrds.commons.utils.jsch;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Vector;

@DocClass(desc = "sftp远程创建文件拉取文件操作类", author = "zxz", createdate = "2019/10/11 11:29")
public class SftpOperate implements Closeable {
	// 打印日志
	private static final Logger logger = LogManager.getLogger();
	// 默认超时时间
	private static final int SFTP_TIME_OUT = 6 * 1000;
	// sftp连接的session
	public Session session;
	// sftp连接
	public ChannelSftp channelSftp;


	/**
	 * sftp远程创建文件拉取文件操作类构造方法,配置实体
	 * <p>1.初始化sftp连接的session 2.初始化sftp连接
	 *
	 * @param sftpDetails 配置实体
	 */
	public SftpOperate(SFTPDetails sftpDetails) {
		this(sftpDetails, SFTP_TIME_OUT);
	}

	/**
	 * sftp远程创建文件拉取文件操作类构造方法,配置实体
	 * <p>1.初始化sftp连接的session 2.初始化sftp连接
	 *
	 * @param sftpDetails 配置实体
	 */
	public SftpOperate(SFTPDetails sftpDetails, int time_out) {
		JSch jsch = new JSch(); // 创建JSch对象
		try {
			//创建session
			session = jsch.getSession(sftpDetails.getUser_name(), sftpDetails.getHost(), sftpDetails.getPort());
			logger.debug("Session created.");
			String ftpPassword = sftpDetails.getPwd();
			if (ftpPassword != null) {
				session.setPassword(ftpPassword); // 设置密码
			}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config); // 为Session对象设置properties
			session.setTimeout(time_out); // 设置timeout时间
			session.connect(); // 通过Session建立链接
			logger.debug("Session connected.");
			//创建 ChannelSftp
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
		} catch (JSchException e) {
			logger.error(e);
			throw new BusinessException("初始化sftp操作对象失败! " + e.getMessage());
		}
	}

	@Method(desc = "获取远程目录下的文件对象集合", logicStep = "1.调用方法全匹配远程目录下的文件对象集合")
	@Param(name = "srcDir", desc = "需要拉取的远程的目录", range = "不能为空")
	@Return(desc = "拉取到的远程的ls的对象的集合", range = "可能为空集合")
	public Vector<LsEntry> listDir(String srcDir) {
		// 1.调用方法全匹配远程目录下的文件对象集合
		return listDir(srcDir, "*");
	}

	@SuppressWarnings("unchecked")
	@Method(desc = "按照正则获取远程目录下的文件对象集合", logicStep = "1.判断需要获取的目录文件夹是否以/结尾，根据是否以/结尾拼接路径获取远程目录下文件的集合")
	@Param(name = "srcDir", desc = "需要拉取的远程的目录", range = "不能为空")
	@Param(name = "regex", desc = "匹配规则", range = "不能为空")
	@Return(desc = "拉取到的远程的ls的对象的集合", range = "可能为空集合")
	public Vector<LsEntry> listDir(String srcDir, String regex) {
		// 1.判断需要获取的目录文件夹是否以/结尾，根据是否以/结尾拼接路径获取远程目录下文件的集合
		try {
			if (srcDir.endsWith("/")) {
				return channelSftp.ls(srcDir + regex);
			} else {
				return channelSftp.ls(srcDir + "/" + regex);
			}
		} catch (SftpException e) {
			e.printStackTrace();
			logger.error("按照正则获取远程目录下的文件对象集合!" + e);
			throw new BusinessException("按照正则获取远程目录下的文件对象集合失败!");
		}
	}

	@Method(desc = "使用sftp拉取远程服务器上的文件到本地", logicStep = "1.使用sftp拉取远程服务器上的文件到本地")
	@Param(name = "srcFile", desc = "远程文件全路径", range = "不能为空")
	@Param(name = "destFile", desc = "本地目录", range = "不能为空")
	public void transferFile(String srcFile, String destFile) {
		try {
			channelSftp.get(srcFile, destFile);
		} catch (SftpException e) {
			e.printStackTrace();
			logger.error("使用sftp拉取远程服务器上的文件到本地! " + e);
			throw new BusinessException("使用sftp拉取远程服务器上的文件到本地!" + e.getMessage());
		}
	}

	@Method(desc = "使用sftp推送本地文件到远程服务器", logicStep = "1.使用sftp推送本地文件到远程服务器")
	@Param(name = "srcFile", desc = "本地文件全路径", range = "不能为空")
	@Param(name = "destFile", desc = "远程目录", range = "不能为空")
	public void transferPutFile(String srcFile, String destFile) {
		try {
			channelSftp.put(srcFile, destFile);
		} catch (SftpException e) {
			e.printStackTrace();
			logger.error("使用sftp推送本地文件到远程服务器失败! " + e);
			throw new BusinessException("使用sftp推送本地文件到远程服务器失败!" + e.getMessage());
		}
	}

	@Method(desc = "使用sftp远程创建目录", logicStep = "1.拼接创建文件夹的命令，使用SFTPChannel执行")
	@Param(name = "currentLoadDir", desc = "需要被创建的远程目录", range = "不能为空")
	public void scpMkdir(String currentLoadDir) {
		// 1.拼接创建文件夹的命令，使用SFTPChannel执行
		String mkdir = "mkdir -p " + currentLoadDir;
		try {
			execCommandByJSchNoRs(mkdir);
		} catch (JSchException | IOException | InterruptedException e) {
			e.printStackTrace();
			throw new BusinessException("使用sftp远程创建目录: " + currentLoadDir + " 失败!");
		}
	}

	/**
	 * exec 执行命令,需要返回值
	 *
	 * @param command 命令
	 */
	public String execCommandByJSch(String command) throws IOException, JSchException {
		String result;
		command = FileNameUtils.normalize(command, true);
		logger.info("执行命令为: " + command);
		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		InputStream in = channelExec.getInputStream();
		channelExec.setCommand(command);
		channelExec.setErrStream(System.err);
		channelExec.connect();
		channelExec.disconnect();
		result = IOUtils.toString(in, StandardCharsets.UTF_8);
		return result;
	}

	/**
	 * 执行命令,不需要返回值
	 *
	 * @param command 命令
	 */
	public void execCommandByJSchNoRs(String command) throws JSchException, IOException, InterruptedException {
		logger.info("执行命令为: " + command);
		ChannelExec channelExec;
		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.getInputStream();
		channelExec.setCommand(command);
		channelExec.setErrStream(System.err);
		channelExec.connect();
		Thread.sleep(1000);
		channelExec.disconnect();
	}

	/**
	 * 执行本地shell命令
	 *
	 * @param executeShell shell命令
	 */
	public void executeLocalShell(String executeShell) throws IOException, InterruptedException {
		logger.info("执行命令为 ：" + executeShell);
		//executeShell linux命令  多个命令可用 " ; " 隔开
		Process ps = Runtime.getRuntime().exec((new String[]{"sh", "-l", "-c", executeShell}));
		ps.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}
		if (!StringUtil.isEmpty(sb.toString())) {
			throw new AppSystemException("Linux命令" + executeShell + "执行失败，" + sb.toString());
		}
	}

	/**
	 * exec 执行命令,根据命令返回的信息返回结果
	 *
	 * @param command 命令
	 * @return String
	 */
	public String execCommandByJSchToReadLine(String command) throws JSchException, IOException, InterruptedException {
		logger.info("执行命令为 : " + command);
		ChannelExec channelExec;
		StringBuilder result = new StringBuilder();
		channelExec = (ChannelExec) session.openChannel("exec");
		InputStream inputStream = channelExec.getInputStream(); // 从远程端到达的所有数据都能从这个流中读取到
		OutputStream outputStream = channelExec.getOutputStream(); // 写入该流的所有数据都将发送到远程端。
		// 使用PrintWriter流的目的就是为了使用println这个方法
		// 好处就是不需要每次手动给字符串加\n
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.println(command);
		Thread.sleep(3000);
		printWriter.println("exit"); // 加上个就是为了，结束本次交互
		printWriter.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String msg;
		while ((msg = in.readLine()) != null) {
			result.append(msg);
		}
		in.close();
		channelExec.disconnect();
		return result.toString();
	}

	/**
	 * 根据参数设置 SFTPDetails
	 *
	 * @param ftpHost     String
	 * @param ftpUserName String
	 * @param ftpPassword String
	 * @param ftpPort     int
	 * @return SFTPDetails Map<String, String>
	 */
	public static SFTPDetails getSftpDetails(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort) {
		return new SFTPDetails(ftpHost, ftpUserName, ftpPassword, ftpPort);
	}

	@Method(desc = "实现Closeable重写的方法，try中构造这个对象，结束方法后会自动调用这个方法",
		logicStep = "1.sftp不为空关闭sftp连接" + "2.session不为空关闭session回话连接")
	@Override
	public void close() {
		// 1.sftp不为空关闭sftp连接
		if (channelSftp != null) {
			channelSftp.disconnect();
		}
		// 2.session不为空关闭session回话连接
		if (session != null) {
			session.disconnect();
		}
	}
}
