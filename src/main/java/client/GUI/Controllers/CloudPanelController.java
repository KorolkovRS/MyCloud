package client.GUI.Controllers;

import client.GUI.Controllers.BaseController;
import client.GUI.FileInfo;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import utils.ControllerContext;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CloudPanelController extends BaseController {
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

    }

    @Override
    public Path getCurrentPath() {
        return null;
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
}

//    @Override
//    protected void login() {
//        update();
//    }
//
//    @Override
//    public void updateList(Path path, List<FileInfo> list) {
//        filesTable.getItems().clear();
//        filesTable.getItems().addAll(list);
//        filesTable.sort();
//    }
//
//    @Override
//    public void btnPathUpAction(ActionEvent actionEvent) {
//        try {
//            if (pathField.getText().isEmpty()) {
//                return;
//            }
//            List<FileInfo> list = client.pathUpRequest(pathField.getText());
//            Path parentPath = Paths.get(pathField.getText()).getParent();
//            updateList(parentPath, list);
//            if (parentPath != null) {
//                pathField.setText(parentPath.toString());
//            } else {
//                pathField.clear();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void openFolder(Path path) {
//        try {
//            List<FileInfo> list = client.depthFileStructRequest(path);
//            if (list != null) {
//                updateList(path, list);
//                pathField.setText(path.normalize().toString());
//            }
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
//            alert.showAndWait();
//        }
//    }
//
//    @Override
//    public void btnFileDelete(ActionEvent actionEvent) {
//        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
//        Path deleteFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
//        try {
//            String msg;
//            if (Files.isDirectory(deleteFile)) {
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удаление каталога");
//                alert.setHeaderText("Вы действительно хотите удалить каталог?");
//                alert.setContentText("Нажмите OK для удаления и Cancel для отмены");
//                Optional<ButtonType> option = alert.showAndWait();
//
//                if (option.get() == null || option.get() == ButtonType.CANCEL) {
//                    return;
//                }
//            }
//            msg = client.fileDeleteRequest(deleteFile);
//            if (msg.equals(Commands.OK.getCode())) {
//                update();
//            } else {
//                Alert alert = new Alert(Alert.AlertType.WARNING, "Ошибка при удалении файла", ButtonType.OK);
//                alert.showAndWait();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void update() {
//
//        try {
//            filesTable.getItems().clear();
//            List<FileInfo> list = client.fileStructRequest(pathField.getText());
//            if (list == null) {
//                throw new IOException();
//            }
//            filesTable.getItems().addAll(list);
//            filesTable.sort();
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
//            alert.showAndWait();
//        }
//    }
//
//    public void btnLoadFile(ActionEvent actionEvent) {
//        if (clientCtr == null) {
//            clientCtr = ControllerContext.getClientCtrInstance();
//        }
//        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
//        Path uploadFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
//
//        if (!Files.isDirectory(uploadFile)) {
//            try {
//                if (client.downloadFile(uploadFile, clientCtr.getCurrentPath()).equals(Commands.OK.getCode())) ;
//                {
//                    clientCtr.btnUpd(null);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "Невозможно загрузить на диск директорию:( Эта возможность появится в ближайшее время", ButtonType.OK);
//            alert.showAndWait();
//        }
//    }
//
//    public String getCurrentPath() {
//        return pathField.getText();
//    }
//}
