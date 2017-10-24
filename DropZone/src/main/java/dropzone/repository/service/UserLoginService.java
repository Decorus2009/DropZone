package dropzone.repository.service;

import dropzone.repository.UserLoginRepository;
import dropzone.repository.entity.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserLoginService implements RelationEntityService<UserLogin> {

    private final UserLoginRepository repository;

    @Autowired
    public UserLoginService(UserLoginRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserLogin> listAll() {
        final List<UserLogin> entities = new ArrayList<>();
        repository.findAll().forEach(entities::add);
        return entities;
    }

    @Override
    public UserLogin save(final UserLogin userLogin) {
        repository.save(userLogin);
        return userLogin;
    }

    @Override
    public UserLogin update(final UserLogin userLogin) {
        save(userLogin);
        return userLogin;
    }

    @Override
    public void delete(final String login) {
        findByLogin(login).ifPresent(repository::delete);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public UserLogin findBy(final String login) {
        return findByLogin(login).orElse(null);
    }

    private Optional<UserLogin> findByLogin(final String login) {
        return listAll().stream().filter(ut -> ut.getLogin().equals(login)).findFirst();
    }
}
