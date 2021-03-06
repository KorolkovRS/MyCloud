package utils;

import client.GUI.Controllers.ClientPanelController;
import client.GUI.Controllers.CloudPanelController;

public class ControllerContext {
    private static ClientPanelController clientCtrInstance;
    private static CloudPanelController cloudCtrInstance;

    public static void setClientCtr(ClientPanelController clientCtr) {
        if (clientCtrInstance == null) {
            clientCtrInstance = clientCtr;
        }
    }

    public static void setCloudCtr(CloudPanelController cloudCtr) {
        if (cloudCtrInstance == null) {
            cloudCtrInstance = cloudCtr;
        }
    }

    public static ClientPanelController getClientCtrInstance() {
        return clientCtrInstance;
    }

    public static CloudPanelController getCloudCtrInstance() {
        return cloudCtrInstance;
    }
}
