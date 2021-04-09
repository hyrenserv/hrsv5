#!/bin/bash

#通过该脚本调用集市作业（调度平台调用）
# shell script execution directory
SH_EXEC_DIR=$(cd $(dirname $0); pwd)
# 集市包名
MARKET_JAR_NAME="hrds_H-5.0.jar"
# 加工程序Main方法
MAIN_CLASS="hrds.h.biz.MainClass"
# HADOOP_OPTS
HADOOP_OPTS="-Djava.library.path=/opt/cloudera/parcels/CDH/lib/hadoop/lib/native"
# datatableId 集市表id
DATA_TABLE_ID="${1}"
# etlDate 调度日期
ETL_DATE="${2}"
# [sqlParams] sql参数
SQL_PARAMS="${3}"

# 脚本运行入口
## 参数1  datatableId 集市表id 765237367308054528
## 参数2  etlDate 调度日期 20210401
## 参数3  [sqlParams] sql参数 {a=1;b=2},字符串类型可为空
## 使用方式 sh datamart.sh 765237367308054528 20210401
## 使用方式 sh datamart.sh 765237367308054528 20210401 a=1;b=2
## 返回状态码说明 {1: 程序包不存在}
main(){
    # if no parameter is passed to script then show how to use.
    if [[ $# -eq 0 ]]; then usage; fi
    # Enter the script directory
    cd ${SH_EXEC_DIR}
    # Check the legality of the file
    if [[ ! -f ${MARKET_JAR_NAME} ]]; then echo "Market service package file does not exist, please check !" && exit 1; fi
    # execute script
    market_main
}
# script main
function market_main() {
    # Configure classpath
    CLASSPATH=.:resources:${MARKET_JAR_NAME}:../lib/*
    printf ${CLASSPATH}
    java -Xms64m -Xmx1024m ${HADOOP_OPTS} \
        -Dproject.name="market-work" \
        -Dproject.id="${DATA_TABLE_ID}" \
        -cp ${CLASSPATH} ${MAIN_CLASS} ${DATA_TABLE_ID} ${ETL_DATE} ${SQL_PARAMS}
}

# 加载脚本
main "$@"