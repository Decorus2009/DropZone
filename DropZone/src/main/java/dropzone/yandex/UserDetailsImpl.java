package dropzone.yandex;

public class UserDetailsImpl implements UserDetails {

    private final String login;
    private final String token;

    public UserDetailsImpl(String login, String token) {
        this.login = login;
        this.token = token;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getToken() {
        return token;
    }
}
