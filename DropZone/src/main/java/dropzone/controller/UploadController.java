package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.http.HttpCodeException;
import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.storage.StorageException;
import dropzone.storage.StorageService;
import dropzone.util.FileUtils;
import dropzone.yandex.YandexDisk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;


@Controller
public class UploadController {

    private final StorageService storageService;
    private final UploadDirectoryService uploadDirectoryService;
    private final Map<String, Integer> uploadProgresses;
    private final Logger log;

    @Autowired
    public UploadController(UploadDirectoryService uploadDirectoryService, StorageService storageService) {
        this.storageService = storageService;
        this.uploadDirectoryService = uploadDirectoryService;
        uploadProgresses = new HashMap<>();
        log = Logger.getLogger("Upload log");
    }

    @PostMapping("/progress")
    @ResponseBody
    public int getUploadProgress(@RequestParam("hash") final String fileHash) {
        return uploadProgresses.getOrDefault(fileHash, 0);
    }

    @GetMapping("/upload/{uniqueKey}")
    public String fileUpload(@PathVariable final String uniqueKey, final Model model) {

        final UploadDirectory uploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        model.addAttribute("uniqueKey", uniqueKey);
        model.addAttribute("uniqueKeyFound", uploadDirectory != null);
        return "upload";
    }

    @PostMapping("/upload/{uniqueKey}")
    @ResponseBody
    public ResponseEntity<String> fileUpload(@PathVariable final String uniqueKey, final MultipartFile file,
                                             @RequestParam("hash") final String fileHash) {

        final UploadDirectory yandexDiskUploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        final UploadResult uploadResult = uploadTo(yandexDiskUploadDirectory, file, fileHash);
        return ResponseEntity.status(uploadResult.getHttpStatus()).body(uploadResult.getMessage());
    }

    private UploadResult uploadTo(final UploadDirectory yandexDiskUploadDirectory, final MultipartFile file, final String fileHash) {

        final UserLogin userLogin = yandexDiskUploadDirectory.getUserLogin();
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());

        final Path localFilePath = storageService.store(file);

        final String login = userLogin.getLogin();
        final String token = userLogin.getToken();

        boolean uploadSuccess;
        try {
            final YandexDisk yandexDisk = new YandexDisk(login, token);

            final long yandexDiskFreeSpace = yandexDisk.getFreeSpace();
            if (yandexDiskFreeSpace < yandexDiskUploadDirectory.getByteLimit()) {
                yandexDiskUploadDirectory.setByteLimit(yandexDiskFreeSpace);
            }
            // суммарный размер файлов в выбранной папке
            final long yandexDiskUploadDirectorySize = yandexDisk.getResourceSize(yandexDiskUploadDirectory.getDirectory());
            final long limit = yandexDiskUploadDirectory.getByteLimit();
            if (yandexDiskUploadDirectorySize > limit || yandexDiskUploadDirectorySize + file.getSize() > limit) {
                log.warning("Not enough space to upload file: " + filename);
                return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "Not enough space to upload file: " + filename);
            }

            final String yandexDiskPath = FileUtils.buildFilePath(yandexDiskUploadDirectory.getDirectory(), filename);

            uploadSuccess = yandexDisk.upload(localFilePath, yandexDiskPath,
                    (loaded, total) -> {
                        if (total > 0) {
                            uploadProgresses.put(fileHash, ((int) (loaded * 100 / total)) / 2);
                        }
                    });

        } catch (HttpCodeException e) {
            final String message = "File " + filename + " already exists";
            log.warning(message + System.getProperty("line.separator") + getStackTrace(e));
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, message);

        } catch (StorageException | ServerException | IOException e) {
            final String message = "Server error";
            log.warning(message + System.getProperty("line.separator") + getStackTrace(e));
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, message);

        } catch (RuntimeException e) {
            final String message = "Unknown server error";
            log.warning(message + System.getProperty("line.separator") + getStackTrace(e));
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, message);

        } finally {
            try {
                storageService.delete(localFilePath);
            } catch (StorageException e) {
                log.warning(e.getMessage() + System.getProperty("line.separator") + getStackTrace(e));
            }
        }

        return uploadSuccess
                ? new UploadResult(UploadStatus.SUCCESS, HttpStatus.OK, "File uploaded")
                : new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "Cannot upload file: " + file.getOriginalFilename());
    }
}