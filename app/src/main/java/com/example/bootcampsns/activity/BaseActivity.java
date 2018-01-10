package com.example.bootcampsns.activity;

import android.app.Activity;

import org.json.JSONObject;

/**
 * Created by 01020410 on 2017/11/07.
 */

public abstract class BaseActivity extends Activity {
    public abstract void asyncHttpCallback(JSONObject result, int requestId);
}
