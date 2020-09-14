package hrds.l.biz.autoanalysis.operate;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.*;
import hrds.commons.collection.ProcessingData;
import hrds.commons.entity.*;
import hrds.commons.entity.fdentity.ProjectTableEntity;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.Constant;
import hrds.commons.utils.DataTableUtil;
import hrds.commons.utils.DboExecute;
import hrds.commons.utils.DruidParseQuerySql;
import hrds.commons.utils.key.PrimayKeyGener;
import hrds.l.biz.autoanalysis.bean.ComponentBean;
import hrds.l.biz.autoanalysis.common.AutoOperateCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

@DocClass(desc = "自主分析操作类", author = "dhw", createdate = "2020/8/24 11:29")
public class OperateAction extends BaseAction {

	private static final Logger logger = LogManager.getLogger();

	private static final String TempTableName = " TEMP_TABLE ";
	private static final ArrayList<String> numbersArray = new ArrayList<>();
	// 图标类型
	// 折线图
	private static final String LINE = "line";
	// 柱状图
	private static final String BAR = "bar";
	// 柱状折线混合图
	private static final String BL = "bl";
	// 柱状折线混合图-简单
	private static final String BLSIMPLE = "blsimple";
	// 多维柱状图(3)
	private static final String BARMD = "barmd";
	// 极坐标柱状图
	private static final String POLARBAR = "polarbar";
	// 散点图
	private static final String SCATTER = "scatter";
	// 气泡图
	private static final String BUBBLE = "bubble";
	// 饼图
	private static final String PIE = "pie";
	// 环形饼图
	private static final String HUANPIE = "huanpie";
	// 发散饼图
	private static final String FASANPIE = "fasanpie";
	// 卡片
	private static final String CARD = "card";
	// 二维表
	private static final String TABLE = "table";
	// 矩形树图
	private static final String TREEMAP = "treemap";
	// 地理坐标/地图
	private static final String MAP = "map";
	// 盒形图
	private static final String BOXPLOT = "boxplot";

	static {
		numbersArray.add("int");
		numbersArray.add("int8");
		numbersArray.add("int16");
		numbersArray.add("integer");
		numbersArray.add("tinyint");
		numbersArray.add("smallint");
		numbersArray.add("mediumint");
		numbersArray.add("bigint");
		numbersArray.add("float");
		numbersArray.add("double");
		numbersArray.add("decimal");
	}


	@Method(desc = "查询自主取数模板信息", logicStep = "1.查询并返回自主取数模板信息")
	@Return(desc = "返回自主取数模板信息", range = "无限制")
	public List<Map<String, Object>> getAccessTemplateInfo() {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.查询并返回自主取数模板信息
		return Dbo.queryList(
				"select t1.template_id,t1.template_name,t1.template_desc,t1.create_date,t1.create_time,"
						+ "t1.create_user,count(t2.fetch_sum_id) as count_number " +
						" from " + Auto_tp_info.TableName + " t1 eft join " + Auto_fetch_sum.TableName + " t2"
						+ " on t1.template_id = t2.template_id "
						+ " where template_status = ? group by t1.template_id "
						+ " order by t1.create_date desc,t1.create_time desc",
				AutoTemplateStatus.FaBu.getCode());
	}

	@Method(desc = "模糊查询自主取数模板信息", logicStep = "1.模糊查询自主取数模板信息")
	@Param(name = "template_name", desc = "模板名称", range = "无限制")
	@Return(desc = "返回模糊查询自主取数模板信息", range = "无限制")
	public List<Map<String, Object>> getAccessTemplateInfoByName(String template_name) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.模糊查询自主取数模板信息
		return Dbo.queryList(
				"select t1.template_id,t1.template_name,t1.template_desc,t1.create_date,t1.create_time,"
						+ "t1.create_user,count(t2.fetch_sum_id) as count_number " +
						" from " + Auto_tp_info.TableName + " t1 eft join " + Auto_fetch_sum.TableName + " t2"
						+ " on t1.template_id = t2.template_id "
						+ " where template_status = ? and template_name like ?"
						+ " group by t1.template_id order by t1.create_date desc,t1.create_time desc",
				AutoTemplateStatus.FaBu.getCode(), "%" + template_name + "%");
	}

	@Method(desc = "根据模板ID查询自主取数模板信息", logicStep = "1.根据模板ID查询自主取数信息")
	@Param(name = "template_id", desc = "自主取数模板ID", range = "新增自主取数模板时生成")
	@Return(desc = "返回根据模板ID查询自主取数信息", range = "无限制")
	public Map<String, Object> getAccessTemplateInfoById(long template_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.根据模板ID查询自主取数信息
		return Dbo.queryOneObject(
				"select template_name,template_desc from " + Auto_tp_info.TableName + " where template_id = ?",
				template_id);
	}

	@Method(desc = "获取自主取数结果字段", logicStep = "1.获取自主取数结果字段")
	@Param(name = "template_id", desc = "自主取数模板ID", range = "新增自主取数模板时生成")
	@Return(desc = "返回自主取数过滤条件", range = "无限制")
	public List<Map<String, Object>> getAccessResultFields(long template_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.获取自主取数结果字段
		return Dbo.queryList(
				"select res_show_column,template_res_id from " + Auto_tp_res_set.TableName
						+ " where template_id = ?",
				template_id);
	}

	@Method(desc = "获取自主取数过滤条件", logicStep = "1.获取自主取数过滤条件")
	@Param(name = "template_id", desc = "自主取数模板ID", range = "新增自主取数模板时生成")
	@Return(desc = "返回自主取数过滤条件", range = "无限制")
	public List<Map<String, Object>> getAutoAccessFilterCond(long template_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.获取自主取数过滤条件
		return Dbo.queryList(
				"select is_required,cond_cn_column,con_relation,template_cond_id,pre_value,value_type,value_size "
						+ Auto_tp_cond_info.TableName + " from where template_id = ?",
				template_id);
	}

	@Method(desc = "获取自主取数选择历史信息", logicStep = "1.获取自主取数选择历史信息")
	@Param(name = "template_id", desc = "自主取数模板ID", range = "新增自主取数模板时生成")
	@Return(desc = "返回自主取数选择历史信息", range = "无限制")
	public List<Map<String, Object>> getAccessSelectHistory(long template_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.获取自主取数选择历史信息
		return Dbo.queryList(
				"select * from " + Auto_fetch_sum.TableName + " where template_id = ? "
						+ " order by create_date desc ,create_time desc limit 10",
				template_id);
	}

	@Method(desc = "通过选择历史情况 获取之前的条件以及结果配置页面", logicStep = "1.获取自主取数选择历史信息")
	@Param(name = "fetch_sum_id", desc = "取数汇总ID", range = "新增取数信息时生成")
	@Return(desc = "返回自主取数选择历史信息", range = "无限制")
	public List<Map<String, Object>> getAccessCondFromHistory(long fetch_sum_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 获取到用户之前在页面中 填写的参数值 用模板条件ID进行匹配 template_cond_id
		return Dbo.queryList(
				"select cond_cn_column,con_relation,template_cond_id,pre_value,is_required,value_type,value_size"
						+ " from " + Auto_tp_cond_info.TableName + " t1 left join " + Auto_tp_info.TableName + " t2 "
						+ " on t1.template_id = t2.template_id left join " + Auto_fetch_sum.TableName + " t3 "
						+ " on t2.template_id = t3.template_id where t3.fetch_sum_id = ?",
				fetch_sum_id);
	}

	@Method(desc = "通过选择历史情况 获取之前的条件以及结果配置页面", logicStep = "1.获取自主取数选择历史信息")
	@Param(name = "fetch_sum_id", desc = "取数汇总ID", range = "新增取数信息时生成")
	@Return(desc = "返回自主取数选择历史信息", range = "无限制")
	public List<Map<String, Object>> getAccessResultFromHistory(long fetch_sum_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 获取用户之前在页面上 勾选的 显示结果 用模板结果ID进行匹配 template_res_id
		return Dbo.queryList(
				"select t1.res_show_column,t1.template_res_id,t3.fetch_sum_id "
						+ " from " + Auto_tp_res_set.TableName + " t1 left join " + Auto_tp_info.TableName + " t2 "
						+ " on t1.template_id = t2.template_id left join " + Auto_fetch_sum.TableName + " t3 "
						+ " on t2.template_id = t3.template_id where t3.fetch_sum_id = ?",
				fetch_sum_id);
	}

	@Method(desc = "获取自主取数清单查询结果", logicStep = "1.根据取数汇总ID获取取数sql" +
			"2.判断取数汇总ID对应的取数sql是否存在" +
			"3.获取自主取数清单查询结果")
	@Param(name = "fetch_sum_id", desc = "取数汇总ID", range = "配置取数模板时生成")
	@Return(desc = "返回主取数清单查询结果", range = "无限制")
	public List<Map<String, Object>> getAutoAccessQueryResult(long fetch_sum_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.根据取数汇总ID获取取数sql
		String fetch_sql = getAccessSql(fetch_sum_id);
		// 3.获取自主取数清单查询结果
		List<Map<String, Object>> accessResult = new ArrayList<>();
		new ProcessingData() {
			@Override
			public void dealLine(Map<String, Object> map) {
				accessResult.add(map);
			}
		}.getDataLayer(fetch_sql, Dbo.db());
		return accessResult;
	}

	@Method(desc = "保存自主取数清单查询入库信息", logicStep = "1.判断模板信息是否已不存在" +
			"2.新增取数汇总表数据" +
			"3.取数条件表入库" +
			"4.取数结果选择情况入库")
	@Param(name = "auto_fetch_sum", desc = "取数汇总表对象", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFetchConds", desc = "自主取数条件对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFetchRes", desc = "自主取数结果对象数组", range = "与数据库对应表规则一致", isBean = true)
	public void saveAutoAccessInfo(Auto_fetch_sum auto_fetch_sum, Auto_fetch_cond[] autoFetchConds,
	                               Auto_fetch_res[] autoFetchRes) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.判断模板信息是否已不存在
		Validator.notNull(auto_fetch_sum.getTemplate_id(), "模板ID不能为空");
		Validator.notBlank(auto_fetch_sum.getFetch_name(), "取数名称不能为空");
		isAutoTpInfoExist(auto_fetch_sum.getTemplate_id());
		auto_fetch_sum.setFetch_sum_id(PrimayKeyGener.getNextId());
		auto_fetch_sum.setCreate_date(DateUtil.getSysDate());
		auto_fetch_sum.setCreate_time(DateUtil.getSysTime());
		auto_fetch_sum.setCreate_user(getUserId());
		auto_fetch_sum.setFetch_sql(getWhereSql(auto_fetch_sum.getTemplate_id(), autoFetchConds, autoFetchRes));
		auto_fetch_sum.setFetch_status(AutoFetchStatus.BianJi.getCode());
		// 2.新增取数汇总表数据
		auto_fetch_sum.add(Dbo.db());
		for (Auto_fetch_cond auto_fetch_cond : autoFetchConds) {
			Validator.notBlank(auto_fetch_cond.getCond_value(), "条件值不能为空");
			Validator.notNull(auto_fetch_cond.getTemplate_cond_id(), "模板条件ID不能为空");
			auto_fetch_cond.setFetch_cond_id(PrimayKeyGener.getNextId());
			auto_fetch_cond.setFetch_sum_id(auto_fetch_sum.getFetch_sum_id());
			// 3.取数条件表入库
			auto_fetch_cond.add(Dbo.db());
		}
		for (Auto_fetch_res auto_fetch_res : autoFetchRes) {
			Validator.notBlank(auto_fetch_res.getFetch_res_name(), "取数结果名称不能为空");
			Validator.notNull(auto_fetch_res.getTemplate_res_id(), "模板结果ID不能为空");
			List<String> res_show_column = Dbo.queryOneColumnList(
					"select res_show_column from " + Auto_tp_res_set.TableName
							+ " where template_res_id = ?",
					auto_fetch_res.getFetch_res_id());
			auto_fetch_res.setFetch_res_name(res_show_column.get(0));
			auto_fetch_res.setShow_num(auto_fetch_res.getShow_num() == null ? 0 : auto_fetch_res.getShow_num());
			auto_fetch_res.setFetch_res_id(PrimayKeyGener.getNextId());
			auto_fetch_res.setFetch_sum_id(auto_fetch_sum.getFetch_sum_id());
			// 4.取数结果选择情况入库
			auto_fetch_res.add(Dbo.db());
		}
	}

	private String getWhereSql(long template_id, Auto_fetch_cond[] autoFetchConds,
	                           Auto_fetch_res[] autoFetchRes) {
		SqlOperator.Assembler assembler = SqlOperator.Assembler.newInstance();
		StringBuilder resultSqlSb = new StringBuilder();
		StringBuilder resultSql = new StringBuilder("select ");
		List<String> template_sql = Dbo.queryOneColumnList(
				"select template_sql from auto_tp_info where template_id = ?",
				template_id);
		String dbType = JdbcConstants.POSTGRESQL;
		String format_sql = SQLUtils.format(template_sql.get(0), dbType);
		List<String> formatSqlList = StringUtil.split(format_sql, "\n");
		List<Long> condIdList = Dbo.queryOneColumnList(
				"select template_cond_id from " + Auto_tp_cond_info.TableName + " where template_id = ?",
				template_id);
		for (long condId : condIdList) {// 模板条件ID
			boolean inMoreConRow = false;
			boolean flag = false;
			Auto_tp_cond_info auto_tp_cond_info = new Auto_tp_cond_info();
			auto_tp_cond_info.setTemplate_cond_id(condId);
			List<String> conRowList = Dbo.queryOneColumnList(
					"select con_row from " + Auto_tp_cond_info.TableName
							+ " where template_cond_id = ?", condId);
			List<String> con_row_list = StringUtil.split(conRowList.get(0), ",");
			if (con_row_list.size() != 1) {
				inMoreConRow = true;
			}
			int con_row = Integer.parseInt(con_row_list.get(0));
			for (Auto_fetch_cond auto_fetch_cond : autoFetchConds) {
				// 如果配置了条件而且选了条件
				if (condId == auto_fetch_cond.getTemplate_cond_id()) {
					Auto_tp_cond_info autoTpCondInfo = Dbo.queryOneObject(Auto_tp_cond_info.class,
							"select * from " + Auto_tp_cond_info.TableName + " where template_cond_id = ?",
							auto_fetch_cond.getTemplate_cond_id())
							.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
					String cond_value = auto_fetch_cond.getCond_value();
					if (AutoValueType.MeiJu == (AutoValueType.ofEnumByCode(autoTpCondInfo.getValue_type()))) {
						// 本来存的是中文
						// 需要转换成值
						List<String> condValueList = StringUtil.split(cond_value, ",");
						StringBuilder tmpCondValue = new StringBuilder();
						for (String everyCondValue : condValueList) {
							List<String> codeList = Dbo.queryOneColumnList(
									"select ci_sp_code from " + Code_info.TableName
											+ " where ci_sp_classname = ? and ci_sp_name = ?",
									autoTpCondInfo.getValue_size(), everyCondValue);
							tmpCondValue.append(codeList.get(0)).append(",");
						}
						cond_value = tmpCondValue.deleteCharAt(tmpCondValue.length() - 1).toString();
					}
					if (autoTpCondInfo.getCon_relation().equals("IN")) {
						List<String> condValueList = StringUtil.split(cond_value, ",");
						StringBuilder sb = new StringBuilder();
						for (String s : condValueList) {
							sb.append(s).append(",");
						}
						// 去除,添加）
						cond_value = sb.deleteCharAt(sb.length() - 1).toString();
					}
					String everySql = formatSqlList.get(con_row);
					StringBuilder everyResultSql = new StringBuilder();
					StringBuilder condValueParam = new StringBuilder("?");
					// 如果是WHERE开头的 将拼上 WHERE,n个（ 条件+关系+值,n个)
					if (everySql.trim().startsWith("WHERE")) {
						everyResultSql = new StringBuilder("WHERE ");
						everySql = everySql.replaceFirst("WHERE", "").trim();
					}

					// 如果是AND开头的 将拼上 WHERE,n个（ 条件+关系+值,n个)
					else if (everySql.trim().startsWith("AND")) {
						everyResultSql = new StringBuilder("AND ");
						everySql = everySql.replaceFirst("AND", "").trim();
					}
					// 如果是OR开头的 将拼上 WHERE,n个（ 条件+关系+值,n个)
					else if (everySql.trim().startsWith("OR")) {
						everyResultSql = new StringBuilder("OR ");
						everySql = everySql.replaceFirst("OR", "").trim();
					}
					// 考虑（问题
					while (everySql.startsWith("(")) {
						everyResultSql.append("( ");
						everySql = everySql.replaceFirst("\\(", "").trim();
					}
					// 开始判断数值问题 如果是IN
					if (autoTpCondInfo.getCon_relation().equals("IN")) {
						condValueParam = new StringBuilder("(");
						List<String> condValueList = StringUtil.split(cond_value, ",");
						for (String s : condValueList) {
							condValueParam.append("?,");
							assembler.addParam(s);
						}
						condValueParam = new StringBuilder(condValueParam.substring(0, condValueParam.length() - 1));
						if (inMoreConRow) {
							condValueParam.append(")");
						}
					}
					// 如果是BETWEEN
					else if (autoTpCondInfo.getCon_relation().equals("BETWEEN")) {
						condValueParam = new StringBuilder("? AND ?");
						List<String> condValueList = StringUtil.split(cond_value, ",");
						assembler.addParam(condValueList.get(0));
						assembler.addParam(condValueList.get(1));
					} else {
						assembler.addParam(cond_value);
					}
					everyResultSql.append(autoTpCondInfo.getCond_para_name()).append(" ")
							.append(autoTpCondInfo.getCon_relation()).append(" ").append(condValueParam);
					// 考虑）问题
					while (everySql.endsWith(")")) {
						everyResultSql.append(")");
						everySql = everySql.trim();
						everySql = everySql.substring(0, everySql.length() - 1);
					}
					formatSqlList.set(con_row, everyResultSql.toString());
					if (inMoreConRow) {
						// IN在多行的情况下
						for (int l = 1; l < con_row_list.size(); l++) {
							// 替换第一行为in的条件
							// 然后去除别的行
							String everycon_row_String = con_row_list.get(l);
							int everycon_row = Integer.parseInt(everycon_row_String);
							formatSqlList.set(everycon_row, "");
						}
					}
					flag = true;
					break;
				}
			}
			if (!flag) {// 如果配置了条件 但是没有选条件
				String everySql = formatSqlList.get(con_row);
				StringBuilder everyresultsql;
				// 如果以WHERE开头 将条件关系值 替换为 1=1
				if (everySql.trim().startsWith("WHERE")) {
					everyresultsql = new StringBuilder("WHERE ");
					everySql = everySql.trim().replaceFirst("WHERE", "").trim();
					while (everySql.startsWith("(")) {
						everySql = everySql.replaceFirst("\\(", "").trim();
						everyresultsql.append("(");
					}
					everyresultsql.append(" 1=1 ");
					formatSqlList.set(con_row, everyresultsql.toString());
				}
				// 如果以AND开头,判断是否有(,没有则替换为1=1 不然有(则看下一行的判断如果是OR,则为1=0
				else if (everySql.trim().startsWith("AND")) {
					int leftcount = 0;
					int rightcount = 0;
					everyresultsql = new StringBuilder("AND ");
					everySql = everySql.trim().replaceFirst("AND", "").trim();
					while (everySql.startsWith("(")) {
						everySql = everySql.replaceFirst("\\(", "").trim();
						everyresultsql.append("(");
						leftcount++;
					}
					while (everySql.endsWith(")")) {
						everySql = everySql.substring(0, everySql.length() - 1);
						rightcount++;
					}
					if (everySql.contains(" IN ")) {
						rightcount--;
					}
					if (leftcount == 0) {
						everyresultsql.append(" 1=1 ");
					} else {
						if (con_row + 1 < formatSqlList.size()) {
							String nextsql = formatSqlList.get(con_row + 1);
							if (nextsql.startsWith("AND")) {
								everyresultsql.append(" 1=1 ");
							} else {
								everyresultsql.append(" 1=0 ");
							}
						}
					}
					for (int i = 0; i < rightcount; i++) {
						everyresultsql.append(")");
					}
					formatSqlList.set(con_row, everyresultsql.toString());
				}
				// 如果以OR开头,判断是否有(,没有则替换为1=1 不然有(则看下一行的判断如果是OR,则为1=0
				else if (everySql.trim().startsWith("OR")) {
					int leftcount = 0;
					int rightcount = 0;
					everyresultsql = new StringBuilder("OR ");
					everySql = everySql.trim().replaceFirst("OR", "").trim();
					while (everySql.startsWith("(")) {
						everySql = everySql.replaceFirst("\\(", "").trim();
						everyresultsql.append("(");
						leftcount++;
					}
					while (everySql.endsWith(")")) {
						everySql = everySql.substring(0, everySql.length() - 1);
						rightcount++;
					}
					if (everySql.contains(" IN ")) {
						rightcount--;
					}
					if (leftcount == 0) {
						everyresultsql.append(" 1=0 ");
					} else {
						if (con_row + 1 < formatSqlList.size()) {
							String nextSql = formatSqlList.get(con_row + 1);
							if (nextSql.startsWith("AND")) {
								everyresultsql.append(" 1=1 ");
							} else {
								everyresultsql.append(" 1=0 ");
							}
						}
					}
					for (int i = 0; i < rightcount; i++) {
						everyresultsql.append(")");
					}
					formatSqlList.set(con_row, everyresultsql.toString());
				}
				// 如果这一行不是以WHERE或者AND或者OR开头
				else {
					throw new BusinessException("SQL拼接出错");
				}
				if (inMoreConRow) {
					for (int l = 1; l < con_row_list.size(); l++) {// IN在多行的情况下
						// 替换第一行为in的条件
						// 然后去除别的行
						int everyCon_row = Integer.parseInt(con_row_list.get(l));
						if (l == con_row_list.size() - 1) {
							String tempresultsql = formatSqlList.get(everyCon_row);
							everyresultsql = new StringBuilder(tempresultsql.substring(tempresultsql.indexOf(")")));
							formatSqlList.set(everyCon_row, everyresultsql.toString());
						} else {
							formatSqlList.set(everyCon_row, "");
						}
					}
				}
			}
		}
		// 拼接起来 重新格式化一遍
		for (String s : formatSqlList) {
			resultSqlSb.append(" ").append(s.trim());
		}
		String formatResultSql = SQLUtils.format(resultSqlSb.toString(), dbType);
		resultSqlSb = new StringBuilder();
		List<String> formatResultSqlList = Arrays.asList(formatResultSql.split("\n"));
		for (String s : formatResultSqlList) {
			resultSqlSb.append(" ").append(s.trim());
		}
		// 以下为处理1=0和1=1的问题
		// 替换1 = 1 和 1 = 0 为空
		resultSqlSb = new StringBuilder(resultSqlSb.toString().replace("1 = 0", "")
				.replace("1 = 1", ""));
		// 如果有括号（那么就考虑去除1=0 和1=1之后可能的情况
		if (resultSqlSb.toString().contains("(")) {
			// 去除1=1之后，可能存在 （ 空 and/or 或者 and/or 空）的情况
			while (resultSqlSb.toString().contains("( AND ") || resultSqlSb.toString().contains(" AND )")
					|| resultSqlSb.toString().contains("( OR ")
					|| resultSqlSb.toString().contains(" OR )")) {
				resultSqlSb = new StringBuilder(resultSqlSb.toString()
						.replace("( AND ", "(")
						.replace(" AND )", ")")
						.replace("( OR ", "(")
						.replace(" OR )", ")"));
			}
			// 去除完以后就不存在（ and/or 或者 and/or）的情况了，但是可能有（）的情况
			while (resultSqlSb.toString().contains("()")) {
				resultSqlSb = new StringBuilder(resultSqlSb.toString().replace("()", ""));
			}
		}
		// 考虑完（）的情况后 因为去除了（） 所以 还有可能存在 and 空 or等情况 所以 and与or之间有两个空格
		while (resultSqlSb.toString().contains("AND  AND") || resultSqlSb.toString().contains("OR  AND")
				|| resultSqlSb.toString().contains("OR  OR")
				|| resultSqlSb.toString().contains("AND  OR")) {
			resultSqlSb = new StringBuilder(resultSqlSb.toString()
					.replace("AND  AND", "AND")
					.replace("OR  AND", "AND")
					.replace("OR  OR", "OR")
					.replace("AND  OR", "OR"));
		}
		// 最后考虑头和为的情况 存在where and的情况时是由于where 空 and造成的 所以where与and之间一样还是两个空格
		while (resultSqlSb.toString().contains("WHERE  AND") || resultSqlSb.toString().contains("WHERE  OR")) {
			resultSqlSb = new StringBuilder(resultSqlSb.toString()
					.replace("WHERE  AND", "WHERE")
					.replace("WHERE  OR", "WHERE"));
		}
		// 去除尾部的空格
		resultSqlSb = new StringBuilder(resultSqlSb.toString().trim());
		if (resultSqlSb.toString().endsWith("AND")) {
			resultSqlSb = new StringBuilder(resultSqlSb.substring(0, resultSqlSb.length() - 3));
		}
		if (resultSqlSb.toString().endsWith("OR")) {
			resultSqlSb = new StringBuilder(resultSqlSb.substring(0, resultSqlSb.length() - 2));
		}
		resultSqlSb = new StringBuilder(resultSqlSb.toString().trim());
		// 考虑尾部 存在and 空的情况，所以先去除空格然后删去尾部的and或者or
		if (resultSqlSb.toString().contains("AND  ") || resultSqlSb.toString().contains("OR  ")) {
			resultSqlSb = new StringBuilder(resultSqlSb.toString()
					.replace("AND  ", "")
					.replace("OR  ", ""));
			resultSqlSb = new StringBuilder(resultSqlSb.toString().trim());
		}
		// 重新格式化，由于去除((age = 18 ))中多余空格的问题
		formatResultSql = SQLUtils.format(resultSqlSb.toString(), dbType);
		resultSqlSb = new StringBuilder();
		formatResultSqlList = Arrays.asList(formatResultSql.split("\n"));
		for (String s : formatResultSqlList) {
			resultSqlSb.append(" ").append(s.trim());
		}
		// 格式化完成
		for (Auto_fetch_res auto_fetch_res : autoFetchRes) {
			Auto_tp_res_set auto_tp_res_set = Dbo.queryOneObject(Auto_tp_res_set.class,
					"select * from " + Auto_tp_res_set.TableName
							+ " where template_res_id = ?",
					auto_fetch_res.getFetch_res_id())
					.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
			// 模板结果中文字段
			String column_cn_name = auto_tp_res_set.getColumn_cn_name();
			String res_show_column = auto_tp_res_set.getRes_show_column();
			if (StringUtil.isNotBlank(res_show_column)) {
				resultSql.append(" `").append(column_cn_name).append("` as `").append(res_show_column).append("` ,");
			} else {
				resultSql.append(" `").append(column_cn_name).append("` ,");
			}
		}
		resultSql = new StringBuilder(resultSql.substring(0, resultSql.length() - 1));
		resultSql.append(" from (").append(resultSqlSb).append(") ").append(TempTableName);
		assembler.addSql(resultSql.toString());
		return assembler.sql();
	}

	@Method(desc = "查看取数sql", logicStep = "1.查询取数sql" +
			"2.格式化取数sql并返回")
	@Param(name = "fetch_sum_id", desc = "取数汇总ID", range = "配置取数模板时生成")
	@Return(desc = "返回取数sql", range = "无限制")
	public String getAccessSql(long fetch_sum_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.查询取数sql
		Auto_fetch_sum auto_fetch_sum = Dbo.queryOneObject(Auto_fetch_sum.class,
				"select fetch_sql from " + Auto_fetch_sum.TableName + " where fetch_sum_id = ?",
				fetch_sum_id)
				.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
		// 2.格式化取数sql并返回
		return auto_fetch_sum.getFetch_sql();
	}

	@Method(desc = "判断模板信息是否已不存在", logicStep = "1.判断模板信息是否已不存在")
	@Param(name = "template_id", desc = "自主取数模板ID", range = "新增自主取数模板时生成")
	private void isAutoTpInfoExist(long template_id) {
		// 1.判断模板信息是否已不存在
		if (Dbo.queryNumber(
				"select count(1) from " + Auto_tp_info.TableName + " where template_id=?",
				template_id)
				.orElseThrow(() -> new BusinessException("sql查询错误")) == 0) {
			throw new BusinessException("当前模板ID:" + template_id + "对应模板信息已不存在");
		}
	}

	@Method(desc = "查询我的取数信息", logicStep = "1.查询我的取数信息")
	@Return(desc = "返回我的取数信息", range = "无限制")
	public List<Map<String, Object>> getMyAccessInfo() {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.查询我的取数信息
		return Dbo.queryList(
				"select * from " + Auto_fetch_sum.TableName + " where user_id = ? and fetch_name is not null "
						+ " order by create_date desc,create_time desc",
				getUserId());
	}

	@Method(desc = "模糊查询我的取数信息", logicStep = "1.模糊查询我的取数信息")
	@Param(name = "fetch_name", desc = "取数名称", range = "新增我的取数信息时生成")
	@Return(desc = "返回模糊查询我的取数信息", range = "无限制")
	public List<Map<String, Object>> getMyAccessInfoByName(String fetch_name) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.模糊查询我的取数信息
		return Dbo.queryList(
				"select * from " + Auto_fetch_sum.TableName + " where user_id = ? and fetch_name like ?"
						+ " order by create_date desc,create_time desc",
				getUserId(), "%" + fetch_name + "%");
	}

	@Method(desc = "查看我的取数信息", logicStep = "1.查看我的取数信息")
	@Param(name = "fetch_sum_id", desc = "取数汇总表ID", range = "新增我的取数信息时生成")
	@Return(desc = "返回查看我的取数信息", range = "无限制")
	public List<Map<String, Object>> getMyAccessInfoById(long fetch_sum_id) {
		// 数据可访问权限处理方式，该方法不需要进行权限控制
		// 1.查看我的取数信息
		return Dbo.queryList("select fetch_name, fetch_desc from " + Auto_fetch_sum.TableName
						+ " where fetch_sum_id = ?",
				fetch_sum_id);
	}

	@Method(desc = "取数时清单查询 显示条数方法", logicStep = "1.获取取数sql" +
			"2.根据sql查询数据结果")
	@Param(name = "fetch_sum_id", desc = "取数汇总表ID", range = "新增我的取数信息时生成")
	@Param(name = "showNum", desc = "显示条数", range = "正整数", valueIfNull = "10")
	@Return(desc = "返回取数结果", range = "无限制")
	public List<Map<String, Object>> getAccessResultByNumber(long fetch_sum_id, int showNum) {
		// 数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 1.获取取数sql
		String accessSql = getAccessSql(fetch_sum_id);
		List<Map<String, Object>> resultData = new ArrayList<>();
		// 2.根据sql查询数据结果
		new ProcessingData() {
			@Override
			public void dealLine(Map<String, Object> map) {
				resultData.add(map);
			}
		}.getPageDataLayer(accessSql, Dbo.db(), 0, Math.min(showNum, 1000));
		// 2.返回数据结果
		return resultData;
	}

	@Method(desc = "我的取数下载模板", logicStep = "")
	@Param(name = "fetch_sum_id", desc = "取数汇总表ID", range = "新增我的取数信息时生成")
	public void downloadMyAccessTemplate(long fetch_sum_id) {
		Auto_fetch_sum auto_fetch_sum = Dbo.queryOneObject(Auto_fetch_sum.class,
				"select fetch_sql,fetch_name from " + Auto_fetch_sum.TableName + " where fetch_sum_id = ?",
				fetch_sum_id)
				.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
		String fileName = WebinfoConf.FileUpload_SavedDirName + File.separator +
				auto_fetch_sum.getFetch_name() + "_" + DateUtil.getDateTime() + ".csv";
		new ProcessingData() {
			@Override
			public void dealLine(Map<String, Object> map) {
				AutoOperateCommon.writeFile(map, fileName);
			}
		}.getDataLayer(auto_fetch_sum.getFetch_sql(), Dbo.db());
	}

	@Method(desc = "获取数据可视化组件信息", logicStep = "1.获取数据可视化组件信息")
	@Return(desc = "返回获取数据可视化组件信息", range = "无限制")
	public List<Map<String, Object>> getVisualComponentInfo() {
		// 数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 1.获取数据可视化组件信息
		return Dbo.queryList("SELECT * FROM " + Auto_comp_sum.TableName
				+ " WHERE create_user = ? order by create_date desc,create_time desc", getUserId());
	}

	@Method(desc = "获取自主数据数据集表信息", logicStep = "1.判断是否为自主数据数据集" +
			"2.获取自主数据数据集表信息")
	@Param(name = "data_source", desc = "可视化源对象值", range = "使用(AutoSourceObject代码项)")
	@Return(desc = "返回自主数据数据集表信息", range = "无限制")
	public List<Map<String, Object>> getTAutoDataTableName(String data_source) {
		// 1.判断是否为自主数据数据集
		if (AutoSourceObject.ZiZhuShuJuShuJuJi != AutoSourceObject.ofEnumByCode(data_source)) {
			throw new BusinessException("不是自主数据数据集,请检查" + data_source);
		}
		// 2.获取自主数据数据集表信息
		List<Map<String, Object>> auto_fetch_sums = Dbo.queryList(
				"SELECT * FROM " + Auto_fetch_sum.TableName
						+ " WHERE fetch_status = ?", AutoFetchStatus.WanCheng.getCode());
		for (Map<String, Object> fetchSum : auto_fetch_sums) {
			fetchSum.put("isParent", false);
			fetchSum.put("tablename", fetchSum.get("fetch_name"));
			fetchSum.put("remark", fetchSum.get("fetch_desc"));
		}
		return auto_fetch_sums;
	}

	@Method(desc = "根据表名获取字段信息", logicStep = "1.获取自主数据集字段信息" +
			"2.根据表名获取系统数据集字段信息" +
			"3.返回字段信息")
	@Param(name = "table_name", desc = "表名", range = "无限制")
	@Param(name = "data_source", desc = "可视化源对象值", range = "使用(AutoSourceObject代码项)")
	@Return(desc = "返回字段信息", range = "无限制")
	public Map<String, Object> getColumnByName(String table_name, String data_source) {
		// 数据可访问权限处理方式：该方法不需要进行访问权限限制
		Map<String, Object> columnMap = new HashMap<>();
		// 数字类型列
		List<Map<String, Object>> numColumnList = new ArrayList<>();
		// 度量列
		List<Map<String, Object>> measureColumnList = new ArrayList<>();
		if (AutoSourceObject.ZiZhuShuJuShuJuJi == AutoSourceObject.ofEnumByCode(data_source)) {
			// 1.获取自主数据集字段信息
			List<Map<String, Object>> columnList = Dbo.queryList(
					"SELECT t1.fetch_res_name,t3.column_type as column_type"
							+ " FROM " + Auto_fetch_res.TableName
							+ " t1 left join " + Auto_fetch_sum.TableName
							+ " t2 on t1.fetch_sum_id = t2.fetch_sum_id"
							+ " left join " + Auto_tp_res_set.TableName
							+ " t3 on t1.template_res_id = t3.template_res_id WHERE t2.fetch_name = ?"
							+ " AND t2.fetch_status = ? order by t1.show_num",
					table_name, AutoFetchStatus.WanCheng.getCode());
			for (Map<String, Object> map : columnList) {
				if (numbersArray.contains(map.get("column_type").toString())) {
					numColumnList.add(map);
				}
				// fixme 02是啥？
				if ("02".equals(map.get("column_type").toString())) {
					measureColumnList.add(map);
				}
			}
			columnMap.put("columns", columnList);
			columnMap.put("numColumns", numColumnList);
			columnMap.put("measureColumns", measureColumnList);
		} else if (AutoSourceObject.XiTongJiShuJuJi == AutoSourceObject.ofEnumByCode(data_source)) {
			// 2.根据表名获取系统数据集字段信息
			List<Map<String, Object>> columnList = DataTableUtil.getColumnByTableName(Dbo.db(), table_name);
			for (Map<String, Object> map : columnList) {
				if (numbersArray.contains(map.get("column_type").toString())) {
					numColumnList.add(map);
				}
			}
			columnMap.put("columns", columnList);
			columnMap.put("numColumns", numColumnList);
		}
		// 3.返回字段信息
		return columnMap;
	}

	@Method(desc = "根据可视化组件ID查看可视化组件信息", logicStep = "1.查询组件汇总表" +
			"2.根据组件id查询组件条件表" +
			"3.根据组件id查询组件分组表" +
			"4.根据组件id查询组件数据汇总信息表" +
			"5.根据组件id查询组件横纵纵轴信息表 字段显示类型show_type使用IsFlag代码项 0:x轴，1:y轴" +
			"6.根据组件id查询图表标题字体属性信息表" +
			"7.根据组件id查询x,y轴标签字体属性信息表" +
			"8.根据组件id查询x/y轴配置信息表" +
			"9.根据组件id查询x/y轴标签配置信息表" +
			"10.根据组件id查询x/y轴线配置信息表" +
			"11.根据组件id查询二维表样式信息表" +
			"12.根据组件id查询图表配置信息表" +
			"13.根据组件id查询文本标签信息表" +
			"14.根据组件id查询图例信息表" +
			"15.获取组件查询结果" +
			"16.获取图表结果" +
			"17.获取列信息" +
			"18.返回根据可视化组件ID查看可视化组件信息")
	@Param(name = "component_id", desc = "组件ID", range = "创建组件时生成")
	@Return(desc = "返回根据可视化组件ID查看可视化组件信息", range = "无限制")
	public Map<String, Object> getVisualComponentInfoById(long component_id) {
		Map<String, Object> resultMap = new HashMap<>();
		// 1.查询组件汇总表
		Auto_comp_sum auto_comp_sum = Dbo.queryOneObject(Auto_comp_sum.class,
				"SELECT * FROM " + Auto_comp_sum.TableName + " WHERE component_id = ?",
				component_id)
				.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
		resultMap.put("compSum", auto_comp_sum);
		// 2.根据组件id查询组件条件表
		List<Map<String, Object>> compCondList = Dbo.queryList(
				"SELECT * FROM " + Auto_comp_cond.TableName + " WHERE component_id = ?"
				, component_id);
		resultMap.put("compCond", compCondList);
		// 3.根据组件id查询组件分组表
		List<Map<String, Object>> compGroupList = Dbo.queryList(
				"SELECT * FROM " + Auto_comp_group.TableName + " WHERE component_id = ?",
				component_id);
		resultMap.put("compGroup", compGroupList);
		// 4.根据组件id查询组件数据汇总信息表
		List<Map<String, Object>> compDataSumList = Dbo.queryList(
				"SELECT * FROM " + Auto_comp_data_sum.TableName + " WHERE component_id = ?",
				component_id);
		resultMap.put("compDataSum", compDataSumList);
		// 5.根据组件id查询组件横纵纵轴信息表 字段显示类型show_type使用IsFlag代码项 0:x轴，1:y轴
		List<Map<String, Object>> xAxisColList = Dbo.queryList(
				"SELECT * FROM " + Auto_axis_col_info.TableName
						+ " WHERE component_id = ? AND show_type = ?",
				component_id, IsFlag.Fou.getCode());
		resultMap.put("xAxisCol", xAxisColList);
		List<Map<String, Object>> yAxisColList = Dbo.queryList(
				"SELECT * FROM " + Auto_axis_col_info.TableName
						+ " WHERE component_id = ? AND show_type = ?",
				component_id, IsFlag.Shi.getCode());
		resultMap.put("yAxisCol", yAxisColList);
		String[] x_columns = new String[xAxisColList.size()];
		for (int i = 0; i < xAxisColList.size(); i++) {
			x_columns[i] = xAxisColList.get(i).get("column_name").toString();
		}
		String[] y_columns = new String[yAxisColList.size()];
		for (int i = 0; i < yAxisColList.size(); i++) {
			y_columns[i] = yAxisColList.get(i).get("column_name").toString();
		}
		// 6.根据组件id查询图表标题字体属性信息表
		Map<String, Object> fontInfoMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_font_info.TableName + " WHERE font_corr_id = ?",
				component_id);
		resultMap.put("titleFontInfo", fontInfoMap);
		// 7.根据组件id查询x,y轴标签字体属性信息表,因为x/y轴字体是一样的,保存的时候是以x轴编号保存所以这里以x轴编号查询
		Map<String, Object> xFontInfoMap = Dbo.queryOneObject("SELECT * FROM " + Auto_font_info.TableName
						+ " WHERE font_corr_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
						+ " WHERE component_id = ? AND axis_type = ?)",
				component_id, IsFlag.Fou.getCode());
		resultMap.put("axisFontInfo", xFontInfoMap);
		// 8.根据组件id查询x/y轴配置信息表
		List<Map<String, Object>> xAxisInfoList = Dbo.queryList(
				"SELECT * FROM " + Auto_axis_info.TableName + " WHERE component_id = ? AND axis_type = ?",
				component_id, IsFlag.Fou.getCode());
		resultMap.put("xAxisInfo", xAxisInfoList);
		List<Map<String, Object>> yAxisInfoList = Dbo.queryList(
				"SELECT * FROM " + Auto_axis_info.TableName + " WHERE component_id = ? AND axis_type = ?",
				component_id, IsFlag.Shi.getCode());
		resultMap.put("yAxisInfo", yAxisInfoList);
		// 9.根据组件id查询x/y轴标签配置信息表
		Map<String, Object> xAxislabelMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_axislabel_info.TableName
						+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
						+ " WHERE component_id = ? AND axis_type = ?)",
				component_id, IsFlag.Fou.getCode());
		resultMap.put("xAxislabel", xAxislabelMap);
		Map<String, Object> yAxislabelMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_axislabel_info.TableName
						+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
						+ " WHERE component_id = ? AND axis_type = ?)",
				component_id, IsFlag.Shi.getCode());
		resultMap.put("yAxislabel", yAxislabelMap);
		// 10.根据组件id查询x/y轴线配置信息表
		Map<String, Object> xAxislineMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_axisline_info.TableName
						+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
						+ " WHERE component_id = ? AND axis_type = ?)",
				component_id, IsFlag.Fou.getCode());
		resultMap.put("xAxisline", xAxislineMap);
		Map<String, Object> yAxislineMap = Dbo.queryOneObject("SELECT * FROM " + Auto_axisline_info.TableName
						+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
						+ " WHERE component_id = ? AND axis_type = ?)",
				component_id, IsFlag.Shi.getCode());
		resultMap.put("yAxisline", yAxislineMap);
		// 11.根据组件id查询二维表样式信息表
		Map<String, Object> tableInfoMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_table_info.TableName + " WHERE component_id = ?",
				component_id);
		resultMap.put("twoDimensionalTable", tableInfoMap);
		// 12.根据组件id查询图表配置信息表
		Map<String, Object> chartsconfigMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_chartsconfig.TableName + " WHERE component_id = ?",
				component_id);
		resultMap.put("chartsconfig", chartsconfigMap);
		// 13.根据组件id查询文本标签信息表
		Map<String, Object> textLabelMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_label.TableName + " WHERE label_corr_id = ?", component_id);
		resultMap.put("textLabel", textLabelMap);
		// 14.根据组件id查询图例信息表
		Map<String, Object> legendMap = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_legend_info.TableName + " WHERE component_id = ?", component_id);
		resultMap.put("legendInfo", legendMap);
		// 15.获取组件查询结果
		Map<String, Object> visualComponentResult = getVisualComponentResult(auto_comp_sum.getExe_sql());
		resultMap.putAll(visualComponentResult);
		// 16.获取图表结果
		Map<String, Object> chartShowMap = getChartShow(auto_comp_sum.getExe_sql(), x_columns, y_columns,
				auto_comp_sum.getChart_type());
		resultMap.putAll(chartShowMap);
		// 17.获取列信息
		Map<String, Object> tableColumn = getColumnByName(auto_comp_sum.getSources_obj(),
				auto_comp_sum.getData_source());
		resultMap.put("columnAndNumberColumnInfo", tableColumn);
		// 18.返回根据可视化组件ID查看可视化组件信息
		return resultMap;
	}

	@Method(desc = "获取图表显示", logicStep = "1.获取组件数据" +
			"2.根据不同图标类型获取图表数据" +
			"3.返回图标显示数据")
	@Param(name = "exe_sql", desc = "执行sql", range = "无限制")
	@Param(name = "x_columns", desc = "x轴列信息", range = "无限制", nullable = true)
	@Param(name = "y_columns", desc = "y轴列信息", range = "无限制", nullable = true)
	@Param(name = "chart_type", desc = "图标类型", range = "无限制")
	@Return(desc = "返回图标显示数据", range = "无限制")
	public Map<String, Object> getChartShow(String exe_sql, String[] x_columns, String[] y_columns,
	                                        String chart_type) {
		List<Map<String, Object>> componentList = new ArrayList<>();
		// 1.获取组件数据
		new ProcessingData() {
			@Override
			public void dealLine(Map<String, Object> map) {
				componentList.add(map);
			}
		}.getDataLayer(exe_sql, Dbo.db());
		Map<String, Object> resultMap = new HashMap<>();
		// 2.根据不同图标类型获取图表数据
		if (LINE.equals(chart_type) || BAR.equals(chart_type) || BL.equals(chart_type)) {
			// 折线图和柱状图
			putDataForLine(componentList, x_columns, y_columns, chart_type, resultMap);
		} else if (PIE.equals(chart_type) || HUANPIE.equals(chart_type) || FASANPIE.equals(chart_type)) {
			// 饼图
			putDataForPie(componentList, x_columns, y_columns, chart_type, resultMap);
		} else if (SCATTER.equals(chart_type)) {
			// 散点图
			putDataForScatter(componentList, x_columns, y_columns, resultMap);
		} else if (CARD.equals(chart_type)) {
			// 卡片
			List<Object> cardData = new ArrayList<>();
			componentList.get(0).forEach((k, v) -> cardData.add(v));
			resultMap.put("cardData", cardData);
		} else if (TABLE.equals(chart_type)) {
			// 二维表
			resultMap.put("tableData", componentList);
		} else if (BOXPLOT.equals(chart_type)) {
			// 盒型图
			putDataForBoxplot(componentList, x_columns, resultMap);
		} else if (TREEMAP.equals(chart_type)) {
			// 矩形树图
			putDataForTreemap(componentList, x_columns, y_columns, resultMap);
		} else if (BARMD.equals(chart_type)) {
			// 混合图
			putDateForBarmd(componentList, x_columns, y_columns, resultMap);
		} else if (BUBBLE.equals(chart_type)) {
			// 气泡图
			putDataForBubble(componentList, x_columns, y_columns, resultMap);
		} else if (POLARBAR.equals(chart_type)) {
			// 极坐标柱状图
			putDataForPolarbar(componentList, x_columns, y_columns, resultMap);
		} else if (BLSIMPLE.equals(chart_type)) {
			// 柱状折线混合图-简单
			putDataForBLSimple(componentList, x_columns, y_columns, resultMap);
		} else if (MAP.equals(chart_type)) {
			// 地理坐标/地图
			putDataForBubble(componentList, x_columns, y_columns, resultMap);
		} else {
			throw new BusinessException("暂不支持该种图例类型" + chart_type);
		}
		// 3.返回图标显示数据
		return resultMap;
	}

	private void putDataForBLSimple(List<Map<String, Object>> componentList, String[] x_columns,
	                                String[] y_columns, Map<String, Object> resultMap) {
		if (x_columns.length < 1 || y_columns.length < 2) {
			return;
		}
		List<Object> xAxisData = new ArrayList<>();
		List<Object> yAxisData1 = new ArrayList<>();
		List<Object> yAxisData2 = new ArrayList<>();
		for (Map<String, Object> stringObjectMap : componentList) {
			xAxisData.add(stringObjectMap.get(x_columns[0]));
			yAxisData1.add(stringObjectMap.get(y_columns[0]));
			yAxisData2.add(stringObjectMap.get(y_columns[1]));
		}
		resultMap.put("series1Name", y_columns[0]);
		resultMap.put("series1Data", yAxisData1);
		resultMap.put("series2Name", y_columns[1]);
		resultMap.put("series2Data", yAxisData2);
		resultMap.put("xAxisData", xAxisData);
	}

	private void putDataForPolarbar(List<Map<String, Object>> componentList, String[] x_columns,
	                                String[] y_columns, Map<String, Object> resultMap) {
		String x_column = x_columns[0];
		String y_column = y_columns[0];
		List<Object> radiusData = new ArrayList<>();
		List<Object> seriesData = new ArrayList<>();
		for (Map<String, Object> stringObjectMap : componentList) {
			radiusData.add(stringObjectMap.get(x_column));
			seriesData.add(stringObjectMap.get(y_column));
		}

		resultMap.put("radiusData", radiusData);
		resultMap.put("seriesData", seriesData);
	}

	private void putDataForBubble(List<Map<String, Object>> componentList, String[] x_columns,
	                              String[] y_columns, Map<String, Object> resultMap) {
		List<Map<String, Object>> seriesData = new ArrayList<>();
		for (Map<String, Object> stringObjectMap : componentList) {
			Map<String, Object> map = new HashMap<>();
			map.put("name", stringObjectMap.get(x_columns[0]));
			map.put("value", stringObjectMap.get(y_columns[0]));
			seriesData.add(map);
		}
		resultMap.put("seriesData", seriesData);
	}

	private void putDateForBarmd(List<Map<String, Object>> componentList, String[] x_columns,
	                             String[] y_columns, Map<String, Object> resultMap) {
		List<List<Object>> data = new ArrayList<>();
		String y_column = y_columns[0];
		for (Map<String, Object> stringObjectMap : componentList) {
			List<Object> dataTmp = new ArrayList<>();
			for (String x_column : x_columns) {
				dataTmp.add(stringObjectMap.get(x_column));
			}
			//支持各种数值类型
			dataTmp.add(stringObjectMap.get(y_column));
			data.add(dataTmp);
		}
		resultMap.put("xColLen", x_columns.length);
		resultMap.put("barmdData", data);
	}

	private void putDataForTreemap(List<Map<String, Object>> componentList, String[] x_columns,
	                               String[] y_columns, Map<String, Object> resultMap) {
		List<Map<String, Object>> seriesData = new ArrayList<>();
		Map<String, Map<String, Object>> map = new HashMap<>();
		for (Map<String, Object> stringObjectMap : componentList) {
			Map<String, Object> xMap = new HashMap<>();
			if (x_columns.length == 1) {
				xMap.put("name", stringObjectMap.get(x_columns[0]));
				xMap.put("value", stringObjectMap.get(y_columns[0]));
				seriesData.add(xMap);
			} else {
				String childrenName = stringObjectMap.get(x_columns[0]).toString();
				xMap.put("name", stringObjectMap.get(x_columns[1]));
				xMap.put("value", stringObjectMap.get(y_columns[0]));
				Map<String, Object> mapTemp;
				if (!map.containsKey(childrenName)) {
					mapTemp = new HashMap<>();
					List<Object> list = new ArrayList<>();
					list.add(map);
					mapTemp.put("children", list);
					mapTemp.put("name", childrenName);
				} else {
					mapTemp = map.get(childrenName);
					List<Object> mapList = JsonUtil.toObject(JsonUtil.toJson(mapTemp.get("children")),
							new TypeReference<List<Object>>() {
							}.getType());
					mapList.add(map);
					mapTemp.put("children", mapList);
				}
				map.put(childrenName, mapTemp);
			}
		}
		if (x_columns.length == 1) {
			resultMap.put("seriesData", seriesData);
		} else {
			resultMap.put("seriesData", map.values());
		}
	}

	private void putDataForBoxplot(List<Map<String, Object>> componentList, String[] x_columns,
	                               Map<String, Object> resultMap) {
		// 添加legend的值
		if (x_columns != null && x_columns.length > 0) {
			List<Object> legend_data = new ArrayList<>();
			Collections.addAll(legend_data, x_columns);
			resultMap.put("legend_data", legend_data);
			// 添加y轴的值
			List<Map<String, List<Object>>> yList = new ArrayList<>();
			for (int i = 0; i < componentList.size(); i++) {
				for (int j = 0; j < x_columns.length; j++) {
					if (i == 0) {
						Map<String, List<Object>> map = new HashMap<>();
						List<Object> xList = new ArrayList<>();
						xList.add(componentList.get(i).get(x_columns[j].trim()));
						map.put("data", xList);
						yList.add(map);
					} else {
						yList.get(j).get("data").add(componentList.get(i).get(x_columns[j].trim()));
					}
				}
			}
			resultMap.put("boxplot", yList);
		}
	}

	private void putDataForScatter(List<Map<String, Object>> componentList, String[] x_columns,
	                               String[] y_columns, Map<String, Object> resultMap) {
		List<Map<String, Object>> scatterData = new ArrayList<>();
		for (Map<String, Object> stringObjectMap : componentList) {
			Map<String, Object> map = new HashMap<>();
			map.put(IsFlag.Fou.getCode(), stringObjectMap.get(x_columns[0]));
			map.put(IsFlag.Shi.getCode(), stringObjectMap.get(y_columns[0]));
			scatterData.add(map);
		}
		resultMap.put("scatterData", scatterData);
	}

	private void putDataForPie(List<Map<String, Object>> componentList, String[] x_columns,
	                           String[] y_columns, String chart_type, Map<String, Object> resultMap) {

		List<String> legendData = new ArrayList<>();
		List<Map<String, Object>> seriesArray = new ArrayList<>();
		List<Map<String, Object>> seriesData = new ArrayList<>();
		int count = 0;
		for (Map<String, Object> stringObjectMap : componentList) {
			Map<String, Object> map = new HashMap<>();
			legendData.add(stringObjectMap.get(x_columns[0]).toString());
			map.put("name", stringObjectMap.get(x_columns[0]));
			map.put("value", stringObjectMap.get(y_columns[0]));
			count = count + Integer.parseInt(stringObjectMap.get(y_columns[0]).toString());
			seriesData.add(map);
		}
		resultMap.put("count", count);
		// 饼图series
		Map<String, Object> series = new HashMap<>();
		series.put("data", seriesData);
		series.put("name", x_columns[0]);
		series.put("type", "pie");

		if (PIE.equals(chart_type)) {
			// 标准饼图
			series.put("radius", "50%");
			resultMap.put("legendData", legendData);
		} else if (HUANPIE.equals(chart_type)) {
			// 环形饼图
			List<String> radius = new ArrayList<>();
			radius.add("35%");
			radius.add("60%");
			series.put("radius", radius);
			resultMap.put("pietype", "huanpie");
			resultMap.put("legendData", legendData);
		} else if (FASANPIE.equals(chart_type)) {
			// 发散饼图
			series.put("roseType", "radius");
			resultMap.put("legendData", legendData);
		}
		seriesArray.add(series);
		resultMap.put("seriesArray", seriesArray);
	}

	private void putDataForLine(List<Map<String, Object>> componentList, String[] x_columns,
	                            String[] y_columns, String chart_type,
	                            Map<String, Object> resultMap) {
		// 添加legend的值
		if (y_columns != null && y_columns.length > 0) {
			List<Object> columns = new ArrayList<>();
			Collections.addAll(columns, y_columns);
			resultMap.put("legend_data", columns);
			// 添加y轴的值
			List<Map<String, Object>> yList = new ArrayList<>();
			for (int i = 0; i < componentList.size(); i++) {
				for (int j = 0; j < y_columns.length; j++) {
					if (i == 0) {
						Map<String, Object> map = new HashMap<>();
						List<Object> data = new ArrayList<>();
						data.add(componentList.get(i).get(y_columns[j].trim()));
						resultMap.put("name", y_columns[j]);
						if ("bl".equals(chart_type)) {
							if (j < 2) {
								map.put("type", "bar");
								map.put("stack", "two");
							} else {
								map.put("type", "line");
								map.put("yAxisIndex", "1");
							}
						} else {
							map.put("type", chart_type);
						}
						map.put("data", data);
						yList.add(map);
					} else {
						List<Object> data = JsonUtil.toObject(yList.get(j).get("data").toString(),
								new TypeReference<List<Object>>() {
								}.getType());
						data.add(componentList.get(i).get(y_columns[j].trim()));
					}
				}
			}
			resultMap.put("seriesArray", yList);
		}
		// 添加x轴的值，默认为一个取x_columns[0]
		if (x_columns != null && x_columns.length > 0) {
			List<String> xList = new ArrayList<>();
			for (Map<String, Object> stringObjectMap : componentList) {
				xList.add(stringObjectMap.get(x_columns[0].trim()).toString());
			}
			resultMap.put("xArray", xList);
		}
	}

	@Method(desc = "获取可视化组件结果（获取答案）", logicStep = "1.获取列信息" +
			"2.获取组件数据" +
			"3.封装并返回列信息与组件结果信息")
	@Param(name = "exe_sql", desc = "可视化组件执行sql", range = "无限制")
	@Return(desc = "返回列信息与组件结果信息", range = "无限制")
	public Map<String, Object> getVisualComponentResult(String exe_sql) {
		List<Map<String, Object>> visualComponentList = new ArrayList<>();
		List<String> columnList = new ArrayList<>();
		new ProcessingData() {
			@Override
			public void dealLine(Map<String, Object> map) {
				// 1.获取列信息
				map.forEach((k, v) -> columnList.add(k)
				);
				// 2.获取组件数据
				visualComponentList.add(map);
			}
		}.getDataLayer(exe_sql, Dbo.db());
		// 3.封装并返回列信息与组件结果信息
		Map<String, Object> visualComponentMap = new HashMap<>();
		visualComponentMap.put("visualComponentList", visualComponentList);
		visualComponentMap.put("columnList", columnList);
		return visualComponentMap;
	}

	@Method(desc = "可视化创建组件根据条件获取sql", logicStep = "")
	@Param(name = "componentBean", desc = "可视化组件参数实体bean", range = "自定义无限制", isBean = true)
	@Param(name = "autoCompConds", desc = "组件条件表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompGroups", desc = "组件分组表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompDataSums", desc = "组件数据汇总信息表对象数组", range = "与数据库表规则一致", isBean = true)
	@Return(desc = "返回可视化创建组件过滤条件获取答案信息", range = "")
	public String getSqlByCondition(ComponentBean componentBean, Auto_comp_cond[] autoCompConds,
	                                Auto_comp_group[] autoCompGroups, Auto_comp_data_sum[] autoCompDataSums) {
		Validator.notNull(componentBean.getFetch_name(), "取数名称不能为空");
		Validator.notNull(componentBean.getData_source(), "数据来源不能为空");
		Validator.notNull(componentBean.getFetch_sum_id(), "取数汇总ID不能为空");
		String fetch_sql;
		if (AutoSourceObject.ZiZhuShuJuShuJuJi == AutoSourceObject.ofEnumByCode(componentBean.getData_source())) {
			// 自主取数数据集
			// 获取自主取数部分的sql
			fetch_sql = getAccessSql(componentBean.getFetch_sum_id());
		} else if (AutoSourceObject.XiTongJiShuJuJi == AutoSourceObject.ofEnumByCode(componentBean.getData_source())) {
			// 系统级数据集
			// 拼接系统数据集sql
			fetch_sql = "SELECT" + Constant.SPACE + "*" + Constant.SPACE +
					"FROM" + Constant.SPACE + componentBean.getFetch_name();
		} else {//数据组件数据集
			throw new BusinessException("暂不支持该种数据集" + componentBean.getData_source());
		}
		// 添加select 部分
		StringBuilder result_sql = new StringBuilder();
		result_sql.append("SELECT").append(Constant.SPACE);
		for (Auto_comp_data_sum auto_comp_data_sum : autoCompDataSums) {
			String column_name = auto_comp_data_sum.getColumn_name();
			String summary_type = auto_comp_data_sum.getSummary_type();
			if (AutoDataSumType.QiuHe == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append("sum(").append(column_name).append(") ,");
			} else if (AutoDataSumType.QiuPingJun == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append("avg(").append(column_name).append(") ,");
			} else if (AutoDataSumType.QiuZuiDaZhi == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append("max(").append(column_name).append(") ,");
			} else if (AutoDataSumType.QiuZuiXiaoZhi == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append("min(").append(column_name).append(") ,");
			} else if (AutoDataSumType.ZongHangShu == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append(column_name).append(" ,");
			} else if (AutoDataSumType.YuanShiShuJu == AutoDataSumType.ofEnumByCode(summary_type)) {
				result_sql.append(column_name).append(",");
			} else {
				result_sql.append("*,");
			}
		}
		// 去除,
		result_sql.deleteCharAt(result_sql.length() - 1);
		// 添加子查询
		result_sql.append(Constant.SPACE).append("FROM (").append(fetch_sql).append(") ")
				.append(Constant.SPACE).append(TempTableName);
		// 对最大的N个做单独处理
		ArrayList<String> upArray = new ArrayList<>();
		// 对最小的N个做单独处理
		ArrayList<String> lowArray = new ArrayList<>();
		boolean flag = true;
		if (autoCompConds != null && autoCompConds.length > 0) {
			// 添加 where部分
			for (Auto_comp_cond auto_comp_cond : autoCompConds) {
				String cond_en_column = auto_comp_cond.getCond_en_column();
				String operator = auto_comp_cond.getOperator();
				String cond_value = auto_comp_cond.getCond_value();
				if (AutoDataOperator.ZuiDaDeNGe == AutoDataOperator.ofEnumByCode(operator)) {
					upArray.add(cond_en_column + Constant.SQLDELIMITER + cond_value);
				} else if (AutoDataOperator.ZuiXiaoDeNGe == AutoDataOperator.ofEnumByCode(operator)) {
					lowArray.add(cond_en_column + Constant.SQLDELIMITER + cond_value);
				} else {
					if (flag) {
						result_sql.append(Constant.SPACE).append("WHERE").append(Constant.SPACE);
						flag = false;
					}
				}
			}
			if (StringUtil.isNotBlank(componentBean.getCondition_sql())) {
				result_sql.append(componentBean.getCondition_sql()).append(Constant.SPACE);
			}
		}
		if (autoCompGroups != null && autoCompGroups.length > 0) {
			// 添加 group by
			result_sql.append(Constant.SPACE).append("GROUP BY").append(Constant.SPACE);
			for (Auto_comp_group auto_comp_group : autoCompGroups) {
				String column_name = auto_comp_group.getColumn_name();
				result_sql.append("`").append(column_name).append("`,");
			}
			// 去除,添加limit条件
			result_sql.deleteCharAt(result_sql.length() - 1);
		}
		if (!upArray.isEmpty()) {
			result_sql.append(Constant.SPACE).append("ORDER BY").append(Constant.SPACE)
					.append(upArray.get(0).split(Constant.SQLDELIMITER)[0]).append(Constant.SPACE).append("DESC LIMIT")
					.append(Constant.SPACE).append(upArray.get(0).split(Constant.SQLDELIMITER)[1]);
		}
		if (!lowArray.isEmpty()) {
			result_sql.append(Constant.SPACE).append("ORDER BY").append(Constant.SPACE)
					.append(lowArray.get(0).split(Constant.SQLDELIMITER)[0]).append(Constant.SPACE).append("LIMIT")
					.append(Constant.SPACE).append(lowArray.get(0).split(Constant.SQLDELIMITER)[1]);
		}
		return result_sql.toString();
	}

	@Method(desc = "获取条件逻辑", logicStep = "1.判断操作符是否为最大的N个或者最小的N个" +
			"2.判断操作符是否为介于或不介于" +
			"3.返回条件逻辑")
	@Param(name = "autoCompConds", desc = "组件条件表对象数组", range = "与数据库表规则一致", isBean = true)
	@Return(desc = "返回条件逻辑", range = "无限制")
	public String getConditionLogic(Auto_comp_cond[] autoCompConds) {
		StringBuilder result_where = new StringBuilder();
		if (autoCompConds != null && autoCompConds.length != 0) {
			// 添加 where部分
			for (Auto_comp_cond auto_comp_cond : autoCompConds) {
				String cond_en_column = auto_comp_cond.getCond_en_column();
				String operator = auto_comp_cond.getOperator();
				String cond_value = auto_comp_cond.getCond_value();
				String arithmetic_logic = auto_comp_cond.getArithmetic_logic();
				// 1.判断操作符是否为最大的N个或者最小的N个
				if (AutoDataOperator.ZuiDaDeNGe != AutoDataOperator.ofEnumByCode(operator) &&
						AutoDataOperator.ZuiXiaoDeNGe == AutoDataOperator.ofEnumByCode(operator)) {
					// 2.判断操作符是否为介于或不介于
					if (AutoDataOperator.JieYu == AutoDataOperator.ofEnumByCode(operator)
							|| AutoDataOperator.BuJieYu == AutoDataOperator.ofEnumByCode(operator)) {
						result_where.append("`").append(cond_en_column).append("`").append(Constant.SPACE)
								.append(transOperator(operator)).append(Constant.SPACE).append("(")
								.append(cond_value).append(")").append(Constant.SPACE)
								.append((arithmetic_logic).toUpperCase()).append(Constant.SPACE);
					} else {
						result_where.append("`").append(cond_en_column).append("`").append(Constant.SPACE)
								.append(transOperator(operator)).append(Constant.SPACE).append(cond_value)
								.append(Constant.SPACE).append((arithmetic_logic).toUpperCase())
								.append(Constant.SPACE);
					}
				}
			}
			// 去除and
			result_where.delete(result_where.length() - 4, result_where.length());
		}
		// 3.返回条件逻辑
		return result_where.toString();
	}

	@Method(desc = "转换sql操作符", logicStep = "1.根据可视化数据操作符代码项转换为sql操作符" +
			"2.返回转换后的sql操作符")
	@Param(name = "operator", desc = "操作符", range = "无限制")
	@Return(desc = "返回转换后的操作符", range = "无限制")
	private String transOperator(String operator) {
		// 1.根据可视化数据操作符代码项转换为sql操作符
		if (AutoDataOperator.BuDengYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "!=";
		} else if (AutoDataOperator.BuJieYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "NOT IN";
		} else if (AutoDataOperator.DaYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = ">";
		} else if (AutoDataOperator.DaYuDengYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = ">=";
		} else if (AutoDataOperator.DengYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "=";
		} else if (AutoDataOperator.JieYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "IN";
		} else if (AutoDataOperator.WeiKong == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "IS NULL";
		} else if (AutoDataOperator.XiaoYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "<";
		} else if (AutoDataOperator.XiaoYuDengYu == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "<";
		} else if (AutoDataOperator.FeiKong == AutoDataOperator.ofEnumByCode(operator)) {
			operator = "IS NOT NULL";
		} else {
			throw new BusinessException("暂不支持此种操作符" + operator);
		}
		// 2.返回转换后的sql操作符
		return operator;
	}

	@Method(desc = "更新保存可视化组件信息", logicStep = "1.校验组件汇总表字段合法性" +
			"2.更新组件汇总表数据" +
			"3.删除组件关联表信息" +
			"4.保存组件条件表" +
			"5.保存组件分组表" +
			"6.保存组件数据汇总信息表" +
			"7.保存横轴纵轴字段信息表" +
			"8.保存图表标题字体属性表数据" +
			"9.保存x/y轴配置信息表数据" +
			"10.保存x/y轴线配置信息/轴标签配置信息表数据" +
			"11.保存x,y轴标签字体属性(因为x轴，y轴字体属性一样，所以这里以x轴编号为字体属性对应的编号）" +
			"12.保存二维表样式信息表" +
			"13.保存图表配置信息表" +
			"14.保存文本标签信息表" +
			"15.保存图例信息表")
	@Param(name = "componentBean", desc = "可视化组件参数实体bean", range = "自定义无限制", isBean = true)
	@Param(name = "auto_comp_sum", desc = "组件汇总表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompConds", desc = "组件条件表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompGroups", desc = "组件分组表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompDataSums", desc = "组件数据汇总信息表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "titleFont", desc = "字体属性表对象（标题）", range = "与数据库表规则一致", isBean = true)
	@Param(name = "axisStyleFont", desc = "字体属性表对象（轴字体样式）", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoAxisInfos", desc = "轴配置信息表对象数组",
			range = "与数据库表规则一致,轴类型axis_type使用（IsFlag代码项，0:x轴，1:y轴）", isBean = true)
	@Param(name = "xAxisLabel", desc = "轴标签配置信息表对象(x轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "yAxisLabel", desc = "轴标签配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "xAxisLine", desc = "轴线配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "yAxisLine", desc = "轴线配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_table_info", desc = "组件数据汇总信息表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_chartsconfig", desc = "图表配置信息表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_label", desc = "图形文本标签表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_legend_info", desc = "组件图例信息表对象", range = "与数据库表规则一致", isBean = true)
	public void updateVisualComponentInfo(ComponentBean componentBean, Auto_comp_sum auto_comp_sum,
	                                      Auto_comp_cond[] autoCompConds, Auto_comp_group[] autoCompGroups,
	                                      Auto_comp_data_sum[] autoCompDataSums, Auto_font_info titleFont,
	                                      Auto_font_info axisStyleFont, Auto_axis_info[] autoAxisInfos,
	                                      Auto_axislabel_info xAxisLabel, Auto_axislabel_info yAxisLabel,
	                                      Auto_axisline_info xAxisLine, Auto_axisline_info yAxisLine,
	                                      Auto_table_info auto_table_info, Auto_chartsconfig auto_chartsconfig,
	                                      Auto_label auto_label, Auto_legend_info auto_legend_info) {
		// 1.校验组件汇总表字段合法性
		Validator.notNull(auto_comp_sum.getComponent_id(), "更新时组件ID不能为空");
		checkAutoCompSumFields(auto_comp_sum);
		auto_comp_sum.setLast_update_date(DateUtil.getSysDate());
		auto_comp_sum.setLast_update_time(DateUtil.getSysTime());
		auto_comp_sum.setUpdate_user(getUserId());
		String exe_sql = getSqlByCondition(componentBean, autoCompConds, autoCompGroups, autoCompDataSums);
		auto_comp_sum.setExe_sql(exe_sql);
		// 2.更新组件汇总表数据
		auto_comp_sum.update(Dbo.db());
		Validator.notBlank(componentBean.getFetch_name(), "取数名称不能为空");
		// 3.删除组件关联表信息
		deleteComponentAssociateTable(auto_comp_sum.getComponent_id());
		// 4.保存组件条件表
		if (autoCompConds != null && autoCompConds.length > 0) {
			for (Auto_comp_cond auto_comp_cond : autoCompConds) {
				Validator.notBlank(auto_comp_cond.getCond_en_column(), "条件英文字段不能为空");
				Validator.notBlank(auto_comp_cond.getCond_value(), "条件值不能为空");
				Validator.notBlank(auto_comp_cond.getOperator(), "操作符不能为空");
				auto_comp_cond.setCreate_date(DateUtil.getSysDate());
				auto_comp_cond.setCreate_time(DateUtil.getSysTime());
				auto_comp_cond.setCreate_user(getUserId());
				auto_comp_cond.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_cond.setComponent_cond_id(PrimayKeyGener.getNextId());
				auto_comp_cond.add(Dbo.db());
			}
		}
		// 5.保存组件分组表
		if (autoCompGroups != null && autoCompGroups.length > 0) {
			for (Auto_comp_group auto_comp_group : autoCompGroups) {
				Validator.notBlank(auto_comp_group.getColumn_name(), "字段名不能为空");
				auto_comp_group.setCreate_date(DateUtil.getSysDate());
				auto_comp_group.setCreate_time(DateUtil.getSysTime());
				auto_comp_group.setCreate_user(getUserId());
				auto_comp_group.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_group.setComponent_group_id(PrimayKeyGener.getNextId());
				auto_comp_group.add(Dbo.db());
			}
		}
		// 6.保存组件数据汇总信息表
		if (autoCompDataSums != null && autoCompDataSums.length > 0) {
			for (Auto_comp_data_sum auto_comp_data_sum : autoCompDataSums) {
				Validator.notBlank(auto_comp_data_sum.getColumn_name(), "字段名不能为空");
				Validator.notBlank(auto_comp_data_sum.getSummary_type(), "汇总类型不能为空");
				auto_comp_data_sum.setCreate_date(DateUtil.getSysDate());
				auto_comp_data_sum.setCreate_time(DateUtil.getSysTime());
				auto_comp_data_sum.setCreate_user(getUserId());
				auto_comp_data_sum.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_data_sum.setComp_data_sum_id(PrimayKeyGener.getNextId());
				auto_comp_data_sum.add(Dbo.db());
			}
		}
		// 7.保存横轴纵轴字段信息表
		// 字段显示类型 使用IsFlag代码项 0:x轴，1:y轴
		addAutoAxisColInfo(componentBean, auto_comp_sum);
		// 8.保存图表标题字体属性表数据
		Validator.notNull(titleFont.getFont_id(), "更新时标题字体信息id不能为空");
		titleFont.update(Dbo.db());
		// 9.保存x/y轴配置信息表数据
		for (Auto_axis_info auto_axis_info : autoAxisInfos) {
			Validator.notBlank(auto_axis_info.getAxis_type(), "轴类型不能为空");
			Validator.notNull(auto_axis_info.getAxis_id(), "更新时轴配置信息轴编号不能为空");
			Validator.notNull(xAxisLine.getAxisline_id(), "更新时x轴线编号不能为空");
			Validator.notNull(yAxisLine.getAxisline_id(), "更新时y轴线编号不能为空");
			Validator.notNull(xAxisLabel.getLable_id(), "更新时x轴标签编号不能为空");
			Validator.notNull(yAxisLabel.getLable_id(), "更新时y轴标签编号不能为空");
			Validator.notNull(yAxisLabel.getLable_id(), "更新时x,y轴字体信息ID不能为空");
			auto_axis_info.update(Dbo.db());
			// 10.保存x/y轴线配置信息/轴标签配置信息表数据
			if (IsFlag.Fou == IsFlag.ofEnumByCode(auto_axis_info.getAxis_type())) {
				// x轴线配置信息表数据
				xAxisLine.update(Dbo.db());
				// x轴标签配置信息表数据
				xAxisLabel.update(Dbo.db());
				// 11.保存x,y轴标签字体属性(因为x轴，y轴字体属性一样，所以这里以x轴编号为字体属性对应的编号）
				axisStyleFont.update(Dbo.db());
			} else {
				// y轴线配置信息表数据
				yAxisLine.update(Dbo.db());
				// y轴标签配置信息表数据
				yAxisLabel.update(Dbo.db());
			}
		}
		// 12.保存二维表样式信息表
		auto_table_info.update(Dbo.db());
		// 13.保存图表配置信息表
		auto_chartsconfig.update(Dbo.db());
		// 14.保存文本标签信息表
		auto_label.update(Dbo.db());
		// 15.保存图例信息表
		auto_legend_info.update(Dbo.db());
	}

	@Method(desc = "新增保存可视化组件信息", logicStep = "1.校验组件汇总表字段合法性" +
			"2.判断组件名称是否已存在" +
			"3.保存组件汇总表数据" +
			"4.保存组件条件表" +
			"5.保存组件分组表" +
			"6.保存组件数据汇总信息表" +
			"7.保存横轴纵轴字段信息表" +
			"8.保存图表标题字体属性表数据" +
			"9.保存x/y轴配置信息表数据" +
			"10.保存x/y轴线配置信息/轴标签配置信息表数据" +
			"11.保存x,y轴标签字体属性(因为x轴，y轴字体属性一样，所以这里以x轴编号为字体属性对应的编号）" +
			"12.保存二维表样式信息表" +
			"13.保存图表配置信息表" +
			"14.保存文本标签信息表" +
			"15.保存图例信息表")
	@Param(name = "componentBean", desc = "可视化组件参数实体bean", range = "自定义无限制", isBean = true)
	@Param(name = "auto_comp_sum", desc = "组件汇总表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompConds", desc = "组件条件表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompGroups", desc = "组件分组表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoCompDataSums", desc = "组件数据汇总信息表对象数组", range = "与数据库表规则一致", isBean = true)
	@Param(name = "titleFont", desc = "字体属性表对象（标题）", range = "与数据库表规则一致", isBean = true)
	@Param(name = "axisStyleFont", desc = "字体属性表对象（轴字体样式）", range = "与数据库表规则一致", isBean = true)
	@Param(name = "autoAxisInfos", desc = "轴配置信息表对象数组",
			range = "与数据库表规则一致,轴类型axis_type使用（IsFlag代码项，0:x轴，1:y轴）", isBean = true)
	@Param(name = "xAxisLabel", desc = "轴标签配置信息表对象(x轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "yAxisLabel", desc = "轴标签配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "xAxisLine", desc = "轴线配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "yAxisLine", desc = "轴线配置信息表对象(y轴)", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_table_info", desc = "组件数据汇总信息表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_chartsconfig", desc = "图表配置信息表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_label", desc = "图形文本标签表对象", range = "与数据库表规则一致", isBean = true)
	@Param(name = "auto_legend_info", desc = "组件图例信息表对象", range = "与数据库表规则一致", isBean = true)
	public void addVisualComponentInfo(ComponentBean componentBean, Auto_comp_sum auto_comp_sum,
	                                   Auto_comp_cond[] autoCompConds, Auto_comp_group[] autoCompGroups,
	                                   Auto_comp_data_sum[] autoCompDataSums, Auto_font_info titleFont,
	                                   Auto_font_info axisStyleFont, Auto_axis_info[] autoAxisInfos,
	                                   Auto_axislabel_info xAxisLabel, Auto_axislabel_info yAxisLabel,
	                                   Auto_axisline_info xAxisLine, Auto_axisline_info yAxisLine,
	                                   Auto_table_info auto_table_info, Auto_chartsconfig auto_chartsconfig,
	                                   Auto_label auto_label, Auto_legend_info auto_legend_info) {
		Validator.notBlank(componentBean.getFetch_name(), "取数名称不能为空");
		// 1.校验组件汇总表字段合法性
		checkAutoCompSumFields(auto_comp_sum);
		auto_comp_sum.setCreate_user(getUserId());
		auto_comp_sum.setSources_obj(componentBean.getFetch_name());
		auto_comp_sum.setComponent_id(PrimayKeyGener.getNextId());
		auto_comp_sum.setCreate_date(DateUtil.getSysDate());
		auto_comp_sum.setCreate_time(DateUtil.getSysTime());
		auto_comp_sum.setComponent_status(AutoFetchStatus.WanCheng.getCode());
		String exe_sql = getSqlByCondition(componentBean, autoCompConds, autoCompGroups, autoCompDataSums);
		auto_comp_sum.setExe_sql(exe_sql);
		Map<String, Object> chartShow = getChartShow(exe_sql, componentBean.getX_columns(),
				componentBean.getY_columns(), auto_comp_sum.getChart_type());
		auto_comp_sum.setComponent_buffer(auto_comp_sum.getComponent_buffer() == null ?
				JsonUtil.toJson(chartShow) : auto_comp_sum.getComponent_buffer());
		// 2.判断组件名称是否已存在
		isAutoCompSumExist(auto_comp_sum.getComponent_name());
		// 3.保存组件汇总表数据
		auto_comp_sum.add(Dbo.db());
		// 4.保存组件条件表
		if (autoCompConds != null && autoCompConds.length > 0) {
			for (Auto_comp_cond auto_comp_cond : autoCompConds) {
				Validator.notBlank(auto_comp_cond.getCond_en_column(), "条件英文字段不能为空");
				Validator.notBlank(auto_comp_cond.getCond_value(), "条件值不能为空");
				Validator.notBlank(auto_comp_cond.getOperator(), "操作符不能为空");
				auto_comp_cond.setCreate_date(DateUtil.getSysDate());
				auto_comp_cond.setCreate_time(DateUtil.getSysTime());
				auto_comp_cond.setCreate_user(getUserId());
				auto_comp_cond.setLast_update_date(DateUtil.getSysDate());
				auto_comp_cond.setLast_update_time(DateUtil.getSysTime());
				auto_comp_cond.setUpdate_user(getUserId());
				auto_comp_cond.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_cond.setComponent_cond_id(PrimayKeyGener.getNextId());
				auto_comp_cond.add(Dbo.db());
			}
		}
		// 5.保存组件分组表
		if (autoCompGroups != null && autoCompGroups.length > 0) {
			for (Auto_comp_group auto_comp_group : autoCompGroups) {
				Validator.notBlank(auto_comp_group.getColumn_name(), "字段名不能为空");
				auto_comp_group.setCreate_date(DateUtil.getSysDate());
				auto_comp_group.setCreate_time(DateUtil.getSysTime());
				auto_comp_group.setCreate_user(getUserId());
				auto_comp_group.setLast_update_date(DateUtil.getSysDate());
				auto_comp_group.setLast_update_time(DateUtil.getSysTime());
				auto_comp_group.setUpdate_user(getUserId());
				auto_comp_group.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_group.setComponent_group_id(PrimayKeyGener.getNextId());
				auto_comp_group.add(Dbo.db());
			}
		}
		// 6.保存组件数据汇总信息表
		if (autoCompDataSums != null && autoCompDataSums.length > 0) {
			for (Auto_comp_data_sum auto_comp_data_sum : autoCompDataSums) {
				Validator.notBlank(auto_comp_data_sum.getColumn_name(), "字段名不能为空");
				Validator.notBlank(auto_comp_data_sum.getSummary_type(), "汇总类型不能为空");
				auto_comp_data_sum.setCreate_date(DateUtil.getSysDate());
				auto_comp_data_sum.setCreate_time(DateUtil.getSysTime());
				auto_comp_data_sum.setCreate_user(getUserId());
				auto_comp_data_sum.setLast_update_date(DateUtil.getSysTime());
				auto_comp_data_sum.setLast_update_time(DateUtil.getSysTime());
				auto_comp_data_sum.setUpdate_user(getUserId());
				auto_comp_data_sum.setComponent_id(auto_comp_sum.getComponent_id());
				auto_comp_data_sum.setComp_data_sum_id(PrimayKeyGener.getNextId());
				auto_comp_data_sum.add(Dbo.db());
			}
		}
		// 7.保存横轴纵轴字段信息表
		addAutoAxisColInfo(componentBean, auto_comp_sum);
		// 8.保存图表标题字体属性表数据
		titleFont.setFont_id(PrimayKeyGener.getNextId());
		titleFont.setFont_corr_id(auto_comp_sum.getComponent_id());
		titleFont.setFont_corr_tname(Auto_comp_sum.TableName);
		titleFont.add(Dbo.db());
		// 9.保存x/y轴配置信息表数据
		for (Auto_axis_info auto_axis_info : autoAxisInfos) {
			Validator.notBlank(auto_axis_info.getAxis_type(), "轴类型不能为空");
			auto_axis_info.setAxis_id(PrimayKeyGener.getNextId());
			auto_axis_info.setComponent_id(auto_comp_sum.getComponent_id());
			auto_axis_info.add(Dbo.db());
			// 10.保存x/y轴线配置信息/轴标签配置信息表数据
			if (IsFlag.Fou == IsFlag.ofEnumByCode(auto_axis_info.getAxis_type())) {
				// x轴线配置信息表数据
				xAxisLine.setAxis_id(auto_axis_info.getAxis_id());
				xAxisLine.setAxisline_id(PrimayKeyGener.getNextId());
				xAxisLine.add(Dbo.db());
				// x轴标签配置信息表数据
				xAxisLabel.setLable_id(PrimayKeyGener.getNextId());
				xAxisLabel.setAxis_id(auto_axis_info.getAxis_id());
				xAxisLabel.add(Dbo.db());
				// 11.保存x,y轴标签字体属性(因为x轴，y轴字体属性一样，所以这里以x轴编号为字体属性对应的编号）
				axisStyleFont.setFont_id(PrimayKeyGener.getNextId());
				axisStyleFont.setFont_corr_id(auto_axis_info.getAxis_id());
				axisStyleFont.setFont_corr_tname(Auto_axis_info.TableName);
				axisStyleFont.add(Dbo.db());
			} else {
				// y轴线配置信息表数据
				yAxisLine.setAxis_id(auto_axis_info.getAxis_id());
				yAxisLine.setAxisline_id(PrimayKeyGener.getNextId());
				yAxisLine.add(Dbo.db());
				// y轴标签配置信息表数据
				yAxisLabel.setLable_id(PrimayKeyGener.getNextId());
				yAxisLabel.setAxis_id(auto_axis_info.getAxis_id());
				yAxisLabel.add(Dbo.db());
			}
		}
		// 12.保存二维表样式信息表
		auto_table_info.setConfig_id(PrimayKeyGener.getNextId());
		auto_table_info.setComponent_id(auto_comp_sum.getComponent_id());
		auto_table_info.add(Dbo.db());
		// 13.保存图表配置信息表
		auto_chartsconfig.setConfig_id(PrimayKeyGener.getNextId());
		auto_chartsconfig.setComponent_id(auto_comp_sum.getComponent_id());
		auto_chartsconfig.add(Dbo.db());
		// 14.保存文本标签信息表
		auto_label.setLable_id(PrimayKeyGener.getNextId());
		auto_label.setLabel_corr_tname(Auto_chartsconfig.TableName);
		auto_label.setLabel_corr_id(auto_comp_sum.getComponent_id());
		auto_label.add(Dbo.db());
		// 15.保存图例信息表
		auto_legend_info.setLegend_id(PrimayKeyGener.getNextId());
		auto_legend_info.setComponent_id(auto_comp_sum.getComponent_id());
		auto_legend_info.add(Dbo.db());
	}

	private void addAutoAxisColInfo(ComponentBean componentBean, Auto_comp_sum auto_comp_sum) {
		// 字段显示类型 使用IsFlag代码项 0:x轴，1:y轴
		String[] x_columns = componentBean.getX_columns();
		if (x_columns != null && x_columns.length > 0) {
			for (int i = 0; i < x_columns.length; i++) {
				Auto_axis_col_info auto_axis_col_info = new Auto_axis_col_info();
				auto_axis_col_info.setAxis_column_id(PrimayKeyGener.getNextId());
				auto_axis_col_info.setSerial_number(i);
				auto_axis_col_info.setColumn_name(x_columns[i]);
				// x轴
				auto_axis_col_info.setShow_type(IsFlag.Fou.getCode());
				auto_axis_col_info.setComponent_id(auto_comp_sum.getComponent_id());
				auto_axis_col_info.add(Dbo.db());
			}
		}
		String[] y_columns = componentBean.getY_columns();
		if (y_columns != null && y_columns.length > 0) {
			for (int i = 0; i < y_columns.length; i++) {
				Auto_axis_col_info auto_axis_col_info = new Auto_axis_col_info();
				auto_axis_col_info.setAxis_column_id(PrimayKeyGener.getNextId());
				auto_axis_col_info.setSerial_number(i);
				auto_axis_col_info.setColumn_name(y_columns[i]);
				auto_axis_col_info.setShow_type(IsFlag.Shi.getCode());
				auto_axis_col_info.setComponent_id(auto_comp_sum.getComponent_id());
				auto_axis_col_info.add(Dbo.db());
			}
		}
	}

	@Method(desc = "校验组件汇总表字段合法性", logicStep = "1.校验组件汇总表字段合法性")
	@Param(name = "auto_comp_sum", desc = "组件汇总表对象", range = "与数据库表规则一致", isBean = true)
	private void checkAutoCompSumFields(Auto_comp_sum auto_comp_sum) {
		// 1.校验组件汇总表字段合法性
		Validator.notBlank(auto_comp_sum.getChart_type(), "图标类型不能为空");
		Validator.notBlank(auto_comp_sum.getChart_theme(), "图形主题不能为空");
		Validator.notBlank(auto_comp_sum.getComponent_name(), "组件名称不能为空");
		Validator.notBlank(auto_comp_sum.getData_source(), "数据来源不能为空");
	}

	@Method(desc = "判断组件名称是否已存在", logicStep = "1.判断组件名称是否已存在")
	@Param(name = "component_name", desc = "组件名称", range = "无限制")
	private void isAutoCompSumExist(String component_name) {
		// 1.判断组件名称是否已存在
		if (Dbo.queryNumber(
				"SELECT count(1) FROM " + Auto_comp_sum.TableName + " WHERE component_name = ?",
				component_name)
				.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			throw new BusinessException("组件名称已存在");
		}
	}

	@Method(desc = "删除可视化组件信息", logicStep = "1.删除可视化组件汇总信息"
			+ "2.删除组件关联表信息")
	@Param(name = "component_id", desc = "组件ID", range = "创建组件时生成")
	public void deleteVisualComponent(long component_id) {
		// 1.删除可视化组件汇总信息
		DboExecute.deletesOrThrow("删除可视化组件信息失败",
				"DELETE FROM " + Auto_comp_sum.TableName + " WHERE component_id = ?", component_id);
		// 2.删除组件关联表信息
		deleteComponentAssociateTable(component_id);
	}

	@Method(desc = "删除组件关联表信息", logicStep = "1.根据组件id删除组件条件表" +
			"2.根据组件id删除组件分组表" +
			"3.根据组件id删除组件数据汇总信息表" +
			"4.根据组件id删除组件横轴纵轴字段信息表" +
			"5.根据组件id删除组件标题字体信息表" +
			"6.根据组件id删除轴标签字体属性信息表" +
			"7.根据组件id删除x,y轴标签配置信息表" +
			"8.根据组件id删除x,y轴线配置信息表" +
			"9.根据组件id删除轴配置信息表" +
			"10.根据组件id删除二维表样式信息表" +
			"11.根据组件id删除图表配置信息表" +
			"12.根据组件id删除文本标签信息表" +
			"13.根据组件id删除图例信息表")
	@Param(name = "component_id", desc = "组件ID", range = "创建组件时生成")
	private void deleteComponentAssociateTable(long component_id) {
		// 1.根据组件id删除组件条件表
		Dbo.execute("DELETE FROM " + Auto_comp_cond.TableName + " WHERE component_id = ?", component_id);
		// 2.根据组件id删除组件分组表
		Dbo.execute("DELETE FROM " + Auto_comp_group.TableName + " WHERE component_id = ?", component_id);
		// 3.根据组件id删除组件数据汇总信息表
		Dbo.execute("DELETE FROM " + Auto_comp_data_sum.TableName + " WHERE component_id = ?", component_id);
		// 4.根据组件id删除组件横轴纵轴字段信息表
		Dbo.execute("DELETE FROM " + Auto_axis_col_info.TableName + " WHERE component_id = ?", component_id);
		// 5.根据组件id删除组件标题字体信息表
		Dbo.execute("DELETE FROM " + Auto_font_info.TableName + " WHERE font_corr_id = ?", component_id);
		// 6.根据组件id删除轴标签字体属性信息表
		Dbo.execute("DELETE FROM " + Auto_font_info.TableName
				+ " WHERE font_corr_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
				+ " WHERE component_id = ?)", component_id);
		// 7.根据组件id删除x,y轴标签配置信息表
		Dbo.execute("DELETE FROM " + Auto_axislabel_info.TableName
				+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName
				+ " WHERE component_id = ?)", component_id);
		// 8.根据组件id删除x,y轴线配置信息表
		Dbo.execute("DELETE FROM " + Auto_axisline_info.TableName
				+ " WHERE axis_id IN (SELECT axis_id FROM " + Auto_axis_info.TableName +
				" WHERE component_id = ?)", component_id);
		// 9.根据组件id删除轴配置信息表
		Dbo.execute("DELETE FROM " + Auto_axis_info.TableName + " WHERE component_id = ?", component_id);
		// 10.根据组件id删除二维表样式信息表
		Dbo.execute("DELETE FROM " + Auto_table_info.TableName + " WHERE component_id = ?", component_id);
		// 11.根据组件id删除图表配置信息表
		Dbo.execute("DELETE FROM " + Auto_chartsconfig.TableName + " WHERE component_id = ?", component_id);
		// 12.根据组件id删除文本标签信息表
		Dbo.execute("DELETE FROM " + Auto_label.TableName + " WHERE label_corr_id = ?", component_id);
		// 13.根据组件id删除图例信息表
		Dbo.execute("DELETE FROM " + Auto_legend_info.TableName + " WHERE component_id = ?", component_id);
	}

	@Method(desc = "获取数据仪表盘首页数据", logicStep = "1.查询数据仪表板信息表数据")
	@Return(desc = "返回数据仪表板信息表数据", range = "无限制")
	public List<Map<String, Object>> getDataDashboardInfo() {
		// 1.查询数据仪表板信息表数据
		return Dbo.queryList("SELECT * FROM " + Auto_dashboard_info.TableName
				+ " WHERE user_id = ? order by create_date desc,create_time desc", getUserId());
	}

	@Method(desc = "根据仪表板id获取数据仪表板信息表数据", logicStep = "1.根据仪表板id获取数据仪表板信息表数据" +
			"2.获取仪表板边框组件信息表信息" +
			"3.查询关联信息表" +
			"4.查询仪表板标题表与字体表信息" +
			"5.查询仪表板分割线表信息" +
			"6.返回仪表盘信息")
	@Param(name = "dashboard_id", desc = "仪表板id", range = "新建仪表盘的时候生成")
	@Return(desc = "返回仪表盘信息", range = "无限制")
	public Map<String, Object> getDataDashboardInfoById(long dashboard_id) {
		// 1.根据仪表板id获取数据仪表板信息表数据
		Map<String, Object> dashboardInfo = Dbo.queryOneObject(
				"SELECT * FROM " + Auto_dashboard_info.TableName + " WHERE dashboard_id=?",
				dashboard_id);
		// 2.获取仪表板边框组件信息表信息
		List<Auto_frame_info> frameInfoList = Dbo.queryList(Auto_frame_info.class,
				"SELECT * FROM " + Auto_frame_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		List<Map<String, Object>> dashboardList = new ArrayList<>();
		if (!frameInfoList.isEmpty()) {
			for (Auto_frame_info auto_frame_info : frameInfoList) {
				Map<String, Object> frameMap = new HashMap<>();
				frameMap.put("x", auto_frame_info.getX_axis_coord());
				frameMap.put("y", auto_frame_info.getY_axis_coord());
				frameMap.put("w", auto_frame_info.getLength());
				frameMap.put("h", auto_frame_info.getWidth());
				frameMap.put("i", auto_frame_info.getSerial_number());
				frameMap.put("type", auto_frame_info.getFrame_id());
				frameMap.put("label", "2");
				frameMap.put("static", true);
				dashboardList.add(frameMap);
			}
			dashboardInfo.put("frameInfo", frameInfoList);
		}
		// 3.查询关联信息表
		List<Auto_asso_info> autoAssoInfoList = Dbo.queryList(Auto_asso_info.class,
				"SELECT * FROM " + Auto_asso_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		List<Auto_comp_sum> autoCompSumList = new ArrayList<>();
		if (!autoAssoInfoList.isEmpty()) {
			for (Auto_asso_info auto_asso_info : autoAssoInfoList) {
				Map<String, Object> object = new HashMap<>();
				Map<String, Object> componentMap = getVisualComponentInfoById(auto_asso_info.getComponent_id());
				Auto_comp_sum auto_comp_sum = JsonUtil.toObjectSafety(
						componentMap.get("compSum").toString(), Auto_comp_sum.class)
						.orElseThrow(() -> new BusinessException("转换实体失败"));
				object.put("x", auto_asso_info.getX_axis_coord());
				object.put("y", auto_asso_info.getY_axis_coord());
				object.put("w", auto_asso_info.getLength());
				object.put("h", auto_asso_info.getWidth());
				object.put("i", auto_asso_info.getSerial_number());
				object.put("type", auto_asso_info.getComponent_id());
				object.put("static", true);
				autoCompSumList.add(auto_comp_sum);
				dashboardInfo.put(String.valueOf(auto_asso_info.getComponent_id()), auto_comp_sum.getComponent_buffer());
				dashboardList.add(object);
			}
			dashboardInfo.put("autoCompSum", autoCompSumList);
		}
		// 4.查询仪表板标题表与字体表信息
		List<Map<String, Object>> labelAndFontList = Dbo.queryList(
				"SELECT * FROM " + Auto_label_info.TableName + " T1 LEFT JOIN "
						+ Auto_font_info.TableName + " T2 ON CAST(T1.label_id AS INT) = T2.font_corr_id" +
						" AND T2.font_corr_tname = ? WHERE dashboard_id = ?",
				Auto_label_info.TableName, dashboard_id);
		if (!labelAndFontList.isEmpty()) {
			for (Map<String, Object> map : labelAndFontList) {
				Auto_label_info auto_label_info = JsonUtil.toObjectSafety(map.toString(), Auto_label_info.class)
						.orElseThrow(() -> new BusinessException("实体转换失败"));
				Auto_font_info auto_font_info = JsonUtil.toObjectSafety(map.toString(), Auto_font_info.class)
						.orElseThrow(() -> new BusinessException("实体转换失败"));
				map.put("textStyle", auto_font_info);
				Map<String, Object> labelMap = new HashMap<>();
				labelMap.put("x", auto_label_info.getX_axis_coord());
				labelMap.put("y", auto_label_info.getY_axis_coord());
				labelMap.put("w", auto_label_info.getLength());
				labelMap.put("h", auto_label_info.getWidth());
				labelMap.put("i", auto_label_info.getSerial_number());
				labelMap.put("type", auto_label_info.getLabel_id());
				labelMap.put("label", "0");
				labelMap.put("static", true);
				Map<String, Object> contentColorSize = new HashMap<>();
				contentColorSize.put("label_content", auto_label_info.getLabel_content());
				contentColorSize.put("label_color", auto_label_info.getLabel_color());
				contentColorSize.put("label_size", auto_label_info.getLabel_size());
				dashboardInfo.put(auto_label_info.getLabel_id().toString(), contentColorSize);
				dashboardList.add(labelMap);
			}
			dashboardInfo.put("labelAndFont", labelAndFontList);
		}
		// 5.查询仪表板分割线表信息
		List<Auto_line_info> autoLineInfoList = Dbo.queryList(Auto_line_info.class,
				"SELECT * FROM " + Auto_line_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		if (!autoLineInfoList.isEmpty()) {
			for (Auto_line_info auto_line_info : autoLineInfoList) {
				Map<String, Object> lineMap = new HashMap<>();
				lineMap.put("x", auto_line_info.getX_axis_coord());
				lineMap.put("y", auto_line_info.getY_axis_coord());
				lineMap.put("w", auto_line_info.getLine_length());
				lineMap.put("h", auto_line_info.getLine_weight());
				lineMap.put("i", auto_line_info.getSerial_number());
				lineMap.put("type", auto_line_info.getLine_id());
				lineMap.put("label", "1");
				lineMap.put("static", true);
				JSONObject contentColorType = new JSONObject();
				contentColorType.put("line_color", auto_line_info.getLine_color());
				contentColorType.put("line_type", auto_line_info.getLine_type());
				dashboardInfo.put(auto_line_info.getLine_id().toString(), contentColorType);
				dashboardList.add(lineMap);
			}
			dashboardInfo.put("autoLineInfo", autoLineInfoList);
		}
		dashboardInfo.put("layout", dashboardList);
		// 6.返回仪表盘信息
		return dashboardInfo;
	}

	@Method(desc = "在仪表板上显示组件", logicStep = "1.根据可视化组件ID查看可视化组件信息" +
			"2.根据组件id查询组件缓存" +
			"3.返回在仪表盘上展示的组件信息")
	@Param(name = "autoCompSums", desc = "组件汇总表实体对象数组", range = "与数据库对应规则一致", isBean = true)
	@Return(desc = "返回在仪表盘上展示的组件信息", range = "无限制")
	public Map<String, Object> showComponentOnDashboard(Auto_comp_sum[] autoCompSums) {
		Map<String, Object> componentOnDashBoard = new HashMap<>();
		if (autoCompSums != null && autoCompSums.length > 0) {
			List<Map<String, Object>> componentList = new ArrayList<>();
			for (int i = 0; i < autoCompSums.length; i++) {
				Auto_comp_sum autoCompSum = autoCompSums[i];
				Map<String, Object> map = new HashMap<>();
				// 1.根据可视化组件ID查看可视化组件信息
				Map<String, Object> componentInfo = getVisualComponentInfoById(autoCompSum.getComponent_id());
				// 2.根据组件id查询组件缓存
				Auto_comp_sum auto_comp_sum = JsonUtil.toObjectSafety(
						componentInfo.get("compSum").toString(), Auto_comp_sum.class)
						.orElseThrow(() -> new BusinessException("转换实体失败"));
				map.put("x", (i % 3) * 33);
				map.put("y", (i / 3) * 30);
				map.put("w", 33);
				map.put("h", 30);
				map.put("i", autoCompSum.getComponent_id());
				map.put("static", true);
				map.put("type", autoCompSum.getComponent_id() + "");
				componentOnDashBoard.put(String.valueOf(autoCompSum.getComponent_id()),
						auto_comp_sum.getComponent_buffer());
				componentList.add(map);
			}
			componentOnDashBoard.put("layout", componentList);
		}
		// 3.返回在仪表盘上展示的组件信息
		return componentOnDashBoard;
	}

	@Method(desc = "保存仪表盘信息", logicStep = "1.校验仪表盘表字段合法性" +
			"2.判断仪表板名称是否已存在" +
			"3.新增仪表盘" +
			"4.新增仪表盘布局信息")
	@Param(name = "auto_dashboard_info", desc = "仪表板信息表实体对象", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFontInfos", desc = "字体属性信息表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLabelInfos", desc = "仪表板标题表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLineInfos", desc = "仪表板分割线表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFrameInfos", desc = "仪表板边框组件信息表数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "layout", desc = "仪表盘布局对象", range = "无限制")
	public void saveDataDashboardInfo(Auto_dashboard_info auto_dashboard_info, Auto_font_info[] autoFontInfos,
	                                  Auto_label_info[] autoLabelInfos, Auto_line_info[] autoLineInfos,
	                                  Auto_frame_info[] autoFrameInfos, String layout) {
		// 1.校验仪表盘表字段合法性
		Validator.notBlank(auto_dashboard_info.getDashboard_name(), "仪表盘名称不能为空");
		// 2.判断仪表板名称是否已存在
		isDashboardNameExist(auto_dashboard_info);
		auto_dashboard_info.setDashboard_id(PrimayKeyGener.getNextId());
		auto_dashboard_info.setUser_id(getUserId());
		auto_dashboard_info.setCreate_date(DateUtil.getSysDate());
		auto_dashboard_info.setCreate_time(DateUtil.getSysTime());
		// 使用IsFlag代码项，0:未发布，1:已发布
		auto_dashboard_info.setDashboard_status(IsFlag.Fou.getCode());
		// 3.新增仪表盘
		auto_dashboard_info.add(Dbo.db());
		// 4.新增仪表盘布局信息
		addLayoutInfo(auto_dashboard_info, autoFontInfos, autoLabelInfos, autoLineInfos, autoFrameInfos, layout);
	}

	@Method(desc = "更新仪表盘信息", logicStep = "1.校验仪表盘表字段合法性" +
			"2.判断仪表板名称是否已存在" +
			"3.更新仪表盘信息" +
			"4.删除仪表盘相关表信息" +
			"5.新增仪表盘布局信息")
	@Param(name = "auto_dashboard_info", desc = "仪表板信息表实体对象", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFontInfos", desc = "字体属性信息表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLabelInfos", desc = "仪表板标题表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLineInfos", desc = "仪表板分割线表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFrameInfos", desc = "仪表板边框组件信息表数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "layout", desc = "仪表盘布局对象", range = "无限制")
	public void updateDataDashboardInfo(Auto_dashboard_info auto_dashboard_info, Auto_font_info[] autoFontInfos,
	                                    Auto_label_info[] autoLabelInfos, Auto_line_info[] autoLineInfos,
	                                    Auto_frame_info[] autoFrameInfos, String layout) {
		// 1.校验仪表盘表字段合法性
		Validator.notBlank(auto_dashboard_info.getDashboard_name(), "仪表盘名称不能为空");
		Validator.notNull(auto_dashboard_info.getDashboard_id(), "更新时仪表盘ID不能为空");
		// 2.判断仪表板名称是否已存在
		isDashboardNameExist(auto_dashboard_info);
		auto_dashboard_info.setUpdate_user(getUserId());
		auto_dashboard_info.setLast_update_date(DateUtil.getSysDate());
		auto_dashboard_info.setLast_update_time(DateUtil.getSysTime());
		// 3.更新仪表盘信息
		auto_dashboard_info.update(Dbo.db());
		// 4.删除仪表盘相关表信息
		deleteDashboardAssoTable(auto_dashboard_info.getDashboard_id());
		// 5.新增仪表盘布局信息
		addLayoutInfo(auto_dashboard_info, autoFontInfos, autoLabelInfos, autoLineInfos, autoFrameInfos, layout);
	}

	@Method(desc = "新增仪表盘布局信息", logicStep = "1.解析仪表盘布局信息" +
			"2.新增报表组件信息" +
			"3.新增标题组件信息" +
			"4.新增分割线组件信息" +
			"5.新增边框组件信息")
	@Param(name = "auto_dashboard_info", desc = "仪表板信息表实体对象", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFontInfos", desc = "字体属性信息表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLabelInfos", desc = "仪表板标题表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoLineInfos", desc = "仪表板分割线表对象数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "autoFrameInfos", desc = "仪表板边框组件信息表数组", range = "与数据库对应表规则一致", isBean = true)
	@Param(name = "layout", desc = "仪表盘布局对象", range = "无限制")
	private void addLayoutInfo(Auto_dashboard_info auto_dashboard_info, Auto_font_info[] autoFontInfos,
	                           Auto_label_info[] autoLabelInfos, Auto_line_info[] autoLineInfos,
	                           Auto_frame_info[] autoFrameInfos, String layout) {
		// 1.解析仪表盘布局信息
		JSONArray layoutArray = JSONArray.parseArray(layout);
		for (int i = 0; i < layoutArray.size(); i++) {
			String label = layoutArray.getJSONObject(i).getString("label");
			Long primayKey = PrimayKeyGener.getNextId();
			String operId = PrimayKeyGener.getOperId();
			if (null == label) {
				// 2.新增报表组件信息
				Auto_asso_info asso_info = new Auto_asso_info();
				asso_info.setDashboard_id(auto_dashboard_info.getDashboard_id());
				asso_info.setComponent_id(layoutArray.getJSONObject(i).getString("type"));
				asso_info.setAsso_info_id(primayKey);
				asso_info.setLength(layoutArray.getJSONObject(i).getIntValue("w"));
				asso_info.setWidth(layoutArray.getJSONObject(i).getIntValue("h"));
				asso_info.setX_axis_coord(layoutArray.getJSONObject(i).getIntValue("x"));
				asso_info.setY_axis_coord(layoutArray.getJSONObject(i).getIntValue("y"));
				asso_info.setSerial_number(operId);
				asso_info.add(Dbo.db());
			} else if ("0".equals(label)) {
				// 3.新增标题组件信息
				Auto_label_info auto_label_info = autoLabelInfos[i];
				auto_label_info.setLabel_id(primayKey);
				auto_label_info.setDashboard_id(auto_dashboard_info.getDashboard_id());
				auto_label_info.setLength(layoutArray.getJSONObject(i).getIntValue("w"));
				auto_label_info.setWidth(layoutArray.getJSONObject(i).getIntValue("h"));
				auto_label_info.setX_axis_coord(layoutArray.getJSONObject(i).getIntValue("x"));
				auto_label_info.setY_axis_coord(layoutArray.getJSONObject(i).getIntValue("y"));
				auto_label_info.setSerial_number(operId);
				auto_label_info.add(Dbo.db());
				Auto_font_info auto_font_info = autoFontInfos[i];
				auto_font_info.setFont_id(PrimayKeyGener.getNextId());
				auto_font_info.setFont_corr_tname(Auto_label_info.TableName);
				auto_font_info.setFont_corr_id(auto_label_info.getLabel_id());
				auto_font_info.add(Dbo.db());
			} else if ("1".equals(label)) {
				// 4.新增分割线组件信息
				Auto_line_info auto_line_info = autoLineInfos[i];
				auto_line_info.setLine_id(primayKey);
				auto_line_info.setDashboard_id(auto_dashboard_info.getDashboard_id());
				auto_line_info.setLine_length(layoutArray.getJSONObject(i).getLongValue("w"));
				auto_line_info.setLine_weight(layoutArray.getJSONObject(i).getLongValue("h"));
				auto_line_info.setX_axis_coord(layoutArray.getJSONObject(i).getIntValue("x"));
				auto_line_info.setY_axis_coord(layoutArray.getJSONObject(i).getIntValue("y"));
				auto_line_info.setSerial_number(primayKey);
				auto_line_info.add(Dbo.db());
			} else if ("2".equals(label)) {
				// 5.新增边框组件信息
				Auto_frame_info auto_frame_info = autoFrameInfos[i];
				auto_frame_info.setFrame_id(primayKey);
				auto_frame_info.setDashboard_id(auto_dashboard_info.getDashboard_id());
				auto_frame_info.setLength(layoutArray.getJSONObject(i).getLongValue("w"));
				auto_frame_info.setWidth(layoutArray.getJSONObject(i).getLongValue("h"));
				auto_frame_info.setX_axis_coord(layoutArray.getJSONObject(i).getIntValue("x"));
				auto_frame_info.setY_axis_coord(layoutArray.getJSONObject(i).getIntValue("y"));
				auto_frame_info.setSerial_number(operId);
				auto_frame_info.add(Dbo.db());
			}
		}
	}

	@Method(desc = "发布仪表盘信息", logicStep = "")
	@Param(name = "dashboard_id", desc = "仪表板id", range = "新建仪表盘的时候生成")
	@Param(name = "dashboard_name", desc = "仪表板名称", range = "新建仪表盘的时候生成")
	public void releaseDashboardInfo(long dashboard_id, String dashboard_name) {
		// 1.更新仪表盘盘发布状态
		DboExecute.updatesOrThrow("更新仪表盘盘发布状态失败",
				"update " + Auto_dashboard_info.TableName + " set dashboard_status=? where dashboard_id=?",
				IsFlag.Shi.getCode(), dashboard_id);
		// 2.发布仪表盘
		String interface_code = Base64.getEncoder().encodeToString(String.valueOf(dashboard_id).getBytes());
		Map<String, Object> interfaceMap = Dbo.queryOneObject(
				"SELECT * FROM " + Interface_info.TableName + " WHERE interface_code = ?",
				interface_code);
		if (interfaceMap.isEmpty()) {
			Interface_info interface_info = new Interface_info();
			interface_info.setInterface_id(PrimayKeyGener.getNextId());
			interface_info.setUser_id(getUserId());
			interface_info.setInterface_code(interface_code);
			interface_info.setInterface_name(dashboard_name);
			interface_info.setInterface_state(InterfaceState.QiYong.getCode());
			interface_info.setInterface_type(InterfaceType.BaoBiaoLei.getCode());
			interface_info.setUrl(Constant.DASHBOARDINTERFACENAME);
			interface_info.add(Dbo.db());
		} else {
			Interface_info interface_info = JsonUtil.toObjectSafety(interfaceMap.toString(), Interface_info.class)
					.orElseThrow(() -> new BusinessException("转换接口信息表实体对象失败"));
			interface_info.setInterface_name(dashboard_name);
			try {
				interface_info.update(Dbo.db());
			} catch (Exception e) {
				if (!(e instanceof ProjectTableEntity.EntityDealZeroException)) {
					logger.error(e);
					throw new BusinessException("更新接口信息失败" + e.getMessage());
				}
			}
		}
	}

	@Method(desc = "删除仪表盘", logicStep = "1.判断仪表盘是否已发布已发布不能删除" +
			"2.删除仪表盘信息" +
			"3.删除仪表盘相关表信息")
	@Param(name = "dashboard_id", desc = "仪表板id", range = "新建仪表盘的时候生成")
	public void deleteDashboardInfo(long dashboard_id) {
		// 1.判断仪表盘是否已发布已发布不能删除
		if (Dbo.queryNumber("select count(*) from " + Auto_dashboard_info.TableName
						+ " where dashboard_id=? and dashboard_status=?",
				dashboard_id, IsFlag.Shi.getCode())
				.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			throw new BusinessException("仪表盘已发布不能删除");
		}
		// 2.删除仪表盘信息
		DboExecute.deletesOrThrow("删除仪表盘信息失败：" + dashboard_id,
				"DELETE FROM" + Auto_dashboard_info.TableName + "WHERE dashboard_id = ? ", dashboard_id);
		// 3.删除仪表盘相关表信息
		deleteDashboardAssoTable(dashboard_id);
	}

	@Method(desc = "删除仪表盘相关表信息", logicStep = "1.删除仪表板组件关联信息表信息" +
			"2.删除仪表板标题表信息" +
			"3.删除仪表板分割线表信息" +
			"4.删除仪表板边框组件信息表信息" +
			"5.删除字体属性信息表信息")
	@Param(name = "dashboard_id", desc = "仪表板id", range = "新建仪表盘的时候生成")
	private void deleteDashboardAssoTable(long dashboard_id) {
		// 1.删除仪表板组件关联信息表信息
		Dbo.execute("DELETE FROM " + Auto_asso_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		// 2.删除仪表板标题表信息
		Dbo.execute("DELETE FROM " + Auto_label_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		// 3.删除仪表板分割线表信息
		Dbo.execute("DELETE FROM " + Auto_line_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		// 4.删除仪表板边框组件信息表信息
		Dbo.execute("DELETE FROM " + Auto_frame_info.TableName + " WHERE dashboard_id = ?", dashboard_id);
		// 5.删除字体属性信息表信息
		Dbo.execute("DELETE FROM " + Auto_font_info.TableName + " WHERE font_corr_tname = ?"
				+ " AND font_corr_id IN (SELECT CAST(label_id AS INT) FROM " + Auto_label_info.TableName
				+ " WHERE dashboard_id = ?)", Auto_font_info.TableName, dashboard_id);
	}

	private void isDashboardNameExist(Auto_dashboard_info auto_dashboard_info) {
		if (Dbo.queryNumber(
				"SELECT count(1) FROM " + Auto_dashboard_info.TableName + " WHERE dashboard_name = ?",
				auto_dashboard_info.getDashboard_name())
				.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			throw new BusinessException("仪表板名称已存在");
		}
	}
}