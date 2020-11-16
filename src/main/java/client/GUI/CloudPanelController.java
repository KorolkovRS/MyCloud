package client.GUI;

import client.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CloudPanelController extends BaseController {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homePath = null;
        super.initialize(url, resourceBundle);
    }

    @Override
    protected void login() {
        update();
    }

    @Override
    public void updateList(Path path, List<FileInfo> list) {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(list);
        filesTable.sort();
    }

    @Override
    public void btnPathUpAction(ActionEvent actionEvent) {
        try {
            if (pathField.getText().isEmpty()) {
                return;
            }
            List<FileInfo> list = client.pathUpRequest(pathField.getText());
            Path parentPath = Paths.get(pathField.getText()).getParent();
            updateList(parentPath, list);
            if (parentPath != null) {
                pathField.setText(parentPath.toString());
            } else {
                pathField.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void openFolder(Path path) {
        try {
            List<FileInfo> list = client.depthFileStructRequest(path);
            if (list != null) {
                updateList(path, list);
                pathField.setText(path.normalize().toString());
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void btnFileDelete(ActionEvent actionEvent) {
        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path deleteFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
        try {
            String msg;
            if (Files.isDirectory(deleteFile)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удаление каталога");
                alert.setHeaderText("Вы действительно хотите удалить каталог?");
                alert.setContentText("Нажмите OK для удаления и Cancel для отмены");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.get() == null || option.get() == ButtonType.CANCEL) {
                    return;
                } else if (option.get() == ButtonType.OK) {
                    msg = client.fileDeleteRequest(deleteFile);
                }
            }
            msg = client.fileDeleteRequest(deleteFile);
            if (msg.equals("ok")) {
                update();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Ошибка при удалении файла", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            filesTable.getItems().clear();
            filesTable.getItems().addAll(client.fileStructRequest());
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
