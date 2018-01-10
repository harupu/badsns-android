package jp.example.mainecfluffy;

import android.app.Activity;
import android.content.Intent;

import java.util.HashMap;

/**
 * Created by 01020410 on 2017/11/10.
 */

public class MaineCFluffyManager {
    private static MaineCFluffyManager instance = new MaineCFluffyManager();

    private MaineCFluffyCallback callback = new MaineCFluffyCallback();

    private MaineCFluffyUser user = null;

    private String token = null;

    private MaineCFluffyManager() {
    }

    public static MaineCFluffyManager getInstance() {
        return instance;
    }

    protected void onLoginFinished(MaineCFluffyResult result){
        this.user = result.getUser();
        this.token = result.getToken();
        callback.onSuccess(result);
    }

    public void setCallback(MaineCFluffyCallback callback) {
        this.callback = callback;
    }

    public void login(Activity activity) {
        Intent intent = new Intent(activity, MaineCFluffyLoginActivity.class);
        activity.startActivity(intent);
    }

    public MaineCFluffyUser getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
