package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.page.PageActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class PostListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<Post> mPosts;

    public PostListAdapter(ArrayList<Post> posts, Context context) {
        mPosts = posts;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Object getItem(int i) {
        return mPosts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.post_list_item, null);
            holder = new ViewHolder();
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.boardView = (TextView) view.findViewById(R.id.board);
            holder.authorView = (TextView) view.findViewById(R.id.author);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.titleView.setText(mPosts.get(i).title);
        holder.boardView.setText(mPosts.get(i).board);
        holder.authorView.setText(mPosts.get(i).author);
        return view;
    }

    private class ViewHolder {
        TextView titleView;
        TextView boardView;
        TextView authorView;
    }

    public void onItemClick(int position, Context context) {
        Intent intent = new Intent(context, PageActivity.class);
        intent.putExtra(PageActivity.POST_URL, mPosts.get(position).url);
        intent.putExtra(PageActivity.PAGE_TITLE, mPosts.get(position).title);
        context.startActivity(intent);
    }

    public void appendData(ArrayList<Post> list) {
        mPosts.addAll(list);
        notifyDataSetChanged();
    }

}
