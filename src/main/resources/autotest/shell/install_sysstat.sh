#!/bin/bash
sar -h
if [ $? != 0 ]
then
    apt install sysstat -y > /dev/null
fi
#sar -h
#if [ $? != 0 ]
#then
#    if [ -f /var/autotest/sysstat.tar.gz ]
#    then
#        tar -zxvf /var/autotest/sysstat.tar.gz -C /usr/local/
#        cd /usr/local/sysstat
#        ./configure > /dev/null
#        make
#        make install
#    fi
#fi

#SYSRES=`sar -u -r -d -n DEV 1 1`