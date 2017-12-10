package dropzone.controller;

@FunctionalInterface
public interface ProgressUpdater {
    void updateProgress(long loaded, long total);
}
