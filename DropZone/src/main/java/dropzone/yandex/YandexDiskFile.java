package dropzone.yandex;

import java.nio.file.Path;
import java.nio.file.Paths;

public class YandexDiskFile {

    private final Path path;
    private final boolean isDirectory;

    public static YandexDiskFile newInstance(final String path, boolean isDirectory) {
        return new YandexDiskFile(Paths.get(path), isDirectory);
    }

    private YandexDiskFile(final Path path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public YandexDiskFile getParent() {
        return newInstance(path.getParent().toString(), true);
    }
}