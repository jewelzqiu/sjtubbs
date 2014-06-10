package com.jewelzqiu.sjtubbs.main;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.Section;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class BBSApplication extends Application {

    public static ArrayList<Post> topTenList = null;

    public static ArrayList<Section> sectionList = null;

    public static int screenWidth = -1;

    public static int screenHeight = -1;

    public static int contentWidth = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        getSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getSize();
    }

    private void getSize() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        contentWidth = (int) (BBSApplication.screenWidth
                - getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
    }
}
