package client.GUI.Controllers;

import client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.Commands;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthController {
    @FXML
    TextField userField;

    @FXML
    PasswordField passwordField;

    private static final Pattern WRONG_SYMBOLS = Pattern.compile("[^\\w]");
    private Client client;

    public AuthController() {
        client = Client.getInstance();
        client.setAuthController(this);
    }

    public void btnLogin(ActionEvent actionEvent) {
        String username = userField.getText();
        String password = passwordField.getText();
        if (!checkSymbols(username, password)) {
            return;
        }
        try {
            client.loginRequest(username, password);
        } catch (Exception e) {
            connectErrorAction();
        }
    }

    public void btnCheckIn(ActionEvent actionEvent) {
        String username = userField.getText();
        String password = passwordField.getText();
        if (!checkSymbols(username, password)) {
            return;
        }
        try {
            client.checkIn(username, password);
        } catch (Exception e) {
            connectErrorAction();
        }
    }

    public void btnExit(ActionEvent actionEvent) {

    }

    public boolean checkSymbols(String username, String password) {
        Matcher matcher = WRONG_SYMBOLS.matcher(username);
        try {
            while (matcher.find()) {
                throw new IOException();
            }

            matcher = WRONG_SYMBOLS.matcher(password);
            while (matcher.find()) {
                throw new IOException();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Недопустимые знаки в поле логина или пароля");
            alert.setContentText("Используйте латинские буквы, цифры или знак _");
            alert.showAndWait();
            userField.clear();
            passwordField.clear();
            return false;
        }
        return true;
    }

    public void checkInErrorAction() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Пользователь с таким именем уже существует");
        alert.setContentText("Введите другое имя");
        alert.showAndWait();
        userField.clear();
        passwordField.clear();
    }

    public void loginErrorAction() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Неверный логин или пароль");
        alert.setContentText("Попробуйте заново");
        alert.showAndWait();
        userField.clear();
        passwordField.clear();
    }

    public void connectErrorAction() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка подключения");
        alert.setHeaderText("Сервер недоступен\nПроверьте подключение или попробуйте войти позже");
        alert.showAndWait();
    }

}

//    Client client = Client.getInstance();
//
//    public void btnLogin(ActionEvent actionEvent) {
//        String username = userField.getText();
//        String password = passwordField.getText();
//
//        Pattern pattern = Pattern.compile("[^\\w]");
//
//        Matcher matcher = pattern.matcher(username);
//        while (matcher.find()) {
//            wrongSymError("Login");
//            break;
//        }
//
//        matcher = pattern.matcher(password);
//        while (matcher.find()) {
//            wrongSymError("Password");
//            break;
//        }
//
//        try {
//            client.connect();
//            client.checkAccount(username, password);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Ошибка подключения");
//            alert.setHeaderText("Сервер недоступен\nПроверьте подключение или попробуйте войти позже");
//            alert.showAndWait();
//        }
//
//    }
//



//        try {
//            client.connect();
//
//            String username = userField.getText();
//            String password = passwordField.getText();
//
//            Pattern pattern = Pattern.compile("[^\\w]");
//            Matcher matcher = pattern.matcher(username);
//            while (matcher.find()) {
//                System.out.println(username.substring(matcher.start(), matcher.end()));
//            }
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Ошибка подключения");
//            alert.setHeaderText("Сервер недоступен\nПроверьте подключение или попробуйте войти позже");
//            alert.showAndWait();



//        try {
//            client.connect();
//            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
//            if (cloudPanelController == null) {
//                throw new IOException();
//            }
//            String str;
//            if (Commands.ERROR.getCode().equals(str = client.login(userField.getText(), passwordField.getText()))) {
//                Alert alert = new Alert(Alert.AlertType.WARNING, "Неверный логин или пароль", ButtonType.OK);
//                alert.showAndWait();
//            } else {
//                cloudPanelController.update();
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Добро пожаловать " + str, ButtonType.OK);
//                alert.showAndWait();
//            }
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "Сервер не доступен", ButtonType.OK);
//            alert.showAndWait();
//        } finally {
//            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//        }
//    }

//    public void btnCheckIn(ActionEvent actionEvent) {
//        try {
//            client.connect();
//            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
//            if (cloudPanelController == null) {
//                throw new IOException();
//            }
//            if(Commands.OK.getCode().equals(client.checkIn(userField.getText(), passwordField.getText()))) {
//                cloudPanelController.update();
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Добро пожаловать ", ButtonType.OK);
//                alert.showAndWait();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//        }
//    }
//
//    public void btnExit(ActionEvent actionEvent) {
//        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//    }
//}


//    public void btnLogin(ActionEvent actionEvent) {
//        try {
//            client.connect();
//            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
//            if (cloudPanelController == null) {
//                throw new IOException();
//            }
//            String str;
//            if (Commands.ERROR.getCode().equals(str = client.login(userField.getText(), passwordField.getText()))) {
//                Alert alert = new Alert(Alert.AlertType.WARNING, "Неверный логин или пароль", ButtonType.OK);
//                alert.showAndWait();
//            } else {
//                cloudPanelController.update();
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Добро пожаловать " + str, ButtonType.OK);
//                alert.showAndWait();
//            }
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "Сервер не доступен", ButtonType.OK);
//            alert.showAndWait();
//        } finally {
//            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//        }
//    }
//
//    public void btnCheckIn(ActionEvent actionEvent) {
//        try {
//            client.connect();
//            CloudPanelController cloudPanelController = ControllerContext.getCloudCtrInstance();
//            if (cloudPanelController == null) {
//                throw new IOException();
//            }
//            if(Commands.OK.getCode().equals(client.checkIn(userField.getText(), passwordField.getText()))) {
//                cloudPanelController.update();
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Добро пожаловать ", ButtonType.OK);
//                alert.showAndWait();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//        }
//    }
//
//    public void btnExit(ActionEvent actionEvent) {
//        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
//    }
