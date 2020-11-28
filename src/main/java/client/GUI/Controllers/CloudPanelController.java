package client.GUI.Controllers;

import client.GUI.Controllers.BaseController;
import client.GUI.FileInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import utils.ControllerContext;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CloudPanelController extends BaseController {
    @FXML
    private TextField userFolder;
    private static final String DEFAULT_FOLDER = "";
    private String goToFolder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        goToFolder = DEFAULT_FOLDER;
        super.initialize(url, resourceBundle);
        ControllerContext.setCloudCtr(this);
        update(homePath);
    }

    @Override
    public void update(Path path) {
        try {
            if (path == null) {
                client.updateCloudPanel(Paths.get(pathField.getText()));
            } else {
                client.updateCloudPanel(path);
            }
            userFolder.setText("MyDisc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openFolder(Path path) {
        try {
            client.openFolderRequest(path);
            goToFolder = path.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void btnPathUpAction(ActionEvent actionEvent) {
            Path parentPath = Paths.get(pathField.getText()).getParent();
            try {
                if (parentPath == null) {
                    goToFolder = DEFAULT_FOLDER;
                    update(Paths.get(DEFAULT_FOLDER));
                } else {
                    client.openFolderRequest(parentPath);
                    goToFolder = parentPath.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void btnFileDelete(ActionEvent actionEvent) {
        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path deleteFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
        if (Files.isDirectory(deleteFile)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удаление каталога");
            alert.setHeaderText("Вы действительно хотите удалить каталог?");
            alert.setContentText("Нажмите OK для удаления и Cancel для отмены");
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() == null || option.get() == ButtonType.CANCEL) {
                return;
            }
        }
        client.fileDeleteRequest(deleteFile.toString());
    }

    @Override
    public void btnLoadFile(ActionEvent actionEvent) {
        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path downloadFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
        client.downloadFileFromServerRequest(downloadFile.toString());
    }

    @Override
    public String getCurrentPath() {
        return pathField.getText();
    }

    public void btnUpdatePanel(ActionEvent actionEvent) {
        update(null);
    }

    public void refreshCloudPanel(List<FileInfo> list) {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(list);
        filesTable.sort();
        filesTable.getItems().addAll();
        pathField.setText(goToFolder);
    }

    public void btnCreateFolder(ActionEvent actionEvent) {
        System.out.println("Create folder");
    }
}
