package dropzone.yandex;

public class YandexDiskPath {

    private final String path;
    private final boolean isDirectory;

    public static YandexDiskPath newInstance(final String path, boolean isDirectory) {
        return new YandexDiskPath(path, isDirectory);
    }

    private YandexDiskPath(final String path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 2).substring(path.lastIndexOf("/") + 1)
                : path.substring(path.lastIndexOf("/") + 1);
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public YandexDiskPath getParent() {
        return newInstance(path.substring(0, path.endsWith("/") && path.length() > 1
                        ? path.length() - 2
                        : path.lastIndexOf("/"))
                , true);
    }
}