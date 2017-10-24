package dropzone.repository;

import dropzone.repository.entity.UploadDirectory;
import org.springframework.data.repository.CrudRepository;

public interface UploadDirectoryRepository extends CrudRepository<UploadDirectory, String> {
}
