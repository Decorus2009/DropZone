package dropzone.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class YandexDiskUserServiceImpl implements YandexDiskUserService {

    private final YandexDiskUserRepository userRepository;

    @Autowired
    public YandexDiskUserServiceImpl(YandexDiskUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<YandexDiskUser> listAll() {
        List<YandexDiskUser> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    @Override
    public YandexDiskUser saveOrUpdate(YandexDiskUser user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public YandexDiskUser findByUniqueKey(String uniqueKey) {
        Optional<YandexDiskUser> optionalUser = listAll().stream()
                .filter(user -> user.getUniqueKey().equals(uniqueKey))
                .findFirst();
        return optionalUser.orElse(null);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

    @Override
    public void delete(String uniqueKey) {
        listAll().stream()
                .filter(user -> user.getUniqueKey().equals(uniqueKey))
                .forEach(userRepository::delete);
    }
}
