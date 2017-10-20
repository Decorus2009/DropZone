package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerIOException;
import dropzone.yandex.YandexDisk;
import dropzone.yandex.YandexDiskFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class YandexDiskController {

    private static String ATTRIBUTE_FILES = "files";
    private static String ATTRIBUTE_CURRENT_PATH = "currentPath";

    @GetMapping("/yandexDiskFileList")
    public String diskContent(final Model model,
                              @RequestParam(value="path", required=false, defaultValue = "/") final String path)
            throws IOException, ServerIOException {
        final YandexDisk yandexDisk = new YandexDisk();
        final List<YandexDiskFile> files = yandexDisk.getDiskContent(Paths.get(path).normalize().toString());
        model.addAttribute(ATTRIBUTE_FILES, files);
        model.addAttribute(ATTRIBUTE_CURRENT_PATH, path);
        return "yandexDiskFileList";
    }
}
