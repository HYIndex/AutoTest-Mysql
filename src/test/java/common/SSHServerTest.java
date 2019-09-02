package common;

import com.jcraft.jsch.SftpException;

import java.util.ArrayList;

public class SSHServerTest {

    public static void main(String[] args) {
        String ip = "192.168.170.129";
        String username = "root";
        String password = "index8023";
        String slaveip = "192.168.170.130";
        ArrayList<String> result;

        SSHServer sshServer = new SSHServer(ip, username, password);
        sshServer.connect();
        //String cmd = String.format("/bin/bash /var/autotest/auto_deploy_master.sh %s %s %s %s\n",
        //        ip,username, password, slaveip);
        ////String cmd = "ls";
        //sshServer.execute(cmd);

        try {
            sshServer.upload("/usr/local", "/autotest/tools/sysbench-3.0.0.tar.gz");
        } catch (SftpException e) {
            e.printStackTrace();
        }

        //try {
        //    sshServer.download("/home/zhy", "login.sh", "");
        //} catch (SftpException e) {
        //    e.printStackTrace();
        //}
        sshServer.disconnect();

        //result = sshServer.getStdout();
        //System.out.println("The result is:");
        //for (int i = 0; i < result.size(); i++) {
        //    System.out.println(result.get(i));
        //}
    }
}
