package dropzone.repository.entity;

import javax.persistence.*;

@Entity
@Table(name = "user_login")
public class UserLogin implements RelationEntity {

    @Id
    @Column(unique = true)
    private String login;
    @Column(unique = true, nullable = false)
    private String token;
    @OneToOne(mappedBy = "userLogin")
    private UploadDirectory uploadDirectory;


    public UserLogin() {
    }

    public UserLogin(String login, String token, UploadDirectory uploadDirectory) {
        this.login = login;
        this.token = token;
        this.uploadDirectory = uploadDirectory;
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

    public UploadDirectory getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(final UploadDirectory uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
}
