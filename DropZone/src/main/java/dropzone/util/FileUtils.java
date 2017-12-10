package dropzone.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtils {

    private FileUtils() {}

    public static String buildFilePath(final String dir, final String fileName) {
        return normalize(dir) + fileName;
    }

    public static String normalize(final String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String getParent(final String path) {
        final List<String> parts = Arrays.asList(path.split("/"));
        return parts.size() > 0 ? String.join("/", parts.subList(0, parts.size() - 1)) : path;
    }

    public static String getFileName(final String path) {
        final List<String> parts = Arrays.asList(path.split("/"));
        return parts.size() > 0 ? parts.get(parts.size() - 1) : path;
    }

    public static boolean equals(final String pathA, final String pathB) {
        return normalize(pathA).equals(normalize(pathB));
    }

    public static List<String> subPaths(final String path) {
        final List<String> subPaths = new ArrayList<>();
        final List<String> parts = Arrays.asList(path.split("/"));
        for (int i = 1; i < parts.size(); ++i) {
            subPaths.add(String.join("/", Collections.unmodifiableList(parts.subList(0, i + 1))));
        }
        return subPaths;
    }
}
