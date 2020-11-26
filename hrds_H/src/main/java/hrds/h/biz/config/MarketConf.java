package hrds.h.biz.config;


import fd.ng.core.utils.Validator;
import hrds.commons.entity.Data_store_layer;
import hrds.commons.entity.Data_store_layer_attr;
import hrds.commons.entity.Datatable_field_info;
import hrds.commons.entity.Dm_datatable;
import hrds.commons.entity.Dtab_relation_store;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 集市作业配置类
 *
 * @Author: Mick
 */
public class MarketConf implements Serializable {

	/**
	 * 集市任务设置id
	 */
	private final String datatableId;
	/**
	 * 调度日期
	 */
	private final String etlDate;
	/**
	 * sql动态参数
	 */
	private final String sqlParams;
	/**
	 * 是否是属于重跑 当属于重跑时，需要将当天已经跑过的数据清除掉
	 */
	private boolean rerun;
	/**
	 * 目的地表名
	 */
	private String tableName;
	/**
	 * 需要执行的主sql,会将贴源登记的前缀替换替换掉
	 */
	private String completeSql;
	private String beforeReplaceSql;
	/**
	 * 多作业导入同一表
	 */
	private boolean multipleInput;
	/**
	 * 是否有分组映射
	 */
	private boolean isGroup;
	/**
	 * 是否是增量
	 */
	private boolean isIncrement;
	/**
	 * 前置作业sql 可为null 可以是多个sql用;;分割
	 */
	private String preSql;
	/**
	 * 后置作业sql 可为null 可以是多个sql用;;分割
	 */
	private String finalSql;
	/**
	 * 数据表信息
	 */
	private Dm_datatable dmDatatable = null;
	/**
	 * 集市字段信息
	 */
	private List<Datatable_field_info> datatableFields;
	/**
	 * 集市表存储关系表
	 */
	private Dtab_relation_store dtabRelationStore = null;
	/**
	 * 集市存储层配置表
	 */
	private Data_store_layer dataStoreLayer = null;
	/**
	 * 数据存储层配置属性表
	 */
	private List<Data_store_layer_attr> dataStoreLayerAttrs = null;

	/**
	 * 存储层字段附加信息属性
	 */
	private Map<String, List<String>> addAttrColMap;

	private MarketConf(String datatableId, String etldate, String sqlParams) {
		this.datatableId = datatableId;
		this.etlDate = etldate;
		this.sqlParams = sqlParams;
	}

	/**
	 * 这里构造一个只需要一个参数的方法,提供给集市生成脚本使用
	 *
	 * @param datatableId : 集市表ID主键
	 */
	public MarketConf(String datatableId) {
		this.datatableId = datatableId;
		this.etlDate = null;
		this.sqlParams = null;
	}

	/**
	 * 获取 集市配置 包括参数检查与初始化
	 *
	 * @param datatableId 集市主表主键
	 * @param etldate     跑批日期
	 * @param sqlParams   sql动态参数
	 * @return 集市配置实体
	 */
	public static MarketConf getConf(String datatableId, String etldate, String sqlParams) {

		//验证输入参数合法性
		MarketConfUtils.checkArguments(datatableId, etldate);
		final MarketConf conf = new MarketConf(datatableId, etldate, sqlParams);
		//初始化实体类
		MarketConfUtils.initBeans(conf);
		//验证是否属于重跑
		MarketConfUtils.checkReRun(conf, etldate);

		return conf;
	}

	/**
	 * 获取 集市配置 包括参数检查与初始化
	 * 提供给集市生成脚本使用
	 * @param datatableId 集市主表主键
	 * @return 集市配置实体
	 */
	public static MarketConf getConf(String datatableId) {

		//验证输入参数合法性
		Validator.notBlank(datatableId, String.format("集市信息id不可为空: %s", datatableId));
		final MarketConf conf = new MarketConf(datatableId);
		//初始化实体类
		MarketConfUtils.initBeans(conf);

		return conf;
	}

	public boolean isIncrement() {
		return isIncrement;
	}

	public void setIncrement(boolean increment) {
		isIncrement = increment;
	}

	public boolean isMultipleInput() {
		return multipleInput;
	}

	public void setMultipleInput(boolean multipleInput) {
		this.multipleInput = multipleInput;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean group) {
		isGroup = group;
	}

	public boolean isRerun() {
		return rerun;
	}

	void setRerun(boolean rerun) {
		this.rerun = rerun;
	}

	public String getDatatableId() {
		return datatableId;
	}

	public String getEtlDate() {
		return etlDate;
	}

	public String getSqlParams() {
		return sqlParams;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getCompleteSql() {
		return completeSql;
	}

	void setCompleteSql(String completeSql) {
		this.completeSql = completeSql;
	}

	public String getPreSql() {
		return preSql;
	}

	public void setPreSql(String preSql) {
		this.preSql = preSql;
	}

	public String getFinalSql() {
		return finalSql;
	}

	public void setFinalSql(String finalSql) {
		this.finalSql = finalSql;
	}

	public Dm_datatable getDmDatatable() {
		return dmDatatable;
	}

	public void setDmDatatable(Dm_datatable dmDatatable) {
		this.dmDatatable = dmDatatable;
	}

	public List<Datatable_field_info> getDatatableFields() {
		return datatableFields;
	}

	public void setDatatableFields(List<Datatable_field_info> datatableFields) {
		this.datatableFields = datatableFields;
	}

	public Dtab_relation_store getDtabRelationStore() {
		return dtabRelationStore;
	}

	void setDtabRelationStore(Dtab_relation_store dtabRelationStore) {
		this.dtabRelationStore = dtabRelationStore;
	}

	public Data_store_layer getDataStoreLayer() {
		return dataStoreLayer;
	}

	void setDataStoreLayer(Data_store_layer dataStoreLayer) {
		this.dataStoreLayer = dataStoreLayer;
	}

	public List<Data_store_layer_attr> getDataStoreLayerAttrs() {
		return dataStoreLayerAttrs;
	}

	void setDataStoreLayerAttrs(List<Data_store_layer_attr> dataStoreLayerAttrs) {
		this.dataStoreLayerAttrs = dataStoreLayerAttrs;
	}

	public Map<String, List<String>> getAddAttrColMap() {
		return addAttrColMap;
	}

	public void setAddAttrColMap(Map<String, List<String>> addAttrColMap) {
		this.addAttrColMap = addAttrColMap;
	}

	public String getBeforeReplaceSql() {
		return beforeReplaceSql;
	}

	public void setBeforeReplaceSql(String beforeReplaceSql) {
		this.beforeReplaceSql = beforeReplaceSql;
	}
}
