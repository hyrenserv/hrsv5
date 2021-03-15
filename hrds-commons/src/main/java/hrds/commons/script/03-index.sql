

-- 管控
ALTER TABLE dq_req_log DROP COLUMN dl_attc;
ALTER TABLE dq_req_log ADD COLUMN dl_attc bytea;
ALTER TABLE dq_failure_table ALTER COLUMN "table_meta_info" TYPE text;
ALTER TABLE dq_failure_column ALTER COLUMN "column_meta_info" TYPE text;
-- 数据采集
ALTER TABLE source_file_attribute ALTER COLUMN "meta_info" TYPE text;
ALTER TABLE source_file_detailed ALTER COLUMN "meta_info" TYPE text;
ALTER TABLE object_collect_task ALTER COLUMN "firstline" TYPE text;
-- 数据集市
ALTER TABLE dm_operation_info ALTER COLUMN "view_sql" TYPE text;
ALTER TABLE dm_operation_info ALTER COLUMN "execute_sql" TYPE text;
-- 接口
alter table interface_use_log alter column request_info  TYPE text;
--作业
alter table etl_job_def alter column pro_para TYPE text;
alter table etl_job_cur alter column pro_para TYPE text;
alter table etl_job_disp_his alter column pro_para TYPE text;
ALTER TABLE etl_job_disp_his DROP constraint etl_job_disp_his_pk;

alter table AUTO_COMP_SUM alter column COMPONENT_BUFFER TYPE text;

create unique index u_index_data_source01 on data_source(datasource_number);-- 数据源编号不能重复

-- 源文件属性
create index index_source_file_attribute01 on source_file_attribute(collect_set_id,source_path);
create index index_source_file_attribute02 on source_file_attribute(source_path);
create index index_source_file_attribute03 on source_file_attribute(file_md5);
create index index_source_file_attribute04 on source_file_attribute(agent_id);
create index index_source_file_attribute05 on source_file_attribute(source_id);
create index index_source_file_attribute06 on source_file_attribute(collect_set_id);
create index index_source_file_attribute07 on source_file_attribute(hbase_name);
CREATE INDEX index_source_file_attribute08 ON source_file_attribute(lower(hbase_name));
create index index_source_file_attribute10 on source_file_attribute(file_avro_path);
create index index_source_file_attribute09 on source_file_attribute(collect_type);

-- 采集情况信息表
create index index_collect_case01 on collect_case(agent_id);
create index index_collect_case02 on collect_case(collect_set_id);
create index index_collect_case03 on collect_case(source_id);
create index index_collect_case04 on collect_case(etl_date);

-- 数据存储登记
create unique index u_index_data_store_reg01 on data_store_reg(hyren_name);
ALTER TABLE data_store_reg ALTER COLUMN "meta_info" TYPE text;
-- 异常信息登记
ALTER TABLE collect_case ALTER COLUMN "cc_remark" TYPE text;