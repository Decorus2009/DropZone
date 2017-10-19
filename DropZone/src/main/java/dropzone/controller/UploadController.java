package dropzone.controller;

import dropzone.repository.YandexDiskUser;
import dropzone.repository.YandexDiskUserService;
import dropzone.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// TODO обработать ограничение на повторное добавление одного и того же uniqueKey
@Controller
public class UploadController {

    private final StorageService storageService;
    private final YandexDiskUserService userService;

    @Autowired
    public UploadController(StorageService storageService, YandexDiskUserService userService) {
        this.storageService = storageService;
        this.userService = userService;
    }

    @GetMapping("/upload/{uniqueKey}")
    public String singleFileUploadIndex(@PathVariable String uniqueKey, Model model) {
        model.addAttribute(uniqueKey);
        return "upload";
    }

    @PostMapping("/upload/{uniqueKey}")
    public String singleFileUpload(@PathVariable String uniqueKey, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        /* store file in the file system */
        storageService.store(file);
        /* update database */
        userService.saveOrUpdate(new YandexDiskUser("thisUser", "token", uniqueKey, "upload"));

        redirectAttributes
                .addFlashAttribute("message", "File \"" + file.getOriginalFilename() + "\" has been uploaded");
        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

    @GetMapping("/users")
    public String YandexDiskUsers(Model model) {
        model.addAttribute("users", userService.listAll());
        return "users";
    }
}