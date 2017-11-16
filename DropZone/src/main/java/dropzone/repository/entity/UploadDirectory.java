package dropzone.repository.entity;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Optional;

@Entity
@Table(name = "upload_directory")
public class UploadDirectory implements RelationEntity {

    private static long NO_SIZE_LIMIT = Long.MAX_VALUE;

    @Id
    @Column(unique = true, nullable = false)
    private String uniqueKey;
    @Column(unique = true, nullable = false)
    private String directory;
    private Long byteLimit;
    // FK
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(cascade= CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name = "login")
    private UserLogin userLogin = new UserLogin();


    public UploadDirectory() {
    }

    public UploadDirectory(String uniqueKey, String directory, UserLogin userLogin) {
        this.uniqueKey = uniqueKey;
        this.directory = directory;
        this.userLogin = userLogin;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(final String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    public Long getByteLimit() {
        return byteLimit;
    }

    public void setByteLimit(Long byteLimit) {
        this.byteLimit = byteLimit;
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(final UserLogin userLogin) {
        this.userLogin = userLogin;
    }
}
