package dropzone.yandex;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Link;
import com.yandex.disk.rest.json.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class YandexDisk {

    private static final Logger LOG = Logger.getLogger(YandexDisk.class.getSimpleName());

    private static final String DEBUG_LOGIN = "login";
    private static final String DEBUG_TOKEN = "token";

    private final RestClient client;

    public YandexDisk() {
        this(DEBUG_LOGIN, DEBUG_TOKEN);
    }

    public YandexDisk(final String login, final String token) {
        client = new RestClient(new Credentials(login, token));
    }

    public List<YandexDiskPath> getDiskContent(final String path) throws IOException, ServerIOException {
        final Resource resource = client.getResources(new ResourcesArgs.Builder().setPath(path).build());
        if (resource.getResourceList() == null) {
            return Collections.emptyList();
        }
        return resource.getResourceList()
                .getItems()
                .stream()
                .map(file -> YandexDiskPath.newInstance(file.getPath().getPath(), file.isDir()))
                .collect(Collectors.toList());
    }

    public boolean upload(final Path filePath, final String yandexDiskPath) throws ServerException, IOException {
        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            final Link uploadLink = client.getUploadLink(yandexDiskPath, false);
            client.uploadFile(uploadLink, false, filePath.toFile(), new ProgressListener() {
                @Override
                public void updateProgress(long loaded, long total) {
                    LOG.info(filePath.toString() + " upload progress: loaded " + loaded + ", total " + total);
                }

                @Override
                public boolean hasCancelled() {
                    return false;
                }
            });

            return true;
        } else {
            throw new IllegalArgumentException("Invalid file path");
        }
    }
}
