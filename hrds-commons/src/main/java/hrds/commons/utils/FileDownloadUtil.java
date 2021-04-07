package hrds.commons.utils;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.utils.CodecUtil;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.util.RequestUtil;
import fd.ng.web.util.ResponseUtil;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.jsch.SftpOperate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Base64;

@DocClass(desc = "文件下载工具类", author = "dhw", createdate = "2019/12/19 16:50")
public class FileDownloadUtil {

	private static final Logger logger = LogManager.getLogger();

	@Method(desc = "通过SFTP删除日志文件",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制" +
			"2.获取连接到sftp操作对象" +
			"3.通过SFTP删除日志文件" +
			"4.退出SFTP连接" +
			"5.关闭Channel连接")
	@Param(name = "directory", desc = "需要删除的文件目录", range = "取值范围")
	@Param(name = "sftpDetails", desc = "连接服务器配置信息", range = "无限制")
	public static void deleteLogFileBySFTP(String directory, SFTPDetails sftpDetails) {
		try {
			// 1.数据可访问权限处理方式，该方法不需要权限控制
			// 2.获取连接到sftp操作对象
			SftpOperate sftpOperate = new SftpOperate(sftpDetails);
			// 3.通过SFTP删除日志文件
			sftpOperate.channelSftp.rm(directory);
			logger.info("###########删除文件成功===");
			// 4.退出SFTP连接
			sftpOperate.channelSftp.quit();
			// 5.关闭Channel连接
			sftpOperate.close();
		} catch (Exception e) {
			throw new AppSystemException(e);
		}
	}

	@Method(desc = "下载日志文件",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制" +
			"2.通过本地路径获取本地文件" +
			"3.获取连接到sftp操作对象" +
			"4.通过sftp下载日志文件" +
			"5.退出SFTP连接" +
			"6.关闭Channel连接")
	@Param(name = "remotePath", desc = "远程路径", range = "无限制")
	@Param(name = "localPath", desc = "本地路径", range = "无限制")
	@Param(name = "sftpDetails", desc = "连接服务器配置信息", range = "无限制")
	public static void downloadLogFile(String remotePath, String localPath, SFTPDetails sftpDetails) {
		// 1.数据可访问权限处理方式，该方法不需要权限控制
		OutputStream outputStream = null;
		try {
			logger.info("==========文件下载远程路径remotePath=========" + remotePath);
			// 2.通过本地路径以获取本地文件
			localPath = localPath + remotePath.substring(remotePath.lastIndexOf(File.separator));
			logger.info("==========文件下载本地路径localPath=========" + localPath);
			File localFile = new File(localPath);
			outputStream = new FileOutputStream(localFile);
			// 3.获取连接到sftp操作对象
			SftpOperate sftpOperate = new SftpOperate(sftpDetails);
			// 4.通过sftp下载日志文件
			sftpOperate.channelSftp.get(remotePath, outputStream);
			logger.info("###########下载文件成功===");
			// 5.退出SFTP连接
			sftpOperate.channelSftp.quit();
			// 6.关闭Channel连接
			sftpOperate.close();
		} catch (FileNotFoundException e) {
			throw new BusinessException("找不到文件");
		} catch (Exception e) {
			logger.info("文件下载失败原因：" + e);
			throw new AppSystemException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	@Method(desc = "下载文件",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限验证" +
			"2.获取本地文件路径" +
			"3.清空response" +
			"4.设置响应头，控制浏览器下载该文件" +
			"4.1firefox浏览器" +
			"4.2其它浏览器" +
			"5.读取要下载的文件，保存到文件输入流" +
			"6.创建输出流" +
			"7.将输入流写入到浏览器中")
	@Param(name = "fileName", desc = "下载文件名", range = "无限制")
	public static void downloadFile(String fileName) {
		// 1.数据可访问权限处理方式，该方法不需要权限验证
		OutputStream out = null;
		InputStream in = null;
		try {
			// 2.获取本地文件路径
			String filePath = WebinfoConf.FileUpload_SavedDirName + File.separator + fileName;
			logger.info("=====本地下载文件路径=====" + filePath);
			// 3.清空response
			ResponseUtil.getResponse().reset();
			// 4.设置响应头，控制浏览器下载该文件
			if (RequestUtil.getRequest().getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				// 4.1firefox浏览器
				ResponseUtil.getResponse().setHeader("content-disposition", "attachment;filename="
					+ new String(fileName.getBytes(CodecUtil.UTF8_CHARSET), DataBaseCode.ISO_8859_1.getCode()));
			} else {
				// 4.2其它浏览器
				ResponseUtil.getResponse().setHeader("content-disposition", "attachment;filename="
					+ Base64.getEncoder().encodeToString(fileName.getBytes(CodecUtil.UTF8_CHARSET)));
			}
			ResponseUtil.getResponse().setContentType("APPLICATION/OCTET-STREAM");
			// 5.读取要下载的文件，保存到文件输入流
			in = new FileInputStream(filePath);
			// 6.创建输出流
			out = ResponseUtil.getResponse().getOutputStream();
			// 7.将输入流写入到浏览器中
			byte[] bytes = new byte[1024];
			int len;
			while ((len = in.read(bytes)) > 0) {
				out.write(bytes, 0, len);
			}
			out.flush();
		} catch (UnsupportedEncodingException e) {
			throw new BusinessException("不支持的编码异常");
		} catch (FileNotFoundException e) {
			throw new BusinessException("文件不存在，可能目录不存在！");
		} catch (IOException e) {
			throw new BusinessException("下载文件失败！");
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error(e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
}
