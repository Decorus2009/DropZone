package dropzone.repository.service;

import dropzone.repository.entity.RelationEntity;

import java.util.List;

public interface RelationEntityService<T extends RelationEntity> {
    List<T> listAll();

    T save(final T t);

    T update(final T t);

    void delete(final String key);

    void deleteAll();

    T findBy(final String key);
}
