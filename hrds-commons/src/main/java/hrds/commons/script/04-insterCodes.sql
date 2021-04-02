--　系统的引用值个数:86
--　本引用值的代码个数:3 -------i==1
delete from code_info where ci_sp_class='105';
INSERT INTO CODE_INFO  VALUES ('01', '105', '自主取数模板状态', '编辑', 'AutoTemplateStatus');
INSERT INTO CODE_INFO  VALUES ('04', '105', '自主取数模板状态', '发布', 'AutoTemplateStatus');
INSERT INTO CODE_INFO  VALUES ('05', '105', '自主取数模板状态', '注销', 'AutoTemplateStatus');
--　本引用值的代码个数:6 -------i==2
delete from code_info where ci_sp_class='106';
INSERT INTO CODE_INFO  VALUES ('01', '106', '自主取数数据展现形式', '文本框', 'AutoDataRetrievalForm');
INSERT INTO CODE_INFO  VALUES ('02', '106', '自主取数数据展现形式', '下拉选择', 'AutoDataRetrievalForm');
INSERT INTO CODE_INFO  VALUES ('03', '106', '自主取数数据展现形式', '下拉多选', 'AutoDataRetrievalForm');
INSERT INTO CODE_INFO  VALUES ('04', '106', '自主取数数据展现形式', '单选按钮', 'AutoDataRetrievalForm');
INSERT INTO CODE_INFO  VALUES ('05', '106', '自主取数数据展现形式', '复选按钮', 'AutoDataRetrievalForm');
INSERT INTO CODE_INFO  VALUES ('06', '106', '自主取数数据展现形式', '日期选择', 'AutoDataRetrievalForm');
--　本引用值的代码个数:3 -------i==3
delete from code_info where ci_sp_class='108';
INSERT INTO CODE_INFO  VALUES ('01', '108', '自主取数取数状态', '编辑', 'AutoFetchStatus');
INSERT INTO CODE_INFO  VALUES ('04', '108', '自主取数取数状态', '完成', 'AutoFetchStatus');
INSERT INTO CODE_INFO  VALUES ('05', '108', '自主取数取数状态', '注销', 'AutoFetchStatus');
--　本引用值的代码个数:12 -------i==4
delete from code_info where ci_sp_class='115';
INSERT INTO CODE_INFO  VALUES ('01', '115', '可视化数据操作符', '介于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('02', '115', '可视化数据操作符', '不介于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('03', '115', '可视化数据操作符', '等于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('04', '115', '可视化数据操作符', '不等于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('05', '115', '可视化数据操作符', '大于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('06', '115', '可视化数据操作符', '小于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('07', '115', '可视化数据操作符', '大于等于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('08', '115', '可视化数据操作符', '小于等于', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('09', '115', '可视化数据操作符', '最大的N个', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('10', '115', '可视化数据操作符', '最小的N个', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('11', '115', '可视化数据操作符', '为空', 'AutoDataOperator');
INSERT INTO CODE_INFO  VALUES ('12', '115', '可视化数据操作符', '非空', 'AutoDataOperator');
--　本引用值的代码个数:7 -------i==5
delete from code_info where ci_sp_class='116';
INSERT INTO CODE_INFO  VALUES ('01', '116', '可视化数据汇总类型', '求和', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('02', '116', '可视化数据汇总类型', '求平均', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('03', '116', '可视化数据汇总类型', '求最大值', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('04', '116', '可视化数据汇总类型', '求最小值', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('05', '116', '可视化数据汇总类型', '总行数', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('06', '116', '可视化数据汇总类型', '原始数据', 'AutoDataSumType');
INSERT INTO CODE_INFO  VALUES ('07', '116', '可视化数据汇总类型', '查看全部', 'AutoDataSumType');
--　本引用值的代码个数:3 -------i==6
delete from code_info where ci_sp_class='117';
INSERT INTO CODE_INFO  VALUES ('01', '117', '可视化源对象', '自主数据数据集', 'AutoSourceObject');
INSERT INTO CODE_INFO  VALUES ('02', '117', '可视化源对象', '系统级数据集', 'AutoSourceObject');
INSERT INTO CODE_INFO  VALUES ('03', '117', '可视化源对象', '数据组件数据集', 'AutoSourceObject');
--　本引用值的代码个数:2 -------i==7
delete from code_info where ci_sp_class='118';
INSERT INTO CODE_INFO  VALUES ('1', '118', '可视化轴类型', 'x轴', 'AxisType');
INSERT INTO CODE_INFO  VALUES ('2', '118', '可视化轴类型', 'y轴', 'AxisType');
--　本引用值的代码个数:38 -------i==8
delete from code_info where ci_sp_class='17';
INSERT INTO CODE_INFO  VALUES ('00', '17', '用户类型', '系统管理员', 'UserType');
INSERT INTO CODE_INFO  VALUES ('01', '17', '用户类型', '采集管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('02', '17', '用户类型', '数据采集', 'UserType');
INSERT INTO CODE_INFO  VALUES ('03', '17', '用户类型', '数据查询', 'UserType');
INSERT INTO CODE_INFO  VALUES ('04', '17', '用户类型', '作业调度', 'UserType');
INSERT INTO CODE_INFO  VALUES ('05', '17', '用户类型', '作业操作员', 'UserType');
INSERT INTO CODE_INFO  VALUES ('06', '17', '用户类型', '数据可视化管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('07', '17', '用户类型', '可视化数据源', 'UserType');
INSERT INTO CODE_INFO  VALUES ('08', '17', '用户类型', '数据可视化分析', 'UserType');
INSERT INTO CODE_INFO  VALUES ('09', '17', '用户类型', '数据可视化查看', 'UserType');
INSERT INTO CODE_INFO  VALUES ('10', '17', '用户类型', '监控管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('11', '17', '用户类型', '服务接口管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('12', '17', '用户类型', '服务接口用户', 'UserType');
INSERT INTO CODE_INFO  VALUES ('13', '17', '用户类型', '分词器管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('14', '17', '用户类型', '数据加工', 'UserType');
INSERT INTO CODE_INFO  VALUES ('15', '17', '用户类型', '数据加工', 'UserType');
INSERT INTO CODE_INFO  VALUES ('16', '17', '用户类型', '机器学习工作台', 'UserType');
INSERT INTO CODE_INFO  VALUES ('17', '17', '用户类型', '机器学习业务', 'UserType');
INSERT INTO CODE_INFO  VALUES ('18', '17', '用户类型', '流数据管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('19', '17', '用户类型', '流数据生产', 'UserType');
INSERT INTO CODE_INFO  VALUES ('20', '17', '用户类型', '数据库配置(永洪)', 'UserType');
INSERT INTO CODE_INFO  VALUES ('21', '17', '用户类型', '报表创建(永洪)', 'UserType');
INSERT INTO CODE_INFO  VALUES ('22', '17', '用户类型', '报表查看(永洪)', 'UserType');
INSERT INTO CODE_INFO  VALUES ('23', '17', '用户类型', '流数据消费', 'UserType');
INSERT INTO CODE_INFO  VALUES ('24', '17', '用户类型', '数据管控', 'UserType');
INSERT INTO CODE_INFO  VALUES ('25', '17', '用户类型', '自主分析管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('27', '17', '用户类型', '资源管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('26', '17', '用户类型', '自主分析操作', 'UserType');
INSERT INTO CODE_INFO  VALUES ('37', '17', '用户类型', '标准元管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('38', '17', '用户类型', '表结构对标', 'UserType');
INSERT INTO CODE_INFO  VALUES ('39', '17', '用户类型', '表数据对标', 'UserType');
INSERT INTO CODE_INFO  VALUES ('55', '17', '用户类型', '标准元查看', 'UserType');
INSERT INTO CODE_INFO  VALUES ('99', '17', '用户类型', '用户管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('98', '17', '用户类型', '部门管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('97', '17', '用户类型', '系统参数管理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('96', '17', '用户类型', '数据整理', 'UserType');
INSERT INTO CODE_INFO  VALUES ('95', '17', '用户类型', '数据存储层定义', 'UserType');
INSERT INTO CODE_INFO  VALUES ('94', '17', '用户类型', '版本管理', 'UserType');
--　本引用值的代码个数:4 -------i==9
delete from code_info where ci_sp_class='119';
INSERT INTO CODE_INFO  VALUES ('01', '119', '值类型', '字符串', 'AutoValueType');
INSERT INTO CODE_INFO  VALUES ('02', '119', '值类型', '数值', 'AutoValueType');
INSERT INTO CODE_INFO  VALUES ('03', '119', '值类型', '日期', 'AutoValueType');
INSERT INTO CODE_INFO  VALUES ('04', '119', '值类型', '枚举', 'AutoValueType');
--　本引用值的代码个数:2 -------i==10
delete from code_info where ci_sp_class='18';
INSERT INTO CODE_INFO  VALUES ('1', '18', '是否标识', '是', 'IsFlag');
INSERT INTO CODE_INFO  VALUES ('0', '18', '是否标识', '否', 'IsFlag');
--　本引用值的代码个数:2 -------i==11
delete from code_info where ci_sp_class='120';
INSERT INTO CODE_INFO  VALUES ('1', '120', '流数据管理Agent类别', '文本流Agent', 'SdmAgentType');
INSERT INTO CODE_INFO  VALUES ('2', '120', '流数据管理Agent类别', 'Rest接收Agent', 'SdmAgentType');
--　本引用值的代码个数:4 -------i==12
delete from code_info where ci_sp_class='19';
INSERT INTO CODE_INFO  VALUES ('1', '19', '用户状态', '正常', 'UserState');
INSERT INTO CODE_INFO  VALUES ('2', '19', '用户状态', '禁用', 'UserState');
INSERT INTO CODE_INFO  VALUES ('3', '19', '用户状态', '删除', 'UserState');
INSERT INTO CODE_INFO  VALUES ('4', '19', '用户状态', '正在使用', 'UserState');
--　本引用值的代码个数:10 -------i==13
delete from code_info where ci_sp_class='20';
INSERT INTO CODE_INFO  VALUES ('SHELL', '20', 'ETL作业类型', 'SHELL', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('PERL', '20', 'ETL作业类型', 'PERL', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('BAT', '20', 'ETL作业类型', 'BAT', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('JAVA', '20', 'ETL作业类型', 'JAVA', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('PYTHON', '20', 'ETL作业类型', 'PYTHON', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('WF', '20', 'ETL作业类型', 'WF', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('DBTRAN', '20', 'ETL作业类型', 'DBTRAN', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('DBJOB', '20', 'ETL作业类型', 'DBJOB', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('Yarn', '20', 'ETL作业类型', 'Yarn', 'Pro_Type');
INSERT INTO CODE_INFO  VALUES ('Thrift', '20', 'ETL作业类型', 'Thrift', 'Pro_Type');
--　本引用值的代码个数:3 -------i==14
delete from code_info where ci_sp_class='21';
INSERT INTO CODE_INFO  VALUES ('1', '21', '用户优先级', '高', 'UserPriority');
INSERT INTO CODE_INFO  VALUES ('2', '21', '用户优先级', '中', 'UserPriority');
INSERT INTO CODE_INFO  VALUES ('3', '21', '用户优先级', '低', 'UserPriority');
--　本引用值的代码个数:3 -------i==15
delete from code_info where ci_sp_class='123';
INSERT INTO CODE_INFO  VALUES ('1', '123', '消费周期', '无限期', 'ConsumerCyc');
INSERT INTO CODE_INFO  VALUES ('2', '123', '消费周期', '按时间结束', 'ConsumerCyc');
INSERT INTO CODE_INFO  VALUES ('3', '123', '消费周期', '按数据量结束', 'ConsumerCyc');
--　本引用值的代码个数:6 -------i==16
delete from code_info where ci_sp_class='22';
INSERT INTO CODE_INFO  VALUES ('D', '22', 'ETL调度频率', '天(D)', 'Dispatch_Frequency');
INSERT INTO CODE_INFO  VALUES ('M', '22', 'ETL调度频率', '月(M)', 'Dispatch_Frequency');
INSERT INTO CODE_INFO  VALUES ('W', '22', 'ETL调度频率', '周(W)', 'Dispatch_Frequency');
INSERT INTO CODE_INFO  VALUES ('X', '22', 'ETL调度频率', '旬(X)', 'Dispatch_Frequency');
INSERT INTO CODE_INFO  VALUES ('Y', '22', 'ETL调度频率', '年(Y)', 'Dispatch_Frequency');
INSERT INTO CODE_INFO  VALUES ('F', '22', 'ETL调度频率', '频率(F)', 'Dispatch_Frequency');
--　本引用值的代码个数:2 -------i==17
delete from code_info where ci_sp_class='124';
INSERT INTO CODE_INFO  VALUES ('1', '124', '消费者类型', 'consumer', 'ConsumerType');
INSERT INTO CODE_INFO  VALUES ('2', '124', '消费者类型', 'streams', 'ConsumerType');
--　本引用值的代码个数:5 -------i==18
delete from code_info where ci_sp_class='23';
INSERT INTO CODE_INFO  VALUES ('B', '23', 'ETL调度类型', '批前(B)', 'Dispatch_Type');
INSERT INTO CODE_INFO  VALUES ('D', '23', 'ETL调度类型', '依赖触发(D)', 'Dispatch_Type');
INSERT INTO CODE_INFO  VALUES ('T', '23', 'ETL调度类型', '定时T+1触发(T)', 'Dispatch_Type');
INSERT INTO CODE_INFO  VALUES ('Z', '23', 'ETL调度类型', '定时T+0触发(Z)', 'Dispatch_Type');
INSERT INTO CODE_INFO  VALUES ('A', '23', 'ETL调度类型', '批后(A)', 'Dispatch_Type');
--　本引用值的代码个数:3 -------i==19
delete from code_info where ci_sp_class='24';
INSERT INTO CODE_INFO  VALUES ('Y', '24', 'ETl作业有效标志', '有效(Y)', 'Job_Effective_Flag');
INSERT INTO CODE_INFO  VALUES ('N', '24', 'ETl作业有效标志', '无效(N)', 'Job_Effective_Flag');
INSERT INTO CODE_INFO  VALUES ('V', '24', 'ETl作业有效标志', '空跑(V)', 'Job_Effective_Flag');
--　本引用值的代码个数:3 -------i==20
delete from code_info where ci_sp_class='126';
INSERT INTO CODE_INFO  VALUES ('1', '126', '流数据管理分区方式', '随机分布', 'SdmPatitionWay');
INSERT INTO CODE_INFO  VALUES ('2', '126', '流数据管理分区方式', 'key', 'SdmPatitionWay');
INSERT INTO CODE_INFO  VALUES ('3', '126', '流数据管理分区方式', '分区', 'SdmPatitionWay');
--　本引用值的代码个数:6 -------i==21
delete from code_info where ci_sp_class='25';
INSERT INTO CODE_INFO  VALUES ('D', '25', 'ETL作业状态', '完成', 'Job_Status');
INSERT INTO CODE_INFO  VALUES ('E', '25', 'ETL作业状态', '错误', 'Job_Status');
INSERT INTO CODE_INFO  VALUES ('P', '25', 'ETL作业状态', '挂起', 'Job_Status');
INSERT INTO CODE_INFO  VALUES ('R', '25', 'ETL作业状态', '运行', 'Job_Status');
INSERT INTO CODE_INFO  VALUES ('S', '25', 'ETL作业状态', '停止', 'Job_Status');
INSERT INTO CODE_INFO  VALUES ('W', '25', 'ETL作业状态', '等待', 'Job_Status');
--　本引用值的代码个数:3 -------i==22
delete from code_info where ci_sp_class='127';
INSERT INTO CODE_INFO  VALUES ('0', '127', '流数据管理自定义业务类类型', 'None', 'SdmCustomBusCla');
INSERT INTO CODE_INFO  VALUES ('1', '127', '流数据管理自定义业务类类型', 'Java', 'SdmCustomBusCla');
INSERT INTO CODE_INFO  VALUES ('2', '127', '流数据管理自定义业务类类型', 'JavaScript', 'SdmCustomBusCla');
--　本引用值的代码个数:2 -------i==23
delete from code_info where ci_sp_class='26';
INSERT INTO CODE_INFO  VALUES ('Y', '26', 'ETL当天调度标志', '是(Y)', 'Today_Dispatch_Flag');
INSERT INTO CODE_INFO  VALUES ('N', '26', 'ETL当天调度标志', '否(N)', 'Today_Dispatch_Flag');
--　本引用值的代码个数:4 -------i==24
delete from code_info where ci_sp_class='128';
INSERT INTO CODE_INFO  VALUES ('1', '128', '流数据管理变量类型', '整数', 'SdmVariableType');
INSERT INTO CODE_INFO  VALUES ('2', '128', '流数据管理变量类型', '字符串', 'SdmVariableType');
INSERT INTO CODE_INFO  VALUES ('3', '128', '流数据管理变量类型', '浮点数', 'SdmVariableType');
INSERT INTO CODE_INFO  VALUES ('4', '128', '流数据管理变量类型', '字节数组', 'SdmVariableType');
--　本引用值的代码个数:4 -------i==25
delete from code_info where ci_sp_class='27';
INSERT INTO CODE_INFO  VALUES ('L', '27', 'ETL主服务器同步', '锁定', 'Main_Server_Sync');
INSERT INTO CODE_INFO  VALUES ('N', '27', 'ETL主服务器同步', '不同步', 'Main_Server_Sync');
INSERT INTO CODE_INFO  VALUES ('Y', '27', 'ETL主服务器同步', '同步', 'Main_Server_Sync');
INSERT INTO CODE_INFO  VALUES ('B', '27', 'ETL主服务器同步', '备份中', 'Main_Server_Sync');
--　本引用值的代码个数:7 -------i==26
delete from code_info where ci_sp_class='129';
INSERT INTO CODE_INFO  VALUES ('1', '129', '流数据管理消费端目的地', '数据库', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('2', '129', '流数据管理消费端目的地', 'hbase', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('3', '129', '流数据管理消费端目的地', 'rest服务', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('4', '129', '流数据管理消费端目的地', '文件', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('5', '129', '流数据管理消费端目的地', '二进制文件', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('6', '129', '流数据管理消费端目的地', 'Kafka', 'SdmConsumeDestination');
INSERT INTO CODE_INFO  VALUES ('7', '129', '流数据管理消费端目的地', '自定义业务类', 'SdmConsumeDestination');
--　本引用值的代码个数:2 -------i==27
delete from code_info where ci_sp_class='28';
INSERT INTO CODE_INFO  VALUES ('T', '28', 'ETL状态', '有效(T)', 'Status');
INSERT INTO CODE_INFO  VALUES ('F', '28', 'ETL状态', '失效(F)', 'Status');
--　本引用值的代码个数:2 -------i==28
delete from code_info where ci_sp_class='130';
INSERT INTO CODE_INFO  VALUES ('1', '130', '流数据管理线程与分区的关系', '一对一', 'SdmThreadPartition');
INSERT INTO CODE_INFO  VALUES ('2', '130', '流数据管理线程与分区的关系', '一对多', 'SdmThreadPartition');
--　本引用值的代码个数:13 -------i==29
delete from code_info where ci_sp_class='29';
INSERT INTO CODE_INFO  VALUES ('GR', '29', 'ETL干预类型', '分组级续跑', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('GP', '29', 'ETL干预类型', '分组级暂停', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('GO', '29', 'ETL干预类型', '分组级重跑，从源头开始', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('JT', '29', 'ETL干预类型', '作业直接跑', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('JS', '29', 'ETL干预类型', '作业停止', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('JR', '29', 'ETL干预类型', '作业重跑', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('JP', '29', 'ETL干预类型', '作业临时调整优先级', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('JJ', '29', 'ETL干预类型', '作业跳过', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('SF', '29', 'ETL干预类型', '系统日切', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('SS', '29', 'ETL干预类型', '系统停止', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('SP', '29', 'ETL干预类型', '系统级暂停', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('SO', '29', 'ETL干预类型', '系统级重跑，从源头开始', 'Meddle_type');
INSERT INTO CODE_INFO  VALUES ('SR', '29', 'ETL干预类型', '系统级续跑', 'Meddle_type');
--　本引用值的代码个数:2 -------i==30
delete from code_info where ci_sp_class='131';
INSERT INTO CODE_INFO  VALUES ('1', '131', '消费方向', '内部', 'ConsDirection');
INSERT INTO CODE_INFO  VALUES ('2', '131', '消费方向', '外部', 'ConsDirection');
--　本引用值的代码个数:5 -------i==31
delete from code_info where ci_sp_class='30';
INSERT INTO CODE_INFO  VALUES ('D', '30', 'ETL干预状态', '完成', 'Meddle_status');
INSERT INTO CODE_INFO  VALUES ('E', '30', 'ETL干预状态', '异常', 'Meddle_status');
INSERT INTO CODE_INFO  VALUES ('F', '30', 'ETL干预状态', '失效', 'Meddle_status');
INSERT INTO CODE_INFO  VALUES ('T', '30', 'ETL干预状态', '有效', 'Meddle_status');
INSERT INTO CODE_INFO  VALUES ('R', '30', 'ETL干预状态', '干预中', 'Meddle_status');
--　本引用值的代码个数:2 -------i==32
delete from code_info where ci_sp_class='31';
INSERT INTO CODE_INFO  VALUES ('url', '31', 'ETL变类型', '路径', 'ParamType');
INSERT INTO CODE_INFO  VALUES ('param', '31', 'ETL变类型', '参数', 'ParamType');
--　本引用值的代码个数:2 -------i==33
delete from code_info where ci_sp_class='32';
INSERT INTO CODE_INFO  VALUES ('1', '32', '组件状态', '启用', 'CompState');
INSERT INTO CODE_INFO  VALUES ('2', '32', '组件状态', '禁用', 'CompState');
--　本引用值的代码个数:2 -------i==34
delete from code_info where ci_sp_class='33';
INSERT INTO CODE_INFO  VALUES ('1', '33', '组件类型', '系统内置组件', 'CompType');
INSERT INTO CODE_INFO  VALUES ('2', '33', '组件类型', '系统运行组件', 'CompType');
--　本引用值的代码个数:3 -------i==35
delete from code_info where ci_sp_class='34';
INSERT INTO CODE_INFO  VALUES ('1', '34', 'Agent状态', '已连接', 'AgentStatus');
INSERT INTO CODE_INFO  VALUES ('2', '34', 'Agent状态', '未连接', 'AgentStatus');
INSERT INTO CODE_INFO  VALUES ('3', '34', 'Agent状态', '正在运行', 'AgentStatus');
--　本引用值的代码个数:5 -------i==36
delete from code_info where ci_sp_class='35';
INSERT INTO CODE_INFO  VALUES ('1', '35', 'Agent类别', '数据库Agent', 'AgentType');
INSERT INTO CODE_INFO  VALUES ('2', '35', 'Agent类别', '文件系统Agent', 'AgentType');
INSERT INTO CODE_INFO  VALUES ('3', '35', 'Agent类别', 'FtpAgent', 'AgentType');
INSERT INTO CODE_INFO  VALUES ('4', '35', 'Agent类别', '数据文件Agent', 'AgentType');
INSERT INTO CODE_INFO  VALUES ('5', '35', 'Agent类别', '对象Agent', 'AgentType');
--　本引用值的代码个数:16 -------i==37
delete from code_info where ci_sp_class='36';
INSERT INTO CODE_INFO  VALUES ('01', '36', '数据库类型', 'MYSQL', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('02', '36', '数据库类型', 'Oracle9i及一下', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('03', '36', '数据库类型', 'Oracle10g及以上', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('04', '36', '数据库类型', 'SQLSERVER2000', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('05', '36', '数据库类型', 'SQLSERVER2005', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('06', '36', '数据库类型', 'DB2', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('07', '36', '数据库类型', 'SybaseASE12.5及以上', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('08', '36', '数据库类型', 'Informatic', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('09', '36', '数据库类型', 'H2', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('10', '36', '数据库类型', 'ApacheDerby', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('11', '36', '数据库类型', 'Postgresql', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('12', '36', '数据库类型', 'GBase', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('13', '36', '数据库类型', 'TeraData', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('14', '36', '数据库类型', 'Hive', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('15', '36', '数据库类型', 'Odps', 'DatabaseType');
INSERT INTO CODE_INFO  VALUES ('16', '36', '数据库类型', 'KingBase', 'DatabaseType');
--　本引用值的代码个数:3 -------i==38
delete from code_info where ci_sp_class='37';
INSERT INTO CODE_INFO  VALUES ('1', '37', '启动方式', '按时启动', 'ExecuteWay');
INSERT INTO CODE_INFO  VALUES ('2', '37', '启动方式', '命令触发', 'ExecuteWay');
INSERT INTO CODE_INFO  VALUES ('3', '37', '启动方式', '信号文件触发', 'ExecuteWay');
--　本引用值的代码个数:2 -------i==39
delete from code_info where ci_sp_class='38';
INSERT INTO CODE_INFO  VALUES ('1', '38', '压缩范围', '全库压缩', 'ReduceScope');
INSERT INTO CODE_INFO  VALUES ('2', '38', '压缩范围', '按表压缩', 'ReduceScope');
--　本引用值的代码个数:6 -------i==40
delete from code_info where ci_sp_class='39';
INSERT INTO CODE_INFO  VALUES ('01', '39', '运行状态', '开始运行', 'ExecuteState');
INSERT INTO CODE_INFO  VALUES ('02', '39', '运行状态', '运行完成', 'ExecuteState');
INSERT INTO CODE_INFO  VALUES ('99', '39', '运行状态', '运行失败', 'ExecuteState');
INSERT INTO CODE_INFO  VALUES ('20', '39', '运行状态', '通知成功', 'ExecuteState');
INSERT INTO CODE_INFO  VALUES ('21', '39', '运行状态', '通知失败', 'ExecuteState');
INSERT INTO CODE_INFO  VALUES ('30', '39', '运行状态', '暂停运行', 'ExecuteState');
--　本引用值的代码个数:4 -------i==41
delete from code_info where ci_sp_class='41';
INSERT INTO CODE_INFO  VALUES ('2', '41', '进数方式', '追加', 'StorageType');
INSERT INTO CODE_INFO  VALUES ('3', '41', '进数方式', '替换', 'StorageType');
INSERT INTO CODE_INFO  VALUES ('4', '41', '进数方式', '全量拉链', 'StorageType');
INSERT INTO CODE_INFO  VALUES ('5', '41', '进数方式', '增量拉链', 'StorageType');
--　本引用值的代码个数:5 -------i==42
delete from code_info where ci_sp_class='42';
INSERT INTO CODE_INFO  VALUES ('1', '42', '采集编码', 'UTF-8', 'DataBaseCode');
INSERT INTO CODE_INFO  VALUES ('2', '42', '采集编码', 'GBK', 'DataBaseCode');
INSERT INTO CODE_INFO  VALUES ('3', '42', '采集编码', 'UTF-16', 'DataBaseCode');
INSERT INTO CODE_INFO  VALUES ('4', '42', '采集编码', 'GB2312', 'DataBaseCode');
INSERT INTO CODE_INFO  VALUES ('5', '42', '采集编码', 'ISO-8859-1', 'DataBaseCode');
--　本引用值的代码个数:6 -------i==43
delete from code_info where ci_sp_class='43';
INSERT INTO CODE_INFO  VALUES ('10000', '43', '记录总数', '1万左右', 'CountNum');
INSERT INTO CODE_INFO  VALUES ('100000', '43', '记录总数', '10万左右', 'CountNum');
INSERT INTO CODE_INFO  VALUES ('1000000', '43', '记录总数', '100万左右', 'CountNum');
INSERT INTO CODE_INFO  VALUES ('10000000', '43', '记录总数', '1000万左右', 'CountNum');
INSERT INTO CODE_INFO  VALUES ('100000000', '43', '记录总数', '亿左右', 'CountNum');
INSERT INTO CODE_INFO  VALUES ('100000001', '43', '记录总数', '亿以上', 'CountNum');
--　本引用值的代码个数:6 -------i==44
delete from code_info where ci_sp_class='44';
INSERT INTO CODE_INFO  VALUES ('0', '44', 'DB文件格式', '定长', 'FileFormat');
INSERT INTO CODE_INFO  VALUES ('1', '44', 'DB文件格式', '非定长', 'FileFormat');
INSERT INTO CODE_INFO  VALUES ('2', '44', 'DB文件格式', 'CSV', 'FileFormat');
INSERT INTO CODE_INFO  VALUES ('3', '44', 'DB文件格式', 'SEQUENCEFILE', 'FileFormat');
INSERT INTO CODE_INFO  VALUES ('4', '44', 'DB文件格式', 'PARQUET', 'FileFormat');
INSERT INTO CODE_INFO  VALUES ('5', '44', 'DB文件格式', 'ORC', 'FileFormat');
--　本引用值的代码个数:4 -------i==45
delete from code_info where ci_sp_class='45';
INSERT INTO CODE_INFO  VALUES ('1', '45', '压缩格式', 'tar', 'ReduceType');
INSERT INTO CODE_INFO  VALUES ('2', '45', '压缩格式', 'gz', 'ReduceType');
INSERT INTO CODE_INFO  VALUES ('3', '45', '压缩格式', 'zip', 'ReduceType');
INSERT INTO CODE_INFO  VALUES ('4', '45', '压缩格式', 'none', 'ReduceType');
--　本引用值的代码个数:2 -------i==46
delete from code_info where ci_sp_class='46';
INSERT INTO CODE_INFO  VALUES ('1', '46', '数据类型', 'xml', 'CollectDataType');
INSERT INTO CODE_INFO  VALUES ('2', '46', '数据类型', 'json', 'CollectDataType');
--　本引用值的代码个数:2 -------i==47
delete from code_info where ci_sp_class='47';
INSERT INTO CODE_INFO  VALUES ('1', '47', '对象采集方式', '行采集', 'ObjectCollectType');
INSERT INTO CODE_INFO  VALUES ('2', '47', '对象采集方式', '对象采集', 'ObjectCollectType');
--　本引用值的代码个数:2 -------i==48
delete from code_info where ci_sp_class='48';
INSERT INTO CODE_INFO  VALUES ('1', '48', '对象数据类型', '数组', 'ObjectDataType');
INSERT INTO CODE_INFO  VALUES ('2', '48', '对象数据类型', '字符串', 'ObjectDataType');
--　本引用值的代码个数:3 -------i==49
delete from code_info where ci_sp_class='49';
INSERT INTO CODE_INFO  VALUES ('1', '49', 'ftp目录规则', '流水号', 'FtpRule');
INSERT INTO CODE_INFO  VALUES ('2', '49', 'ftp目录规则', '固定目录', 'FtpRule');
INSERT INTO CODE_INFO  VALUES ('3', '49', 'ftp目录规则', '按时间', 'FtpRule');
--　本引用值的代码个数:4 -------i==50
delete from code_info where ci_sp_class='50';
INSERT INTO CODE_INFO  VALUES ('1', '50', '时间类型', '日', 'TimeType');
INSERT INTO CODE_INFO  VALUES ('2', '50', '时间类型', '小时', 'TimeType');
INSERT INTO CODE_INFO  VALUES ('3', '50', '时间类型', '分钟', 'TimeType');
INSERT INTO CODE_INFO  VALUES ('4', '50', '时间类型', '秒', 'TimeType');
--　本引用值的代码个数:2 -------i==51
delete from code_info where ci_sp_class='52';
INSERT INTO CODE_INFO  VALUES ('1', '52', '字符拆分方式', '偏移量', 'CharSplitType');
INSERT INTO CODE_INFO  VALUES ('2', '52', '字符拆分方式', '自定符号', 'CharSplitType');
--　本引用值的代码个数:3 -------i==52
delete from code_info where ci_sp_class='53';
INSERT INTO CODE_INFO  VALUES ('1', '53', '数据文件源头', '数据库抽取落地', 'DataExtractType');
INSERT INTO CODE_INFO  VALUES ('2', '53', '数据文件源头', '原数据格式', 'DataExtractType');
INSERT INTO CODE_INFO  VALUES ('3', '53', '数据文件源头', '数据加载格式', 'DataExtractType');
--　本引用值的代码个数:7 -------i==53
delete from code_info where ci_sp_class='54';
INSERT INTO CODE_INFO  VALUES ('1', '54', '清洗方式', '字符补齐', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('2', '54', '清洗方式', '字符替换', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('3', '54', '清洗方式', '时间转换', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('4', '54', '清洗方式', '码值转换', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('5', '54', '清洗方式', '字符合并', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('6', '54', '清洗方式', '字符拆分', 'CleanType');
INSERT INTO CODE_INFO  VALUES ('7', '54', '清洗方式', '字符trim', 'CleanType');
--　本引用值的代码个数:2 -------i==54
delete from code_info where ci_sp_class='55';
INSERT INTO CODE_INFO  VALUES ('1', '55', '补齐方式', '前补齐', 'FillingType');
INSERT INTO CODE_INFO  VALUES ('2', '55', '补齐方式', '后补齐', 'FillingType');
--　本引用值的代码个数:4 -------i==55
delete from code_info where ci_sp_class='57';
INSERT INTO CODE_INFO  VALUES ('1', '57', '数据申请类型', '查看', 'ApplyType');
INSERT INTO CODE_INFO  VALUES ('2', '57', '数据申请类型', '下载', 'ApplyType');
INSERT INTO CODE_INFO  VALUES ('3', '57', '数据申请类型', '发布', 'ApplyType');
INSERT INTO CODE_INFO  VALUES ('4', '57', '数据申请类型', '重命名', 'ApplyType');
--　本引用值的代码个数:4 -------i==56
delete from code_info where ci_sp_class='58';
INSERT INTO CODE_INFO  VALUES ('1', '58', '权限类型', '允许', 'AuthType');
INSERT INTO CODE_INFO  VALUES ('2', '58', '权限类型', '不允许', 'AuthType');
INSERT INTO CODE_INFO  VALUES ('3', '58', '权限类型', '一次', 'AuthType');
INSERT INTO CODE_INFO  VALUES ('0', '58', '权限类型', '申请', 'AuthType');
--　本引用值的代码个数:7 -------i==57
delete from code_info where ci_sp_class='59';
INSERT INTO CODE_INFO  VALUES ('1', '59', '存储层类型', '关系型数据库', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('2', '59', '存储层类型', 'hive', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('3', '59', '存储层类型', 'Hbase', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('4', '59', '存储层类型', 'solr', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('5', '59', '存储层类型', 'ElasticSearch', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('6', '59', '存储层类型', 'mongodb', 'Store_type');
INSERT INTO CODE_INFO  VALUES ('7', '59', '存储层类型', 'CarBonData', 'Store_type');
--　本引用值的代码个数:12 -------i==58
delete from code_info where ci_sp_class='60';
INSERT INTO CODE_INFO  VALUES ('1001', '60', '文件类型', '全部文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1002', '60', '文件类型', '图片', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1003', '60', '文件类型', '文档', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1013', '60', '文件类型', 'PDF文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1023', '60', '文件类型', 'office文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1033', '60', '文件类型', '文本文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1043', '60', '文件类型', '压缩文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1053', '60', '文件类型', '日志文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1063', '60', '文件类型', '表数据文件', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1004', '60', '文件类型', '视频', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1005', '60', '文件类型', '音频', 'FileType');
INSERT INTO CODE_INFO  VALUES ('1006', '60', '文件类型', '其它', 'FileType');
--　本引用值的代码个数:7 -------i==59
delete from code_info where ci_sp_class='62';
INSERT INTO CODE_INFO  VALUES ('01', '62', '存储层附件属性', '主键', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('02', '62', '存储层附件属性', 'rowkey', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('03', '62', '存储层附件属性', '索引列', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('04', '62', '存储层附件属性', '预聚合列', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('05', '62', '存储层附件属性', '排序列', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('06', '62', '存储层附件属性', '分区列', 'StoreLayerAdded');
INSERT INTO CODE_INFO  VALUES ('07', '62', '存储层附件属性', 'Solr列', 'StoreLayerAdded');
--　本引用值的代码个数:8 -------i==60
delete from code_info where ci_sp_class='63';
INSERT INTO CODE_INFO  VALUES ('ISL', '63', '数据源类型', '贴源层_01', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('DCL', '63', '数据源类型', '贴源层', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('DPL', '63', '数据源类型', '加工层-废弃', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('DML', '63', '数据源类型', '加工层', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('SFL', '63', '数据源类型', '系统层', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('AML', '63', '数据源类型', 'AI模型层', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('DQC', '63', '数据源类型', '管控层', 'DataSourceType');
INSERT INTO CODE_INFO  VALUES ('UDL', '63', '数据源类型', '自定义层', 'DataSourceType');
--　本引用值的代码个数:9 -------i==61
delete from code_info where ci_sp_class='67';
INSERT INTO CODE_INFO  VALUES ('101', '67', '对标-数据类别', '编码类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('102', '67', '对标-数据类别', '标识类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('103', '67', '对标-数据类别', '代码类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('104', '67', '对标-数据类别', '金额类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('105', '67', '对标-数据类别', '日期类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('106', '67', '对标-数据类别', '日期时间类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('107', '67', '对标-数据类别', '时间类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('108', '67', '对标-数据类别', '数值类', 'DbmDataType');
INSERT INTO CODE_INFO  VALUES ('109', '67', '对标-数据类别', '文本类', 'DbmDataType');
--　本引用值的代码个数:2 -------i==62
delete from code_info where ci_sp_class='68';
INSERT INTO CODE_INFO  VALUES ('1', '68', '对标-对标方式', '数据对标', 'DbmMode');
INSERT INTO CODE_INFO  VALUES ('2', '68', '对标-对标方式', '表结构对标', 'DbmMode');
--　本引用值的代码个数:3 -------i==63
delete from code_info where ci_sp_class='69';
INSERT INTO CODE_INFO  VALUES ('0', '69', 'Operation类型', 'INSERT', 'OperationType');
INSERT INTO CODE_INFO  VALUES ('1', '69', 'Operation类型', 'UPDATE', 'OperationType');
INSERT INTO CODE_INFO  VALUES ('2', '69', 'Operation类型', 'DELETE', 'OperationType');
--　本引用值的代码个数:2 -------i==64
delete from code_info where ci_sp_class='71';
INSERT INTO CODE_INFO  VALUES ('0', '71', '更新方式', '直接更新', 'UpdateType');
INSERT INTO CODE_INFO  VALUES ('1', '71', '更新方式', '拉链更新', 'UpdateType');
--　本引用值的代码个数:4 -------i==65
delete from code_info where ci_sp_class='72';
INSERT INTO CODE_INFO  VALUES ('1', '72', '接口类型', '数据类', 'InterfaceType');
INSERT INTO CODE_INFO  VALUES ('2', '72', '接口类型', '功能类', 'InterfaceType');
INSERT INTO CODE_INFO  VALUES ('3', '72', '接口类型', '报表类', 'InterfaceType');
INSERT INTO CODE_INFO  VALUES ('4', '72', '接口类型', '监控类', 'InterfaceType');
--　本引用值的代码个数:2 -------i==66
delete from code_info where ci_sp_class='73';
INSERT INTO CODE_INFO  VALUES ('1', '73', '接口状态', '启用', 'InterfaceState');
INSERT INTO CODE_INFO  VALUES ('2', '73', '接口状态', '禁用', 'InterfaceState');
--　本引用值的代码个数:2 -------i==67
delete from code_info where ci_sp_class='76';
INSERT INTO CODE_INFO  VALUES ('0', '76', '数据质量规则级别', '警告', 'EdRuleLevel');
INSERT INTO CODE_INFO  VALUES ('1', '76', '数据质量规则级别', '严重', 'EdRuleLevel');
--　本引用值的代码个数:3 -------i==68
delete from code_info where ci_sp_class='78';
INSERT INTO CODE_INFO  VALUES ('0', '78', '数据质量校验结果', '检查通过', 'DqcVerifyResult');
INSERT INTO CODE_INFO  VALUES ('1', '78', '数据质量校验结果', '数据异常', 'DqcVerifyResult');
INSERT INTO CODE_INFO  VALUES ('2', '78', '数据质量校验结果', '执行失败', 'DqcVerifyResult');
--　本引用值的代码个数:7 -------i==69
delete from code_info where ci_sp_class='79';
INSERT INTO CODE_INFO  VALUES ('w', '79', '数据质量处理状态', '等待处理', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('b', '79', '数据质量处理状态', '已退回', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('i', '79', '数据质量处理状态', '已忽略', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('d', '79', '数据质量处理状态', '已处理', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('oki', '79', '数据质量处理状态', '处理完结', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('okd', '79', '数据质量处理状态', '已忽略通过', 'DqcDlStat');
INSERT INTO CODE_INFO  VALUES ('zc', '79', '数据质量处理状态', '正常', 'DqcDlStat');
--　本引用值的代码个数:2 -------i==70
delete from code_info where ci_sp_class='80';
INSERT INTO CODE_INFO  VALUES ('MAN', '80', '数据质量执行方式', '手工', 'DqcExecMode');
INSERT INTO CODE_INFO  VALUES ('AUTO', '80', '数据质量执行方式', '自动', 'DqcExecMode');
--　本引用值的代码个数:6 -------i==71
delete from code_info where ci_sp_class='81';
INSERT INTO CODE_INFO  VALUES ('1', '81', 'hdfs文件类型', 'csv', 'HdfsFileType');
INSERT INTO CODE_INFO  VALUES ('2', '81', 'hdfs文件类型', 'parquet', 'HdfsFileType');
INSERT INTO CODE_INFO  VALUES ('3', '81', 'hdfs文件类型', 'avro', 'HdfsFileType');
INSERT INTO CODE_INFO  VALUES ('4', '81', 'hdfs文件类型', 'orcfile', 'HdfsFileType');
INSERT INTO CODE_INFO  VALUES ('5', '81', 'hdfs文件类型', 'sequencefile', 'HdfsFileType');
INSERT INTO CODE_INFO  VALUES ('6', '81', 'hdfs文件类型', '其他', 'HdfsFileType');
--　本引用值的代码个数:2 -------i==72
delete from code_info where ci_sp_class='85';
INSERT INTO CODE_INFO  VALUES ('1', '85', '数据表生命周期', '永久', 'TableLifeCycle');
INSERT INTO CODE_INFO  VALUES ('2', '85', '数据表生命周期', '临时', 'TableLifeCycle');
--　本引用值的代码个数:2 -------i==73
delete from code_info where ci_sp_class='86';
INSERT INTO CODE_INFO  VALUES ('0', '86', '数据表存储方式', '数据表', 'TableStorage');
INSERT INTO CODE_INFO  VALUES ('1', '86', '数据表存储方式', '数据视图', 'TableStorage');
--　本引用值的代码个数:6 -------i==74
delete from code_info where ci_sp_class='87';
INSERT INTO CODE_INFO  VALUES ('100', '87', '作业运行状态', '等待', 'JobExecuteState');
INSERT INTO CODE_INFO  VALUES ('101', '87', '作业运行状态', '运行', 'JobExecuteState');
INSERT INTO CODE_INFO  VALUES ('102', '87', '作业运行状态', '暂停', 'JobExecuteState');
INSERT INTO CODE_INFO  VALUES ('103', '87', '作业运行状态', '中止', 'JobExecuteState');
INSERT INTO CODE_INFO  VALUES ('104', '87', '作业运行状态', '完成', 'JobExecuteState');
INSERT INTO CODE_INFO  VALUES ('105', '87', '作业运行状态', '失败', 'JobExecuteState');
--　本引用值的代码个数:3 -------i==75
delete from code_info where ci_sp_class='88';
INSERT INTO CODE_INFO  VALUES ('1', '88', 'sql执行引擎', 'JDBC', 'SqlEngine');
INSERT INTO CODE_INFO  VALUES ('2', '88', 'sql执行引擎', 'SPARK', 'SqlEngine');
INSERT INTO CODE_INFO  VALUES ('3', '88', 'sql执行引擎', '默认', 'SqlEngine');
--　本引用值的代码个数:2 -------i==76
delete from code_info where ci_sp_class='90';
INSERT INTO CODE_INFO  VALUES ('1', '90', '落地文件-卸数方式', '全量卸数', 'UnloadType');
INSERT INTO CODE_INFO  VALUES ('2', '90', '落地文件-卸数方式', '增量卸数', 'UnloadType');
--　本引用值的代码个数:5 -------i==77
delete from code_info where ci_sp_class='91';
INSERT INTO CODE_INFO  VALUES ('1', '91', '数据处理方式', '定值', 'ProcessType');
INSERT INTO CODE_INFO  VALUES ('2', '91', '数据处理方式', '自增', 'ProcessType');
INSERT INTO CODE_INFO  VALUES ('3', '91', '数据处理方式', '映射赋值', 'ProcessType');
INSERT INTO CODE_INFO  VALUES ('4', '91', '数据处理方式', '函数映射', 'ProcessType');
INSERT INTO CODE_INFO  VALUES ('5', '91', '数据处理方式', '分组映射', 'ProcessType');
--　本引用值的代码个数:6 -------i==78
delete from code_info where ci_sp_class='93';
INSERT INTO CODE_INFO  VALUES ('1', '93', '存储层关系-数据来源', 'db采集', 'StoreLayerDataSource');
INSERT INTO CODE_INFO  VALUES ('2', '93', '存储层关系-数据来源', '数据库采集', 'StoreLayerDataSource');
INSERT INTO CODE_INFO  VALUES ('3', '93', '存储层关系-数据来源', '对象采集', 'StoreLayerDataSource');
INSERT INTO CODE_INFO  VALUES ('4', '93', '存储层关系-数据来源', '数据集市', 'StoreLayerDataSource');
INSERT INTO CODE_INFO  VALUES ('5', '93', '存储层关系-数据来源', '数据管控', 'StoreLayerDataSource');
INSERT INTO CODE_INFO  VALUES ('6', '93', '存储层关系-数据来源', '自定义', 'StoreLayerDataSource');
--　本引用值的代码个数:4 -------i==79
delete from code_info where ci_sp_class='94';
INSERT INTO CODE_INFO  VALUES ('S', '94', '对标-状态', '成功', 'DbmState');
INSERT INTO CODE_INFO  VALUES ('F', '94', '对标-状态', '失败', 'DbmState');
INSERT INTO CODE_INFO  VALUES ('R', '94', '对标-状态', '运行', 'DbmState');
INSERT INTO CODE_INFO  VALUES ('N', '94', '对标-状态', '未运行', 'DbmState');
--　本引用值的代码个数:3 -------i==80
delete from code_info where ci_sp_class='96';
INSERT INTO CODE_INFO  VALUES ('1', '96', '数据库采集方式', '贴元登记', 'CollectType');
INSERT INTO CODE_INFO  VALUES ('2', '96', '数据库采集方式', '数据库抽数', 'CollectType');
INSERT INTO CODE_INFO  VALUES ('3', '96', '数据库采集方式', '数据库采集', 'CollectType');
--　本引用值的代码个数:4 -------i==81
delete from code_info where ci_sp_class='133';
INSERT INTO CODE_INFO  VALUES ('1', '133', 'StreamingPro输入输出的类型', '文本文件', 'SdmSpInputOutputType');
INSERT INTO CODE_INFO  VALUES ('2', '133', 'StreamingPro输入输出的类型', '数据库表', 'SdmSpInputOutputType');
INSERT INTO CODE_INFO  VALUES ('3', '133', 'StreamingPro输入输出的类型', '消费主题', 'SdmSpInputOutputType');
INSERT INTO CODE_INFO  VALUES ('4', '133', 'StreamingPro输入输出的类型', '内部表', 'SdmSpInputOutputType');
--　本引用值的代码个数:2 -------i==82
delete from code_info where ci_sp_class='134';
INSERT INTO CODE_INFO  VALUES ('1', '134', 'StreamingPro输入的数据模式', '批量表', 'SdmSpDataMode');
INSERT INTO CODE_INFO  VALUES ('2', '134', 'StreamingPro输入的数据模式', '流表', 'SdmSpDataMode');
--　本引用值的代码个数:3 -------i==83
delete from code_info where ci_sp_class='135';
INSERT INTO CODE_INFO  VALUES ('1', '135', 'StreamingPro输出模式', 'Append', 'SdmSpOutputMode');
INSERT INTO CODE_INFO  VALUES ('2', '135', 'StreamingPro输出模式', 'Update', 'SdmSpOutputMode');
INSERT INTO CODE_INFO  VALUES ('3', '135', 'StreamingPro输出模式', 'Complete', 'SdmSpOutputMode');
--　本引用值的代码个数:3 -------i==84
delete from code_info where ci_sp_class='138';
INSERT INTO CODE_INFO  VALUES ('1', '138', 'StreamingPro文本文件格式', 'Csv', 'SdmSpFileType');
INSERT INTO CODE_INFO  VALUES ('2', '138', 'StreamingPro文本文件格式', 'Parquent', 'SdmSpFileType');
INSERT INTO CODE_INFO  VALUES ('3', '138', 'StreamingPro文本文件格式', 'Json', 'SdmSpFileType');
--　本引用值的代码个数:3 -------i==85
delete from code_info where ci_sp_class='140';
INSERT INTO CODE_INFO  VALUES ('1', '140', 'StreamingPro流式数据的版本', 'kafka8', 'SdmSpStreamVer');
INSERT INTO CODE_INFO  VALUES ('2', '140', 'StreamingPro流式数据的版本', 'kafka9', 'SdmSpStreamVer');
INSERT INTO CODE_INFO  VALUES ('3', '140', 'StreamingPro流式数据的版本', 'kafka', 'SdmSpStreamVer');
--　本引用值的代码个数:3 -------i==86
delete from code_info where ci_sp_class='142';
INSERT INTO CODE_INFO  VALUES ('0', '142', 'StreamingProREST返回处理方式', '忽略', 'SdmSpRsType');
INSERT INTO CODE_INFO  VALUES ('1', '142', 'StreamingProREST返回处理方式', '数据库', 'SdmSpRsType');
INSERT INTO CODE_INFO  VALUES ('2', '142', 'StreamingProREST返回处理方式', 'kafka', 'SdmSpRsType');
