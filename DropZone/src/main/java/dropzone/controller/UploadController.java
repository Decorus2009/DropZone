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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @GetMapping("/upload/{uniqueKey}")
    public String singleFileUpload(@PathVariable final String uniqueKey, final Model model) {
        model.addAttribute("uniqueKey", uniqueKey);
        final UploadDirectory uploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        model.addAttribute("uniqueKeyFound", uploadDirectory != null);
        return "upload";
    }

    @PostMapping("/upload/{uniqueKey}")
    public String singleFileUpload(@PathVariable final String uniqueKey, @RequestParam("file") final MultipartFile file,
                                   final RedirectAttributes redirectAttributes) {
        final UploadDirectory yandexDiskUploadDirectory = uploadDirectoryService.findBy(uniqueKey);
        final UserLogin userLogin = yandexDiskUploadDirectory.getUserLogin();

        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        /*
        Put file into temporary storage first,
        because RestClient.uploadFile accepts file only as a local source.
        */
        final Path filePath = storageService.store(file);
        final String yandexDiskPath = FileUtils.buildFilePath(yandexDiskUploadDirectory.getDirectory(), filename);

        final String login = userLogin.getLogin();
        final String token = userLogin.getToken();
        boolean uploadResult = false;

        try {
            uploadResult = new YandexDisk(login, token).upload(filePath, yandexDiskPath);
        } catch (ServerException | IOException e) {
            e.printStackTrace();
        }

        redirectAttributes
                .addFlashAttribute("yandexDiskPath", yandexDiskUploadDirectory.getDirectory())
                .addFlashAttribute("fileName", filename)
                .addFlashAttribute("uploadResult", uploadResult);
        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }
}