

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

CREATE VIEW
    field_info_feature_view AS
    (
        SELECT
            t1.SYS_CLASS_CODE     AS SYS_CLASS_CODE,
            t1.TABLE_SCHEMA AS TABLE_SCHEMA,
            t1.TABLE_CODE   AS TABLE_CODE,
            t1.COL_CODE     AS COL_CODE,
            t2.COL_RECORDS  AS COL_RECORDS,
            t2.COL_DISTINCT AS COL_DISTINCT,
            t2.MAX_LEN      AS MAX_LEN,
            t2.MIN_LEN      AS MIN_LEN,
            t2.AVG_LEN      AS AVG_LEN,
            t2.SKEW_LEN     AS SKEW_LEN,
            t2.KURT_LEN     AS KURT_LEN,
            t2.MEDIAN_LEN   AS MEDIAN_LEN,
            t2.VAR_LEN      AS VAR_LEN,
            t2.HAS_CHINESE  AS HAS_CHINESE,
            t2.TECH_CATE    AS TECH_CATE,
            t1.COL_NUM      AS COL_NUM,
            t1.COL_NAME     AS COL_NAME,
            t1.COL_COMMENT  AS COL_COMMENT,
            (
                CASE
                    WHEN (t1.COL_TYPE_JUDGE_RATE = 1.0)
                    THEN t1.COL_TYPE
                    ELSE (
                            CASE
                                WHEN (t2.MAX_LEN = t2.MIN_LEN)
                                THEN 'CHAR'
                                ELSE 'VARCHAR'
                            END)
                END)               AS COL_TYPE,
            t1.COL_LENGTH          AS COL_LENGTH,
            t1.COL_NULLABLE        AS COL_NULLABLE,
            t1.COL_PK              AS COL_PK,
            t1.IS_STD              AS IS_STD,
            t1.CDVAL_NO            AS CDVAL_NO,
            t1.COL_CHECK           AS COL_CHECK,
            t1.COLTRA              AS COLTRA,
            t1.COLFORMAT           AS COLFORMAT,
            t1.TRATYPE             AS TRATYPE,
            t1.ST_TM               AS ST_TM,
            t1.END_TM              AS END_TM,
            t1.DATA_SRC            AS DATA_SRC,
            t1.COL_AUTOINCRE       AS COL_AUTOINCRE,
            t1.COL_DEFULT          AS COL_DEFULT,
            t1.COL_TYPE_JUDGE_RATE AS COL_TYPE_JUDGE_RATE
        FROM
            dbm_mmm_field_info_tab t1
        JOIN
            dbm_feature_tab t2
        ON
            ((
                    t1.SYS_CLASS_CODE = t2.SYS_CLASS_CODE)
            AND (
                    t1.TABLE_SCHEMA = t2.TABLE_SCHEMA)
            AND (
                    t1.TABLE_CODE = t2.TABLE_CODE)
            AND (
                    t1.COL_CODE = t2.COL_CODE))
    );
