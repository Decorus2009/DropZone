package dropzone.repository;

import javax.persistence.*;

@Entity
public class YandexDiskUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String login;
    private String token;
    @Column(unique = true)
    private String uniqueKey;
    private String directory;

    public YandexDiskUser() {
    }

    public YandexDiskUser(String login, String token, String uniqueKey, String directory) {
        this.login = login;
        this.token = token;
        this.uniqueKey = uniqueKey;
        this.directory = directory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "YandexDiskUser{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", token='" + token + '\'' +
                ", uniqueKey='" + uniqueKey + '\'' +
                ", directory='" + directory + '\'' +
                '}';
    }
}
