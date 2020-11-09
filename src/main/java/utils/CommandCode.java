package utils;

/**
 * Команды для протокола между клиентом и сервером.
 * title - для работы со строками
 * code - если в будущем сделаю байтовы протокол
 */

public enum CommandCode {
    HELP("help", 0),
    LIST("ls", 1),
    CD("cd", 2),
    TOUCH("touch", 3),
    MKDIR("mkdir", 4),
    REMOVE("rm", 5),
    COPY("copy", 6),
    CAT("cat", 7),
    MSG("msg", 8);

    private String title;
    private int code;


    CommandCode(String title, int code) {
        this.title = title;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public int getCode() {
        return code;
    }

    //  ls - список файлов (сделано на уроке),
    //  cd (name) - перейти в папку
    //  touch (name) создать текстовый файл с именем
    //  mkdir (name) создать директорию
    //  rm (name) удалить файл по имени
    //  copy (src, target) скопировать файл из одного пути в другой
    //  cat (name) - вывести в консоль содержимое файла
}
