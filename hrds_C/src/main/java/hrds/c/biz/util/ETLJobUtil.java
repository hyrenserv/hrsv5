package hrds.c.biz.util;

import com.jcraft.jsch.JSchException;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.jsch.SftpOperate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@DocClass(desc = "作业调度工程工具类", author = "dhw", createdate = "2019/11/26 11:11")
public class ETLJobUtil {

	@Method(desc = "判断当前工程是否还存在",
		logicStep = "1.判断当前工程是否还存在，存在返回true,不存在返回false")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "user_id", desc = "创建工程用户ID", range = "新增用户时生成", nullable = true)
	@Return(desc = "返回工程是否存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlSysExist(String etl_sys_cd, DatabaseWrapper db) {
		// 1.判断当前工程是否还存在，存在返回true,不存在返回false
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_sys.TableName + " where etl_sys_cd=?",
			etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "根据用户判断当前工程是否还存在",
		logicStep = "1.判断当前用户下该工程是否已存在")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "user_id", desc = "创建工程用户ID", range = "新增用户时生成", nullable = true)
	@Return(desc = "返回工程是否存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlSysExistById(String etl_sys_cd, Long user_id, DatabaseWrapper db) {
		// 1.判断当前用户下该工程是否已存在
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_sys.TableName + " where etl_sys_cd=? and user_id=?",
			etl_sys_cd, user_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "确定该工程下对应的任务确实存在",
		logicStep = "1.确定该工程下对应的任务是否存在，存在返回true,不存在返回false")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "无限制")
	@Param(name = "sub_sys_cd", desc = "任务编号", range = "无限制")
	@Return(desc = "该工程下对应的任务是否存在的标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlSubSysExist(String etl_sys_cd, String sub_sys_cd, DatabaseWrapper db) {
		// 1.确定该工程下对应的任务是否存在，存在返回true,不存在返回false
		return SqlOperator.queryNumber(db,
			"SELECT count(1) FROM " + Etl_sub_sys_list.TableName +
				" WHERE etl_sys_cd=? AND sub_sys_cd=?",
			etl_sys_cd, sub_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误")) == 1;
	}

	@Method(desc = "判断该工程对应的任务下是否还有作业",
		logicStep = "1.判断该工程对应的任务下是否还有作业，有作业则不能删除")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "sub_sys_cd", desc = "任务编号", range = "新增任务时生成")
	public static void isEtlJobDefExistUnderEtlSubSys(String etl_sys_cd, String sub_sys_cd, DatabaseWrapper db) {
		// 1.判断该工程对应的任务下是否还有作业，有作业则不能删除
		if (SqlOperator.queryNumber(db,
			"select count(1) from " + Etl_job_def.TableName + "  WHERE etl_sys_cd=? AND sub_sys_cd=?",
			etl_sys_cd, sub_sys_cd).
			orElseThrow(() -> new BusinessException("sql查询错误！")) > 0) {
			throw new BusinessException("该工程对应的任务下还有作业，不能删除！");
		}
	}

	@Method(desc = "判断作业调度工程是否已部署",
		logicStep = "1.验证服务器IP是否为空"
			+ "2.服务器端口是否为空"
			+ "3.服务器用户名为空"
			+ "4.服务器密码是否为空"
			+ "5.服务器部署目录是否为空")
	@Param(name = "etlSys", desc = "作业工程实体对象", range = "与数据对应字段规则一致")
	public static void isETLDeploy(Etl_sys etlSys) {
		// 1.验证服务器IP是否为空
		Validator.notBlank(etlSys.getEtl_serv_ip(), "服务器IP为空，请检查工程是否已部署！");
		// 2.服务器端口是否为空
		Validator.notBlank(etlSys.getEtl_serv_port(), "服务器端口是否为空，请检查工程是否已部署！");
		// 3.服务器用户名为空
		Validator.notBlank(etlSys.getUser_name(), "服务器用户名为空，请检查工程是否已部署！");
		// 4.服务器密码是否为空
		Validator.notBlank(etlSys.getUser_pwd(), "服务器密码为空，请检查工程是否已部署！");
		// 5.服务器部署目录是否为空
		Validator.notBlank(etlSys.getServ_file_path(), "服务器部署目录为空，请检查工程是否已部署！");

	}

	@Method(desc = "判断作业名称是否已存在",
		logicStep = "1.判断作业名称是否已存在，存在返回true，不存在返回false")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "etl_job", desc = "作业名称", range = "新增作业时生成")
	@Return(desc = "作业名称是否已存在标志", range = "true代表已存在，false代表不存在")
	public static boolean isEtlJobDefExist(String etl_sys_cd, String etl_job, DatabaseWrapper db) {
		// 1.判断作业名称是否已存在，存在返回true，不存在返回false
		return SqlOperator.queryNumber(db,
			"SELECT count(1) FROM " + Etl_job_def.TableName + " WHERE etl_job=? AND etl_sys_cd=?",
			etl_job, etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "判断当前工程下是否有作业",
		logicStep = "1.判断当前工程下是否有作业，存在返回true，不存在返回false")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	public static boolean isEtlJObDefExistBySysCd(String etl_sys_cd, DatabaseWrapper db) {
		// 1.判断当前工程下是否有作业，存在返回true，不存在返回false
		return SqlOperator.queryNumber(db,
			"SELECT count(1) FROM " + Etl_job_def.TableName + " WHERE etl_sys_cd=?",
			etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误")) != 0;
	}

	@Method(desc = "判断是否资源需求过大",
		logicStep = "1.判断资源是否存在" +
			"2.检测当前作业分配的占用资源数是否过大")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "resource_type", desc = "参数类型", range = "新增资源定义时生成")
	@Param(name = "resource_seq", desc = "资源需求数", range = "不能大于最大阈值")
	public static void isResourceDemandTooLarge(String etl_sys_cd, String resource_type,
	                                            Integer resource_seq, DatabaseWrapper db) {
		// 1.判断资源是否存在
		if (!isEtlResourceExist(etl_sys_cd, resource_type, db)) {
			throw new BusinessException("当前工程对应的资源已不存在！");
		}
		// 2.检测当前作业分配的占用资源数是否过大
		List<Integer> resource_max = SqlOperator.queryOneColumnList(db,
			"select resource_max from " + Etl_resource.TableName
				+ " where etl_sys_cd=? AND resource_type=?",
			etl_sys_cd, resource_type);
		if (resource_seq > resource_max.get(0)) {
			throw new BusinessException("当前分配的作业资源需求数过大 ,已超过当前资源类型的最大阀值数!");
		}
	}

	@Method(desc = "判断当前工程对应作业资源分配信息是否存在",
		logicStep = "1.判断当前工程对应作业资源分配信息是否存在")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "etl_job", desc = "作业名称", range = "新增作业时生成")
	@Return(desc = "当前工程对应作业资源分配信息是否存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlJobResourceRelaExist(String etl_sys_cd, String etl_job, DatabaseWrapper db) {
		// 1.判断当前工程对应作业资源分配信息是否存在
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_job_resource_rela.TableName + " where etl_sys_cd=? and etl_job=?",
			etl_sys_cd, etl_job)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "判断资源是否存在",
		logicStep = "1.判断资源是否存在")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "无限制")
	@Param(name = "resource_type", desc = "资源类型", range = "无限制")
	@Return(desc = "资源是否存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlResourceExist(String etl_sys_cd, String resource_type, DatabaseWrapper db) {
		// 1.判断资源是否存在
		return SqlOperator.queryNumber(db,
			"SELECT count(1) FROM " + Etl_resource.TableName
				+ " WHERE resource_type=? AND etl_sys_cd=?",
			resource_type, etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误！")) > 0;
	}

	@Method(desc = "判断当前表信息是否存在",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限验证" +
			"2.判断作业名称与上游作业名称是否相同，相同则不能依赖" +
			"3.判断当前工程对应作业依赖作业是否存在")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "pre_etl_sys_cd", desc = "上游工程编号", range = "目前与工程编号相同（因为暂无工程之间依赖）")
	@Param(name = "etl_job", desc = "作业名称", range = "新增作业时生成")
	@Param(name = "pre_etl_job", desc = "作业名称", range = "无限制")
	@Return(desc = "当前工程对应作业依赖作业是否存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlDependencyExist(String etl_sys_cd, String pre_etl_sys_cd, String etl_job,
	                                           String pre_etl_job, DatabaseWrapper db) {
		// 1.判断作业名称与上游作业名称是否相同，相同则不能依赖
		if (etl_job.equals(pre_etl_job)) {
			throw new BusinessException("作业名称与上游作业名称相同不能依赖！");
		}
		// 2.判断当前工程对应作业依赖作业是否存在
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_dependency.TableName
				+ " where etl_sys_cd=? And etl_job=? AND pre_etl_sys_cd=? AND pre_etl_job=?",
			etl_sys_cd, etl_job, pre_etl_sys_cd, pre_etl_job)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "判断作业系统参数变量名称是否已存在",
		logicStep = "1.数据可访问权限处理方式，此方法不需要权限认证" +
			"2.判断作业系统参数变量名称是否已存在")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "para_cd", desc = "变量名称", range = "新增作业系统参数时生成")
	@Return(desc = "作业系统参数变量名称是否已存在标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlParaExist(String etl_sys_cd, String para_cd, DatabaseWrapper db) {
		// 1.数据可访问权限处理方式，此方法不需要权限认证
		// 2.判断作业系统参数变量名称是否已存在
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_para.TableName + " where etl_sys_cd=? AND para_cd=?",
			etl_sys_cd, para_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误！")) > 0;
	}

	@Method(desc = "判断工程下是否有作业正在干预",
		logicStep = "1.判断工程下是否有作业正在干预")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Return(desc = "工程下是否有作业正在干预标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlJobHandExist(String etl_sys_cd, DatabaseWrapper db) {
		// 1.判断工程下是否有作业正在干预
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_job_hand.TableName + " where etl_sys_cd=?",
			etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误！")) > 0;
	}

	@Method(desc = "根据作业名称判断工程下当前作业是否正在被干预",
		logicStep = "1.判断工程下当前作业是否正在被干预")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "etl_job", desc = "作业名称", range = "新增作业时生成", nullable = true)
	@Return(desc = "工程下是否有作业正在干预标志", range = "true代表存在，false代表不存在")
	public static boolean isEtlJobHandExistByJob(String etl_sys_cd, String etl_job, DatabaseWrapper db) {
		// 1.根据作业名称判断工程下当前作业是否正在被干预
		return SqlOperator.queryNumber(db,
			"select count(*) from " + Etl_job_hand.TableName + " where etl_sys_cd=? and etl_job=?",
			etl_sys_cd, etl_job)
			.orElseThrow(() -> new BusinessException("sql查询错误！")) > 0;
	}

	@Method(desc = "查询当前用户对应工程信息",
		logicStep = "1.返回当前用户对应工程信息")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "user_id", desc = "创建工程用户ID", range = "新增用户时生成")
	@Return(desc = "返回当前用户对应工程信息", range = "不能为空")
	public static Etl_sys getEtlSysById(String etl_sys_cd, long user_id, DatabaseWrapper db) {
		// 2.返回当前用户对应工程信息
		return SqlOperator.queryOneObject(db, Etl_sys.class,
			"select * from " + Etl_sys.TableName + " where user_id=? and etl_sys_cd=?",
			user_id, etl_sys_cd)
			.orElseThrow(() -> new BusinessException("sql查询错误或映射实体失败"));
	}

	@Method(desc = "根据工程编号作业名称获取作业信息",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限验证" +
			"2.获取作业信息")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "etl_job", desc = "作业名称", range = "新增作业时生成")
	@Return(desc = "返回作业信息", range = "无限制")
	public static Map<String, Object> getEtlJobByJob(String etl_sys_cd, String etl_job, DatabaseWrapper db) {
		// 1.数据可访问权限处理方式，该方法不需要权限验证
		// 2.获取作业信息
		return SqlOperator.queryOneObject(db,
			"select * FROM " + Etl_job_def.TableName + " where etl_sys_cd=? AND etl_job=?",
			etl_sys_cd, etl_job);
	}

	@Method(desc = "与ETLAgent服务交互压缩日志文件",
		logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制" +
			"2.封装与远端服务器进行交互所需参数" +
			"3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据" +
			"4.执行压缩日志命令")
	@Param(name = "compressCommand", desc = "压缩命令", range = "不为空")
	@Param(name = "etlSysInfo", desc = "工程参数", range = "不为空")
	@Param(name = "sftpDetails", desc = "sftp参数对象", range = "无限制")
	public static void interactingWithTheAgentServer(String compressCommand, Etl_sys etlSysInfo,
	                                                 SFTPDetails sftpDetails) throws JSchException, IOException {
		// 1.数据可访问权限处理方式，该方法不需要权限控制
		// 2.封装与远端服务器进行交互所需参数
		sftpDetails.setHost(etlSysInfo.getEtl_serv_ip());
		sftpDetails.setPort(Integer.parseInt(etlSysInfo.getEtl_serv_port()));
		sftpDetails.setUser_name(etlSysInfo.getUser_name());
		sftpDetails.setPwd(etlSysInfo.getUser_pwd());
		// 3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据
		try (SftpOperate sftpOperate = new SftpOperate(sftpDetails, 0)) {
			// 4.执行压缩日志命令
			sftpOperate.execCommandByJSch(compressCommand);
		}
	}
}
