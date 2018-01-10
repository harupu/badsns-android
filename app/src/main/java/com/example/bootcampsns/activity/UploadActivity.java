package com.example.bootcampsns.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.bootcampsns.R;
import com.example.bootcampsns.util.AsyncHttpRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadActivity extends BaseActivity {

    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        /** アップロードボタン押下時のアクションの設定 */
        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button uploadButton = (Button) findViewById(R.id.upload_button);
                uploadButton.setText("アップロードしています…");
                uploadButton.setEnabled(false);
                Button cancelButton = (Button) findViewById(R.id.upload_cancel_button);
                cancelButton.setEnabled(false);

                // 画像の読み込みで固まるのでスレッドを立てる
                final Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        AsyncHttpRequest request = new AsyncHttpRequest(UploadActivity.this, 0);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        request.addImage("image", outputStream.toByteArray());
                        request.addParam("feed_type", "image");
                        request.execute("/feeds");
                    }
                };
                handler.post(r);
            }
        });

        /** キャンセルボタン押下時のアクションの設定 */
        Button cancelButton = (Button) findViewById(R.id.upload_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadActivity.this.finish();
            }
        });

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK && resultData != null) {
            try {
                Uri uri = resultData.getData();
                ParcelFileDescriptor parcel = getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor file = parcel.getFileDescriptor();
                bitmap = BitmapFactory.decodeFileDescriptor(file);
                ImageView imageView = (ImageView) findViewById(R.id.upload_image_view);
                imageView.setImageBitmap(bitmap);
                parcel.close();
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
        //失敗しても成功しても閉じる
        this.finish();
    }
}
