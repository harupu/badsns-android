package com.example.bootcampsns.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bootcampsns.R;
import com.example.bootcampsns.util.AsyncHttpRequest;
import com.example.bootcampsns.util.UserSessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static android.util.Base64.DEFAULT;

public class RegisterActivity extends BaseActivity {

    private enum REQ_TYPE {
        ICON_UPLOAD(0), // アイコンの登録
        REGISTER(1),    // ユーザ情報登録
        ICON_GET(2)     // アイコンの取得
        ;
        private final int id;
        private REQ_TYPE(final int id){
            this.id = id;
        }
        public int getId(){
            return id;
        }
    };

    Bitmap bitmap = null;
    String icon_file_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView icon = (ImageView) findViewById(R.id.register_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });

        /** 新規ユーザ登録ボタン押下時のアクション設定 */
        final Button registerButton = (Button) findViewById(R.id.register_exec_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText idView = (EditText) findViewById(R.id.register_login_id);
                EditText nameView = (EditText) findViewById(R.id.register_nickname);
                EditText passView = (EditText) findViewById(R.id.register_pass);

                if(idView.getText().toString().isEmpty()
                        || nameView.getText().toString().isEmpty()
                        || passView.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "必要項目を入力してください", Toast.LENGTH_LONG).show();
                    return;
                }

                registerButton.setEnabled(false);
                if(bitmap != null) {
                    AsyncHttpRequest request = new AsyncHttpRequest(RegisterActivity.this,
                            REQ_TYPE.ICON_UPLOAD.getId());
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] bitmap_byte = outputStream.toByteArray();
                    String bitmap_base64 = null;
                    try {
                        bitmap_base64 = new String(Base64.encode(bitmap_byte , DEFAULT), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    UserSessionInfo.getInstance().setIconData(bitmap_base64);
                    SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putString("icon_data", bitmap_base64);
                    editor.apply();
                    request.addImage("image", bitmap_byte);
                    request.addParam("resize_max_pixel", "240");
                    request.execute("/icons");
                }else {
                    sendRegisterRequest();
                }

                Button button = (Button) findViewById(R.id.register_exec_button);
                button.setEnabled(false);
            }
        });

        /** キャンセルボタン押下時のアクション設定 */
        Button cancelButton = (Button) findViewById(R.id.register_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.this.finish();
            }
        });
    }

    /** 新規登録リクエストの送信 */
    private void sendRegisterRequest() {
        EditText idView = (EditText) findViewById(R.id.register_login_id);
        EditText nameView = (EditText) findViewById(R.id.register_nickname);
        EditText passView = (EditText) findViewById(R.id.register_pass);

        AsyncHttpRequest request = new AsyncHttpRequest(RegisterActivity.this,
                REQ_TYPE.REGISTER.getId());
        request.addParam("login_id", idView.getText().toString());
        request.addParam("name", nameView.getText().toString());
        request.addParam("pass", passView.getText().toString());
        if(icon_file_name != null) {
            request.addParam("icon_file_name", icon_file_name);
        }
        request.execute("/users");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK && resultData != null) {
            try {
                Uri uri = resultData.getData();
                ParcelFileDescriptor parcel = getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor file = parcel.getFileDescriptor();
                bitmap = BitmapFactory.decodeFileDescriptor(file);
                ImageView imageView = (ImageView) findViewById(R.id.register_icon);
                imageView.setImageBitmap(bitmap);
                parcel.close();
                icon_file_name = null;//UPLOADが必要な状態にする
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            this.finish();
        }
    }

    @Override
    public void asyncHttpCallback(JSONObject result, int requestId) {
        if(requestId == REQ_TYPE.ICON_UPLOAD.getId()) {
            // アイコンの登録
            if (result.has("file_name")) {
                try {
                    icon_file_name = result.getString("file_name");
                    sendRegisterRequest();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Button button = (Button) findViewById(R.id.register_exec_button);
            button.setEnabled(true);
            Toast.makeText(this, "画像のアップロードに失敗しました", Toast.LENGTH_LONG).show();
            return;
        }else if(requestId == REQ_TYPE.REGISTER.getId()){
            // ユーザ情報登録
            if (result.has("errors")) {
                Button button = (Button) findViewById(R.id.register_exec_button);
                button.setEnabled(true);
                Toast.makeText(this, "そのアカウント名は使えません", Toast.LENGTH_LONG).show();
                return;
            }else {
                saveUserInfo(result);
                if(bitmap != null) {
                    Intent intent = new Intent(RegisterActivity.this, TimelineActivity.class);
                    startActivity(intent);
                    this.finish();
                    return;
                }else {
                    AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.ICON_GET.getId(),
                            AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                    request.execute("/users/me/icon");
                }
            }
        }else {
            try {
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preference.edit();
                UserSessionInfo.getInstance().setIconData(result.getString("data"));
                editor.putString("icon_data", result.getString("data"));
                editor.apply();
                Intent intent = new Intent(RegisterActivity.this, TimelineActivity.class);
                startActivity(intent);
                this.finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
