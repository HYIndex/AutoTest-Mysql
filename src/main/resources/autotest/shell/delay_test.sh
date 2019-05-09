#!/bin/bash

user=${1}
password=${2}
database=${3}
mod=${4}


# 1.保证主机从机时间同步 通过ntpdate同步网络时钟
#   crontab定时同步 每30分钟同步一次时间
#echo "*/10 *    * * *   root    /usr/sbin/ntpdate -u ntp.api.bz" >> /etc/crontab
## 重启cron应用修改
#service cron restart
# 为防止服务器重启后时间不一致，开机自动执行同步时间
#echo "/usr/sbin/ntpdate -u ntp.api.bz" >> /etc/rc.local
##chmod +x /etc/rc.local

# 同步时间
ntpdate -u ntp.api.bz

# 在Master端守护进程方式运行pt-heartbeat
pt-heartbeat --host=127.0.0.1 --user=${user} --password=${password} \
             --create-table --database=${database} --update --replace \
             --daemonize

# 在Slave端获取主从延迟时间
if [ ${mod} == "check" ]
then
pt-heartbeat --host=127.0.0.1 --user=${user} --password=${password} \
             --database=${database} --master-server-id=1 --check
else
pt-heartbeat --host=127.0.0.1 --user=${user} --password=${password} \
             --database=${database} --master-server-id=1 --monitor
fi