package hrds.c.biz.etlsys;

import com.jcraft.jsch.JSchException;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.resultset.Result;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.util.Dbo;
import hrds.c.biz.util.ETLAgentDeployment;
import hrds.c.biz.util.ETLJobUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.Job_Status;
import hrds.commons.codes.Main_Server_Sync;
import hrds.commons.codes.Pro_Type;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.*;
import hrds.commons.utils.jsch.SFTPDetails;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DocClass(desc = "作业调度工程", author = "dhw", createdate = "2019/11/25 15:48")
public class EltSysAction extends BaseAction {

	private static final Logger logger = LogManager.getLogger();

	private static final String separator = File.separator;

	@Method(desc = "查询作业调度工程信息", logicStep = "1.数据可访问权限处理方式，根据user_id进行权限控制" + "2.查询工程信息")
	@Return(desc = "返回工程信息", range = "无限制")
	public Result searchEtlSys() {
		// 1.数据可访问权限处理方式，根据user_id进行权限控制
		// 2.查询工程信息
		return Dbo.queryResult(
			"select etl_sys_cd,etl_sys_name,comments,curr_bath_date,sys_run_status from "
				+ Etl_sys.TableName + " where user_id=? order by etl_sys_cd", getUserId());
	}

	@Method(desc = "根据工程编号查询作业调度工程信息",
		logicStep = "1.数据可访问权限处理方式，根据user_id进行权限控制"
			+ "2.验证当前工程是否存在"
			+ "3.根据工程编号查询工程信息"
			+ "4.判断remarks是否为空，不为空则分割获取部署工程的redis ip与port并封装数据返回")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Return(desc = "返回根据工程编号查询的工程信息", range = "无限制")
	public Map<String, Object> searchEtlSysById(String etl_sys_cd) {
		// 1.数据可访问权限处理方式，根据user_id进行权限控制
		// 2.根据工程编号查询工程信息
		Etl_sys etl_sys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
		// 系统默认作业部署路径
		String etlDeployPath = PropertyParaValue.getString("etlDeployPath", "/home/hyshf/");
		Map<String, Object> etlSysMap = new HashMap<>();
		if (StringUtil.isBlank(etl_sys.getServ_file_path()) ||
			etl_sys.getServ_file_path().equals(etlDeployPath)) {
			// 部署路径等于默认路径或部署路径为空则为默认部署路径
			etlSysMap.put("isCustomize", IsFlag.Fou.getCode());
		} else {
			etlSysMap.put("isCustomize", IsFlag.Shi.getCode());
		}
		etlSysMap.put("etlSys", etl_sys);
		return etlSysMap;
	}

	@Method(
		desc = "新增保存作业调度工程信息",
		logicStep =
			"1.数据可访问权限处理方式，该方法不需要权限控制"
				+ "2.检查作业调度工程字段合法性"
				+ "3.设置一些默认参数"
				+ "4.检查工程编号是否已存在，存在不能新增"
				+ "5.新增保存工程"
				+ "6.每个工程创建时,都默认创建2种资源类型"
				+ "7.遍历获取资源类型"
				+ "8.循环新增保存资源")
	@Param(name = "etl_sys", desc = "作业调度工程登记表", range = "与数据库对应表规则一致", isBean = true)
	public void addEtlSys(Etl_sys etl_sys) {
		// 1.数据可访问权限处理方式，该方法不需要权限控制
		// 2.检查作业调度工程字段合法性
		checkEtlSysField(etl_sys.getEtl_sys_cd(), etl_sys.getEtl_sys_name());
		// 3.设置一些默认参数
		etl_sys.setUser_id(getUserId());
		etl_sys.setBath_shift_time(DateUtil.getSysDate());
		etl_sys.setMain_serv_sync(Main_Server_Sync.YES.getCode());
		etl_sys.setSys_run_status(Job_Status.STOP.getCode());
		// 跑批日期默认为当天
		etl_sys.setCurr_bath_date(DateUtil.getSysDate());
		// 4.检查工程编号是否已存在，存在不能新增,这里user_id传值为空，因为不管是什么用户工程编号都不能重复
		if (ETLJobUtil.isEtlSysExist(etl_sys.getEtl_sys_cd(), Dbo.db())) {
			throw new BusinessException("工程编号已存在，不能新增！");
		}
		// 5.新增保存工程
		etl_sys.add(Dbo.db());
		// 6.每个工程创建时,都默认创建2种资源类型
		String[] paraType = {Pro_Type.Thrift.getCode(), Pro_Type.Yarn.getCode()};
		// 7.遍历获取资源类型
		for (String para_type : paraType) {
			Etl_resource resource = new Etl_resource();
			resource.setEtl_sys_cd(etl_sys.getEtl_sys_cd());
			resource.setResource_type(para_type);
			// 默认服务器同步标志位同步
			resource.setMain_serv_sync(Main_Server_Sync.YES.getCode());
			// 默认分配最大资源为10
			resource.setResource_max(10);
			resource.setResource_used(0);
			// 8.循环新增保存资源
			resource.add(Dbo.db());
		}
	}

	@Method(
		desc = "检查作业调度工程字段合法性",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制" + "2.验证工程名称是否为空" + "3.验证工程编号是否为数字英文下划线")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "etl_sys_name", desc = "工程名称", range = "新增工程时生成")
	private void checkEtlSysField(String etl_sys_cd, String etl_sys_name) {
		// 1.数据可访问权限处理方式，该方法不需要权限控制
		// 2.验证工程名称是否为空
		Validator.notBlank(etl_sys_name, "作业调度工程名不能为空！");
		// 3.验证工程编号是否为数字英文下划线
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z_]+$");
		Matcher matcher = pattern.matcher(etl_sys_cd);
		if (!matcher.matches()) {
			throw new BusinessException("工程编号只能为数字英文下划线！");
		}
	}

	@Method(
		desc = "更新保存作业调度工程",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.更新保存工程")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "etl_sys_name", desc = "工程名称", range = "新增工程时生成")
	@Param(name = "comments", desc = "工程描述", range = "无限制", nullable = true)
	public void updateEtlSys(String etl_sys_cd, String etl_sys_name, String comments) {
		// 1.数据可访问权限处理方式，该方法不需要权限控制
		// 2.更新保存工程
		DboExecute.updatesOrThrow("更新保存失败!", "update " + Etl_sys.TableName
				+ " set etl_sys_name=?,comments=? where etl_sys_cd=? and user_id=?",
			etl_sys_name, comments, etl_sys_cd, getUserId());
	}

	@Method(desc = "部署作业调度工程",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.判断系统是否正在运行中，如果正在运行中不允许部署"
			+ "3.部署作业工程"
			+ "4.部署成功，更新用户信息")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "etl_serv_ip", desc = "ETL部署Agent服务器IP", range = "新增工程时生成")
	@Param(name = "serv_file_path", desc = "ETL服务器部署路径", range = "目录为自定义时传值", nullable = true)
	@Param(name = "user_name", desc = "ETL部署Agent服务器用户名", range = "无限制")
	@Param(name = "user_pwd", desc = "ETL部署Agent服务器密码", range = "无限制")
	@Param(name = "isCustomize", desc = "部署目录是否自定义", range = "使用IsFlag代码项，0-代表默认，1-代表自定义")
	public void deployEtlJobScheduleProject(String etl_sys_cd, String etl_serv_ip, String serv_file_path,
	                                        String user_name, String user_pwd, String isCustomize) {
		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.判断系统是否正在运行中，如果正在运行中不允许部署
		Etl_sys etlSys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
		if (Job_Status.STOP != (Job_Status.ofEnumByCode(etlSys.getSys_run_status()))) {
			throw new BusinessException("系统不是停止状态不能部署");
		}
		if (IsFlag.Fou == IsFlag.ofEnumByCode(isCustomize)) {
			// 默认
			serv_file_path = PropertyParaValue.getString("etlDeployPath", "/home/hyshf/");
		} else {
			// 部署目录选择自定义时不能为空
			Validator.notBlank(serv_file_path, "部署目录选择自定义时不能为空，请检查");
		}
		// 格式化路径
		serv_file_path = FilenameUtils.normalize(serv_file_path);
		// 3.部署作业工程
		ETLAgentDeployment.scpETLAgent(etl_sys_cd, etl_serv_ip, CommonVariables.SFTP_PORT, user_name, user_pwd,
			serv_file_path, etlSys.getServ_file_path());
		String redisIP = PropertyParaValue.getString("redis_ip", "172.168.0.61");
		String redisPort = PropertyParaValue.getString("redis_port", "56379");
		Etl_sys etl_sys = new Etl_sys();
		etl_sys.setEtl_serv_ip(etl_serv_ip);
		etl_sys.setEtl_sys_cd(etl_sys_cd);
		etl_sys.setUser_name(user_name);
		etl_sys.setUser_pwd(user_pwd);
		etl_sys.setServ_file_path(serv_file_path);
		etl_sys.setRemarks(redisIP + ':' + redisPort);
		etl_sys.setEtl_serv_port(String.valueOf(CommonVariables.SFTP_PORT));
		// 4.部署成功，更新用户信息
		etl_sys.update(Dbo.db());
	}

	@Method(desc = "启动CONTROL",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.根据工程编号获取工程信息"
			+ "3.判断工程是否已部署"
			+ "4.判断是否续跑，如果续跑日期从数据库查询当前批量日期，因为续跑是续跑当前跑批的"
			+ "5.如果日切方式不是自动日切且工程下作业列表为空，则不能启动"
			+ "6.获取系统状态,如果不是停止说明系统不是停止状态,不是停止状态不能启动control"
			+ "7.调用脚本启动启动Control")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "isResumeRun", desc = "是否续跑", range = "使用（IsFlag）代码项，1代表是，0代表否")
	@Param(name = "isAutoShift", desc = "是否自动日切", range = "使用（IsFlag）代码项，1代表是，0代表否")
	@Param(name = "curr_bath_date", desc = "批量日期", range = "yyyyMMdd格式的年月日，如：20191219")
	public void startControl(String etl_sys_cd, String isResumeRun, String isAutoShift, String curr_bath_date) {
		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.根据工程编号获取工程信息
		Etl_sys etlSys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
		// 3.判断工程是否已部署
		ETLJobUtil.isETLDeploy(etlSys);
		// 4.判断是否续跑，如果续跑日期从数据库查询当前批量日期，因为续跑是续跑当前跑批的
		if (IsFlag.Shi == IsFlag.ofEnumByCode(isResumeRun)) {
			curr_bath_date = etlSys.getCurr_bath_date();
			Validator.notBlank(curr_bath_date, "当前批量日期不能为空");
		}
		// 5.如果日切方式不是自动日切且工程下作业列表为空，则不能启动
		if (IsFlag.Fou == IsFlag.ofEnumByCode(isAutoShift)) {
			if (!ETLJobUtil.isEtlJObDefExistBySysCd(etl_sys_cd, Dbo.db())) {
				throw new BusinessException("如果日切方式不是自动日切且工程下作业列表为空，则不能启动!");
			}
		}
		// 6.获取系统状态,如果不是停止说明系统不是停止状态,不是停止状态不能启动control
		if (Job_Status.STOP != (Job_Status.ofEnumByCode(etlSys.getSys_run_status()))) {
			throw new BusinessException("系统不是停止状态不能启动control");
		}
		// 检查当前工程下的作业有没有分配资源，没有分配资源不能启动
		List<Etl_job_def> etlJobDefList = Dbo.queryList(Etl_job_def.class,
			"select * from " + Etl_job_def.TableName + " where etl_sys_cd=?",
			etl_sys_cd);
		for (Etl_job_def etl_job_def : etlJobDefList) {
			if (Dbo.queryNumber(
				"select count(1) from " + Etl_job_resource_rela.TableName
					+ " where etl_sys_cd=? and etl_job=?",
				etl_sys_cd, etl_job_def.getEtl_job())
				.orElseThrow(() -> new BusinessException("sql查询错误")) != 1) {
				throw new BusinessException(etl_job_def.getEtl_job() + "作业没有分配资源，不能启动!");
			}
		}
		if (curr_bath_date.contains("-")) {
			curr_bath_date = curr_bath_date.replaceAll("-", "");
		}
		// 7.调用脚本启动启动Control
		ETLAgentDeployment.startEngineBatchControl(curr_bath_date, etl_sys_cd, isResumeRun, isAutoShift,
			etlSys.getEtl_serv_ip(), etlSys.getEtl_serv_port(), etlSys.getUser_name(),
			etlSys.getUser_pwd(), etlSys.getServ_file_path());
	}

	@Method(desc = "启动TRIGGER",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.根据工程编号获取工程信息"
			+ "3.获取系统状态,如果不是运行说明CONTROL还未启动，不能启动TRIGGER"
			+ "4.调用脚本启动启动Trigger")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	public void startTrigger(String etl_sys_cd) {
		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.根据工程编号获取工程信息
		Etl_sys etlSys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
		// 3.获取系统状态,如果不是运行说明CONTROL还未启动，不能启动TRIGGER
		if (Job_Status.RUNNING != Job_Status.ofEnumByCode(etlSys.getSys_run_status())) {
			throw new BusinessException("CONTROL还未启动，不能启动TRIGGER");
		}
		// 4.调用脚本启动启动Trigger
		ETLAgentDeployment.startEngineBatchTrigger(etl_sys_cd, etlSys.getEtl_serv_ip(),
			etlSys.getEtl_serv_port(), etlSys.getUser_name(), etlSys.getUser_pwd(),
			etlSys.getServ_file_path());
	}

	@Method(desc = "读取Control或Trigger日志信息",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.根据工程编号获取工程信息"
			+ "3.判断工程是否已部署"
			+ "4.获取当前日期"
			+ "5.获取control或trigger日志路径"
			+ "6.日志读取行数最大为1000行"
			+ "7.读取control或trigger日志信息")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "readNum", desc = "查看日志行数", range = "最多显示1000行,大于0的正整数", valueIfNull = "100")
	@Param(name = "isControl", desc = "是否读取Control日志", range = "使用（IsFlag代码项），0代表control，" +
		"1代表trigger")
	@Return(desc = "返回内容描述", range = "取值范围")
	public String readControlOrTriggerLog(String etl_sys_cd, Integer readNum, String isControl) {

		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.根据工程编号获取工程信息
		Etl_sys etlSys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
		// 3.判断工程是否已部署
		ETLJobUtil.isETLDeploy(etlSys);
		// 4.获取当前日期
		String sysDate = DateUtil.getSysDate();
		// 5.获取control或trigger日志路径
		String logDir = FilenameUtils.normalize(etlSys.getServ_file_path())
			+ separator + etl_sys_cd + separator;
		if (IsFlag.Fou == IsFlag.ofEnumByCode(isControl)) {
			// CONTROL日志目录
			logDir = logDir + "control" + separator + sysDate.substring(0, 4) + separator
				+ sysDate.substring(0, 6) + separator + sysDate + "_ControlOut.log";
		} else {
			// TRIGGER日志目录
			logDir = logDir + "trigger" + separator + sysDate.substring(0, 4) + separator
				+ sysDate.substring(0, 6) + separator + sysDate + "_TriggerOut.log";
		}
		// 6.日志读取行数最大为1000行
		if (readNum > 1000) {
			readNum = 1000;
		}
		SFTPDetails sftpDetails = new SFTPDetails();
		sftpDetails.setUser_name(etlSys.getUser_name());
		sftpDetails.setPwd(etlSys.getUser_pwd());
		sftpDetails.setHost(etlSys.getEtl_serv_ip());
		sftpDetails.setPort(Integer.parseInt(etlSys.getEtl_serv_port()));
		// 7.读取control或trigger日志信息
		return ReadLog.readAgentLog(logDir, sftpDetails, readNum);
	}

	@Method(desc = "下载Control或Trigger日志",
		logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制"
			+ "2.根据工程编号获取工程信息"
			+ "3.判断工程是否已部署"
			+ "4.修改批量日期格式"
			+ "5.获取control或trigger日志路径"
			+ "6.获取压缩日志命令"
			+ "7.判断是control日志还是trigger日志,获取日志目录以及压缩日志目录命令"
			+ "8.与工程部署服务器进行交互压缩日志文件"
			+ "9.获取文件下载路径"
			+ "10.从服务器下载文件到本地"
			+ "11.下载完删除压缩包"
			+ "12.返回CONTROL或TRIGGER下载日志文件名")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	@Param(name = "curr_bath_date", desc = "批量日期", range = "yyyyMMdd格式的年月日，如：20191219")
	@Param(name = "isControl", desc = "是否读取Control日志", range = "使用（IsFlag代码项），" +
		"0代表control日志，1代表trigger日志")
	public String downloadControlOrTriggerLog(String etl_sys_cd, String curr_bath_date, String isControl) {
		try {
			// 1.数据可访问权限处理方式，通过user_id进行权限控制
			// 2.根据工程编号获取工程信息
			Etl_sys etlSys = ETLJobUtil.getEtlSysById(etl_sys_cd, getUserId(), Dbo.db());
			// 3.判断工程是否已部署
			ETLJobUtil.isETLDeploy(etlSys);
			// 4.修改批量日期格式
			if (curr_bath_date.contains("-") && curr_bath_date.length() == 10) {
				curr_bath_date = StringUtil.replace(curr_bath_date, "-", "");
			}
			// 5.获取control或trigger日志路径
			String logDir = FilenameUtils.normalize(etlSys.getServ_file_path())
				+ separator + etl_sys_cd + separator;
			// 6.获取压缩日志命令
			String compressCommand;
			// 7.判断是control日志还是trigger日志,获取日志目录以及压缩日志目录命令
			if (IsFlag.Fou == IsFlag.ofEnumByCode(isControl)) {
				// CONTROL日志目录
				logDir = logDir + "control" + separator + curr_bath_date.substring(0, 4)
					+ separator + curr_bath_date.substring(0, 6) + separator + curr_bath_date;
				// 压缩CONTROL日志命令
				compressCommand = "tar -zvcPf " + logDir + "_ControlLog.tar.gz" + " " + logDir + "*.log";
			} else {
				// TRIGGER日志目录
				logDir = logDir + "trigger" + separator + curr_bath_date.substring(0, 4)
					+ separator + curr_bath_date.substring(0, 6) + separator + curr_bath_date;
				// 压缩TRIGGER日志命令
				compressCommand = "tar -zvcPf " + logDir + "_TriggerLog.tar.gz" + " " + logDir + "*.log";
			}
			SFTPDetails sftpDetails1 = new SFTPDetails();
			// 8.与工程部署服务器进行交互压缩日志文件
			ETLJobUtil.interactingWithTheAgentServer(compressCommand, etlSys, sftpDetails1);
			// 9.获取文件下载路径
			String localPath = WebinfoConf.FileUpload_SavedDirName + File.separator;
			logger.info("==========control/trigger文件下载本地路径=========" + localPath);
			// 10.从服务器下载文件到本地
			if (IsFlag.Fou == IsFlag.ofEnumByCode(isControl)) {
				// CONTROL日志文件名称
				logDir = logDir + "_ControlLog.tar.gz";
			} else {
				// TRIGGER日志文件名称
				logDir = logDir + "_TriggerLog.tar.gz";
			}
			FileDownloadUtil.downloadLogFile(logDir, localPath, sftpDetails1);
			// 11.下载完删除压缩包
			FileDownloadUtil.deleteLogFileBySFTP(logDir, sftpDetails1);
			if (curr_bath_date.contains("-")) {
				curr_bath_date = curr_bath_date.replaceAll("-", "");
			}
			// 12.返回CONTROL或TRIGGER下载日志文件名
			if (IsFlag.Fou == IsFlag.ofEnumByCode(isControl)) {
				// CONTROL日志文件名称
				return curr_bath_date + "_ControlLog.tar.gz";
			} else {
				// TRIGGER日志文件名称
				return curr_bath_date + "_TriggerLog.tar.gz";
			}
		} catch (JSchException e) {
			throw new BusinessException("与远端服务器进行交互，建立连接失败！");
		} catch (IOException e) {
			throw new BusinessException("下载日志文件失败！");
		}
	}

	@Method(desc = "停止工程信息", logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制" +
		"2.验证当前用户对应的工程是否已不存在" +
		"3.停止工程信息")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	public void stopEtlProject(String etl_sys_cd) {
		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.停止工程信息
		DboExecute.updatesOrThrow("停止工程，更新系统运行状态失败", "update " + Etl_sys.TableName
			+ " set sys_run_status=? where etl_sys_cd=?", Job_Status.STOP.getCode(), etl_sys_cd);
	}

	@Method(desc = "下载文件",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限验证" +
			"2.下载文件")
	@Param(name = "fileName", desc = "下载文件名", range = "无限制")
	public void downloadFile(String fileName) {
		// 1.数据可访问权限处理方式，该方法不需要权限验证
		// 2.下载文件
		FileDownloadUtil.downloadFile(fileName);
	}

	@Method(desc = "删除作业调度工程", logicStep = "1.数据可访问权限处理方式，通过user_id进行权限控制" +
		"2.验证当前用户对应的工程是否已不存在" +
		"3.判断该工程下是否还有任务" +
		"4.判断该工程下是否还有作业" +
		"5.删除工程信息,这里删除资源定义表信息的原因是新增工程时会默认初始化thrift，yarn这两个资源给工程")
	@Param(name = "etl_sys_cd", desc = "作业调度工程登记表主键ID", range = "新增工程时生成")
	public void deleteEtlProject(String etl_sys_cd) {
		// 1.数据可访问权限处理方式，通过user_id进行权限控制
		// 2.判断该工程下是否还有任务
		if (Dbo.queryNumber("select count(1) from " + Etl_sub_sys_list.TableName + "  WHERE etl_sys_cd=?"
			, etl_sys_cd).orElseThrow(() -> new BusinessException("sql查询错误！")) > 0) {
			throw new BusinessException("该工程下还有任务，不能删除！");
		}
		// 3.判断该工程下是否还有作业
		if (Dbo.queryNumber("select count(1) from " + Etl_job_def.TableName + "  WHERE etl_sys_cd=?"
			, etl_sys_cd).orElseThrow(() -> new BusinessException("sql查询错误！")) > 0) {
			throw new BusinessException("该工程下还有作业，不能删除！");
		}
		// 4.删除工程信息,这里删除资源定义表信息的原因是新增工程时会默认初始化thrift，yarn这两个资源给工程
		Dbo.execute("delete from " + Etl_resource.TableName + " where etl_sys_cd=?", etl_sys_cd);
		// 删除已经配置的系统参数
		Dbo.execute("delete from " + Etl_para.TableName + " where etl_sys_cd=?", etl_sys_cd);
		// 删除作业调度历史
		Dbo.execute("delete from " + Etl_job_disp_his.TableName + " where etl_sys_cd=?", etl_sys_cd);
		// 删除作业干预信息
		Dbo.execute("delete from " + Etl_job_hand.TableName + " where etl_sys_cd=?", etl_sys_cd);
		// 删除作业干预历史
		Dbo.execute("delete from " + Etl_job_hand_his.TableName + " where etl_sys_cd=?", etl_sys_cd);
		// 删除作业调度信息
		Dbo.execute("delete from " + Etl_job_cur.TableName + " where etl_sys_cd=?", etl_sys_cd);
		DboExecute.deletesOrThrow("删除工程失败", "delete from " + Etl_sys.TableName +
			" where etl_sys_cd=?", etl_sys_cd);
	}
}
