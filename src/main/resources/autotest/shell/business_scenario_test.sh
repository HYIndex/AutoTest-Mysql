#!/bin/bash

set -u
set -e

HOST=$1
USER=$2
PASS=$3
WH=$4
CONNECTS=$5
WARMUP=$6
DURATION=$7
INTERVAL=$8
LOGFILE=$9

DBNAME=TPCC
STEP=100

if [ ! -d "/usr/local/tpcc-mysql" ]
then
    if [ -f /var/autotest/tpcc-mysql.tar.gz ]
    then
    tar -zxvf /var/autotest/tpcc-mysql.tar.gz -C /usr/local/ > /dev/null
    #安装mysql开发环境
    apt-get -y install libmysqlclient-dev > /dev/null
    #克隆源码
    cd /usr/local/tpcc-mysql/src
    make > /dev/null
    fi
fi

cd /usr/local/tpcc-mysql


## 准备测试环境
mysql -h${HOST} -u${USER} -p${PASS} -e "DROP DATABASE IF EXISTS ${DBNAME};CREATE DATABASE ${DBNAME};"
mysql -h${HOST} -u${USER} -p${PASS} TPCC < ./create_table.sql
mysql -h${HOST} -u${USER} -p${PASS} TPCC < ./add_fkey_idx.sql


## 并行load测试数据
./tpcc_load -h $HOST -d ${DBNAME} -u ${USER} -p ${PASS} -w ${WH} -l 1 -m 1 -n ${WH} > /dev/null &
x=1
while [ $x -le ${WH} ]
do
 echo $x $(( $x + $STEP - 1 ))
./tpcc_load -h ${HOST} -d ${DBNAME} -u ${USER} -p ${PASS} -w ${WH} -l 2 -m $x -n $(( $x + $STEP - 1 ))  > /dev/null &
./tpcc_load -h ${HOST} -d ${DBNAME} -u ${USER} -p ${PASS} -w ${WH} -l 3 -m $x -n $(( $x + $STEP - 1 ))  > /dev/null &
./tpcc_load -h ${HOST} -d ${DBNAME} -u ${USER} -p ${PASS} -w ${WH} -l 4 -m $x -n $(( $x + $STEP - 1 ))  > /dev/null &
 x=$(( $x + $STEP ))
done


## 开始TPCC测试
if [ ! -d "/var/log/tpcc" ]
then
    mkdir -p /var/log/tpcc
fi
SAVEFILE="/var/log/tpcc/${LOGFILE}"
oldIFS=$IFS
IFS=,
for CONNECT in ${CONNECTS}
do
./tpcc_start -h${HOST} -P3306 -d ${DBNAME} -u ${USER} -p ${PASS} -w ${WH} -c ${CONNECT} -r ${WARMUP} -l ${DURATION} -i ${INTERVAL} >> ${SAVEFILE}

# 每轮测试结束重启mysql
service mysql restart

# 清除OS cache
echo 3 > /proc/sys/vm/drop_caches

# 等待30秒，等待服务器状态恢复
sleep 30

done
mysql -h${HOST} -u${USER} -p${PASS} -e "DROP DATABASE IF EXISTS ${DBNAME};"
pkill tpcc_load
IFS=${oldIFS}