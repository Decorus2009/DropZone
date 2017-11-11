package dropzone.controller;

public class UploadResult {

    private UploadStatus status;
    private String message;

    public UploadResult(UploadStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
