package common;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class MessageBox {

    public static void showMessage(Alert.AlertType msgType, String title, String text, ButtonType... buttonTypes) {
        Alert alert = new Alert(msgType);
        alert.setTitle(title);
        alert.setContentText(text);
        if (buttonTypes.length > 0) {
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(buttonTypes);
        }
        alert.showAndWait();
    }

    public static boolean confirmBox(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional res = alert.showAndWait();
        if (res.get() == ButtonType.YES) {
            return true;
        } else {
            return false;
        }
    }
}
