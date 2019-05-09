package common;

public class ServerNode {
    private String ip;
    private String username;
    private String password;
    private String mysqlUsername;
    private String mysqlPassword;

    public ServerNode(String ip, String username, String password, String mysqlUsername, String mysqlPassword) {
        this.ip = ip;
        this.username = username;
        this.password = password;
        this.mysqlUsername = mysqlUsername;
        this.mysqlPassword = mysqlPassword;
    }

    public ServerNode() {

    }

    public ServerNode(ServerNode s) {
        this.ip = s.getIp();
        this.username = s.getUsername();
        this.password = s.getPassword();
        this.mysqlUsername = s.getMysqlUsername();
        this.mysqlPassword = s.getMysqlPassword();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setMysqlUsername(String mysqlUsername) {
        this.mysqlUsername = mysqlUsername;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String toString() {
        return ip;
    }
}
