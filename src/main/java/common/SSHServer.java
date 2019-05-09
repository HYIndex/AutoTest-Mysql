package common;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import static java.lang.Thread.sleep;

public class SSHServer {
    private static final Logger LOGGER = Logger.getLogger(SSHServer.class.getName());
    /*
     * 远程主机信息
     */
    private String ip;
    private String username;
    private String password;
    /*
     * SSH默认端口
     */
    public static final int DEFAULT_SSH_PORT = 22;
    /*
     * 保存输出结果
     */
    private ArrayList<String> stdout;

    Session session = null;
    ChannelExec channelExec = null;
    ChannelSftp channelSftp = null;

    public SSHServer(final String ip, final String uname, final String passwd) {
        this.ip = ip;
        this.username = uname;
        this.password = passwd;
        stdout = new ArrayList<String>();
    }

    public SSHServer() {
        stdout = new ArrayList<String>();
    }

    /*
     * 创建Session，连接sftp服务器
     */
    public int connect(){
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(username, ip, DEFAULT_SSH_PORT);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            LOGGER.info("sshSession connected...");
        } catch (JSchException e) {
            e.printStackTrace();
            LOGGER.error("connect failed!");
            return -1;
            //showMessage(Alert.AlertType.INFORMATION, "error", "connect failed!", ButtonType.OK);
        }
        return 0;
    }

    /*
     * 断开连接
     */
    public void disconnect() {
        if (session != null) {
            session.disconnect();
            LOGGER.info("sshSession closed!");
            session = null;
        }
    }

    /*
     * 在远程服务器执行命令
     */
    public int execute(final String cmd) {
        int retCode = 0;
        try {
            /*
             * 打开通道，设置通道类型和执行的命令
             */
            Channel channel = this.session.openChannel("exec");
            channelExec = (ChannelExec)channel;
            channelExec.setCommand(cmd);

            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            BufferedReader input = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            channelExec.connect();
            /*
             * 接受远程服务器执行结果/bin/bash /var/autotest/auto_deploy_master.sh 192.168.170.129 root index8023 192.168.170.130
             */
            String line;
            stdout.clear();
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            input.close();

            if (channelExec.isClosed()) {
                retCode = channelExec.getExitStatus();
            }

            if (retCode < 0) {
                LOGGER.info("Done, but exit status not set!");
            } else if (retCode > 0) {
                LOGGER.info("Done, but with error!");
            } else {
                LOGGER.info("Done!");
            }
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (channelExec != null && channelExec.isConnected()) {
            channelExec.disconnect();
            channelExec = null;
            LOGGER.info("channel-exec closed!");
        }
        return retCode;
    }

    /*
     * 获取命令输出
     */
    public ArrayList<String> getStdout() {
        return stdout;
    }

    /*
     * 上传文件
     */
    public void upload(final String targetDir, final String sourceFile) throws SftpException {
        try {
            Channel channel = session.openChannel("sftp");
            channelSftp = (ChannelSftp)channel;
            channelSftp.connect();
            LOGGER.info("channel-sftp connected...");
            channelSftp.cd(targetDir);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            LOGGER.warn("target directory is not exist!");
            channelSftp.mkdir(targetDir);
            LOGGER.info("success create target directory...");
            channelSftp.cd(targetDir);
        }
        File srcfile = new File(SSHServer.class.getResource(sourceFile).getPath());
        try {
            channelSftp.put(new FileInputStream(srcfile), srcfile.getName());
            LOGGER.info(String.format("file:%s upload success...", sourceFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
            channelSftp = null;
            LOGGER.info("channel-sftp closed!");
        }
    }

    /*
     * 下载文件
     */
    public void download(String downloadDir, String downloadFile, String saveDir) throws SftpException {
        try {
            Channel channel = session.openChannel("sftp");
            channelSftp = (ChannelSftp)channel;
            channelSftp.connect();
            LOGGER.info("channel-sftp connected...");
            if (downloadDir != null && !downloadDir.isEmpty()) {
                channelSftp.cd(downloadDir);
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
        File savefile = new File(saveDir);
        if (!savefile.exists()) {
            savefile.mkdirs();
        }
        try {
            OutputStream os = new FileOutputStream(new File(saveDir, downloadFile));
            channelSftp.get(downloadFile, os);
            os.flush();
            os.close();
            LOGGER.info(String.format("file:%s download success...", downloadFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
            channelSftp = null;
            LOGGER.info("channel-sftp closed!");
        }
    }

}
