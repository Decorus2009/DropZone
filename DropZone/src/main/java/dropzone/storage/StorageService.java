package dropzone.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {

    void init();

    Path store(final MultipartFile file) throws StorageException;

    void delete(Path file) throws StorageException;

    void deleteAll() throws StorageException;
}