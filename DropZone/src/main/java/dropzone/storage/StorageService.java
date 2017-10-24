package dropzone.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    void init();

    Path store(final MultipartFile file);
/*
    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);
*/

    void deleteAll();
}