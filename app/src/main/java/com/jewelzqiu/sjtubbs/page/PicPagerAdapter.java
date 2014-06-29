package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by jewelzqiu on 6/9/14.
 */
public class PicPagerAdapter extends PagerAdapter {

    private Activity mActivity;

    private ArrayList<String> urlList;

    private Dialog longPressDialog;

    public PicPagerAdapter(Activity activity, ArrayList<String> urlList) {
        mActivity = activity;
        this.urlList = urlList;
    }

    @Override
    public int getCount() {
        return urlList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        Ion.with(photoView).load(urlList.get(position));
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v2) {
                mActivity.finish();
            }
        });
        photoView.setTag(urlList.get(position));
        photoView.setOnLongClickListener(new MyLongClickListener());
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private class MyLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            if (longPressDialog == null) {
                ListView listView = new ListView(mActivity);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                        android.R.layout.simple_list_item_1,
                        mActivity.getResources().getStringArray(
                                R.array.long_press_options)
                );
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnOptionClickListener((String) view.getTag()));
                longPressDialog = new AlertDialog.Builder(mActivity)
                        .setView(listView).create();
                longPressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            );
                        }
                    }
                });
            }
            longPressDialog.show();
            return true;
        }
    }

    private class OnOptionClickListener implements AdapterView.OnItemClickListener {

        private String imgUrl;

        public OnOptionClickListener(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            longPressDialog.dismiss();
            switch (position) {
                case 0:
                    if (Utils.PIC_CACHE_PATH == null) {
                        File cacheDir = mActivity.getExternalCacheDir();
                        if (cacheDir == null) {
                            Toast.makeText(mActivity, mActivity.getString(R.string.storage_failed),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Utils.PIC_CACHE_PATH = cacheDir.getAbsolutePath();
                    }
                    final String filePath = Utils.PIC_CACHE_PATH + imgUrl
                            .substring(imgUrl.lastIndexOf('/'));
                    Ion.with(mActivity).load(imgUrl).write(new File(filePath))
                            .setCallback(new FutureCallback<File>() {
                                @Override
                                public void onCompleted(Exception e, File result) {
                                    if (result == null) {
                                        e.printStackTrace();
                                        String msg = mActivity.getString(R.string.pic_not_saved);
                                        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_STREAM,
                                            Uri.parse("file://" + filePath));
                                    intent.setType("image/*");
                                    mActivity.startActivity(
                                            Intent.createChooser(intent,
                                                    mActivity.getString(R.string.share_pic))
                                    );
                                }
                            });
                    break;
                case 1:
                    String filepath = Utils.PIC_STORE_PATH + imgUrl
                            .substring(imgUrl.lastIndexOf('/'));
                    Ion.with(mActivity).load(imgUrl).write(new File(filepath))
                            .setCallback(new FutureCallback<File>() {
                                @Override
                                public void onCompleted(Exception e, File result) {
                                    String msg;
                                    if (result == null) {
                                        e.printStackTrace();
                                        msg = mActivity.getString(R.string.pic_not_saved);
                                    } else {
                                        msg = mActivity.getString(R.string.pic_saved);
                                    }
                                    Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
            }
        }
    }
}
