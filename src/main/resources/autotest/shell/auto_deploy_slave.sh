#!/usr/bin bash

mysqlUser=${1}
mysqlPswd=${2}
masterIp=${3}
binlogName=${4}
position=${5}
serverid=${6}

repUser='repuser'
repPswd='1234567'

# 配置从节点

# check mysql exist or not and version is 5.7.25
mysql_is_installed=0
checkMysql() {
    mysql --version
    if [ $? -eq 0 ]
    then
        mysql_is_installed=1
#        mysql_version=`mysql --version | grep '5.7.25' | wc -l`
#        if [ ${mysql_version} != 1 ]
#        then
#            # Uninstall
#            apt-get -y remove mysql-*
#            dpkg -l |grep ^rc|awk '{print $2}' | xargs dpkg -P
#            # Install
#            export DEBIAN_FRONTEND=noninteractive
#            apt-get update -y
#            apt-get install mysql-server mysql-client -y
#        fi
    else
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -y >/dev/null
        apt-get install mysql-server mysql-client -y >/dev/null
    fi
    mysql_status=`service mysql status | grep 'running' | wc -l`
    return ${mysql_status}
}

#ins_pt_heartbeat() {
#    pt-heartbeat --version
#    if [ $? != 1 ]
#    then
#        apt-get update -y >/dev/null
#        apt-get install percona-toolkit -y >/dev/null
#    fi
#}

# 1. 安装MySQL（如果服务器没有安装）
checkMysql
#ins_pt_heartbeat

sed -i "/^bind-address/c\bind-address=0.0.0.0" /etc/mysql/mysql.conf.d/mysqld.cnf
sed -i "/^\[mysqld\]$/a\server-id=${serverid}" /etc/mysql/mysql.conf.d/mysqld.cnf
sed -i '/^\[mysqld\]$/a\log-bin=/var/log/mysql/mysql-bin.log' /etc/mysql/mysql.conf.d/mysqld.cnf
sed -i '/^\[mysqld\]$/a\relay-log=/var/log/mysql/mysql-relay-bin.log' /etc/mysql/mysql.conf.d/mysqld.cnf
mysql -u${mysqlUser} -p${mysqlPswd} -e "CREATE USER 'root'@'%' IDENTIFIED BY '${mysqlPswd}';GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';FLUSH PRIVILEGES;"

service mysql restart

if [ ${mysql_is_installed} -eq 0 ]
then
    mysqladmin -u ${mysqlUser} password ${mysqlPswd}
fi

mysql -u${mysqlUser} -p${mysqlPswd} -e "stop slave;"
mysql -u${mysqlUser} -p${mysqlPswd} -e "change master to master_host='${masterIp}', master_user='${repUser}', master_password='${repPswd}', master_log_file='${binlogName}', master_log_pos=${position};"
#mysql -u${mysqlUser} -p${mysqlPswd} -e "set GLOBAL SQL_SLAVE_SKIP_COUNTER=1;"
mysql -u${mysqlUser} -p${mysqlPswd} -e "start slave;"

sleep 1

slaveStatus=`mysql -u${mysqlUser} -p${mysqlPswd} -e "show slave status\G;" | grep Yes | wc -l`

echo ${slaveStatus}

if [ ${slaveStatus} -eq "2" ]
then
    echo "SUCCESS"
else 
    echo "FAIL"
fi