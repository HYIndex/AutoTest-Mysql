package autotest.controller;

import autotest.service.AutotestTool;
import autotest.service.DataAnalysis;
import common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainwindowController implements Initializable {
    public static final int CHECK = 1;
    public static final int MONITOR = 2;
    public static volatile boolean monitorStop = false;

    @FXML
    private Tab tabDeploy;
    @FXML
    private Tab tabPerformance;
    @FXML
    private Tab tabDelay;
    @FXML
    private Tab tabBusinessScenario;
    @FXML
    private Tab tabSystemBottleneck;

    @FXML
    private TextField tfMasterIp;
    @FXML
    private TextField tfMasterUname;
    @FXML
    private PasswordField pfMasterPswd;
    @FXML
    private TextField tfMasterSqlUname;
    @FXML
    private PasswordField pfMasterSqlPswd;
    @FXML
    private TextField tfSlaveIp;
    @FXML
    private TextField tfSlaveUname;
    @FXML
    private PasswordField pfSlavePswd;
    @FXML
    private TextField tfSlaveSqlUname;
    @FXML
    private PasswordField pfSlaveSqlPswd;
    @FXML
    private TextArea trDeployStatusInfo;
    @FXML
    private Button butAutoDeploy;
    @FXML
    private TableView<GroupInfo> tvGroupInfo;
    @FXML
    private TableColumn<GroupInfo, String> tcNodeType;
    @FXML
    private TableColumn<GroupInfo, String> tcNodeIp;
    @FXML
    private TableColumn<GroupInfo, String> tcUsername;
    @FXML
    private TableColumn<GroupInfo, String> tcMysqlUsername;

    @FXML
    private ComboBox<String> cbTestNodeIp;
    @FXML
    private TextField tfTestNodeUsername;
    @FXML
    private TextField pfTestNodePassword;
    @FXML
    private ComboBox<String> cbTestType;
    @FXML
    private ComboBox<String> cbTableSize;
    @FXML
    private ComboBox<String> cbTableNum;
    @FXML
    private ComboBox<String> cbThreadNum;
    @FXML
    private Label lbThreads;
    @FXML
    private ComboBox<String> cbTestTime;
    @FXML
    private ComboBox<String> cbReportInterval;
    @FXML
    private TextArea trPerformanceSatusInfo;
    @FXML
    private TextArea trSysbenchTestReport;
    @FXML
    private LineChart<String, Float> lcTPSOfThread;

    @FXML
    private ComboBox<ServerNode> cbSlaveNode;
    @FXML
    private TextField tfTestDatabase;
    @FXML
    private RadioButton rbCheckMod;
    @FXML
    private RadioButton rbMonitorMod;
    @FXML
    private TextField tfMonitorInterval;
    @FXML
    private TableView<DelayInfo> tvDelayInfo;
    @FXML
    private TableColumn<DelayInfo, String> tcDatetime;
    @FXML
    private TableColumn<DelayInfo, String> tcRealtimeDelay;
    @FXML
    private TableColumn<DelayInfo, String> tcAverageDelay;
    @FXML
    private Button butCheckDelay;

    @FXML
    private TextField tfConsistentTestdb;
    @FXML
    private TableView<ConsistentInfo> tvConsistentInfo;
    @FXML
    private TableColumn<ConsistentInfo, String> tcErrors;
    @FXML
    private TableColumn<ConsistentInfo, String> tcDiffs;
    @FXML
    private TableColumn<ConsistentInfo, String> tcRows;
    @FXML
    private TableColumn<ConsistentInfo, String> tcDiffRows;
    @FXML
    private TableColumn<ConsistentInfo, String> tcChunks;
    @FXML
    private TableColumn<ConsistentInfo, String> tcSkipped;
    @FXML
    private TableColumn<ConsistentInfo, String> tcTime;
    @FXML
    private TableColumn<ConsistentInfo, String> tcTableWithDb;
    @FXML
    private TableView<SlaveDiffInfo> tvSlaveDiffInfo;
    @FXML
    private TableColumn<SlaveDiffInfo, String> tcSlaveIp;
    @FXML
    private TableColumn<SlaveDiffInfo, String> tcDatabase;
    @FXML
    private TableColumn<SlaveDiffInfo, String> tcTable;
    @FXML
    private TableColumn<SlaveDiffInfo, String> tcTotalRows;
    @FXML
    private TableColumn<SlaveDiffInfo, String> tcSlaveChunks;

    @FXML
    private ComboBox<String> cbBSTestNodeIp;
    @FXML
    private TextField tfBSTestNodeUsername;
    @FXML
    private PasswordField pfBSTestNodePassword;
    @FXML
    private ComboBox<String> cbWarehouses;
    @FXML
    private ComboBox<String> cbConnects;
    @FXML
    private Label lbConnects;
    @FXML
    private ComboBox<String> cbWarmupTime;
    @FXML
    private ComboBox<String> cbDuration;
    @FXML
    private ComboBox<String> cbBSReportInterval;
    @FXML
    private TextArea trBusinessScenarioStatus;
    @FXML
    private TextArea trBSTestReport;
    @FXML
    private LineChart<String, Float> lcTpmCOfConnect;

    @FXML
    private TableView<SystemInfo> tvSystemInfo;
    @FXML
    private TableColumn<SystemInfo, String> tcSBNodeIp;
    @FXML
    private TableColumn<SystemInfo, String> tcCpuUtil;
    @FXML
    private TableColumn<SystemInfo, String> tcMemUtil;
    @FXML
    private TableColumn<SystemInfo, String> tcDiskUtil;
    @FXML
    private TableColumn<SystemInfo, String> tcNetUtil;
    @FXML
    private Button butSysBotStart;

    private ServerNode master;
    private ArrayList<ServerNode> slaves;
    private boolean isMasterConfirmed;
    private boolean isSlaveAdded;

    private  ServerNode testNode;
    private Set<Integer> threadNums = new TreeSet<>();
    private Set<Integer> connectNums = new TreeSet<>();
    public String curSysbenchReport = null;
    public String curTpccReport = null;

    private ObservableList<DelayInfo> delayInfos = FXCollections.observableArrayList();
    private ObservableList<GroupInfo> groupInfos = FXCollections.observableArrayList();
    private ObservableList<ConsistentInfo> consistentInfos = FXCollections.observableArrayList();
    private ObservableList<SlaveDiffInfo> slaveDiffInfos = FXCollections.observableArrayList();
    private ObservableList<SystemInfo> systemInfos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDeployment();
        initPerformanceTest();
        initMSDeplayTest();
        initConsistentTest();
        initBusinessScenarioTest();
        initSystemBottleneck();
    }

    /*
     * 集群部署模块--begin
     */
    private void initDeployment() {
        tfMasterIp.setText("192.168.170.129");
        tfMasterUname.setText("root");
        pfMasterPswd.setText("index8023");
        tfMasterSqlUname.setText("root");
        pfMasterSqlPswd.setText("mysql");
        tfSlaveIp.setText("192.168.170.130");
        tfSlaveUname.setText("root");
        pfSlavePswd.setText("index8023");
        tfSlaveSqlUname.setText("root");
        pfSlaveSqlPswd.setText("mysql");
        master = null;
        slaves = new ArrayList<ServerNode>();
        isMasterConfirmed = false;
        isSlaveAdded = false;
        butAutoDeploy.setDisable(true);

        tvGroupInfo.setItems(groupInfos);
        tcNodeType.setCellValueFactory(cellData -> cellData.getValue().nodeTypeProperty());
        tcNodeIp.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        tcUsername.setCellValueFactory(cellData -> cellData.getValue().userNameProperty());
        tcMysqlUsername.setCellValueFactory(cellData -> cellData.getValue().mysqlUsernameProperty());
    }

    public void onClickAddSlave() {
        if (master != null) {
            if (master.getIp().equals(tfSlaveIp.getText())) {
                MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"该节点已为主节点！", ButtonType.OK);
                return;
            }
        }
        if (slaves != null && slaves.size() > 0) {
            for (ServerNode s : slaves) {
                if (s.getIp().equals(tfSlaveIp.getText())) {
                    MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"节点已存在！", ButtonType.OK);
                    return;
                }
            }
        }
        isSlaveAdded = true;
        slaves.add(new ServerNode(tfSlaveIp.getText(), tfSlaveUname.getText(),
                pfSlavePswd.getText(), tfSlaveSqlUname.getText(), pfMasterSqlPswd.getText()));
        groupInfos.add(new GroupInfo((String)"Slave", tfSlaveIp.getText(), tfSlaveUname.getText(), tfSlaveSqlUname.getText()));

        if (isSlaveAdded && isMasterConfirmed) {
            butAutoDeploy.setDisable(false);
        }
    }

    public void onClickConfirmMaster() {
        if (master != null) {
            if (master.getIp().equals(tfMasterIp.getText())) {
                MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"请勿重复操作！", ButtonType.OK);
                return;
            } else {
                boolean res = MessageBox.confirmBox((String)"确认", (String)"是否更改主节点\n注意：更改主节点会清除当前集群信息");
                if (res) {
                    slaves.clear();
                    groupInfos.clear();
                } else {
                    return;
                }
            }
        }
        isMasterConfirmed = true;
        master = new ServerNode(tfMasterIp.getText(), tfMasterUname.getText(),
                pfMasterPswd.getText(), tfMasterSqlUname.getText(), pfMasterSqlPswd.getText());
        groupInfos.add((int)0, new GroupInfo((String)"Master", tfMasterIp.getText(), tfMasterUname.getText(), tfMasterSqlUname.getText()));

        if (isSlaveAdded && isMasterConfirmed) {
            butAutoDeploy.setDisable(false);
        }
    }

    public void onClickClearMaster() {
        tfMasterIp.clear();
        tfMasterUname.clear();
        pfMasterPswd.clear();
        tfMasterSqlUname.clear();
        pfMasterSqlPswd.clear();
    }

    public void onClickClearSlave() {
        tfSlaveIp.clear();
        tfSlaveUname.clear();
        pfSlavePswd.clear();
        tfSlaveSqlUname.clear();
        pfSlaveSqlPswd.clear();
    }

    public void onClickDeploy() {
        trDeployStatusInfo.clear();
        // TODO:检查所有参数是否都不为空
        if (master == null || slaves.size() < 1) {
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AutotestTool.autoDeployment(master, slaves, MainwindowController.this);

            }
        });
        t.start();
    }

    public void setButDeloyDisable(boolean v) {
        butAutoDeploy.setDisable(v);
    }

    public void updateDeployStatusInfo(String info) {
        synchronized (this) {
            trDeployStatusInfo.appendText(info + "\n");
        }
    }
    /*
     * 集群部署模块--end
     */


    /*
     * 性能测试模块--begin
     */
    private void initPerformanceTest() {
        lcTPSOfThread.setVisible(false);

        cbTestType.getItems().addAll("point_select", "insert", "update_index",
                "update_non_index", "delete", "read_only", "write_only", "read_write");
        cbTestType.getSelectionModel().select(0);
        cbTableSize.getItems().addAll("1000", "10000", "100000");
        cbTableSize.getSelectionModel().select(1);
        cbTableNum.getItems().addAll("10", "100", "1000");
        cbTableNum.getSelectionModel().select(1);
        cbThreadNum.getItems().addAll("4", "8", "16", "32", "64", "128", "256", "512");
        cbThreadNum.getSelectionModel().select(0);
        cbTestTime.getItems().addAll("60", "180", "300", "600");
        cbTestTime.getSelectionModel().select(1);
        cbReportInterval.getItems().addAll("5", "10", "20");
        cbReportInterval.getSelectionModel().select(1);

        tabPerformance.setOnSelectionChanged(event -> {
            cbTestNodeIp.getItems().clear();
            if (master != null) {
                cbTestNodeIp.getItems().add(master.getIp());
            }
            if (slaves != null) {
                for (ServerNode s : slaves) {
                    cbTestNodeIp.getItems().add(s.getIp());
                }
            }
        });

        cbTestNodeIp.setOnAction(event -> {
            String ip = (String)cbTestNodeIp.getValue();
            if (ip == null) {
                return;
            }
            if (ip.equals(master.getIp())) {
                tfTestNodeUsername.setText(master.getUsername());
                pfTestNodePassword.setText(master.getPassword());
            } else {
                for (ServerNode s : slaves) {
                    if (ip.equals(s.getIp())) {
                        tfTestNodeUsername.setText(s.getUsername());
                        pfTestNodePassword.setText(s.getPassword());
                        break;
                    }
                }
            }
        });
    }

    public void onClickClearThreads() {
        lbThreads.setText("");
        threadNums.clear();
    }

    public void onClickStartTest() {
        if (master == null || slaves.size() == 0) {
            return;
        }
        ServerNode testNode = null;
        String ip = (String)cbTestNodeIp.getValue();
        if (ip == null) {
            MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"参数不完整！", ButtonType.OK);
            return;
        }
        if (ip.equals(master.getIp())) {
            testNode = master;
        } else {
            for (ServerNode s : slaves) {
                if (ip.equals(s.getIp())) {
                    testNode = s;
                    break;
                }
            }
        }
        if (testNode == null) {
            testNode = new ServerNode(ip, tfTestNodeUsername.getText(), pfTestNodePassword.getText(), "", "");
        }
        final ServerNode test = testNode;
        String pTesttype = (String)cbTestType.getValue();
        String pTablesize = (String)cbTableSize.getValue();
        String pTablenum = (String)cbTableNum.getValue();
        String pThreads = lbThreads.getText();
        String pTesttime = (String)cbTestTime.getValue();
        String pInterval = (String)cbReportInterval.getValue();
        if (pTesttype == null || pTablesize == null || pTablenum == null || pThreads == null || pThreads.isEmpty() || pTesttime == null || pInterval == null) {
            MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"参数不完整！", ButtonType.OK);
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AutotestTool.performanceTest(test, master, slaves, pTesttype, pTablesize, pTablenum, pThreads, pTesttime, pInterval, MainwindowController.this);
            }
        });
        t.start();
    }

    public void onClickAddThread() {
        String thread = (String)cbThreadNum.getValue();
        if (!thread.isEmpty()) {
            threadNums.add(Integer.parseInt(thread));
        }
        List<String> list = new ArrayList<>();
        for (Integer t : threadNums) {
            list.add(t.toString());
        }
        String s = String.join(",", list);
        lbThreads.setText(s);
    }

    public void onClickSBShowChart() {
        trSysbenchTestReport.setVisible(false);
        lcTPSOfThread.setVisible(true);
        lcTPSOfThread.getData().clear();
        lcTPSOfThread.getXAxis().setLabel("Thread");
        lcTPSOfThread.getYAxis().setLabel("TPS");
        String[] tpsSets = null;
        //curSysbenchReport = "C:\\Users\\HYIndex\\Downloads\\AutoTest-Mysql\\src\\main\\resources\\autotest\\testreport\\sysbench_point_select_20190505025513.log";
        if (curSysbenchReport == null) {
            return;
        }
        try {
            tpsSets = DataAnalysis.extractSysbenchReport(curSysbenchReport);
            if (tpsSets == null) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        XYChart.Series<String, Float> series = new XYChart.Series<String, Float>();
        //threadNums.add(2);
        //threadNums.add(4);
        //threadNums.add(8);
        //threadNums.add(16);
        //threadNums.add(32);
        //threadNums.add(64);
        //threadNums.add(128);
        //threadNums.add(256);

        ArrayList<String> xData = new ArrayList<>();
        for (Integer t : threadNums) {
            xData.add(t.toString());
        }
        series.setName("TPS");
        for (int i = 0; i < tpsSets.length && i < xData.size(); i++) {
            series.getData().add(new XYChart.Data<String, Float>(xData.get(i), Float.parseFloat(tpsSets[i])));
        }
        lcTPSOfThread.getData().add(series);
    }

    public void onClickSBShowReport() {
        lcTPSOfThread.setVisible(false);
        trSysbenchTestReport.setVisible(true);
        //curSysbenchReport = "C:\\Users\\HYIndex\\Downloads\\AutoTest-Mysql\\src\\main\\resources\\autotest\\testreport\\sysbench_point_select_20190505025513.log";
        if (curSysbenchReport != null) {
            AutotestTool.showSysbenchTestReport(curSysbenchReport, this);
        }
    }

    public void updataPerformanceStatusInfo(String info) {
        trPerformanceSatusInfo.appendText(info + "\n");
    }

    public void clearPerformanceStatusInfo() {
        trPerformanceSatusInfo.clear();
    }

    public void appendSysbenchTestReport(String s) {
        trSysbenchTestReport.appendText(s + "\n");
    }

    public void clearSysbenchTestReport() {
        trSysbenchTestReport.clear();
    }
    /*
     * 性能测试模块--end
     */


    /*
     * 主从延迟测试模块--begin
     */
    private void initMSDeplayTest() {
        ToggleGroup modGroup = new ToggleGroup();
        rbCheckMod.setToggleGroup(modGroup);
        rbMonitorMod.setToggleGroup(modGroup);
        rbCheckMod.setSelected(true); // 默认选中check模式
        tfMonitorInterval.setEditable(false);
        tabDelay.setOnSelectionChanged(event -> {
            if (slaves != null) {
                cbSlaveNode.getItems().clear();
                for (ServerNode s : slaves) {
                    cbSlaveNode.getItems().add(s);
                }
            }
        });

        rbMonitorMod.setOnAction(event -> {
            if (rbMonitorMod.isSelected()) {
                tfMonitorInterval.setEditable(true);
            }
        });

        rbCheckMod.setOnAction(event -> {
            if (rbCheckMod.isSelected()) {
                tfMonitorInterval.setEditable(false);
            }
        });

        //
        tvDelayInfo.setItems(delayInfos);
        tcDatetime.setCellValueFactory(cellData -> cellData.getValue().dateTimeProperty());
        tcRealtimeDelay.setCellValueFactory(cellData -> cellData.getValue().realtimeDelayProperty());
        tcAverageDelay.setCellValueFactory(cellData -> cellData.getValue().averageDelayProperty());

        tfMonitorInterval.setText("2");
        tfTestDatabase.setText("testdb");
    }

    public void onClickDelayTestInit() {
        String database = tfTestDatabase.getText();
        if (database == null || database.isEmpty()) {
            MessageBox.showMessage(Alert.AlertType.WARNING, "警告：", "请输入正确的数据库名！", ButtonType.OK);
            return;
        }
        if (master != null && slaves != null) {
            AutotestTool.delayTestInit(master, slaves, database, MainwindowController.this);
            MessageBox.showMessage(Alert.AlertType.INFORMATION, "提示：", "初始化完成！", ButtonType.OK);
        }

    }

    public void onClickCheckDelay() {

        if (master == null) {
            return;
        }
        ServerNode slaveNode = cbSlaveNode.getValue();
        if (slaveNode == null) {
            return;
        }
        String database = tfTestDatabase.getText();
        String interval = tfMonitorInterval.getText();
        if (rbCheckMod.isSelected()) {
            AutotestTool.delayTest(slaveNode, database, CHECK, interval, this);
        } else if (rbMonitorMod.isSelected()) {
            if (butCheckDelay.getText().equals("检测")) {
                butCheckDelay.setText("停止");
                monitorStop = false;
                new Thread() {
                    public void run() {
                        AutotestTool.delayTest(slaveNode, database, MONITOR, interval, MainwindowController.this);
                    }
                }.start();
            } else {
                butCheckDelay.setText("检测");
                monitorStop = true;
            }

        }
    }

    public void addDelayInfo(String dt, String rtDelay, String avgDelay) {
        delayInfos.add(0, new DelayInfo(dt, rtDelay, avgDelay));
    }

    public void clearDelayInfo() {
        delayInfos.clear();
    }
    /*
     * 主从延迟测试模块--end
     */


    /*
     * 主从一致测试--begin
     */
    public void initConsistentTest() {
        tvConsistentInfo.setItems(consistentInfos);
        tcErrors.setCellValueFactory(cellData -> cellData.getValue().errorsProperty());
        tcDiffs.setCellValueFactory(cellData -> cellData.getValue().diffsProperty());
        tcRows.setCellValueFactory(cellData -> cellData.getValue().rowsProperty());
        tcDiffRows.setCellValueFactory(cellData -> cellData.getValue().diffRowsProperty());
        tcChunks.setCellValueFactory(cellData -> cellData.getValue().chunksProperty());
        tcSkipped.setCellValueFactory(cellData -> cellData.getValue().skippedProperty());
        tcTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tcTableWithDb.setCellValueFactory(cellData -> cellData.getValue().tableProperty());

        tvSlaveDiffInfo.setItems(slaveDiffInfos);
        tcSlaveIp.setCellValueFactory(cellData -> cellData.getValue().slaveIpProperty());
        tcDatabase.setCellValueFactory(cellData -> cellData.getValue().databaseProperty());
        tcTable.setCellValueFactory(cellData -> cellData.getValue().tableProperty());
        tcTotalRows.setCellValueFactory(cellData -> cellData.getValue().totalRowsProperty());
        tcSlaveChunks.setCellValueFactory(cellData -> cellData.getValue().chunksProperty());

        tfConsistentTestdb.setText("testdb");
    }

    public void onClickConsistentTest() {
        String db = tfConsistentTestdb.getText();
        if (db == null || db.isEmpty()) {
            MessageBox.showMessage(Alert.AlertType.WARNING, "警告：", "请输入正确的数据库名！", ButtonType.OK);
            return;
        }
        if (master == null) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ConsistentInfo> res = null;
                res =  AutotestTool.consistentTest(master, db);
                if (res != null) {
                    consistentInfos.addAll(res);
                }
            }
        });
        t.start();
    }

    public void onClickSlaveDiffTest() {
        if (slaves == null) {
            return;
        }
        Thread[] threads = new Thread[slaves.size()];
        for (int i = 0; i < slaves.size(); i++) {
            ServerNode slave = slaves.get(i);
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<SlaveDiffInfo> sdi = AutotestTool.slaveDiffTest(slave);
                    if (sdi != null) {
                        addSlaveDiffInfo(sdi);
                    }
                }
            });
            threads[i].start();
        }
    }

    public void addSlaveDiffInfo(ArrayList<SlaveDiffInfo> sdi) {
        synchronized (this) {
            slaveDiffInfos.addAll(sdi);
        }
    }

    /*
     * 主从一致测试--end
     */


    /*
     * 业务场景测试--begin
     */
    private void initBusinessScenarioTest() {
        lcTpmCOfConnect.setVisible(false);

        cbWarehouses.getItems().addAll("10", "100", "1000");
        cbWarehouses.getSelectionModel().select(0);
        cbConnects.getItems().addAll("4", "8", "16", "32", "64", "128", "256", "384", "512");
        cbConnects.getSelectionModel().select(0);
        cbWarmupTime.getItems().addAll("60", "120", "180", "240", "300");
        cbWarmupTime.getSelectionModel().select(1);
        cbDuration.getItems().addAll("60", "180", "300", "600");
        cbDuration.getSelectionModel().select(1);
        cbBSReportInterval.getItems().addAll("5", "10", "20");
        cbBSReportInterval.getSelectionModel().select(1);

        tabBusinessScenario.setOnSelectionChanged(event -> {
            cbBSTestNodeIp.getItems().clear();
            if (master != null) {
                cbBSTestNodeIp.getItems().add(master.getIp());
            }
            if (slaves != null) {
                for (ServerNode s : slaves) {
                    cbBSTestNodeIp.getItems().add(s.getIp());
                }
            }
        });

        cbBSTestNodeIp.setOnAction(event -> {
            String ip = (String)cbBSTestNodeIp.getValue();
            if (ip.equals(master.getIp())) {
                tfBSTestNodeUsername.setText(master.getUsername());
                pfBSTestNodePassword.setText(master.getPassword());
            } else {
                for (ServerNode s : slaves) {
                    if (ip.equals(s.getIp())) {
                        tfBSTestNodeUsername.setText(s.getUsername());
                        pfBSTestNodePassword.setText(s.getPassword());
                        break;
                    }
                }
            }
        });
    }

    public void onClickStartBSTest() {
        if (master == null || slaves.size() == 0) {
            return;
        }
        ServerNode testNode = null;
        String ip = (String)cbBSTestNodeIp.getValue();
        if (ip == null) {
            MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"参数不完整！", ButtonType.OK);
            return;
        }
        if (ip.equals(master.getIp())) {
            testNode = master;
        } else {
            for (ServerNode s : slaves) {
                if (ip.equals(s.getIp())) {
                    testNode = s;
                    break;
                }
            }
        }
        if (testNode == null) {
            testNode = new ServerNode(ip, tfBSTestNodeUsername.getText(), pfBSTestNodePassword.getText(), "", "");
        }
        final ServerNode test = testNode;
        String pWarehouses = (String)cbWarehouses.getValue();
        String pConnects = lbConnects.getText();
        String pWarmup = (String)cbWarmupTime.getValue();
        String pDuration = (String)cbDuration.getValue();
        String pInterval = (String)cbBSReportInterval.getValue();
        if (pWarehouses == null || pConnects == null || pConnects.isEmpty() || pWarmup == null || pDuration == null || pInterval == null) {
            MessageBox.showMessage(Alert.AlertType.WARNING, (String)"警告", (String)"参数不完整！", ButtonType.OK);
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AutotestTool.businessScenarioTest(test, master, pWarehouses, pConnects, pWarmup, pDuration, pInterval, MainwindowController.this);
            }
        });
        t.start();
    }

    public void onClickBSShowChart() {
        trBSTestReport.setVisible(false);
        lcTpmCOfConnect.setVisible(true);
        lcTpmCOfConnect.getData().clear();
        lcTpmCOfConnect.getXAxis().setLabel("Connect");
        lcTpmCOfConnect.getYAxis().setLabel("TpmC");
        String[] tpmCs = null;
        if (curTpccReport == null) {
            return;
        }
        try {
            tpmCs = DataAnalysis.extractTpccReport(curTpccReport);
            if (tpmCs == null) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        XYChart.Series<String, Float> series = new XYChart.Series<String, Float>();
        ArrayList<String> xData = new ArrayList<>();
        for (Integer c : connectNums) {
            xData.add(c.toString());
        }
        series.setName("TpmC");
        for (int i = 0; i < tpmCs.length && i < xData.size(); i++) {
            series.getData().add(new XYChart.Data<String, Float>(xData.get(i), Float.parseFloat(tpmCs[i])));
        }
        lcTpmCOfConnect.getData().add(series);
    }

    public void onClickBSShowReport() {
        lcTpmCOfConnect.setVisible(false);
        trBSTestReport.setVisible(true);
        if (curTpccReport != null) {
            AutotestTool.showBSTestReport(curTpccReport, this);
        }
    }

    public void onClickAddConnects() {
        String connect = (String)cbConnects.getValue();
        if (!connect.isEmpty()) {
            connectNums.add(Integer.parseInt(connect));
        }
        List<String> list = new ArrayList<>();
        for (Integer c : connectNums) {
            list.add(c.toString());
        }
        String s = String.join(",", list);
        lbConnects.setText(s);
    }

    public void onClickClearConnects() {
        lbConnects.setText("");
        connectNums.clear();
    }

    public void updataBusinessScenarioStatusInfo(String info) {
        trBusinessScenarioStatus.appendText(info + "\n");
    }

    public void clearBusinessScenarioStatusInfo() {
        trBusinessScenarioStatus.clear();
    }

    public void appendBSTestReport(String s) {
        trBSTestReport.appendText(s + "\n");
    }

    public void clearBSTestReport() {
        trBSTestReport.clear();
    }
    /*
     * 业务场景测试--end
     */
    // /bin/bash /var/autotest/business_scenario_test.sh 192.168.170.129 root mysql 10 8 60 60 10 tpcc_test.log


    /*
     * 系统瓶颈测试--begin
     */
    private void initSystemBottleneck() {
        tvSystemInfo.setItems(systemInfos);
        tcSBNodeIp.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        tcCpuUtil.setCellValueFactory(cellData -> cellData.getValue().cpuUtilProperty());
        tcMemUtil.setCellValueFactory(cellData -> cellData.getValue().memUtilProperty());
        tcDiskUtil.setCellValueFactory(cellData -> cellData.getValue().diskUtilProperty());
        tcNetUtil.setCellValueFactory(cellData -> cellData.getValue().netUtilProperty());

        tabSystemBottleneck.setOnSelectionChanged(event -> {
            SystemInfo tmp = new SystemInfo();
            systemInfos.clear();
            for (int i = 0; i <= slaves.size(); i++) {
                systemInfos.add(tmp);
            }
        });

    }

    public void onClickSysBotStart() {
        if (master == null) {
            return;
        }
        if (butSysBotStart.getText().equals("启动")) {
            monitorStop = false;
            butSysBotStart.setText("终止");
            AutotestTool.systemResourceMonitor(master, slaves, this);
        } else {
            monitorStop = true;
            butSysBotStart.setText("启动");
        }
    }

    public void updateSystemInfo(int index, SystemInfo systemInfo) {
        synchronized (this) {
            systemInfos.set(index, systemInfo);
        }
    }

    /*
     * 系统瓶颈测试--end
     */
}
