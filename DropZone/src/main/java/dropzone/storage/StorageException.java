package dropzone.storage;

public class StorageException extends RuntimeException {

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(final String message) {
        super(message);
    }

    public StorageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}