package com.jewelzqiu.sjtubbs.sections;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.page.PicActivity;
import com.koushikdutta.ion.Ion;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jewelzqiu on 6/11/14.
 */
public class UploadedPicsAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mInflater;

    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    public UploadedPicsAdapter(Context context, ArrayList<String> picUrlList) {
        BBSApplication.imgUrlList = picUrlList;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void appendUrlList(ArrayList<String> urlList) {
        BBSApplication.imgUrlList.addAll(urlList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return BBSApplication.imgUrlList.size();
    }

    @Override
    public Object getItem(int position) {
        return BBSApplication.imgUrlList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DynamicHeightImageView imageView;
        if (convertView == null) {
            imageView = (DynamicHeightImageView) mInflater
                    .inflate(R.layout.uploaded_list_item, null);
        } else {
            imageView = (DynamicHeightImageView) convertView;
        }

        double positionHeight = getPositionRatio(position);
        imageView.setHeightRatio(positionHeight);

        final int viewWidth = BBSApplication.gridViewPicWidth;
        final int viewHeight = (int) (viewWidth * positionHeight);
        Ion.with(imageView)
                .resize(viewWidth, viewHeight)
                .centerCrop()
                .load(BBSApplication.imgUrlList.get(position));
        return imageView;
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (new Random().nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5 the width
    }

    public void onItemClick(int position) {
        Intent intent = new Intent(mContext, PicActivity.class);
        intent.putExtra(PicActivity.PHOTO_POSITION, position);
        mContext.startActivity(intent);
    }

}
