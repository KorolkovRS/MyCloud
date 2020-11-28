package client.GUI.Controllers;

import client.GUI.FileInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.apache.commons.io.FileUtils;
import utils.ControllerContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientPanelController extends BaseController {
    private CloudPanelController cloudCtr;

    @FXML
    ComboBox<String> disksBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homePath = Paths.get(".");
        super.initialize(url, resourceBundle);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }

        List<String> disks = disksBox.getItems();
        disksBox.getSelectionModel().select(disks.indexOf(homePath.toAbsolutePath().getRoot().toString()));
        update(homePath);
        ControllerContext.setClientCtr(this);
    }

    @Override
    public void update(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
            filesTable.getItems().addAll();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void openFolder(Path path) {
        if (Files.isDirectory(path)) {
            update(path);
        }
    }

        @Override
    public void btnPathUpAction(ActionEvent actionEvent) {
            System.out.println("client up");
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            update(upperPath);
        } else update(Paths.get(pathField.getText()));
    }

        @Override
    public void btnFileDelete(ActionEvent actionEvent) {
            FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path deleteFile = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());

        try {
            if (Files.isDirectory(deleteFile)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удаление каталога");
                alert.setHeaderText("Вы действительно хотите удалить каталог?");
                alert.setContentText("Нажмите OK для удаления и Cancel для отмены");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.get() == null || option.get() == ButtonType.CANCEL) {
                    return;
                } else if (option.get() == ButtonType.OK) {
                    FileUtils.forceDelete(new File(deleteFile.toString()));
                }
            } else {
                Files.delete(deleteFile);
            }
            update(Paths.get(pathField.getText()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось удалить файл", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void btnLoadFile(ActionEvent actionEvent) {
        FileInfo fileInfo = filesTable.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(pathField.getText()).resolve(fileInfo.getFilename());
        if (!Files.isDirectory(filePath)) {
            client.uploadFileToServer(filePath);
        }
    }

    @Override
    public String getCurrentPath() {
        return pathField.getText();
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        update(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void btnUpd(ActionEvent actionEvent) {
        update(Paths.get(pathField.getText()));
    }
}
