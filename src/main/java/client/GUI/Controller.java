package client.GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller {
    @FXML
    VBox leftPanel;

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnLogIn(ActionEvent actionEvent) {
        CloudPanelController cloudCtrl = (CloudPanelController) leftPanel.getProperties().get("ctrl");
        try {
            cloudCtrl.getClient().connect();
            cloudCtrl.login();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось подключится к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }
}