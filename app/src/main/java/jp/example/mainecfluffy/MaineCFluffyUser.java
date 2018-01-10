package jp.example.mainecfluffy;

/**
 * Created by 01020410 on 2017/11/10.
 */

public class MaineCFluffyUser {
    private String nick_name = "";
    private String user_id = "";
    private String email = "";

    protected MaineCFluffyUser(String user_id, String nick_name, String email) {
        this.nick_name = nick_name;
        this.user_id = user_id;
        this.email = email;
    }

    public String getNick_name() {
        return nick_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getEmail() {
        return email;
    }

}
