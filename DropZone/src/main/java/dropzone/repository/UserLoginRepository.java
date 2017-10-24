package dropzone.repository;

import dropzone.repository.entity.UserLogin;
import org.springframework.data.repository.CrudRepository;

public interface UserLoginRepository extends CrudRepository<UserLogin, String> {
}
