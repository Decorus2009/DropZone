package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerIOException;
import dropzone.service.ShareDirectoryService;
import dropzone.yandex.YandexDisk;
import dropzone.yandex.YandexDiskPath;
import dropzone.yandex.service.YandexDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class YandexDiskController {

    @Autowired
    ShareDirectoryService shareDirectoryService;

    @Autowired
    YandexDiskService yandexDiskService;

    private static String ATTRIBUTE_FILES = "files";
    private static String ATTRIBUTE_CURRENT_PATH = "currentPath";
    private static String ATTRIBUTE_SHARED_URL = "url";

    @GetMapping("/disk")
    public String listFiles(final Principal principal,
                            final Model model,
                            @RequestParam(value="path", required=false, defaultValue = "/") final String path)
            throws IOException, ServerIOException {
        final YandexDisk yandexDisk = yandexDiskService.getDisk(principal.getName());
        final List<YandexDiskPath> files = yandexDisk.getFiles(path);

        model.addAttribute(ATTRIBUTE_FILES, files);
        model.addAttribute(ATTRIBUTE_CURRENT_PATH, YandexDiskPath.newInstance(path, true));
        return "disk";
    }

    @GetMapping("/disk/share")
    public String shareDir(final Principal principal,
                           final Model model,
                           @RequestParam(value="path", required=false, defaultValue = "/") final String path) {
        final YandexDisk yandexDisk = yandexDiskService.getDisk(principal.getName());

        model.addAttribute(
                ATTRIBUTE_SHARED_URL,
                shareDirectoryService.shareDirectory(yandexDisk.getUserDetails(), path)
        );
        return "sharedUrl";
    }


}
