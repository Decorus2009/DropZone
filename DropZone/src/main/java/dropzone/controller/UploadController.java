package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerException;
import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.storage.StorageService;
import dropzone.yandex.YandexDisk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;


@Controller
public class UploadController {

    private final StorageService storageService;
    private final UploadDirectoryService uploadDirectoryService;
    private final UserLoginService userLoginService;

    @Autowired
    public UploadController(UploadDirectoryService uploadDirectoryService, UserLoginService userLoginService,
                            StorageService storageService) {
        this.storageService = storageService;
        this.uploadDirectoryService = uploadDirectoryService;
        this.userLoginService = userLoginService;
    }

/*
    // для отладки
    // этот атрибут хорошо распознается в test.html через thymeleaf, можно с ним оперировать
    @GetMapping("/test/{name}")
    public String test(@PathVariable final String name, final Model model) {
        model.addAttribute("name", name);
        return "test";
    }
*/


    /**
     * GET method for /upload/{uniqueKey}
     *
     * @param uniqueKey generated short-url for a folder
     * @param model
     * @return view upload.html
     */
    @GetMapping("/upload/{uniqueKey}")
    public String singleFileUpload(@PathVariable final String uniqueKey, final Model model) {
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
    public String singleFileUpload(@PathVariable final String uniqueKey, MultipartHttpServletRequest request,
                                   final RedirectAttributes redirectAttributes) {

        final UploadDirectory yandexDiskUploadDirectory = uploadDirectoryService.findBy(uniqueKey);

        // Getting uploaded files from the request object
        for (MultipartFile file : request.getFileMap().values()) {
            UploadResult uploadResult = uploadTo(yandexDiskUploadDirectory, file);

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


    private UploadResult uploadTo(UploadDirectory yandexDiskUploadDirectory, MultipartFile file) {
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
        final String yandexDiskPath = yandexDiskUploadDirectory.getDirectory() + filename;

        final String login = userLogin.getLogin();
        final String token = userLogin.getToken();

        boolean uploadResult;
        try {
            uploadResult = new YandexDisk(login, token).upload(localFilePath, yandexDiskPath);
        } catch (ServerException | IOException e) {
            e.printStackTrace();
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
        return yandexDiskUploadDirectory.getByteLimit() >= file.getSize();
    }
}