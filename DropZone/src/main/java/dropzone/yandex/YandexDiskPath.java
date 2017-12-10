package dropzone.yandex;

import dropzone.util.FileUtils;

import java.util.Date;

public class YandexDiskPath {

    private final String path;
    private final Date dateModified;
    private final boolean isDirectory;

    public static YandexDiskPath newInstance(final String path, final Date dateModified, boolean isDirectory) {
        return new YandexDiskPath(path, dateModified, isDirectory);
    }

    private YandexDiskPath(final String path, final Date dateModified, boolean isDirectory) {
        this.path = path;
        this.dateModified = dateModified;
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

    public String getDateModified() {
        return dateModified != null ? dateModified.toString() : "";
    }

    public YandexDiskPath getParent() {
        return newInstance(FileUtils.getParent(path), null, true);
    }

    public boolean isRoot() {
        return path.equals("/") || path.equals("disk:/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YandexDiskPath that = (YandexDiskPath) o;

        if (isDirectory != that.isDirectory) return false;
        if (!path.equals(that.path)) return false;
        return dateModified != null ? dateModified.equals(that.dateModified) : that.dateModified == null;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + (dateModified != null ? dateModified.hashCode() : 0);
        result = 31 * result + (isDirectory ? 1 : 0);
        return result;
    }
}