package client.GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuthController {
    @FXML
    TextField userField;

    @FXML
    PasswordField passwordField;

    public void btnSignin(ActionEvent actionEvent) {

    }

    public void btnLogin(ActionEvent actionEvent) {

    }

    public void btnExit(ActionEvent actionEvent) {
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
    }
}
