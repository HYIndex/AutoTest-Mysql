#!/bin/bash


nodetype=${1}
database=${2}
user=${3}
pass=${4}
#echo ${nodetype}
#echo ${database}
#echo ${user}
#echo ${pass}


# 安装pt-heartbeat工具
pt-heartbeat --version
if [ $? != 0 ]
then
    if [ -f /var/autotest/percona-toolkit_3.0.13-1.xenial_amd64.deb ]
    then
        apt-get update -y >/dev/null
        dpkg -i /var/autotest/percona-toolkit_3.0.13-1.xenial_amd64.deb >/dev/null
        apt-get -f install -y >/dev/null
    fi
fi

# 同步时间
ntpdate -v
if [ $? == 127 ]
then
    apt-get update >/dev/null
    apt-get install ntpdate -y >/dev/null
fi
ntpdate -u ntp.api.bz

if [ "${nodetype}" == "master" ]
then
    # echo "here 1"
    mysql -e "create database if not exists ${database};"
    exist=`ps -aux | grep pt-heartbeat | grep -v grep | wc -l`
    if [ $exist != 0 ]
    then
        pt-heartbeat --stop
        rm /tmp/pt-heartbeat-sentinel
    fi
    nohup pt-heartbeat --user=${user} --password=${pass} --host=localhost --create-table -D ${database} --update --replace --daemonize &>/dev/null 2>&1 &

fi
echo $?

#pt-heartbeat --user=root --password=mysql -D testdb --master-server-id=1 --monitor
#CREATE TABLE IF NOT EXISTS `runoob_tbl`(
#   `runoob_id` INT UNSIGNED AUTO_INCREMENT,
#   `runoob_title` VARCHAR(100) NOT NULL,
#   `runoob_author` VARCHAR(40) NOT NULL,
#   `submission_date` DATE,
#   PRIMARY KEY ( `runoob_id` )
#)ENGINE=InnoDB DEFAULT CHARSET=utf8;
#INSERT INTO runoob_tbl
#(runoob_title, runoob_author, submission_date)
#VALUES("test1", "test1", NOW());