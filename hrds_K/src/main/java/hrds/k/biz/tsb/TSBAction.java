package hrds.k.biz.tsb;

import com.alibaba.fastjson.TypeReference;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.DbmMode;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.tree.background.TreeNodeInfo;
import hrds.commons.tree.background.bean.TreeConf;
import hrds.commons.tree.commons.TreePageSource;
import hrds.commons.utils.DataTableFieldUtil;
import hrds.commons.utils.DataTableUtil;
import hrds.commons.utils.PropertyParaValue;
import hrds.commons.utils.key.PrimayKeyGener;
import hrds.commons.utils.tree.Node;
import hrds.commons.utils.tree.NodeDataConvertedTreeList;
import hrds.k.biz.dbm.normbasic.DbmNormbasicAction;
import hrds.k.biz.tsb.bean.DbmColInfo;
import hrds.k.biz.tsb.bean.TSBConf;

import java.lang.reflect.Type;
import java.util.*;

@DocClass(desc = "表结构对标", author = "BY-HLL", createdate = "2020/3/12 0012 上午 09:10")
public class TSBAction extends BaseAction {

    private static final TSBConf tsbConf = new TSBConf();

    @Method(desc = "获取表结构对标树", logicStep = "获取表结构对标树")
    @Return(desc = "表结构对标树信息", range = "表结构对标树信息")
    public List<Node> getTSBTreeData() {
        //配置树不显示文件采集的数据
        TreeConf treeConf = new TreeConf();
        treeConf.setShowFileCollection(Boolean.FALSE);
        //根据源菜单信息获取节点数据列表
        List<Map<String, Object>> dataList =
                TreeNodeInfo.getTreeNodeInfo(TreePageSource.DATA_BENCHMARKING, getUser(), treeConf);
        //转换节点数据列表为分叉树列表
        return NodeDataConvertedTreeList.dataConversionTreeInfo(dataList);
    }

    @Method(desc = "获取表字段信息列表", logicStep = "获取表字段信息列表")
    @Param(name = "data_layer", desc = "数据层", range = "String类型,DCL,DML")
    @Param(name = "data_own_type", desc = "类型标识", range = "dcl_batch:批量数据,dcl_realtime:实时数据", nullable = true)
    @Param(name = "file_id", desc = "表源属性id", range = "String[]")
    @Return(desc = "字段信息列表", range = "字段信息列表")
    public List<Map<String, Object>> getColumnByFileId(String data_layer, String data_own_type, String file_id) {
        //设置 Dbm_normbm_detect 对象
        setDbmNormbmDetect(data_layer);
        //设置 Dbm_dtable_info 对象
        setDbmDtableInfo(file_id, data_layer);
        //数据层获取不同表结构
        return DataTableUtil.getColumnByFileId(data_layer, data_own_type, file_id);
    }

    @Method(desc = "预测对标结果", logicStep = "预测对标结果")
    @Param(name = "dbmColInfos", desc = "待保存字段信息集合", range = "DbmColInfo自定义实体类型", isBean = true)
    public void predictBenchmarking(DbmColInfo[] dbmColInfos) {
        //设置待检测字段信息
        setDbmDtcolInfo(dbmColInfos);
        //获取待检测字段信息
        List<Dbm_dtcol_info> dbm_dtcol_infos = tsbConf.getDbm_dtcol_info_list();
        //获取预测接口地址信息
        String predict_address = PropertyParaValue.getString("predict_address", "http://127.0.0.1:38081/predict");
        //设置请求参数
        List<Map<String, String>> params = new ArrayList<>();
        dbm_dtcol_infos.forEach(dbm_dtcol_info -> {
            Map<String, String> map = new HashMap<>();
            if (StringUtil.isBlank(dbm_dtcol_info.getCol_id().toString())) {
                throw new BusinessException("检测字段主键为空!");
            }
            map.put("col_id", dbm_dtcol_info.getCol_id().toString());
            if (StringUtil.isBlank(dbm_dtcol_info.getCol_cname())) {
                throw new BusinessException("检测字段中文名为空!");
            }
            map.put("col_cnname", dbm_dtcol_info.getCol_cname());
            map.put("col_ename", dbm_dtcol_info.getCol_ename());
            map.put("col_desc", dbm_dtcol_info.getCol_remark());
            params.add(map);
        });
        //根据接口获取预测结果
        String bodyString = new HttpClient()
                .addData("content", JsonUtil.toJson(params)).post(predict_address).getBodyString();
        ActionResult ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(() ->
                new BusinessException("URL请求失败!" + predict_address));
        //初始化结果数据List
        List<Map<String, String>> dbm_normbmd_info_list = new ArrayList<>();
        //设置预测结果集合数据类型为 List<Map<String, Object>>
        Type type = new TypeReference<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> predictDataList = JsonUtil.toObject(ar.getData().toString(), type);
        //处理每个字段的预测结果
        predictDataList.forEach(predictData -> {
            Type lo = new TypeReference<List<Object>>() {
            }.getType();
            List<Object> predicts = JsonUtil.toObject(predictData.get("predict").toString(), lo);
            String col_id = predictData.get("col_id").toString();
            //处理每个字段预测结果的匹配度
            predicts.forEach(predict -> {
                List<Object> pMap = JsonUtil.toObject(predict.toString(), lo);
                Type mss = new TypeReference<Map<String, String>>() {
                }.getType();
                Map<String, String> predictMap = JsonUtil.toObject(pMap.get(0).toString(), mss);
                String standard_id = predictMap.get("standard_id");
                String col_similarity = pMap.get(1).toString();
                //设置预测结果集信息
                Map<String, String> map = new HashMap<>();
                map.put("col_id", col_id);
                map.put("standard_id", standard_id);
                map.put("col_similarity", col_similarity);
                dbm_normbmd_info_list.add(map);
            });
        });
        //设置 Dbm_normbmd_result
        setDbmNormbmdResult(dbm_normbmd_info_list);
    }

    @Method(desc = "获取预测结果信息", logicStep = "获取预测结果信息")
    @Return(desc = "预测结果信息", range = "预测结果信息")
    public List<Map<String, Object>> getPredictResult() {
        List<Map<String, Object>> predictResult = new ArrayList<>();
        //获取对标结果字段信息
        List<Dbm_dtcol_info> dbm_dtcol_info_s = tsbConf.getDbm_dtcol_info_list();
        if (null == dbm_dtcol_info_s || dbm_dtcol_info_s.isEmpty()) {
            throw new BusinessException("对标信息已失效,请重新选择字段进行对标!");
        }
        dbm_dtcol_info_s.forEach(dbm_dtcol_info -> {
            Map<String, Object> map = new HashMap<>();
            map.put("col_id", dbm_dtcol_info.getCol_id());
            map.put("col_cname", dbm_dtcol_info.getCol_cname());
            map.put("col_ename", dbm_dtcol_info.getCol_ename());
            //获取预测结果信息
            List<Map<String, String>> pList = new ArrayList<>();
            List<Dbm_normbmd_result> dbm_normbmd_result_s = tsbConf.getDbm_normbmd_result_list();
            dbm_normbmd_result_s.forEach(dbm_normbmd_result -> {
                if (dbm_dtcol_info.getCol_id().equals(dbm_normbmd_result.getCol_id())) {
                    Map<String, String> pMap = new HashMap<>();
                    //通过标准的basic_id获取标准信息
                    DbmNormbasicAction dbmNormbasicAction = new DbmNormbasicAction();
                    Optional<Dbm_normbasic> dbmNormbasic =
                            dbmNormbasicAction.getDbmNormbasicInfoById(dbm_normbmd_result.getBasic_id());
                    if (dbmNormbasic.isPresent()) {
                        pMap.put("result_id", dbm_normbmd_result.getResult_id());
                        pMap.put("norm_ename", dbmNormbasic.get().getNorm_ename());
                        pMap.put("norm_cname", dbmNormbasic.get().getNorm_cname());
                    }
                    pMap.put("col_similarity", dbm_normbmd_result.getCol_similarity().toString());
                    pList.add(pMap);
                }
            });
            map.put("predict", pList);
            predictResult.add(map);
        });
        return predictResult;
    }

    @Method(desc = "保存表结构对标数据", logicStep = "保存表结构对标数据")
    @Param(name = "dbm_normbmd_info_s", desc = "对标结果数据信息",
            range = "String类型,JsonArray格式,[{col_id:'',result_id:'',is_artificial:''}]" +
                    "col_id: 字段id,唯一性" +
                    "result_id: 对标最终选择结果id" +
                    "is_artificial: 是否人工对标,IsFlag标识,1:是,0:否")
    public void saveTSBConfData(String dbm_normbmd_info_s) {
        //字符串类型内容转换成List<Map<String, String>>
        Type type = new TypeReference<List<Map<String, String>>>() {
        }.getType();
        List<Map<String, String>> dbm_normbmd_info_list = JsonUtil.toObject(dbm_normbmd_info_s, type);
        //数据校验
        if (null == dbm_normbmd_info_list || dbm_normbmd_info_list.isEmpty()) {
            throw new BusinessException("待保存的信息已失效,请重新选择字段并进行对标结果定义后再保存!");
        }
        dbm_normbmd_info_list.forEach(dbm_normbmd_info -> {
            if (StringUtil.isBlank(dbm_normbmd_info.get("col_id"))) {
                throw new BusinessException("字段:" + dbm_normbmd_info.get("col_ename") + "对标字段id为空!");
            }
            if (StringUtil.isBlank(dbm_normbmd_info.get("is_artificial"))) {
                throw new BusinessException("字段:" + dbm_normbmd_info.get("col_ename") + "对标结果为空,请选择对标结果!");
            }
            if (StringUtil.isBlank(dbm_normbmd_info.get("result_id"))) {
                throw new BusinessException("字段:" + dbm_normbmd_info.get("col_ename") + "对标最终选择结果为空!");
            }
        });
        //更新 Dbm_normbmd_result
        dbm_normbmd_info_list.forEach(dbm_normbmd_info -> {
            //如果不是人工对标,标记选中的对标结果为最终结果
            if (dbm_normbmd_info.get("is_artificial").equals(IsFlag.Fou.getCode())) {
                List<Dbm_normbmd_result> dbm_normbmd_result_s = tsbConf.getDbm_normbmd_result_list();
                if (null == dbm_normbmd_result_s || dbm_normbmd_result_s.isEmpty()) {
                    throw new BusinessException("待标记为最终结果的对标结果已失效,请重新选择字段并进行对标后再保存!");
                }
                //修改保存结果集中选中的结果为最终结果
                dbm_normbmd_result_s.forEach(dbm_normbmd_result -> {
                    if (dbm_normbmd_info.get("result_id").equals(dbm_normbmd_result.getResult_id())) {
                        dbm_normbmd_result.setIs_tag(IsFlag.Shi.getCode());
                    }
                });
            }
            //如果是人工对标则保存人工对标结果
            else if (dbm_normbmd_info.get("is_artificial").equals(IsFlag.Shi.getCode())) {
                Dbm_normbmd_result dbm_normbmd_result = new Dbm_normbmd_result();
                dbm_normbmd_result.setResult_id(String.valueOf(PrimayKeyGener.getNextId()));
                //如果是人工对标,字段相识度和描述相似度都为1
                dbm_normbmd_result.setCol_similarity(IsFlag.Shi.getCode());
                dbm_normbmd_result.setRemark_similarity(IsFlag.Shi.getCode());
                dbm_normbmd_result.setDetect_id(tsbConf.getDbm_normbm_detect().getDetect_id());
                dbm_normbmd_result.setCol_id(dbm_normbmd_info.get("col_id"));
                //如果是人工对标,result_id是页面选中标准的basic_id
                dbm_normbmd_result.setBasic_id(dbm_normbmd_info.get("result_id"));
                dbm_normbmd_result.setIs_artificial(dbm_normbmd_info.get("is_artificial"));
                dbm_normbmd_result.setIs_tag(IsFlag.Shi.getCode());
                tsbConf.getDbm_normbmd_result_list().add(dbm_normbmd_result);
            } else {
                throw new BusinessException("对标方式为空或者对标方式不正确!" + dbm_normbmd_info.get("is_artificial"));
            }
        });
        //结果存入数据库
        //设置检测结束时间
        Dbm_normbm_detect dbm_normbm_detect = tsbConf.getDbm_normbm_detect();
        dbm_normbm_detect.setDetect_edate(DateUtil.getSysDate());
        dbm_normbm_detect.setDetect_etime(DateUtil.getSysTime());
        //保存对标检测记录表表信息
        tsbConf.setDbm_normbm_detect(dbm_normbm_detect);
        tsbConf.getDbm_normbm_detect().add(Dbo.db());
        //保存对标检测表信息表
        tsbConf.getDbm_dtable_info().add(Dbo.db());
        //保存对标检测字段信息表
        tsbConf.getDbm_dtcol_info_list().forEach(dbm_dtcol_info -> dbm_dtcol_info.add(Dbo.db()));
        //保存对标检测结果表
        tsbConf.getDbm_normbmd_result_list().forEach(dbm_normbmd_result -> dbm_normbmd_result.add(Dbo.db()));
        //提交所有数据库执行操作
        SqlOperator.commitTransaction(Dbo.db());
        //数据保存完成后,清理本次操作数据
        tsbConf.setDbm_normbm_detect(new Dbm_normbm_detect());
        tsbConf.setDbm_dtable_info(new Dbm_dtable_info());
        tsbConf.getDbm_dtcol_info_list().clear();
        tsbConf.getDbm_normbmd_result_list().clear();
    }

    @Method(desc = "设置对标检测记录",
            logicStep = "设置对标检测记录")
    @Param(name = "data_layer", desc = "数据层", range = "String类型,DCL,DML")
    private void setDbmNormbmDetect(String data_layer) {
        //数据校验
        if (StringUtil.isBlank(data_layer)) {
            throw new BusinessException("数据来源类型为空!");
        }
        //设置 Dbm_normbm_detect
        Dbm_normbm_detect dbm_normbm_detect = new Dbm_normbm_detect();
        dbm_normbm_detect.setDetect_id(String.valueOf(PrimayKeyGener.getNextId()));
        dbm_normbm_detect.setDetect_name(dbm_normbm_detect.getDetect_id());
        dbm_normbm_detect.setSource_type(data_layer);
        dbm_normbm_detect.setIs_import(IsFlag.Fou.getCode());
        dbm_normbm_detect.setDetect_status(IsFlag.Shi.getCode());
        dbm_normbm_detect.setDbm_mode(DbmMode.BiaoJieGouDuiBiao.getCode());
        dbm_normbm_detect.setCreate_user(getUserId().toString());
        dbm_normbm_detect.setDetect_sdate(DateUtil.getSysDate());
        dbm_normbm_detect.setDetect_stime(DateUtil.getSysTime());
        tsbConf.setDbm_normbm_detect(dbm_normbm_detect);
    }

    @Method(desc = "设置对标检测表信息表", logicStep = "设置对标检测表信息表")
    @Param(name = "file_id", desc = "表源属性id", range = "String类型")
    @Param(name = "data_layer", desc = "数据层", range = "String类型,DCL,DML")
    private void setDbmDtableInfo(String file_id, String data_layer) {
        //根据表源属性id获取表信息
        Map<String, Object> tableInfo = DataTableUtil.getTableInfoAndColumnInfo(data_layer, file_id);
        if (tableInfo.isEmpty()) {
            throw new BusinessException("查询的表信息已经不存在!");
        }
        //设置 Dbm_dtable_info
        Dbm_dtable_info dbm_dtable_info = new Dbm_dtable_info();
        dbm_dtable_info.setDbm_tableid(PrimayKeyGener.getNextId());
        dbm_dtable_info.setTable_cname(tableInfo.get("table_ch_name").toString());
        dbm_dtable_info.setTable_ename(tableInfo.get("table_name").toString());
        dbm_dtable_info.setIs_external(IsFlag.Fou.getCode());
        //如果源表的描述为null,则设置为""
        if (null == tableInfo.get("remark")) {
            dbm_dtable_info.setTable_remark("");
        } else {
            dbm_dtable_info.setTable_remark(tableInfo.get("remark").toString());
        }
        dbm_dtable_info.setDetect_id(tsbConf.getDbm_normbm_detect().getDetect_id());
        dbm_dtable_info.setTable_id(tableInfo.get("table_id").toString());
        if (null == tableInfo.get("source_id")) {
            dbm_dtable_info.setSource_id("");
        } else {
            dbm_dtable_info.setSource_id(tableInfo.get("source_id").toString());
        }
        if (null == tableInfo.get("agent_id")) {
            dbm_dtable_info.setAgent_id("");
        } else {
            dbm_dtable_info.setAgent_id(tableInfo.get("agent_id").toString());
        }
        if (null == tableInfo.get("database_id")) {
            dbm_dtable_info.setDatabase_id("");
        } else {
            dbm_dtable_info.setDatabase_id(tableInfo.get("database_id").toString());
        }
        tsbConf.setDbm_dtable_info(dbm_dtable_info);
    }

    @Method(desc = "设置对标检测字段信息表",
            logicStep = "设置对标检测字段信息表")
    @Param(name = "dbmColInfos", desc = "字段信息", range = "DbmColInfo自定义实体类型")
    @Return(desc = "检测字段信息表信息集合", range = "检测字段信息表信息集合")
    private void setDbmDtcolInfo(DbmColInfo[] dbmColInfos) {
        //设置 Dbm_dtcol_info 对象
        List<Dbm_dtcol_info> dbm_dtcol_info_list = new ArrayList<>();
        for (DbmColInfo col_info : dbmColInfos) {
            Dbm_dtcol_info dbm_dtcol_info = new Dbm_dtcol_info();
            dbm_dtcol_info.setCol_id(PrimayKeyGener.getNextId());
            dbm_dtcol_info.setCol_cname(col_info.getColumn_ch_name());
            dbm_dtcol_info.setCol_ename(col_info.getColumn_name());
            if (null == col_info.getTc_remark()) {
                dbm_dtcol_info.setCol_remark("");
            } else {
                dbm_dtcol_info.setCol_remark(col_info.getTc_remark());
            }
            Map<String, String> col_type_map = DataTableFieldUtil.parsingFiledType(col_info.getColumn_type());
            dbm_dtcol_info.setData_type(col_type_map.get("data_type"));
            dbm_dtcol_info.setData_len(col_type_map.get("data_len"));
            dbm_dtcol_info.setDecimal_point(col_type_map.get("decimal_point"));
            dbm_dtcol_info.setIs_key(col_info.getIs_primary_key());
            dbm_dtcol_info.setIs_null(IsFlag.Fou.getCode());
            dbm_dtcol_info.setDefault_value("");
            dbm_dtcol_info.setDbm_tableid(tsbConf.getDbm_dtable_info().getDbm_tableid());
            dbm_dtcol_info.setDetect_id(tsbConf.getDbm_normbm_detect().getDetect_id());
            dbm_dtcol_info.setColumn_id(col_info.getColumn_id());
            dbm_dtcol_info.setDatabase_id(col_info.getDatabase_id());
            dbm_dtcol_info.setAgent_id(col_info.getAgent_id());
            dbm_dtcol_info.setSource_id(col_info.getSource_id());
            dbm_dtcol_info_list.add(dbm_dtcol_info);
            tsbConf.setDbm_dtcol_info_list(dbm_dtcol_info_list);
        }
    }

    @Method(desc = "设置对标检测结果表",
            logicStep = "逻辑说明")
    @Param(name = "dbm_normbmd_info_list", desc = "对标结果集合",
            range = "List类型,[{col_id:'',col_similarity:'',standard_id:'',is_artificial:''}]")
    private static void setDbmNormbmdResult(List<Map<String, String>> dbm_normbmd_info_list) {
        List<Dbm_normbmd_result> dbm_normbmd_results_list = new ArrayList<>();
        dbm_normbmd_info_list.forEach(dbm_normbmd_info -> {
            //设置对标检测结果对象
            Dbm_normbmd_result dbm_normbmd_result = new Dbm_normbmd_result();
            dbm_normbmd_result.setResult_id(String.valueOf(PrimayKeyGener.getNextId()));
            dbm_normbmd_result.setCol_similarity(dbm_normbmd_info.get("col_similarity"));
            dbm_normbmd_result.setRemark_similarity("0");
            dbm_normbmd_result.setDetect_id(tsbConf.getDbm_normbm_detect().getDetect_id());
            dbm_normbmd_result.setCol_id(dbm_normbmd_info.get("col_id"));
            //根据 standard_id 获取对应标准元id
            dbm_normbmd_result.setBasic_id(getDbmNormbasicInfoById(dbm_normbmd_info.get("standard_id")));
            dbm_normbmd_result.setIs_artificial(IsFlag.Fou.getCode());
            dbm_normbmd_result.setIs_tag(IsFlag.Fou.getCode());
            dbm_normbmd_results_list.add(dbm_normbmd_result);
            tsbConf.setDbm_normbmd_result_list(dbm_normbmd_results_list);
        });
    }

    @Method(desc = "根据标准编号获取标准id", logicStep = "根据标准编号获取标准id")
    @Param(name = "norm_code", desc = "标准编码", range = "String类型,该值唯一")
    @Return(desc = "basic_id标准元id", range = "basic_id标准元id")
    private static Long getDbmNormbasicInfoById(String norm_code) {
        List<Long> list = Dbo.queryOneColumnList("select basic_id from " + Dbm_normbasic.TableName +
                " where norm_code = ?", norm_code);
        if ((list.size() != 1)) {
            throw new BusinessException(norm_code + "标准编号已不存在或者标准编号不唯一,请检查标准信息!");
        }
        return list.get(0);
    }
}
