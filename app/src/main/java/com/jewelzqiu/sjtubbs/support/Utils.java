package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class Utils {

    public static final String TYPE_TOP_TEN = "table.Bg_Color_Midium table:contains(十大热门话题)";

    public static final String BBS_BASE_URL = "https://bbs.sjtu.edu.cn";

    public static String PIC_STORE_PATH;

    public static String PIC_CACHE_PATH;

    public static String CURRENT_BOARD;

    public static void setInsets(Activity activity, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        view.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                config.getPixelInsetBottom());
    }

    public static boolean isMarkReadEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences == null) {
            return true;
        }
        return preferences.getBoolean(context.getString(R.string.key_mark_read), true);
    }

}
