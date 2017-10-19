package dropzone.repository;

import java.util.List;

public interface YandexDiskUserService {

    List<YandexDiskUser> listAll();

    YandexDiskUser saveOrUpdate(YandexDiskUser user);

    YandexDiskUser findByUniqueKey(String uniqueKey);

    void delete(Long id);

    void delete(String uniqueKey);
}
