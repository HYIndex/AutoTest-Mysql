package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SystemInfo {
    private StringProperty ip;
    private StringProperty cpuUtil;
    private StringProperty memUtil;
    private StringProperty diskUtil;
    private StringProperty netUtil;

    public SystemInfo() {

    }

    public SystemInfo(String ip, String cpuUtil, String memUtil, String diskUtil, String netUtil) {
        this.ip = new SimpleStringProperty(ip);
        this.cpuUtil = new SimpleStringProperty(cpuUtil);
        this.memUtil = new SimpleStringProperty(memUtil);
        this.diskUtil = new SimpleStringProperty(diskUtil);
        this.netUtil = new SimpleStringProperty(netUtil);
    }

    public void setIp(String ip) {
        this.ip = new SimpleStringProperty(ip);
    }

    public void setCpuUtil(String cpuUtil) {
        this.cpuUtil = new SimpleStringProperty(cpuUtil);
    }

    public void setDiskUtil(String diskUtil) {
        this.diskUtil = new SimpleStringProperty(diskUtil);
    }

    public void setMemUtil(String memUtil) {
        this.memUtil = new SimpleStringProperty(memUtil);
    }

    public void setNetUtil(String netUtil) {
        this.netUtil = new SimpleStringProperty(netUtil);
    }

    public String getIp() {
        return ip.get();
    }

    public String getCpuUtil() {
        return cpuUtil.get();
    }

    public String getDiskUtil() {
        return diskUtil.get();
    }

    public String getMemUtil() {
        return memUtil.get();
    }

    public String getNetUtil() {
        return netUtil.get();
    }

    public StringProperty ipProperty() {
        return ip;
    }

    public StringProperty cpuUtilProperty() {
        return cpuUtil;
    }

    public StringProperty diskUtilProperty() {
        return diskUtil;
    }

    public StringProperty memUtilProperty() {
        return memUtil;
    }

    public StringProperty netUtilProperty() {
        return netUtil;
    }
}