package dropzone.util;

import java.util.logging.Logger;

public final class DropzoneLog {

    private static final Logger log = Logger.getLogger("Dropzone");

    private DropzoneLog() {}

    public static void info(final String msg) {
        log.info(msg);
    }
}
