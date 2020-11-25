#!/bin/bash

# 通过该脚本调用采集作业（调度平台调用）

PROJECTDIR=$(
  cd $(dirname $0)
  pwd
)

MainClass=hrds.main.CommandExecute

CLASSPATH=.:$PROJECTDIR/hrds_Agent-5.0.jar:$PROJECTDIR/resources_spark/:$PROJECTDIR/../lib/*

printf $CLASSPATH

HADOOP_OPTS="-Djava.library.path=/opt/cloudera/parcels/CDH/lib/hadoop/lib/native"
cd $PROJECTDIR
java -Xms64m -Xmx1024m $HADOOP_OPTS -Dproject.name="market-work"  -cp $CLASSPATH $MainClass "$@"
