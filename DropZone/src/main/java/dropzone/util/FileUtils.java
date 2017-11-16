package dropzone.util;

public class FileUtils {

    private FileUtils() {}

    public static String buildFilePath(final String dir, final String fileName) {
        return normalize(dir) + fileName;
    }

    public static String normalize(final String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String getParent(final String path) {
        return path.substring(0, path.endsWith("/") && path.length() > 1
                ? path.length() - 2
                : path.lastIndexOf("/"));
    }

    public static String getFileName(final String path) {
        return path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 2).substring(path.lastIndexOf("/") + 1)
                : path.substring(path.lastIndexOf("/") + 1);
    }

    public static boolean equals(final String pathA, final String pathB) {
        return normalize(pathA).equals(normalize(pathB));
    }
}
