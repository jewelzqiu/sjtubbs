package com.jewelzqiu.sjtubbs.main;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Board;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.Section;
import com.jewelzqiu.sjtubbs.support.Utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class BBSApplication extends Application {

    public static ArrayList<Post> topTenList = null;

    public static ArrayList<Section> sectionList = null;

    public static LinkedHashMap<String, Integer> imgUrlMap = new LinkedHashMap<String, Integer>();

    public static ArrayList<String> imgUrlList = new ArrayList<String>();

    public static HashMap<String, Board> boardMap = new HashMap<String, Board>();

    public static ArrayList<String> boardNameList = new ArrayList<String>();

    public static int screenWidth = -1;

    public static int screenHeight = -1;

    public static int contentWidth = -1;

    public static int gridViewPicWidth = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        getSize(getResources().getConfiguration().orientation);
        Utils.PIC_STORE_PATH = Environment.getExternalStoragePublicDirectory("Pictures")
                .getAbsolutePath() + "/sjtubbs";
        File file = new File(Utils.PIC_STORE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        File cacheDir = getExternalCacheDir();
        if (cacheDir != null) {
            Utils.PIC_CACHE_PATH = getExternalCacheDir().getAbsolutePath();
        }

        if (Utils.isAutoLoginEnabled(this)) {
            Utils.autoLogin(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getSize(newConfig.orientation);
    }

    private void getSize(int orientation) {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        contentWidth = (int) (screenWidth
                - getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
        gridViewPicWidth = (int) ((screenWidth - 3 * 5 * getResources()
                .getDisplayMetrics().density));
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridViewPicWidth /= 3;
        } else {
            gridViewPicWidth /= 2;
        }
    }
}
