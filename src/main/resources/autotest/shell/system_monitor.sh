#!/bin/bash

echo `top -n 1 | grep "Cpu(s)" | cut -d ":" -f 2`
echo `top -n 1 | grep "Mem :" | cut -d ":" -f 2`
#echo `top -n 1 | grep "Swap" | cut -d ":" -f 2`

iostat -V > /dev/null
if [ $? != 0 ]
then
    apt-get install sysstat -y > /dev/null
fi
echo `iostat -x 1 1 | grep sda | awk '{print $14}'`

echo `sar -n DEV 1 1 | grep Average | grep -v IFACE | awk '{print $10}'`

SYSRES=`sar -u -r -d -n DEV 1 1`