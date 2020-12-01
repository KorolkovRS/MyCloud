package com.korolkovrs;

import java.io.IOException;
import java.io.Serializable;

public class DownloadFileCard implements Serializable {
    private String serverPath;
    private String clientPath;

    public DownloadFileCard(String serverPath, String clientPath) {
        this.serverPath = serverPath;
        this.clientPath = clientPath;
    }

    public String getServerPath() throws IOException {
        if (serverPath == null) {
            throw new IOException();
        }
        return serverPath;
    }

    public String getClientPath() throws IOException {
        if (clientPath == null) {
            throw new IOException();
        }
        return clientPath;
    }
}
