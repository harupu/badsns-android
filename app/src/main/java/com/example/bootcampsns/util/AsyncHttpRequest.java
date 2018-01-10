package com.example.bootcampsns.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import com.example.bootcampsns.activity.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 01020410 on 2017/11/07.
 */

public class AsyncHttpRequest extends AsyncTask<String, Void, byte[]> {
    public enum RESPONSE_TYPE {JSON, IMAGE};
    public enum HTTP_METHOD {GET, POST};

    private BaseActivity activity;
    private HashMap<String, String> params = new HashMap<String, String>();
    private HashMap<String, byte[]> images = new HashMap<String, byte[]>();
    private int requestId;
    private RESPONSE_TYPE responseType;
    private HTTP_METHOD httpMethod;

    public AsyncHttpRequest(BaseActivity activity, int requestId) {
        this.activity = activity;
        this.requestId = requestId;
        this.responseType = RESPONSE_TYPE.JSON;
        this.httpMethod = HTTP_METHOD.POST;
    }
    public AsyncHttpRequest(BaseActivity activity, int requestId, HTTP_METHOD method) {
        this.activity = activity;
        this.requestId = requestId;
        this.responseType = RESPONSE_TYPE.JSON;
        this.httpMethod = method;
    }

    public AsyncHttpRequest(BaseActivity activity, int requestId, HTTP_METHOD method, RESPONSE_TYPE responseType) {
        this.activity = activity;
        this.requestId = requestId;
        this.httpMethod = method;
        this.responseType = responseType;
    }

    public void addParam(String name, String value) {
        params.put(name, value);
    }

    public void addImage(String name, byte[] value) {
        images.put(name, value);
    }

    @Override
    protected byte[] doInBackground(String... argvs) {
        String path = argvs[0];

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(activity);
        String server_host = preference.getString("server_host", "");
        int server_port = Integer.parseInt(preference.getString("server_port", "3000"));
        String proxy_host = preference.getString("proxy_host", "");
        int proxy_port = Integer.parseInt(preference.getString("proxy_port", "8080"));

        String token = UserSessionInfo.getInstance().getToken();

        String url = "http://" + server_host + ":" + server_port + path;
        OkHttpClient client = null;

        if (!proxy_host.equals("")) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_host, proxy_port));
            client = new OkHttpClient.Builder().proxy(proxy).build();
        } else {
            client = new OkHttpClient();
        }

        Request.Builder requestBuilder = new Request.Builder().url(url).header("X-Requested-With", "Fetch");
        if (token != null && !token.equals("")) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        Request request = null;
        if(httpMethod == HTTP_METHOD.GET) {
            request = requestBuilder.get().build();
        }else {
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder(String.valueOf(System.currentTimeMillis()));
            for (Iterator<String> ite = params.keySet().iterator(); ite.hasNext(); ) {
                String key = ite.next();
                bodyBuilder.addFormDataPart(key, params.get(key));
            }
            for (Iterator<String> ite = images.keySet().iterator(); ite.hasNext(); ) {
                String key = ite.next();
                bodyBuilder.addFormDataPart(key, "image.png",
                        RequestBody.create(MediaType.parse("image/png"), images.get(key)));
            }
            RequestBody formBody = bodyBuilder.build();
            request = requestBuilder.post(formBody).build();
        }
        try {
            Response response = client.newCall(request).execute();
            return response.body().bytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "".getBytes();
    }

    @Override
    protected void onPostExecute(byte[] result) {
        try {
            if (this.responseType == RESPONSE_TYPE.IMAGE) {
                JSONObject json = new JSONObject();
                json.put("data", new String(Base64.encode(result,Base64.DEFAULT)));
                activity.asyncHttpCallback(json, this.requestId);
            } else {
                JSONObject json = new JSONObject(new String(result, "UTF-8"));
                activity.asyncHttpCallback(json, this.requestId);
            }
        } catch (JSONException e) {
            Toast.makeText(activity, "通信に失敗しました", Toast.LENGTH_LONG).show();
            try {
                JSONObject json = new JSONObject();
                json.put("error", "CONNECTION_FAILED");
                activity.asyncHttpCallback(json, this.requestId);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
