package client.GUI.Controllers;

import client.Client;
import client.GUI.FileInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {

    protected Path homePath;
    protected Client client;

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getInstance();
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        filesTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openFolder(Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFilename()));
                }
            }
        });
    }

    public abstract void update(Path path);

    public abstract void openFolder(Path path);

    public abstract void btnPathUpAction(ActionEvent actionEvent);

    public abstract void btnFileDelete(ActionEvent actionEvent);

    public abstract void btnLoadFile(ActionEvent actionEvent);



    //    @Override
//    public void btnPathUpAction(ActionEvent actionEvent) {
//        Path upperPath = Paths.get(pathField.getText()).getParent();
//        if (upperPath != null) {
//            updateList(upperPath, null);
//        } else updateList(Paths.get(pathField.getText()), null);
//    }

//
//
//
//    public Client getClient() {
//        return client;
//    }
//
//    protected abstract void login();
//
//    public abstract void updateList(Path path, List<FileInfo> list);
//
//    public abstract void btnPathUpAction(ActionEvent actionEvent);
//
//    public abstract void btnFileDelete(ActionEvent actionEvent);
}
