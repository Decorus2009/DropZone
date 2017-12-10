package dropzone.controller;

import com.yandex.disk.rest.exceptions.ServerIOException;
import dropzone.service.ShareDirectoryService;
import dropzone.util.FileUtils;
import dropzone.yandex.YandexDisk;
import dropzone.yandex.YandexDiskPath;
import dropzone.yandex.service.YandexDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class YandexDiskController {

    @Autowired
    ShareDirectoryService shareDirectoryService;

    @Autowired
    YandexDiskService yandexDiskService;

    private static final String ATTRIBUTE_FILES = "files";
    private static final String ATTRIBUTE_YANDEX_DISK_PATH = "yandexDiskPath";
    private static final String ATTRIBUTE_BASE_PATH = "basePath";
    private static final String ATTRIBUTE_SERVER_SUB_PATHS = "subPaths";

    private static final String BASE_URL = "/disk";

    @GetMapping("/disk/**")
    public String disk(final HttpServletRequest request,
                            final Principal principal,
                            final Model model)
            throws IOException, ServerIOException {
        final YandexDisk yandexDisk = yandexDiskService.getDisk(principal.getName());
        final String yandexDiskPath = FileUtils.normalize(URLDecoder.decode(request.getRequestURI(), "UTF-8").substring("/disk".length()));
        final Map<YandexDiskPath, Boolean> files = yandexDisk.getFiles(yandexDiskPath).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        file -> shareDirectoryService.isShared(yandexDisk.getUserDetails(), file.getPath()),
                        (v1, v2) -> v2,
                        LinkedHashMap::new));

        model.addAttribute(ATTRIBUTE_FILES, files);
        model.addAttribute(ATTRIBUTE_YANDEX_DISK_PATH, YandexDiskPath.newInstance(yandexDiskPath, null, true));
        final String serverPath = BASE_URL + FileUtils.normalize(yandexDiskPath);
        model.addAttribute(ATTRIBUTE_BASE_PATH, BASE_URL);
        model.addAttribute(ATTRIBUTE_SERVER_SUB_PATHS, FileUtils.subPaths(serverPath));
        return "disk";
    }

    @PostMapping("/shareDirectory")
    @ResponseBody
    public String shareDir(final Principal principal,
                           final Model model,
                           @RequestParam(value="path", required=false, defaultValue = "/") final String path) {
        final YandexDisk yandexDisk = yandexDiskService.getDisk(principal.getName());
        return shareDirectoryService.shareDirectory(yandexDisk.getUserDetails(), path);
    }
}
