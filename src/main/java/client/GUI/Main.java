package client.GUI;

import client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.ControllerContext;

import java.io.IOException;

public class Main extends Application {
    private static final String AUTH_WINDOW = "/AuthWindow.fxml";
    private static final String MAIN_WINDOW = "/MainWindow.fxml";

    private boolean firstStart = true;

    private Client client;
    private Stage primaryStage;
    private Stage authStage;

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

    public void createMainWindow() {
        if (firstStart) {
            try {
                primaryStage.close();
                Parent root = FXMLLoader.load(getClass().getResource(MAIN_WINDOW));
                primaryStage.setTitle("MyCloud");
                primaryStage.setScene(new Scene(root, 1280, 640));
                primaryStage.show();
                firstStart = false;
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }
    }

    public void userChanging() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(AUTH_WINDOW));
            Scene scene = new Scene(fxmlLoader.load(), 480, 240);
            authStage = new Stage();
            authStage.setTitle("Authentication");
            authStage.setScene(scene);
            authStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAuthStage() {
        authStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


