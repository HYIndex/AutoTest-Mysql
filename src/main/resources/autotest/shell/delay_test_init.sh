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
        # wget https://www.percona.com/downloads/percona-toolkit/3.0.13/binary/debian/xenial/x86_64/percona-toolkit_3.0.13-1.xenial_amd64.deb >/dev/null
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
    mysqladmin create ${database}
    exist=`ps -aux | grep pt-heartbeat | grep -v grep | wc -l`
    if [ $exist == 0 ]
    then
        nohup pt-heartbeat --user=${user} --password=${pass} --host=localhost --create-table -D ${database} --update --replace --daemonize &>/dev/null 2>&1 &
    fi
fi
echo $?

#pt-heartbeat --user=root --password=mysql -D testdb --master-server-id=1 --monitor