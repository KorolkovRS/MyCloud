package client.GUI;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.Commands;
import utils.ControllerContext;

import java.io.IOException;

public class AuthController {
    @FXML
    TextField userField;

    @FXML
    PasswordField passwordField;

    Client client = Client.getInstance();

    public void btnLogin(ActionEvent actionEvent) {
        try {
            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
            if (cloudPanelController == null) {
                throw new IOException();
            }
            String str;
            if (Commands.ERROR.getCode().equals(str = client.login(userField.getText(), passwordField.getText()))) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Неверный логин или пароль", ButtonType.OK);
                alert.showAndWait();
            } else {
                cloudPanelController.update();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Добро пожаловать " + str, ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Сервер не доступен", ButtonType.OK);
            alert.showAndWait();
        } finally {
            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
        }
    }

    public void btnCheckIn(ActionEvent actionEvent) {
        try {
            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
            if (cloudPanelController == null) {
                throw new IOException();
            }
            if(Commands.OK.getCode().equals(client.checkIn(userField.getText(), passwordField.getText()))) {
                cloudPanelController.update();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Добро пожаловать ", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
        }
    }

    public void btnExit(ActionEvent actionEvent) {
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
    }
}
