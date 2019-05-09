package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GroupInfo {
    private StringProperty nodeType;
    private StringProperty ip;
    private StringProperty userName;
    private StringProperty mysqlUsername;

    public GroupInfo() {
    }

    public GroupInfo(String nodeType, String ip, String userName, String mysqlUsername) {
        this.nodeType = new SimpleStringProperty(nodeType);
        this.ip = new SimpleStringProperty(ip);
        this.userName = new SimpleStringProperty(userName);
        this.mysqlUsername = new SimpleStringProperty(mysqlUsername);
    }

    public String getNodeType() {
        return nodeType.get();
    }

    public String getIp() {
        return ip.get();
    }

    public String getUserName() {
        return userName.get();
    }

    public String getMysqlUsername() {
        return mysqlUsername.get();
    }

    public StringProperty nodeTypeProperty() {
        return nodeType;
    }

    public StringProperty ipProperty() {
        return ip;
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public StringProperty mysqlUsernameProperty() {
        return mysqlUsername;
    }

    public void setNodeType(String nodeType) {
        this.nodeType.set(nodeType);
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public void setMysqlUsername(String mysqlUsername) {
        this.mysqlUsername.set(mysqlUsername);
    }
}
