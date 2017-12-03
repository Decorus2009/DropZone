package dropzone.storage;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService() {
        rootLocation = Paths.get("upload_tmp");
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public Path store(final MultipartFile file) throws StorageException {

        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty filesystem " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException("Cannot store filesystem with relative path outside current directory " + filename);
            }
            Files.copy(file.getInputStream(), rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return Paths.get(rootLocation.toString(), filename);
        } catch (IOException e) {
            throw new StorageException("Failed to store filesystem " + filename, e);
        }
    }

    @Override
    public void delete(Path file) throws StorageException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteAll() throws StorageException {
        try {
            FileUtils.cleanDirectory(rootLocation.toFile());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}