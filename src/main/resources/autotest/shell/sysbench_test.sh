#!/bin/bash

#  sysbench test
###test environment #####
#  ubuntu-server 16.04
#  sysbench 1.0.17
#  msyql 5.7.25
#########################

test_type=${1}

user=${2}
password=${3}
host=${4}

table_size=${5}
tables=${6}
threads=${7}
time=${8}
interval=${9}

savefile=${10}

slaves=${11}
slave_host=${12}
slave_port=${13}
slave_user=${14}
slave_pass=${15}

envir_status=1
if [ ! -d "/usr/local/sysbench" ]
then
    envir_status=0
    if [ -f "/var/autotest/sysbench-3.0.0.tar.gz" ]
    then
        apt -y update >/dev/null
        apt -y install make automake libtool pkg-config libaio-dev >/dev/null
        apt -y install libmysqlclient-dev libssl-dev >/dev/null

        tar -zxvf /var/autotest/sysbench-3.0.0.tar.gz -C /usr/local/ >/dev/null
        cd /usr/local/sysbench
        ./autogen.sh >/dev/null
        ./configure --prefix=/usr/local/sysbench/ --with-mysql-includes=/usr/include/mysql --with-mysql-libs=/usr/lib/mysql >/dev/null
        make >/dev/null
        make install >/dev/null
        envir_status=1
    fi
fi

if [ ${envir_status} == 1 ]
then
    if [ ! -d "/var/log/sysbench" ]
    then
        mkdir -p /var/log/sysbench
    fi
    testname="/usr/local/sysbench/src/lua/oltp_"${test_type}".lua"
    resname="/var/log/sysbench/"${savefile}

    # 首先要创建测试数据库
    mysql -u ${user} -p${password} -e "create database testdb;"
    # -o ${test_type} == "read_write"
    oldIFS=$IFS
    IFS=,
    for thread in ${threads}
    do
    if [ ${test_type} == "insert" -o ${test_type} == "update_index" -o ${test_type} == "update_non_index" -o ${test_type} == "delete" ]
    then
        /usr/local/sysbench/src/sysbench ${testname} --mysql-user=${user} --mysql-password=${password} --mysql-host=${host} --mysql-port=3306 --mysql-db=testdb --mysql-storage-engine=innodb --table-size=${table_size} --tables=${tables} --threads=${thread} --time=${time} --report-interval=${interval} prepare
        /usr/local/sysbench/src/sysbench ${testname} --mysql-user=${user} --mysql-password=${password} --mysql-host=${host} --mysql-port=3306 --mysql-db=testdb --mysql-storage-engine=innodb --table-size=${table_size} --tables=${tables} --threads=${thread} --time=${time} --report-interval=${interval} run >> ${resname}
        /usr/local/sysbench/src/sysbench ${testname} --mysql-user=${user} --mysql-password=${password} --mysql-host=${host} --mysql-port=3306 --mysql-db=testdb --mysql-storage-engine=innodb --table-size=${table_size} --tables=${tables} --threads=${thread} --time=${time} --report-interval=${interval} cleanup
    else
        /usr/local/sysbench/src/sysbench ${testname} --mysql-host=${host} --mysql-user=${user} --mysql-password=${password} --mysql-port=3306 --mysql-db=testdb --tables=${tables} --table-size=${table_size} --time=${time} --threads=${thread} prepare
        /usr/local/sysbench/src/sysbench ${testname} --mysql-host=${host} --mysql-user=${user} --mysql-password=${password} --mysql-port=3306 --mysql-db=testdb --tables=${tables} --table-size=${table_size} --slaves=${slaves} --slave-host=${slave_host} --slave-port=${slave_port} --slave-user=${slave_user} --slave-password=${slave_pass} --slave-db=testdb --range_selects=off --report-interval=${interval} --time=${time}  --threads=${thread} run >> ${resname}
        /usr/local/sysbench/src/sysbench ${testname} --mysql-host=${host} --mysql-user=${user} --mysql-password=${password} --mysql-port=3306 --mysql-db=testdb --tables=${tables} --table-size=${table_size} --time=${time} --threads=${thread} cleanup
    fi
    sleep 10
    done
    IFS=$oldIFS

    # 删除测试数据库
    mysql -u ${user} -p${password} -e "drop database testdb;"
fi