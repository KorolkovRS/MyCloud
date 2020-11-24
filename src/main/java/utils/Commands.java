package utils;

public enum Commands {
    HOME_PATH_REQ("1"),
    FILE_STRUCT_REQ("2"),
    UP_REQ("3"),
    DEPTH_REQ("4"),
    DEL_REQ("5"),
    OK("6"),
    ERROR("7"),
    DOWNLOAD_REQ("8");

    private String  code;

    Commands(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
