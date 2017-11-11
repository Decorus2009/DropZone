package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerIOException;
import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.util.FileUtils;
import dropzone.util.UniqueKeyGenerator;
import dropzone.yandex.YandexDisk;
import dropzone.yandex.YandexDiskPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class YandexDiskController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UploadDirectoryService uploadDirectoryService;

    private static String ATTRIBUTE_FILES = "files";
    private static String ATTRIBUTE_CURRENT_PATH = "currentPath";
    private static String ATTRIBUTE_SHARED_URL = "url";

    @GetMapping("/yandexDiskFileList")
    public String diskContent(final Model model,
                              @RequestParam(value="path", required=false, defaultValue = "/") final String path)
            throws IOException, ServerIOException {
        UserLogin userLogin = userLoginService.findBy("DropZoneCSC");
        final YandexDisk yandexDisk = new YandexDisk(userLogin.getLogin(), userLogin.getToken());
        final List<YandexDiskPath> files = yandexDisk.getDiskContent(path);
        model.addAttribute(ATTRIBUTE_FILES, files);
        model.addAttribute(ATTRIBUTE_CURRENT_PATH, YandexDiskPath.newInstance(path, true));
        return "yandexDiskFileList";
    }

    @GetMapping("/yandexDiskFileList/shareDir")
    public String shareDir(final Model model,
                           @RequestParam(value="path", required=false, defaultValue = "/") final String path) {
        final String normalizedPath = FileUtils.normalize(path);
        UserLogin userLogin = userLoginService.findBy("DropZoneCSC");
        if (userLogin.getUploadDirectories().stream()
                .map(UploadDirectory::getDirectory)
                .filter(dir -> dir.equals(normalizedPath)).count() == 0) {
            UploadDirectory uploadDirectory = new UploadDirectory(
                    UniqueKeyGenerator.nextKey(),
                    FileUtils.normalize(path),
                    userLogin);
            userLogin.getUploadDirectories().add(uploadDirectory);

            uploadDirectoryService.save(uploadDirectory);
            userLoginService.save(userLogin);
        }
        model.addAttribute(ATTRIBUTE_SHARED_URL, userLogin.getUploadDirectories().stream()
                .filter(dir -> dir.getDirectory().equals(normalizedPath))
                .map(UploadDirectory::getUniqueKey)
                .findFirst()
                .get());
        return "sharedDirUrl";
    }
}
