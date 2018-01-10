package com.example.bootcampsns.util;

import android.app.Activity;

/**
 * Created by 01020410 on 2017/11/09.
 */

public class Utils {
    public static int dpToPixel(Activity activity, int pixel) {
        return (int)(pixel*activity.getResources().getDisplayMetrics().density);
    }
}
