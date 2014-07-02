package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.page.PostPageActivity;

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

    private Context mContext;

    private LayoutInflater mInflater;

    private ArrayList<Post> mPosts;

    private DatabaseHelper mDatabaseHelper;

    public PostListAdapter(ArrayList<Post> posts, Context context) {
        mContext = context;
        mPosts = posts;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDatabaseHelper = new DatabaseHelper(context);
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
    public View getView(int i, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.post_list_item, parent, false);
            holder = new ViewHolder();
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.boardView = (TextView) view.findViewById(R.id.board);
            holder.authorView = (TextView) view.findViewById(R.id.author);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Post post = mPosts.get(i);
        holder.titleView.setText(post.title);
        holder.authorView.setText(post.author);
        if (post.desc != null) {
            holder.boardView.setText(post.desc);
        } else {
            holder.boardView.setText(post.board);
        }

        if (Utils.isMarkReadEnabled(mContext) && mDatabaseHelper.isPostViewed(post)) {
            holder.titleView
                    .setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
        }

        return view;
    }

    private class ViewHolder {

        TextView titleView;

        TextView boardView;

        TextView authorView;
    }

    public void onItemClick(int position, Context context) {
        Intent intent = new Intent(context, PostPageActivity.class);
        intent.putExtra(PostPageActivity.POST_URL, mPosts.get(position).url);
        intent.putExtra(PostPageActivity.PAGE_TITLE, mPosts.get(position).title);
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        dbHelper.setPostViewed(mPosts.get(position));
        context.startActivity(intent);
    }

    public void appendData(ArrayList<Post> list) {
        mPosts.addAll(list);
        notifyDataSetChanged();
    }

}
