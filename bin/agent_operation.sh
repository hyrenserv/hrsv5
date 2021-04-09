#!/bin/bash

# agent deployment directory
AGENT_DEPLOYMENT_DIR=$(dirname $(pwd))
# shell script execution directory
SH_EXEC_DIR=$(cd $(dirname $0); pwd)
# agent service package name
AGENT_JAR_NAME="${1}"
# agent log full file
AGENT_LOG_FILE="${2}"
# maxFormContentSize
MAX_FORM_CONTENT_SIZE=99900000
# agent tag
AGENT_TAG="HYREN_AGENT_RECEIVE"
# agent port
AGENT_PORT="${3}"
# agent mode of operation
AGENT_OPERATE="${4}"

# 脚本运行入口
## 参数1  Agent 程序包名 hrds_Agent-5.0.jar
## 参数2  Agent 程序运行日志文件路径 /home/hyshf/agent/agent_log_dir/log.out OR /home/hyshf/agent/agent_log_dir/
## 参数3  Agent 程序运行绑定端口  34561
## 参数4  Agent 服务操作类型 start 或者 stop 或者 restart
## 使用方式 sh agent_operation.sh hrds_Agent-5.0.jar ./log/log.out 34561 restart
## 返回状态码说明 {0: 启动成功; 1: 程序包不存在; 2: 不支持的操作类型; 3: 服务已经启动; 4: Jre文件不存在; -1: 启动失败}
main(){
    # if no parameter is passed to script then show how to use.
    if [[ $# -eq 0 ]]; then usage; fi
    if [[ $# -ne 4 ]]; then usage; fi
    # Enter the script directory
    cd ${SH_EXEC_DIR}
    # file add execute permission
    if [[ -d ${AGENT_DEPLOYMENT_DIR} ]]; then chmod -R 755 ${AGENT_DEPLOYMENT_DIR}; fi
    # Check the legality of the file
    if [[ ! -f ${AGENT_JAR_NAME} ]]; then echo "Agent service package file does not exist, please check !" && exit 1; fi
    # Log directory initialization
    ## Create the directory where the log configuration is located
    AGENT_LOG_DIR=${AGENT_LOG_FILE%/*}
    if [[ ! -d ${AGENT_LOG_DIR} ]]; then mkdir -p ${AGENT_LOG_DIR}; fi
    # Check port legality
    if [[ -z ${AGENT_PORT} ]]; then echo "Port is empty, please check port legality !" ; fi
    # Get system bits
    OS_BIT=`getconf LONG_BIT`
    # execute script
    agent_main
}

# script main
function agent_main() {
    if [[ ${AGENT_OPERATE} == "start" ]]; then
        # Start the agent process.
        start_agent
    elif [[ ${AGENT_OPERATE} == "stop" ]]; then
        # Stop the agent process.
        stop_agent
    elif [[ ${AGENT_OPERATE} == "restart" ]]; then
        # Restart the agent process.
        restart_agent
    else
        echo "Unsupported operation type. see start or stop or restart."
        exit 2
    fi
}

# Stop agent service
function stop_agent(){
    # Get the agent_pid of the specified
    local AGENT_PID=$(ps -ef | grep ${AGENT_TAG} \
        | grep ${AGENT_DEPLOYMENT_DIR} \
        | grep Dport=${AGENT_PORT} \
        | grep -v grep \
        | awk '{print $2}'| xargs -n 1)
    if [[ -n "${AGENT_PID}" ]]; then
        # The agent service already exists and starts, stop the agent service
        kill -15 ${AGENT_PID}
        # Wait for the port to stop
        waiting_proc_status ${AGENT_PORT} "stop" 1
        echo_done
        echo "The agent service is Successful stop."
    else
        echo "The agent service is not running, ignore."
    fi
}

# Start agent service
function start_agent(){
    # Get the agent_pid of the specified
    local AGENT_PID=$(ps -ef | grep ${AGENT_TAG} \
        | grep ${AGENT_DEPLOYMENT_DIR} \
        | grep Dport=${AGENT_PORT} \
        | grep -v grep \
        | awk '{print $2}'| xargs -n 1)
    if [[ -n "${AGENT_PID}" ]]; then
        echo "The agent service is already startup on ${AGENT_DEPLOYMENT_DIR}, its port is ${AGENT_PORT}."
        exit 3
    else
        # The agent service is not running, start it.
        ## Load user operating environment
        #source ~/.bash_profile; source ~/.bashrc;
        ## Enter the script directory
        cd ${SH_EXEC_DIR}
        ## Check jre environment
        if [[ ! -d "${SH_EXEC_DIR}/jre" ]]; then echo "jre does not exist, please check the jre env." ; exit 4; fi
        ## Configure classpath
        CLASSPATH=.:resources:${AGENT_JAR_NAME}
        CLASSPATH=${CLASSPATH}:../lib/hrds-commons-5.0.jar:../lib/cpdetector_1.0.10.jar:../lib/fd-core-200710.jar:../lib/fd-database-200927.jar:../lib/fd-netclient-200710.jar:../lib/fd-netserver-200710.jar:../lib/fd-testing-190703.jar:../lib/fd-web-200826.jar:../lib/kettle-core-5.2.0.0-209.jar:../lib/kettle-engine-5.2.0.0-209.jar:../lib/postmsg-ump-2.1.jar:../lib/jetty-servlet-9.4.19.v20190610.jar:../lib/jetty-security-9.4.19.v20190610.jar:../lib/jetty-server-9.4.19.v20190610.jar:../lib/jetty-jmx-9.4.19.v20190610.jar:../lib/log4j-slf4j-impl-2.11.2.jar:../lib/hadoop-client-2.6.0-cdh5.16.2.jar:../lib/hadoop-mapreduce-client-app-2.6.0-cdh5.16.2.jar:../lib/hadoop-mapreduce-client-jobclient-2.6.0-cdh5.16.2.jar:../lib/hadoop-mapreduce-client-shuffle-2.6.0-cdh5.16.2.jar:../lib/hadoop-mapreduce-client-common-2.6.0-cdh5.16.2.jar:../lib/hadoop-mapreduce-client-core-2.6.0-cdh5.16.2.jar:../lib/hive-exec-1.1.0-cdh5.16.2.jar:../lib/hive-jdbc-1.1.0-cdh5.16.2.jar:../lib/solr-solrj-5.3.1.jar:../lib/spark-core_2.11-2.3.2.jar:../lib/HikariCP-3.3.1.jar:../lib/pp4j-2.2.jar:../lib/parquet-hadoop-1.10.0.jar:../lib/parquet-column-1.10.0.jar:../lib/parquet-encoding-1.10.0.jar:../lib/parquet-common-1.10.0.jar:../lib/parquet-format-2.4.0.jar:../lib/orc-mapreduce-1.5.2.jar:../lib/orc-core-1.5.2.jar:../lib/hive-storage-api-2.6.0.jar:../lib/hadoop-yarn-client-2.6.0-cdh5.16.2.jar:../lib/hadoop-yarn-server-nodemanager-2.6.0-cdh5.16.2.jar:../lib/hive-service-1.1.0-cdh5.16.2.jar:../lib/hive-metastore-1.1.0-cdh5.16.2.jar:../lib/hive-serde-1.1.0-cdh5.16.2.jar:../lib/hive-common-1.1.0-cdh5.16.2.jar:../lib/hive-shims-1.1.0-cdh5.16.2.jar:../lib/hive-shims-0.23-1.1.0-cdh5.16.2.jar:../lib/hadoop-yarn-server-resourcemanager-2.6.0-cdh5.16.2.jar:../lib/hadoop-yarn-server-applicationhistoryservice-2.6.0-cdh5.16.2.jar:../lib/hadoop-yarn-server-web-proxy-2.6.0-cdh5.16.2.jar:../lib/hadoop-yarn-server-common-2.6.0-cdh5.16.2.jar:../lib/hadoop-yarn-common-2.6.0-cdh5.16.2.jar:../lib/hbase-server-1.2.0-cdh5.16.2.jar:../lib/hbase-client-1.2.0-cdh5.16.2.jar:../lib/metrics-core-2.2.0.jar:../lib/hbase-procedure-1.2.0-cdh5.16.2.jar:../lib/hbase-prefix-tree-1.2.0-cdh5.16.2.jar:../lib/hbase-hadoop2-compat-1.2.0-cdh5.16.2.jar:../lib/hbase-common-1.2.0-cdh5.16.2.jar:../lib/hbase-common-1.2.0-cdh5.16.2-tests.jar:../lib/hadoop-aws-2.6.0-cdh5.16.2.jar:../lib/hadoop-common-2.6.0-cdh5.16.2.jar:../lib/hadoop-auth-2.6.0-cdh5.16.2.jar:../lib/hive-shims-scheduler-1.1.0-cdh5.16.2.jar:../lib/hive-shims-common-1.1.0-cdh5.16.2.jar:../lib/libfb303-0.9.3.jar:../lib/libthrift-0.9.3.jar:../lib/hadoop-core-2.6.0-mr1-cdh5.16.2.jar:../lib/apache-curator-2.6.0.pom:../lib/curator-recipes-2.7.1.jar:../lib/curator-framework-2.7.1.jar:../lib/curator-client-2.7.1.jar:../lib/zookeeper-3.4.6.jar:../lib/spark-sql_2.11-2.3.2.jar:../lib/arrow-vector-0.8.0.jar:../lib/avro-mapred-1.7.7-hadoop2.jar:../lib/avro-ipc-1.7.7.jar:../lib/avro-ipc-1.7.7-tests.jar:../lib/avro-1.7.7.jar:../lib/jul-to-slf4j-1.7.16.jar:../lib/jcl-over-slf4j-1.7.16.jar:../lib/spark-network-shuffle_2.11-2.3.2.jar:../lib/spark-network-common_2.11-2.3.2.jar:../lib/metrics-jvm-3.1.5.jar:../lib/metrics-json-3.1.5.jar:../lib/metrics-graphite-3.1.5.jar:../lib/metrics-core-3.1.5.jar:../lib/orc-shims-1.5.2.jar:../lib/apacheds-kerberos-codec-2.0.0-M15.jar:../lib/hive-classification-1.1.0-cdh5.16.2.jar:../lib/metrics-jvm-3.0.2.jar:../lib/metrics-json-3.0.2.jar:../lib/metrics-core-3.0.2.jar:../lib/bonecp-0.8.0.RELEASE.jar:../lib/arrow-memory-0.8.0.jar:../lib/apacheds-i18n-2.0.0-M15.jar:../lib/api-asn1-api-1.0.0-M20.jar:../lib/api-util-1.0.0-M20.jar:../lib/slf4j-api-1.7.25.jar:../lib/log4j-core-2.11.2.jar:../lib/commons-exec-1.3.jar:../lib/hanlp-portable-1.2.8.jar:../lib/mysql-connector-java-5.1.31.jar:../lib/postgresql-42.2.6.jar:../lib/ojdbc8-12.2.0.1.jar:../lib/zip4j-1.3.2.jar:../lib/druid-1.2.3.jar:../lib/fastjson-1.2.72.jar:../lib/poi-ooxml-3.12.jar:../lib/poi-scratchpad-3.12.jar:../lib/poi-3.12.jar:../lib/poi-ooxml-schemas-3.12.jar:../lib/super-csv-2.4.0.jar:../lib/tess4j-3.4.7.jar:../lib/pdfbox-tools-2.0.9.jar:../lib/pdfbox-debugger-2.0.9.jar:../lib/pdfbox-2.0.9.jar:../lib/jchardet-1.0.jar:../lib/pinyin4j-2.5.0.jar:../lib/jsch-0.1.54.jar:../lib/UserAgentUtils-1.21.jar:../lib/hazelcast-3.12.8.jar:../lib/hadoop-hdfs-2.6.0-cdh5.16.2.jar:../lib/hadoop-hdfs-2.6.0-cdh5.16.2-tests.jar:../lib/calcite-core-1.0.0-incubating.jar:../lib/calcite-avatica-1.0.0-incubating.jar:../lib/hadoop-yarn-api-2.6.0-cdh5.16.2.jar:../lib/calcite-linq4j-1.0.0-incubating.jar:../lib/guava-23.0.jar:../lib/fastutil-7.2.1.jar:../lib/lucene-core-4.10.4.jar:../lib/neo4j-java-driver-4.2.0.jar:../lib/univocity-parsers-2.8.2.jar:../lib/ghost4j-1.0.1.jar:../lib/lept4j-1.6.4.jar:../lib/jna-4.1.0.jar:../lib/mapdb-1.0.9.jar:../lib/xmlrpc-client-3.1.3.jar:../lib/gexf4j-1.0.0.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/jetty-http-9.4.19.v20190610.jar:../lib/jetty-io-9.4.19.v20190610.jar:../lib/jetty-util-9.4.19.v20190610.jar:../lib/log4j-api-2.11.2.jar:../lib/commons-httpclient-3.1.jar:../lib/spark-catalyst_2.11-2.3.2.jar:../lib/jets3t-0.9.4.jar:../lib/httpclient-4.5.jar:../lib/commons-codec-1.10.jar:../lib/parquet-jackson-1.10.0.jar:../lib/logredactor-1.0.3.jar:../lib/jersey-json-1.9.jar:../lib/jackson-jaxrs-1.8.10.jar:../lib/jackson-xc-1.8.10.jar:../lib/jackson-mapper-asl-1.9.13.jar:../lib/jackson-core-asl-1.9.13.jar:../lib/snappy-java-1.1.2.6.jar:../lib/commons-dbcp-1.4.jar:../lib/commons-pool-1.6.jar:../lib/spark-unsafe_2.11-2.3.2.jar:../lib/chill_2.11-0.8.4.jar:../lib/chill-java-0.8.4.jar:../lib/kryo-shaded-3.0.3.jar:../lib/hadoop-annotations-2.6.0-cdh5.16.2.jar:../lib/hbase-protocol-1.2.0-cdh5.16.2.jar:../lib/protobuf-java-2.5.0.jar:../lib/jersey-guice-1.9.jar:../lib/guice-servlet-3.0.jar:../lib/netty-3.10.5.Final.jar:../lib/hbase-annotations-1.2.0-cdh5.16.2.jar:../lib/xmlgraphics-commons-1.4.jar:../lib/commons-io-2.6.jar:../lib/hbase-hadoop-compat-1.2.0-cdh5.16.2.jar:../lib/fontbox-2.0.9.jar:../lib/jasper-runtime-5.5.23.jar:../lib/commons-el-1.0.jar:../lib/commons-configuration-1.6.jar:../lib/pentaho-aggdesigner-algorithm-5.1.5-jhyde.jar:../lib/jpam-1.1.jar:../lib/commons-digester-1.8.jar:../lib/commons-beanutils-core-1.8.0.jar:../lib/commons-beanutils-1.9.2.jar:../lib/commons-logging-1.2.jar:../lib/netty-all-4.1.17.Final.jar:../lib/htrace-core-3.2.0-incubating.jar:../lib/joni-2.1.2.jar:../lib/jcodings-1.0.8.jar:../lib/findbugs-annotations-1.3.9-1.jar:../lib/xmlrpc-common-3.1.3.jar:../lib/ws-commons-util-1.0.2.jar:../lib/junit-4.12.jar:../lib/commons-collections-3.2.2.jar:../lib/jetty-sslengine-6.1.26.cloudera.4.jar:../lib/jetty-6.1.26.cloudera.4.jar:../lib/jetty-util-6.1.26.cloudera.4.jar:../lib/apache-log4j-extras-1.2.17.jar:../lib/log4j-1.2.17.jar:../lib/jersey-client-1.9.jar:../lib/jersey-servlet-1.14.jar:../lib/jersey-server-1.14.jar:../lib/jersey-core-1.14.jar:../lib/commons-cli-1.2.jar:../lib/high-scale-lib-1.1.1.jar:../lib/commons-math-2.1.jar:../lib/jsp-2.1-6.1.14.jar:../lib/jsp-api-2.1-6.1.14.jar:../lib/servlet-api-2.5-6.1.14.jar:../lib/jasper-compiler-5.5.23.jar:../lib/jamon-runtime-2.4.1.jar:../lib/disruptor-3.3.0.jar:../lib/hamcrest-core-1.3.jar:../lib/ST4-4.0.4.jar:../lib/antlr-runtime-3.4.jar:../lib/ant-1.9.1.jar:../lib/commons-compress-1.4.1.jar:../lib/ivy-2.4.0.jar:../lib/groovy-all-2.4.4.jar:../lib/datanucleus-core-3.2.10.jar:../lib/gson-2.2.4.jar:../lib/xmlbeans-2.6.0.jar:../lib/stax-api-1.0.1.jar:../lib/jline-2.12.jar:../lib/httpcore-4.4.1.jar:../lib/httpmime-4.4.1.jar:../lib/woodstox-core-asl-4.4.1.jar:../lib/stax2-api-3.1.4.jar:../lib/noggit-0.6.jar:../lib/spark-sketch_2.11-2.3.2.jar:../lib/spark-launcher_2.11-2.3.2.jar:../lib/spark-kvstore_2.11-2.3.2.jar:../lib/spark-tags_2.11-2.3.2.jar:../lib/json4s-jackson_2.11-3.2.11.jar:../lib/jackson-module-scala_2.11-2.6.7.1.jar:../lib/jackson-databind-2.6.7.1.jar:../lib/xbean-asm5-shaded-4.4.jar:../lib/unused-1.0.0.jar:../lib/commons-lang3-3.5.jar:../lib/commons-math3-3.4.1.jar:../lib/jsr305-3.0.2.jar:../lib/compress-lzf-1.0.3.jar:../lib/lz4-java-1.4.0.jar:../lib/zstd-jni-1.3.2-2.jar:../lib/RoaringBitmap-0.5.11.jar:../lib/commons-net-3.1.jar:../lib/json4s-core_2.11-3.2.11.jar:../lib/scalap-2.11.0.jar:../lib/scala-compiler-2.11.0.jar:../lib/scala-reflect-2.11.8.jar:../lib/json4s-ast_2.11-3.2.11.jar:../lib/scala-xml_2.11-1.0.1.jar:../lib/scala-parser-combinators_2.11-1.0.4.jar:../lib/scala-library-2.11.8.jar:../lib/jersey-container-servlet-2.22.2.jar:../lib/jersey-container-servlet-core-2.22.2.jar:../lib/jersey-server-2.22.2.jar:../lib/jersey-client-2.22.2.jar:../lib/jersey-media-jaxb-2.22.2.jar:../lib/jersey-common-2.22.2.jar:../lib/stream-2.7.0.jar:../lib/oro-2.0.8.jar:../lib/pyrolite-4.13.jar:../lib/py4j-0.10.7.jar:../lib/commons-crypto-1.0.0.jar:../lib/ucp-12.2.0.1.jar:../lib/oraclepki-12.2.0.1.jar:../lib/osdt_cert-12.2.0.1.jar:../lib/osdt_core-12.2.0.1.jar:../lib/simplefan-12.2.0.1.jar:../lib/ons-12.2.0.1.jar:../lib/jai-imageio-core-1.4.0.jar:../lib/jbig2-imageio-3.0.0.jar:../lib/jboss-vfs-3.2.12.Final.jar:../lib/error_prone_annotations-2.0.18.jar:../lib/j2objc-annotations-1.1.jar:../lib/animal-sniffer-annotations-1.14.jar:../lib/reactive-streams-1.0.3.jar:../lib/commons-pool2-2.4.2.jar:../lib/aircompressor-0.10.jar:../lib/minlog-1.3.0.jar:../lib/objenesis-2.1.jar:../lib/xmlenc-0.52.jar:../lib/htrace-core4-4.0.1-incubating.jar:../lib/commons-daemon-1.0.13.jar:../lib/xercesImpl-2.9.1.jar:../lib/leveldbjni-all-1.8.jar:../lib/aws-java-sdk-bundle-1.11.134.jar:../lib/jackson-core-2.7.9.jar:../lib/jackson-annotations-2.6.7.jar:../lib/jaxb-impl-2.2.3-1.jar:../lib/jaxb-api-2.2.2.jar:../lib/jetty-all-7.6.0.v20120127.jar:../lib/servlet-api-2.5.jar:../lib/guice-3.0.jar:../lib/jsp-api-2.1.jar:../lib/hsqldb-1.8.0.10.jar:../lib/core-3.1.1.jar:../lib/cglib-2.2.1-v20090111.jar:../lib/asm-commons-3.1.jar:../lib/asm-tree-3.1.jar:../lib/asm-3.1.jar:../lib/stringtemplate-3.2.1.jar:../lib/antlr-2.7.7.jar:../lib/ant-launcher-1.9.1.jar:../lib/xz-1.0.jar:../lib/eigenbase-properties-1.1.4.jar:../lib/janino-3.0.8.jar:../lib/commons-compiler-3.0.8.jar:../lib/joda-time-2.9.9.jar:../lib/opencsv-2.3.jar:../lib/parquet-hadoop-bundle-1.5.0-cdh5.16.2.jar:../lib/derby-10.11.1.1.jar:../lib/datanucleus-api-jdo-3.2.6.jar:../lib/datanucleus-rdbms-3.2.9.jar:../lib/jdo-api-3.0.1.jar:../lib/stax-api-1.0-2.jar:../lib/antlr4-runtime-4.7.jar:../lib/arrow-format-0.8.0.jar:../lib/hppc-0.7.2.jar:../lib/flatbuffers-1.2.0-3f79e055.jar:../lib/jackson-module-paranamer-2.7.9.jar:../lib/paranamer-2.8.jar:../lib/mail-1.4.1.jar:../lib/activation-1.1.1.jar:../lib/bcprov-jdk15on-1.52.jar:../lib/java-xmlbuilder-1.1.jar:../lib/javax.ws.rs-api-2.0.1.jar:../lib/hk2-locator-2.4.0-b34.jar:../lib/hk2-api-2.4.0-b34.jar:../lib/javax.inject-2.4.0-b34.jar:../lib/javax.annotation-api-1.2.jar:../lib/jersey-guava-2.22.2.jar:../lib/osgi-resource-locator-1.0.1.jar:../lib/validation-api-1.1.0.Final.jar:../lib/itext-2.1.7.jar:../lib/jboss-logging-3.1.4.GA.jar:../lib/jettison-1.1.jar:../lib/xml-apis-1.3.04.jar:../lib/hk2-utils-2.4.0-b34.jar:../lib/javax.inject-1.jar:../lib/aopalliance-1.0.jar:../lib/servlet-api-2.5-20081211.jar:../lib/mockito-all-1.9.5.jar:../lib/geronimo-jta_1.1_spec-1.1.1.jar:../lib/geronimo-jaspic_1.0_spec-1.0.jar:../lib/geronimo-annotation_1.0_spec-1.1.1.jar:../lib/jta-1.1.jar:../lib/base64-2.3.8.jar:../lib/aopalliance-repackaged-2.4.0-b34.jar:../lib/javassist-3.18.1-GA.jar
#        for file in `ls -a ../lib/*`; do
#        for file in `ls -a ../lib/* | sort -r`; do
#            if [[ -f ${file} ]] ; then
#             CLASSPATH="$CLASSPATH:$file"
#            fi
#        done
#        echo '############################'${CLASSPATH}
        ## Start the agent service {-verbose:class 查看加载类信息}
        nohup ${SH_EXEC_DIR}/jre/linux/${OS_BIT}/jre/bin/java -Dport=${AGENT_PORT} \
            -Dorg.eclipse.jetty.server.Request.maxFormContentSize=${MAX_FORM_CONTENT_SIZE} \
            -Dproject.dir=${AGENT_DEPLOYMENT_DIR} \
            -Dproject.name=${AGENT_TAG} \
            -cp ${CLASSPATH} hrds.main.AppMain > nohup.out &
#            -jar ${AGENT_JAR_NAME} &
        # After the startup is executed, check agent service startup status
        ## Wait for the port to start
        waiting_proc_status ${AGENT_PORT} "start" 0
        sleep 1
        ## Check the agent service startup log
        check_startup_log
        ## Get the agent_pid of the specified
        local AGENT_PID=$(ps -ef | grep ${AGENT_TAG} \
            | grep ${AGENT_DEPLOYMENT_DIR} \
            | grep Dport=${AGENT_PORT} \
            | grep -v grep \
            | awk '{print $2}'| xargs -n 1)
        if [[ -n "${AGENT_PID}" ]]; then
            echo "The agent service is Successful start."
            exit 0;
        else
            echo "The agent service did not start successfully, please contact the administrator."
            exit -1;
        fi
    fi
}

# Restart agent service
function restart_agent(){
    # stop agent service
    stop_agent
    # start agent service
    start_agent
}

#function usage means how to use this script.
function usage(){
    echo "Usage: $0 AGENT_JAR_NAME AGENT_LOG_DIR AGENT_PORT OPERATE"
    echo "for example: $0 hrds_Agent-5.0.jar /home/hyshf/agent/log.out 32101 start"
    exit
}

# 第1个参数：端口
# 第2个参数：提示信息
# 第3个参数：0-等待启动成功，1-等待停止成功
waiting_proc_status() {
    echo_doing "Waiting [$1] for $2 ..."
    local nums=0
    while true; do
        ss -tnl | grep -w $1 > /dev/null 2>&1
        if [[ $? -ne $3 ]]; then
            nums=$(expr ${nums} + 1)
            if [[ ${nums} -gt 10 ]]; then
                echo "$2 failed, please check the log information."
                exit 1
            else
                sleep 1
                echo_doing "."
            fi
        else
            sleep 1
            echo_doing "."
            break
        fi
    done
}

# 检查 agent 服务启动日志
check_startup_log(){
    local nums=0
    while true; do
        # 有这一行表明已经启动成功
        grep "Web Server started successfully" ${AGENT_LOG_FILE} > /dev/null
        if [[ $? -eq 0 ]]; then
            echo_done
            break;
        fi
        nums=$(expr ${nums} + 1 )
        if [[ ${nums} -gt 10 ]]; then
            echo "Startup failed, please check the log: ${AGENT_LOG_FILE}"
            exit 1
        else
            sleep 1
            echo_doing "."
        fi
    done
}

echo_doing() {
    if [[ "x$1" = "x" ]]; then
        echo -ne "install doing ..."
    else
        echo -ne "$1"
    fi
}

echo_done() {
    if [[ "x$1" = "x" ]]; then
        echo " done"
    else
        echo " $1"
    fi
}

# 加载脚本
main "$@"
