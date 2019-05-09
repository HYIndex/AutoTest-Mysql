# 使用说明

运行Main.java进入主界面

## 集群部署

添加主节点和从节点信息后可一键部署

## 性能测试

选择一个测试节点（集群节点或非集群机器），填写测试参数, 创建测试数据库后可开始测试，测试结果下载在resources/autotest/testreport目录
（这里我代码里用的绝对路径，后面打算改配置文件读取，现在使用的话需要\src\main\java\autotest\service\AutotestTool.java中216行中的目录修改为自己的目录）

## 主从延迟测试

填写参数后初始化，然后可选check模式或monitor模式检测，monitor模式持续检测