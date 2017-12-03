package dropzone.yandex;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.http.HttpCodeException;
import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;
import com.yandex.disk.rest.json.Resource;
import com.yandex.disk.rest.json.ResourceList;
import dropzone.controller.ProgressUpdater;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class YandexDisk {

    private final RestClient client;
    private final UserDetails userDetails;

    public YandexDisk(final String login, final String token) {
        userDetails = new UserDetailsImpl(login, token);
        client = new RestClient(new Credentials(login, token));
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public List<YandexDiskPath> getFiles(final String path) throws IOException, ServerIOException {
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

    public boolean upload(final Path filePath, final String yandexDiskPath, ProgressUpdater progressUpdater)
            throws HttpCodeException, ServerException, IOException {

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            final Link uploadLink = client.getUploadLink(yandexDiskPath, false);
            client.uploadFile(uploadLink, false, filePath.toFile(), new ProgressListener() {
                @Override
                public void updateProgress(long loaded, long total) {
                    progressUpdater.updateProgress(loaded, total);
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

    public long getFreeSpace() throws IOException, ServerIOException {
        final DiskInfo diskInfo = getDiskInfo();
        return diskInfo.getTotalSpace() - diskInfo.getUsedSpace();
    }

    private DiskInfo getDiskInfo() throws IOException, ServerIOException {
        return client.getDiskInfo();
    }

    public long getResourceSize(final String path) throws IOException, ServerIOException {
        Resource resource = client.getResources(new ResourcesArgs.Builder().setPath(path).build());
        return getResourceSize(resource);
    }

    private long getResourceSize(final Resource resource)   {
        if (resource.isDir()) {
            ResourceList resourceList = resource.getResourceList();
            return resourceList == null
                    ? safeGetResourceSize(resource.getPath().getPath())
                    : resourceList.getItems().stream().map(this::getResourceSize).mapToLong(Long::longValue).sum();
        }
        return resource.getSize();
    }

    private long safeGetResourceSize(final String path) {
        try {
            return getResourceSize(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ServerIOException e) {
            throw new RuntimeException(e);
        }
    }
}