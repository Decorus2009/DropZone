package dropzone.repository.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_login")
public class UserLogin implements RelationEntity {

    @Id
    @Column(unique = true)
    private String login;
    @Column(unique = true, nullable = false)
    private String token;
    @OneToMany(mappedBy = "userLogin")
    private Set<UploadDirectory> uploadDirectories;


    public UserLogin() {
    }

    public UserLogin(String login, String token, Set<UploadDirectory> uploadDirectories) {
        this.login = login;
        this.token = token;
        this.uploadDirectories = uploadDirectories;
    }

    public UserLogin(String login, String token) {
        this(login, token,  new HashSet<>());
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public Set<UploadDirectory> getUploadDirectories() {
        return uploadDirectories;
    }

    public void setUploadDirectories(final Set<UploadDirectory> uploadDirectories) {
        this.uploadDirectories = uploadDirectories;
    }

    public void addUploadDirectory(final UploadDirectory uploadDirectory) {
        uploadDirectories.add(uploadDirectory);
    }
}
