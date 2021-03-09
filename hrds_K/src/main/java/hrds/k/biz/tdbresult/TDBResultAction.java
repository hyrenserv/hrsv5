package hrds.k.biz.tdbresult;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.k.biz.tdb.bean.AdaptRelationBean;
import hrds.k.biz.tdb.bean.NodeRelationBean;
import hrds.k.biz.tdbresult.bean.SearchFKAnalysisBean;
import hrds.k.biz.tdbresult.bean.SearchJoinPKAnalysisBean;
import hrds.k.biz.tdbresult.bean.SearchTableFuncDepResultBean;
import hrds.k.biz.tdbresult.echarts.bar.BarUtil;
import hrds.k.biz.tdbresult.echarts.graph.GraphUtil;
import hrds.k.biz.tdbresult.echarts.pie.PieUtil;
import hrds.k.biz.tdbresult.echarts.tree.DataConvertedEcharsTreeNode;
import hrds.k.biz.tdbresult.echarts.tree.EcharsTreeNode;
import hrds.k.biz.tdbresult.echarts.tree.NodeDataConvertedTreeList;
import hrds.k.biz.tdbresult.echarts.tree.query.JoinPKAnalysisQuery;
import hrds.k.biz.tdbresult.echarts.tree.query.TableFuncDepAnalysisQuery;
import hrds.k.biz.utils.Neo4jUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "表数据对标结果(TableDataBenchmarkingResult)", author = "zxz,BY-HLL", createdate = "2021/2/2 0000 上午 09:49")
public class TDBResultAction extends BaseAction {

	@Method(desc = "获取数据对标分析的表主键信息", logicStep = "获取数据对标分析的表主键信息")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "表主键信息", range = "表主键信息")
	@Deprecated
	public Map<String, Object> getPageTablePkData(String table_code, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT " +
			"  table_code, " +
			"  col_code, " +
			"  col_type, " +
			"  case col_nullable when '0' then '否' else '是' end as col_nullable, " +
			"  case col_pk when '0' then '否' else '是' end as col_pk, " +
			" row_number() over (partition BY table_code ORDER BY col_num) as col_num " +
			" FROM " + Dbm_mmm_field_info_tab.TableName);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", "%" + table_code + "%", "WHERE");
		}
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> tablePkData = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> tablePkDataMap = new HashMap<>();
		tablePkDataMap.put("tablePkData", tablePkData);
		tablePkDataMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return tablePkDataMap;
	}

	@Method(desc = "获取数据对标分析,表主键分析表名列表", logicStep = "获取数据对标分析,表主键分析表名列表")
	@Return(desc = "表主键分析表名列表", range = "表主键分析表名列表")
	public List<String> getPKAnalysisTableCodeList() {
		return Dbo.queryOneColumnList("SELECT table_code FROM " + Dbm_mmm_field_info_tab.TableName + " GROUP BY table_code");
	}

	@Method(desc = "获取数据对标分析的表主键信息", logicStep = "获取数据对标分析的表主键信息")
	@Param(name = "table_code", desc = "表名", range = "可为空")
	@Return(desc = "表主键信息", range = "表主键信息")
	public List<Map<String, Object>> searchPKAnalysisResult(String table_code) {
		//数据校验
		if (StringUtil.isBlank(table_code)) {
			throw new BusinessException("主键分析结果,检索的表名不能为空!");
		}
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT " +
			"  table_code, " +
			"  col_code, " +
			"  col_type, " +
			"  case col_nullable when '0' then '否' else '是' end as col_nullable, " +
			"  case col_pk when '0' then '否' else '是' end as col_pk, " +
			" row_number() over (partition BY table_code ORDER BY col_num) as col_num " +
			" FROM " + Dbm_mmm_field_info_tab.TableName);
		asmSql.addLikeParam("table_code", table_code, "WHERE");
		// 4.返回查询信息
		return Dbo.queryList(asmSql.sql(), asmSql.params());
	}

	@Method(desc = "获取数据对标分析的表联合主键信息", logicStep = "获取数据对标分析的表联合主键信息")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "表联合主键信息", range = "表联合主键信息")
	@Deprecated
	public Map<String, Object> getPageTableJoinPkData(String table_code, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" row_number() over(partition BY table_code ORDER BY group_code) col_num," +
			" table_code," +
			" string_agg(col_code,',') AS join_pk_col_code," +
			" group_code" +
			" FROM " + Dbm_joint_pk_tab.TableName);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", "%" + table_code + "%", "WHERE");
		}
		asmSql.addSql(" GROUP BY table_code,group_code");
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> tableJoinPkData = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> tableJoinPkDataMap = new HashMap<>();
		tableJoinPkDataMap.put("tableJoinPkData", tableJoinPkData);
		tableJoinPkDataMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return tableJoinPkDataMap;
	}

	@Method(desc = "获取数据对标分析的表联合主键及表函数表名信息list", logicStep = "获取数据对标分析的表联合主键及表函数表名信息list")
	@Return(desc = "表联合主键及表函数表名信息list", range = "表联合主键及表函数表名信息list")
	public List<String> getJoinPKAnalysisAndTableFuncDepTableCodeList() {
		//获取联合主键和表函数分析的表名list,取合集
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT DISTINCT(table_code) FROM " + Dbm_joint_pk_tab.TableName);
		asmSql.addSql(" UNION");
		asmSql.addSql("SELECT DISTINCT(table_code) FROM " + Dbm_function_dependency_tab.TableName);
		return Dbo.queryOneColumnList(asmSql.sql(), asmSql.params());
	}

	@Method(desc = "获取数据对标分析的表联合主键信息", logicStep = "获取数据对标分析的表联合主键信息")
	@Param(name = "searchJoinPKAnalysisBean", desc = "自定义搜索bean", range = "SearchJoinPKAnalysisBean")
	@Return(desc = "表联合主键信息", range = "表联合主键信息")
	public EcharsTreeNode getJoinPKAnalysisResult(SearchJoinPKAnalysisBean searchJoinPKAnalysisBean) {
		List<EcharsTreeNode> echarsTreeNodes = new ArrayList<>();
		//获取根据搜索条件获取需要检索的表名
		List<Object> table_code_s =
			JoinPKAnalysisQuery.getJoinPKAnalysisTableCode(Dbo.db(), searchJoinPKAnalysisBean.getTable_name());
		if (!table_code_s.isEmpty()) {
			//转化表信息为Echars tree 节点
			List<Map<String, Object>> dataList = new ArrayList<>();
			dataList.add(DataConvertedEcharsTreeNode.conversionRootNode((String) table_code_s.get(0)));
			//获取并转化,子节点信息
			List<Map<String, Object>> joinPkDataByTableCode
				= JoinPKAnalysisQuery.getJoinPkDataByTableCode(Dbo.db(), (String) table_code_s.get(0));
			dataList.addAll(DataConvertedEcharsTreeNode.conversionJointPKInfos(joinPkDataByTableCode,
				(String) table_code_s.get(0)));
			//设置并转化为 echars tree 需要的数据
			echarsTreeNodes = NodeDataConvertedTreeList.echarsTreeNodesConversionTreeInfo(dataList);
		}
		if (echarsTreeNodes.isEmpty()) {
			throw new BusinessException("未找到表联合主键信息! 表名: " + searchJoinPKAnalysisBean.getTable_name());
		}
		return echarsTreeNodes.get(0);
	}

	@Method(desc = "获取数据对标分析的表函数依赖的信息", logicStep = "获取数据对标分析的表函数依赖的信息")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "表函数依赖的信息", range = "表函数依赖的信息")
	@Deprecated
	public Map<String, Object> getPageTableFuncDepData(String table_code, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" row_number() over(partition BY table_code ORDER BY LENGTH(right_columns)-LENGTH(REPLACE" +
			" (right_columns,',','')) DESC,LENGTH(left_columns)-LENGTH(REPLACE(left_columns,',','')) ) AS" +
			" row_num," +
			" table_code," +
			" left_columns," +
			" right_columns" +
			" FROM" +
			" (" +
			" SELECT" +
			" string_agg(right_columns,',') AS right_columns," +
			" table_code," +
			" left_columns" +
			" FROM " + Dbm_function_dependency_tab.TableName);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", "%" + table_code + "%", "WHERE");
		}
		asmSql.addSql(" GROUP BY" +
			" table_code," +
			" left_columns) temp_dep");
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> tableFuncDepData = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> tableFuncDepDataMap = new HashMap<>();
		tableFuncDepDataMap.put("tableFuncDepData", tableFuncDepData);
		tableFuncDepDataMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return tableFuncDepDataMap;
	}

	@Method(desc = "获取数据对标分析的表函数依赖的信息", logicStep = "获取数据对标分析的表函数依赖的信息")
	@Param(name = "searchTableFuncDepResultBean", desc = "自定义搜索bean", range = "SearchTableFuncDepResultBean")
	@Return(desc = "表函数依赖的信息", range = "表函数依赖的信息")
	public EcharsTreeNode getTableFuncDepResult(SearchTableFuncDepResultBean searchTableFuncDepResultBean) {
		//获取根据搜索条件获取需要检索的表名
		List<Object> table_code_s =
			TableFuncDepAnalysisQuery.getTableFuncDepTableCode(Dbo.db(), searchTableFuncDepResultBean.getTable_name());
		List<EcharsTreeNode> echarsTreeNodes = new ArrayList<>();
		if (!table_code_s.isEmpty()) {
			//转化表信息为Echars tree 节点
			List<Map<String, Object>> dataList = new ArrayList<>();
			dataList.add(DataConvertedEcharsTreeNode.conversionRootNode((String) table_code_s.get(0)));
			//获取并转化,子节点信息
			List<Map<String, Object>> tableFuncDepDataByTableCode
				= TableFuncDepAnalysisQuery.getTableFuncDepDataByTableCode(Dbo.db(), (String) table_code_s.get(0));
			dataList.addAll(DataConvertedEcharsTreeNode.conversionTableFuncInfos(tableFuncDepDataByTableCode,
				(String) table_code_s.get(0)));
			//设置并转化为 echars tree 需要的数据
			echarsTreeNodes = NodeDataConvertedTreeList.echarsTreeNodesConversionTreeInfo(dataList);
		}
		if (echarsTreeNodes.isEmpty()) {
			throw new BusinessException("未找到表函数依赖信息! 表名: " + searchTableFuncDepResultBean.getTable_name());
		}
		return echarsTreeNodes.get(0);
	}

	@Method(desc = "获取数据对标分析的表字段外键信息", logicStep = "获取数据对标分析的表字段外键信息")
	@Param(name = "fk_table_code", desc = "主表表名", range = "可为空", nullable = true)
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "表字段外键信息", range = "表字段外键信息")
	@Deprecated
	public Map<String, Object> getPageTableFkData(String fk_table_code, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" fk_table_code ," +
			" fk_col_code ," +
			" table_code," +
			" col_code," +
			" row_number() over(partition BY fk_table_code ORDER BY col_code) row_num" +
			" FROM " + Dbm_fk_info_tab.TableName);
		if (StringUtil.isNotBlank(fk_table_code)) {
			asmSql.addLikeParam("fk_table_code", "%" + fk_table_code + "%", "WHERE");
		}
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> tableFkData = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> tableFkDataMap = new HashMap<>();
		tableFkDataMap.put("tableFkData", tableFkData);
		tableFkDataMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return tableFkDataMap;
	}

	@Method(desc = "获取数据对标分析的表字段外键信息", logicStep = "获取数据对标分析的表字段外键信息")
	@Param(name = "searchFKAnalysisBean", desc = "自定义bean,搜索外键分析结果", range = "SearchFKAnalysisBean")
	@Return(desc = "表字段外键信息", range = "表字段外键信息")
	public Map<String, Object> getFKAnalysisResults(SearchFKAnalysisBean searchFKAnalysisBean) {
		//根据搜索条件,检索结果
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" fk_table_code ," +
			" fk_col_code ," +
			" table_code," +
			" col_code," +
			" row_number() over(partition BY fk_table_code ORDER BY col_code) row_num" +
			" FROM " + Dbm_fk_info_tab.TableName + " WHERE id !=''");
		if (StringUtil.isNotBlank(searchFKAnalysisBean.getTable_name())) {
			asmSql.addLikeParam("upper(fk_table_code)",
				"%" + searchFKAnalysisBean.getTable_name().toUpperCase() + "%");
		}
		if (StringUtil.isNotBlank(searchFKAnalysisBean.getTable_field_name())) {
			asmSql.addLikeParam("fk_col_code", "%" + searchFKAnalysisBean.getFk_table_field_name() + "%");
		}
		if (StringUtil.isNotBlank(searchFKAnalysisBean.getFk_table_name())) {
			asmSql.addLikeParam("table_code", "%" + searchFKAnalysisBean.getFk_table_name() + "%");
		}
		if (StringUtil.isNotBlank(searchFKAnalysisBean.getFk_table_field_name())) {
			asmSql.addLikeParam("col_code", "%" + searchFKAnalysisBean.getFk_table_field_name() + "%");
		}
		// 2.查询作业定义信息
		List<Map<String, Object>> tableFkDatas = Dbo.queryList(asmSql.sql(), asmSql.params());
		//3.处理查询结果,返回e-chars关系图格式数据
		// 获取节点分类
		Map<String, Integer> category_info_map = GraphUtil.extractCategoryNode(tableFkDatas);
		// 获取节点
		List<Map<String, Object>> node_info_map_s = GraphUtil.extractRelationNode(tableFkDatas, category_info_map);
		// 获取节点关系
		List<Map<String, Object>> link_map_s = GraphUtil.extractLink(tableFkDatas);
		// 转化节点信息为图数据信息
		return GraphUtil.dataConvertedGraphData(category_info_map, node_info_map_s, link_map_s);
	}


	@Method(desc = "获取数据对标字段相等类别分析结果", logicStep = "获取数据对标字段相等类别分析结果")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "字段相等类别分析结果", range = "字段相等类别分析结果")
	@Deprecated
	public Map<String, Object> getPageFieldSameResult(String table_code, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" dim_order," +
			" table_code," +
			" col_code," +
			" category_same," +
			" rel_type" +
			" FROM " + Dbm_field_same_result.TableName
		);
		if (StringUtil.isNotBlank(table_code)) {
			if (table_code.contains("=")) {
				List<String> split = StringUtil.split(table_code, "=");
				if ("class".equals(split.get(0).trim()) && StringUtil.isNotBlank(split.get(1))) {
					asmSql.addSql("WHERE category_same = " + split.get(1));
				} else if (StringUtil.isNotBlank(split.get(1))) {
					asmSql.addLikeParam("table_code", "%" + split.get(1) + "%", "WHERE");
				}
			} else {
				asmSql.addLikeParam("table_code", "%" + table_code + "%", "WHERE");
			}
		}
		asmSql.addSql(" ORDER BY" +
			" category_same," +
			" dim_order");
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> fieldSameResult = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> fieldSameResultMap = new HashMap<>();
		fieldSameResultMap.put("fieldSameResult", fieldSameResult);
		fieldSameResultMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return fieldSameResultMap;
	}

	@Method(desc = "根据检索条件检索字段相等类别分析统计结果", logicStep = "根据检索条件检索字段相等类别分析统计结果")
	@Param(name = "table_code", desc = "表名", range = "表名称搜索", nullable = true)
	@Param(name = "category_same", desc = "分类编号", range = "不可为空", nullable = true)
	@Return(desc = "字段相等类别分析统计结果", range = "字段相等类别分析统计结果")
	public Map<String, Object> getFieldSameStatisticalResult(String table_code, String category_same) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT category_same,count(*) FROM " + Dbm_field_same_result.TableName);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", "%" + table_code + "%", "WHERE");
			if (StringUtil.isNotBlank(category_same)) {
				asmSql.addLikeParam("cast(category_same as varchar(100))", "%" + category_same + "%", "AND");
			}
		} else if (StringUtil.isNotBlank(category_same)) {
			asmSql.addLikeParam("cast(category_same as varchar(100))", "%" + category_same + "%", "WHERE");
		}
		asmSql.addSql(" GROUP BY category_same");
		asmSql.addSql(" ORDER BY category_same");
		//获取字段相等类别分析结果
		List<Map<String, Object>> fieldSameStatisticalResult = Dbo.queryList(asmSql.sql(), asmSql.params());
		//处理数据格式,处理成echars的pie图所需的格式
		return PieUtil.extractLegendDataAndSeriesData(fieldSameStatisticalResult);
	}

	@Method(desc = "根据检索条件检索字段相等类别分析统计结果", logicStep = "根据检索条件检索字段相等类别分析统计结果")
	@Param(name = "table_code", desc = "表名", range = "表名称搜索", nullable = true)
	@Param(name = "category_same", desc = "分类编号", range = "不可为空")
	@Param(name = "currPage", desc = "分页查询当前页", range = "大于0的正整数", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页查询每页显示记录数", range = "大于0的正整数", valueIfNull = "10")
	@Return(desc = "字段相等类别分析统计结果", range = "字段相等类别分析统计结果")
	public Map<String, Object> getFieldSameResultByCategorysame(String table_code, int category_same, int currPage, int pageSize) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT dim_order,table_code,col_code,category_same,rel_type FROM " + Dbm_field_same_result.TableName);
		asmSql.addSql(" WHERE category_same=?").addParam(category_same);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", "%" + table_code + "%", "AND");
		}
		asmSql.addSql(" ORDER BY category_same,dim_order");
		// 2.分页查询作业定义信息
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<Map<String, Object>> fieldSameResult = Dbo.queryPagedList(page, asmSql.sql(), asmSql.params());
		// 3.创建存放分页查询作业定义信息、分页查询总记录数集合并封装数据
		Map<String, Object> fieldSameResultMap = new HashMap<>();
		fieldSameResultMap.put("fieldSameResult", fieldSameResult);
		fieldSameResultMap.put("totalSize", page.getTotalSize());
		// 4.返回分页查询信息
		return fieldSameResultMap;
	}

	@Method(desc = "获取数据对标字段特征分析结果", logicStep = "获取数据对标字段特征分析结果")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Return(desc = "字段特征分析结果", range = "字段特征分析结果")
	public List<Map<String, Object>> getColumnFeatureAnalysisResult(String table_code) {
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT" +
			" table_code," +
			" col_code," +
			" col_records," +
			" col_distinct," +
			" max_len," +
			" min_len," +
			" avg_len," +
			" skew_len," +
			" kurt_len," +
			" median_len," +
			" var_len," +
			"case when has_chinese = '0' then '否' else '是' end as has_chinese," +
			"case when tech_cate = '1' then '日期' when tech_cate = '2' then '金额' when tech_cate = '3' then " +
			"'码值' when tech_cate = '4' then '数值' when tech_cate = '5' then '费率' else 'UNK' end as tech_cate" +
			" FROM " + Dbm_feature_tab.TableName
		);
		if (StringUtil.isNotBlank(table_code)) {
			asmSql.addLikeParam("table_code", table_code, "WHERE");
		}
		asmSql.addSql(" ORDER BY table_code");
		// 4.返回分页查询信息
		return Dbo.queryList(asmSql.sql(), asmSql.params());
	}

	@Method(desc = "获取数据对标字段特征分析表名list", logicStep = "获取数据对标字段特征分析表名list")
	@Return(desc = "数据对标字段特征分析表名list", range = "数据对标字段特征分析表名list")
	public List<String> getColumnFeatureAnalysisTableCodeList() {
		return Dbo.queryOneColumnList("SELECT table_code FROM " + Dbm_feature_tab.TableName + "  GROUP BY table_code");
	}

	@Method(desc = "获取数据对标字段特征分析结果", logicStep = "获取数据对标字段特征分析结果")
	@Param(name = "table_code", desc = "表名", range = "可为空", nullable = true)
	@Return(desc = "字段特征分析结果", range = "字段特征分析结果")
	public Map<String, Object> searchColumnFeatureAnalysisResult(String table_code) {
		//数据校验
		Validator.notBlank(table_code, "检所表名不能为空!");
		// 1.拼接sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT table_code,col_code,col_records,col_distinct,max_len,min_len,avg_len,skew_len,kurt_len," +
			" median_len,var_len, case when has_chinese = '0' then '否' else '是' end as has_chinese," +
			" case when tech_cate = '1' then '日期' when tech_cate = '2' then '金额' when tech_cate = '3' then " +
			" '码值' when tech_cate = '4' then '数值' when tech_cate = '5' then '费率' else 'UNK' end as tech_cate" +
			" FROM " + Dbm_feature_tab.TableName);
		asmSql.addLikeParam("table_code", table_code, "WHERE");
		asmSql.addSql(" ORDER BY table_code");
		// 2.查询字段特征分析结果信息
		List<Map<String, Object>> columnFeatureAnalysisResult = Dbo.queryList(asmSql.sql(), asmSql.params());
		// 3.创建存放查询结果信息 封装数据
		Map<String, Object> columnFeatureAnalysisResultMap = new HashMap<>();
		columnFeatureAnalysisResultMap.put("table_code", table_code);
		columnFeatureAnalysisResultMap.putAll(BarUtil.extractStatisticsChartData(columnFeatureAnalysisResult));
		return columnFeatureAnalysisResultMap;
	}

	@Method(desc = "搜索关系列", logicStep = "1.参数合法性验证" +
		"2.根据变的属性获取所有节点关系数据" +
		"3.数据格式转换")
	@Param(name = "relationship", desc = "页面传参边的属性", range = "FK、FD、EQUALS、SAME、BDF")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回数据格式转换后的节点关系数据", range = "无限制")
	public Map<String, Object> searchColumnOfRelation(String relationship, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(relationship, "页面传参边的属性不能为空");
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.根据变的属性获取所有节点关系数据
			List<NodeRelationBean> nodeRelationBeans = getNodeRelationBeans(relationship, limitNum, example);
			// 3.返回数据格式转换后的节点关系数据
			return GraphUtil.convertToGraphData(nodeRelationBeans);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "LPA社区发现算法", logicStep = "1.参数合法性验证" +
		"2.查询lpa算法结果数据" +
		"3.根据边的属性获取所有节点关系数据" +
		"4.LPA算法数据格式转换")
	@Param(name = "relationship", desc = "页面传参边的属性", range = "FK、FD、EQUALS、SAME、BDF")
	@Param(name = "iterations", desc = "算法迭代次数", range = "不能为空")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的lpa社区发现算法数据", range = "无限制")
	public Map<String, Object> searchLabelPropagation(String relationship, int iterations, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(relationship, "页面传参边的属性不能为空");
		Validator.notNull(iterations, "算法迭代次数不能为空");
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.查询lpa算法结果数据
			List<Map<String, Object>> mapList = example.searchLabelPropagation(relationship, iterations, limitNum);
			// 3.根据边的属性获取所有节点关系数据
			List<NodeRelationBean> nodeRelationBeans = getNodeRelationBeans(relationship, limitNum, example);
			// 4.LPA算法数据格式转换
			return GraphUtil.lpaOrLouvainConversion(nodeRelationBeans, mapList, IsFlag.Shi.getCode());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

	}

	private List<NodeRelationBean> getNodeRelationBeans(String relationship, String limitNum, Neo4jUtils example) {
		List<NodeRelationBean> nodeRelationBeans;
		switch (relationship) {
			case "FK":
				nodeRelationBeans = example.searchColumnOfFkRelation(limitNum);
				break;
			case "FD":
				nodeRelationBeans = example.searchColumnOfFdRelation(limitNum);
				break;
			case "EQUALS":
				nodeRelationBeans = example.searchColumnOfEqualsRelation(limitNum);
				break;
			case "SAME":
				nodeRelationBeans = example.searchColumnOfSameRelation(limitNum);
				break;
			case "BFD":
				nodeRelationBeans = example.searchColumnOfBfdRelation(limitNum);
				break;
			default:
				throw new BusinessException("暂不支持边的属性为" + relationship);
		}
		return nodeRelationBeans;
	}

	@Method(desc = "LOUVAIN社区发现算法", logicStep = "1.参数合法性验证" +
		"2.查询louvain算法结果数据" +
		"3.根据变的属性获取所有节点关系数据" +
		"4.Louvain算法数据格式转换")
	@Param(name = "relationship", desc = "页面传参边的属性", range = "FK、FD、EQUALS、SAME、BDF")
	@Param(name = "iterations", desc = "算法迭代次数", range = "不能为空")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的louvain社区发现算法数据", range = "无限制")
	public Map<String, Object> searchLouVain(String relationship, int iterations, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(relationship, "页面传参边的属性不能为空");
		Validator.notNull(iterations, "算法迭代次数不能为空");
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.查询louvain算法结果数据
			List<Map<String, Object>> mapList = example.searchLouVain(relationship, iterations, limitNum);
			// 3.根据变的属性获取所有节点关系数据
			List<NodeRelationBean> nodeRelationBeans = getNodeRelationBeans(relationship, limitNum, example);
			// 4.Louvain算法数据格式转换
			return GraphUtil.lpaOrLouvainConversion(nodeRelationBeans, mapList, IsFlag.Fou.getCode());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "求全部最短路径", logicStep = "1.参数合法性验证" +
		"2.获取全部最短路径neo4j结果数据" +
		"3.最长最短数据格式转换")
	@Param(name = "columnNodeName1", desc = "第一个字段的节点名称", range = "不为空")
	@Param(name = "columnNodeName2", desc = "第二个字段的节点名称", range = "不为空")
	@Param(name = "level", desc = "最多找多少层", range = "不能为空")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的全部最短路径数据", range = "无限制")
	public Map<String, Object> searchAllShortPath(String columnNodeName1, String columnNodeName2,
	                                              int level, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(columnNodeName1, "第一个字段的节点名称不能为空");
		Validator.notBlank(columnNodeName2, "第二个字段的节点名称不能为空");
		Validator.notNull(level, "最多找多少层不能为空");
		if (columnNodeName1.equals(columnNodeName2)) {
			throw new BusinessException("第一个节点名称与第二个节点名称重复");
		}
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.获取全部最短路径neo4j结果数据
			List<AdaptRelationBean> adaptRelationBeans =
				example.searchAllShortPath(columnNodeName1, columnNodeName2, level, limitNum);
			// 3.最长最短数据格式转换
			return GraphUtil.longestAndShortestDataConversion(adaptRelationBeans, columnNodeName1,
				columnNodeName2);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "求最长路径", logicStep = "1.参数合法性验证" +
		"2.获取最长路径neo4j结果数据" +
		"3.最长路径数据格式转换")
	@Param(name = "columnNodeName1", desc = "第一个字段的节点名称", range = "不为空")
	@Param(name = "columnNodeName2", desc = "第二个字段的节点名称", range = "不为空")
	@Param(name = "level", desc = "最多找多少层", range = "这个值不能太大，不然查询超级慢")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的最长路径数据", range = "无限制")
	public Map<String, Object> searchLongestPath(String columnNodeName1, String columnNodeName2,
	                                             int level, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(columnNodeName1, "第一个字段的节点名称不能为空");
		Validator.notBlank(columnNodeName2, "第二个字段的节点名称不能为空");
		Validator.notNull(level, "最多找多少层不能为空");
		if (columnNodeName1.equals(columnNodeName2)) {
			throw new BusinessException("第一个节点名称与第二个节点名称重复");
		}
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.获取最长路径neo4j结果数据
			List<AdaptRelationBean> adaptRelationBeans =
				example.searchLongestPath(columnNodeName1, columnNodeName2, level, limitNum);
			// 3.最长路径数据格式转换
			return GraphUtil.longestAndShortestDataConversion(adaptRelationBeans, columnNodeName1,
				columnNodeName2);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "求远近邻关系", logicStep = "1.参数合法性验证" +
		"2.获取远近邻关系neo4j结果数据" +
		"3.远近邻关系数据格式转换")
	@Param(name = "columnNodeName", desc = "字段的节点名称", range = "不为空")
	@Param(name = "level", desc = "最多找多少层", range = "这个值不能太大，不然查询超级慢")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的远近邻关系数据", range = "无限制")
	public Map<String, Object> searchNeighbors(String columnNodeName, int level, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(columnNodeName, "字段节点名称不能为空");
		Validator.notNull(level, "最多找多少层不能为空");
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.获取远近邻关系neo4j结果数据
			List<NodeRelationBean> nodeRelationBeans = example.searchNeighbors(columnNodeName, level, limitNum);
			// 3.远近邻关系数据格式转换
			return GraphUtil.convertToEchartsTree(nodeRelationBeans);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "三角关系展示", logicStep = "1.参数合法性验证" +
		"2.获取三角关系neo4j结果数据" +
		"3.三角关系数据格式转换")
	@Param(name = "relationship", desc = "为页面传参边的属性", range = "FK、FD、EQUALS、SAME、BDF")
	@Param(name = "limitNum", desc = "查询前多少条", range = "可为空，为空则表示查询全部数据", nullable = true)
	@Return(desc = "返回转换格式后的三角关系数据", range = "无限制")
	public Map<String, Object> searchTriangleRelation(String relationship, String limitNum) {
		// 1.参数合法性验证
		Validator.notBlank(relationship, "字段节点名称不能为空");
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 2.获取三角关系neo4j结果数据
			List<AdaptRelationBean> adaptRelationBeans = example.searchTriangleRelation(relationship, limitNum);
			// 3.三角关系数据格式转换
			return GraphUtil.convertToTriangle(adaptRelationBeans);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Method(desc = "查询所有字段节点信息", logicStep = "1.查询所有字段节点信息")
	@Return(desc = "返回所有字段节点信息", range = "无限制")
	public List<String> searchAllColumnOfNodes() {
		try (Neo4jUtils example = new Neo4jUtils()) {
			// 1.查询所有字段节点信息
			Map<Long, Map<String, Object>> map = example.searchAllColumnOfNodes(null);
			List<String> columnList = new ArrayList<>();
			for (Map.Entry<Long, Map<String, Object>> entry : map.entrySet()) {
				if (!columnList.contains(entry.getValue().get("name").toString())) {
					columnList.add(entry.getValue().get("name").toString());
				}
			}
			return columnList;
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}
}
