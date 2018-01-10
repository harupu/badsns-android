package com.example.bootcampsns.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 01020410 on 2017/11/09.
 */

public class FriendsIconManager {

    private HashMap<Integer, Bitmap> iconMap = new HashMap<Integer, Bitmap>();

    private static FriendsIconManager instance = new FriendsIconManager();;

    private FriendsIconManager() {
    }

    public static FriendsIconManager getInstance() {
        return instance;
    }

    public Bitmap getIcon(int userId) {
        return iconMap.get(userId);
    }

    public void addIcon(int userId, String iconDataBase64) {
        byte[] iconByte = Base64.decode(iconDataBase64.getBytes(),Base64.DEFAULT);
        iconMap.put(userId,BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length));
    }

}
