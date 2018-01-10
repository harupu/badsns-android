package jp.example.mainecfluffy;

/**
 * Created by 01020410 on 2017/11/10.
 */

public class MaineCFluffyResult {
    private MaineCFluffyUser user;
    private String token;

    protected MaineCFluffyResult(MaineCFluffyUser user, String token) {
        this.user = user;
        this.token = token;
    }

    public MaineCFluffyUser getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
