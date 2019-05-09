package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SlaveDiffInfo {
    private StringProperty slaveIp;
    private StringProperty database;
    private StringProperty table;
    private StringProperty totalRows;
    private StringProperty chunks;

    public SlaveDiffInfo() {

    }

    public SlaveDiffInfo(String slaveIp, String database, String table, String totalRows, String chunks) {
        this.slaveIp = new SimpleStringProperty(slaveIp);
        this.database = new SimpleStringProperty(database);
        this.table = new SimpleStringProperty(table);
        this.totalRows = new SimpleStringProperty(totalRows);
        this.chunks = new SimpleStringProperty(chunks);
    }

    public void setSlaveIp(String slaveIp) {
        this.slaveIp.set(slaveIp);
    }

    public void setDatabase(String database) {
        this.database.set(database);
    }

    public void setTable(String table) {
        this.table.set(table);
    }

    public void setTotalRows(String totalRows) {
        this.totalRows.set(totalRows);
    }

    public void setChunks(String chunks) {
        this.chunks.set(chunks);
    }

    public String getSlaveIp() {
        return slaveIp.get();
    }

    public String getDatabase() {
        return database.get();
    }

    public String getTable() {
        return table.get();
    }

    public String getTotalRows() {
        return totalRows.get();
    }

    public String getChunks() {
        return chunks.get();
    }

    public StringProperty slaveIpProperty() {
        return slaveIp;
    }

    public StringProperty databaseProperty() {
        return database;
    }

    public StringProperty tableProperty() {
        return table;
    }

    public StringProperty totalRowsProperty() {
        return totalRows;
    }

    public StringProperty chunksProperty() {
        return chunks;
    }
}
