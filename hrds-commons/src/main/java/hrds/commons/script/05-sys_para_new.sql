truncate table sys_para;
-- 系统配置
INSERT INTO sys_para VALUES ('1', 'sys_Name', '海云数服', 'system.properties', '系统名称最多输入8个字');
INSERT INTO sys_para VALUES ('2', 'logo_img', 'hyrenlogo.png', 'system.properties', '系统logo图片名称');
INSERT INTO sys_para VALUES ('3', 'logo_depict', '博彦泓智科技（上海）有限公司', 'system.properties', 'logo描述信息');
INSERT INTO sys_para VALUES ('4', 'hyren_host', 'hdp007.beyondsoft.com', 'common.properties', 'HYREN服务器IP');
INSERT INTO sys_para VALUES ('5', 'hyren_port', '2000', 'system.properties', 'HYREN服务器接受进程端口');
INSERT INTO sys_para VALUES ('6', 'hyren_osname', 'linux', 'system.properties', 'HYREN服务器操作系统');
INSERT INTO sys_para VALUES ('7', 'system_language', 'zh_CN', 'system.properties', '服务器系统语言(默认zh_CN,例如英文；en_US)');
INSERT INTO sys_para VALUES ('8', 'sys_dateformat', 'yyyy-MM-dd', 'system.properties', '系统日期参数格式化定义');
INSERT INTO sys_para VALUES ('9', 'hyren_pwd', 'q1w2e3',  'system.properties', '系统默认海云用户的密码');
INSERT INTO sys_para VALUES ('10', 'is_save_operation_log', '0', 'system.properties', '是否保存操作日志 (1:是,0:否)');
-- 公共配置
INSERT INTO sys_para VALUES ('101', 'solrclassname', 'hrds.commons.hadoop.solr.impl.SolrOperatorImpl5_3_1', 'common.properties', 'solr具体实现类全名hrds.commons.hadoop.solr.impl.SolrOperatorImpl5_3_1');
INSERT INTO sys_para VALUES ('102', 'zkHost', 'hdp001.beyondsoft.com:2181,hdp002.beyondsoft.com:2181,hdp003.beyondsoft.com:2181/solr', 'common.properties', 'solr的zookeeper配置');
INSERT INTO sys_para VALUES ('103', 'collection', 'HrdsFullTextIndexing', 'common.properties', 'solr的collection');
INSERT INTO sys_para VALUES ('104', 'solr_bulk_submissions_num', '50000', 'common.properties', 'solr创建索引批量提交数');
INSERT INTO sys_para VALUES ('105', 'sftp_port', '22', 'common.properties', '部署时服务器ssh的端口');
INSERT INTO sys_para VALUES ('902', 'pathprefix', '/hrds', 'common.properties', 'hdfs上的目录规范，此目录必须为hdfs的根目录，即必须以"/"开头');
-- 采集配置
INSERT INTO sys_para VALUES ('201', 'summary_volumn', '3', 'hrds_b.properties', '摘要获取行数');
INSERT INTO sys_para VALUES ('202', 'file_blocksize', '1024', 'hrds_b.properties', '写单个文件大小(单位:M),建议128M的整数倍,默认1G(2G则写成2048)');
INSERT INTO sys_para VALUES ('203', 'writemultiplefiles', '1', 'hrds_b.properties', '卸数时是否写多个文件(默认1 否)');
INSERT INTO sys_para VALUES ('204', 'determineFileChangesType', 'MD5', 'hrds_b.properties', '文件采集或者ftp采集比较文件是否变化的方式:MD5或fileAttr');
INSERT INTO sys_para VALUES ('205', 'singleAvroSize', '536870912', 'hrds_b.properties', '文件采集单个Avro的大小，默认512MB');
INSERT INTO sys_para VALUES ('206', 'thresholdFileSize', '26214400', 'hrds_b.properties', '文件采集大文件阈值，默认25MB');
INSERT INTO sys_para VALUES ('207', 'isAddOperateInfo', 'true', 'hrds_b.properties','采集是否添加操作时间、操作日期、操作人：true或false');
INSERT INTO sys_para VALUES ('208', 'isWriteDictionary', 'false', 'hrds_b.properties','每次数据库抽取结束是否写数据字典：true或false');
INSERT INTO sys_para VALUES ('209', 'agentpath', '/home/hyshf/HRSDATA/agent_download_package/collect/hrds_Agent-5.0.jar', 'hrds_b.properties', '下载agent的文件');
INSERT INTO sys_para VALUES ('210', 'agentDeployPath', '/home/hyshf/HRSDATA/agent_deploy_dir/hrsagent', 'hrds_b.properties', '海云用户默认的安装路径');
INSERT INTO sys_para VALUES ('211', 'agentConfigPath', '/home/hyshf/HRSDATA/agent_config', 'hrds_b.properties', 'agent接受页面配置文件和数据字典存放的顶层目录');
-- 接口配置
INSERT INTO sys_para VALUES ('301', 'restAuthority', '', 'hrds_g.properties', '对于SQL的字段是否使用字段验证');
INSERT INTO sys_para VALUES ('302', 'restFilePath', '/hyren/HRSDATA/bigdata/restFilePath', 'hrds_g.properties', '接口数据文件的存放路径');
INSERT INTO sys_para VALUES ('303', 'isRecordInterfaceLog', '1', 'hrds_g.properties','接口使用日志是否记录标志,1：是，0：否');
-- 集市&加工配置
INSERT INTO sys_para VALUES ('401', 'load.executor.count', '2', 'hrds_h.properties', '集市并发线程数量，当isconcurrent配置为0时，此配置生效，区间在1~4');
INSERT INTO sys_para VALUES ('402', 'mpp.batch.size', '5000', 'hrds_h.properties', 'spark通过jdbc写入到数据库，单批次提交的数量');
INSERT INTO sys_para VALUES ('403', 'isconcurrent', '0', 'hrds_h.properties', '集市是否采用多线程作业，0：是，1：否');
INSERT INTO sys_para VALUES ('404', 'sysnumber', '2000000', 'hrds_h.properties', '数据表记录阈值,超过时使用spark程序生成文件');
-- 数据管控配置
INSERT INTO sys_para VALUES ('501', 'predict_address', 'http://192.168.1.101:38081/predict', 'hrds_k.properties','数据对标-表结构对标预测地址');
INSERT INTO sys_para VALUES ('502', 'algorithms_result_root_path', 'file:///D:/algorithms_result_root_path/', 'hrds_k.properties','数据对标，表函数依赖和主键分析输出结果的根目录');
INSERT INTO sys_para VALUES ('503', 'algorithms_python_serve', 'http://127.0.0.1:33333/', 'hrds_k.properties','数据对标，调用python服务的url');
INSERT INTO sys_para VALUES ('504', 'algorithms_spark_classpath', 'hrds_K/build/libs/hrds_K-5.0.jar;lib/*;hrds_K/src/main/resources/;spark/jars/*;libs/runtime/*;hrds-commons/build/libs/hrds-commons-5.0.jar', 'hrds_k.properties','数据对标算主键函数依赖执行spark程序所依赖的classpath');
-- 作业配置
INSERT INTO sys_para VALUES ('601', 'etlDeployPath', '/home/hyshf/HRSDATA/agent_deploy_dir/etlagent','hrds_c.properties', '作业调度默认部署路径');
INSERT INTO sys_para VALUES ('602', 'controlPath', '/home/hyshf/HRSDATA/agent_download_package/etl/hrds_Control-5.0.jar','hrds_c.properties','CONTROL程序jar包地址');
INSERT INTO sys_para VALUES ('603', 'triggerPath', '/home/hyshf/HRSDATA/agent_download_package/etl/hrds_Trigger-5.0.jar', 'hrds_c.properties','TRIGGER程序jar包地址');
--流项目配置
INSERT INTO sys_para VALUES ('703', 'kafka_zk_address', '127.0.0.1:2181', 'hrds_j.properties','kafka的zookeeper服务地址');
-- 预留配置
-- INSERT INTO sys_para VALUES ('901', 'availableProcessors', '8', 'server.properties', '多线程采集每个任务可用线程数,这一行默认不用存库，系统默认取最大值');
-- INSERT INTO sys_para VALUES ('903', 'ocr_rpc_cpu', 'http://10.71.4.57:33333', 'server.properties', 'OCR图片文字识别的RPC服务器地址');
-- INSERT INTO sys_para VALUES ('904', 'language', 'eng', 'server.properties', 'ocr识别语言，eng（英文）或者chi_sim，默认chi_sim（简体中文）');
