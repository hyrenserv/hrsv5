package hrds.h.biz.market;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.bean.EqualsBuilder;
import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator.Assembler;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.UploadFile;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.FileUploadUtil;
import fd.ng.web.util.RequestUtil;
import fd.ng.web.util.ResponseUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.*;
import hrds.commons.codes.fdCode.WebCodesItem;
import hrds.commons.collection.ProcessingData;
import hrds.commons.collection.bean.LayerBean;
import hrds.commons.entity.*;
import hrds.commons.entity.fdentity.ProjectTableEntity;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.tree.background.TreeNodeInfo;
import hrds.commons.tree.background.bean.TreeConf;
import hrds.commons.tree.background.query.UDLDataQuery;
import hrds.commons.tree.commons.TreePageSource;
import hrds.commons.utils.*;
import hrds.commons.utils.etl.EtlJobUtil;
import hrds.commons.utils.key.PrimayKeyGener;
import hrds.commons.utils.tree.Node;
import hrds.commons.utils.tree.NodeDataConvertedTreeList;
import hrds.h.biz.MainClass;
import hrds.h.biz.bean.CategoryRelationBean;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

//import hrds.h.biz.SqlAnalysis.HyrenOracleTableVisitor;
//import com.alibaba.druid.

@DocClass(desc = "加工信息查询类", author = "TBH", createdate = "2020年5月20日 16点55分")
public class MarketInfoAction extends BaseAction {

	//新增完加工表source_size（加工表的大小）存储大小为0
	private static final String Zero = "0";
	//新增完加工表的日期
	private static final String ZeroDate = "00000000";
	//统一命名：目标字段 作为may当中的key值
	private static final String TargetColumn = "targecolumn";
	//统一命名：来源字段 作为may当中的key值
	private static final String SourceColumn = "sourcecolumn";
	// excel文件后缀名
	private static final String xlsxSuffix = ".xlsx";
	private static final String xlsSuffix = ".xls";
	private static final Logger logger = LogManager.getLogger();
	//不需要长度的字段类型们
	private static final String[] nolengthcolumntypes = {"string", "text", "bigint"};
	// Excel导入模板文件名称
	private final static String EXCEL_FILEPATH = "dmImportExcel.xlsx";

	/**
	 * 封装一个检查字段正确的方法
	 */
	private void CheckColummn(String column, String columname) {
		Validator.notBlank(column, "不为空且不为空格，" + columname + "=" + column);
	}

	/**
	 * 封装一个update方法
	 */
	private void updatebean(ProjectTableEntity bean) {
		try {
			bean.update(Dbo.db());
		} catch (Exception e) {
			if (!(e instanceof ProjectTableEntity.EntityDealZeroException)) {
				throw new BusinessException(e.getMessage());
			}
		}
	}

	@Method(desc = "获取加工所有用到的存储层",
		logicStep = "获取加工所有用到的存储层")
	@Return(desc = "加工所有用到的存储层以及每个存储层的个数", range = "返回值取值范围")
	public List<Map<String, Object>> getAllDslInMart() {
		return Dbo.queryList("select  dsl_name,count(dsl_name) from " + Data_store_layer.TableName + " t1  join " +
				Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id and t2.data_source = ? group by dsl_name",
			StoreLayerDataSource.DM.getCode());
	}


	@Method(desc = "获取各个存储层中表大小的前五名",
		logicStep = "获取各个存储层中表大小的前五名")
	@Return(desc = "获取各个存储层中表大小的前五名", range = "返回值取值范围")
	public List<Map<String, Object>> getTableTop5InDsl() {
		List<Map<String, Object>> resultlist = new ArrayList<>();
		//获取加工用到的所有存储层
		List<Map<String, Object>> maps = Dbo
			.queryList("select  distinct t1.dsl_id,dsl_name from " + Data_store_layer.TableName + " t1  join " +
					Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id and t2.data_source = ? ",
				StoreLayerDataSource.DM.getCode());
		//遍历存储层，获取每一层的加工表前5
		for (Map<String, Object> map : maps) {
			String dsl_id = map.get("dsl_id").toString();
			String dsl_name = map.get("dsl_name").toString();
			Dtab_relation_store dm_relation_datatable = new Dtab_relation_store();
			dm_relation_datatable.setDsl_id(dsl_id);
			List<Map<String, Object>> maps1 = Dbo.queryList(
				"select t1.datatable_en_name,t1.soruce_size from " + Dm_datatable.TableName + " t1 left join "
					+ Dtab_relation_store.TableName +
					" t2 on t1.datatable_id = t2.tab_id where t2.dsl_id = ? and t2.data_source = ? order by soruce_size desc limit 5",
				dm_relation_datatable.getDsl_id(), StoreLayerDataSource.DM.getCode());
			Map<String, Object> tempmap = new HashMap<>();
			tempmap.put("dsl_name", dsl_name);
			tempmap.put("result", maps1);
			resultlist.add(tempmap);
		}
		return resultlist;
	}

	@Method(desc = "获取登录用户数据加工首页信息",
		logicStep = "根据用户ID进行搜索")
	@Return(desc = "获取登录用户数据加工首页信息", range = "返回值取值范围")
	public List<Dm_info> getMarketInfo() {
		return Dbo.queryList(Dm_info.class,
			"SELECT mart_name,data_mart_id FROM " + Dm_info.TableName + " where create_id = ? order by " +
				"data_mart_id asc", getUserId());
	}

	@Method(desc = "新增加工工程",
		logicStep = "1.检查数据合法性" +
			"2.新增前查询加工编号是否已存在" +
			"3.对dm_info初始化一些非页面传值" +
			"4.保存data_source信息")
	@Param(name = "dm_info", desc = "Dm_info整表bean信息", range = "与Dm_info表字段规则一致",
		isBean = true)
	@Param(name = "categoryRelationBeans", desc = "自定义加工分类实体对象数组", range = "无限制", isBean = true)
	public void addMarket(Dm_info dm_info, CategoryRelationBean[] categoryRelationBeans) {
		//1.检查数据合法性
		String mart_name = dm_info.getMart_name();
		String mart_number = dm_info.getMart_number();
		CheckColummn(mart_name, "加工名称");
		CheckColummn(mart_number, "加工编号");
		//2.新增前查询加工编号是否已存在
		Long data_mart_id = dm_info.getData_mart_id();
		//如果是更新
		if (data_mart_id != null) {
			if (Dbo
				.queryNumber("select count(*) from " + Dm_info.TableName + " where  mart_number = ? and data_mart_id != ?",
					mart_number, data_mart_id)
				.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
				throw new BusinessException("加工编号重复，请重新填写");
			}
			if (Dbo.queryNumber("select count(*) from " + Dm_info.TableName + " where mart_name = ? and data_mart_id != ?",
				mart_name, data_mart_id)
				.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
				throw new BusinessException("加工名称重复，请重新填写");
			}
			updatebean(dm_info);
		} else {
			if (Dbo.queryNumber("select count(*) from " + Dm_info.TableName + " where  mart_number = ? ", mart_number)
				.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
				throw new BusinessException("加工编号重复，请重新填写");
			}
			if (Dbo.queryNumber("select count(*) from " + Dm_info.TableName + " where mart_name = ? ", mart_name)
				.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
				throw new BusinessException("加工名称重复，请重新填写");
			}
			//3.对dm_info初始化一些非页面传值
			data_mart_id = PrimayKeyGener.getNextId();
			dm_info.setData_mart_id(data_mart_id);
			dm_info.setMart_storage_path("");
			dm_info.setCreate_date(DateUtil.getSysDate());
			dm_info.setCreate_time(DateUtil.getSysTime());
			dm_info.setCreate_id(getUserId());
			//4.保存data_source信息
			dm_info.add(Dbo.db());
		}
		for (CategoryRelationBean categoryRelationBean : categoryRelationBeans) {
			if (categoryRelationBean.getParent_category_id() == null &&
				categoryRelationBean.getParent_category_name().equals(dm_info.getMart_name())) {
				categoryRelationBean.setParent_category_id(data_mart_id);
			}
			categoryRelationBean.setData_mart_id(data_mart_id);
		}
		// 保存加工分类
		saveDmCategory(data_mart_id, categoryRelationBeans);
	}

	@Method(desc = "保存加工分类", logicStep = "1.判断加工工程是否存在" +
		"2.实体字段合法性检查" +
		"3.判断是新增还是更新加工分类" +
		"4.判断分类名称时候已存在，已存在不能新增" +
		"4.1新增加工分类" +
		"5.更新加工分类")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "categoryRelationBeans", desc = "自定义加工分类实体对象数组", range = "无限制", isBean = true)
	public void saveDmCategory(long data_mart_id, CategoryRelationBean[] categoryRelationBeans) {
		List<Map<String, Long>> idNameList = getIdNameList(categoryRelationBeans);
		for (CategoryRelationBean categoryRelationBean : categoryRelationBeans) {
			categoryRelationBean.setData_mart_id(data_mart_id);
			// 2.实体字段合法性检查
			checkDmCategoryFields(categoryRelationBean);
			// 3.判断是新增还是更新加工分类
			long num = Dbo.queryNumber(
				"select count(*) from " + Dm_category.TableName + " where category_id=?",
				categoryRelationBean.getCategory_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			// 4.判断加工分类名称与分类编号是否已存在，已存在不能新增
			if (num == 0) {
				if (isCategoryNameExist(categoryRelationBean.getData_mart_id(),
					categoryRelationBean.getCategory_name())) {
					throw new BusinessException("分类名称已存在" + categoryRelationBean.getCategory_name());
				}
				if (isCategoryNumExist(categoryRelationBean.getData_mart_id(),
					categoryRelationBean.getCategory_num())) {
					throw new BusinessException("分类编号已存在" + categoryRelationBean.getCategory_num());
				}
				// 4.1新增加工分类
				addDmCategory(categoryRelationBean.getData_mart_id(), categoryRelationBean, idNameList);
			} else {
				// 更新加工分类
				updateCategory(categoryRelationBean.getData_mart_id(), idNameList, categoryRelationBean);
			}
		}
	}

	@Method(desc = "更新加工分类", logicStep = "1.上级分类ID为空，编辑时已存在分类选择新增分类作为上级分类" +
		"2.不能选择同名分类，即不能选择自己作为上级分类" +
		"3.更新分类")
	@Param(name = "data_mart_id", desc = "新建加工工程时生成", range = "无限制")
	@Param(name = "idNameList", desc = "上级分类对应分类ID集合", range = "无限制")
	@Param(name = "categoryRelationBeans", desc = "自定义加工分类实体对象数组", range = "无限制", isBean = true)
	private void updateCategory(long data_mart_id, List<Map<String, Long>> idNameList,
	                            CategoryRelationBean categoryRelationBean) {
		// 1.上级分类ID为空，编辑时已存在分类选择新增分类作为上级分类
		if (categoryRelationBean.getParent_category_id() == null) {
			for (Map<String, Long> map : idNameList) {
				for (Map.Entry<String, Long> entry : map.entrySet()) {
					if (entry.getKey().equals(categoryRelationBean.getParent_category_name())) {
						categoryRelationBean.setParent_category_id(entry.getValue());
					}
				}
			}
		}
		// 2.不能选择同名分类，即不能选择自己作为上级分类
		isEqualsCategoryId(categoryRelationBean.getCategory_id(),
			categoryRelationBean.getParent_category_id(), data_mart_id);
		// 3.更新分类
		Dbo.execute(
			"update " + Dm_category.TableName
				+ " set category_name=?,category_num=?,parent_category_id=?,category_desc=?"
				+ " where category_id=?",
			categoryRelationBean.getCategory_name(), categoryRelationBean.getCategory_num(),
			categoryRelationBean.getParent_category_id(), categoryRelationBean.getCategory_desc(),
			categoryRelationBean.getCategory_id());
	}

	@Method(desc = "获取上级分类对应分类ID集合", logicStep = "1.给新增分类分配主键ID并获取上级分类对应分类ID集合")
	@Param(name = "categoryRelationBeans", desc = "自定义加工分类实体对象数组", range = "无限制", isBean = true)
	@Return(desc = "返回上级分类对应分类ID集合", range = "")
	private List<Map<String, Long>> getIdNameList(CategoryRelationBean[] categoryRelationBeans) {
		List<Map<String, Long>> idNameList = new ArrayList<>();
		// 1.给新增分类分配主键ID并获取上级分类对应分类ID集合
		for (CategoryRelationBean categoryRelationBean : categoryRelationBeans) {
			if (categoryRelationBean.getCategory_id() == null) {
				Map<String, Long> idNameMap = new HashMap<>();
				categoryRelationBean.setCategory_id(PrimayKeyGener.getNextId());
				idNameMap.put(categoryRelationBean.getCategory_name(), categoryRelationBean.getCategory_id());
				idNameList.add(idNameMap);
			}
		}
		return idNameList;
	}

	@Method(desc = "判断上级分类是否与分类ID相同", logicStep = "1.判断上级分类是否与分类ID相同")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	@Param(name = "parent_category_id", desc = "加工分类ID", range = "不能与分类ID相同")
	private void isEqualsCategoryId(long category_id, long parent_category_id, long data_mart_id) {
		// 1.判断上级分类是否与分类ID相同
		if (data_mart_id != parent_category_id && category_id == parent_category_id) {
			throw new BusinessException("不能选择同名的分类");
		}
	}

	@Method(desc = "根据加工分类id删除加工分类", logicStep = "1.判断加工分类是否被使用" +
		"2.删除加工分类")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	public void deleteDmCategory(long category_id) {
		// 1.判断加工分类是否被使用
		isDmDatatableExist(category_id);
		// 2.判断该分类下是否还有分类
		if (Dbo.queryNumber("select count(*) from " + Dm_category.TableName + " where parent_category_id=?",
			category_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			throw new BusinessException(category_id + "对应加工分类下还有分类");
		}
		// 2.删除加工分类
		DboExecute.deletesOrThrow("删除加工分类失败，可能分类不存在",
			"delete from " + Dm_category.TableName + " where category_id=?",
			category_id);
	}

	@Method(desc = "根据数据加工id查询加工分类信息", logicStep = "1.根据数据加工id查询加工分类信息")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Return(desc = "返回加工分类信息", range = "无限制")
	public Result getDmCategoryInfo(long data_mart_id) {
		// 1.根据数据加工id查询加工分类信息
		return Dbo.queryResult(
			"select * from " + Dm_category.TableName + " where data_mart_id = ?",
			data_mart_id);
	}

	@Method(desc = "获取加工分类树数据", logicStep = "1.判断加工工程是否存在" +
		"2.查询当前加工工程下的所有分类" +
		"3.获取当前分类的上级分类" +
		"4.获取当前分类的子分类" +
		"5.返回转换后的加工分类树数据")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Return(desc = "返回转换后的加工分类树数据", range = "无限制")
	public List<Node> getDmCategoryTreeData(long data_mart_id) {
		// 1.判断加工工程是否存在
		isDmInfoExist(data_mart_id);
		// 2.查询当前加工工程下的所有分类
		List<Long> categoryIdList = Dbo.queryOneColumnList(
			"select category_id from " + Dm_category.TableName + " where data_mart_id = ?",
			data_mart_id);
		if (categoryIdList.isEmpty()) {
			throw new BusinessException("当前加工工程下没有加工分类，请检查");
		}
		List<Map<String, Object>> treeList = new ArrayList<>();
		for (long category_id : categoryIdList) {
			// 3.获取当前分类的上级分类
			Dm_category dm_category = Dbo.queryOneObject(Dm_category.class,
				"select parent_category_id from " + Dm_category.TableName + " where category_id = ?",
				category_id)
				.orElseThrow(() -> new BusinessException("sql查询错误或映射实体失败"));
			if (dm_category.getParent_category_id() == data_mart_id) {
				// 加工ID等于上级分类ID,获取加工名称
				Dm_info dm_info = Dbo.queryOneObject(Dm_info.class,
					"select mart_name from " + Dm_info.TableName + " where data_mart_id = ?",
					data_mart_id)
					.orElseThrow(() -> new BusinessException("sql查询错误或映射实体失败"));
				Map<String, Object> treeMap = new HashMap<>();
				treeMap.put("id", data_mart_id);
				treeMap.put("label", dm_info.getMart_name());
				treeMap.put("parent_id", IsFlag.Fou.getCode());
				treeMap.put("description", dm_info.getMart_name());
				treeList.add(treeMap);
			}
			// 4.获取当前分类的子分类
			getChildDmCategoryTreeNodeData(data_mart_id, data_mart_id, treeList);
		}
		// 5.返回转换后的加工分类树数据
		return NodeDataConvertedTreeList.dataConversionTreeInfo(treeList);
	}

	@Method(desc = "获取所有分类节点信息", logicStep = "1.判断加工工程是否存在" +
		"2.根据加工ID获取加工工程信息" +
		"3.获取所有子分类信息" +
		"4.封装所有分类子节点信息并返回")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Return(desc = "返回所有分类子节点信息", range = "无限制")
	public List<Map<String, Object>> getDmCategoryNodeInfo(long data_mart_id) {
		// 1.判断加工工程是否存在
		isDmInfoExist(data_mart_id);
		// 2.根据加工ID获取加工工程信息
		Dm_info dm_info = getdminfo(String.valueOf(data_mart_id));
		List<Map<String, Object>> categoryList = new ArrayList<>();
		Map<String, Object> categoryMap = new HashMap<>();
		categoryMap.put("id", data_mart_id);
		categoryMap.put("isroot", true);
		categoryMap.put("topic", dm_info.getMart_name());
		categoryMap.put("direction", "left");
		categoryList.add(categoryMap);
		// 3.获取所有子分类信息
		getChildDmCategoryNodeInfo(data_mart_id, data_mart_id, categoryList);
		// 4.封装所有分类子节点信息并返回
		return categoryList;
	}

	@Method(desc = "根据分类ID，分类名称获取分类信息", logicStep = "1.判断加工工程是否存在" +
		"2.获取所有子分类信息" +
		"3.封装所有分类子节点信息并返回")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "category_name", desc = "分类ID", range = "新增加工分类ID时生成")
	@Param(name = "category_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Return(desc = "返回根据分类ID，分类名称获取分类信息", range = "无限制")
	public List<Map<String, Object>> getDmCategoryNodeInfoByIdAndName(long data_mart_id, String category_name,
	                                                                  long category_id) {
		// 1.判断加工工程是否存在
		isDmInfoExist(data_mart_id);
		List<Map<String, Object>> categoryList = new ArrayList<>();
		Map<String, Object> categoryMap = new HashMap<>();
		categoryMap.put("id", category_id);
		categoryMap.put("isroot", true);
		categoryMap.put("topic", category_name);
		categoryMap.put("direction", "left");
		categoryList.add(categoryMap);
		// 2.获取所有子分类信息
		getChildDmCategoryNodeInfo(data_mart_id, category_id, categoryList);
		// 3.封装所有分类子节点信息并返回
		return categoryList;
	}

	@Method(desc = "根据加工表主键查询当前存储层是否是关系性数据库", logicStep = "")
	@Param(name = "datatable_id", desc = "加工表主键", range = "无限制")
	@Param(name = "sql", desc = "查询SQL", range = "无限制")
	@UploadFile
	public boolean getIfRelationDatabase(String datatable_id, String sql) {
		Dtab_relation_store dtab_relation_store = new Dtab_relation_store();
		dtab_relation_store.setTab_id(datatable_id);
		Optional<Data_store_layer> data_store_layer = Dbo.queryOneObject(Data_store_layer.class, "select t1.dsl_id,t1.store_type from " + Data_store_layer.TableName + " t1 left join " + Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
			" where t2.tab_id = ? and t2.data_source = ?", dtab_relation_store.getTab_id(), StoreLayerDataSource.DM.getCode());
		if (data_store_layer.isPresent()) {
			Data_store_layer data_store_layer1 = data_store_layer.get();
			if (data_store_layer1.getStore_type().equals(Store_type.DATABASE.getCode())) {
				List<String> tablenames = DruidParseQuerySql.parseSqlTableToList(sql);
				for (String tablename : tablenames) {
					List<LayerBean> layerByTable = ProcessingData.getLayerByTable(tablename, Dbo.db());
					if (layerByTable.size() == 0) {
						return false;
					} else {
						if (data_store_layer1.getDsl_id().equals(layerByTable.get(0).getDsl_id())) {
							continue;
						}
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			throw new BusinessSystemException("查询表data_store_layer错误，根据datatable_id查找不到存储层");
		}
	}


	@Method(desc = "根据加工表主键查询当前存储层是否是关系性数据库", logicStep = "")
	@Param(name = "datatable_id", desc = "加工表主键", range = "无限制")
	public boolean getIfRelationDatabase2(String datatable_id) {
		Dtab_relation_store dtab_relation_store = new Dtab_relation_store();
		dtab_relation_store.setTab_id(datatable_id);
		Optional<Dm_operation_info> dm_operation_info = Dbo.queryOneObject(Dm_operation_info.class, "select view_sql from " + Dm_operation_info.TableName + " where datatable_id = ? and (end_date = ? or end_date = ?)",
			dtab_relation_store.getTab_id(), Constant.INITDATE, Constant.MAXDATE);
		if (dm_operation_info.isPresent()) {
			String sql = dm_operation_info.get().getView_sql();
			Optional<Data_store_layer> data_store_layer = Dbo.queryOneObject(Data_store_layer.class, "select t1.dsl_id,t1.store_type from " + Data_store_layer.TableName + " t1 left join " + Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
				" where t2.tab_id = ? and t2.data_source = ?", dtab_relation_store.getTab_id(), StoreLayerDataSource.DM.getCode());
			if (data_store_layer.isPresent()) {
				Data_store_layer data_store_layer1 = data_store_layer.get();
				if (data_store_layer1.getStore_type().equals(Store_type.DATABASE.getCode())) {
					List<String> tablenames = DruidParseQuerySql.parseSqlTableToList(sql);
					for (String tablename : tablenames) {
						List<LayerBean> layerByTable = ProcessingData.getLayerByTable(tablename, Dbo.db());
						if (layerByTable.size() == 0) {
							return false;
						} else {
							if (data_store_layer1.getDsl_id().equals(layerByTable.get(0).getDsl_id())) {
								continue;
							}
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			} else {
				throw new BusinessSystemException("查询表data_store_layer错误，根据datatable_id查找不到存储层");
			}
		} else {
			return false;
		}
	}


	@Method(desc = "获取所有子分类信息", logicStep = "1.根据加工ID与父分类ID查询加工分类信息" +
		"2.获取所有子分类信息" +
		"3.根据父分类ID查询加工分类信息不存在，查询当前分类信息并封装数据")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	@Param(name = "categoryList", desc = "加工分类集合", range = "无限制")
	private void getChildDmCategoryNodeInfo(long data_mart_id, long category_id,
	                                        List<Map<String, Object>> categoryList) {
		// 1.根据加工ID与父分类ID查询加工分类信息
		List<Dm_category> dmCategoryList = getDm_categories(data_mart_id, category_id);
		if (!dmCategoryList.isEmpty()) {
			for (Dm_category dm_category : dmCategoryList) {
				Map<String, Object> categoryMap = new HashMap<>();
				categoryMap.put("id", dm_category.getCategory_id());
				categoryMap.put("parentid", category_id);
				categoryMap.put("isroot", true);
				categoryMap.put("topic", dm_category.getCategory_name());
				categoryMap.put("direction", "right");
				if (!categoryList.contains(categoryMap)) {
					categoryList.add(categoryMap);
				}
				// 2.获取所有子分类信息
				getChildDmCategoryNodeInfo(data_mart_id, dm_category.getCategory_id(), categoryList);
			}
		}
	}

	@Method(desc = "更新加工分类名称", logicStep = "1.更新加工分类名称")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	@Param(name = "category_name", desc = "加工分类名称", range = "无限制")
	public void updateDmCategoryName(long category_id, String category_name) {
		// 1.更新加工分类名称
		DboExecute.updatesOrThrow("更新加工分类名称失败",
			"update " + Dm_category.TableName + " set category_name=? where category_id=?",
			category_name, category_id);
	}

	@Method(desc = "根据加工分类获取数据表信息", logicStep = "1.判断加工工程是否存在" +
		"2.关联查询加工数据表与加工分类表获取数据表信息")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	@Return(desc = "返回数据表信息", range = "无限制")
	public Result getDmDataTableByDmCategory(long data_mart_id, long category_id) {
		// 1.判断加工工程是否存在
		isDmInfoExist(data_mart_id);
		// 2.关联查询加工数据表与加工分类表获取数据表信息
		return Dbo.queryResult(
			"select t1.datatable_id,t1.datatable_en_name,t1.datatable_cn_name,t1.category_id," +
				"t2.category_name,t2.parent_category_id from "
				+ Dm_datatable.TableName + " t1 left join " + Dm_category.TableName
				+ " t2 on t1.category_id=t2.category_id and t1.data_mart_id=t2.data_mart_id"
				+ " where t1.data_mart_id=? and t2.category_id=? order by t1.category_id desc",
			data_mart_id, category_id);
	}

	@Method(desc = "获取数据表所有分类信息集合", logicStep = "1.判断加工工程是否存在" +
		"2.根据加工ID与父分类ID查询加工分类信息" +
		"3.获取所有分类信息并返回")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Return(desc = "返回数据表所有分类信息集合", range = "无限制")
	public List<Map<String, Object>> getDmCategoryForDmDataTable(long data_mart_id) {
		// 1.判断加工工程是否存在
		isDmInfoExist(data_mart_id);
		// 2.根据加工ID与父分类ID查询加工分类信息
		List<Dm_category> dmCategoryList = getDm_categories(data_mart_id, data_mart_id);
		List<Map<String, Object>> categoryList = new ArrayList<>();
		// 3.获取所有分类信息并返回
		for (Dm_category dm_category : dmCategoryList) {
			Map<String, Object> categoryMap = new HashMap<>();
			categoryMap.put("category_id", dm_category.getCategory_id());
			categoryMap.put("category_name", dm_category.getCategory_name());
			categoryList.add(categoryMap);
			getChildDmCategoryForDmDataTable(data_mart_id, dm_category.getCategory_id(),
				dm_category.getCategory_name(), categoryList);
		}
		return categoryList;
	}

	@Method(desc = "根据加工ID与父分类ID查询加工分类信息", logicStep = "1.根据加工ID与父分类ID查询加工分类信息")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "parent_category_id", desc = "父分类ID", range = "无限制")
	@Return(desc = "返回加工分类信息", range = "无限制")
	private List<Dm_category> getDm_categories(long data_mart_id, long parent_category_id) {
		// 1.根据加工ID与父分类ID查询加工分类信息
		return Dbo.queryList(Dm_category.class,
			"select category_id,category_name from " + Dm_category.TableName
				+ " where parent_category_id=? and data_mart_id=?",
			parent_category_id, data_mart_id);
	}

	@Method(desc = "获取所有子分类信息", logicStep = "1.根据加工ID与父分类ID查询加工分类信息" +
		"2.获取所有子分类信息")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "parent_category_id", desc = "父分类ID", range = "无限制")
	@Param(name = "category_name", desc = "分类名称", range = "新增加工分类时生成")
	@Param(name = "categoryList", desc = "加工分类集合", range = "无限制")
	@Return(desc = "返回子分类信息", range = "无限制")
	private void getChildDmCategoryForDmDataTable(long data_mart_id, long parent_category_id,
	                                              String category_name, List<Map<String, Object>> categoryList) {
		// 1.根据加工ID与父分类ID查询加工分类信息
		List<Dm_category> dmCategoryList = getDm_categories(data_mart_id, parent_category_id);
		// 2.获取所有子分类信息
		if (!dmCategoryList.isEmpty()) {
			for (Dm_category dm_category : dmCategoryList) {
				Map<String, Object> categoryMap = new HashMap<>();
				String categoryName =
					category_name + Constant.MARKETDELIMITER + dm_category.getCategory_name();
				categoryMap.put("category_id", dm_category.getCategory_id());
				categoryMap.put("category_name", categoryName);
				categoryList.add(categoryMap);
				if (!getDm_categories(data_mart_id, dm_category.getCategory_id()).isEmpty()) {
					getChildDmCategoryForDmDataTable(data_mart_id, dm_category.getCategory_id(),
						categoryName, categoryList);
				}
			}
		}
	}

	@Method(desc = "据父分类ID查询子节点加工分类信息", logicStep = "1.根据父分类ID查询加工分类信息" +
		"2.判断根据父分类ID查询加工分类信息是否存在做不同处理" +
		"3.循环判断根据父分类ID查询加工分类信息是否存在" +
		"4.据父分类ID查询加工分类信息不存在，查询当前分类信息")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	@Param(name = "treeList", desc = "加工分类数据集合", range = "无限制")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	private void getChildDmCategoryTreeNodeData(long data_mart_id, long category_id, List<Map<String,
		Object>> treeList) {
		// 1.根据父分类ID查询加工分类信息
		List<Dm_category> dmCategories = getDm_categories(data_mart_id, category_id);
		// 2.判断根据父分类ID查询加工分类信息是否存在做不同处理
		if (!dmCategories.isEmpty()) {
			for (Dm_category dmCategory : dmCategories) {
				Map<String, Object> treeMap = new HashMap<>();
				treeMap.put("id", dmCategory.getCategory_id());
				treeMap.put("label", dmCategory.getCategory_name());
				treeMap.put("parent_id", category_id);
				treeMap.put("description", dmCategory.getCategory_name());
				if (!treeList.contains(treeMap)) {
					treeList.add(treeMap);
				}
				// 3.循环判断根据父分类ID查询加工分类信息是否存在
				getChildDmCategoryTreeNodeData(data_mart_id, dmCategory.getCategory_id(), treeList);
			}
		}
	}

	@Method(desc = "判断加工分类是否被使用", logicStep = "1.判断加工分类是否被使用")
	@Param(name = "category_id", desc = "加工分类ID", range = "新增加工分类时生成")
	private void isDmDatatableExist(long category_id) {
		// 1.判断加工分类是否被使用
		if (Dbo.queryNumber(
			"select count(1) from " + Dm_datatable.TableName + " WHERE category_id = ?",
			category_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0) {
			throw new BusinessException(category_id + "对应加工分类正在被使用不能删除");
		}
	}

	@Method(desc = "新增加工分类", logicStep = "1.如果上级分类ID为空，设置上级分类ID为加工ID" +
		"2.判断分类ID与上级分类ID是否相同，相同不能选择同名的分类" +
		"3.新增加工分类")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "dm_category", desc = "加工分类实体对象", range = "与数据库表字段规则一致", isBean = true)
	private void addDmCategory(long data_mart_id, CategoryRelationBean categoryRelationBean,
	                           List<Map<String, Long>> idNameList) {
		if (categoryRelationBean.getParent_category_id() == null) {
			// 1.新增选择新增的分类作为上级分类
			for (Map<String, Long> map : idNameList) {
				for (Map.Entry<String, Long> entry : map.entrySet()) {
					// 1.1根据分类名称分配分类ID
					if (entry.getKey().equals(categoryRelationBean.getCategory_name())) {
						Dm_category dm_category = new Dm_category();
						categoryRelationBean.setCategory_id(entry.getValue());
						for (Map<String, Long> parentMap : idNameList) {
							for (Map.Entry<String, Long> parentEntry : parentMap.entrySet()) {
								if (parentEntry.getKey().equals(categoryRelationBean.getParent_category_name())) {
									BeanUtils.copyProperties(categoryRelationBean, dm_category);
									dm_category.setCreate_id(getUserId());
									dm_category.setCreate_date(DateUtil.getSysDate());
									dm_category.setCreate_time(DateUtil.getSysTime());
									dm_category.setParent_category_id(parentEntry.getValue());
									// 1.2判断分类ID与上级分类ID是否相同，相同不能选择同名的分类
									isEqualsCategoryId(dm_category.getCategory_id(), dm_category.getParent_category_id(),
										data_mart_id);
									// 1.3.新增加工分类
									dm_category.add(Dbo.db());
								}
							}
						}
					}
				}
			}
		} else {
			// 2.1新增选择已存在的上级分类
			Dm_category dm_category = new Dm_category();
			BeanUtils.copyProperties(categoryRelationBean, dm_category);
			dm_category.setData_mart_id(data_mart_id);
			dm_category.setCreate_id(getUserId());
			dm_category.setCreate_date(DateUtil.getSysDate());
			dm_category.setCreate_time(DateUtil.getSysTime());
			// 2.2判断分类ID与上级分类ID是否相同，相同不能选择同名的分类
			isEqualsCategoryId(dm_category.getCategory_id(), dm_category.getParent_category_id(), data_mart_id);
			// 2.3.新增加工分类
			dm_category.add(Dbo.db());
		}
	}

	@Method(desc = "判断加工分类名称是否已存在", logicStep = "1.判断分类名称是否已存在")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "category_name", desc = "加工分类名称", range = "无限制")
	private boolean isCategoryNameExist(long data_mart_id, String category_name) {
		// 1.判断加工分类名称是否已存在
		return Dbo.queryNumber(
			"select count(*) from " + Dm_category.TableName
				+ " where category_name=? and data_mart_id=?",
			category_name, data_mart_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "判断加工分类编号是否已存在", logicStep = "1.判断加工分类编号是否已存在")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	@Param(name = "category_num", desc = "加工分类编号", range = "无限制")
	private boolean isCategoryNumExist(long data_mart_id, String category_num) {
		// 1.判断加工分类编号是否已存在
		return Dbo.queryNumber(
			"select count(*) from " + Dm_category.TableName
				+ " where category_num=? and data_mart_id=?",
			category_num, data_mart_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) > 0;
	}

	@Method(desc = "加工分类表字段合法性检查", logicStep = "1.加工分类表字段合法性检查")
	@Param(name = "dm_category", desc = "Dm_category表实体对象", range = "与Dm_category表字段规则一致",
		isBean = true)
	private void checkDmCategoryFields(CategoryRelationBean dm_category) {
		// 1.加工分类表字段合法性检查
		Validator.notBlank(dm_category.getCategory_name(), "加工分类名称不能为空");
		Validator.notBlank(dm_category.getCategory_num(), "加工分类编号不能为空");
	}

	@Method(desc = "判断加工工程是否存在", logicStep = "1.判断加工工程是否存在")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "新增加工工程时生成")
	private void isDmInfoExist(long data_mart_id) {
		// 1.判断加工工程是否存在
		if (Dbo.queryNumber(
			"select count(*) from " + Dm_info.TableName + " where data_mart_id=?",
			data_mart_id)
			.orElseThrow(() -> new BusinessException("sql查询错误")) == 0) {
			throw new BusinessException(data_mart_id + "对应加工工程已不存在");
		}
	}

	@Method(desc = "获取加工工程的具体信息",
		logicStep = "获取加工工程的具体信息")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "data_mart_id")
	@Return(desc = "加工工程信息", range = "返回值取值范围")
	public Dm_info getdminfo(String data_mart_id) {
		Dm_info dm_info = new Dm_info();
		dm_info.setData_mart_id(data_mart_id);
		Optional<Dm_info> dm_info1 = Dbo
			.queryOneObject(Dm_info.class, "select * from " + Dm_info.TableName + " where data_mart_id = ?",
				dm_info.getData_mart_id());
		if (dm_info1.isPresent()) {
			return dm_info1.get();
		} else {
			throw new BusinessSystemException("查询表dm_info错误，根据data_mart_id查找不存在该表");
		}
	}


	@Method(desc = "获取登录用户查询数据加工工程下的所有加工表",
		logicStep = "根据数据加工工程ID进行查询")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "data_mart_id")
	@Return(desc = "当前加工工程下创建的所有加工表", range = "返回值取值范围")
	public List<Map<String, Object>> queryDMDataTableByDataMartID(String data_mart_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setData_mart_id(data_mart_id);
		List<Map<String, Object>> maps = Dbo.queryList("SELECT * ,case when t1.datatable_id in (select datatable_id from " +
				Datatable_field_info.TableName + ") then true else false end as isadd,"
				+ "case when ? in (select ds.store_type from "
				+ Data_store_layer.TableName
				+ " ds where dsl_id = t2.dsl_id) "
				+ "then true else false end as iscb from " +
				Dm_datatable.TableName + " t1 left join " + Dtab_relation_store.TableName +
				" t2 on t1.datatable_id = t2.tab_id left join " + Dm_category.TableName +
				" t3 on t1.category_id=t3.category_id" +
				" where t1.data_mart_id = ? and t2.data_source = ? order by t1.datatable_id asc",
			Store_type.CARBONDATA.getCode(), dm_datatable.getData_mart_id(), StoreLayerDataSource.DM.getCode());
		List<Map<String, Object>> resultmaps = new ArrayList<>();
		for (Map<String, Object> map : maps) {
			Object remarkObject = map.get("remark");
			if (remarkObject != null) {
				String remark = remarkObject.toString();
				if (!remark.isEmpty()) {
					if (checkIfNumeric(remark)) {
						Dm_datatable dm_datatable1 = new Dm_datatable();
						dm_datatable1.setDatatable_id(remark);
						List<Dm_datatable> dm_datatables = Dbo.queryList(Dm_datatable.class, "select * from " + Dm_datatable.TableName + " where datatable_id = ? and data_mart_id = ?"
							, dm_datatable1.getDatatable_id(), dm_datatable.getData_mart_id());
						if (!dm_datatables.isEmpty()) {
							continue;
						}
					}
				}
			}
			resultmaps.add(map);
		}
		return resultmaps;
	}

	private boolean checkIfNumeric(String s) {
		if (s.matches("-[0-9]+(.[0-9]+)?|[0-9]+(.[0-9]+)?")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 封装一个删除SQL的方法
	 *
	 * @param sql       开头为from的SQL
	 * @param param     接收一个参数 可以为 任意类型
	 * @param tablename 要删除的单一目标表的名称
	 */
	private void deletesql(String sql, Object param, String tablename) {
		long number = 0L;
		OptionalLong optionalLong = Dbo.queryNumber("select count(*) from (select * " + sql + ") as a", param);
		if (optionalLong.isPresent()) {
			number = optionalLong.getAsLong();
		}
		int execute = Dbo.execute("delete " + sql, param);
		if (execute != number) {
			throw new BusinessSystemException("删除表" + tablename + "失败，查询结果与删除结果存在差异");
		}
	}

	@Method(desc = "删除加工表及其相关的所有信息",
		logicStep = "1、删除数据表信息" +
			"2、删除数据操作信息表" +
			"3、删除数据表已选数据源信息" +
			"4、删除结果映射信息表" +
			"5、删除数据源表字段" +
			"6、删除数据表字段信息" +
			"7、删除加工表存储关系表" +
			"8、删除加工字段存储信息")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id")
	//提供给管控的接口 用于删除加工表
	public void deleteDMDataTable(String datatable_id) {

//		Boolean runStatus = checkRunStatus(datatable_id);
//		if (runStatus) {
//			throw new BusinessSystemException("该表已经生成，不能删除");
//		}
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		//5、删除数据源表字段
		String sql = " from " + Own_source_field.TableName + " where own_dource_table_id in " +
			"(select own_dource_table_id from " + Dm_datatable_source.TableName + " where datatable_id = ? )";
		deletesql(sql, dm_datatable.getDatatable_id(), Own_source_field.TableName);
		//8、删除加工字段存储信息

		sql = " from " + Dcol_relation_store.TableName + " where col_id in " +
			"(select datatable_field_id from " + Datatable_field_info.TableName + " where datatable_id = ?)";
		deletesql(sql, dm_datatable.getDatatable_id(), Own_source_field.TableName);
		//1、删除数据表信息
		sql = " from " + Dm_datatable.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dm_datatable.TableName);
		//2、删除数据操作信息表
		sql = " from " + Dm_operation_info.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dm_operation_info.TableName);
		//3、删除数据表已选数据源信息
		sql = " from " + Dm_datatable_source.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dm_datatable_source.TableName);
		//4、删除结果映射信息表
		sql = " from " + Dm_etlmap_info.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dm_etlmap_info.TableName);
		//6、删除数据表字段信息
		sql = " from " + Datatable_field_info.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Datatable_field_info.TableName);
		//7、删除加工表存储关系表
		sql = " from " + Dtab_relation_store.TableName + " where tab_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dtab_relation_store.TableName);
		//删除前后置处理关系表
		sql = " from " + Dm_relevant_info.TableName + " where datatable_id = ?";
		deletesql(sql, dm_datatable.getDatatable_id(), Dm_relevant_info.TableName);
	}

	@Method(desc = "加工查询存储配置表",
		logicStep = "加工查询存储配置表")
	@Return(desc = "加工查询存储配置表", range = "返回值取值范围")
	public List<Data_store_layer> searchDataStore() {
		return Dbo.queryList(Data_store_layer.class, "SELECT * from " + Data_store_layer.TableName);
	}

	@Method(desc = "加工查询存储配置表（模糊查询）",
		logicStep = "加工查询存储配置表（模糊查询）")
	@Param(name = "fuzzyqueryitem", desc = "fuzzyqueryitem", range = "模糊查询字段", nullable = true)
	@Return(desc = "加工查询存储配置表", range = "返回值取值范围")
	public List<Data_store_layer> searchDataStoreByFuzzyQuery(String fuzzyqueryitem) {
		return Dbo
			.queryList(Data_store_layer.class, "select * from " + Data_store_layer.TableName + " where dsl_name like ?",
				"%" + fuzzyqueryitem + "%");
	}


	@Method(desc = "保存加工添加表页面1的信息，新增加工表",
		logicStep = "1.检查数据合法性" +
			"2.新增时前查询加工表英文名是否已存在" +
			"3.新增时对Dm_datatable初始化一些非页面传值" +
			"4.保存Dm_datatable信息" +
			"5.新增数据至dm_relation_datatable" +
			"6 返回主键datatable_id")
	@Param(name = "dm_datatable", desc = "dm_datatable", range = "与dm_datatable表字段规则一致",
		isBean = true)
	@Param(name = "dsl_id", desc = "dsl_id", range = "与Dm_info表字段规则一致")
	@Return(desc = "查询结果", range = "返回值取值范围")
	public Map<String, Object> addDMDataTable(Dm_datatable dm_datatable, String dsl_id) {
		Map<String, Object> map = new HashMap<>();
		//1检查数据合法性
		CheckColummn(dm_datatable.getDatatable_en_name(), "表英文名");
		CheckColummn(dm_datatable.getDatatable_cn_name(), "表中文名");
		CheckColummn(dm_datatable.getSql_engine(), "Sql执行引擎");
		CheckColummn(dm_datatable.getTable_storage(), "数据存储方式");
		CheckColummn(dm_datatable.getStorage_type(), "进数方式");
		CheckColummn(dm_datatable.getDatatable_lifecycle(), "数据生命周期");
		CheckColummn(dm_datatable.getRepeat_flag(), "表名可能重复");
		Validator.notNull(dm_datatable.getCategory_id(), "加工分类ID不能为空");
		if (TableLifeCycle.YongJiu.getCode().equalsIgnoreCase(dm_datatable.getDatatable_lifecycle())) {
			dm_datatable.setDatatable_due_date(Constant.MAXDATE);
		} else {
			CheckColummn(dm_datatable.getDatatable_due_date(), "数据表到期日期");
		}
		CheckColummn(dsl_id, "数据存储");
		//2检查表名重复
		if (Dbo.queryNumber("select count(*) from " + Dm_datatable.TableName + " where  datatable_en_name = ?",
			dm_datatable.getDatatable_en_name())
			.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
			if (dm_datatable.getRepeat_flag().equals(IsFlag.Shi.getCode())) {
				map.put("ifrepeat", true);
			} else {
				throw new BusinessException("表英文名重复且表名不可能重复，请重新填写");
			}
		} else {
			map.put("ifrepeat", false);
		}
		//3.对Dm_datatable初始化一些非页面传值
		long datatable_id = PrimayKeyGener.getNextId();
		dm_datatable.setDatatable_id(datatable_id);
		dm_datatable.setDatatable_create_date(DateUtil.getSysDate());
		dm_datatable.setDatatable_create_time(DateUtil.getSysTime());
		dm_datatable.setDdlc_date(DateUtil.getSysDate());
		dm_datatable.setDdlc_time(DateUtil.getSysTime());
		dm_datatable.setDatac_date(DateUtil.getSysDate());
		dm_datatable.setDatac_time(DateUtil.getSysTime());
		dm_datatable.setSoruce_size(Zero);
		dm_datatable.setEtl_date(ZeroDate);
		//4.保存dm_datatable信息
		dm_datatable.add(Dbo.db());
		//5.新增数据至Dtab_relation_store
		Dtab_relation_store dm_relation_datatable = new Dtab_relation_store();
		dm_relation_datatable.setDsl_id(dsl_id);
		dm_relation_datatable.setTab_id(datatable_id);
		dm_relation_datatable.setIs_successful(JobExecuteState.DengDai.getCode());
		dm_relation_datatable.setData_source(StoreLayerDataSource.DM.getCode());
		dm_relation_datatable.add(Dbo.db());
		//6 返回主键datatable_id
		map.put("datatable_id", datatable_id);
		return map;
	}

	@Method(desc = "根据用户所属的部门查询所有加工表",
		logicStep = "根据用户所属的部门查询所有加工表")
	@Return(desc = "查询结果", range = "返回值取值范围")
	public List<Dm_datatable> getAllDatatable_En_Name() {
		return Dbo.queryList(Dm_datatable.class,
			"select distinct t1.datatable_en_name from " + Dm_datatable.TableName + " t1 left join " +
				Dm_info.TableName + " t2 on t1.data_mart_id = t2.data_mart_id left join " + Sys_user.TableName
				+ " t3 on t2.create_id = t3.user_id " +
				" where t3.dep_id = ?", getUser().getDepId());
	}

	@Method(desc = "检查加工表状态",
		logicStep = "检查加工表状态")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id")
	@Return(desc = "加工表状态", range = "加工表状态")
	public Boolean checkRunStatus(String datatable_id) {
		Map<String, Object> resultmap = new HashMap<>();
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		Map<String, Object> stringObjectMap = Dbo
			.queryOneObject("select etl_date from " + Dm_datatable.TableName + " where datatable_id = ?",
				dm_datatable.getDatatable_id());
		//是否运行完成过
		boolean haveRun = !stringObjectMap.get("etl_date").equals(ZeroDate);
		Map<String, Object> stringObjectMap2 = Dbo.queryOneObject(
			"select is_successful from " + Dtab_relation_store.TableName + " where tab_id = ? and data_source = ?",
			dm_datatable.getDatatable_id(), StoreLayerDataSource.DM.getCode());
		//是否正在运行
		boolean isrunning = stringObjectMap2.get("is_successful").equals(JobExecuteState.YunXing.getCode());
		boolean isdengdai = stringObjectMap2.get("is_successful").equals(JobExecuteState.DengDai.getCode());
		//如果是等待 则返回false 表示页面不用上锁
		if (isdengdai) {
			return false;
		} else if (haveRun) {
			return true;
		} else {
			return true;
		}
	}

	@Method(desc = "查询与当前datatable_id拥有相同datatable_en_name的另外一组datatable_id",
		logicStep = "查询与当前datatable_id拥有相同datatable_en_name的另外一组datatable_id")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id")
	@Return(desc = "查询结果", range = "返回值取值范围")
	public List<Dm_datatable> getTableIdFromSameNameTableId(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		return Dbo.queryList(Dm_datatable.class, "select datatable_id  from " + Dm_datatable.TableName
				+ " where datatable_en_name in (select datatable_en_name from "
				+ Dm_datatable.TableName
				+ " where datatable_id = ?) and datatable_id != ? order by datatable_create_date,datatable_create_time",
			dm_datatable.getDatatable_id(), dm_datatable.getDatatable_id());
	}

	@Method(desc = "编辑更新加工添加表页面1的信息，更新加工表",
		logicStep = "1.检查数据合法性" +
			"3.新增时对Dm_datatable初始化一些非页面传值" +
			"4.保存或者更新Dm_datatable信息" +
			"4.编辑时，删除Dm_datatable中原有信息" +
			"5.新增数据至dm_relation_datatable" +
			"6 返回主键datatable_id")
	@Param(name = "dm_datatable", desc = "dm_datatable", range = "与dm_datatable表字段规则一致",
		isBean = true)
	@Param(name = "dsl_id", desc = "dsl_id", range = "与Dm_info表字段规则一致")
	public Map<String, Object> updateDMDataTable(Dm_datatable dm_datatable, long dsl_id) {
		Map<String, Object> map = new HashMap<>();
		//1检查数据合法性
		CheckColummn(dm_datatable.getDatatable_en_name(), "表英文名");
		CheckColummn(dm_datatable.getDatatable_cn_name(), "表中文名");
		CheckColummn(dm_datatable.getSql_engine(), "Sql执行引擎");
		CheckColummn(dm_datatable.getTable_storage(), "数据存储方式");
		CheckColummn(dm_datatable.getStorage_type(), "进数方式");
		CheckColummn(dm_datatable.getDatatable_lifecycle(), "数据生命周期");
		CheckColummn(dm_datatable.getRepeat_flag(), "表名可能重复");
		if (TableLifeCycle.LinShi.getCode().equalsIgnoreCase(dm_datatable.getDatatable_lifecycle())) {
			CheckColummn(dm_datatable.getDatatable_due_date(), "数据表到期日期");
			dm_datatable.setDatatable_due_date(dm_datatable.getDatatable_due_date().substring(0, 10).replace("-", ""));
		} else {
			dm_datatable.setDatatable_due_date(Constant.MAXDATE);
		}

		if (Dbo.queryNumber(
			"select count(*) from " + Dm_datatable.TableName + " where  datatable_en_name = ? and datatable_id != ?",
			dm_datatable.getDatatable_en_name(), dm_datatable.getDatatable_id())
			.orElseThrow(() -> new BusinessException("sql查询错误！")) != 0) {
			if (dm_datatable.getRepeat_flag().equals(IsFlag.Shi.getCode())) {
				map.put("ifrepeat", true);
			} else {
				throw new BusinessException("表英文名重复且表名不可能重复，请重新填写");
			}
		} else {
			map.put("ifrepeat", false);
		}
		//4.dm_datatable
		updatebean(dm_datatable);
		//查询记录
		Optional<Dtab_relation_store> dm_relation_datatableOptional = Dbo.queryOneObject(Dtab_relation_store.class,
			"select * from " + Dtab_relation_store.TableName + " where tab_id = ? and data_source = ?",
			dm_datatable.getDatatable_id(), StoreLayerDataSource.DM.getCode());
		//更新dm_relation_datatable库中的数据
		if (dm_relation_datatableOptional.isPresent()) {
			Dtab_relation_store dm_relation_datatable = dm_relation_datatableOptional.get();
			DboExecute.updatesOrThrow("更新的存储层信息超出了预期范围",
				"UPDATE " + Dtab_relation_store.TableName + " SET dsl_id = ? WHERE tab_id = ?",
				dsl_id, dm_relation_datatable.getTab_id());
		}
		//6 返回主键datatable_id
		map.put("datatable_id", String.valueOf(dm_datatable.getDatatable_id()));
		return map;

	}

	@Method(desc = "加工页面1回显",
		logicStep = "根据数据加工表ID进行查询")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id")
	@Return(desc = "当前加工表的信息", range = "返回值取值范围")
	public List<Map<String, Object>> queryDMDataTableByDataTableId(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		return Dbo.queryList("select t1.*,t2.*,t3.category_name from "
				+ Dm_datatable.TableName + " t1 left join "
				+ Dtab_relation_store.TableName + " t2 on t1.datatable_id = t2.tab_id left join "
				+ Dm_category.TableName + " t3 on t3.category_id=t1.category_id" +
				" where t1.datatable_id= ? and t2.data_source=?",
			dm_datatable.getDatatable_id(), StoreLayerDataSource.DM.getCode());
	}

	@Method(desc = "根据数据加工表英文名 检查表名是否重复",
		logicStep = "根据数据加工表英文名进行查询")
	@Param(name = "datatable_en_name", desc = "datatable_en_name", range = "datatable_en_name")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id", nullable = true)
	@Return(desc = "是否重复,如果重复,返回重复了的主键ID", range = "返回值取值范围")
	public Map<String, Object> queryTableNameIfRepeat(String datatable_en_name, String datatable_id) {
		Map<String, Object> resultmap = new HashMap<>();
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_en_name(datatable_en_name);
		List<Dm_datatable> dm_datatables;
		//如果是新增加工表
		if (StringUtils.isEmpty(datatable_id)) {
			//查询相同表名的表
			dm_datatables = Dbo
				.queryList(Dm_datatable.class, "select * from " + Dm_datatable.TableName + "  where datatable_en_name= ?",
					dm_datatable.getDatatable_en_name());
		}
		//更新 SQL多增加一个不包括当前ID
		else {
			dm_datatable.setDatatable_id(datatable_id);
			//查询相同表名的表
			dm_datatables = Dbo.queryList(Dm_datatable.class,
				"select * from " + Dm_datatable.TableName + "  where datatable_en_name= ? and datatable_id != ?",
				dm_datatable.getDatatable_en_name(), dm_datatable.getDatatable_id());
		}
		//如果不是空的话 那么无论有多少个相同的表 他们的配置也是一样的
		if (!dm_datatables.isEmpty()) {
			dm_datatable = dm_datatables.get(0);
			resultmap.put("datatable_id", dm_datatable.getDatatable_id());
			resultmap.put("result", true);
		} else {
			resultmap.put("result", false);
		}
		return resultmap;
	}

//	@Method(desc = "根据加工表主键ID:datatable_id 判断当前加工是否重复 ",
//			logicStep = "根据数据加工表ID进行查询")
//	@Param(name = "datatable_id", desc = "加工数据表主键", range = "datatable_id")
//	@Return(desc = "是否重复", range = "返回值取值范围")
//	public Boolean queryDataTableIdIfRepeat(String datatable_id) {
//		Map<String, Object> resultmap = new HashMap<>();
//		Dm_datatable dm_datatable = new Dm_datatable();
//		dm_datatable.setDatatable_id(datatable_id);
//		OptionalLong optionalLong = Dbo.queryNumber("select count(*) from " + Dm_datatable.TableName +
//				" where datatable_en_name in (select datatable_en_name from " + Dm_datatable.TableName + " where datatable_id = ? )", dm_datatable.getDatatable_id());
//		if (optionalLong.isPresent()) {
//			long asLong = optionalLong.getAsLong();
//			if (asLong > 1) {
//				return true;
//			} else {
//				return false;
//			}
//		} else {
//			throw new BusinessSystemException("查询是否加工重复错误");
//		}
//	}

	@Method(desc = "根据SQL获取采集数据，默认显示10条",
		logicStep = "1.处理SQL" +
			"2.查询SQL")
	@Param(name = "querysql", desc = "查询SQL", range = "String类型SQL")
	@Param(name = "sqlparameter", desc = "SQL参数", range = "String类型参数", nullable = true)
	@Return(desc = "查询返回结果集", range = "无限制")
	@UploadFile
	public List<Map<String, Object>> getDataBySQL(String querysql, String sqlparameter) {
		//初始化查询结果集
		List<Map<String, Object>> dataBySQL_rs = new ArrayList<>();
		//1.处理SQL
		try {
			DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql();
			//处理视图问题
			querysql = druidParseQuerySql.GetNewSql(querysql);
			//使用SQL解析，检查SQL是否存在语法错误
			DruidParseQuerySql dpqs = new DruidParseQuerySql();
			dpqs.getBloodRelationMap(querysql);
			//将页面填写的参数替换
			if (!StringUtils.isEmpty(sqlparameter)) {
				// 获取参数组
				String[] singlePara = StringUtils.split(sqlparameter, ';');// 获取单个动态参数
				for (String s : singlePara) {
					String[] col_val = StringUtils.split(s, '=');
					if (col_val.length > 1) {
						// 按顺序从左到右对原始sql中的#{}进行替换
						querysql = StringUtils.replace(querysql, "#{" + col_val[0].trim() + "}", col_val[1]);
					}
				}
			}
			querysql = querysql.trim();
			if (querysql.endsWith(";")) {
				//去除分号
				querysql = querysql.substring(0, querysql.length() - 1);
			}
			//使用分页查询 查询10条数据
			try (DatabaseWrapper db = new DatabaseWrapper()) {
				new ProcessingData() {
					@Override
					public void dealLine(Map<String, Object> map) {
						dataBySQL_rs.add(map);
					}
				}.getPageDataLayer(querysql, db, 1, 10);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			throw e;
		}
		return dataBySQL_rs;
	}

	@Method(desc = "根据数据表ID,获取数据库类型，获取选中数据库的附加属性字段",
		logicStep = "查询数据库，返回结果")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "查询返回结果集", range = "无限制")
	public List<Map<String, Object>> getColumnMore(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		return Dbo.queryList("select dslad_id,dsla_storelayer from " + Data_store_layer_added.TableName + " t1 " +
				"left join " + Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
				"where t2.tab_id = ? and t2.data_source = ? order by dsla_storelayer", dm_datatable.getDatatable_id(),
			StoreLayerDataSource.DM.getCode());
	}

	@Method(desc = "根据SQL获取列结构",
		logicStep = "1.根据SQL解析获取所有字段" +
			"2.设置默认的字段类型" +
			"3.根据血缘来分析目标字段来源字段的字段类型，并且转换字段类型" +
			"4.设置默认附加字段属性不勾选" +
			"5.返回结果")
	@Param(name = "querysql", desc = "查询SQL", range = "String类型SQL")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Param(name = "sqlparameter", desc = "SQL参数", range = "String类型参数", nullable = true)
	@Return(desc = "列结构", range = "无限制")
	@UploadFile
	public Map<String, Object> getColumnBySql(String querysql, String datatable_id, String sqlparameter) {
		Map<String, Object> resultmap = new HashMap<>();
		List<Map<String, Object>> resultlist = new ArrayList<>();
		//检查数据合法性
		CheckColummn(querysql, "查询sql");
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		//获取当前加工选择的存储目的地
		List<Map<String, Object>> storeTypeList = Dbo
			.queryList("select store_type,t1.dsl_id from " + Data_store_layer.TableName + " t1 left join "
					+ Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
					"where t2.tab_id = ? and t2.data_source = ?", dm_datatable.getDatatable_id(),
				StoreLayerDataSource.DM.getCode());
		if (storeTypeList.isEmpty() || storeTypeList.get(0).get("store_type") == null
			|| storeTypeList.get(0).get("dsl_id") == null) {
			throw new BusinessSystemException("查询当前加工存储目的地错误，请检查");
		}
		String storeType = storeTypeList.get(0).get("store_type").toString();
		String dsl_id = storeTypeList.get(0).get("dsl_id").toString();
		//根据存储目的地 设置默认的字段类型
		String field_type = getDefaultFieldType(storeType);
		//设置List，记录所有目标字段
		List<String> columnNameList = new ArrayList<>();
		HashMap<String, Object> bloodRelationMap = new HashMap<>();
		//此处进行获取血缘关系map 如果sql写的不规范 会存在报错
		try {
			DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql(querysql);
			columnNameList = druidParseQuerySql.parseSelectAliasField();
			DruidParseQuerySql dpqs = new DruidParseQuerySql();
			bloodRelationMap = dpqs.getBloodRelationMap(querysql);
		} catch (Exception e) {
			if (!StringUtils.isEmpty(e.getMessage())) {
				logger.error(e.getMessage());
				throw e;
			}
			//如果druid解析错误 并且没有返回信息 说明sql存在问题 用获取sql查询结果的方法返回错误信息
			else {
				List<Map<String, Object>> dataBySQL = getDataBySQL(querysql, sqlparameter);
			}
		}
		String targetfield_type;
		String field_length = "";
		for (int i = 0; i < columnNameList.size(); i++) {
			String everyColumnName = columnNameList.get(i);
			Map<String, Object> map = new LinkedHashMap<>();
			//设置默认值
			map.put("field_en_name", everyColumnName);
			map.put("field_cn_name", everyColumnName);
			map.put("process_mapping", everyColumnName);
			map.put("field_seq", i);
			map.put("group_mapping", "");
			//根据字段名称，获取改字段的血缘关系
			Object object = bloodRelationMap.get(everyColumnName);
			//如果没有获取到血缘关系，则提供默认的字段类型
			if (null == object) {
				targetfield_type = field_type;
			} else {
				//获取到血缘关系
				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) object;
				//如果改字段来源仅为单一字段
				if (list.size() == 1) {
					HashMap<String, Object> stringObjectHashMap = list.get(0);
					//获取字段来源表名称
					String sourcetable = stringObjectHashMap.get(DruidParseQuerySql.sourcetable).toString();
					//获取字段来源字段名称
					String sourcecolumn = stringObjectHashMap.get(DruidParseQuerySql.sourcecolumn).toString();
					//获取字段类型
					Map<String, String> fieldType = getFieldType(sourcetable, sourcecolumn, field_type, dsl_id);
					targetfield_type = fieldType.get("targettype");
					field_length = fieldType.get("field_length");
					if (field_length == null || Arrays.asList(nolengthcolumntypes).contains(targetfield_type)) {
						field_length = "";
					}
				}
				//如果改字段来源为多个字段，则设置默认的字段类型
				else {
					targetfield_type = field_type;
				}
			}
			map.put("field_type", targetfield_type);
			map.put("field_length", field_length);
			//默认提供varchar 长度为100
			map.put("field_process", ProcessType.YingShe.getCode());
			//将所有勾选的 附加字段属性 默认选为不勾选
			List<Map<String, Object>> dslaStorelayerList = Dbo
				.queryList("select dslad_id,dsla_storelayer from " + Data_store_layer_added.TableName + " t1 " +
						"left join " + Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
						"where t2.tab_id = ? and t2.data_source = ? order by dsla_storelayer", dm_datatable.getDatatable_id(),
					StoreLayerDataSource.DM.getCode());
			for (Map<String, Object> dslaStorelayeMap : dslaStorelayerList) {
				map.put(StoreLayerAdded.ofValueByCode(dslaStorelayeMap.get("dsla_storelayer").toString()), false);
			}
			resultlist.add(map);
		}
		resultmap.put("result", resultlist);
		List<Map<String, Object>> columnlist = new ArrayList<>();
		for (int i = 0; i < columnNameList.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("value", columnNameList.get(i));
			map.put("code", i);
			columnlist.add(map);
		}
		resultmap.put("columnlist", columnlist);
		return resultmap;
	}

	/**
	 * 获取字段类型 默认的字段类型 String/Varchar
	 */
	private Map<String, String> getFieldType(String sourcetable, String sourcecolumn, String
		field_type, String dsl_id) {
		Map<String, String> resultmap = new HashMap<>();
		//根据表名,寻找表来自哪一层
		List<LayerBean> layerByTable = ProcessingData.getLayerByTable(sourcetable, Dbo.db());
		//如果没有找到该表属于哪一层 则返回原始类型
		if (layerByTable.isEmpty()) {
			resultmap.put("sourcetype", field_type);
			resultmap.put("targettype", field_type);
			return resultmap;
		}
		//找到了该表来源
		else {
			String dataSourceType = layerByTable.get(0).getDst();
			//如果是帖源层
			if (dataSourceType.equals(DataSourceType.DCL.getCode())) {
				//根据表名和字段名查找字段信息
				List<Map<String, Object>> maps = Dbo.queryList(
					"select t2.column_type,t4.dsl_id from " + Data_store_reg.TableName + " t1 left join "
						+ Table_column.TableName + " t2 on t1.table_id = t2.table_id" +
						" left join " + Table_storage_info.TableName + " t3 on t1.table_id = t3.table_id left join " +
						Dtab_relation_store.TableName + " t4 on t4.tab_id = t3.storage_id " +
						"where lower(t2.column_name) = ? and lower(t1.hyren_name) = ? ",
					sourcecolumn.toLowerCase(), sourcetable.toLowerCase());
				//如果为空，说明字段不存在
				if (maps.isEmpty()) {
					resultmap.put("sourcetype", field_type);
					resultmap.put("targettype", field_type);
					return resultmap;
				}
				//如果不为空，则找到了
				else {
					//帖源层的字段类型
					String column_type = maps.get(0).get("column_type").toString();
					//帖源层所记录的存储目的地ID
					String DCLdsl_id = maps.get(0).get("dsl_id").toString();
					resultmap.put("sourcetype", column_type.toLowerCase());
					//摘取长度，并记录
					if (column_type.contains("(") && column_type.contains(")") && column_type.indexOf("(") < column_type
						.indexOf(")")) {
						String field_length = column_type.substring(column_type.indexOf("(") + 1, column_type.indexOf(")"));
						resultmap.put("field_length", field_length);
					}
					////如果是来自帖源的话 就需要做两次转换
					column_type = transFormColumnType(column_type, DCLdsl_id);
					column_type = transFormColumnType(column_type, dsl_id);
					resultmap.put("targettype", column_type.toLowerCase());
					return resultmap;
				}
			}
			//如果是加工层
			else if (dataSourceType.equals(DataSourceType.DML.getCode())) {
				//根据表名和字段名查找字段信息
				List<Map<String, Object>> maps = Dbo.queryList(
					"select field_length,field_type from " + Datatable_field_info.TableName + " t1 left join "
						+ Dm_datatable.TableName +
						" t2 on t1.datatable_id = t2.datatable_id where lower(t2.datatable_en_name) = ? and lower(t1.field_en_name) = ?",
					sourcetable.toLowerCase(), sourcecolumn);
				if (maps.isEmpty()) {
					resultmap.put("sourcetype", field_type);
					resultmap.put("targettype", field_type);
					return resultmap;
				} else {
					//加工层原始字段类型
					String DMLfield_type = maps.get(0).get("field_type").toString();
					//加工层原始字段长度
					String DMLfield_length = maps.get(0).get("field_length").toString();
					if (!StringUtils.isEmpty(DMLfield_length)) {
						resultmap.put("field_length", DMLfield_length);
						DMLfield_type = DMLfield_type + "(" + DMLfield_length + ")";
					}
					//记录原始类型
					resultmap.put("sourcetype", DMLfield_type.toLowerCase());
					//转换
					DMLfield_type = transFormColumnType(DMLfield_type, dsl_id);
					resultmap.put("targettype", DMLfield_type.toLowerCase());
					return resultmap;
				}
			} else {
				resultmap.put("sourcetype", field_type);
				resultmap.put("targettype", field_type);
				return resultmap;
			}
			//TODO 之后层级加入 还需要补充
		}
	}

	/**
	 * 根据数据类型转换表 进行字段类型的转换
	 */
	private String transFormColumnType(String column_type, String dsl_id) {
		//如果dsl_id为空，表示不需要转换，在存储血缘关系的时候，需要调用到该方法。
		if (StringUtils.isEmpty(dsl_id)) {
			return column_type;
		}
		//统一小写
		column_type = column_type.toLowerCase();
		//去除（
		if (column_type.contains("(")) {
			column_type = column_type.substring(0, column_type.indexOf("("));
		}
		Data_store_layer data_store_layer = new Data_store_layer();
		data_store_layer.setDsl_id(dsl_id);
		//根据原始字段类型 查询目标类型 去除所有的（）和大小写问题
		List<Type_contrast> type_contrasts = Dbo.queryList(Type_contrast.class,
			"select target_type from " + Type_contrast.TableName + " t1 left join " + Data_store_layer.TableName
				+ " t2 on t1.dtcs_id = t2.dtcs_id " +
				"where t2.dsl_id = ? and  LOWER(  CASE  WHEN position ('(' IN t1.source_type) !=0  THEN substring(t1.source_type,0,position ('(' IN t1.source_type)) "
				+
				"  ELSE t1.source_type  END ) = ?", data_store_layer.getDsl_id(), column_type);
		//如果为空，标识没有记录改字段类型的转换 则返回原有字段类型
		if (!type_contrasts.isEmpty()) {
			Type_contrast type_contrast = type_contrasts.get(0);
			String target_type = type_contrast.getTarget_type();
			target_type = target_type.toLowerCase();
			//去除（
			if (target_type.contains("(")) {
				target_type = target_type.substring(0, target_type.indexOf("("));
			}
			return target_type.toLowerCase();
		} else {
			return column_type;
		}
	}

	@Method(desc = "回显新增加工页面2中记录在数据库中的字段信息",
		logicStep = "1.查询所有字段" +
			"2.判断附加属性是否勾选" +
			"3.返回结果")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "列结构", range = "无限制")
	public List<Map<String, Object>> getColumnFromDatabase(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		//获取所有字段
		List<Map<String, Object>> list = Dbo.queryList("select * from " + Datatable_field_info.TableName +
			" where datatable_id = ? AND end_date in (?,?) order by field_seq", dm_datatable.getDatatable_id(), Constant.MAXDATE, Constant.INITDATE);
		Datatable_field_info datatable_field_info = new Datatable_field_info();
		for (Map<String, Object> map : list) {
			String datatable_field_id = map.get("datatable_field_id").toString();
//			String process_para = map.get("process_para").toString();
//			if (process_para != null) {
//				//如果是数字，把他转成数字类型
//				if (!StringUtils.isEmpty(process_para) && StringUtils.isNumeric(process_para)) {
//					map.put("process_para", Integer.valueOf(process_para));
//				}
//			}
			datatable_field_info.setDatatable_field_id(datatable_field_id);
			//查看 附件属性的字段的勾选情况
			List<Map<String, Object>> list2 = Dbo
				.queryList("select dsla_storelayer from " + Data_store_layer_added.TableName + "" +
						" t1 left join " + Dcol_relation_store.TableName
						+ " t2 on t1.dslad_id = t2.dslad_id where t2.col_id = ? and t2.data_source = ?",
					datatable_field_info.getDatatable_field_id(), StoreLayerDataSource.DM.getCode());
			if (list2 != null) {
				for (Map<String, Object> everymap : list2) {
					String dsla_storelayer = everymap.get("dsla_storelayer").toString();
					map.put(StoreLayerAdded.ofValueByCode(dsla_storelayer), true);
				}
			}
		}
		return list;
	}

	@Method(desc = "回显新增加工页面2中记录所有来源字段",
		logicStep = "回显新增加工页面2中记录所有来源字段")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "列结构", range = "无限制")
	public List<Map<String, Object>> getFromColumnList(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		Optional<Dm_operation_info> dm_operation_infoOptional = Dbo.queryOneObject(Dm_operation_info.class,
			"select execute_sql from " + Dm_operation_info.TableName + " where datatable_id = ? AND end_date in (?,?)",
			dm_datatable.getDatatable_id(), Constant.INITDATE, Constant.MAXDATE);
		//如果有数据就回显
		if (dm_operation_infoOptional.isPresent()) {
			Dm_operation_info dm_operation_info = dm_operation_infoOptional.get();
			String execute_sql = dm_operation_info.getExecute_sql();
			DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql(execute_sql);
			List<String> columnNameList = druidParseQuerySql.parseSelectAliasField();
			List<Map<String, Object>> columnlist = new ArrayList<>();
			//将来源字段的list拼接完成
			for (int i = 0; i < columnNameList.size(); i++) {
				Map<String, Object> map = new HashMap<>();
				map.put("value", columnNameList.get(i));
				map.put("code", i);
				columnlist.add(map);
			}
			return columnlist;
		} else {
			//如果是新增，则没有记录
			return null;
		}
	}


	@Method(desc = "根据加工表ID,获取字段类型的所有类型",
		logicStep = "1.获取所有字段类型" +
			"2.判断默认类型是否包含在所有字段类型中" +
			"3.返回结果")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "查询返回结果集", range = "无限制")
	public List<Map<String, Object>> getAllField_Type(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		//查看存储层目的地
		Optional<Data_store_layer> data_store_layerOptional = Dbo.queryOneObject(Data_store_layer.class,
			"select store_type from " + Data_store_layer.TableName + " t1 left join " +
				Dtab_relation_store.TableName + " t2 on t1.dsl_id = t2.dsl_id " +
				"where t2.tab_id = ? and t2.data_source = ?", dm_datatable.getDatatable_id(),
			StoreLayerDataSource.DM.getCode());
		if (data_store_layerOptional.isPresent()) {
			Data_store_layer data_store_layer = data_store_layerOptional.get();
			String storeType = data_store_layer.getStore_type();
			Validator.notBlank(storeType, "存储类型(Store_type)不能为空");
			String field_type = getDefaultFieldType(storeType);
			//根据存储目的地 查看所有字段类型
			List<Map<String, Object>> targetTypeList = Dbo.queryList("SELECT distinct " +
					"CASE  WHEN position('(' IN t1.target_type) !=0 " +
					"        THEN LOWER(SUBSTR(t1.target_type,0,position('(' IN t1.target_type))) " +
					"        ELSE LOWER(t1.target_type) " +
					"    END AS target_type " +
					"FROM " + Type_contrast.TableName + " t1 LEFT JOIN " + Data_store_layer.TableName
					+ " t2 ON t1.dtcs_id = t2.dtcs_id " +
					"LEFT JOIN " + Dtab_relation_store.TableName + " t3 ON t2.dsl_id=t3.dsl_id" +
					" WHERE t3.tab_id = ? and t3.data_source = ?", dm_datatable.getDatatable_id(),
				StoreLayerDataSource.DM.getCode());
			Map<String, Object> resultmap = new HashMap<>();
			resultmap.put("target_type", field_type);
			//判断 如果list中没有当前类型 则加入
			if (!targetTypeList.contains(resultmap)) {
				targetTypeList.add(resultmap);
			}
			return targetTypeList;
		}
		throw new BusinessSystemException("选择的数据目的地不包含字段类型，请检查");

	}

	/**
	 * 设置一个默认的字段类型 以便于对于多字段合成的字段类型进行初始化
	 */
	private String getDefaultFieldType(String storeType) {
		//TODO 分类讨论 目前只考虑关系性数据库、hive、hbase这三种情况 提出去
		String field_type = "";
		if (storeType.equals(Store_type.DATABASE.getCode())) {
			field_type = "varchar";
		} else if (storeType.equals(Store_type.HIVE.getCode())) {
			field_type = "string";
		} else if (storeType.equals(Store_type.HBASE.getCode())) {
			field_type = "string";
		}
		return field_type;
	}

	private boolean beanContains(List<Datatable_field_info> o1, Datatable_field_info o2) {
		for (Datatable_field_info a : o1) {
			if (EqualsBuilder.reflectionEquals(a, o2,
				"datatable_field_id", "field_desc", "field_seq",
				"remark", "field_process", "process_mapping", "datatable_id",
				"group_mapping", "start_date", "end_date")) {
				return true;
			}
		}
		return false;
	}

	@Method(desc = "保存新增加工2的数据",
		logicStep = "1.检查页面数据合法性" +
			"2.删除相关6张表中的数据" +
			"3.保存数据进入数据库")
	@Param(name = "datatable_field_info", desc = "datatable_field_info", range = "与Datatable_field_info表字段规则一致",
		isBean = true)
	@Param(name = "dm_column_storage", desc = "dm_column_storage", range = "与Dm_column_storage表字段规则一致",
		isBean = true)
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Param(name = "querysql", desc = "querysql", range = "String类型加工查询SQL")
	@Param(name = "hbasesort", desc = "hbasesort", range = "hbaserowkey的排序")
	@Param(name = "pre_partition", desc = "预分区,只有是存储层选择hbase时可能存在",
		range = "预分区键，如 a,b,c 或者是预分区数，如 10", nullable = true)
	public Map<String, Object> addDFInfo(Datatable_field_info[] datatable_field_info, String datatable_id,
	                                     String pre_partition, Dcol_relation_store[] dm_column_storage,
	                                     String querysql, String hbasesort) {
		Map<String, Object> resultmap = new HashMap<>();
		//循环 检查数据合法性
		for (int i = 0; i < datatable_field_info.length; i++) {
			Datatable_field_info df_info = datatable_field_info[i];
			CheckColummn(df_info.getField_en_name(), "字段英文名第" + (i + 1) + "个");
			CheckColummn(df_info.getField_cn_name(), "字段中文名" + (i + 1) + "个");
			CheckColummn(df_info.getField_type(), "字段类型" + (i + 1) + "个");
			CheckColummn(df_info.getField_process(), "字段处理方式" + (i + 1) + "个");
		}
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		// 更新预分区键
		Dbo.execute("update " + Dm_datatable.TableName + " set pre_partition=? where datatable_id=?",
			pre_partition, dm_datatable.getDatatable_id());
		//新增时判断SQL是否存在
		Optional<Dm_operation_info> dm_operation_infoOptional = Dbo.queryOneObject(Dm_operation_info.class,
			"select execute_sql,id,datatable_id,end_date,start_date from " + Dm_operation_info.TableName
				+ " where datatable_id = ? AND end_date = ?",
			dm_datatable.getDatatable_id(), Constant.INITDATE);
		//新增时判断SQL是否存在
		Optional<Dm_operation_info> dm_operation_infoOptional1 = Dbo.queryOneObject(Dm_operation_info.class,
			"select execute_sql,id,datatable_id,end_date,start_date from " + Dm_operation_info.TableName
				+ " where datatable_id = ? AND end_date = ?",
			dm_datatable.getDatatable_id(), Constant.MAXDATE);
		//根据querysql和datatable_field_info获取最终执行的sql
		String execute_sql = getExecute_sql(datatable_field_info, querysql);
		//设置标签 判断是新增 还是 更新
		//没有数据 表示新增 增加sql创建时间
		if (!dm_operation_infoOptional1.isPresent() && !dm_operation_infoOptional.isPresent()) {
			dm_datatable.setDdlc_date(DateUtil.getSysDate());
			dm_datatable.setDdlc_time(DateUtil.getSysTime());
			dm_datatable.update(Dbo.db());
			Dm_operation_info dm_operation_info = new Dm_operation_info();
			dm_operation_info.setId(PrimayKeyGener.getNextId());
			dm_operation_info.setDatatable_id(datatable_id);
			dm_operation_info.setView_sql(querysql);
			dm_operation_info.setExecute_sql(execute_sql);
			dm_operation_info.setStart_date(DateUtil.getSysDate());
			dm_operation_info.setEnd_date(Constant.INITDATE);
			dm_operation_info.add(Dbo.db());
			//保存血缘关系的表，进入数据库
			saveBloodRelationToPGTable(execute_sql, datatable_id);
		}
		//更新时判断SQL是否一致
		else {
			if (dm_operation_infoOptional.isPresent()) {
				Dm_operation_info dm_operation_info = dm_operation_infoOptional.get();
				if (!dm_operation_info.getExecute_sql().equals(execute_sql)) {
					dm_datatable.setDdlc_date(DateUtil.getSysDate());
					dm_datatable.setDdlc_time(DateUtil.getSysTime());
					//更新加工表的SQL修改时间字段
					dm_datatable.update(Dbo.db());
					Dbo.execute("DELETE FROM " + Dm_operation_info.TableName
							+ " WHERE end_date = ? AND datatable_id = ?", Constant.INITDATE,
						dm_operation_info.getDatatable_id());

					//3: 新增此次修改的SQL记录信息
					dm_operation_info.setView_sql(querysql);
					dm_operation_info.setExecute_sql(execute_sql);
					dm_operation_info.setEnd_date(Constant.INITDATE);
					dm_operation_info.setId(PrimayKeyGener.getNextId());
					dm_operation_info.setStart_date(DateUtil.getSysDate());
					dm_operation_info.add(Dbo.db());
					//保存血缘关系的表，进入数据库
					saveBloodRelationToPGTable(execute_sql, datatable_id);
				}
			} else {

				Dm_operation_info dm_operation_info1 = dm_operation_infoOptional1.get();
				if (!dm_operation_info1.getExecute_sql().equals(execute_sql)) {
					dm_datatable.setDdlc_date(DateUtil.getSysDate());
					dm_datatable.setDdlc_time(DateUtil.getSysTime());
					dm_datatable.update(Dbo.db());
					dm_operation_info1.setEnd_date(Constant.INVDATE);
					dm_operation_info1.update(Dbo.db());
					Dm_operation_info dmOperationInfo = new Dm_operation_info();
					dmOperationInfo.setId(PrimayKeyGener.getNextId());
					dmOperationInfo.setDatatable_id(datatable_id);
					dmOperationInfo.setView_sql(querysql);
					dmOperationInfo.setExecute_sql(execute_sql);
					dmOperationInfo.setStart_date(DateUtil.getSysDate());
					dmOperationInfo.setEnd_date(Constant.INITDATE);
					dmOperationInfo.add(Dbo.db());
					//保存血缘关系的表，进入数据库
					saveBloodRelationToPGTable(execute_sql, datatable_id);
				}
			}
		}
		//删除原有数据 因为页面可能会存在修改sql 导致的字段大幅度变动 所以针对更新的逻辑会特别复杂 故采用全删全增的方式
		Dbo.execute(
			"delete from " + Dcol_relation_store.TableName + " where col_id in (select datatable_field_id from " +
				Datatable_field_info.TableName + " where datatable_id = ?) and data_source = ?",
			dm_datatable.getDatatable_id(),
			StoreLayerDataSource.DM.getCode());

		//如果查询不到任务字段信息说明是新增的
		List<Datatable_field_info> dataBaseFields = Dbo.queryList(Datatable_field_info.class,
			"SELECT datatable_id,datatable_field_id,field_cn_name,field_en_name,field_length,field_process,field_seq,field_type,"
				+ "group_mapping,process_mapping,end_date,start_date FROM " + Datatable_field_info.TableName
				+ " where datatable_id = ? AND end_date in (?,?)",
			dm_datatable.getDatatable_id(), Constant.MAXDATE, Constant.INITDATE);

		if (dataBaseFields.size() == 0) {
			//新增字段表
			for (Datatable_field_info df_info : datatable_field_info) {
				//这里说明是新增的
				long datatable_field_id = PrimayKeyGener.getNextId();
				df_info.setDatatable_field_id(datatable_field_id);
				df_info.setDatatable_id(datatable_id);
				df_info.setStart_date(DateUtil.getSysDate());
				df_info.setEnd_date(Constant.INITDATE);
				df_info.add(Dbo.db());
			}
		} else {
			/*
				检查字段是否和此次前端传递的一致
				1: 如果不一致说明进行了修改,则将数据库的字段信息经行关链,然后新增一条
				2: 如果全部没有匹配上,则表示新增了字段
			 */
			if (datatable_field_info.length == 0) {
				throw new BusinessException("没有表字段信息");
			}
			List<Datatable_field_info> webField_infos = Arrays.asList(datatable_field_info);
			//查找不存在数据库中的,说明被删除了
			List<Datatable_field_info> notExists = dataBaseFields.stream()
				.filter(item -> !beanContains(webField_infos, item))
				.collect(Collectors.toList());
			if (notExists.size() != 0) {
				//更新修改的原字段信息为失效
				Assembler assembler = Assembler.newInstance().addSql("UPDATE " + Datatable_field_info.TableName
					+ " SET end_date = ? WHERE end_date in (?,?) AND datatable_id = ?")
					.addParam(Constant.INVDATE).addParam(Constant.MAXDATE).addParam(Constant.INITDATE).addParam(dm_datatable.getDatatable_id());
				assembler.addORParam("field_en_name",
					notExists.stream().map(Datatable_field_info::getField_en_name).toArray(String[]::new), "AND");
				Dbo.execute(assembler.sql(), assembler.params());
			}

			List<Datatable_field_info> exists = webField_infos.stream()
				.filter(item -> !beanContains(dataBaseFields, item))
				.collect(Collectors.toList());
			for (Datatable_field_info add : exists) {
				add.setDatatable_field_id(PrimayKeyGener.getNextId());
				add.setDatatable_id(datatable_id);
				add.setStart_date(DateUtil.getSysDate());
				add.setEnd_date(Constant.INITDATE);
				add.add(Dbo.db());
			}

		}

		//新增 字段存储关系表 即字段勾选了什么附加属性
		for (Dcol_relation_store dc_storage : dm_column_storage) {
			//通过csi_number 来确定字段的位置
			Datatable_field_info datatable_field_info1 = datatable_field_info[dc_storage.getCsi_number().intValue()];
			//设置datatable_field_id 为字段的那个ID
			dc_storage.setCol_id(datatable_field_info1.getDatatable_field_id());
			dc_storage.setData_source(StoreLayerDataSource.DM.getCode());
			dc_storage.add(Dbo.db());
		}
		//排序dc_storage
		JSONArray jsonarray = JSONArray.parseArray(hbasesort);
		//
		List<Map<String, Object>> maps = Dbo
			.queryList("select distinct t1.dslad_id,t2.dsla_storelayer from " + Dcol_relation_store.TableName
					+ " t1 left join " + Data_store_layer_added.TableName + " t2 on t1.dslad_id = t2.dslad_id where col_id in " +
					"(select datatable_field_id from " + Datatable_field_info.TableName
					+ " where datatable_id = ? ) and t1.data_source = ?", dm_datatable.getDatatable_id(),
				StoreLayerDataSource.DM.getCode());
		for (Map<String, Object> everymap : maps) {
			String dslad_id = everymap.get("dslad_id").toString();
			String dsla_storelayer = everymap.get("dsla_storelayer").toString();
			Dcol_relation_store dcs = new Dcol_relation_store();
			dcs.setDslad_id(dslad_id);
			//如果是rowkey的话 排序的时候 需要根据hbasesort来排序
			if (dsla_storelayer.equals(StoreLayerAdded.RowKey.getCode())) {
				for (int i = 0; i < jsonarray.size(); i++) {
					JSONObject jsonObject = jsonarray.getJSONObject(i);
					String field_en_name = jsonObject.getString("field_en_name");
					Datatable_field_info datatable_field_info1 = new Datatable_field_info();
					datatable_field_info1.setField_en_name(field_en_name);
					Optional<Dcol_relation_store> dm_column_storageOptional = Dbo.queryOneObject(Dcol_relation_store.class,
						"select * from " + Dcol_relation_store.TableName + " where col_id = " +
							"(select datatable_field_id from " + Datatable_field_info.TableName
							+ " where datatable_id = ? and field_en_name = ? )" +
							" and dslad_id = ? and data_source = ?",
						dm_datatable.getDatatable_id(), datatable_field_info1.getField_en_name(), dcs.getDslad_id(),
						StoreLayerDataSource.DM.getCode());
					//如果有数据
					if (dm_column_storageOptional.isPresent()) {
						Dcol_relation_store dc_storage = dm_column_storageOptional.get();
						dc_storage.setCsi_number(String.valueOf(i));
						updatebean(dc_storage);
					} else {
						throw new BusinessSystemException("查询Dm_column_storage表不存在数据，错误，请检查");
					}
				}
			}
			//如果不是rowkey 那么排序的时候 只需简单排序即可
			else {
				List<Dcol_relation_store> dm_column_storages = Dbo.queryList(Dcol_relation_store.class,
					"select * from " + Dcol_relation_store.TableName + " where col_id in " +
						"(select datatable_field_id from " + Datatable_field_info.TableName
						+ " where datatable_id = ? ) and dslad_id = ? and data_source = ? order by csi_number",
					dm_datatable.getDatatable_id(), dcs.getDslad_id(), StoreLayerDataSource.DM.getCode());
				for (int i = 0; i < dm_column_storages.size(); i++) {
					Dcol_relation_store dc_storage = dm_column_storages.get(i);
					dc_storage.setCsi_number(String.valueOf(i));
					dc_storage.update(Dbo.db());
				}
			}
		}
//		saveBloodRelationToPGTable(querysql, datatable_id);
		return resultmap;
	}

	@Method(desc = "根据页面选择的列的信息和查询的sql获取最终执行的sql",
		logicStep = "1.根据datatable_field_info信息拼接所有查询列的信息" +
			"2.遍历所有为分组映射的字段" +
			"3.判断，如果有分组映射返回分组映射拼接的sql,没有则返回根据查询列处理拼接的sql。")
	@Param(name = "datatable_field_info", desc = "datatable_field_info", range = "与Datatable_field_info表字段规则一致",
		isBean = true)
	@Param(name = "querySql", desc = "querySql", range = "String类型加工查询SQL")
	public String getExecute_sql(Datatable_field_info[] datatable_field_info, String querySql) {
		//解析querySql,获取查询sql的别名，加查询列的map
		DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql(querySql);
		Map<String, String> selectColumnMap = druidParseQuerySql.getSelectColumnMap();
		boolean flag = true;
		//1.根据datatable_field_info信息拼接所有查询列的信息
		StringBuilder sb = new StringBuilder(1024);
		sb.append("SELECT ");
		for (Datatable_field_info field_info : datatable_field_info) {
			ProcessType processType = ProcessType.ofEnumByCode(field_info.getField_process());
			if (ProcessType.DingZhi == processType) {
				sb.append(field_info.getProcess_mapping()).append(" as ")
					.append(field_info.getField_en_name()).append(",");
			} else if (ProcessType.ZiZeng == processType) {
				//这里拼接的sql不处理自增的，后台会根据自增的数据库类型去拼接对应的自增函数
				logger.info("自增不拼接字段");
			} else if (ProcessType.YingShe == processType) {
				sb.append(selectColumnMap.get(field_info.getProcess_mapping().toUpperCase())).append(" as ")
					.append(field_info.getField_en_name()).append(",");
			} else if (ProcessType.HanShuYingShe == processType) {
				//TODO 这里如果函数映射包含函数映射会有问题，待讨论
				sb.append(field_info.getProcess_mapping()).append(" as ")
					.append(field_info.getField_en_name()).append(",");
			} else if (ProcessType.FenZhuYingShe == processType && flag) {
				//分组映射，当前预览的sql只取第一个查询列的值作为表字段的位置，多个作业分别查询不同的列加上分组的列
				sb.append("#{HyrenFenZhuYingShe}").append(",");
				flag = false;
			} else if (!flag) {
				logger.info("第二次进来分组映射不做任何操作，跳过");
			} else {
				throw new BusinessException("错误的字段映射规则");
			}
		}
		String selectSql = sb.toString();
		sb.delete(0, sb.length());
		StringBuilder groupSql = new StringBuilder();
		//2.遍历所有为分组映射的字段
		for (Datatable_field_info field_info : datatable_field_info) {
			//为分组映射
			if (ProcessType.FenZhuYingShe == ProcessType.ofEnumByCode(field_info.getField_process())) {
				String replacement = field_info.getProcess_mapping() + " as " + field_info.getField_en_name();
				List<String> split = StringUtil.split(field_info.getGroup_mapping(), "=");
				sb.append(selectSql.replace("#{HyrenFenZhuYingShe}", replacement));
				sb.append("'").append(split.get(1)).append("'").append(" as ").append(split.get(0)).append(" FROM ");
				//先格式化查询的sql,替换掉原始查询SQL的select部分
				String execute_sql = SQLUtils.format(querySql, JdbcConstants.ORACLE).replace(
					druidParseQuerySql.getSelectSql(), sb.toString());
				sb.delete(0, sb.length());
				groupSql.append(execute_sql).append(" union all ");
			}
		}
		//3.判断，如果有分组映射返回分组映射拼接的sql,没有则返回根据查询列处理拼接的sql。
		if (groupSql.length() > 0) {
			return groupSql.delete(groupSql.length() - " union all ".length(), groupSql.length()).toString();
		} else {
			selectSql = selectSql.substring(0, selectSql.length() - 1) + " FROM ";
			//先格式化查询的sql,替换掉原始查询SQL的select部分
			return SQLUtils.format(querySql, JdbcConstants.HIVE).replace(
				druidParseQuerySql.getSelectSql(), selectSql);
		}
	}

	/**
	 * 保存血缘关系到PGSQL中的表里
	 */
	private void saveBloodRelationToPGTable(String querysql, String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		Dbo.execute("delete from " + Own_source_field.TableName + " where own_dource_table_id in " +
			"(select own_dource_table_id from dm_datatable_source where datatable_id =  ?)", dm_datatable.getDatatable_id());
		Dbo.execute("delete from " + Dm_datatable_source.TableName + " where datatable_id = ?",
			dm_datatable.getDatatable_id());
		Dbo.execute("delete from " + Dm_etlmap_info.TableName + " where datatable_id = ?", dm_datatable.getDatatable_id());
		DruidParseQuerySql dpqs = new DruidParseQuerySql();
		HashMap<String, Object> bloodRelationMap = dpqs.getBloodRelationMap(querysql);
		Iterator<Map.Entry<String, Object>> iterator = bloodRelationMap.entrySet().iterator();
		Map<String, Object> tableMap = new HashMap<>();
		//重新整理数据结构，原本的map key是目标字段 新的tableMap的数据结构key为来源表的表名
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			String columnname = entry.getKey();
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) entry.getValue();
			for (HashMap<String, Object> map : list) {
				String sourcecolumn = map.get(DruidParseQuerySql.sourcecolumn).toString().toLowerCase();
				String sourcetable = map.get(DruidParseQuerySql.sourcetable).toString().toLowerCase();
				List<Map<String, Object>> templist = new ArrayList<>();
				if (!tableMap.containsKey(sourcetable)) {
					tableMap.put(sourcetable, templist);
				} else {
					templist = (ArrayList<Map<String, Object>>) tableMap.get(sourcetable);
				}
				Map<String, Object> tempmap = new HashMap<>();
				tempmap.put(TargetColumn, columnname.toLowerCase());
				tempmap.put(SourceColumn, sourcecolumn.toLowerCase());
				templist.add(tempmap);
				tableMap.put(sourcetable, templist);
			}
		}
		iterator = tableMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			String tablename = entry.getKey();
			String dataSourceType = "";
			List<LayerBean> layerByTable = ProcessingData.getLayerByTable(tablename, Dbo.db());
			//TODO 如果所涉及到的表找不到层级 则使用UDL(自定义层）
			if (layerByTable.isEmpty()) {
				dataSourceType = DataSourceType.UDL.getCode();
			} else {
				dataSourceType = layerByTable.get(0).getDst();
			}
			//存储血缘关系表1
			Dm_datatable_source dm_datatable_source = new Dm_datatable_source();
			long own_dource_table_id = PrimayKeyGener.getNextId();
			dm_datatable_source.setOwn_dource_table_id(own_dource_table_id);
			dm_datatable_source.setDatatable_id(datatable_id);
			dm_datatable_source.setOwn_source_table_name(tablename);
			dm_datatable_source.setSource_type(dataSourceType);
			dm_datatable_source.add(Dbo.db());
			List<Map<String, Object>> templist = (ArrayList<Map<String, Object>>) tableMap.get(tablename.toLowerCase());
			for (Map<String, Object> map : templist) {
				//获取目标字段名
				String targetcolumn = map.get(TargetColumn).toString();
				//获取来源字段名
				String sourcecolumn = map.get(SourceColumn).toString();
				Dm_etlmap_info dm_etlmap_info = new Dm_etlmap_info();
				dm_etlmap_info.setEtl_id(PrimayKeyGener.getNextId());
				dm_etlmap_info.setDatatable_id(datatable_id);
				dm_etlmap_info.setOwn_dource_table_id(own_dource_table_id);
				dm_etlmap_info.setSourcefields_name(sourcecolumn);
				dm_etlmap_info.setTargetfield_name(targetcolumn);
				//循环存储血缘关系表2
				dm_etlmap_info.add(Dbo.db());
				Own_source_field own_source_field = new Own_source_field();
				own_source_field.setOwn_dource_table_id(own_dource_table_id);
				own_source_field.setOwn_field_id(PrimayKeyGener.getNextId());
				own_source_field.setField_name(sourcecolumn);
				String target_type = getDefaultFieldType(Store_type.DATABASE.getCode());
				Map<String, String> fieldType = getFieldType(tablename, sourcecolumn, target_type, "");
				String sourcetype = fieldType.get("sourcetype");
				own_source_field.setField_type(sourcetype);
				//循环存储血缘关系表3
				own_source_field.add(Dbo.db());
			}
		}
	}

	@Method(desc = "根据加工表ID,获取SQL回显",
		logicStep = "返回查询结果")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "查询返回结果集", range = "无限制")
	public String getQuerySql(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		Optional<Dm_operation_info> dm_operation_infoOptional1 = Dbo.queryOneObject(Dm_operation_info.class,
			"select view_sql as execute_sql from " + Dm_operation_info.TableName + " t1 where " +
				"datatable_id = ? AND end_date = ?", dm_datatable.getDatatable_id(), Constant.INITDATE);
		Optional<Dm_operation_info> dm_operation_infoOptional = Dbo.queryOneObject(Dm_operation_info.class,
			"select view_sql as execute_sql from " + Dm_operation_info.TableName + " t1 where " +
				"datatable_id = ? AND end_date = ?", dm_datatable.getDatatable_id(), Constant.MAXDATE);
		if (dm_operation_infoOptional1.isPresent()) {
			Dm_operation_info dm_operation_info1 = dm_operation_infoOptional1.get();
			return dm_operation_info1.getExecute_sql();
		} else {
			return dm_operation_infoOptional.map(Dm_operation_info::getExecute_sql).orElse(null);
		}
	}

	@Method(desc = "根据加工表ID，判断是否是进入Hbase的目的地",
		logicStep = "判断目的地是否为hbase")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Return(desc = "返回true或者false", range = "无限制")
	public Boolean getIfHbase(String datatable_id) {
		Map<String, Object> map = new HashMap<>();
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		OptionalLong optionalLong = Dbo.queryNumber(
			"select count(*) from " + Data_store_layer.TableName + " t1 left join " + Dtab_relation_store.TableName + " t2 "
				+
				"on t1.dsl_id = t2.dsl_id where t2.tab_id = ? and t1.store_type = ? and t2.data_source = ?",
			dm_datatable.getDatatable_id(), Store_type.HBASE.getCode(), StoreLayerDataSource.DM.getCode());
		return optionalLong.isPresent() && optionalLong.getAsLong() > 0;
	}

	@Method(desc = "回显hbase的rowkey排序",
		logicStep = "1.查询结果" +
			"2.与页面选中的字段名称进行匹配，如果匹配到，就顺序放在前面，如果匹配不到，就顺序放到后面" +
			"3.返回结果")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Param(name = "hbasesort", desc = "hbasesort", range = "hbaserowkey的排序")
	@Return(desc = "排序完成后的hbasesort", range = "无限制")
	public List<Map<String, Object>> sortHbae(String datatable_id, String hbasesort) {
		JSONArray jsonArray = JSONArray.parseArray(hbasesort);
		List<String> enNameList = new ArrayList<>();
		//获取页面传来的 勾选中的rowkey的值，并将其放入list中
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String field_en_name = jsonObject.getString("field_en_name");
			enNameList.add(field_en_name);
		}
		Datatable_field_info datatable_field_info = new Datatable_field_info();
		datatable_field_info.setDatatable_id(datatable_id);
		//利用list.add的有序性 将页面选中的与数据库中记录的rowkey顺序相结合 回显选中的rowkey
		//查询数据库中已有的选中的rowkey字段 并根据序号进行排序
		List<Object> objects = Dbo.queryOneColumnList("SELECT t3.field_en_name FROM " + Dcol_relation_store.TableName +
				" t1 LEFT JOIN " + Data_store_layer_added.TableName + " t2 ON t1.dslad_id = t2.dslad_id " +
				" LEFT JOIN " + Datatable_field_info.TableName + " t3 ON t1.col_id = t3.datatable_field_id " +
				" WHERE t2.dsla_storelayer = ? AND t1.col_id IN " +
				" ( SELECT datatable_field_id FROM Datatable_field_info WHERE datatable_id = ?) and t1.data_source = ?" +
				" order by csi_number", StoreLayerAdded.RowKey.getCode(), datatable_field_info.getDatatable_id(),
			StoreLayerDataSource.DM.getCode());
		List<Map<String, Object>> resultlist = new ArrayList<>();
		//遍历库中已有的rowkey字段
		for (Object object : objects) {
			Map<String, Object> resultmap = new HashMap<>();
			//获取字段名称
			String field_en_name = object.toString();
			//如果页面选中的字段，包含库中存储的字段 就将改字段加入
			if (enNameList.contains(field_en_name)) {
				resultmap.put("field_en_name", field_en_name);
				resultlist.add(resultmap);
				//从页面选中的rowkey list中去除改字段
				enNameList.remove(field_en_name);
			}
		}
		//再循环页面选中的字段 加入到resultlist的最后
		for (String field_en_name : enNameList) {
			Map<String, Object> resultmap = new HashMap<>();
			resultmap.put("field_en_name", field_en_name);
			resultlist.add(resultmap);
//			enNameList.remove(field_en_name);
		}
		return resultlist;
	}


	@Method(desc = "获取树的数据信息",
		logicStep = "1.声明获取到 zTreeUtil 的对象" +
			"2.设置树实体" +
			"3.调用ZTreeUtil的getTreeDataInfo获取treeData的信息")
	@Return(desc = "树数据Map信息", range = "无限制")
	public List<Node> getTreeDataInfo() {
		//配置树不显示文件采集的数据
		TreeConf treeConf = new TreeConf();
		treeConf.setShowFileCollection(Boolean.FALSE);
		//根据源菜单信息获取节点数据列表
		List<Map<String, Object>> dataList = TreeNodeInfo.getTreeNodeInfo(TreePageSource.MARKET, getUser(), treeConf);
		return NodeDataConvertedTreeList.dataConversionTreeInfo(dataList);
	}


	@Method(desc = "树上的展示根据表名,返回源表名和全表字段名",
		logicStep = "返回查询结果")
	@Param(name = "source", desc = "source", range = "String类型表来源")
	@Param(name = "id", desc = "id", range = "String类型id")
	@Return(desc = "查询返回结果集", range = "无限制")
	public Map<String, Object> queryAllColumnOnTableName(String source, String id) {
		Map<String, Object> resultmap = new HashMap<>();
		if (source.equals(DataSourceType.DCL.getCode())) {
			Data_store_reg data_store_reg = new Data_store_reg();
			data_store_reg.setFile_id(id);
			List<Map<String, Object>> maps = Dbo.queryList(
				"select column_name as columnname,column_type as columntype,false as selectionstate from "
					+ Table_column.TableName +
					" t1 left join " + Data_store_reg.TableName
					+ " t2 on t1.table_id = t2.table_id where t2.file_id = ? and upper(column_name) not in (?,?,?,?,?,?)",
				data_store_reg.getFile_id(),
				Constant.SDATENAME, Constant.EDATENAME, Constant.MD5NAME, Constant.HYREN_OPER_DATE, Constant.HYREN_OPER_TIME,
				Constant.HYREN_OPER_PERSON);
			resultmap.put("columnresult", maps);
			List<Map<String, Object>> tablenamelist = Dbo
				.queryList("select hyren_name as tablename from " + Data_store_reg.TableName + " where file_id = ?",
					data_store_reg.getFile_id());
			if (tablenamelist.isEmpty()) {
				throw new BusinessSystemException("查询表data_store_reg错误，没有数据");
			}
			resultmap.put("tablename", tablenamelist.get(0).get("tablename"));
			return resultmap;
		} else if (source.equals(DataSourceType.DML.getCode())) {
			Datatable_field_info datatable_field_info = new Datatable_field_info();
			datatable_field_info.setDatatable_id(id);
			List<Map<String, Object>> maps = Dbo.queryList(
				"select field_en_name as columnname,field_type as columntype,false as selectionstate from "
					+ Datatable_field_info.TableName +
					" where datatable_id = ? and upper(field_en_name) not in (?,?,?) AND end_date = ?",
				datatable_field_info.getDatatable_id(), Constant.SDATENAME, Constant.EDATENAME, Constant.MD5NAME,
				Constant.MAXDATE);
			resultmap.put("columnresult", maps);
			List<Map<String, Object>> tablenamelist = Dbo.queryList(
				"select datatable_en_name as tablename  from " + Dm_datatable.TableName + " where datatable_id = ?",
				datatable_field_info.getDatatable_id());
			if (tablenamelist.isEmpty()) {
				throw new BusinessSystemException("查询Dm_datatable表错误，没有数据");
			}
			resultmap.put("tablename", tablenamelist.get(0).get("tablename"));
			return resultmap;
		} else if (source.equals(DataSourceType.UDL.getCode())) {
			Dq_table_info dq_table_info = UDLDataQuery.getUDLTableInfo(id);
			String table_name = dq_table_info.getTable_name();
			//查询表字段信息
			List<Map<String, Object>> column_list = Dbo.queryList(
				"select column_name as columnname," +
					" column_type AS columntype,false as selectionstate " +
					" FROM " + Dq_table_column.TableName + " WHERE table_id=?",
				dq_table_info.getTable_id());
			resultmap.put("tablename", table_name);
			resultmap.put("columnresult", column_list);
			return resultmap;
		}
		//TODO 新的层加进来后 还需要补充
		return null;
	}


	@Method(desc = "执行加工作业",
		logicStep = "立即执行")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表ID")
	@Param(name = "date", desc = "date", range = "String类型跑批日期")
	@Param(name = "parameter", desc = "parameter", range = "动态参数", nullable = true)
	public void excutMartJob(String datatable_id, String date, String parameter) {
		try {
			MainClass.run(datatable_id, date, parameter);
		} catch (Throwable e) {
			throw new AppSystemException(e);
		}
	}

	@Method(desc = "查询所有作业调度工程",
		logicStep = "返回查询结果g")
	@Return(desc = "查询返回结果集", range = "无限制")
	public List<Etl_sys> queryAllEtlSys() {
		return Dbo.queryList(Etl_sys.class, "SELECT * from " + Etl_sys.TableName);
	}


	@Method(desc = "查询作业调度工程下的所有任务",
		logicStep = "返回查询结果")
	@Param(name = "etl_sys_cd", desc = "etl_sys_cd", range = "String类型作业调度工程主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public List<Etl_sub_sys_list> queryEtlTaskByEtlSys(String etl_sys_cd) {
		Etl_sys etl_sys = new Etl_sys();
		etl_sys.setEtl_sys_cd(etl_sys_cd);
		return Dbo.queryList(Etl_sub_sys_list.class, "select * from " + Etl_sub_sys_list.TableName + " where etl_sys_cd = ?",
			etl_sys.getEtl_sys_cd());
	}

	@Method(desc = "控制响应头下载工程的hrds信息",
		logicStep = "下载加工工程")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "String类型加工工程主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public void downloadMart(String data_mart_id) {
		String fileName = data_mart_id + ".hrds";
		try (OutputStream out = ResponseUtil.getResponse().getOutputStream()) {
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
			// 6.创建输出流
			//2.通过文件id获取文件的 byte
			byte[] bye = getdownloadFile(data_mart_id);
			//3.写入输出流，返回结果
			out.write(bye);
			out.flush();
		} catch (Exception e) {
			logger.error(e);
			throw new BusinessException("加工工程下载错误" + e.getMessage());
		}
	}


	@Method(desc = "控制响应头下载加工表的excel信息",
		logicStep = "")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public void downloadDmDatatable(String datatable_id) {
		String fileName = datatable_id + ".xlsx";
		try (OutputStream out = ResponseUtil.getResponse().getOutputStream();
		     XSSFWorkbook workbook = new XSSFWorkbook()) {
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
			generatexlsx(workbook, datatable_id);
			workbook.write(out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("加工数据表下载错误");
		}
	}

	/**
	 * 生成导出每张表的excel
	 */
	private void generatexlsx(XSSFWorkbook workbook, String datatable_id) {
		//设置背景高亮为黄色
		XSSFColor xssfColor = new XSSFColor(Color.YELLOW);
//        XSSFWorkbook workbook = new XSSFWorkbook();
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		List<Dm_datatable> dm_datatables = Dbo.queryList(Dm_datatable.class,
			"select * from " + Dm_datatable.TableName + " where datatable_id = ?", dm_datatable.getDatatable_id());
		if (dm_datatables.isEmpty()) {
			throw new BusinessSystemException("查询表dm_datatables错误，没有数据，请检查");
		}
		dm_datatable = dm_datatables.get(0);
		XSSFSheet sheet1 = workbook.createSheet("sheet1");
		//第一部分
		sheet1.createRow(0).createCell(0).setCellValue("基本设置");
		//合并单元格
		CellRangeAddress region = new CellRangeAddress(0, 0, 0, 1);
		sheet1.addMergedRegion(region);
		//设置高亮
		sheet1.getRow(0).getCell(0).getCellStyle().setFillForegroundColor(xssfColor);
		sheet1.createRow(1).createCell(0).setCellValue("表英文名");
		sheet1.getRow(1).createCell(1).setCellValue(dm_datatable.getDatatable_en_name());
		sheet1.createRow(2).createCell(0).setCellValue("表中文名");
		sheet1.getRow(2).createCell(1).setCellValue(dm_datatable.getDatatable_cn_name());
		sheet1.createRow(3).createCell(0).setCellValue("表描述");
		sheet1.getRow(3).createCell(1).setCellValue(dm_datatable.getDatatable_desc());
		sheet1.createRow(4).createCell(0).setCellValue("执行引擎");
		sheet1.getRow(4).createCell(1).setCellValue(SqlEngine.ofValueByCode(dm_datatable.getSql_engine()));
		//设置设置sqlengine的下拉框的下拉框
		String[] sqlenginesubjects = new String[SqlEngine.values().length];
		for (int i = 0; i < SqlEngine.values().length; i++) {
			sqlenginesubjects[i] = SqlEngine.values()[i].getValue();
		}
		addValidationData(sheet1, sqlenginesubjects, 4, 1);
		sheet1.createRow(5).createCell(0).setCellValue("进数方式");
		sheet1.getRow(5).createCell(1).setCellValue(StorageType.ofValueByCode(dm_datatable.getStorage_type()));
		//设置StorageType的下拉框
		String[] storagettypesubjects = new String[StorageType.values().length];
		for (int i = 0; i < StorageType.values().length; i++) {
			storagettypesubjects[i] = StorageType.values()[i].getValue();
		}
		addValidationData(sheet1, storagettypesubjects, 5, 1);
		sheet1.createRow(6).createCell(0).setCellValue("数据存储方式");
		sheet1.getRow(6).createCell(1).setCellValue(TableStorage.ofValueByCode(dm_datatable.getTable_storage()));
		//设置TableStorage的下拉框
		String[] tablestoragesubjects = new String[TableStorage.values().length];
		for (int i = 0; i < TableStorage.values().length; i++) {
			tablestoragesubjects[i] = TableStorage.values()[i].getValue();
		}
		addValidationData(sheet1, tablestoragesubjects, 6, 1);
		sheet1.createRow(7).createCell(0).setCellValue("数据生命周期");
		sheet1.getRow(7).createCell(1).setCellValue(TableLifeCycle.ofValueByCode(dm_datatable.getDatatable_lifecycle()));
		//设置TableLifeCycle的下拉框
		String[] tablelifecyclesubjects = new String[TableLifeCycle.values().length];
		for (int i = 0; i < TableLifeCycle.values().length; i++) {
			tablelifecyclesubjects[i] = TableLifeCycle.values()[i].getValue();
		}
		addValidationData(sheet1, tablelifecyclesubjects, 7, 1);
		sheet1.createRow(8).createCell(0).setCellValue("数据表到期日期");
		sheet1.getRow(8).createCell(1).setCellValue(dm_datatable.getDatatable_due_date());
		//第二部分
		sheet1.createRow(10).createCell(0).setCellValue("数据目的地");
		//合并单元格
		region = new CellRangeAddress(10, 10, 0, 4);
		sheet1.addMergedRegion(region);
		//设置高亮
		sheet1.getRow(10).getCell(0).getCellStyle().setFillBackgroundColor(xssfColor);
		sheet1.createRow(11).createCell(0).setCellValue("选择");
		sheet1.getRow(11).createCell(1).setCellValue("名称");
		sheet1.getRow(11).createCell(2).setCellValue("存储类型");
		sheet1.getRow(11).createCell(3).setCellValue("备注");
		sheet1.getRow(11).createCell(4).setCellValue("hadoop客户端");
		sheet1.getRow(11).createCell(5).setCellValue("存储层配置信息");
		List<Map<String, Object>> maps = Dbo.queryList("SELECT t1.dsl_id,dsl_name,store_type,is_hadoopclient,dsl_remark," +
			" string_agg(t2.storage_property_key || ':' || t2.storage_property_val,'@;@') as configure FROM " +
			Data_store_layer.TableName + " t1 LEFT JOIN " + Data_store_layer_attr.TableName +
			" t2 ON t1.dsl_id = t2.dsl_id  group by t1.dsl_id,dsl_name,store_type,is_hadoopclient,dsl_remark");
		int count = maps.size();
		//设置IsFlag的下拉框
		String[] isflagsubjects = new String[IsFlag.values().length];
		for (int i = 0; i < IsFlag.values().length; i++) {
			isflagsubjects[i] = IsFlag.values()[i].getValue();
		}
		for (int i = 0; i < maps.size(); i++) {
			Map<String, Object> stringObjectMap = maps.get(i);
			String dsl_id = stringObjectMap.get("dsl_id").toString();
			String dsl_name = stringObjectMap.get("dsl_name").toString();
			String store_type = stringObjectMap.get("store_type").toString();
			String dsl_remark =
				stringObjectMap.get("dsl_remark") == null ? "" : stringObjectMap.get("dsl_remark").toString();
			String is_hadoopclient = stringObjectMap.get("is_hadoopclient").toString();
			String configure = stringObjectMap.get("configure").toString();
			addValidationData(sheet1, isflagsubjects, 12 + i, 0);
			Dtab_relation_store dm_relation_datatable = new Dtab_relation_store();
			dm_relation_datatable.setDsl_id(dsl_id);
			//查询是否当前单元格为是
			//这里本来想弄一个单选框的，但是查看了apache.poi 到2020.4.26，没有发现有提供单选框的组件，于是放弃
			List<Dtab_relation_store> dm_relation_datatables = Dbo.queryList(Dtab_relation_store.class,
				"select * from " + Dtab_relation_store.TableName + " where dsl_id = ? and tab_id = ? and data_source=?"
				, dm_relation_datatable.getDsl_id(), dm_datatable.getDatatable_id(), StoreLayerDataSource.DM.getCode());
			if (dm_relation_datatables.isEmpty()) {
				sheet1.createRow(12 + i).createCell(0).setCellValue(IsFlag.Fou.getValue());
			} else {
				sheet1.createRow(12 + i).createCell(0).setCellValue(IsFlag.Shi.getValue());
			}
			sheet1.getRow(12 + i).createCell(1).setCellValue(dsl_name);
			sheet1.getRow(12 + i).createCell(2).setCellValue(Store_type.ofValueByCode(store_type));
			sheet1.getRow(12 + i).createCell(3).setCellValue(dsl_remark);
			sheet1.getRow(12 + i).createCell(4).setCellValue(IsFlag.ofValueByCode(is_hadoopclient));
			//根据sql拼接中特殊的@;@部分进行替换
			sheet1.getRow(12 + i).createCell(5).setCellValue(configure.replace("@;@", "\n"));
			//合并单元格
			region = new CellRangeAddress(12 + i, 12 + i, 5, 8);
			sheet1.addMergedRegion(region);
		}
		//第三部分
		List<Dm_operation_info> dm_operation_infos = Dbo.queryList(Dm_operation_info.class,
			"select execute_sql,view_sql from " + Dm_operation_info.TableName + " where datatable_id = ?",
			dm_datatable.getDatatable_id());
		if (dm_operation_infos.isEmpty()) {
			throw new BusinessSystemException("查询表Dm_operation_info错误，没有数据，请检查");
		}
		// 预览sql语句
		String view_sql = dm_operation_infos.get(0).getView_sql();
		sheet1.createRow(13 + count).createCell(0).setCellValue("view_sql");
		//设置高亮
		sheet1.getRow(13 + count).getCell(0).getCellStyle().setFillBackgroundColor(xssfColor);
		sheet1.getRow(13 + count).createCell(1).setCellValue(view_sql);
		//合并单元格
		region = new CellRangeAddress(13 + count, 13 + count, 1, 4);
		sheet1.addMergedRegion(region);
		// 执行sql语句
		String execute_sql = dm_operation_infos.get(0).getExecute_sql();
		sheet1.createRow(14 + count).createCell(0).setCellValue("execute_sql");
		sheet1.getRow(14 + count).getCell(0).getCellStyle().setFillBackgroundColor(xssfColor);
		sheet1.getRow(14 + count).createCell(1).setCellValue(execute_sql);
		//合并单元格
		region = new CellRangeAddress(14 + count, 14 + count, 1, 4);
		sheet1.addMergedRegion(region);
		// 前置处理
		List<Dm_relevant_info> dm_relevant_infos = Dbo
			.queryList(Dm_relevant_info.class, "select * from " + Dm_relevant_info.TableName + " where datatable_id = ?",
				dm_datatable.getDatatable_id());
		sheet1.createRow(15 + count).createCell(0).setCellValue("前置处理");
		//合并单元格
		region = new CellRangeAddress(15 + count, 15 + count, 1, 4);
		sheet1.addMergedRegion(region);
		if (!dm_relevant_infos.isEmpty()) {
			if (!StringUtils.isEmpty(dm_relevant_infos.get(0).getPre_work())) {
				sheet1.getRow(15 + count).createCell(1).setCellValue(dm_relevant_infos.get(0).getPre_work());
			}
		}
		// 后置处理
		sheet1.createRow(16 + count).createCell(0).setCellValue("后置处理");
		//合并单元格
		region = new CellRangeAddress(16 + count, 16 + count, 1, 4);
		sheet1.addMergedRegion(region);
		if (!dm_relevant_infos.isEmpty()) {
			if (!StringUtils.isEmpty(dm_relevant_infos.get(0).getPost_work())) {
				sheet1.getRow(16 + count).createCell(1).setCellValue(dm_relevant_infos.get(0).getPost_work());
			}
		}
		//第四部分
		List<Datatable_field_info> datatable_field_infos = Dbo.queryList(Datatable_field_info.class,
			"select * from " + Datatable_field_info.TableName + " where datatable_id = ?", dm_datatable.getDatatable_id());
		sheet1.createRow(18 + count).createCell(0).setCellValue("字段信息");
		sheet1.getRow(18 + count).getCell(0).getCellStyle().setFillBackgroundColor(xssfColor);
		sheet1.createRow(19 + count).createCell(0).setCellValue("序号");
		sheet1.getRow(19 + count).createCell(1).setCellValue("英文名");
		sheet1.getRow(19 + count).createCell(2).setCellValue("中文名");
		sheet1.getRow(19 + count).createCell(3).setCellValue("类型");
		sheet1.getRow(19 + count).createCell(4).setCellValue("长度");
		sheet1.getRow(19 + count).createCell(5).setCellValue("处理方式");
		sheet1.getRow(19 + count).createCell(6).setCellValue("映射规则mapping");
		sheet1.getRow(19 + count).createCell(7).setCellValue("分组映射对应规则");
		for (int i = 0; i < StoreLayerAdded.values().length; i++) {
			sheet1.getRow(19 + count).createCell(8 + i).setCellValue(StoreLayerAdded.values()[i].getValue());
		}
		//设置ProcessType的下拉框
		String[] processtypesubjects = new String[ProcessType.values().length];
		for (int i = 0; i < ProcessType.values().length; i++) {
			processtypesubjects[i] = ProcessType.values()[i].getValue();
		}
		for (int i = 0; i < datatable_field_infos.size(); i++) {
			Datatable_field_info datatable_field_info = datatable_field_infos.get(i);
			//查询字段的附加属性是否为是
			List<Data_store_layer_added> data_store_layer_addeds = Dbo.queryList(Data_store_layer_added.class,
				"select dsla_storelayer from " + Data_store_layer_added.TableName + " t1 left join "
					+ Dcol_relation_store.TableName +
					" t2 on t1.dslad_id = t2.dslad_id where col_id = ? and t2.data_source = ?",
				datatable_field_info.getDatatable_field_id(), StoreLayerDataSource.DM.getCode());
			List<String> dsla_storelayers = new ArrayList<>();
			for (Data_store_layer_added data_store_layer_added : data_store_layer_addeds) {
				dsla_storelayers.add(data_store_layer_added.getDsla_storelayer());
			}
			sheet1.createRow(20 + count + i).createCell(0).setCellValue((i + 1));
			sheet1.getRow(20 + count + i).createCell(1).setCellValue(datatable_field_info.getField_en_name());
			sheet1.getRow(20 + count + i).createCell(2).setCellValue(datatable_field_info.getField_cn_name());
			sheet1.getRow(20 + count + i).createCell(3).setCellValue(datatable_field_info.getField_type());
			sheet1.getRow(20 + count + i).createCell(4).setCellValue(datatable_field_info.getField_length());
			sheet1.getRow(20 + count + i).createCell(5)
				.setCellValue(ProcessType.ofValueByCode(datatable_field_info.getField_process()));
			addValidationData(sheet1, processtypesubjects, 19 + count + i, 5);
			if (ProcessType.YingShe == ProcessType.ofEnumByCode(datatable_field_info.getField_process())) {
				sheet1.getRow(20 + count + i).createCell(6).setCellValue(datatable_field_info.getProcess_mapping());
			}
			if (ProcessType.FenZhuYingShe == ProcessType.ofEnumByCode(datatable_field_info.getField_process())) {
				sheet1.getRow(20 + count + i).createCell(7).setCellValue(datatable_field_info.getGroup_mapping());
			}
			for (int j = 0; j < StoreLayerAdded.values().length; j++) {
				addValidationData(sheet1, isflagsubjects, 20 + count + i, 8 + j);
				if (dsla_storelayers.contains(StoreLayerAdded.values()[j].getCode())) {
					sheet1.getRow(20 + count + i).createCell(8 + j).setCellValue(IsFlag.Shi.getValue());
				} else {
					sheet1.getRow(20 + count + i).createCell(8 + j).setCellValue(IsFlag.Fou.getValue());
				}
			}
		}
	}

	/**
	 * 处理生成的excel中下拉选框的问题
	 */
	private void addValidationData(XSSFSheet sheet1, String[] sqlenginesubjects, int row, int col) {
		//配置基础
		DataValidationHelper helper = sheet1.getDataValidationHelper();
		DataValidationConstraint constraint;
		CellRangeAddressList addressList;
		DataValidation dataValidation;
		//创建constraint
		constraint = helper.createExplicitListConstraint(sqlenginesubjects);
		//选择位置
		addressList = new CellRangeAddressList(row, row, col, col);
		dataValidation = helper.createValidation(constraint, addressList);
		//添加到sheet中
		sheet1.addValidationData(dataValidation);
	}

	/**
	 * 根据data_mart_id 返回工程下的所有信息
	 */
	private byte[] getdownloadFile(String data_mart_id) {
		Map<String, Object> resultmap = new HashMap<>();
		Dm_info dmInfo = new Dm_info();
		dmInfo.setData_mart_id(data_mart_id);
		//加工工程表
		Dm_info dm_info = Dbo.queryOneObject(Dm_info.class, "select * from " + Dm_info.TableName + " where " +
			"data_mart_id = ?", dmInfo.getData_mart_id())
			.orElseThrow(() -> new BusinessException("sql查询错误或者映射实体失败"));
		//加工表表
		List<Dm_datatable> dm_datatables = Dbo
			.queryList(Dm_datatable.class, "select * from " + Dm_datatable.TableName + " where data_mart_id = ?",
				dm_info.getData_mart_id());
		//sql表
		List<Dm_operation_info> dm_operation_infos = Dbo
			.queryList(Dm_operation_info.class, "select * from " + Dm_operation_info.TableName + " where datatable_id in " +
					"(select datatable_id from " + Dm_datatable.TableName
					+ " where data_mart_id =  ? ) and end_date=?",
				dm_info.getData_mart_id(), Constant.MAXDATE);
		//血缘表1
		List<Dm_datatable_source> dm_datatable_sources = Dbo.queryList(Dm_datatable_source.class,
			"select * from " + Dm_datatable_source.TableName + " where datatable_id in " +
				"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? )",
			dm_info.getData_mart_id());
		//血缘表2
		List<Dm_etlmap_info> dm_etlmap_infos = Dbo
			.queryList(Dm_etlmap_info.class, "select * from " + Dm_etlmap_info.TableName + " where datatable_id in " +
					"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? )",
				dm_info.getData_mart_id());
		//血缘表3
		List<Own_source_field> own_source_fields = Dbo.queryList(Own_source_field.class,
			"select * from " + Own_source_field.TableName + " where own_dource_table_id in (" +
				"select own_dource_table_id from " + Dm_datatable_source.TableName + " where datatable_id in " +
				"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? ))",
			dm_info.getData_mart_id());
		//字段表
		List<Datatable_field_info> datatable_field_infos = Dbo.queryList(Datatable_field_info.class,
			"select * from " + Datatable_field_info.TableName + " where datatable_id in " +
				"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? )" +
				" and end_date=?",
			dm_info.getData_mart_id(), Constant.MAXDATE);
		List<Dtab_relation_store> dm_relation_datatables = Dbo
			.queryList(Dtab_relation_store.class, "select * from " + Dtab_relation_store.TableName + " where tab_id in " +
					"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? ) and data_source =?",
				dm_info.getData_mart_id(), StoreLayerDataSource.DM.getCode());
		List<Dcol_relation_store> dm_column_storages = Dbo
			.queryList(Dcol_relation_store.class, "select * from " + Dcol_relation_store.TableName + " where col_id in (" +
					"select datatable_field_id from " + Datatable_field_info.TableName + " where datatable_id in " +
					"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? )) and data_source = ?",
				dm_info.getData_mart_id(), StoreLayerDataSource.DM.getCode());
		//前后置作业表
		List<Dm_relevant_info> dm_relevant_infos = Dbo
			.queryList(Dm_relevant_info.class, "select * from " + Dm_relevant_info.TableName + " where datatable_id in " +
					"(select datatable_id from " + Dm_datatable.TableName + " where data_mart_id =  ? )",
				dm_info.getData_mart_id());
		//加工分类表
		List<Dm_category> dm_categories = Dbo.queryList(Dm_category.class,
			"select * from " + Dm_category.TableName + " where data_mart_id =? ",
			dm_info.getData_mart_id());
		resultmap.put("dm_info", dm_info);
		resultmap.put("dm_datatables", dm_datatables);
		resultmap.put("dm_operation_infos", dm_operation_infos);
		resultmap.put("dm_datatable_sources", dm_datatable_sources);
		resultmap.put("dm_etlmap_infos", dm_etlmap_infos);
		resultmap.put("own_source_fields", own_source_fields);
		resultmap.put("datatable_field_infos", datatable_field_infos);
		resultmap.put("dm_relation_datatables", dm_relation_datatables);
		resultmap.put("dm_column_storages", dm_column_storages);
		resultmap.put("dm_relevant_infos", dm_relevant_infos);
		resultmap.put("dm_categories", dm_categories);
		// 加密
		String martResult = Base64.getEncoder().encodeToString(JSON.toJSONString(resultmap).getBytes());
		return martResult.getBytes();
	}


	@Method(desc = "上传加工工程",
		logicStep = "上传接受加工工程的hrds文件")
	@Param(name = "file_path", desc = "上传文件名称（全路径），上传要导入的加工工程", range = "不能为空以及空格")
	public void uploadFile(String file_path) throws Exception {
		if (!new File(file_path).exists()) {
			throw new BusinessException("上传文件不存在！");
		}
		String strTemp = new String(Files.readAllBytes(new File(file_path).toPath()));
		// 解密
		strTemp = new String(Base64.getDecoder().decode(strTemp));
		JSONObject jsonObject = JSONObject.parseObject(strTemp);
		//加工工程表
		Dm_info dm_info = JSONObject.parseObject(jsonObject.getJSONObject("dm_info").toJSONString(), Dm_info.class);
		long num = Dbo.queryNumber(
			"select count(*) from " + Dm_info.TableName + " where data_mart_id=? and mart_number=?",
			dm_info.getData_mart_id(), dm_info.getMart_number())
			.orElseThrow(() -> new BusinessException("sql查询错误"));
		if (num == 0) {
			// 不存在，新增
			dm_info.add(Dbo.db());
		} else {
			dm_info.update(Dbo.db());
		}
		//加工分类表
		List<Dm_category> dm_categories = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_categories").toJSONString(), Dm_category.class);
		for (Dm_category dm_category : dm_categories) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_category.TableName + " where category_id=?",
				dm_category.getCategory_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0 && !isCategoryNumExist(dm_category.getData_mart_id(), dm_category.getCategory_num())
				&& !isCategoryNameExist(dm_category.getData_mart_id(), dm_category.getCategory_name())) {
				// 不存在，新增
				dm_category.add(Dbo.db());
			} else {
				dm_category.update(Dbo.db());
			}
		}
		//加工数据表
		List<Dm_datatable> dm_datatables = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_datatables").toJSONString(), Dm_datatable.class);
		for (Dm_datatable dm_datatable : dm_datatables) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_datatable.TableName + " where datatable_id=?",
				dm_datatable.getDatatable_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_datatable.add(Dbo.db());
			} else {
				dm_datatable.update(Dbo.db());
			}
		}
		//sql表
		List<Dm_operation_info> dm_operation_infos = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_operation_infos").toJSONString(), Dm_operation_info.class);
		for (Dm_operation_info dm_operation_info : dm_operation_infos) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_operation_info.TableName + " where id=?",
				dm_operation_info.getId())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_operation_info.add(Dbo.db());
			} else {
				dm_operation_info.update(Dbo.db());
			}
		}
		//关系表
		List<Dtab_relation_store> dm_relation_datatables = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_relation_datatables").toJSONString(), Dtab_relation_store.class);
		for (Dtab_relation_store dm_relation_datatable : dm_relation_datatables) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dtab_relation_store.TableName
					+ " where dsl_id=? and tab_id=?",
				dm_relation_datatable.getDsl_id(), dm_relation_datatable.getTab_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_relation_datatable.add(Dbo.db());
			} else {
				dm_relation_datatable.update(Dbo.db());
			}
		}
		//字段表
		// 获取数据库当前字段有效数据全部设置有效日期为当天日期
		List<Datatable_field_info> datatableFieldInfos = getDatatable_field_infos(dm_info.getData_mart_id());
		List<Datatable_field_info> datatable_field_infos = JSONObject
			.parseArray(jsonObject.getJSONArray("datatable_field_infos").toJSONString(), Datatable_field_info.class);
		List<Datatable_field_info> reduce1 =
			datatable_field_infos.stream().filter(item -> !datatableFieldInfos.contains(item))
				.collect(Collectors.toList());
		for (Datatable_field_info datatable_field_info : reduce1) {
			// 下载多于数据库的直接新增
			datatable_field_info.setDatatable_field_id(PrimayKeyGener.getNextId());
			datatable_field_info.add(Dbo.db());
		}
		// 将下载字段表数据插入
		List<Datatable_field_info> reduce2 =
			datatableFieldInfos.stream().filter(item -> !datatable_field_infos.contains(item))
				.collect(Collectors.toList());
		for (Datatable_field_info datatable_field_info : reduce2) {
			// 更新数据库多于下载的结束日期为当天
			datatable_field_info.setEnd_date(DateUtil.getSysDate());
			datatable_field_info.update(Dbo.db());
		}
		//字段关系表
		List<Dcol_relation_store> dm_column_storages = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_column_storages").toJSONString(), Dcol_relation_store.class);
		for (Dcol_relation_store dm_column_storage : dm_column_storages) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dcol_relation_store.TableName
					+ " where dslad_id=? and col_id=?",
				dm_column_storage.getDslad_id(), dm_column_storage.getCol_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_column_storage.add(Dbo.db());
			} else {
				dm_column_storage.update(Dbo.db());
			}
		}
		//数据表已选数据源信息-血缘表1
		List<Dm_datatable_source> dm_datatable_sources = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_datatable_sources").toJSONString(), Dm_datatable_source.class);
		for (Dm_datatable_source dm_datatable_source : dm_datatable_sources) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_datatable_source.TableName + " where own_dource_table_id=?",
				dm_datatable_source.getOwn_dource_table_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_datatable_source.add(Dbo.db());
			} else {
				dm_datatable_source.update(Dbo.db());
			}
		}
		//结果映射信息表-血缘表2
		List<Dm_etlmap_info> dm_etlmap_infos = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_etlmap_infos").toJSONString(), Dm_etlmap_info.class);
		for (Dm_etlmap_info dm_etlmap_info : dm_etlmap_infos) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_etlmap_info.TableName + " where etl_id=?",
				dm_etlmap_info.getEtl_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_etlmap_info.add(Dbo.db());
			} else {
				dm_etlmap_info.update(Dbo.db());
			}
		}
		//数据源表字段-血缘表3
		List<Own_source_field> own_source_fields = JSONObject
			.parseArray(jsonObject.getJSONArray("own_source_fields").toJSONString(), Own_source_field.class);
		for (Own_source_field own_source_field : own_source_fields) {
			num = Dbo.queryNumber(
				"select count(*) from " + Own_source_field.TableName + " where own_field_id=?",
				own_source_field.getOwn_field_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				own_source_field.add(Dbo.db());
			} else {
				own_source_field.update(Dbo.db());
			}
		}
		//前后置作业表
		List<Dm_relevant_info> dm_relevant_infos = JSONObject
			.parseArray(jsonObject.getJSONArray("dm_relevant_infos").toJSONString(), Dm_relevant_info.class);
		for (Dm_relevant_info dm_relevant_info : dm_relevant_infos) {
			num = Dbo.queryNumber(
				"select count(*) from " + Dm_relevant_info.TableName + " where rel_id=?",
				dm_relevant_info.getRel_id())
				.orElseThrow(() -> new BusinessException("sql查询错误"));
			if (num == 0) {
				// 不存在，新增
				dm_relevant_info.add(Dbo.db());
			} else {
				dm_relevant_info.update(Dbo.db());
			}
		}
		// 导入成功后删除上传文件
		deleteImportFilePath(file_path);

	}

	@Method(desc = "获取加工导入审核文件路径", logicStep = "")
	@Param(name = "file", desc = "上传文件名称（全路径），上传要导入的加工工程", range = "不能为空以及空格")
	@Return(desc = "返回导入审核文件路径", range = "无限制")
	@UploadFile
	public String getImportFilePath(String file) {
		// 通过文件名称获取文件
		File uploadedFile = FileUploadUtil.getUploadedFile(file);
		if (!uploadedFile.exists()) {
			throw new BusinessException("上传文件不存在！");
		}
		return uploadedFile.getAbsolutePath();
	}

	@Method(desc = "删除加工导入审核上传文件数据", logicStep = "")
	@Param(name = "file_path", desc = "加工导入审核文件路径", range = "无限制")
	public void deleteImportFilePath(String file_path) {
		try {
			Files.delete(new File(file_path).toPath());
		} catch (IOException e) {
			throw new BusinessException("删除文件失败" + e.getMessage());
		}
	}

	@Method(desc = "获取加工导入审核数据", logicStep = "1.判断上传文件是否存在" +
		"2.解密获取上传文件信息" +
		"3.获取加工工程表差异信息" +
		"4.获取加工分类表差异信息" +
		"5.加工数据表差异信息" +
		"6.获取数据表发生改变表名称集合" +
		"7.获取数据表差异信息" +
		"8.获取sql表差异信息" +
		"9.获取数据字段表差异信息" +
		"10.获取前后置作业表差异信息" +
		"11.表作业影响" +
		"12.表影响")
	@Param(name = "file_path", desc = "加工导入审核文件路径", range = "无限制")
	@Return(desc = "返回加工导入审核数据", range = "无限制")
	public Map<String, Object> getImportReviewData(String file_path) {
		try {
			// 1.判断上传文件是否存在
			if (!new File(file_path).exists()) {
				throw new BusinessException("上传文件不存在！");
			}
			String strTemp = new String(Files.readAllBytes(new File(file_path).toPath()));
			// 2.解密获取上传文件信息
			strTemp = new String(Base64.getDecoder().decode(strTemp));
			Validator.notBlank(strTemp, "上传文件信息不能为空");
			JSONObject jsonObject = JSONObject.parseObject(strTemp);
			Map<String, Object> differenceMap = new HashMap<>();
			//3.获取加工工程表差异信息
			Dm_info dm_info = JSON.parseObject(jsonObject.getJSONObject("dm_info").toJSONString(), Dm_info.class);
			String dmInfoDiff = getDmInfoDiff(dm_info);
			differenceMap.put(Dm_info.TableName, dmInfoDiff);
			//4.获取加工分类表差异信息
			List<Dm_category> dm_categories = JSONObject
				.parseArray(jsonObject.getJSONArray("dm_categories").toJSONString(), Dm_category.class);
			Set<String> categoryDiff = getCategoryDiff(dm_categories);
			differenceMap.put(Dm_category.TableName, categoryDiff);
			//5.加工数据表差异信息
			List<Dm_datatable> dm_datatables = JSONObject
				.parseArray(jsonObject.getJSONArray("dm_datatables").toJSONString(), Dm_datatable.class);
			// 6.获取数据表发生改变表名称集合
			List<String> enNameList = new ArrayList<>();
			// 7.获取数据表差异信息
			Set<String> datatableDiff = getDatatableDiff(dm_datatables, enNameList);
			differenceMap.put(Dm_datatable.TableName, datatableDiff);
			//8.获取sql表差异信息
			List<Dm_operation_info> dm_operation_infos = JSONObject
				.parseArray(jsonObject.getJSONArray("dm_operation_infos").toJSONString(), Dm_operation_info.class);
			Set<String> operationInfoDiff = getOperationInfoDiff(dm_operation_infos);
			differenceMap.put(Dm_operation_info.TableName, operationInfoDiff);
			//9.获取数据字段表差异信息
			List<Datatable_field_info> datatable_field_infos = JSONObject
				.parseArray(jsonObject.getJSONArray("datatable_field_infos").toJSONString(), Datatable_field_info.class);
			List<String> datatableFieldDiff = getDatatableFieldDiff(datatable_field_infos, dm_info.getData_mart_id());
			differenceMap.put(Datatable_field_info.TableName, datatableFieldDiff);
			//10.获取前后置作业表差异信息
			List<Dm_relevant_info> dm_relevant_infos = JSONObject
				.parseArray(jsonObject.getJSONArray("dm_relevant_infos").toJSONString(), Dm_relevant_info.class);
			Set<String> relevantInfoDiff = getRelevantInfoDiff(dm_relevant_infos);
			differenceMap.put(Dm_relevant_info.TableName, relevantInfoDiff);
			Map<String, Object> jobInfluence = new HashMap<>();
			Map<String, Object> tableInfluence = new HashMap<>();
			for (String datatable_en_name : enNameList) {
				// 11.表作业影响
				List<Map<String, Object>> jobList = jobUpAndDownInfluences(datatable_en_name);
				// 12.表影响
				List<Map<String, Object>> tableList = tableUpAndDownInfluences(datatable_en_name);
				jobInfluence.put(datatable_en_name, jobList);
				tableInfluence.put(datatable_en_name, tableList);
			}
			differenceMap.put("jobInfluence", jobInfluence);
			differenceMap.put("tableInfluence", tableInfluence);
			return differenceMap;
		} catch (IOException e) {
			logger.error(e);
			throw new BusinessException("读取文件失败" + e.getMessage());
		}
	}

	private String getDmInfoDiff(Dm_info dm_info) {
		Map<String, Object> dmInfoMap = Dbo.queryOneObject(
			"select * from " + Dm_info.TableName + " where data_mart_id=?",
			dm_info.getData_mart_id());
		List<String> diffList = new ArrayList<>();
		if (dmInfoMap.isEmpty()) {
			// 不存在，新增
			diffList.add("新增的工程编号:" + dm_info.getMart_number());
			diffList.add("新增的工程名称:" + dm_info.getMart_name());
		} else {
			if (!dmInfoMap.get("mart_number").equals(dm_info.getMart_number())) {
				diffList.add("原工程编号:" + dmInfoMap.get("mart_number"));
				diffList.add("更新后工程编号:" + dm_info.getMart_number());
			}
			if (!dmInfoMap.get("mart_name").equals(dm_info.getMart_name())) {
				diffList.add("原工程名称:" + dmInfoMap.get("mart_name"));
				diffList.add("更新后工程名称:" + dm_info.getMart_name());
			}
		}
		return String.join(",", diffList);
	}

	private Set<String> getCategoryDiff(List<Dm_category> dm_categories) {
		Set<String> diffList = new HashSet<>();
		for (Dm_category dm_category : dm_categories) {
			Map<String, Object> categoryMap = Dbo.queryOneObject(
				"select * from " + Dm_category.TableName + " where category_id=?",
				dm_category.getCategory_id());
			Set<String> addList = new HashSet<>();
			if (categoryMap.isEmpty()) {
				addList.add("新增的的分类编号:" + dm_category.getCategory_num());
				addList.add("新增的的分类名称:" + dm_category.getCategory_name());
				diffList.add(String.join(",", addList));
			} else {
				if (!categoryMap.get("category_num").equals(dm_category.getCategory_num())) {
					addList.add("原分类编号:" + categoryMap.get("category_num"));
					addList.add("更新后分类编号:" + dm_category.getCategory_num());
				}
				if (!categoryMap.get("category_name").equals(dm_category.getCategory_name())) {
					addList.add("原分类名称:" + categoryMap.get("category_name"));
					addList.add("更新后分类名称:" + dm_category.getCategory_name());
				}
				if (!addList.isEmpty()) {
					diffList.add(String.join(",", addList));
				}
			}
		}
		return diffList;
	}

	private Set<String> getDatatableDiff(List<Dm_datatable> dm_datatables,
	                                     List<String> enNameList) {
		Set<String> diffSet = new HashSet<>();
		for (Dm_datatable dm_datatable : dm_datatables) {
			Map<String, Object> datatableMap = Dbo.queryOneObject(
				"select * from " + Dm_datatable.TableName + " where datatable_id=?",
				dm_datatable.getDatatable_id());
			Set<String> addList = new HashSet<>();
			if (datatableMap.isEmpty()) {
				addList.add("新增数据表英文名称:" + dm_datatable.getDatatable_en_name());
				addList.add("新增数据表中文名称:" + dm_datatable.getDatatable_cn_name());
				addList.add("新增进数方式:" + StorageType.ofValueByCode(dm_datatable.getStorage_type()));
				addList.add("新增数据表存储方式:" + TableStorage.ofValueByCode(dm_datatable.getTable_storage()));
				addList.add("新增执行引擎:" + SqlEngine.ofValueByCode(dm_datatable.getSql_engine()));
				diffSet.add(String.join(",", addList));
			} else {
				if (!datatableMap.get("datatable_en_name").equals(dm_datatable.getDatatable_en_name())) {
					addList.add("原表英文名称:" + datatableMap.get("datatable_en_name"));
					addList.add("更新后表英文名称:" + dm_datatable.getDatatable_en_name());
					enNameList.add(datatableMap.get("datatable_en_name").toString());
				}
				if (!datatableMap.get("datatable_cn_name").equals(dm_datatable.getDatatable_cn_name())) {
					addList.add("原表中文名称:" + datatableMap.get("datatable_cn_name"));
					addList.add("原更新后表中文名称:" + dm_datatable.getDatatable_cn_name());
				}
				if (!datatableMap.get("storage_type").equals(dm_datatable.getStorage_type())) {
					addList.add("原进数方式:" + StorageType.ofValueByCode(datatableMap.get("storage_type").toString()));
					addList.add("原更新后进数方式:" + StorageType.ofValueByCode(dm_datatable.getStorage_type()));
				}
				if (!datatableMap.get("table_storage").equals(dm_datatable.getTable_storage())) {
					addList.add("原数据表存储方式:" + TableStorage.ofValueByCode(datatableMap.get("table_storage").toString()));
					addList.add("原更新后数据表存储方式:" + TableStorage.ofValueByCode(dm_datatable.getTable_storage()));
				}
				if (!datatableMap.get("sql_engine").equals(dm_datatable.getSql_engine())) {
					addList.add("原执行引擎:" + SqlEngine.ofValueByCode(datatableMap.get("sql_engine").toString()));
					addList.add("更新后执行引擎:" + SqlEngine.ofValueByCode(dm_datatable.getSql_engine()));
				}
				if (!addList.isEmpty()) {
					diffSet.add(String.join(", ", addList));
				}
			}
		}
		return diffSet;
	}

	private Set<String> getOperationInfoDiff(List<Dm_operation_info> dm_operation_infos) {
		Set<String> diffSet = new HashSet<>();
		for (Dm_operation_info dm_operation_info : dm_operation_infos) {
			Map<String, Object> operationInfoMap = Dbo.queryOneObject(
				"select * from " + Dm_operation_info.TableName + " where id=? and end_date=?",
				dm_operation_info.getId(), Constant.MAXDATE);
			Set<String> addSet = new HashSet<>();
			if (operationInfoMap.isEmpty()) {
				addSet.add("新增的预览sql语句:" + dm_operation_info.getView_sql());
				addSet.add("新增的执行sql语句:" + dm_operation_info.getExecute_sql());
				diffSet.add(String.join(",", addSet));
			} else {
				if (!operationInfoMap.get("view_sql").equals(dm_operation_info.getView_sql())) {
					addSet.add("原预览sql语句:" + operationInfoMap.get("view_sql").toString());
					addSet.add("更新后预览sql语句:" + dm_operation_info.getView_sql());
				}
				if (!operationInfoMap.get("execute_sql").equals(dm_operation_info.getExecute_sql())) {
					addSet.add("原执行sql语句:" + operationInfoMap.get("execute_sql"));
					addSet.add("更新后执行sql语句:" + dm_operation_info.getExecute_sql());
				}
				if (!addSet.isEmpty()) {
					diffSet.add(String.join(",", addSet));
				}
			}
		}
		return diffSet;
	}

	private List<String> getDatatableFieldDiff(List<Datatable_field_info> datatable_field_infos,
	                                           long data_mart_id) {
		List<String> diffList = new ArrayList<>();
		// 新版本有效数据表字段数据
		List<Datatable_field_info> datatableFieldInfos = getDatatable_field_infos(data_mart_id);
		List<Datatable_field_info> reduce1 =
			datatable_field_infos.stream().filter(item -> !datatableFieldInfos.contains(item))
				.collect(Collectors.toList());
		for (Datatable_field_info fieldInfo : reduce1) {
			List<String> list = new ArrayList<>();
			list.add("新版本的字段中文名称:" + fieldInfo.getField_en_name());
			list.add("新版本的字段英文名称:" + fieldInfo.getField_cn_name());
			list.add("新版本的字段类型:" + fieldInfo.getField_type());
			list.add("新版本的字段长度:" + fieldInfo.getField_length());
			list.add("新版本的处理方式:" + ProcessType.ofValueByCode(fieldInfo.getField_process()));
			list.add("新版本的映射规则mapping:" + fieldInfo.getProcess_mapping());
			if (StringUtil.isNotBlank(fieldInfo.getGroup_mapping())) {
				list.add("新版本的分组映射:" + fieldInfo.getGroup_mapping());
			}
			if (!list.isEmpty() && !diffList.contains(String.join(",", list))) {
				diffList.add(String.join(",", list));
			}
		}
		// 旧版本失效数据表字段数据
		List<Datatable_field_info> reduce2 =
			datatableFieldInfos.stream().filter(item -> !datatable_field_infos.contains(item))
				.collect(Collectors.toList());
		for (Datatable_field_info datatable_field_info : reduce2) {
			List<String> list = new ArrayList<>();
			list.add("旧版本的字段中文名称:" + datatable_field_info.getField_en_name());
			list.add("旧版本的字段英文名称:" + datatable_field_info.getField_cn_name());
			list.add("旧版本的字段类型:" + datatable_field_info.getField_type());
			list.add("旧版本的字段长度:" + datatable_field_info.getField_length());
			list.add("旧版本的处理方式:" + ProcessType.ofValueByCode(datatable_field_info.getField_process()));
			list.add("旧版本的映射规则mapping:" + datatable_field_info.getProcess_mapping());
			if (StringUtil.isNotBlank(datatable_field_info.getGroup_mapping())) {
				list.add("旧版本的分组映射:" + datatable_field_info.getGroup_mapping());
			}
			if (!list.isEmpty() && !diffList.contains(String.join(",", list))) {
				diffList.add(String.join(",", list));
			}
		}
		return diffList;
	}

	private List<Datatable_field_info> getDatatable_field_infos(long data_mart_id) {
		return Dbo.queryList(Datatable_field_info.class,
			"select t1.* from " + Datatable_field_info.TableName + " t1 "
				+ " left join " + Dm_datatable.TableName + " t2 on t1.datatable_id=t2.datatable_id"
				+ " left join " + Dm_info.TableName + " t3 on t3.data_mart_id=t2.data_mart_id"
				+ " where t1.end_date=? and t3.data_mart_id=?",
			Constant.MAXDATE, data_mart_id);
	}

	private Set<String> getRelevantInfoDiff(List<Dm_relevant_info> dm_relevant_infos) {
		Set<String> diffSet = new HashSet<>();
		for (Dm_relevant_info dm_relevant_info : dm_relevant_infos) {
			Map<String, Object> relevantInfoMap = Dbo.queryOneObject(
				"select * from " + Dm_relevant_info.TableName + " where rel_id=?",
				dm_relevant_info.getRel_id());
			List<String> diffList = new ArrayList<>();
			if (relevantInfoMap.isEmpty()) {
				diffList.add("新增的前置作业:" + dm_relevant_info.getPre_work());
				diffList.add("新增的后置作业:" + dm_relevant_info.getPost_work());
			} else {
				if (null != relevantInfoMap.get("pre_work") &&
					!relevantInfoMap.get("pre_work").equals(dm_relevant_info.getPre_work())) {
					diffList.add("更新后前置作业:" + relevantInfoMap.get("pre_work"));
					diffList.add("原前置作业:" + dm_relevant_info.getPost_work());
				}
				if (null != relevantInfoMap.get("post_work") &&
					!relevantInfoMap.get("post_work").equals(dm_relevant_info.getPost_work())) {
					diffList.add("更新后后置作业:" + relevantInfoMap.get("post_work"));
					diffList.add("原后置作业:" + dm_relevant_info.getPost_work());
				}
				if (!diffList.isEmpty()) {
					diffSet.add(String.join(",", diffList));
				}
			}
		}
		return diffSet;
	}

	@Method(desc = "作业上下游影响", logicStep = "1.根据作业名称查询作业工程" +
		"2.获取上游作业信息" +
		"3.获取下游作业信息" +
		"4.将上游作业信息封装入下游作业中" +
		"5.按需要展示的格式封装数据并返回")
	@Param(name = "datatable_en_name", desc = "数据表英文名称", range = "新增数据表时生成")
	@Return(desc = "返回作业上下级影响数据", range = "无限制")
	private List<Map<String, Object>> jobUpAndDownInfluences(String datatable_en_name) {
		// 1.根据作业名称查询作业工程
		List<Etl_job_def> etlJobDefs = getEtlJobByDatatableName(datatable_en_name);
		List<Map<String, Object>> influences = new ArrayList<>();
		if (!etlJobDefs.isEmpty()) {
			for (Etl_job_def etl_job_def : etlJobDefs) {
				// 2.获取上游作业信息
				List<Map<String, Object>> topJobInfoList =
					EtlJobUtil.topEtlJobDependencyInfo(etl_job_def.getEtl_job(),
						etl_job_def.getEtl_sys_cd(), Dbo.db()).toList();
				// 3.获取下游作业信息
				List<Map<String, Object>> downJobInfoList =
					EtlJobUtil.downEtlJobDependencyInfo(etl_job_def.getEtl_sys_cd(), etl_job_def.getEtl_job(),
						Dbo.db()).toList();
				// 4.将上游作业信息封装入下游作业中
				if (!topJobInfoList.isEmpty()) {
					downJobInfoList.addAll(topJobInfoList);
				}
				influences.addAll(downJobInfoList);
			}
		}
		influences.forEach(map -> map.put("parentid", datatable_en_name));
		// 5.按需要展示的格式封装数据并返回
		Map<String, Object> dataInfo = new HashMap<>();
		dataInfo.put("id", datatable_en_name);
		dataInfo.put("isroot", true);
		dataInfo.put("topic", datatable_en_name);
		dataInfo.put("background-color", "red");
		influences.add(dataInfo);
		return influences;
	}

	@Method(desc = "通过数据表英文名称获取对应作业", logicStep = "1.通过数据表英文名称获取对应作业")
	@Param(name = "datatable_en_name", desc = "数据表英文名称", range = "新增数据表时生成")
	@Return(desc = "返回数据表英文名称对应作业", range = "无限制")
	public List<Etl_job_def> getEtlJobByDatatableName(String datatable_en_name) {
		// 1.通过数据表英文名称获取对应作业
		return Dbo.queryList(Etl_job_def.class,
			"select etl_sys_cd,sub_sys_cd,etl_job from " + Etl_job_def.TableName
				+ " where etl_job like ?",
			"%" + DataSourceType.DML.getCode() + "_" + datatable_en_name + "%");
	}

	@Method(desc = "表影响", logicStep = "1.获取表影响关系并返回")
	@Param(name = "datatable_en_name", desc = "数据表英文名称", range = "无限制")
	@Return(desc = "返回表影响关系", range = "无限制")
	private List<Map<String, Object>> tableUpAndDownInfluences(String datatable_en_name) {
		// 1.获取表影响关系并返回
		List<Map<String, Object>> tableList = Dbo.queryList(
			" select own_source_table_name AS source_table_name,sourcefields_name AS source_fields_name," +
				"datatable_en_name AS table_name,targetfield_name AS target_column_name,'' as mapping" +
				" from " + Dm_datatable.TableName + " dd join " + Dm_datatable_source.TableName + " dds" +
				" on dd.datatable_id = dds.datatable_id join " + Dm_etlmap_info.TableName + " dei on" +
				" dds.own_dource_table_id = dei.own_dource_table_id and dd.datatable_id = dei.datatable_id" +
				" where lower(own_source_table_name) = lower(?)", datatable_en_name);
		Set<String> set = new HashSet<>();
		List<Map<String, Object>> influencesResult = new ArrayList<>();
		tableList.forEach(influences_data -> {
			//获取模型表名
			String tableName = influences_data.get("table_name").toString();
			//过滤重复的模型表名称
			if (!set.contains(tableName)) {
				Map<String, Object> map = new HashMap<>();
				set.add(tableName);
				map.put("id", tableName);
				map.put("parentid", datatable_en_name);
				map.put("direction", "right");
				map.put("topic", tableName);
				map.put("background-color", "#0000ff");
				influencesResult.add(map);
			}
		});
		Map<String, Object> rootMap = new HashMap<>();
		rootMap.put("id", datatable_en_name);
		rootMap.put("isroot", true);
		rootMap.put("topic", datatable_en_name);
		rootMap.put("background-color", "red");
		influencesResult.add(rootMap);
		return influencesResult;
	}

	@Method(desc = "删除加工工程",
		logicStep = "1.判断工程下是否还有表信息" +
			"2.删除加工工程")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "String类型加工工程主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public void deleteMart(String data_mart_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setData_mart_id(data_mart_id);
		OptionalLong optionalLong = Dbo
			.queryNumber("select count(*) from " + Dm_datatable.TableName + " where data_mart_id = ?",
				dm_datatable.getData_mart_id());
		//判断是否工程下还有表
		if (optionalLong.isPresent()) {
			long asLong = optionalLong.getAsLong();
			if (asLong > 0) {
				throw new BusinessSystemException("工程下还存在表，请先删除表");
			} else {
				// 删除加工工程时先删除加工分类
				Dbo.execute("delete from " + Dm_category.TableName + " where data_mart_id = ?",
					dm_datatable.getData_mart_id());
				deletesql(" from " + Dm_info.TableName + " where data_mart_id = ?", dm_datatable.getData_mart_id(),
					Dm_info.TableName);
			}
		}
	}

	@Method(desc = "生成加工表到作业调度",
		logicStep = "生成加工表到作业调度")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Param(name = "etl_sys_cd", desc = "etl_sys_cd", range = "String类型作业调度ID")
	@Param(name = "sub_sys_cd", desc = "sub_sys_cd", range = "String类型作业调度任务ID")
	@Return(desc = "查询返回结果集", range = "无限制")
	public void generateMartJobToEtl(String etl_sys_cd, String sub_sys_cd, String datatable_id) {
		EtlJobUtil.saveJob(datatable_id, DataSourceType.DML, etl_sys_cd, sub_sys_cd, null);
	}

	@Method(desc = "根据表主键查询表名",
		logicStep = "根据表主键查询表名")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public Dm_datatable getTableName(String datatable_id) {
		Dm_datatable dm_datatable = new Dm_datatable();
		dm_datatable.setDatatable_id(datatable_id);
		dm_datatable = Dbo.queryOneObject(Dm_datatable.class,
			"select datatable_en_name,pre_partition from " + Dm_datatable.TableName
				+ " where datatable_id = ?", dm_datatable.getDatatable_id())
			.orElseThrow(() -> new BusinessException("查询" + Dm_datatable.TableName + "失败"));
		return dm_datatable;

	}

	@Method(desc = "保存前置作业",
		logicStep = "保存前置作业" +
			"判断SQL是否为空" +
			"分隔SQL" +
			"判断SQL的表名是否正确")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Param(name = "pre_work", desc = "pre_work", range = "String类型前置作业sql", nullable = true)
	@Param(name = "post_work", desc = "post_work", range = "String类型后置作业sql", nullable = true)
	@Return(desc = "查询返回结果集", range = "无限制")
	public void savePreAndAfterJob(String datatable_id, String pre_work, String post_work) {
		Dm_relevant_info dm_relevant_info = new Dm_relevant_info();
		dm_relevant_info.setDatatable_id(datatable_id);
		List<Dm_datatable> dm_datatables = Dbo.queryList(Dm_datatable.class,
			"select datatable_en_name from " + Dm_datatable.TableName + " where datatable_id = ?",
			dm_relevant_info.getDatatable_id());
		if (dm_datatables.isEmpty()) {
			throw new BusinessSystemException("没有查询到表英文名，请检查");
		}
		String datatable_en_name = dm_datatables.get(0).getDatatable_en_name();
		//判空
		if (!StringUtils.isBlank(pre_work)) {
			//分隔
			if (pre_work.contains(";;")) {
				String[] pre_works = pre_work.split(";;");
				for (String pre_sql : pre_works) {
					//SQL解析判断表名是否正确
					String preworktablename = DruidParseQuerySql.getInDeUpSqlTableName(pre_sql);
					if (!preworktablename.equalsIgnoreCase(datatable_en_name)) {
						throw new BusinessException("前置处理的操作表为" + preworktablename + ",非本加工表,保存失败");
					}
				}
			} else {
				pre_work = pre_work.trim();
				if (pre_work.endsWith(";")) {
					pre_work = pre_work.substring(0, pre_work.length() - 1);
				}
				//SQL解析判断表名是否正确
				String preworktablename = DruidParseQuerySql.getInDeUpSqlTableName(pre_work);
				if (!preworktablename.equalsIgnoreCase(datatable_en_name)) {
					throw new BusinessException("前置处理的操作表为" + preworktablename + ",非本加工表,保存失败");
				}
			}
		}
		//判空
		if (!StringUtils.isBlank(post_work)) {
			//分隔
			if (post_work.contains(";;")) {
				String[] post_works = post_work.split(";;");
				for (String post_sql : post_works) {
					String preworktablename = DruidParseQuerySql.getInDeUpSqlTableName(post_sql);
					if (!preworktablename.equalsIgnoreCase(datatable_en_name)) {
						throw new BusinessException("后置处理的操作表为" + preworktablename + ",非本加工表,保存失败");
					}
				}
			} else {
				post_work = post_work.trim();
				if (post_work.endsWith(";")) {
					post_work = post_work.substring(0, post_work.length() - 1);
				}
				String preworktablename = DruidParseQuerySql.getInDeUpSqlTableName(post_work);
				if (!preworktablename.equalsIgnoreCase(datatable_en_name)) {
					throw new BusinessException("后置处理的操作表为" + preworktablename + ",非本加工表,保存失败");
				}
			}
		}

		Optional<Dm_relevant_info> dm_relevant_infoOptional = Dbo.queryOneObject(Dm_relevant_info.class,
			"select * from " + Dm_relevant_info.TableName + " where datatable_id = ?", dm_relevant_info.getDatatable_id());
		//如果存在就更新
		if (dm_relevant_infoOptional.isPresent()) {
			dm_relevant_info = dm_relevant_infoOptional.get();
			dm_relevant_info.setPre_work(pre_work);
			dm_relevant_info.setPost_work(post_work);
			Dbo.execute("UPDATE " + Dm_relevant_info.TableName + " SET pre_work = ?, post_work = ? WHERE rel_id = ?",
				pre_work, post_work, dm_relevant_info.getRel_id());
//			updatebean(dm_relevant_info);
		}
		//如果不存在 就新增
		else {
			dm_relevant_info.setRel_id(PrimayKeyGener.getNextId());
			dm_relevant_info.setPre_work(pre_work);
			dm_relevant_info.setPost_work(post_work);
			dm_relevant_info.add(Dbo.db());
		}
		//保存
	}

	@Method(desc = "前后置处理SQL回显",
		logicStep = "前后置处理SQL回显")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Return(desc = "查询返回结果集", range = "无限制")
	public Map<String, Object> getPreAndAfterJob(String datatable_id) {
		Dm_relevant_info dm_relevant_info = new Dm_relevant_info();
		dm_relevant_info.setDatatable_id(datatable_id);
		return Dbo.queryOneObject(
			"select * from " + Dm_relevant_info.TableName + " where datatable_id = ?", dm_relevant_info.getDatatable_id());
	}


	@Method(desc = "新增页面判断选择的当前存储类型是否为oracle,且判断表名是否过长",
		logicStep = "获取存储层判断是否为oracle")
	@Param(name = "dsl_id", desc = "数据存储层主键", range = "String类型数据存储层主键")
	@Param(name = "datatable_en_name", desc = "加工数据表名", range = "String类型加工数据表名")
	@Return(desc = "查询返回结果集", range = "无限制")
	public Boolean checkOracle(String dsl_id, String datatable_en_name) {
		Data_store_layer_attr data_store_layer_attr = new Data_store_layer_attr();
		data_store_layer_attr.setDsl_id(dsl_id);
		List<Data_store_layer_attr> data_store_layer_attrs = Dbo.queryList(Data_store_layer_attr.class,
			"select * from " + Data_store_layer_attr.TableName
				+ " where storage_property_key like ? and storage_property_val like ? and dsl_id = ?",
			"%jdbc_url%", "%oracle%", data_store_layer_attr.getDsl_id());
		if (data_store_layer_attrs.isEmpty()) {
			return true;
		} else {
			return datatable_en_name.length() <= 26;
		}
	}

	@Method(desc = "下载加工数据表excel模板", logicStep = "1.获取响应信息" +
		"2.获取请求信息" +
		"3.获取excel模板文件路径" +
		"4、设置响应头信息" +
		"5、使用response获得输出流，完成文件下载")
	public void downloadExcel() {
		// 1.获取响应信息
		HttpServletResponse response = ResponseUtil.getResponse();
		// 2.获取请求信息
		HttpServletRequest request = RequestUtil.getRequest();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try (OutputStream out = response.getOutputStream()) {
			response.reset();
			// 3.获取excel模板文件路径
			URL url = this.getClass().getClassLoader().getResource(EXCEL_FILEPATH);
			try {
				Objects.requireNonNull(url);
			} catch (Exception e) {
				throw new BusinessException("Excel模板不存在");
			}
			logger.info("excel模板路径：" + url.getPath());
			Validator.notBlank(url.getPath(), "Excel模板不存在");
			File downloadFile = new File(url.getPath());
			// 4、设置响应头信息
			if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				// 对firefox浏览器做特殊处理
				response.setHeader(
					"content-disposition",
					"attachment;filename="
						+ new String(EXCEL_FILEPATH.getBytes(), CodecUtil.GBK_STRING));
			} else {
				response.setHeader(
					"content-disposition",
					"attachment;filename="
						+ URLEncoder.encode(EXCEL_FILEPATH, CodecUtil.UTF8_STRING));
			}
			response.setContentType("APPLICATION/OCTET-STREAM");
			// 5、使用response获得输出流，完成文件下载
			bis = new BufferedInputStream(new FileInputStream(downloadFile));
			bos = new BufferedOutputStream(out);
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
			bos.flush();
		} catch (IOException e) {
			logger.error(e);
			throw new AppSystemException(e);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

	}

	@Method(desc = "上传Excel文件",
		logicStep = "接收excel文件并解析文件入库")
	@Param(name = "file", desc = "上传文件,文件名为作业配置对应那几张表名", range = "以每个模块对应表名为文件名")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "加工工程主键data_mart_id")
	@UploadFile
	public void uploadExcelFile(String file, String data_mart_id) {
		FileInputStream fis = null;
		Workbook workBook = null;
		try {
			File uploadedFile = FileUploadUtil.getUploadedFile(file);
			if (!uploadedFile.exists()) {
				throw new BusinessException("上传文件不存在！");
			}
			//创建的输入流
			String path = uploadedFile.toPath().toString();
			fis = new FileInputStream(path);
			//判断文件后缀名
			if (path.toLowerCase().endsWith(xlsxSuffix)) {
				try {
					workBook = new XSSFWorkbook(fis);
				} catch (IOException e) {
					throw new BusinessException("定义XSSFWorkbook失败");
				}
			} else if (path.toLowerCase().endsWith(xlsSuffix)) {
				try {
					workBook = new HSSFWorkbook(fis);
				} catch (IOException e) {
					throw new BusinessException("定义XSSFWorkbook失败");
				}
			} else {
				throw new BusinessException("文件格式不正确，不是excel文件");
			}
			saveimportexcel(workBook, data_mart_id);
			workBook.close();
			fis.close();
		} catch (Exception e) {
			throw new BusinessException("导入excel文件数据失败！");
		}
	}

	/**
	 * 保存上传的excel
	 */
	private void saveimportexcel(Workbook workBook, String data_mart_id) {
		int numberOfSheets = workBook.getNumberOfSheets();
		if (numberOfSheets == 0) {
			throw new BusinessSystemException("没有获取到sheet页，请检查文件");
		}
		Sheet sheet = workBook.getSheetAt(0);
		try {
			int lastRowNum = sheet.getLastRowNum();
			Row row;
			Dm_datatable dm_datatable;
			Dtab_relation_store dm_relation_datatable;
			Dm_operation_info dm_operation_info;
			Dm_relevant_info dm_relevant_info;
			Datatable_field_info datatable_field_info;
			Dcol_relation_store dcol_relation_store;
			for (int i = 1; i <= lastRowNum; i++) {
				row = sheet.getRow(i);
				// 导入excel数据表数据
				dm_datatable = new Dm_datatable();
				dm_datatable.setData_mart_id(data_mart_id);
				saveDmDataTable(dm_datatable, data_mart_id, row, i);
				// 导入存储dm_relation_datatable表数据
				// 存储层名称
				String dsl_name = ExcelUtil.getValue(row.getCell(9)).toString();
				Validator.notBlank(dsl_name, "存储层名称不能为空");
				Data_store_layer data_store_layer = Dbo.queryOneObject(Data_store_layer.class,
					"select * from " + Data_store_layer.TableName + " where dsl_name = ?",
					dsl_name)
					.orElseThrow(() -> new BusinessException("sql查询错误或者当前存储层名称对应存储层信息有误"));
				dm_relation_datatable = new Dtab_relation_store();
				dm_relation_datatable.setTab_id(dm_datatable.getDatatable_id());
				dm_relation_datatable.setDsl_id(data_store_layer.getDsl_id());
				dm_relation_datatable.setIs_successful(JobExecuteState.DengDai.getCode());
				dm_relation_datatable.setData_source(StoreLayerDataSource.DM.getCode());
				dm_relation_datatable.add(Dbo.db());
				// 导入存储dm_operation_info表数据
				// 执行sql
				String execute_sql = ExcelUtil.getValue(row.getCell(10)).toString();
				Validator.notBlank(execute_sql, "执行sql不能为空");
				// 预览sql
				String view_sql = ExcelUtil.getValue(row.getCell(11)).toString();
				Validator.notBlank(view_sql, "预览sql不能为空");
				dm_operation_info = new Dm_operation_info();
				dm_operation_info.setExecute_sql(execute_sql);
				dm_operation_info.setView_sql(view_sql);
				dm_operation_info.setDatatable_id(dm_datatable.getDatatable_id());
				dm_operation_info.setId(PrimayKeyGener.getNextId());
				dm_operation_info.setStart_date(DateUtil.getSysDate());
				dm_operation_info.setEnd_date(Constant.MAXDATE);
				dm_operation_info.add(Dbo.db());
				// 导入存储dm_relevant_info表数据
				dm_relevant_info = new Dm_relevant_info();
				dm_relevant_info.setRel_id(PrimayKeyGener.getNextId());
				dm_relevant_info.setDatatable_id(dm_datatable.getDatatable_id());
				// 前置作业
//				String pre_work = ExcelUtil.getValue(row.getCell(12)).toString();
				// 后置作业
				String post_work = ExcelUtil.getValue(row.getCell(12)).toString();
//				dm_relevant_info.setPre_work(pre_work);
				dm_relevant_info.setPost_work(post_work);
				dm_relevant_info.add(Dbo.db());
				//存储datatable_field_info表数据
				// 字段英文名
				String field_en_name = ExcelUtil.getValue(row.getCell(13)).toString();
				Validator.notBlank(field_en_name, "字段英文名不能为空");
				List<String> fieldEnNameList = StringUtil.split(field_en_name, "|");
				// 字段中文名
				String field_cn_name = ExcelUtil.getValue(row.getCell(14)).toString();
				Validator.notBlank(field_cn_name, "字段中文名不能为空");
				List<String> fieldCnNameList = StringUtil.split(field_cn_name, "|");
				// 字段类型
				String field_type = ExcelUtil.getValue(row.getCell(15)).toString();
				Validator.notBlank(field_type, "字段类型不能为空");
				List<String> fieldTypeList = StringUtil.split(field_type, "|");
				// 字段长度
				String field_length = ExcelUtil.getValue(row.getCell(16)).toString();
				List<String> fieldLengthList = StringUtil.split(field_length, "|");
				// 处理方式
				String field_process = ExcelUtil.getValue(row.getCell(17)).toString();
				Validator.notBlank(field_process, "处理方式不能为空");
				List<String> fieldProcessList = StringUtil.split(field_process, "|");
				// 映射规则mapping
				String process_mapping = ExcelUtil.getValue(row.getCell(18)).toString();
				Validator.notBlank(process_mapping, "映射规则mapping不能为空");
				List<String> mappingList = StringUtil.split(process_mapping, "|");
				// 分组映射对应规则
				String group_mapping = ExcelUtil.getValue(row.getCell(19)).toString();
				List<String> groupList = StringUtil.split(group_mapping, "|");
				if (fieldEnNameList.size() != fieldCnNameList.size()) {
					throw new BusinessException("字段中文名数量与字段英文名数量不相等");
				}
				if (fieldEnNameList.size() != fieldTypeList.size()) {
					throw new BusinessException("字段中文名数量与字段类型数量不相等");
				}
				if (fieldEnNameList.size() != fieldProcessList.size()) {
					throw new BusinessException("字段中文名数量与处理方式数量不相等");
				}
				if (fieldEnNameList.size() != mappingList.size()) {
					throw new BusinessException("字段中文名数量与映射规则mapping数量不相等");
				}
				if (field_length.contains("|")) {
					if (fieldEnNameList.size() != fieldLengthList.size()) {
						throw new BusinessException("字段中文名数量与字段长度数量不相等");
					}
				}
				if (group_mapping.contains("|")) {
					if (fieldEnNameList.size() != groupList.size()) {
						throw new BusinessException("字段中文名数量与分组映射规则数量不相等");
					}
				}
				DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql(execute_sql);
				List<String> columns = druidParseQuerySql.parseSelectOriginalField();
				List<String> columnList = new ArrayList<>();
				columns.forEach(column -> columnList.add(column.toUpperCase()));
				if (mappingList.size() != columns.size()) {
					throw new BusinessException("执行sql中的字段数量与映射规则mapping中的数量不相等");
				}
				datatable_field_info = new Datatable_field_info();
				for (int j = 0; j < fieldEnNameList.size(); j++) {
					// 如果分组映射设置分组映射对应规则
					if (ProcessType.FenZhuYingShe == ProcessType.ofEnumByCode(fieldProcessList.get(j))) {
						datatable_field_info.setGroup_mapping(groupList.get(j));
					}
					datatable_field_info.setProcess_mapping(mappingList.get(j));
					if (!columnList.contains(mappingList.get(j).toUpperCase())) {
						throw new BusinessSystemException("填写的来源字段，不存在于SQL中:" + mappingList.get(j));
					}
					datatable_field_info.setDatatable_field_id(PrimayKeyGener.getNextId());
					datatable_field_info.setDatatable_id(dm_datatable.getDatatable_id());
					datatable_field_info.setField_en_name(fieldEnNameList.get(j));
					datatable_field_info.setField_cn_name(fieldCnNameList.get(j));
					datatable_field_info.setField_type(fieldTypeList.get(j));
					datatable_field_info.setField_length(fieldLengthList.get(j));
					datatable_field_info.setField_process(fieldProcessList.get(j));
					datatable_field_info.setField_seq(String.valueOf(j + 1));
					datatable_field_info.setStart_date(DateUtil.getSysDate());
					datatable_field_info.setEnd_date(Constant.MAXDATE);
					datatable_field_info.add(Dbo.db());
				}
				// 主键
				String key = ExcelUtil.getValue(row.getCell(20)).toString();
				// rowkey
				String rowkey = ExcelUtil.getValue(row.getCell(21)).toString();
				// 索引列
				String indexColumn = ExcelUtil.getValue(row.getCell(22)).toString();
				// 预聚合列
				String preAggregatedColumn = ExcelUtil.getValue(row.getCell(23)).toString();
				// 排序列
				String sortColumn = ExcelUtil.getValue(row.getCell(24)).toString();
				// 分区列
				String partitionColumn = ExcelUtil.getValue(row.getCell(25)).toString();
				// 存储dcol_relation_store表数据
				dcol_relation_store = new Dcol_relation_store();
				dcol_relation_store.setCol_id(datatable_field_info.getDatatable_field_id());
				dcol_relation_store.setData_source(StoreLayerDataSource.DM.getCode());
				// 设置主键附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, key, StoreLayerAdded.ZhuJian.getCode());
				// 设置rowkey附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, rowkey, StoreLayerAdded.RowKey.getCode());
				// 设置索引列附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, indexColumn,
					StoreLayerAdded.SuoYinLie.getCode());
				// 设置预聚合列附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, preAggregatedColumn,
					StoreLayerAdded.YuJuHe.getCode());
				// 设置排序列附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, sortColumn,
					StoreLayerAdded.PaiXuLie.getCode());
				// 设置分区列附加属性
				setStoreAdd(dcol_relation_store, dm_relation_datatable, partitionColumn,
					StoreLayerAdded.FenQuLie.getCode());
				dcol_relation_store.add(Dbo.db());
				// 保存血缘关系到PGSQL中的表里
				saveBloodRelationToPGTable(execute_sql, String.valueOf(dm_datatable.getDatatable_id()));
			}
		} catch (Exception e) {
			logger.error(e);
			if (e.getMessage() != null) {
				throw new BusinessException(e.getMessage());
			} else {
				throw new BusinessSystemException("上传的excel模板存在问题,请检查");
			}
		}

	}

	private void setStoreAdd(Dcol_relation_store dcol_relation_store, Dtab_relation_store dm_relation_datatable,
	                         String key, String dsla_storelayer) {
		if (StringUtil.isNotBlank(key)) {
			List<String> keyList = StringUtil.split(key, "|");
			for (int k = 0; k < keyList.size(); k++) {
				if (IsFlag.Shi == IsFlag.ofEnumByCode(keyList.get(k))) {
					List<Long> dsladIdList = Dbo.queryOneColumnList(
						"select dslad_id from " + Data_store_layer_added.TableName
							+ " where dsl_id =? and dsla_storelayer=?",
						dm_relation_datatable.getDsl_id(), dsla_storelayer);
					if (!dsladIdList.isEmpty()) {
						dcol_relation_store.setDslad_id(dsladIdList.get(k));
					}
					if (StoreLayerAdded.RowKey == StoreLayerAdded.ofEnumByCode(dsla_storelayer)) {
						dcol_relation_store.setCsi_number(String.valueOf(k));
					}
				}
			}
		}
	}

	private void saveDmDataTable(Dm_datatable dm_datatable, String data_mart_id, Row row, int i) {
		//表英文名
		String table_name = ExcelUtil.getValue(row.getCell(0)).toString();
		Validator.notBlank(table_name, "第" + i + "行,表名不能为空");
		dm_datatable.setDatatable_en_name(table_name);
		//表中文名
		String table_ch_name = ExcelUtil.getValue(row.getCell(1)).toString();
		Validator.notBlank(table_ch_name, "第" + i + "行,表中文名不能为空");
		dm_datatable.setDatatable_cn_name(table_ch_name);
		//执行引擎
		String sql_engine = ExcelUtil.getValue(row.getCell(2)).toString();
		dm_datatable.setSql_engine(WebCodesItem.getCode(SqlEngine.CodeName, sql_engine));
		//表描述
		String datatable_desc = ExcelUtil.getValue(row.getCell(3)).toString();
		dm_datatable.setDatatable_desc(datatable_desc);
		//进数方式
		String storage_type = ExcelUtil.getValue(row.getCell(4)).toString();
		dm_datatable.setStorage_type(WebCodesItem.getCode(StorageType.CodeName, storage_type));
		//数据存储方式
		String table_storage = ExcelUtil.getValue(row.getCell(5)).toString();
		dm_datatable.setTable_storage(WebCodesItem.getCode(TableStorage.CodeName, table_storage));
		//数据生命周期
		String datatable_lifecycle = ExcelUtil.getValue(row.getCell(6)).toString();
		String datatableLifecycle = WebCodesItem.getCode(TableLifeCycle.CodeName,
			datatable_lifecycle);
		dm_datatable.setDatatable_lifecycle(datatableLifecycle);
		//数据表到期日期
		String datatable_due_date = ExcelUtil.getValue(row.getCell(7)).toString();
		if (TableLifeCycle.ofEnumByCode(datatableLifecycle) == TableLifeCycle.YongJiu) {
			dm_datatable.setDatatable_due_date(Constant.MAXDATE);
		} else {
			Validator.notBlank(datatable_due_date, "第" + i + "行,数据生命周期为临时，数据表到期日期不能为空");
			dm_datatable.setDatatable_due_date(datatable_due_date);

		}
		//选择分类
		String category_name = ExcelUtil.getValue(row.getCell(8)).toString();
		Validator.notBlank(table_ch_name, "第" + i + "行,表分类名称不能为空");
		List<Long> idList = Dbo.queryOneColumnList(
			"select category_id from " + Dm_category.TableName
				+ " where category_name=? and data_mart_id=?",
			category_name, dm_datatable.getData_mart_id());
		if (idList.isEmpty()) {
			throw new BusinessException("当前加工工程对应的加工分类不存在:" + category_name);
		}
		dm_datatable.setCategory_id(idList.get(0));
		// 设置默认参数
		dm_datatable.setRepeat_flag(IsFlag.Fou.getCode());
		dm_datatable.setDatatable_id(PrimayKeyGener.getNextId());
		dm_datatable.setDatatable_create_date(DateUtil.getSysDate());
		dm_datatable.setDatatable_create_time(DateUtil.getSysTime());
		dm_datatable.setDdlc_date(DateUtil.getSysDate());
		dm_datatable.setDdlc_time(DateUtil.getSysTime());
		dm_datatable.setDatac_date(DateUtil.getSysDate());
		dm_datatable.setDatac_time(DateUtil.getSysTime());
		dm_datatable.setEtl_date(ZeroDate);
		dm_datatable.add(Dbo.db());
	}

	@Method(desc = "获取加工函数映射可用的函数", logicStep = "1.查询Edw_sparksql_gram表获取可用的函数")
	@Return(desc = "返回加工函数映射可用的函数", range = "无限制")
	public Map<String, List<Object>> getSparkSqlGram() {
		//1.查询Edw_sparksql_gram表获取可用的函数
		List<Edw_sparksql_gram> sparkSqlGramsList = Dbo.queryList(Edw_sparksql_gram.class,
			"select * from " + Edw_sparksql_gram.TableName + " ORDER BY function_name");

		Map<String, List<Object>> sparkSqlGramsMap = new LinkedHashMap<>();
		List<Object> classifyList = new ArrayList<>();
		sparkSqlGramsList.forEach(itemBean -> {
			if (sparkSqlGramsMap.containsKey(itemBean.getFunction_classify())) {
				sparkSqlGramsMap.get(itemBean.getFunction_classify()).add(itemBean);
			} else {
				classifyList.add(itemBean.getFunction_classify());
				List<Object> itemList = new ArrayList<>();
				itemList.add(itemBean);
				sparkSqlGramsMap.put(itemBean.getFunction_classify(), itemList);
			}
		});
		sparkSqlGramsMap.put("classify", classifyList);
		return sparkSqlGramsMap;
	}

	@Method(desc = "获取预聚合SQL数据信息", logicStep = "1.根据表ID获取预聚合数据信息")
	@Param(name = "datatable_id", range = "集市表ID信息", desc = "不可为空")
	@Return(desc = "返回当前表ID的预聚合SQL信息", range = "可为空,首次配置")
	public List<Map<String, Object>> prePolymerization(long datatable_id) {

		return Dbo.queryList("SELECT * FROM " + Cb_preaggregate.TableName + " WHERE datatable_id = ?", datatable_id);
	}

	@Method(desc = "获取预聚合SQL数据信息", logicStep = "1.根据表ID获取预聚合数据信息")
	@Param(name = "preaggregate", range = "预聚合保存数据信息", desc = "不可为空", isBean = true)
	public void savePrePolymerization(Cb_preaggregate[] preaggregate) {

		List<String> nameList = new ArrayList<>();
		for (Cb_preaggregate cb_preaggregate : preaggregate) {

			if (!nameList.contains(cb_preaggregate.getAgg_name())) {
				nameList.add(cb_preaggregate.getAgg_name());
			} else {
				throw new BusinessException("预聚合名称: " + cb_preaggregate.getAgg_name() + " 重复");
			}
			Validator.notBlank(cb_preaggregate.getAgg_name(), "预聚合名称不能为空");
			Validator.notBlank(cb_preaggregate.getAgg_sql(), "预聚合SQL不能为空");

			//更新预聚合
			if (cb_preaggregate.getAgg_id() != null && cb_preaggregate.getAgg_id() != 0) {
				cb_preaggregate.update(Dbo.db());
			} else {
				//保存预聚合SQL信息
				cb_preaggregate.setAgg_id(PrimayKeyGener.getNextId());
				cb_preaggregate.setAgg_date(DateUtil.getSysDate());
				cb_preaggregate.setAgg_time(DateUtil.getSysTime());
				cb_preaggregate.setAgg_status(JobExecuteState.ShiBai.getCode());
				cb_preaggregate.add(Dbo.db());
			}
		}
	}

	@Method(desc = "删除预聚合SQL", logicStep = "1.根据预聚合ID删除预聚合数据信息")
	@Param(name = "agg_id", range = "预聚合主键ID", desc = "不可为空")
	public void deletePrePolymerization(long agg_id) {

		DboExecute.deletesOrThrow("删除的预聚合信息超出了范围", "DELETE FROM "
			+ Cb_preaggregate.TableName + " WHERE agg_id = ?", agg_id);
	}

	@Method(desc = "上传Excel文件",
		logicStep = "接收excel文件并解析文件入库")
	@Param(name = "file", desc = "上传文件,文件名为作业配置对应那几张表名", range = "以每个模块对应表名为文件名")
	@Param(name = "data_mart_id", desc = "Dm_info主键，加工工程ID", range = "加工工程主键data_mart_id")
	@UploadFile
	public void uploadExcelFile2(String file, Long data_mart_id) {
		FileInputStream fis = null;
		Workbook workBook = null;
		try {
			File uploadedFile = FileUploadUtil.getUploadedFile(file);
			if (!uploadedFile.exists()) {
				throw new BusinessException("上传文件不存在！");
			}
			//创建的输入流
			String path = uploadedFile.toPath().toString();
			fis = new FileInputStream(path);
			//判断文件后缀名
			if (path.toLowerCase().endsWith(xlsxSuffix)) {
				try {
					workBook = new XSSFWorkbook(fis);
				} catch (IOException e) {
					throw new BusinessException("定义XSSFWorkbook失败");
				}
			} else if (path.toLowerCase().endsWith(xlsSuffix)) {
				try {
					workBook = new HSSFWorkbook(fis);
				} catch (IOException e) {
					throw new BusinessException("定义XSSFWorkbook失败");
				}
			} else {
				throw new BusinessException("文件格式不正确，不是excel文件");
			}
			MappingExcelImExport mappingExcelImExport = new MappingExcelImExport();
			mappingExcelImExport.importExcel(workBook, data_mart_id, getUserId());
			workBook.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("导入excel文件数据失败！");
		}
	}

	@Method(desc = "控制响应头下载加工表的excel信息",
		logicStep = "")
	@Param(name = "datatable_id", desc = "加工数据表主键", range = "String类型加工表主键")
	@Param(name = "tablename", desc = "加工数据表表名", range = "String类型加工表表名")
	@Return(desc = "查询返回结果集", range = "无限制")
	public void exportMappingExcel(Long datatable_id, String tablename) {
		String fileName = tablename + ".xlsx";
		try (OutputStream out = ResponseUtil.getResponse().getOutputStream();
		     XSSFWorkbook workbook = new XSSFWorkbook()) {
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
			MappingExcelImExport mappingExcelImExport = new MappingExcelImExport();
			int i = 0;
			mappingExcelImExport.generateMappingExcel(workbook, datatable_id, "映射模式", i);
			Dm_datatable dm_datatable = new Dm_datatable();
			dm_datatable.setDatatable_id(datatable_id);
			List<Dm_datatable> dm_datatables = Dbo.queryList(Dm_datatable.class, "select * from " + Dm_datatable.TableName + " where remark = ?", String.valueOf(dm_datatable.getDatatable_id()));
			if (!dm_datatables.isEmpty()) {

				for (Dm_datatable dm_datatable1 : dm_datatables) {
					Long datatable_id1 = dm_datatable1.getDatatable_id();
					i = mappingExcelImExport.generateMappingExcel(workbook, datatable_id1, "映射模式-子表", i);
				}
			}
			workbook.write(out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("加工数据表下载错误");
		}
	}

	@Method(desc = "根据表名获取关系表及其字段",
		logicStep = "")
	@Param(name = "tableNameListString", desc = "加工页面勾选的表", range = "String类加工表名")
	@Return(desc = "查询返回结果集", range = "无限制")
	public List<RelationTable> getRelationTable(String tableNameListString) throws Exception {
		List<RelationTable> relationTables = new ArrayList<>();
		List<String> tableNameList = Arrays.asList(tableNameListString.split(",", -1));
		for (String tablename : tableNameList) {
			RelationTable relation = getRelation(tablename);
			if (relation.getSubTables().isEmpty()) {
				throw new BusinessSystemException("表" + tablename + "暂时没有关系，请选择其他表");
			}
			relationTables.add(relation);
		}
		return relationTables;
	}

	/**
	 * 封装一个根据表名获取该表关系的方法
	 *
	 * @param tablename
	 * @return
	 */
	private RelationTable getRelation(String tablename) {
		RelationTable relationTable = new RelationTable();
		relationTable.setTablename(tablename);
		List<Dbm_fk_info_tab> main_dbm_fk_info_tabs = Dbo.queryList(Dbm_fk_info_tab.class, "select * from " + Dbm_fk_info_tab.TableName + " where lower(fk_table_code) = ?", tablename.toLowerCase());
		if (!main_dbm_fk_info_tabs.isEmpty()) {
			for (Dbm_fk_info_tab dbm_fk_info_tab : main_dbm_fk_info_tabs) {
				RelationTable.subTable subTable = relationTable.new subTable();
				subTable.setJointype("LEFT JOIN");
				subTable.setMaincolumn(dbm_fk_info_tab.getFk_col_code());
				subTable.setSubcolumn(dbm_fk_info_tab.getCol_code());
				List<Map<String, Object>> columnsByTablenames = getColumnsByTablename(tablename);
				if (!columnsByTablenames.isEmpty()) {
					subTable.setColumns(columnsByTablenames);
					relationTable.getSubTables().add(subTable);
				}
			}
		}
		List<Dbm_fk_info_tab> sub_dbm_fk_info_tabs = Dbo.queryList(Dbm_fk_info_tab.class, "select * from " + Dbm_fk_info_tab.TableName + " where lower(table_code) = ?", tablename.toLowerCase());
		if (!sub_dbm_fk_info_tabs.isEmpty()) {
			for (Dbm_fk_info_tab dbm_fk_info_tab : sub_dbm_fk_info_tabs) {
				RelationTable.subTable subTable = relationTable.new subTable();
				subTable.setJointype("RIGHT JOIN");
				subTable.setMaincolumn(dbm_fk_info_tab.getFk_col_code());
				subTable.setSubcolumn(dbm_fk_info_tab.getCol_code());
				List<Map<String, Object>> columnsByTablenames = getColumnsByTablename(tablename);
				if (!columnsByTablenames.isEmpty()) {
					subTable.setColumns(columnsByTablenames);
					relationTable.getSubTables().add(subTable);
				}
			}
		}
		return relationTable;
	}

	private List<Map<String, Object>> getColumnsByTablename(String tablename) {
		//DCL
		List<Map<String, Object>> maps = Dbo.queryList(
			"select column_name as columnname,t2.hyren_name as tablename  from "
				+ Table_column.TableName +
				" t1 left join " + Data_store_reg.TableName
				+ " t2 on t1.table_id = t2.table_id where lower(t2.hyren_name) = ? and upper(column_name) not in (?,?,?,?,?,?)",
			tablename.toLowerCase(),
			Constant.SDATENAME, Constant.EDATENAME, Constant.MD5NAME, Constant.HYREN_OPER_DATE, Constant.HYREN_OPER_TIME,
			Constant.HYREN_OPER_PERSON);
		if (!maps.isEmpty()) {
			return maps;
		} else {
			//DML
			maps = Dbo.queryList(
				"select field_en_name as columnname,t2.datatable_en_name as tablename from "
					+ Datatable_field_info.TableName +
					"t1 left join " + Dm_datatable.TableName + " t2 on t1.datatable_id = t2.datatable_id " +
					" where lower(t2.datatable_en_name) = ? and upper(field_en_name) not in (?,?,?) AND end_date = ?",
				tablename.toLowerCase(), Constant.SDATENAME, Constant.EDATENAME, Constant.MD5NAME,
				Constant.MAXDATE);
			if (!maps.isEmpty()) {
				return maps;
			} else {
				//UDL
				//查询表字段信息
				maps = Dbo.queryList(
					"select column_name as columnname,t2.table_name as tablename" +
						" FROM " + Dq_table_column.TableName + "t1 left join " + Dq_table_info.TableName + " t2 on t1.table_id = t2.table_id" +
						" WHERE lower(t2.table_name) =?", tablename.toLowerCase());
				if (!maps.isEmpty()) {
					return maps;
				} else {
					//TODO 如果根据表名 没有找到对应的该表的字段 则返回空
					//TODO 后续还有层级 需要补充
					return null;
				}
			}
		}
	}

}
