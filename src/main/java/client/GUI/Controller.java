package client.GUI;

import client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    @FXML
    VBox leftPanel;

    @FXML
    VBox rightPanel;

    private Client client;

    public void btnExitAction(ActionEvent actionEvent) {
        CloudPanelController cloudCtrl = (CloudPanelController) leftPanel.getProperties().get("ctrl");
        try {
            client.disconnect();
        } catch (Exception e) {
            System.out.println("Нет открытых соединений");
        } finally {
            Platform.exit();
        }
    }

    public void btnLogIn(ActionEvent actionEvent) {
        CloudPanelController cloudCtrl = (CloudPanelController) leftPanel.getProperties().get("ctrl");
        Client client = cloudCtrl.getClient();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/AuthWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 480, 240);
            Stage stage = new Stage();
            stage.setTitle("Аутентификация");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            cloudCtrl.getClient().connect();
            cloudCtrl.login();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось подключится к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }
}