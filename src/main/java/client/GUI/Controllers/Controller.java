package client.GUI.Controllers;

import client.Client;
import client.GUI.CloudPanelController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    @FXML
    VBox leftPanel;

    @FXML
    VBox rightPanel;

    Client client;

    public Controller() {
        this.client = Client.getInstance();
    }

    public void btnExitAction(ActionEvent actionEvent) {
        try {
            client.disconnect();

        } catch (Exception e) {
            System.out.println("Нет открытых соединений");
        } finally {
            Platform.exit();
        }
    }

    public void btnLogIn(ActionEvent actionEvent) {
        client.userChanging();
    }
}