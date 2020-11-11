package UIClient;

import baseClient.NettyTransmitClient;
import utils.CommandCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Заглушка для работы из командной строки
 */

public class CMDClient {
    private NettyTransmitClient transmitClient;
    private String host;
    private int port;

    public CMDClient(String host, int port) {
        this.host = host;
        this.port = port;
        transmitClient = new NettyTransmitClient(host, port);
    }

    public void start() {
        transmitClient.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        while (true) {
            try {
                msg = reader.readLine();
                if (msg.equals("exit")) {
                    transmitClient.disconnect();
                    break;
                } else if (msg.equals(CommandCode.LIST.getTitle())) {
                    transmitClient.list();
                } else if (msg.startsWith(CommandCode.TOUCH.getTitle())) {
                    transmitClient.touch(msg.substring(CommandCode.TOUCH.getTitle().length()).trim());
                } else if (msg.startsWith(CommandCode.MKDIR.getTitle())) {
                    transmitClient.mkdir(msg.substring(CommandCode.MKDIR.getTitle().length()).trim());
                } else if (msg.startsWith(CommandCode.REMOVE.getTitle())) {
                    transmitClient.remove(msg.substring(CommandCode.REMOVE.getTitle().length()).trim());
                }
                else {
                    transmitClient.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }

    public static void main(String[] args) {
        new CMDClient("localhost", 8888).start();
    }
}
