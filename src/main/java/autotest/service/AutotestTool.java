package autotest.service;

import com.jcraft.jsch.SftpException;
import common.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import autotest.controller.MainwindowController;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import static autotest.controller.MainwindowController.*;
import static java.lang.Thread.sleep;

public class AutotestTool {
    private static final Logger LOGGER = Logger.getLogger(SSHServer.class.getName());
    private static MainwindowController mwc = null;
    private static float sumDelay;
    private static long frequency;
    private static ServerNode[] serverGroup = null;
    /*
     * 自动部署主从复制
     */
    public static void threadDeploySlave(ServerNode slave, String masterIp, String binlogName, String position, int serverId, MainwindowController mwCtrller) {
        // 配置从节点
        SSHServer sshServer = new SSHServer(slave.getIp(), slave.getUsername(), slave.getPassword());
        int res = sshServer.connect();
        if (res == 0) {
            mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：连接成功...", slave.getIp()));
        } else {
            mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：连接失败...", slave.getIp()));
            return;
        }
        try {
            sshServer.upload("/var/autotest", "/autotest/shell/auto_deploy_slave.sh");
        } catch (SftpException e) {
            e.printStackTrace();
            return;
        }
        mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：开始配置...", slave.getIp()));
        String cmd = String.format("/bin/bash /var/autotest/auto_deploy_slave.sh %s %s %s %s %s %d",
                slave.getMysqlUsername(), slave.getMysqlPassword(), masterIp, binlogName, position, serverId);
        sshServer.execute(cmd);
        ArrayList<String> results = sshServer.getStdout();
        LOGGER.debug("Replication Result: " + results.get(results.size() - 1));
        if ("SUCCESS".equals(results.get(results.size() - 1))) {
            mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：配置成功！", slave.getIp()));
        } else {
            mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：配置失败！", slave.getIp()));
        }
        sshServer.disconnect();
        mwCtrller.updateDeployStatusInfo(String.format("从节点[%s]：断开连接...\n", slave.getIp()));

    }

    public static void autoDeployment(ServerNode master, ArrayList<ServerNode> slaves, MainwindowController mwCtrller) {
        mwCtrller.setButDeloyDisable(true);
        // 配置主节点
        SSHServer sshServer = new SSHServer(master.getIp(), master.getUsername(), master.getPassword());
        // 将部署脚本上传到远程主机
        int res = sshServer.connect();
        if (res == 0) {
            mwCtrller.updateDeployStatusInfo(String.format("主节点[%s]：连接成功...", master.getIp()));
        } else {
            mwCtrller.updateDeployStatusInfo(String.format("主节点[%s]：连接失败...", master.getIp()));
            return;
        }
        try {
            sshServer.upload("/var/autotest", "/autotest/shell/auto_deploy_master.sh");
        } catch (SftpException e) {
            e.printStackTrace();
            return;
        }
        // 在远程主机执行脚本
        mwCtrller.updateDeployStatusInfo(String.format("主节点[%s]：开始配置...", master.getIp()));
        String cmd = String.format("/bin/bash /var/autotest/auto_deploy_master.sh %s %s %s",
                master.getIp(), master.getMysqlUsername(), master.getMysqlPassword());
        for (ServerNode slave : slaves) {
            cmd += String.format(" %s", slave.getIp());
        }
        //cmd += "\n";
        sshServer.execute(cmd);
        mwCtrller.updateDeployStatusInfo(String.format("主节点[%s]：主节点配置完成！", master.getIp()));
        sshServer.disconnect();
        mwCtrller.updateDeployStatusInfo(String.format("主节点[%s]：断开连接...\n", master.getIp()));

        ArrayList<String> results = sshServer.getStdout();
        String binlogName = results.get(results.size() - 2);
        LOGGER.debug("binlogName: " + binlogName);
        String position = results.get(results.size() - 1);
        LOGGER.debug("position: " + position);

        // 配置从节点
        Thread[] threads = new Thread[slaves.size()];
        for (int i = 0; i < slaves.size(); i++) {
            ServerNode slave = slaves.get(i);
            final int id = i + 2;
            if (threads[i] == null) {
                threads[i] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        threadDeploySlave(slave, master.getIp(), binlogName, position, id, mwCtrller);
                    }
                });
                threads[i].start();
            }
        }

        for (int i = 0; i < slaves.size(); i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mwCtrller.updateDeployStatusInfo("所有节点配置完成!");
        mwCtrller.setButDeloyDisable(false);
    }

    /*
     * Sysbench性能测试
     */

    // 开始进行sysbench测试
    public static void performanceTest(ServerNode testNode, ServerNode master, ArrayList<ServerNode> slaves, String type, String tableSize,
                                       String tables, String threads, String time, String interval, MainwindowController mwCtrller) {
        mwCtrller.clearPerformanceStatusInfo();
        SSHServer sshServer = new SSHServer(testNode.getIp(), testNode.getUsername(), testNode.getPassword());
        int res = sshServer.connect();
        if (res == 0) {
            mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：连接成功...", testNode.getIp()));
        } else {
            mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：连接失败...", testNode.getIp()));
            return;
        }
        // 上传sysbench3.0和测试脚本
        try {
            sshServer.upload("/var/autotest", "/autotest/testtool/sysbench-3.0.0.tar.gz");
            sshServer.upload("/var/autotest", "/autotest/shell/sysbench_test.sh");
        } catch (SftpException e) {
            e.printStackTrace();
            return;
        }
        // 执行测试脚本
        Date cur = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
        String resname = String.format("sysbench_%s_%s.log", type, fmt.format(cur));
        String slaveHosts = slaves.get(0).getIp();
        String slavePorts = "3306";
        for (int i = 1; i < slaves.size(); i++) {
            slaveHosts += String.format(",%s", slaves.get(i).getIp());
            slavePorts += ",3306";
        }
        String slaveUser = slaves.get(0).getMysqlUsername();
        String slavePass = slaves.get(0).getMysqlPassword();
        String cmd = String.format("/bin/bash /var/autotest/sysbench_test.sh %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
                type, master.getMysqlUsername(), master.getMysqlPassword(), master.getIp(), tableSize, tables, threads, time, interval, resname,
                slaves.size(), slaveHosts, slavePorts, slaveUser, slavePass);
        mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：开始测试...", testNode.getIp()));
        sshServer.execute(cmd);
        mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：测试完成！", testNode.getIp()));
        mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：正在下载测试报告...", testNode.getIp()));
        // 下载测试结果报告
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\autotest\\testreport";
        try {
            sshServer.download("/var/log/sysbench", resname, path);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：测试报告下载完成！", testNode.getIp()));
        sshServer.disconnect();
        mwCtrller.updataPerformanceStatusInfo(String.format("测试节点[%s]：断开连接...\n", testNode.getIp()));
        String filename = String.format("%s\\%s", path, resname);
        mwCtrller.curSysbenchReport = new String(filename);
        //showSysbenchTestReport(filename, mwCtrller);
    }

    public static void showSysbenchTestReport(String reportFile, MainwindowController mwc) {
        mwc.clearSysbenchTestReport();
        File file = null;
        while (file == null) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            file = new File(reportFile);
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                mwc.appendSysbenchTestReport(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /*
     * 主从延迟测试
     */
    public static void delayTestInit(ServerNode master, ArrayList<ServerNode> slaves, String db, MainwindowController mwCtrller) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SSHServer sshServer = new SSHServer(master.getIp(), master.getUsername(), master.getPassword());
                int res = sshServer.connect();
                if (res == 1) {
                    MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", master.getIp()), ButtonType.OK);
                    return;
                }
                try {
                    sshServer.upload("/var/autotest", "/autotest/testtool/percona-toolkit_3.0.13-1.xenial_amd64.deb");
                    sshServer.upload("/var/autotest", "/autotest/shell/delay_test_init.sh");
                } catch (SftpException e) {
                    e.printStackTrace();
                    return;
                }
                String cmd = String.format("/bin/bash /var/autotest/delay_test_init.sh %s %s %s %s", "master", db, master.getMysqlUsername(), master.getMysqlPassword());
                sshServer.execute(cmd);
                sshServer.disconnect();
                //ArrayList<String> results = sshServer.getStdout();
                //if (results.size() > 0) {
                //    LOGGER.debug("Master init result: " + results.get(results.size() - 1));
                //}
            }
        });
        t.start();
        Thread[] ts = new Thread[slaves.size()];
        int i = 0;
        for (ServerNode slave : slaves) {
            ts[i] = new Thread(new Runnable() {
                @Override
                public void run() {

                    SSHServer sshServer = new SSHServer(slave.getIp(), slave.getUsername(), slave.getPassword());
                    int res = sshServer.connect();
                    if (res == 1) {
                        MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", slave.getIp()), ButtonType.OK);
                        return;
                    }
                    try {
                        sshServer.upload("/var/autotest", "/autotest/testtool/percona-toolkit_3.0.13-1.xenial_amd64.deb");
                        sshServer.upload("/var/autotest", "/autotest/shell/delay_test_init.sh");
                    } catch (SftpException e) {
                        e.printStackTrace();
                        return;
                    }
                    String cmd = String.format("/bin/bash /var/autotest/delay_test_init.sh %s %s %s %s", "slave", db, slave.getMysqlUsername(), slave.getMysqlPassword());
                    sshServer.execute(cmd);
                    sshServer.disconnect();
                    //ArrayList<String> results = sshServer.getStdout();
                    //if (results.size() > 0) {
                    //    LOGGER.debug("Master init result: " + results.get(results.size() - 1));
                    //}
                }
            });
            ts[i].start();
            i++;
        }
        try {
            t.join();
            for (i = 0; i < slaves.size(); i++) {
                ts[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void delayTest(ServerNode testSlaveNode, String db, int mod, String interval, MainwindowController mwCtrller) {
        SSHServer sshServer = new SSHServer(testSlaveNode.getIp(), testSlaveNode.getUsername(), testSlaveNode.getPassword());
        int res = sshServer.connect();
        if (res == 1) {
            MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", testSlaveNode.getIp()), ButtonType.OK);
            return;
        }
        String cmd = null;;
        ArrayList<String> results = null;
        if (mod == CHECK) {
            cmd = String.format("pt-heartbeat --user=%s --password=%s --database=%s --master-server-id=1 --check",
                    testSlaveNode.getMysqlUsername(), testSlaveNode.getMysqlPassword(), db);
            sshServer.execute(cmd);
            results = sshServer.getStdout();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if (results.size() == 1) {
                mwCtrller.addDelayInfo(fmt.format(new Date()), String.format("%s", results.get(0)), "");
            }
        } else if (mod == MONITOR) {
            mwc = mwCtrller;
            sumDelay = 0;
            frequency = 0;
            mwCtrller.clearDelayInfo();
            while (true) {
                if (monitorStop) {
                    break;
                }
                final String cmd1 = String.format("pt-heartbeat --user=%s --password=%s --database=%s --master-server-id=1 --check",
                        testSlaveNode.getMysqlUsername(), testSlaveNode.getMysqlPassword(), db);
                CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
                    sshServer.execute(cmd1);
                    ArrayList<String> r = sshServer.getStdout();
                    if (r.size() == 1) {
                        return r.get(0);
                    } else {
                        return "";
                    }
                });
                CompletableFuture<Void> f1 = f.thenAccept(AutotestTool::showDelayInfoAsycn);
                int itime = Integer.parseInt(interval) * 1000;
                try {
                    sleep(itime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mwc = null;
            sumDelay = 0;
            frequency = 0;
        }
        sshServer.disconnect();
    }

    public static void showDelayInfoAsycn(String delay) {
        sumDelay += Float.parseFloat(delay);
        frequency += 1;
        float avg = sumDelay / frequency;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        mwc.addDelayInfo(fmt.format(new Date()), String.format("%s", delay), String.format("%.3f", avg));
    }

    /*
     * 主从一致测试
     */
    public static ArrayList<ConsistentInfo> consistentTest(ServerNode master, String db) {
        SSHServer sshServer = new SSHServer(master.getIp(), master.getUsername(), master.getPassword());
        int res = sshServer.connect();
        if (res == 1) {
            MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", master.getIp()), ButtonType.OK);
            return null;
        }
        try {
            sshServer.upload("/var/autotest", "/autotest/shell/consistent_test.sh");
        } catch (SftpException e) {
            e.printStackTrace();
            return null;
        }
        String cmd = String.format("/bin/bash /var/autotest/consistent_test.sh %s %s %s %s", master.getIp(), master.getMysqlUsername(), master.getMysqlPassword(), db);
        sshServer.execute(cmd);
        sshServer.disconnect();
        ArrayList<String> results = sshServer.getStdout();
        if (results.size() <= 3) {
            return null;
        }
        ArrayList<ConsistentInfo> ret = new ArrayList<ConsistentInfo>();
        for (int i = 3; i < results.size(); i++) {
            String[] sl = results.get(i).split("[ ]+");
            if (sl.length > 8) {
                ret.add(new ConsistentInfo(sl[1], sl[2], sl[3], sl[4], sl[5], sl[6], sl[7], sl[8]));
            }
        }
        return ret;
    }

    public static ArrayList<SlaveDiffInfo> slaveDiffTest(ServerNode slave) {
        SSHServer sshServer = new SSHServer(slave.getIp(), slave.getUsername(), slave.getPassword());
        int res = sshServer.connect();
        if (res == 1) {
            MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", slave.getIp()), ButtonType.OK);
            return null;
        }
        String cmd = String.format("mysql -e \"SELECT db, tbl, SUM(this_cnt) AS total_rows, COUNT(*) AS chunks FROM percona.checksums WHERE (  master_cnt <> this_cnt  OR master_crc <> this_crc  OR ISNULL(master_crc) <> ISNULL(this_crc)) GROUP BY db, tbl;\"");
        sshServer.execute(cmd);
        sshServer.disconnect();
        ArrayList<String> results = sshServer.getStdout();
        if (results.size() <= 1) {
            return null;
        }
        ArrayList<SlaveDiffInfo> ret = new ArrayList<SlaveDiffInfo>();
        for (int i = 1; i < results.size(); i++) {
            String[] sl = results.get(i).split("\t");
            if (sl.length == 4) {
                ret.add(new SlaveDiffInfo(slave.getIp(), sl[0], sl[1], sl[2], sl[3]));
            }
        }
        return ret;
    }


    /*
     * 业务场景测试
     */
    public static void businessScenarioTest(ServerNode testNode, ServerNode server, String warehouses, String connects, String warmup,
                                            String duration, String reportInterval, MainwindowController mwCtrller) {
        mwCtrller.clearBusinessScenarioStatusInfo();
        SSHServer sshServer = new SSHServer(testNode.getIp(), testNode.getUsername(), testNode.getPassword());
        int res = sshServer.connect();
        if (res == 0) {
            mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：连接成功...", testNode.getIp()));
        } else {
            mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：连接失败...", testNode.getIp()));
            return;
        }
        // 上传测试脚本
        try {
            sshServer.upload("/var/autotest", "/autotest/shell/business_scenario_test.sh");
        } catch (SftpException e) {
            e.printStackTrace();
            return;
        }
        Date cur = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
        String resname = String.format("tpcc_%s_%s.log", warehouses, fmt.format(cur));
        String cmd = String.format("/bin/bash /var/autotest/business_scenario_test.sh %s %s %s %s %s %s %s %s %s",
                server.getIp(), server.getMysqlUsername(), server.getMysqlPassword(), warehouses, connects, warmup, duration, reportInterval, resname);
        mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：开始测试...", testNode.getIp()));
        sshServer.execute(cmd);
        mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：测试完成！", testNode.getIp()));
        mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：正在下载测试报告...", testNode.getIp()));
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\autotest\\testreport";
        try {
            sshServer.download("/var/log/tpcc", resname, path);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：测试报告下载完成！", testNode.getIp()));
        sshServer.disconnect();
        mwCtrller.updataBusinessScenarioStatusInfo(String.format("测试节点[%s]：断开连接...\n", testNode.getIp()));
        String filename = String.format("%s\\%s", path, resname);
        mwCtrller.curTpccReport = filename;
    }

    public static void showBSTestReport(String reportFile, MainwindowController mwc) {
        mwc.clearBSTestReport();
        File file = null;
        while (file == null) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            file = new File(reportFile);
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                mwc.appendBSTestReport(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public static void systemResourceMonitor(ServerNode master, ArrayList<ServerNode> slaves, MainwindowController mwCtrller) {
        mwc = mwCtrller;
        serverGroup = new ServerNode[slaves.size() + 1];
        serverGroup[0] = new ServerNode(master);
        for (int i = 1; i < serverGroup.length; i++) {
            serverGroup[i] = new ServerNode(slaves.get(i - 1));
        }
        Thread[] thds = new Thread[serverGroup.length];
        for (int i = 0; i < serverGroup.length; i ++) {
            final int idx = i;
            thds[idx] =  new Thread(new Runnable() {
                @Override
                public void run() {
                    SSHServer sshServer = new SSHServer(serverGroup[idx].getIp(), serverGroup[idx].getUsername(), serverGroup[idx].getPassword());
                    int res = sshServer.connect();
                    if (res != 0) {
                        MessageBox.showMessage(Alert.AlertType.ERROR, "错误：", String.format("[%s]:连接失败！", serverGroup[idx].getIp()), ButtonType.OK);
                        return;
                    }
                    String cmd = "apt-get install sysstat -y > /dev/null";
                    sshServer.execute(cmd);
                    while (true) {
                        if (monitorStop) {
                            break;
                        }
                        CompletableFuture<Pair<Integer, ArrayList<String>>> f = CompletableFuture.supplyAsync(() -> {
                            String cmd1 = "sar -urd -n DEV 1 1 | grep Average";
                            sshServer.execute(cmd1);
                            ArrayList<String> results = sshServer.getStdout();
                            Pair<Integer, ArrayList<String>> ret = new Pair<>(idx, results);
                            return ret;
                        });
                        CompletableFuture<Void> f1 = f.thenAccept(AutotestTool::showSystemInfoAsycn);
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sshServer.disconnect();
                }
            });
            thds[idx].start();
        }
    }

    public static void showSystemInfoAsycn(Pair<Integer, ArrayList<String>> pair) {
        int index = pair.getKey();
        ArrayList<String> info = pair.getValue();
        SystemInfo systemInfo = DataAnalysis.extractSystemInfo(info);
        systemInfo.setIp(serverGroup[index].getIp());
        mwc.updateSystemInfo(index, systemInfo);
    }
}
