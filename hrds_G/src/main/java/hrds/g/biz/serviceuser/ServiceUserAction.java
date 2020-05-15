package hrds.g.biz.serviceuser;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.RequestUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.entity.Interface_use;
import hrds.commons.entity.Sysreg_parameter_info;
import hrds.commons.entity.Table_use_info;
import hrds.commons.utils.PropertyParaValue;
import hrds.g.biz.commons.InterfaceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "接口/数据表信息", author = "dhw", createdate = "2020/3/30 13:39")
public class ServiceUserAction extends BaseAction {

	@Method(desc = "查询接口信息", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.判断接口名称是否为空，不为空加条件查询" +
			"3.查询接口使用信息" +
			"4.返回查询接口使用信息")
	@Param(name = "interface_name", desc = "接口名称", range = "无限制", nullable = true)
	@Return(desc = "返回查询接口使用信息", range = "无限制")
	public Result searchInterfaceInfo(String interface_name) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		SqlOperator.Assembler assembler = SqlOperator.Assembler.newInstance();
		assembler.clean();
		assembler.addSql("SELECT interface_use_id,interface_name,use_valid_date,start_use_date,url,user_id FROM "
				+ Interface_use.TableName + " WHERE user_id = ?").addParam(getUserId());
		// 2.判断接口名称是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(interface_name)) {
			assembler.addLikeParam("interface_name", "%" + interface_name + "%");
		}
		assembler.addSql(" order by interface_use_id");
		// 3.查询接口使用信息
		Result interfaceResult = Dbo.queryResult(assembler.sql(), assembler.params());
		for (int i = 0; i < interfaceResult.getRowCount(); i++) {
			interfaceResult.setObject(i, "start_use_date", DateUtil.parseStr2DateWith8Char(
					interfaceResult.getString(i, "start_use_date")));
			interfaceResult.setObject(i, "use_valid_date", DateUtil.parseStr2DateWith8Char(
					interfaceResult.getString(i,
							"use_valid_date")));
		}
		// 4.返回查询接口使用信息
		return interfaceResult;
	}

	@Method(desc = "根据表名查询数据表信息", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.判断系统登记表名是否为空，不为空加条件查询" +
			"4.查询表使用信息并返回")
	@Param(name = "sysreg_name", desc = "系统登记表名", range = "无限制", nullable = true)
	@Return(desc = "返回查询表使用信息", range = "无限制")
	public Result searchDataTableInfo(String sysreg_name) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		SqlOperator.Assembler assembler = SqlOperator.Assembler.newInstance();
		assembler.clean();
		assembler.addSql("SELECT sysreg_name,original_name,use_id FROM " + Table_use_info.TableName
				+ " WHERE user_id = ?").addParam(getUserId());
		// 2.判断系统登记表名是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(sysreg_name)) {
			// 忽略大小写查询
			assembler.addSql(" and sysreg_name ilike ?").addParam("%" + sysreg_name.toUpperCase() + "%");
		}
		// 3.查询表使用信息并返回
		return Dbo.queryResult(assembler.sql(), assembler.params());
	}

	@Method(desc = "根据表使用ID查询当前用户对应的列信息",
			logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
					"2.返回根据表使用ID查询当前用户对应的列信息" +
					"3.封装成list<map>格式数据返回")
	@Param(name = "use_id", desc = "接口使用ID", range = "新增接口使用信息时生成")
	@Return(desc = "返回根据表使用ID查询当前用户对应的列信息", range = "无限制")
	public List<Map<String, String>> searchColumnInfoById(long use_id) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.返回根据表使用ID查询当前用户对应的列信息
		List<String> columnList = Dbo.queryOneColumnList("SELECT table_column_name FROM "
				+ Sysreg_parameter_info.TableName + " WHERE use_id = ? and user_id=?", use_id, getUserId());
		// 3.封装成list<map>格式数据返回
		return InterfaceUtils.getMaps(columnList);
	}

	@Method(desc = "获取当前用户请求ip端口",
			logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
					"2.封装当前用户请求ip端口" +
					"3.返回当前用户请求ip端口")
	@Return(desc = "返回当前用户请求ip端口", range = "无限制")
	public Map<String, Object> getIpAndPort() {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		Map<String, Object> useMap = new HashMap<>();
		// 2.封装当前用户请求ip端口
		useMap.put("ipAndPort", PropertyParaValue.getString("hyren_host", "127.0.0.1") + ":"
				+ RequestUtil.getRequest().getLocalPort());
		// 3.返回当前用户请求ip端口
		return useMap;
	}

}
