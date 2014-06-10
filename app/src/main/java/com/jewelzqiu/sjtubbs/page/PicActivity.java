package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class PicActivity extends Activity {

    public static final String PHOTO_POSITION = "photo_pos";

    private static final String ISLOCKED_ARG = "isLocked";

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);
        mViewPager.setAdapter(new PicPagerAdapter(this, BBSApplication.imgUrlMap));
        mViewPager.setCurrentItem(getIntent().getIntExtra(PHOTO_POSITION, 0));

        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }
    }

}
