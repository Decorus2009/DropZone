package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.http.HttpCodeException;
import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.storage.StorageService;
import dropzone.util.FileUtils;
import dropzone.yandex.YandexDisk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


@Controller
public class UploadController {

    private final StorageService storageService;
    private final UploadDirectoryService uploadDirectoryService;
    private final UserLoginService userLoginService;

    // TODO если загружать файл заново, то в ответ сразу улетает значение шкалы прогресса 100

    // TODO InterruptedIOException в процессе загрузки отключение сервера

    private final Map<String, Integer> uploadProgresses;

    @Autowired
    public UploadController(UploadDirectoryService uploadDirectoryService, UserLoginService userLoginService,
                            StorageService storageService) {
        this.storageService = storageService;
        this.uploadDirectoryService = uploadDirectoryService;
        this.userLoginService = userLoginService;

        uploadProgresses = new HashMap<>();
    }

    @PostMapping("/progress")
    @ResponseBody
    public String getUploadProgress(@RequestParam("hash") String fileHash) {
        System.out.println("Progress of " + fileHash + ": " + uploadProgresses.get(fileHash));
        return String.valueOf(uploadProgresses.get(fileHash));
    }

    @GetMapping("/upload/{uniqueKey}")
    public String singleFileUpload(@PathVariable final String uniqueKey, final Model model) {
        final UploadDirectory uploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        model.addAttribute("uniqueKey", uniqueKey);
        model.addAttribute("uniqueKeyFound", uploadDirectory != null);
        return "upload";
    }

    @PostMapping("/upload/{uniqueKey}")
    @ResponseBody
    public ResponseEntity<String> singleFileUpload(@PathVariable final String uniqueKey, MultipartFile file,
                                                   final RedirectAttributes redirectAttributes, @RequestParam("hash") String fileHash) {
        System.out.println("UPLOAD START");

        uploadProgresses.put(fileHash, 0);
        final UploadDirectory yandexDiskUploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        UploadResult uploadResult = uploadTo(yandexDiskUploadDirectory, file, fileHash);

        System.out.println("UPLOAD END");
        return ResponseEntity.status(uploadResult.getHttpStatus()).body(uploadResult.getMessage());
    }

//    @GetMapping("/uploadStatus")
//    public String uploadStatus() {
//        return "uploadStatus";
//    }


    private UploadResult uploadTo(UploadDirectory yandexDiskUploadDirectory, MultipartFile file, String fileHash) {

//        if (!hasEnoughSpaceToUploadTo(yandexDiskUploadDirectory, file)) {
//            return new UploadResult(UploadStatus.FAILURE, "Not enough space to upload file: " + file.getOriginalFilename() + "\n");
//        }

        final UserLogin userLogin = yandexDiskUploadDirectory.getUserLogin();
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());

        // Put file into temporary storage first, because RestClient.uploadFile accepts file only as a local source.
        final Path localFilePath = storageService.store(file);
        final String yandexDiskPath = FileUtils.buildFilePath(yandexDiskUploadDirectory.getDirectory(), filename);

        final String login = userLogin.getLogin();
        final String token = userLogin.getToken();


        // TODO Пока проверять, что общее кол-во байт не больше лимита.
        // TODO Потом через api диска может получиться запросить размер папки и можно будет при каждой загрузке запрашивать текущий размер (параллельно могут что-то и удалять)

        boolean success;
        try {
            success = new YandexDisk(login, token).upload(localFilePath, yandexDiskPath, uploadProgresses, fileHash);
        } catch (HttpCodeException e) {
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "File " + filename + " already exists");
        } catch (ServerException | IOException e) {
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "Server error");
        } catch (RuntimeException e) {
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "Unknown server error");
        }

        if (success) {
            return new UploadResult(UploadStatus.SUCCESS, HttpStatus.OK, "File uploaded\n");
        } else {
            return new UploadResult(UploadStatus.FAILURE, HttpStatus.CONFLICT, "Cannot upload file: " + file.getOriginalFilename() + "\n");
        }
    }

    private boolean hasEnoughSpaceToUploadTo(UploadDirectory yandexDiskUploadDirectory, MultipartFile file) {
        final Long byteLimit = yandexDiskUploadDirectory.getByteLimit();
        return byteLimit == null || byteLimit >= file.getSize();
    }
}