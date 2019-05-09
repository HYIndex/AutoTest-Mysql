package common;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import static common.MessageBox.showMessage;

public class MessageBoxTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/autotest/fxml/mainwindow.fxml"));
        primaryStage.setTitle("AutoTestTool");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        showMessage(Alert.AlertType.INFORMATION, "Info:", "connect failed!", ButtonType.OK);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
