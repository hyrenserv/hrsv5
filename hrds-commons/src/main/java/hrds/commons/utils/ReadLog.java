package hrds.commons.utils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import hrds.commons.utils.jsch.SFTPChannel;
import hrds.commons.utils.jsch.SFTPDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@DocClass(desc = "日志读取工具类")
public class ReadLog {

  private static final Logger logger = LogManager.getLogger();

  @Method(
      desc = "根据日志文件路径、agent_ip,agent_port,userName,password读取日志",
      logicStep =
          ""
              + "1、构建使用jsch访问日志的Map集合"
              + "2、调用方法获取jSchSession"
              + "3、拼接读取日志的linux命令"
              + "4、读取日志"
              + "5、返回日志信息")
  @Param(name = "logPath", desc = "日志文件路径", range = "不为空")
  @Param(name = "ip", desc = "agent所在服务器ip", range = "不为空,格式为 : XXX.XXX.XXX.XXX")
  @Param(name = "port", desc = "agent提供服务的端口", range = "")
  @Param(name = "userName", desc = "jsch使用的userName", range = "不为空")
  @Param(name = "password", desc = "jsch使用的password", range = "不为空")
  @Param(name = "readNum", desc = "读取日志文件的行数", range = "不为空")
  @Return(desc = "日志信息", range = "不为空，如果没有日志信息，返回值为空字符串")
  public static String readAgentLog(String logPath, SFTPDetails sftpDetails, int readNum) {
    String execCommandByJSch;
    Session jSchSession = null;

    try {
      // 2、调用方法获取jSchSession
      jSchSession = SFTPChannel.getJSchSession(sftpDetails, 0);
      // 3、拼接读取日志的linux命令
      String readShell = "cat " + logPath + " |tail -n " + readNum;
      // 4、读取日志
      execCommandByJSch = SFTPChannel.execCommandByJSch(jSchSession, readShell);
    } catch (JSchException ex) {
      execCommandByJSch = "读取Agent日志失败!";
      logger.error("登录验证失败...", ex);
    } catch (IOException ex) {
      execCommandByJSch = "读取Agent日志失败!";
      logger.error("读取日志文件-----" + logPath + "-----失败...", ex);
    } finally {
      if (jSchSession != null) {
        jSchSession.disconnect();
      }
    }
    // 5、返回日志信息
    return execCommandByJSch;
  }
}
