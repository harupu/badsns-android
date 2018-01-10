package com.example.bootcampsns.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by 01020410 on 2017/11/08.
 */

public class UserSessionInfo {

    static UserSessionInfo instance = new UserSessionInfo();;

    private UserSessionInfo() {

    }

    public static UserSessionInfo getInstance(){
        return instance;
    }

    /* token */
    private String token;

    /* ユーザの表示名 */
    private String userName;

    /* ユーザのアイコンパス(/users/me/iconでも可) */
    private String iconPath;

    /* ユーザのアイコンデータ */
    private String iconData;

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    private Bitmap iconBitmap;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconData() {
        return iconData;
    }

    public void setIconData(String iconDataBase64) {
        this.iconData = iconDataBase64;
        byte[] iconByte = Base64.decode(iconDataBase64.getBytes(),Base64.DEFAULT);
        this.iconBitmap = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void clear() {
        instance = new UserSessionInfo();
    }

}
