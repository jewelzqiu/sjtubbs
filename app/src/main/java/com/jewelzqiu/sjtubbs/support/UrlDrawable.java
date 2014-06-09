package com.jewelzqiu.sjtubbs.support;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by jewelzqiu on 6/9/14.
 */
public class UrlDrawable extends BitmapDrawable {

    protected Drawable mDrawable;

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
