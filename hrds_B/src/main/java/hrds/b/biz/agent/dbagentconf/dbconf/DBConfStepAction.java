package hrds.b.biz.agent.dbagentconf.dbconf;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.SqlOperator.Assembler;
import fd.ng.db.resultset.Result;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import fd.ng.web.util.Dbo;
import hrds.b.biz.agent.bean.DBConnectionProp;
import hrds.b.biz.agent.tools.ConnUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.AgentType;
import hrds.commons.codes.CollectType;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Agent_down_info;
import hrds.commons.entity.Agent_info;
import hrds.commons.entity.Collect_job_classify;
import hrds.commons.entity.Data_source;
import hrds.commons.entity.Database_set;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.*;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.key.PrimayKeyGener;
import java.util.List;

@DocClass(desc = "配置源DB属性", author = "WangZhengcheng")
public class DBConfStepAction extends BaseAction {

	@Method(
		desc = "根据数据库采集任务ID进行查询并在页面上回显数据源配置信息",
		logicStep =
			""
				+ "1、在数据库设置表(database_set)中，根据databaseId判断是否查询到数据，如果查询不到，抛异常给前端"
				+ "2、如果任务已经设置完成并发送成功，则不允许编辑"
				+ "3、在数据库设置表表中，关联采集作业分类表(collect_job_classify)，查询出当前database_id的所有信息并返回")
	@Param(name = "databaseId", desc = "源系统数据库设置表主键", range = "不为空")
	@Return(desc = "数据源信息查询结果集", range = "不会为null")
	public Result getDBConfInfo(long databaseId) {
		// 1、在数据库设置表(database_set)中，根据databaseId判断是否查询到数据，如果查询不到，抛异常给前端
//		Database_set dbSet =
//			Dbo.queryOneObject(
//				Database_set.class,
//				"SELECT das.* "
//					+ " FROM "
//					+ Database_set.TableName
//					+ " das "
//					+ " JOIN "
//					+ Agent_info.TableName
//					+ " ai ON ai.agent_id = das.agent_id "
//					+ " WHERE das.database_id=? AND ai.user_id = ? AND das.is_reg = ? ",
//				databaseId,
//				getUserId(), IsFlag.Fou.getCode())
//				.orElseThrow(() -> new BusinessException("未能找到该任务"));
		// 数据可访问权限处理方式
		// 以上SQL中，通过当前用户ID进行关联查询，达到了数据权限的限制

		// 2、如果任务已经设置完成并发送成功，则不允许编辑
		//		if(IsFlag.Shi == IsFlag.ofEnumByCode(dbSet.getIs_sendok())){
		//			throw new BusinessException("该任务已经设置完成并发送成功，不允许编辑");
		//		}
		// 3、在数据库设置表表中，关联采集作业分类表(collect_job_classify)，查询出当前database_id的所有信息并返回
		return Dbo.queryResult("select t1.database_id, t1.agent_id, t1.database_number, t1.task_name,t1.collect_type, " +
			" t1.database_name, t1.database_drive, t1.database_type, t1.user_name, t1.database_pad, t1.database_ip, " +
			" t1.database_port, t1.db_agent, t1.is_sendok, t1.jdbc_url, t2.classify_id, t2.classify_num, " +
			" t2.classify_name, t2.remark " +
			" from " + Database_set.TableName + " t1 " +
			" left join " + Collect_job_classify.TableName + " t2 on t1.classify_id = t2.classify_id" +
			" where database_id = ? AND t1.collect_type = ? ", databaseId, CollectType.ShuJuKuChouShu.getCode());
	}

	@Method(desc = "新增时获取数据库采集的数据", logicStep = "使用是否发生完成的标识来获取上次为配置文采的任务")
	@Param(name = "databaseId", desc = "源系统数据库设置表主键", range = "不为空")
	@Param(name = "agent_id", desc = "数据库采集AgentID", range = "不为空", nullable = true)
	@Return(desc = "返回上次为配置文采的任务采集信息", range = "可以为空,为空表示首次配置")
	public Result addDBConfInfo(long databaseId, long agent_id) {

		// 3、在数据库设置表表中，关联采集作业分类表(collect_job_classify)，查询出当前database_id的所有信息并返回
		return Dbo.queryResult(
			"select t1.database_id, t1.agent_id, t1.database_number, t1.task_name,t1.collect_type, "
				+ " t1.database_name, t1.database_drive, t1.database_type, t1.user_name, t1.database_pad, t1.database_ip, "
				+ " t1.database_port, t1.db_agent, t1.is_sendok, t1.jdbc_url, t2.classify_id, t2.classify_num, "
				+ " t2.classify_name, t2.remark "
				+ " from "
				+ Database_set.TableName
				+ " t1 "
				+ " left join "
				+ Collect_job_classify.TableName
				+ " t2 on "
				+ " t1.classify_id = t2.classify_id  join "
				+ Agent_info.TableName
				+ " ai on t1.agent_id = ai.agent_id "
				+ "where  t1.is_sendok = ? AND ai.agent_type = ? AND ai.user_id = ? "
				+ "AND ai.source_id = ? AND ai.agent_id = ? AND t1.collect_type = ?",
			IsFlag.Fou.getCode(),
			AgentType.ShuJuKu.getCode(),
			getUserId(),
			databaseId,
			agent_id, CollectType.ShuJuKuChouShu.getCode());
	}

	@Method(desc = "根据数据库类型获得数据库连接url等信息", logicStep = "" + "1、调用工具类方法直接获取数据并返回")
	@Param(name = "dbType", desc = "数据库类型", range = "DatabaseType代码项")
	@Param(
		name = "port",
		desc = "端口号",
		range = "如果用户选择TaraData，并且没有传端口号，则这个参数可以不传",
		nullable = true,
		valueIfNull = "")
	@Return(desc = "数据库连接url属性信息", range = "不会为null", isBean = true)
	public DBConnectionProp getDBConnectionProp(String dbType, String port) {
		return ConnUtil.getConnURLProp(dbType, port);
		// 数据可访问权限处理方式
		// 不与数据库交互，无需限制访问权限
	}

	@Method(desc = "根据数据库类型获取数据库驱动", logicStep = "" + "1、调用工具类方法直接获取数据并返回")
	@Param(name = "dbType", desc = "数据库类型", range = "DatabaseType代码项")
	@Return(desc = "数据库连接驱动信息", range = "不会为null")
	public String getJDBCDriver(String dbType) {
		// 1、调用工具类方法直接获取数据并返回
		return ConnUtil.getJDBCDriver(dbType);
		// 数据可访问权限处理方式
		// 不与数据库交互，无需限制访问权限
	}

	@Method(desc = "通过对数据库IP和端口号进行分组筛选数据库直连采集配置信息", logicStep = "" + "1、查询数据并返回")
	@Param(name = "agentId", desc = "AgentID", range = "agent_info表主键，database_set表外键")
	@Return(desc = "查询结果，包含数据库名称、用户名、密码、IP、端口号", range = "不会为null")
	public Result getHisConnection(long agentId) {
		// 1、查询数据并返回
		return Dbo.queryResult(
			"select ds.database_name, ds.database_pad, ds.user_name, ds.database_port, "
				+ " ds.database_ip from "
				+ Database_set.TableName
				+ " ds"
				+ " where ds.database_id in"
				+ " (select max(database_id) as id from database_set group by database_port, database_ip)"
				+ " and ds.agent_id = ? ",
			agentId);
	}

	@Method(
		desc = "根据分类Id判断当前分类是否被使用",
		logicStep =
			""
				+ "1、在collect_job_classify表中查询传入的classifyId是否存在"
				+ "2、如果存在，在数据库中查询database_set表中是否有使用到当前classifyId的数据"
				+ "3、如果有，返回false，表示该分类被使用，不能编辑"
				+ "4、如果没有，返回true，表示该分类没有被使用，可以编辑")
	@Param(name = "classifyId", desc = "采集任务分类表ID", range = "不可为空")
	@Return(desc = "该分类是否可以被编辑", range = "返回false，表示该分类被使用，不能编辑；返回true，" + "表示该分类没有被使用，可以编辑")
	public boolean checkClassifyId(long classifyId) {
		// 1、在collect_job_classify表中查询传入的classifyId是否存在
		long count =
			Dbo.queryNumber(
				" SELECT count(1) FROM "
					+ Collect_job_classify.TableName
					+ " WHERE classify_id = ? AND user_id = ? ",
				classifyId,
				getUserId())
				.orElseThrow(() -> new BusinessException("SQL查询错误"));
		if (count != 1) {
			throw new BusinessException("采集作业分类信息不存在");
		}
		// 2、在数据库中查询database_set表中是否有使用到当前classifyId的数据
		long val =
			Dbo.queryNumber(
				"SELECT count(1) FROM "
					+ Data_source.TableName
					+ " ds"
					+ " JOIN "
					+ Agent_info.TableName
					+ " ai ON ds.source_id = ai.source_id "
					+ " JOIN "
					+ Database_set.TableName
					+ " das ON ai.agent_id = das.agent_id "
					+ " WHERE das.classify_id = ? AND ai.user_id = ? ",
				classifyId,
				getUserId())
				.orElseThrow(() -> new BusinessException("SQL查询错误"));
		// 数据可访问权限处理方式
		// 以上SQL中，通过当前用户ID进行关联查询，达到了数据权限的限制

		// 3、如果有，返回false，表示该分类被使用，不能编辑
		// 4、如果没有，返回true，表示该分类没有被使用，可以编辑
		return val == 0;
	}

	@Method(desc = "根据数据源ID获取分类信息", logicStep = "1、在数据库中查询相应的信息并返回")
	@Param(name = "sourceId", desc = "数据源表ID", range = "不可为空")
	@Return(desc = "所有在该数据源下的分类信息的List集合", range = "不会为空")
	public List<Collect_job_classify> getClassifyInfo(long sourceId) {
		// 1、在数据库中查询相应的信息并返回
		return Dbo.queryList(
			Collect_job_classify.class,
			"SELECT cjc.* FROM "
				+ Data_source.TableName
				+ " ds "
				+ " JOIN "
				+ Agent_info.TableName
				+ " ai ON ds.source_id = ai.source_id"
				+ " JOIN "
				+ Collect_job_classify.TableName
				+ " cjc ON ai.agent_id = cjc.agent_id"
				+ " WHERE ds.source_id = ? AND cjc.user_id = ? order by cjc.classify_num ",
			sourceId,
			getUserId());
		// 数据可访问权限处理方式
		// 以上SQL中，通过当前用户ID进行关联查询，达到了数据权限的限制
	}

	@Method(desc = "保存采集任务分类信息",
		logicStep = "1、对传入的数据进行判断，对不能为空的字段进行校验，如果不合法，提供明确的提示信息"
			+ "2、在数据库或中对新增数据进行校验"
			+ "3、分类编号重复抛异常给前台"
			+ "4、分类编号不重复可以新增"
			+ "5、给新增数据设置ID"
			+ "6、完成新增")
	@Param(name = "classify", desc = "Collect_job_classify类对象", range = "Collect_job_classify类对象", isBean = true)
	@Param(name = "sourceId", desc = "数据源表ID", range = "不可为空")
	public void saveClassifyInfo(Collect_job_classify classify, long sourceId) {
		// 1、对传入的数据进行判断，对不能为空的字段进行校验，如果不合法，提供明确的提示信息
		verifyClassifyEntity(classify, true);
		// 2、在数据库或中对新增数据进行校验
		long val = Dbo.queryNumber(
			"SELECT count(1) FROM " + Collect_job_classify.TableName + " cjc "
				+ " LEFT JOIN " + Agent_info.TableName + " ai ON cjc.agent_id=ai.agent_id"
				+ " LEFT JOIN " + Data_source.TableName + " ds ON ds.source_id=ai.source_id"
				+ " WHERE cjc.classify_num=? AND ds.source_id=? AND ds.create_user_id = ? ",
			classify.getClassify_num(), sourceId, getUserId())
			.orElseThrow(() -> new BusinessException("SQL查询错误"));
		// 数据可访问权限处理方式
		// 以上SQL中，通过当前用户ID进行关联查询，达到了数据权限的限制
		// 3、分类编号重复抛异常给前台
		if (val != 0) {
			throw new BusinessException("分类编号重复，请重新输入");
		}
		// 4、分类编号不重复可以新增
		// 5、给新增数据设置ID和user_id
		classify.setClassify_id(PrimayKeyGener.getNextId());
		classify.setUser_id(getUserId());
		// 6、完成新增
		classify.add(Dbo.db());
	}

	@Method(
		desc = "更新采集任务分类信息",
		logicStep =
			""
				+ "1、对传入的数据进行判断，对不能为空的字段进行校验，如果不合法，提供明确的提示信息"
				+ "2、在数据库或中对待更新数据进行校验，判断待更新的数据是否存在"
				+ "3、不存在抛异常给前台"
				+ "4、存在则校验更新后的分类编号是否重复"
				+ "5、完成更新操作")
	@Param(
		name = "classify",
		desc = "Collect_job_classify类对象",
		range = "Collect_job_classify类对象",
		isBean = true)
	@Param(name = "sourceId", desc = "数据源表ID", range = "不可为空")
	public void updateClassifyInfo(Collect_job_classify classify, long sourceId) {
		// 1、对传入的数据进行判断，对不能为空的字段进行校验，如果不合法，提供明确的提示信息
		verifyClassifyEntity(classify, false);
		// 2、在数据库或中对待更新数据进行校验，判断待更新的数据是否存在
		long val =
			Dbo.queryNumber(
				"SELECT count(1) FROM "
					+ Collect_job_classify.TableName
					+ " cjc "
					+ " LEFT JOIN "
					+ Agent_info.TableName
					+ " ai ON cjc.agent_id=ai.agent_id"
					+ " LEFT JOIN "
					+ Data_source.TableName
					+ " ds ON ds.source_id=ai.source_id"
					+ " WHERE cjc.classify_id=? AND ds.source_id=? AND ai.user_id = ? ",
				classify.getClassify_id(),
				sourceId,
				getUserId())
				.orElseThrow(() -> new BusinessException("SQL查询错误"));
		// 数据可访问权限处理方式
		// 以上SQL中，通过当前用户ID进行关联查询，达到了数据权限的限制
		// 3、不存在抛异常给前台
		if (val != 1) {
			throw new BusinessException("待更新的数据不存在");
		}
		// 4、存在则校验更新后的分类编号是否重复
		long count =
			Dbo.queryNumber(
				"SELECT count(1) FROM "
					+ Collect_job_classify.TableName
					+ " cjc"
					+ " LEFT JOIN "
					+ Agent_info.TableName
					+ " ai ON cjc.agent_id=ai.agent_id"
					+ " LEFT JOIN "
					+ Data_source.TableName
					+ " ds ON ds.source_id=ai.source_id"
					+ " WHERE cjc.classify_num = ? AND ds.source_id = ? AND ds.create_user_id = ? and cjc.classify_id != ?",
				classify.getClassify_num(),
				sourceId,
				getUserId(),
				classify.getClassify_id())
				.orElseThrow(() -> new BusinessException("SQL查询错误"));
		if (count != 0) {
			throw new BusinessException("分类编号重复，请重新输入");
		}
		// 5、存在则完成更新
		if (classify.update(Dbo.db()) != 1) {
			throw new BusinessException("保存分类信息失败！data=" + classify);
		}
	}

	@Method(
		desc = "删除采集任务分类信息",
		logicStep = "" + "1、在数据库或中对待更新数据进行校验，判断待删除的分类数据是否被使用" + "2、若正在被使用，则不能删除" + "3、若没有被使用，可以删除")
	@Param(name = "classifyId", desc = "采集任务分类表ID", range = "不可为空")
	public void deleteClassifyInfo(long classifyId) {
		// 1、在数据库或中对待更新数据进行校验，判断待删除的分类数据是否被使用
		boolean flag = checkClassifyId(classifyId);
		// 2、若正在被使用，则不能删除
		if (!flag) {
			throw new BusinessException("待删除的采集任务分类已被使用，不能删除");
		}
		// 3、若没有被使用，可以删除
		DboExecute.deletesOrThrowNoMsg(
			"delete from " + Collect_job_classify.TableName + " where classify_id = ?", classifyId);
	}

	@Method(
		desc = "保存数据库采集Agent数据库配置信息",
		logicStep =
			"" + "1、调用方法对传入数据的合法性进行校验" + "2、获取实体中的database_id" + "3、如果存在，则更新信息" + "4、如果不存在，则新增信息")
	@Param(
		name = "databaseSet",
		desc = "Database_set实体对象",
		range =
			"不为空,必须传递的有"
				+ "数据采集任务名：task_name"
				+ "作业编号：database_number"
				+ "分类ID：classify_id"
				+ "数据库类型：database_type"
				+ "数据库驱动：database_drive"
				+ "数据库名称：database_name"
				+ "IP：database_ip"
				+ "端口号：database_port"
				+ "用户名：user_name"
				+ "密码：database_pad"
				+ "jdbcurl: jdbc_url",
		isBean = true)
	@Return(desc = "保存成功后返回当前采集任务ID", range = "不为空")
	public long saveDbConf(Database_set databaseSet) {
		// 1、调用方法对传入数据的合法性进行校验
		verifyDatabaseSetEntity(databaseSet);

		// 校验作业编号是否唯一
		if (StringUtil.isNotBlank(databaseSet.getDatabase_number())) {
			String sql = "select count(1) from " + Database_set.TableName + " where database_number = ?";
			Assembler assembler = Assembler.newInstance().addSql(sql).addParam(databaseSet.getDatabase_number());
			if (databaseSet.getDatabase_id() != null) {
				assembler.addSql(" AND database_id != ?").addParam(databaseSet.getDatabase_id());
			}
			long val =
				Dbo.queryNumber(assembler.sql(), assembler.params()).orElseThrow(() -> new BusinessException("SQL查询错误"));
			if (val != 0) {
				throw new BusinessException("作业编号重复，请重新定义作业编号");
			}
		}

		// 2、获取实体中的database_id
		if (databaseSet.getDatabase_id() != null) {
			// 3、如果存在，则更新信息
			long val =
				Dbo.queryNumber(
					"select count(1) from " + Database_set.TableName + " where database_id = ?",
					databaseSet.getDatabase_id())
					.orElseThrow(() -> new BusinessException("SQL查询错误"));
			if (val != 1) {
				throw new BusinessException("待更新的数据不存在");
			}

			databaseSet.setDb_agent(IsFlag.Fou.getCode());
			//			databaseSet.setIs_sendok(IsFlag.Fou.getCode());
			databaseSet.setCp_or(Constant.DATABASE_CLEAN.toJSONString());
			databaseSet.update(Dbo.db());
		} else {
			// 4、如果不存在，则新增信息
			long id = PrimayKeyGener.getNextId();
			databaseSet.setDatabase_id(id);
			databaseSet.setDb_agent(IsFlag.Fou.getCode());
			databaseSet.setIs_sendok(IsFlag.Fou.getCode());
			databaseSet.setCollect_type(CollectType.ShuJuKuChouShu.getCode());
			// 任务级别的清洗规则，在这里新增时定义一个默认顺序，后面的页面可能改动这个顺序,
			databaseSet.setCp_or(Constant.DATABASE_CLEAN.toJSONString());

			databaseSet.add(Dbo.db());
		}
		// 返回id的目的是为了在点击下一步跳转页面的时候能通过database_id拿到上一个页面的信息
		return databaseSet.getDatabase_id();
	}

	@Method(desc = "测试连接",
		logicStep = "1、调用工具类获取本次访问的agentserver端url"
			+ "2、给agent发消息，并获取agent响应"
			+ "3、如果测试连接不成功，则抛异常给前端，说明连接失败，如果成功，则不做任务处理")
	@Param(name = "databaseSet", desc = "Database_set实体类对象",
		range = "不为空，只传database_drive，jdbc_url，user_name，database_pad，database_type这些字段即可", isBean = true)
	public void testConnection(Database_set databaseSet) {
		// 1、调用工具类获取本次访问的agentserver端url
		String url = AgentActionUtil.getUrl(databaseSet.getAgent_id(), getUserId(), AgentActionUtil.TESTCONNECTION);
		// 2、给agent发消息，并获取agent响应
		HttpClient.ResponseValue resVal = new HttpClient()
			.addData("database_drive", databaseSet.getDatabase_drive())
			.addData("jdbc_url", databaseSet.getJdbc_url())
			.addData("user_name", databaseSet.getUser_name())
			.addData("database_pad", databaseSet.getDatabase_pad())
			.addData("database_type", databaseSet.getDatabase_type())
			.addData("database_name",databaseSet.getDatabase_name())
			.post(url);
		// 3、如果测试连接不成功，则抛异常给前端，说明连接失败，如果成功，则不做任务处理
		ActionResult actionResult = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
			.orElseThrow(() -> new BusinessException("应用管理端与" + url + "服务交互异常"));
		if (!actionResult.isSuccess()) {
			throw new BusinessException("连接失败");
		}
		boolean connectFlag = (boolean) actionResult.getData();
		if (!connectFlag) {
			throw new BusinessException("连接失败");
		}
	}

	@Method(
		desc = "查看日志",
		logicStep =
			""
				+ "1、根据agent_id和user_id获取agent信息"
				+ "2、在agent信息中获取日志目录等信息"
				+ "3、调用读取日志的工具类读取日志"
				+ "4、如果读取到的日志为空，则返回未获取到日志"
				+ "5、否则，则返回日志信息")
	@Param(name = "agentId", desc = "agent信息表主键，Agent下载信息表外键", range = "不为空")
	@Param(
		name = "readNum",
		desc = "日志读取条数",
		range = "可以不传，默认显示100条",
		nullable = true,
		valueIfNull = "100")
	@Return(desc = "日志信息", range = "根据实际情况而定")
	public String viewLog(long agentId, int readNum) {
		// 1、根据agent_id和user_id获取agent信息
		Agent_down_info agentDownInfo =
			Dbo.queryOneObject(
				Agent_down_info.class,
				"select * from "
					+ Agent_down_info.TableName
					+ " where agent_id = ? and user_id = ?",
				agentId,
				getUserId())
				.orElseThrow(() -> new BusinessException("根据AgentID和userID未能找到Agent下载信息"));

		// 2、在agent信息中获取日志目录等信息
		String logDir = agentDownInfo.getLog_dir();
		SFTPDetails sftpDetails = new SFTPDetails();
		sftpDetails.setHost(agentDownInfo.getAgent_ip());
		sftpDetails.setPort(Integer.parseInt(CommonVariables.SFTP_PORT));
		sftpDetails.setUser_name(agentDownInfo.getUser_name());
		sftpDetails.setPwd(agentDownInfo.getPasswd());
		// 最多显示1000行
		if (readNum > 1000) {
			readNum = 1000;
		}

		// 3、调用读取日志的工具类读取日志
		String taskLog = ReadLog.readAgentLog(logDir, sftpDetails, readNum);

		// 4、如果读取到的日志为空，则返回未获取到日志
		if (StringUtil.isBlank(taskLog)) {
			return "未获取到日志";
		}

		// 5、否则，则返回日志信息
		return taskLog;
	}

	@Method(
		desc = "新增/更新操作校验Collect_job_classify中数据的合法性，对数据库中不能为空的字段，校验合法性，" + "       若不合法，提供明确的提示信息",
		logicStep =
			""
				+ "1、对于新增操作，校验classify_id不能为空"
				+ "2、校验classify_num不能为空"
				+ "3、校验classify_name不能为空"
				+ "4、校验user_id不能为空"
				+ "5、校验Agent_id不能为空")
	@Param(name = "entity", desc = "Collect_job_classify实体类对象", range = "不为空")
	@Param(name = "isAdd", desc = "新增/更新的标识位", range = "true为新增，false为更新")
	private void verifyClassifyEntity(Collect_job_classify entity, boolean isAdd) {
		// 1、对于更新操作，校验classify_id不能为空
		if (!isAdd) {
			if (entity.getClassify_id() == null) {
				throw new BusinessException("分类id不能为空");
			}
		}
		// 2、校验classify_num不能为空
		if (StringUtil.isBlank(entity.getClassify_num())) {
			throw new BusinessException("分类编号不能为空");
		}
		// 3、校验classify_name不能为空
		if (StringUtil.isBlank(entity.getClassify_name())) {
			throw new BusinessException("分类名称不能为空");
		}
		// 4、校验Agent_id不能为空
		if (entity.getAgent_id() == null) {
			throw new BusinessException("AgentID不能为空");
		}
		// 数据可访问权限处理方式
		// 该方法不与数据库交互，无需校验用户访问权限
	}

	@Method(
		desc = "保存数据库配置页面时，校验Database_set中数据的合法性，对数据库中不能为空的字段，校验合法性，若不合法" + "提供明确的提示信息",
		logicStep =
			""
				+ "1、校验database_type不能为空，并且取值范围必须在DatabaseType代码项中"
				+ "2、校验classify_id不能为空"
				+ "3、校验作业编号不为能空，并且长度不能超过10"
				+ "4、校验数据库驱动不能为空"
				+ "5、校验数据库名称不能为空"
				+ "6、校验数据库IP不能为空"
				+ "7、校验数据库端口号不能为空"
				+ "8、校验用户名不能为空"
				+ "9、校验数据库密码不能为空"
				+ "10、校验JDBCURL不能为空"
				+ "11、校验agent_id不能为空")
	@Param(name = "databaseSet", desc = "Database_set实体类对象", range = "不为空")
	private void verifyDatabaseSetEntity(Database_set databaseSet) {
		// 1、校验database_type不能为空，并且取值范围必须在DatabaseType代码项中
		if (StringUtil.isBlank(databaseSet.getDatabase_type())) {
			throw new BusinessException("保存数据库配置信息时数据库类型不能为空");
		}
		DatabaseType.ofEnumByCode(databaseSet.getDatabase_type());
		// 2、校验classify_id不能为空
		if (databaseSet.getClassify_id() == null) {
			throw new BusinessException("保存数据库配置信息时分类信息不能为空");
		}
		// 3、校验作业编号不为能空，并且长度不能超过10
//		if (StringUtil.isBlank(databaseSet.getDatabase_number())
//			|| databaseSet.getDatabase_number().length() > 10) {
//			throw new BusinessException("保存数据库配置信息时作业编号不为能空，并且长度不能超过10");
//		}
		// 4、校验数据库驱动不能为空
		if (StringUtil.isBlank(databaseSet.getDatabase_drive())) {
			throw new BusinessException("保存数据库配置信息时数据库驱动不能为空");
		}
		// 5、校验数据库名称不能为空
		if (StringUtil.isBlank(databaseSet.getDatabase_name())) {
			throw new BusinessException("保存数据库配置信息时数据库名称不能为空");
		}
		// 6、校验数据库IP不能为空
		if (StringUtil.isBlank(databaseSet.getDatabase_ip())) {
			throw new BusinessException("保存数据库配置信息时数据库IP地址不能为空");
		}
		// 7、校验数据库端口号不能为空
		if (StringUtil.isBlank(databaseSet.getDatabase_port())) {
			throw new BusinessException("保存数据库配置信息时数据库端口号不能为空");
		}
		// 8、校验用户名不能为空
		if (StringUtil.isBlank(databaseSet.getUser_name())) {
			throw new BusinessException("保存数据库配置信息时数据库用户名不能为空");
		}
		// 9、校验数据库密码不能为空
		if (StringUtil.isBlank(databaseSet.getDatabase_pad())) {
			throw new BusinessException("保存数据库配置信息时数据库密码不能为空");
		}
		// 10、校验JDBCURL不能为空
		if (StringUtil.isBlank(databaseSet.getJdbc_url())) {
			throw new BusinessException("保存数据库配置信息时数据库连接URL不能为空");
		}
		// 11、校验agent_id不能为空
		if (databaseSet.getAgent_id() == null) {
			throw new BusinessException("保存数据库配置信息时必须关联Agent信息不能为空");
		}
	}
}
