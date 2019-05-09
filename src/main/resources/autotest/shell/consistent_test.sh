#!/usr/bin bash

host=$1
user=$2
pass=$3
database=$4

# 安装pt-table-checksum工具
pt-table-checksum --version >/dev/null
if [ $? == 127 ]
then
    wget https://www.percona.com/downloads/percona-toolkit/3.0.13/binary/debian/xenial/x86_64/percona-toolkit_3.0.13-1.xenial_amd64.deb >/dev/null
    dpkg -i percona-toolkit_3.0.13-1.xenial_amd64.deb >/dev/null
    apt-get -f install -y >/dev/null
fi

pt-table-checksum h=${host},u=${user},p=${pass},P=3306 -d ${database}  --nocheck-replication-filters --no-check-binlog-format --recursion-method=processlist

## 本示例为DSN方式检查
## 主库上创建percona数据库
#CREATE DATABASE percona;
## 主库上创建checksums和dsns表
#CREATE TABLE checksums (
#   db             CHAR(64)     NOT NULL,
#   tbl            CHAR(64)     NOT NULL,
#   chunk          INT          NOT NULL,
#   chunk_time     FLOAT            NULL,
#   chunk_index    VARCHAR(200)     NULL,
#   lower_boundary TEXT             NULL,
#   upper_boundary TEXT             NULL,
#   this_crc       CHAR(40)     NOT NULL,
#   this_cnt       INT          NOT NULL,
#   master_crc     CHAR(40)         NULL,
#   master_cnt     INT              NULL,
#   ts             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
#   PRIMARY KEY (db, tbl, chunk),
#   INDEX ts_db_tbl (ts, db, tbl)
#) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#CREATE TABLE `dsns` (
#  `id` int(11) NOT NULL AUTO_INCREMENT,
#  `parent_id` int(11) DEFAULT NULL,
#  `dsn` varchar(255) NOT NULL,
#  PRIMARY KEY (`id`)
#);
## 主库上创建一个用户，从pt测试的服务器上既可以通过该用户连接主库，也可以通过该用户连接从库
#GRANT ALL PRIVILEGEES on percona.* to 'percona_user'@'%' IDENTIFIED BY 'percona_pass';
#GRANT SELECT,LOCK TABLES,PROCESS,SUPER on *.* to 'percona_user'@'%';
## 主库上dsns表插入从库信息
#INSERT INTO dsns(dsn) VALUES('h=repl_host,P=repl_port')
## 连接主库执行pt工具
#pt-table-checksum h='master_host',u='percona_user',p='percona_pass',P=master_port --databases=xxx --tables=xx,xxx --nocheck-replication-filters --nocreate-replicate-table --no-check-binlog-format --recursion-method dsn=h=repl_host,P=repl_port,D='percona',t='dsns'
## 返回结果