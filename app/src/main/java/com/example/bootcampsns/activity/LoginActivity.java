package com.example.bootcampsns.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bootcampsns.R;
import com.example.bootcampsns.util.AsyncHttpRequest;
import com.example.bootcampsns.util.UserSessionInfo;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import jp.example.mainecfluffy.MaineCFluffyCallback;
import jp.example.mainecfluffy.MaineCFluffyManager;
import jp.example.mainecfluffy.MaineCFluffyResult;

public class LoginActivity extends BaseActivity {

    private enum REQ_TYPE {
        LOGIN(0),           // ログイン実行
        ICON(1),            // 自分のアイコン画像の取得
        FLUFFY_LOGIN(2),    // ID連携ログイン
        FLUFFY_REGIST(3)    // ID連携新規登録
        ;
        private final int id;
        private REQ_TYPE(final int id){
            this.id = id;
        }
        public int getId(){
            return id;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_login);

        /** ログインボタン押下時のアクション設定 */
        Button loginButton = (Button) findViewById(R.id.login_button);
        final LoginActivity activity = this;
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncHttpRequest request = new AsyncHttpRequest(activity, REQ_TYPE.LOGIN.getId());
                EditText idView = (EditText) findViewById(R.id.login_id);
                EditText passView = (EditText) findViewById(R.id.pass);

                request.addParam("login_id",idView.getText().toString());
                request.addParam("pass",passView.getText().toString());
                request.execute("/sessions");
            }
        });

        /** 新規ユーザ登録ボタン押下時のアクション設定 */
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        /** MaineCFluffyログインボタン押下時のアクション設定 */
        ImageButton maineButton = (ImageButton) findViewById(R.id.maine_button);
        maineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MaineCFluffyManager manager = MaineCFluffyManager.getInstance();
                manager.setCallback(new MaineCFluffyCallback(){
                    @Override
                    public void onSuccess(MaineCFluffyResult loginResult) {
                        EditText idView = (EditText) findViewById(R.id.login_id);
                        EditText passView = (EditText) findViewById(R.id.pass);
                        idView.setEnabled(false);
                        passView.setEnabled(false);

                        AsyncHttpRequest request = new AsyncHttpRequest(activity, REQ_TYPE.FLUFFY_LOGIN.getId());
                        request.addParam("login_id",loginResult.getUser().getUser_id());
                        // Android内包のHexとバージョンが合わないため
                        request.addParam("pass", new String(
                                Hex.encodeHex(
                                        DigestUtils.sha256(loginResult.getUser().getUser_id().getBytes()
                                        )
                                )
                        ));
                        request.addParam("login_type","fluffy");
                        request.execute("/sessions");

                    }
                });
                manager.login(LoginActivity.this);
            }
        });

        /** サーバ設定リンク押下時のアクション設定 */
        TextView serverSettingsLink = (TextView) findViewById(R.id.server_settings_link);
        serverSettingsLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ServerSettingsActivity.class);
                startActivity(intent);
            }
        });

        /** 何も設定がない時は強制的にサーバ設定へ遷移 */
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = preference.getString("server_host", null);
        String server_port = preference.getString("server_port", null);
        if(server_host == null || server_port == null) {
            Intent intent = new Intent(this, ServerSettingsActivity.class);
            startActivity(intent);
        }

    }


    @Override
    public void asyncHttpCallback(JSONObject result, int requestId) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preference.edit();
        try {
            if(requestId == REQ_TYPE.LOGIN.getId() && result.has("errors")) {
                // ログイン失敗
                Toast.makeText(this, "ログインに失敗しました", Toast.LENGTH_LONG).show();
            }else if(requestId == REQ_TYPE.LOGIN.getId() && !result.getString("name").equals("")) {
                // ログイン成功
                saveUserInfo(result);
                AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.ICON.getId(), AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                request.execute("/users/me/icon");
            }else if(requestId == REQ_TYPE.ICON.getId()) {
                // アイコン取得
                UserSessionInfo.getInstance().setIconData(result.getString("data"));
                editor.putString("icon_data", result.getString("data"));
                editor.apply();
                Intent intent = new Intent(this, TimelineActivity.class);
                startActivity(intent);
                this.finishAffinity();
            }else if(requestId == REQ_TYPE.FLUFFY_LOGIN.getId()){
                // ID連携
                if(result.has("errors")) {
                    // ログインに失敗したら新規登録
                    MaineCFluffyManager manager = MaineCFluffyManager.getInstance();
                    AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.FLUFFY_REGIST.getId());
                    request.addParam("login_id",manager.getUser().getUser_id());
                    request.addParam("name",manager.getUser().getUser_id());
                    // Android内包のHexとバージョンが合わないため
                    request.addParam("pass", new String(
                            Hex.encodeHex(
                                    DigestUtils.sha1(manager.getUser().getUser_id().getBytes()
                                    )
                            )
                    ));
                    request.execute("/users");
                }else {
                    // ログイン成功
                    saveUserInfo(result);
                    AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.ICON.getId(), AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                    request.execute("/users/me/icon");
                }
            }else if(requestId == REQ_TYPE.FLUFFY_REGIST.getId()){
                // ID連携登録
                // ここでも失敗したら、そのIDは通常ログインで使用済み
                if(result.has("errors")) {
                    EditText idView = (EditText) findViewById(R.id.login_id);
                    EditText passView = (EditText) findViewById(R.id.pass);
                    idView.setEnabled(true);
                    passView.setEnabled(true);
                    Toast.makeText(this, "そのアカウント名は使えません", Toast.LENGTH_LONG).show();
                    return;
                }
                saveUserInfo(result);
                AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.ICON.getId(), AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                request.execute("/users/me/icon");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveUserInfo(JSONObject result) {
        try {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preference.edit();

            UserSessionInfo userSession = UserSessionInfo.getInstance();
            userSession.setUserName(result.getString("name"));
            userSession.setIconPath(result.getString("icon"));
            userSession.setToken(result.getString("token"));

            editor.putString("name", result.getString("name"));
            editor.putString("icon_path", result.getString("icon"));
            editor.putString("token", result.getString("token"));
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

