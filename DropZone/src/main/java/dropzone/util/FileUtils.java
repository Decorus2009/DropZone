package dropzone.util;

public class FileUtils {

    private FileUtils() {}

    public static String buildFilePath(final String dir, final String fileName) {
        return normalize(dir) + fileName;
    }

    public static String normalize(final String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}
