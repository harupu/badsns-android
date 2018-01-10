package com.example.bootcampsns.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by 01020410 on 2017/11/08.
 */

public class FeedInfo {

    private long id;

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    private int feedId;

    private int userId;

    /* ユーザ名 */
    private String userName;

    /* ユーザのアイコンデータ */
    private String iconData;

    /* ユーザアイコン */
    private Bitmap iconBitmap;

    /* テキストメッセージ */
    private String comment;

    /* 投稿画像データ */
    private Bitmap imageBitmap;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /* テキストメッセージ */
    private String date;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

/*    public String getIconData() {
        return iconData;
    }

    public void setIconData(String iconDataBase64) {
        this.iconData = iconDataBase64;
        byte[] iconByte = Base64.decode(iconDataBase64.getBytes(),Base64.DEFAULT);
        this.iconBitmap = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
    }*/

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
}
