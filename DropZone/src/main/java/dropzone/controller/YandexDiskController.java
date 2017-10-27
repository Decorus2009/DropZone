package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerIOException;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UserLoginService;
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

    private static String ATTRIBUTE_FILES = "files";
    private static String ATTRIBUTE_CURRENT_PATH = "currentPath";

    @GetMapping("/yandexDiskFileList")
    public String diskContent(final Model model,
                              @RequestParam(value="path", required=false, defaultValue = "/") final String path)
            throws IOException, ServerIOException {
        UserLogin userLogin = userLoginService.findBy("login");
        final YandexDisk yandexDisk = new YandexDisk(userLogin.getLogin(), userLogin.getToken());
        final List<YandexDiskPath> files = yandexDisk.getDiskContent(path);
        model.addAttribute(ATTRIBUTE_FILES, files);
        model.addAttribute(ATTRIBUTE_CURRENT_PATH, YandexDiskPath.newInstance(path, true));
        return "yandexDiskFileList";
    }
}
