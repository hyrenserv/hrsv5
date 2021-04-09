package hrds.b.biz.fulltextsearch;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.RequestUtil;
import hrds.b.biz.fulltextsearch.tools.EssaySimilar;
import hrds.b.biz.fulltextsearch.tools.PictureSearch;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.AgentType;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.hadoop.solr.ISolrOperator;
import hrds.commons.hadoop.solr.SolrFactory;
import hrds.commons.utils.PathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@DocClass(desc = "全文检索数据查询", author = "BY-HLL", createdate = "2019/10/8 0008 下午 03:10")
public class FullTextSearchAction extends BaseAction {

	private static final Logger logger = LogManager.getLogger();

	@Method(desc = "获取用户收藏的文件列表,返回结果默认显示最近9条收藏",
		logicStep = "数据可访问权限处理方式: 根据 User_fav 表的 user_id做权限校验" +
			"1.如果查询条数小于1条则显示默认9条,查询条数大于99条则显示99条,否则取传入的查询条数" +
			"2.返回当前登录的用户已经收藏的文件列表的List结果集"
	)
	@Param(name = "queryNum", desc = "查询显示条数", range = "int类型值,1-99,默认为9", valueIfNull = "9")
	@Return(desc = "用户收藏文件列表的结果集List", range = "无限制")
	public List<Map<String, Object>> getCollectFiles(int queryNum) {
		//数据可访问权限处理方式: 根据 User_fav 表的 user_id做权限校验
		//1.如果查询条数小于1条则显示默认9条,查询条数大于99条则显示99条,否则取传入的查询条数
		queryNum = Math.max(1, queryNum);
		queryNum = Math.min(queryNum, 99);
		//2.获取当前用户已经收藏的文件列表
		//数据可访问权限处理方式: 根据 User_fav 的 user_id 做权限检查
		return Dbo.queryList("SELECT uf.*,sfa.file_suffix,sfa.hbase_name,sfa.file_type FROM " + User_fav.TableName + " uf" +
				" JOIN " + Source_file_attribute.TableName + " sfa ON sfa.file_id = uf.file_id" +
				" WHERE user_id = ? AND fav_flag = ?" +
				" ORDER BY fav_id DESC LIMIT ?",
			getUserId(), IsFlag.Shi.getCode(), queryNum
		);
	}

	@Method(desc = "全文检索方法",
		logicStep = "数据可访问权限处理方式: 全文检索不做权限校验" +
			"1.根据类型获取对应搜索结果集" +
			"1-1.全文检索返回结果集" +
			"1-2.以图搜图返回结果集" +
			"1-3.文章相似度返回结果集" +
			"1-4.文件名搜索返回结果集" +
			"2.结果集处理"
	)
	@Param(name = "queryKeyword", desc = "查询内容", range = "String类型值,无输入限制", nullable = true)
	@Param(name = "searchType", desc = "检索类型", range = "String类型值,检索类型（全文检索：fullTextSearch," +
		"searchByMap：以图搜图,articleSimilarityQuery：文章相似度,fileNameSearch：文件名搜索）",
		valueIfNull = "fullTextSearch")
	@Param(name = "start", desc = "查询开始行", range = "int类型值,1-99,默认为9", valueIfNull = "1")
	@Param(name = "currPage", desc = "分页", range = "int类型值,默认为1", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页大小", range = "int类型值,默认为10", valueIfNull = "10")
	@Param(name = "imageAddress", desc = "以图搜图上传文件路径", range = "String类型,上传的文件全路径地址", nullable = true)
	@Param(name = "docAddress", desc = "文章上传文件路径", range = "String类型值,上传的文件全路径地址", nullable = true)
	@Param(name = "fileName", desc = "搜索文件名", range = "String类型值,待检索的文件名", nullable = true)
	@Param(name = "similarityRate", desc = "相似度百分率", range = "String类型值,1%-100%", nullable = true)
	@Param(name = "searchWay", desc = "是否返回文本信息", range = "String类型值,(1：是,0：否)", valueIfNull = "0")
	@Return(desc = "用户收藏文件列表的结果集List", range = "无限制")
	public Map<String, Object> fullTextSearch(String queryKeyword, String searchType, int start, int currPage,
	                                          int pageSize, String imageAddress, String docAddress, String fileName,
	                                          String similarityRate, String searchWay) {
		Map<String, Object> ftsMap = new HashMap<>();
		//1.根据类型获取对应搜索结果集
		int totalSize = 0;
		//高亮词列表
		List<String> analysis = new ArrayList<>();
		Result result;
		switch (searchType) {
			//1-1.全文检索返回结果集
			case "fullTextSearch":
				//检索条件校验
				Validator.notNull(queryKeyword, "检索内容不能为空");
				queryKeyword = getParticipleQuery(queryKeyword.trim());
				result = getFinalResult(queryKeyword, start, pageSize, currPage);
				analysis = Arrays.asList(queryKeyword.substring(1, queryKeyword.length() - 1).split("\" OR \""));
				if (!result.isEmpty()) {
					// 返回总数据记录
					totalSize = result.getIntDefaultZero(0, "totalSize");
				}
				break;
			//1-2.以图搜图返回结果集
			case "searchByMap":
				//TODO 以图搜图的方法未实现
				PictureSearch picSearch = new PictureSearch();
				result = picSearch.pictureSearchResult(imageAddress);
				break;
			//1-3.文章相似度返回结果集
			case "articleSimilarityQuery":
				IsFlag is_return_text = IsFlag.ofEnumByCode(searchWay);
				result = getWZXSDResult(docAddress, similarityRate, is_return_text, currPage, pageSize);
				if (!result.isEmpty()) {
					// 返回总数据记录
					totalSize = result.getIntDefaultZero(0, "totalSize");
				}
				break;
			//1-4.文件名搜索返回结果集
			case "fileNameSearch":
				result = getWJMSSResult(fileName, currPage, pageSize);
				analysis.add(fileName);
				break;
			default:
				throw new BusinessException("searchType is not matching...");
		}
		//2.结果集处理
		List<Map<String, Object>> rList = new ArrayList<>();
		if (!result.isEmpty()) {
			List<Map<String, Object>> rsList = result.toList();
			//请求 action 地址
			String requestUrl = RequestUtil.getRequest().getRequestURL().toString();
			String action = requestUrl.substring(0, requestUrl.lastIndexOf('/') + 1);
			for (Map<String, Object> stringObjectMap : rsList) {
				String fileId = (String) stringObjectMap.get("file_id");
				String originalName = (String) stringObjectMap.get("original_name");
				String downloadPath = action +
					"downloadFileSDO.do?view_down_file_id=" + fileId +
					"&view_down_file_name=" + originalName;
				stringObjectMap.put("downloadPath", downloadPath);
				stringObjectMap.put("collectTime",
					DateUtil.parseStr2DateWith8Char((String) stringObjectMap.get("storage_date")) + " " +
						DateUtil.parseStr2TimeWith6Char((String) stringObjectMap.get("storage_time")));
				rList.add(stringObjectMap);
			}
			ftsMap.put("analysis", analysis);
			ftsMap.put("result", rList);
			ftsMap.put("collectType", AgentType.WenJianXiTong.getCode());
			ftsMap.put("totalSize", totalSize);
		}
		return ftsMap;
	}

	@Method(desc = "全文检索方法",
		logicStep = "数据可访问权限处理方式: 全文检索不做权限校验" +
			"1.初始化待返回的结果集" +
			"2.查询结果结果集为空直接返回" +
			"3.获取结果集中的 agent_id 和 file_id" +
			"3-1.agentIdList去重" +
			"4.全文库检索" +
			"4-1.创建查询sql" +
			"4-2.获取查询sql返回结果" +
			"5.设置返回结果集内容"
	)
	@Param(name = "queryConditions", desc = "查询条件", range = "String类型值,无输入限制")
	@Param(name = "start", desc = "查询开始行", range = "int类型值,1-99,默认为9", valueIfNull = "9")
	@Param(name = "currPage", desc = "分页", range = "int类型值,默认为1", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页大小", range = "int类型值,默认为10", valueIfNull = "10")
	@Return(desc = "通过查询结果查出更多关联信息的结果集", range = "无限制")
	private Result getFinalResult(String queryConditions, int start, int pageSize, int currPage) {
		//1.初始化待返回的结果集
		Result resultProcessing = queryResultProcessing(queryConditions, start, pageSize);
		//2.查询结果的结果集为空直接返回
		if (resultProcessing.isEmpty()) {
			return new Result();
		}
		//3.获取结果集中的 agent_id 和 file_id
		List<Long> agentIdList = new ArrayList<>();
		List<String> fileIdList = new ArrayList<>();
		for (int i = 0; i < resultProcessing.getRowCount(); i++) {
			long agentId = resultProcessing.getLong(i, "agent_id");
			String fileId = resultProcessing.getString(i, "file_id");
			agentIdList.add(agentId);
			fileIdList.add(fileId);
		}
		//3-1.agentIdList去重
		Object[] newAgentIdArr = agentIdList.stream().distinct().toArray();
		Object[] newFileIdArr = fileIdList.toArray();
		//4.全文检索
		Search_info searchInfo = new Search_info();
		searchInfo.setWord_name(queryConditions);
		//源文件属性表
		Source_file_attribute sourceFileAttribute = new Source_file_attribute();
		sourceFileAttribute.setCollect_type(AgentType.WenJianXiTong.getCode());
		//4-1.创建查询sql
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		//查询非结构化采集的sql
		asmSql.addSql(" SELECT sfa.source_path,sfa.file_suffix,sfa.file_id,sfa.storage_time,sfa.storage_date," +
			" sfa.original_update_date,sfa.hbase_name, sfa.original_update_time,sfa.file_md5,sfa.original_name," +
			" sfa.file_size,sfa.seqencing,collect_type,sfa.collect_set_id,sfa.source_id,sfa.agent_id," +
			" fcs.fcs_name,datasource_name,agent_name,si.si_count,uf.fav_id,uf.fav_flag,sfa.file_type" +
			" FROM " + Data_source.TableName + " ds" +
			" JOIN " + Agent_info.TableName + " gi ON gi.SOURCE_ID = ds.SOURCE_ID" +
			" JOIN " + File_collect_set.TableName + " fcs ON fcs.agent_id = gi.agent_id" +
			" JOIN " + Source_file_attribute.TableName + " sfa ON sfa.SOURCE_ID = ds.SOURCE_ID and sfa.AGENT_ID = gi.AGENT_ID" +
			" and sfa.COLLECT_SET_ID = fcs.FCS_ID");
		asmSql.addSql(" LEFT JOIN " + Search_info.TableName + " si on word_name=?").addParam(searchInfo.getWord_name());
		asmSql.addSql(" LEFT JOIN " + User_fav.TableName + " uf ON sfa.file_id=uf.file_id");
		asmSql.addSql(" where collect_type=? ").addParam(sourceFileAttribute.getCollect_type());
		asmSql.addORParam("sfa.AGENT_ID", newAgentIdArr);
		asmSql.addORParam("sfa.file_id", newFileIdArr);
		//查询数据库采集和DB文件采集的sql
//		asmSql.addSql(" UNION");
//		asmSql.addSql(" SELECT sfa.source_path,sfa.file_suffix,sfa.file_id,sfa.storage_time,sfa.storage_date," +
//			" sfa.original_update_date,sfa.hbase_name, sfa.original_update_time,sfa.file_md5,sfa.original_name," +
//			" sfa.file_size,sfa.seqencing,collect_type,sfa.collect_set_id,sfa.source_id,sfa.agent_id," +
//			" db.task_name as fcs_name,datasource_name,agent_name,0 as si_count,uf.fav_id,uf.fav_flag" +
//			" FROM data_source ds JOIN  agent_info gi ON ds.SOURCE_ID = gi.SOURCE_ID JOIN database_set db" +
//			" ON db.agent_id = gi.agent_id JOIN source_file_attribute sfa ON sfa.SOURCE_ID = ds.SOURCE_ID" +
//			" and sfa.AGENT_ID = gi.AGENT_ID and sfa.COLLECT_SET_ID = db.DATABASE_ID LEFT JOIN user_fav uf" +
//			" ON sfa.file_id = uf.file_id where collect_type in(?,?)");
//		asmSql.addParam(AgentType.ShuJuKu.getCode());
//		asmSql.addParam(AgentType.DBWenJian.getCode());
//		asmSql.addSql(" AND sfa.AGENT_ID in (");
//		agentIdListToSql(newAgentIdList, asmSql, agentInfo);
//		asmSql.addSql(") AND sfa.file_id in (");
//		fileIdListToSql(fileIdList, asmSql, sourceFileAttribute);
//		asmSql.addSql(")");
		asmSql.addSql(" ORDER BY si_count asc");
		//4-2.获取查询sql返回结果
		Result resultSet = Dbo.queryPagedResult(new DefaultPageImpl(currPage, pageSize), asmSql.sql(), asmSql.params());
		//5.设置返回结果集内容
		for (int i = 0; i < resultSet.getRowCount(); i++) {
			String fileId = resultSet.getString(i, "file_id");
			for (int j = 0; j < resultProcessing.getRowCount(); j++) {
				String fileIdSolr = resultProcessing.getString(j, "file_id");
				if (fileId.equals(fileIdSolr)) {
					resultSet.setObject(i, "summary_content", resultProcessing.getString(j, "summary_content"));
					resultSet.setObject(i, "totalSize", resultProcessing.getString(j, "totalSize"));
					resultSet.setObject(i, "csv", resultProcessing.getString(j, "csv"));
				}
			}
		}
		return resultSet;
	}

	@Method(desc = "获取solr分词关键字", logicStep = "获取solr分词关键字")
	@Param(name = "queryKeyword", desc = "查询关键字", range = "String类型值,无限制条件")
	@Return(desc = "solr分词关键字", range = "无限制")
	private String getParticipleQuery(String queryKeyword) {
		//获取solr分词关键字
		try (ISolrOperator os = SolrFactory.getInstance()) {
			List<String> participleList = os.getAnalysis(queryKeyword);
			StringBuilder queryPlus = new StringBuilder();
			queryPlus.append("\"");
			for (int i = 0; i < participleList.size(); i++) {
				queryPlus.append(participleList.get(i));
				if (i != participleList.size() - 1) {
					queryPlus.append("\" OR \"");
				} else {
					queryPlus.append("\"");
				}
			}
			return queryPlus.toString();
		} catch (Exception e) {
			throw new BusinessException("获取分词关键字失败！");
		}
	}

	@Method(desc = "查询结果集处理",
		logicStep = "数据可访问权限处理方式:暂不做数据校验处理（该表存储数据量多的情况下,数据查询效率问题）" +
			"1.获取solr返回结果" +
			"2.返回两类数据 数据文件和文本文件" +
			"2-1.存放文件卸数的 因为有摘要" +
			"2-2.存放数据库卸数的 因为返回每一行数据量大" +
			"2-3.记录文件的file_id" +
			"2-3-1.通过file_id查询文件,不为空则继续往结果集添加新的列,否则传入空集,文件采集" +
			"2-4.同样对表名进行处理,类似" +
			"2-5.把结果集放入一个新的结果集,然后返回"
	)
	@Param(name = "queryConditions", desc = "查询条件", range = "String类型值,符合solr查询规则")
	@Param(name = "start", desc = "查询开始行", range = "int类型值,1-99,默认为9", valueIfNull = "9")
	@Param(name = "pageSize", desc = "分页大小", range = "int类型值,默认为10", valueIfNull = "10")
	@Param(name = "totalSize", desc = "查询结果行数统计", range = "long类型值,不为空", valueIfNull = "0")
	@Return(desc = "solr分词关键字", range = "无限制")
	private Result queryResultProcessing(String queryConditions, int start, int pageSize) {
		Result result = new Result();
		//数据可访问权限处理方式: source_file_attribute 暂不做数据校验处理（该表存储数据量多的情况下,数据查询效率问题）
		//1.获取solr返回结果
		List<Map<String, Object>> querySolrRs = getQueryFromSolr(queryConditions, start, pageSize);
		if (querySolrRs.size() == 0) {
			//如果没有符合的返回null
			return result;
		}
		//2.两类数据 数据文件和文本文件
		HashSet<String> tableNameList = new HashSet<>();
		//2-1.存放文件卸数的 因为有摘要
		Map<String, String> map = new HashMap<>();
		//2-2.存放数据库卸数的 因为返回每一行数据量大
		List<Map<String, Object>> mapCsv = new ArrayList<>();
		logger.info("全文检索条件:" + queryConditions + ", 检索结果条数: " + querySolrRs.size() + " 条");
		int totalSize = querySolrRs.size();
		for (Map<String, Object> parseObject : querySolrRs) {
			//获取file_id或者数据文件的MD5值
			String id = parseObject.get("id").toString();
			//是否为大文件
			String isBigFile = parseObject.get("is_big_file").toString();
			//数据库卸数(必须保证,db卸数的表中没有表的列名为is_big_file)
			if (StringUtil.isEmpty(isBigFile)) {
				//数据库表名
				String tableName = parseObject.get("table-name").toString();
				if (!StringUtil.isEmpty(tableName)) {
					tableNameList.add(tableName);
					mapCsv.add(parseObject);
				}
			} else {
				//文件采集的文件摘要
				String summary = parseObject.get("file_summary").toString();
				map.put(id, summary);
			}
		}
		//2-3.记录文件的file_id
		HashSet<String> idList = new HashSet<>(map.keySet());
		//2-3-1.通过file_id查询文件,不为空则继续往结果集添加新的列,否则传入空集,文件采集
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		if (idList.size() != 0) {
			asmSql.clean();
			asmSql.addSql("select * from " + Source_file_attribute.TableName + " where");
			asmSql.addORParam("file_id", idList.toArray(), "");
			asmSql.addSql(" ORDER BY file_id");
			Result queryResultByFileId = Dbo.queryResult(asmSql.sql(), asmSql.params());
			for (int i = 0; i < queryResultByFileId.getRowCount(); i++) {
				String key = queryResultByFileId.getString(i, "file_id");
				String summary = map.get(key);
				queryResultByFileId.setObject(i, "summary_content", summary);
				queryResultByFileId.setObject(i, "totalSize", totalSize);
				queryResultByFileId.setObject(i, "csv", mapCsv);
			}
			//把结果集放入一个新的结果集
			result.add(queryResultByFileId);
		}
		//2-4.同样对表名进行处理,类似
		Result queryResultByTableName;
		if (tableNameList.size() != 0) {
			asmSql.clean();
			asmSql.addSql(" select * from source_file_attribute where ");
			asmSql.addORParam("hbase_name", tableNameList.toArray());
			queryResultByTableName = Dbo.queryResult(asmSql.sql(), asmSql.params());
			for (int i = 0; i < queryResultByTableName.getRowCount(); i++) {
				queryResultByTableName.setObject(i, "summary_content", "");
				queryResultByTableName.setObject(i, "totalSize", totalSize);
				queryResultByTableName.setObject(i, "csv", mapCsv);
			}
			//把结果集放入一个新的结果集,然后返回
			result.add(queryResultByTableName);
		}
		return result;
	}

	@Method(desc = "根据查询条件在solr中检索出符合条件的结果",
		logicStep = "数据可访问权限处理方式: 无数据库访问,不做权限校验" +
			"1.如果查询条数小于1条则显示默认9条,查询条数大于99条则显示99条,否则取传入的查询条数" +
			"2.返回当前登录的用户已经收藏的文件列表的List结果集"
	)
	@Param(name = "queryConditions", desc = "查询关键字", range = "String类型值,无限制条件,不为空", nullable = true)
	@Param(name = "start", desc = "查询开始行数", range = "int类型值,正整数")
	@Param(name = "rows", desc = "查询 行数", range = "int类型值,正整数")
	@Return(desc = "solr检索出的结果集", range = "无限制")
	private List<Map<String, Object>> getQueryFromSolr(String queryConditions, int start, int rows) {
		if (StringUtil.isEmpty(queryConditions)) {
			queryConditions = "*:*";
		}
		try (ISolrOperator os = SolrFactory.getInstance()) {
			return os.querySolr(queryConditions, start, rows);
		} catch (Exception e) {
			throw new BusinessException("Failed to queryFromSolr...");
		}

	}

	@Method(desc = "获取文章相似度查询结果",
		logicStep = "获取文章相似度查询结果")
	@Param(name = "docAddress", desc = "文章上传地址", range = "文章上传地址全路径")
	@Param(name = "similarityRate", desc = "文章相似率", range = "1%-100%")
	@Param(name = "searchWay", desc = "查询类型,是否返回文本信息", range = "1：是,0：否")
	@Return(desc = "相似文件查询结果的Result", range = "无限制")
	private Result getWZXSDResult(String docAddress, String similarityRate, IsFlag is_return_text, int currPage,
	                              int pageSize) {

		//合并后的结果集
		Result filterQuery = new Result();
		EssaySimilar es = new EssaySimilar();
		Result documentSimilar;
		if (StringUtil.isEmpty(similarityRate)) {
			similarityRate = "0";
		}
		//TODO getDocumentSimilarFromSolr方法未实现
		documentSimilar = es.getDocumentSimilarFromSolr(docAddress, similarityRate, is_return_text);
		for (int i = 0; i < documentSimilar.getRowCount(); i++) {
			String rowKey = documentSimilar.getString(i, "file_id");
			SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
			asmSql.clean();
			asmSql.addSql(" SELECT sfa.*,ds.datasource_name,gi.agent_name,fcs.fcs_name,uf.fav_id,uf.fav_flag FROM");
			asmSql.addSql(" data_source ds  JOIN agent_info gi ON ds.source_id = gi.source_id");
			asmSql.addSql(" JOIN file_collect_set fcs ON fcs.agent_id = gi.agent_id");
			asmSql.addSql(" JOIN source_file_attribute sfa ON sfa.source_id = ds.source_id");
			asmSql.addSql(" and  sfa.agent_id = gi.agent_id");
			asmSql.addSql(" and sfa.collect_set_id = fcs.fcs_id");
			asmSql.addSql(" LEFT JOIN user_fav uf ON sfa.file_id = uf.file_id");
			asmSql.addSql(" where sfa.file_id = ?");
			asmSql.addSql(" and collect_type = ? ORDER BY  sfa.file_id");
			asmSql.addParam(rowKey);
			asmSql.addParam(AgentType.WenJianXiTong.getCode());
			Result query = Dbo.queryPagedResult(new DefaultPageImpl(currPage, pageSize), asmSql.sql(), asmSql.params());
			if (!query.isEmpty()) {
				query.setObject(0, "summary_content",
					documentSimilar.getString(i, "summary_content"));
				query.setObject(0, "rate",
					documentSimilar.getString(i, "rate"));
				query.setObject(0, "totalSize",
					documentSimilar.getRowCount());
				filterQuery.add(query);
			}
		}
		filterQuery.sortResult("rate", "asc");
		return filterQuery;
	}

	@Method(desc = "获取文件名检索结果",
		logicStep = "获取文件名检索结果")
	@Param(name = "fileName", desc = "搜索文件名", range = "String类型字符串,没有输入限制")
	@Param(name = "currPage", desc = "分页开始页面", range = "1%-100%", valueIfNull = "1")
	@Param(name = "pageSize", desc = "分页大小", range = "1：是,0：否", valueIfNull = "10")
	@Return(desc = "文件名搜索查询结果的Result", range = "无限制")
	private Result getWJMSSResult(String fileName, int currPage, int pageSize) {
		Object[] sourceIdsObj = Dbo.queryOneColumnList("select source_id from data_source").toArray();
		//对搜索内容进行字段添加
		fileName = fileName.trim();
		SqlOperator.Assembler asmSql = SqlOperator.Assembler.newInstance();
		asmSql.clean();
		asmSql.addSql("SELECT sfa.*,ds.datasource_name,gi.agent_name,fcs.fcs_name,uf.fav_id,uf.fav_flag from (" +
			" SELECT a.* FROM source_file_attribute a  WHERE collect_type = ? ");
		asmSql.addParam(AgentType.WenJianXiTong.getCode());
		asmSql.addLikeParam("original_name", "%" + fileName + "%");
		asmSql.addORParam("a.source_id", sourceIdsObj);
		asmSql.addSql(" ) sfa join data_source ds  ON sfa.source_id=ds.source_id JOIN agent_info gi ON sfa.agent_id =" +
			" gi.agent_id JOIN file_collect_set fcs ON sfa.collect_set_id = fcs.fcs_id LEFT JOIN user_fav uf ON" +
			" sfa.file_id = uf.file_id ORDER BY seqencing DESC");
		DefaultPageImpl page = new DefaultPageImpl(currPage, pageSize);
		Result fileNameSearchResult = Dbo.queryPagedResult(page, asmSql.sql(), asmSql.params());
		EssaySimilar es = new EssaySimilar();
		for (int i = 0; i < fileNameSearchResult.getRowCount(); i++) {
			String fileAvroPath = fileNameSearchResult.getString(i, "file_avro_path");
			fileAvroPath = PathUtil.convertLocalPathToHDFSPath(fileAvroPath);
			String fileAvroBlock = fileNameSearchResult.getString(i, "file_avro_block");
			String fileId = fileNameSearchResult.getString(i, "file_id");
			//获取文件的摘要信息
			String fileSummary = es.getFileSummaryFromAvro(fileAvroPath, fileAvroBlock, fileId);
			fileNameSearchResult.setObject(i, "summary_content", fileSummary);
		}
		return fileNameSearchResult;
	}
}
