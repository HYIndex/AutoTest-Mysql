#!/bin/bash
#
# Run under root privileges

# Program1:
# Install curl
apt -y install curl
# Install sysbench
curl -s https://packagecloud.io/install/repositories/akopytov/sysbench/script.deb.sh | sudo bash
sudo apt -y install sysbench



# # Program2:
# wget https://github.com/akopytov/sysbench/archive/1.0.17.tar.gz
# tar -zxvf 1.0.17.tar.gz -C /usr/local/

# # Install dependency
# apt -y install make automake libtool pkg-config libaio-dev
# # For MySQL support
# apt -y install libmysqlclient-dev libssl-dev
# # Build and Install
# cd /usr/local/sysbench-1.0.17
# ./autogen.sh
# ./configure --prefix=/usr/local/sysbench/ --with-mysql-includes=/usr/include/mysql --with-mysql-libs=/usr/lib/mysql
# make
# make install
