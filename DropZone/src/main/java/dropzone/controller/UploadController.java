package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerException;
import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.storage.StorageService;
import dropzone.util.FileUtils;
import dropzone.yandex.YandexDisk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


@Controller
public class UploadController {

    private final StorageService storageService;
    private final UploadDirectoryService uploadDirectoryService;
    private final UserLoginService userLoginService;

    // TODO если загружать файл заново, то в ответ сразу улетает значение шкалы прогресса 100
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
        return String.valueOf(uploadProgresses.get(fileHash));
    }

    /**
     * GET method for /upload/{uniqueKey}
     *
     * @param uniqueKey generated short-url for a folder
     * @param model
     * @return view upload.html
     */
    @GetMapping("/upload/{uniqueKey}")
    public String multipleFileUpload(@PathVariable final String uniqueKey, final Model model) {
        final UploadDirectory uploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        model.addAttribute("uniqueKey", uniqueKey);
        model.addAttribute("uniqueKeyFound", uploadDirectory != null);
        return "upload";
    }

    /**
     * POST method for /upload/{uniqueKey} (runs automatically via upload.html)
     *
     * @param uniqueKey          generated short-url for a folder
     * @param request            multipart request that contains all the files taken via the html form
     * @param redirectAttributes
     * @return redirects to the view uploadStatus.html
     */
    @PostMapping("/upload/{uniqueKey}")
    public String multipleFileUpload(@PathVariable final String uniqueKey, MultipartHttpServletRequest request,
                                     final RedirectAttributes redirectAttributes, @RequestParam("hash") String fileHash) {

        final UploadDirectory yandexDiskUploadDirectory = uploadDirectoryService.findBy(uniqueKey);

        // Getting uploaded files from the request object
        for (MultipartFile file : request.getFileMap().values()) {
            uploadProgresses.put(fileHash, 0);

            UploadResult uploadResult = uploadTo(yandexDiskUploadDirectory, file, fileHash);

            redirectAttributes
                    .addFlashAttribute("uploadSuccess", uploadResult.getStatus() == UploadStatus.SUCCESS)
                    .addFlashAttribute("uploadResultMessage", uploadResult.getMessage());

            if (uploadResult.getStatus() == UploadStatus.FAILURE) {
                break;
            }
        }
        return "redirect:/uploadStatus";
    }

    /**
     * GET method for /uploadStatus. Shows the result status of file(s) uploading
     *
     * @return view uploadStatus.html
     */
    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }


    private UploadResult uploadTo(UploadDirectory yandexDiskUploadDirectory, MultipartFile file, String fileHash) {
        if (!hasEnoughSpaceToUploadTo(yandexDiskUploadDirectory, file)) {
            return new UploadResult(UploadStatus.FAILURE, "Not enough space to upload file: " + file.getOriginalFilename() + "\n");
        }

        final UserLogin userLogin = yandexDiskUploadDirectory.getUserLogin();
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        /*
        Put file into temporary storage first,
        because RestClient.uploadFile accepts file only as a local source.
        */
        final Path localFilePath = storageService.store(file);
        final String yandexDiskPath = FileUtils.buildFilePath(yandexDiskUploadDirectory.getDirectory(), filename);

        final String login = userLogin.getLogin();
        final String token = userLogin.getToken();

        // TODO Пока проверять, что общее кол-во байт не больше лимита.
        // TODO Потом через api диска может получиться запросить размер папки и можно будет при каждой загрузке запрашивать текущий размер (параллельно могут что-то и удалять)

        boolean uploadResult;
        try {
            uploadResult = new YandexDisk(login, token).upload(localFilePath, yandexDiskPath, uploadProgresses, fileHash);
        } catch (ServerException | IOException e) {
            e.printStackTrace();
            // TODO проверить исключение без интернета  java.net.UnknownHostException
            // TODO handle error
            return new UploadResult(UploadStatus.FAILURE, e.getMessage());
        }

        if (uploadResult) {
            return new UploadResult(UploadStatus.SUCCESS, "File(s) uploaded\n");
        } else {
            return new UploadResult(UploadStatus.FAILURE, "Cannot upload file: " + file.getOriginalFilename() + "\n");
        }
    }

    private boolean hasEnoughSpaceToUploadTo(UploadDirectory yandexDiskUploadDirectory, MultipartFile file) {
        final Long byteLimit = yandexDiskUploadDirectory.getByteLimit();
        return byteLimit == null || byteLimit >= file.getSize();
    }
}