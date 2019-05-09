package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DelayInfo {
    private StringProperty dateTime;
    private StringProperty realtimeDelay;
    private StringProperty averageDelay;

    public DelayInfo() {}

    public DelayInfo(String dateTime, String realtimeDelay, String averageDelay) {
        this.dateTime = new SimpleStringProperty(dateTime);
        this.realtimeDelay = new SimpleStringProperty(realtimeDelay);
        this.averageDelay = new SimpleStringProperty(averageDelay);
    }

    public String getDateTime() {
        return dateTime.get();
    }

    public String getRealtimeDelay() {
        return realtimeDelay.get();
    }

    public String getAverageDelay() {
        return averageDelay.get();
    }

    public StringProperty dateTimeProperty() {
        return dateTime;
    }

    public StringProperty realtimeDelayProperty() {
        return realtimeDelay;
    }

    public StringProperty averageDelayProperty() {
        return averageDelay;
    }

    public void setDateTime(String dateTime) {
        this.dateTime.set(dateTime);
    }

    public void setRealtimeDelay(String realtimeDelay) {
        this.realtimeDelay.set(realtimeDelay);
    }

    public void setAverageDelay(String averageDelay) {
        this.averageDelay.set(averageDelay);
    }
}
