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