package client.GUI;

import client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static final String AUTH_WINDOW = "/AuthWindow.fxml";
    private static final String MAIN_WINDOW = "/MainWindow.fxml";

    private Client client;
    private Stage primaryStage;


    @Override
    public void init() {
        client = Client.getInstance();
        client.setMain(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        createAuthWindow();
        try {
            client.connect();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Сервер недоступен\nПроверьте подключение или попробуйте войти позже");
            alert.showAndWait();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        try {
            client.disconnect();
        } catch (NullPointerException | IOException e) {
            System.out.println("Нет активных подключенний");
        }
    }

    public void createAuthWindow() {
        stop();
        try {
            client.connect();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Сервер недоступен\nПроверьте подключение или попробуйте войти позже");
            alert.showAndWait();
            Platform.exit();
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource(AUTH_WINDOW));
            primaryStage.setTitle("Authentication");
            primaryStage.setScene(new Scene(root, 480, 240));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }

    }
    public void createMainWindow(String username) {
        try {
            primaryStage.close();
            Parent root = FXMLLoader.load(getClass().getResource(MAIN_WINDOW));
            primaryStage.setTitle(String.format("User: %s", username));
            primaryStage.setScene(new Scene(root, 1280, 640));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


