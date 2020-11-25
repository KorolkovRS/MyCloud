package client;

//import client.GUI.FileInfo;

import client.GUI.Controllers.ClientPanelController;
import client.GUI.Controllers.CloudPanelController;
import client.GUI.Controllers.AuthController;
import client.GUI.Main;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

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

    private Client() {
        buff = new byte[BUFF_SIZE];
    }

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
        if (authCard.isCheckReq()) {
            if ((token = authCard.getToken()) != null) {
                Platform.runLater(() -> main.createMainWindow(authCard.getUsername()));
            } else {
                Platform.runLater(() -> authController.checkInErrorAction());
            }
        } else {
            if ((token = authCard.getToken()) != null) {
                Platform.runLater(() -> main.createMainWindow(authCard.getUsername()));
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
        main.createAuthWindow();
    }

    /**
     * Отправка запроса на обновление файловой структуры облачного харнилища на клиенте
     *
     * @param path - путь к активной папке
     * @throws IOException
     */
    public void updateCloudPanel(Path path) throws IOException {
        String sPath = null;
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
        Path serverPath = cloudPanelController.getCurrentPath();
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

    public void downloadFileFromServer(Path serverPath) {
        clientPanelController = ControllerContext.getClientCtrInstance();
        //Path clientFilePath = clientPanelController.

    }

    public void fileDeleteRequest(String path) {
        try {
            out.writeObject(new DataPack(token, Commands.DEL_REQ, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//    public List<FileInfo> pathUpRequest(String path) throws IOException {
//        out.writeObject(new DataPack(token, username, password, Commands.UP_REQ.getCode(), path));
//        try {
//            Object incoming = in.readObject();
//            if (incoming instanceof List) {
//                List<FileInfo> fileList = (List<FileInfo>) incoming;
//                return fileList;
//            }
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public List<FileInfo> depthFileStructRequest(Path path) throws IOException {
//        out.writeObject(new DataPack(token, username, password, Commands.DEPTH_REQ.getCode(), path.toString()));
//        try {
//            Object incoming = in.readObject();
//            if (incoming instanceof List && incoming != null) {
//                List<FileInfo> fileList = (List<FileInfo>) incoming;
//                return fileList;
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public String fileDeleteRequest(Path path) throws IOException {
//        out.writeObject(new DataPack(token, username, password, Commands.DEL_REQ.getCode(), path.toString()));
//        Object msg;
//        try {
//            msg = in.readObject();
//            if (msg instanceof String) {
//                return (String) msg;
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public String downloadFile(Path path, String downloadDir) throws IOException {
//        String msg = Commands.ERROR.getCode();
//        File newFile = new File(downloadDir + "\\" + path.getFileName());
//        if (newFile.exists()) {
//            newFile.delete();
//        }
//        Object obj;
//        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
//            out.writeObject(new DataPack(token, username, password, Commands.DOWNLOAD_REQ.getCode(), path.toString()));
//            while (true) {
//                obj = in.readObject();
//                if (obj instanceof FileCard) {
//                    FileCard fileCard = (FileCard) obj;
//                    if (!newFile.exists()) {
//                        newFile.createNewFile();
//                    }
//                    if (fileCard.getFileName() != null) {
//                        currentFile = fileCard.getFileName();
//                    } else {
//                        throw new IOException();
//                    }
//                    byte[] bytes = fileCard.getData();
//                    if (bytes == null) {
//                        currentFile = "";
//                        msg = Commands.OK.getCode();
//                        break;
//                    } else {
//                        fos.write(bytes, 0, fileCard.getLength());
//                        fos.flush();
//                    }
//                }
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return msg;
//    }
//
//    public String uploadFile(Path path, String loadDir) {
//        File file = new File(path.toString());
//        int read;
//        String msg = Commands.ERROR.getCode();
//        String fileDir = loadDir + "\\" + path.getFileName().toString();
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            while ((read = fis.read(buff)) != -1) {
//                out.writeObject(new DataPack(token, username, password, new FileCard(fileDir, buff, read)));
//                out.flush();
//            }
//            out.writeObject(new DataPack(token, username, password, new FileCard(fileDir, null, 0)));
//            out.flush();
//            Object obj = in.readObject();
//            if (obj instanceof String) {
//                msg = (String) obj;
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return msg;
//    }
//}