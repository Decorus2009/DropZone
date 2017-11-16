package dropzone.yandex;

import dropzone.util.FileUtils;

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
        return FileUtils.getFileName(path);
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public YandexDiskPath getParent() {
        return newInstance(FileUtils.getParent(path), true);
    }
}