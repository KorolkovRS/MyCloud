package client.GUI;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import utils.Commands;
import utils.ControllerContext;

import java.io.IOException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CloudPanelController extends BaseController {
    private ClientPanelController clientCtr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homePath = null;
        super.initialize(url, resourceBundle);
        ControllerContext.setCloudCtr(this);
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
                }
            }
            msg = client.fileDeleteRequest(deleteFile);
            if (msg.equals(Commands.OK.getCode())) {
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
            filesTable.getItems().addAll(client.fileStructRequest(pathField.getText()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnLoadFile(ActionEvent actionEvent) {
        if (clientCtr == null) {
            clientCtr = ControllerContext.getClientCtrInstance();
        }
        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path uploadFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
        if (!Files.isDirectory(uploadFile)) {
            if (client.downloadFile(uploadFile, clientCtr.getCurrentPath()).equals(Commands.OK.getCode())); {
                clientCtr.btnUpd(null);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Невозможно загрузить на диск директорию:( Эта возможность появится в ближайшее время", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public String getCurrentPath() {
        return pathField.getText();
    }
}
