package client;

//import client.GUI.FileInfo;

import client.GUI.Controllers.ClientPanelController;
import client.GUI.Controllers.CloudPanelController;
import client.GUI.Controllers.AuthController;
import client.GUI.Main;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Client {
    private static final int BUFF_SIZE = 8192;
    private static Client instance;
    private Socket socket;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;
    private byte[] buff;
    private Main main;
    private AuthController authController;
    private CloudPanelController cloudPanelController;
    private ClientPanelController clientPanelController;
    private Integer token;
    private boolean firstStart = true;

    private Client() {
        buff = new byte[BUFF_SIZE];
    }
    private static Set<File> filesInProgress = new HashSet<>();

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    /**
     * Регистрация главного окна приложения
     *
     * @param main
     */
    public void setMain(Main main) {
        this.main = main;
        cloudPanelController = ControllerContext.getCloudCtrInstance();
    }

    /**
     * Регистрация контроллера окна аутентификации
     *
     * @param authController
     */
    public void setAuthController(AuthController authController) {
        this.authController = authController;
    }

    /**
     * Подключение к серверу и запуск потока чтения.
     * Сообщения от сервера перехватываются и в зависимости от типа пришедшего пакета:
     *
     * @throws IOException
     * @see AuthCard
     * @see DataPack
     * передаются в соответствующие обработчики:
     * {@link #incAuthMessageHandler(AuthCard authCard)}
     * {@link #incDataMessageHandler(DataPack dataPack)}
     */
    public void connect() throws IOException {
        socket = new Socket("localhost", 8889);
        out = new ObjectEncoderOutputStream(socket.getOutputStream());
        in = new ObjectDecoderInputStream(socket.getInputStream());
        System.out.println("Connect");

        Thread readThread = new Thread(() -> {
            Object incomingObject;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    incomingObject = in.readObject();
                    if (incomingObject instanceof AuthCard) {
                        incAuthMessageHandler((AuthCard) incomingObject);
                    } else if (incomingObject instanceof DataPack) {
                        incDataMessageHandler((DataPack) incomingObject);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Разрыв соединения");
                    Thread.currentThread().interrupt();
                }
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    /**
     * Отключение от сервера
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        in.close();
        out.close();
        System.out.println("Disconnect");
    }

    /**
     * Отправка запроса на вход в аккаунт
     *
     * @param username - имя пользователя
     * @param password - пароль
     * @throws IOException
     */
    public void loginRequest(String username, String password) throws IOException {
        out.writeObject(new AuthCard(false, username, password));
    }

    /**
     * Отправка запроса на регистрацию аккаунта
     *
     * @param username - имя пользователя
     * @param password - пароль
     * @throws IOException
     */
    public void checkIn(String username, String password) throws IOException {
        out.writeObject(new AuthCard(true, username, password));
    }

    /**
     * Обработчик входящих пакетов типа
     *
     * @param authCard
     * @see AuthCard
     */
    private void incAuthMessageHandler(AuthCard authCard) {
        if (authCard.getUsername() == null) {
            Platform.runLater(() -> authController.connectErrorAction());
            return;
        }
        if (authCard.isCheckReq()) {
            if ((token = authCard.getToken()) != null) {
                if (firstStart) {
                    Platform.runLater(() -> main.createMainWindow());
                    firstStart = false;
                } else {
                    try {
                        Platform.runLater(() -> main.closeAuthStage());
                        updateCloudPanel(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Platform.runLater(() -> authController.checkInErrorAction());
            }
        } else {
            if ((token = authCard.getToken()) != null) {
                if (firstStart) {
                    Platform.runLater(() -> main.createMainWindow());
                    firstStart = false;
                } else {
                    try {
                        Platform.runLater(() -> main.closeAuthStage());
                        updateCloudPanel(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Platform.runLater(() -> authController.loginErrorAction());
            }
        }
    }

    /**
     * Обработчик входящих пакетов типа
     *
     * @param dataPack
     * @see DataPack
     */
    private void incDataMessageHandler(DataPack dataPack) {
        Commands command = dataPack.getCommand();
        try {
            switch (command) {
                case FILE_STRUCT:
                    cloudPanelController.refreshCloudPanel(dataPack.getFolderStruct());
                    break;
                case OK:
                    cloudPanelController.update(null);
                    break;
                case UPLOAD_REQ:
                    saveDownloadFile(dataPack.getFileCard());
                    break;
                default:
                    throw new IOException();
            }
        } catch (IOException e) {
            try {
                System.out.println(dataPack.getStringData());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Сменить пользователя
     */
    public void userChanging() {
        main.userChanging();
    }

    /**
     * Отправка запроса на обновление файловой структуры облачного харнилища на клиенте
     *
     * @param path - путь к активной папке
     * @throws IOException
     */
    public void updateCloudPanel(Path path) throws IOException {
        String sPath = "";
        if (path != null) {
            sPath = path.toString();
        }
        cloudPanelController = ControllerContext.getCloudCtrInstance();
        out.writeObject(new DataPack(token, Commands.FILE_STRUCT_REQ, sPath));
    }

    /**
     * Отправка запроса на открытие папки, находящийся в облаке с клиента
     *
     * @param path
     * @throws IOException
     */
    public void openFolderRequest(Path path) throws IOException {
        out.writeObject(new DataPack(token, Commands.OPEN_FOLDER_REQ, path.toString()));
    }

    /**
     * Отправить файл, располагающийся в
     *
     * @param clientFilePath на сервер, в
     */
    public void uploadFileToServer(Path clientFilePath) {
        cloudPanelController = ControllerContext.getCloudCtrInstance();
        String serverPath = cloudPanelController.getCurrentPath();
        File file = clientFilePath.toFile();
        int read;
        String fileDir;
        if (serverPath != null) {
            fileDir = serverPath + "\\" + clientFilePath.getFileName().toString();
        } else {
            fileDir = clientFilePath.getFileName().toString();
        }

        System.out.println(fileDir);
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((read = fis.read(buff)) != -1) {
                out.writeObject(new DataPack(token, Commands.UPLOAD_REQ, new FileCard(fileDir, buff, read)));
                out.flush();
            }
            out.writeObject(new DataPack(token, Commands.UPLOAD_REQ, new FileCard(fileDir, null, 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFileFromServerRequest(String serverPath) {
        clientPanelController = ControllerContext.getClientCtrInstance();
        String clientFilePath = clientPanelController.getCurrentPath();
        try {
            out.writeObject(new DataPack(token, Commands.DOWNLOAD_REQ, new DownloadFileCard(serverPath, clientFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDownloadFile(FileCard inputFileCard) {
        if (inputFileCard == null) {
            return;
        }
        String inputFileName = inputFileCard.getFileName();
        File newFile = new File(inputFileName);

        if (!filesInProgress.contains(newFile) && newFile.exists()) {
            newFile.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
            filesInProgress.add(newFile);
            byte[] bytes = inputFileCard.getData();
            if (bytes == null) {
                filesInProgress.remove(newFile);
                clientPanelController.update(Paths.get(clientPanelController.getCurrentPath()));
            } else {
                fos.write(bytes, 0, inputFileCard.getLength());
                fos.flush();
            }
        } catch (IOException e) {
            System.out.println("Ошибка записи");
        }
    }

    public void fileDeleteRequest(String path) {
        try {
            out.writeObject(new DataPack(token, Commands.DEL_REQ, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}