package com.example.bootcampsns.adapter;

import android.graphics.Bitmap;

/**
 * Created by 01020410 on 2017/11/08.
 */

public class FriendInfo {

    private long id;

    private int userId;

    /* ユーザ名 */
    private String userName;

    /* ユーザのアイコンデータ */
    private String iconData;

    /* ユーザアイコン */
    private Bitmap iconBitmap;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

}
