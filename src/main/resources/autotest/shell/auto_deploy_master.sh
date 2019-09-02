#!/usr/bin/env bash
#！/bin/bash
# 所有命令默认在root用户执行
# Master：
masterIp=${1}
mysqlUser=${2}
mysqlPswd=${3}
# 第三个参数之后是若干个从节点ip
repUser='repuser'
repPswd='1234567'
# check mysql exist or not and version is 5.7.25
mysql_is_installed=0
checkMysql() {
    mysql --version
    if [ $? -eq 0 ]
    then
        mysql_is_installed=1
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

# Master 配置
# 1. 安装MySQL（如果服务器没有安装）
checkMysql
#ins_pt_heartbeat
# 2. 修改MySQL配置文件
sed -i "/^bind-address/c\bind-address=0.0.0.0" /etc/mysql/mysql.conf.d/mysqld.cnf
sed -i '/^\[mysqld\]$/a\server-id=1' /etc/mysql/mysql.conf.d/mysqld.cnf
sed -i '/^\[mysqld\]$/a\log-bin=/var/log/mysql/mysql-bin.log' /etc/mysql/mysql.conf.d/mysqld.cnf
mysql -u${mysqlUser} -p${mysqlPswd} -e "CREATE USER 'root'@'%' IDENTIFIED BY '${mysqlPswd}';GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';FLUSH PRIVILEGES;"
# 3. 重启MySQL
service mysql restart
# 4. 设置密码，授予从库复制权限
if [ ${mysql_is_installed} -eq 0 ]
then
    mysqladmin -u ${mysqlUser} password ${mysqlPswd}
fi
index=1;
for arg in $*
do
    if [ ${index} -gt 3 ]
    then
        mysql -u${mysqlUser} -p${mysqlPswd} -e "grant replication slave on *.* to '${repUser}'@'${arg}' identified by '${repPswd}';"
    fi
    let index+=1
done
masterStatus=`mysql -u${mysqlUser} -p${mysqlPswd} -e "show master status;"`

# 5. 获取日志文件名和位置
binlogName=`echo "${masterStatus}" | grep "bin" | awk '{print $1}'`
position=`echo "${masterStatus}" | grep "bin" | awk '{print $2}'`
echo ${binlogName}
echo ${position}