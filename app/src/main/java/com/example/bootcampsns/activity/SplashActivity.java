package com.example.bootcampsns.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.bootcampsns.R;
import com.example.bootcampsns.util.AsyncHttpRequest;
import com.example.bootcampsns.util.UserSessionInfo;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new splashHandler(), 2000);

    }
    // splashHandlerクラス
    class splashHandler implements Runnable {
        public void run() {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
            String token = preference.getString("token", "");
            Intent intent = null;
            if(token.equals("")) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }else{
                UserSessionInfo userSession = UserSessionInfo.getInstance();
                userSession.setUserName(preference.getString("name", ""));
                userSession.setIconPath(preference.getString("icon_path", ""));
                userSession.setToken(preference.getString("token", ""));
                userSession.setIconData(preference.getString("icon_data", ""));

                intent = new Intent(SplashActivity.this, TimelineActivity.class);
            }
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }}
