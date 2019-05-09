package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConsistentInfo {
    private StringProperty errors;
    private StringProperty diffs;
    private StringProperty rows;
    private StringProperty diffRows;
    private StringProperty chunks;
    private StringProperty skipped;
    private StringProperty time;
    private StringProperty table;

    public ConsistentInfo() {

    }

    public ConsistentInfo(String errors, String diffs, String rows, String diffRows, String chunks, String skipped, String time, String table) {
        this.errors = new SimpleStringProperty(errors);
        this.diffs = new SimpleStringProperty(diffs);
        this.rows = new SimpleStringProperty(rows);
        this.diffRows = new SimpleStringProperty(diffRows);
        this.chunks = new SimpleStringProperty(chunks);
        this.skipped = new SimpleStringProperty(skipped);
        this.time = new SimpleStringProperty(time);
        this.table = new SimpleStringProperty(table);
    }

    public void setErrors(String errors) {
        this.errors.set(errors);
    }

    public void setDiffs(String diffs) {
        this.diffs.set(diffs);
    }

    public void setRows(String rows) {
        this.rows.set(rows);
    }

    public void setDiffRows(String diffRows) {
        this.diffRows.set(diffRows);
    }

    public void setChunks(String chunks) {
        this.chunks.set(chunks);
    }

    public void setSkipped(String skipped) {
        this.skipped.set(skipped);
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public void setTable(String table) {
        this.table.set(table);
    }

    public String getErrors() {
        return errors.get();
    }

    public String getDiffs() {
        return diffs.get();
    }

    public String getRows() {
        return rows.get();
    }

    public String getDiffRows() {
        return diffRows.get();
    }

    public String getChunks() {
        return chunks.get();
    }

    public String getSkipped() {
        return skipped.get();
    }

    public String getTime() {
        return time.get();
    }

    public String getTable() {
        return table.get();
    }

    public StringProperty errorsProperty() {
        return errors;
    }

    public StringProperty diffsProperty() {
        return diffs;
    }

    public StringProperty rowsProperty() {
        return rows;
    }

    public StringProperty diffRowsProperty() {
        return diffRows;
    }

    public StringProperty chunksProperty() {
        return chunks;
    }

    public StringProperty skippedProperty() {
        return skipped;
    }

    public StringProperty timeProperty() {
        return time;
    }

    public StringProperty tableProperty() {
        return table;
    }
}

