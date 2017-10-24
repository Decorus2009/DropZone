package dropzone.repository.service;

import dropzone.repository.UploadDirectoryRepository;
import dropzone.repository.entity.UploadDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UploadDirectoryService implements RelationEntityService<UploadDirectory> {

    private final UploadDirectoryRepository repository;

    @Autowired
    public UploadDirectoryService(UploadDirectoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UploadDirectory> listAll() {
        final List<UploadDirectory> entities = new ArrayList<>();
        repository.findAll().forEach(entities::add);
        return entities;
    }

    @Override
    public UploadDirectory save(final UploadDirectory uploadDirectory) {
        repository.save(uploadDirectory);
        return uploadDirectory;
    }

    @Override
    public UploadDirectory update(final UploadDirectory uploadDirectory) {
        return save(uploadDirectory);
    }

    @Override
    public void delete(final String uniqueKey) {
        findByUniqueKey(uniqueKey).ifPresent(repository::delete);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public UploadDirectory findBy(final String uniqueKey) {
        return findByUniqueKey(uniqueKey).orElse(null);
    }

    private Optional<UploadDirectory> findByUniqueKey(final String uniqueKey) {
        return listAll().stream().filter(ud -> ud.getUniqueKey().equals(uniqueKey)).findFirst();
    }
}
