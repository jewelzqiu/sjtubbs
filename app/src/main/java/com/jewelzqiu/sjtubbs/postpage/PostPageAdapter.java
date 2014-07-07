package com.jewelzqiu.sjtubbs.postpage;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.newpost.NewPostActivity;
import com.jewelzqiu.sjtubbs.support.Reply;
import com.jewelzqiu.sjtubbs.support.UrlDrawable;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/9/14.
 */
public class PostPageAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Reply> mReplyList;

    public PostPageAdapter(Context context, ArrayList<Reply> replyList) {
        mContext = context;
        mReplyList = replyList;
    }

    public void appendPosts(ArrayList<Reply> replyList) {
        mReplyList.addAll(replyList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mReplyList.size();
    }

    @Override
    public Object getItem(int position) {
        return mReplyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.post_detail_list_item, parent, false);

            holder = new ViewHolder();
            holder.userIdView = (TextView) convertView.findViewById(R.id.text_id);
            holder.timeView = (TextView) convertView.findViewById(R.id.text_time);
            holder.titleView = (TextView) convertView.findViewById(R.id.text_title);
            holder.contentView = (TextView) convertView.findViewById(R.id.text_content);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Reply reply = mReplyList.get(position);
        holder.userIdView.setText(reply.userId);
        holder.timeView.setText(reply.time);
        holder.titleView.setText(reply.title);
        holder.contentView.setText(
                Html.fromHtml(reply.content, new MyImageGetter(mContext, holder.contentView),
                        null));

        Utils.setSexColor(holder.userIdView, reply.userId);

        return convertView;
    }

    public void onItemClick(Context context, int position, String boardName) {
//        Intent intent = new Intent(context, ReplyDetailActivity.class);
//        Reply reply = mReplyList.get(position);
//        intent.putExtra(ReplyDetailActivity.REPLY_USER, reply.userId);
//        intent.putExtra(ReplyDetailActivity.REPLY_TIME, reply.time);
//        intent.putExtra(ReplyDetailActivity.REPLY_TITLE, reply.title);
//        intent.putExtra(ReplyDetailActivity.REPLY_CONTENT, reply.content);
        String url = mReplyList.get(position).url;
        if (url == null) {
            return;
        }
        Intent intent = new Intent(context, NewPostActivity.class);
        intent.putExtra(NewPostActivity.FLAG_IS_REPLY, true);
        intent.putExtra(NewPostActivity.REPLY_URL, url);
        intent.putExtra(NewPostActivity.REPLY_TO, mReplyList.get(position).userId);
        intent.putExtra(NewPostActivity.BOARD_NAME, boardName);
        mContext.startActivity(intent);
    }

    private class ViewHolder {

        public TextView userIdView;

        public TextView timeView;

        public TextView titleView;

        public TextView contentView;
    }

    private class MyImageGetter implements Html.ImageGetter {

        private Context mContext;

        private View mContainer;

        public MyImageGetter(Context context, View container) {
            mContext = context;
            mContainer = container;
        }

        @Override
        public Drawable getDrawable(String source) {
            final UrlDrawable urlDrawable = new UrlDrawable();
            Future<Bitmap> bitmapFuture = Ion.with(mContext, source).asBitmap();
            bitmapFuture.then(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap bitmap) {
                    if (bitmap == null) {
                        return;
                    }
                    int width = Math.min(BBSApplication.contentWidth, bitmap.getWidth());
                    double scale = (double) width / bitmap.getWidth();
                    int height = (int) (bitmap.getHeight() * scale);
                    WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(
                            Bitmap.createScaledBitmap(bitmap, width, height, true));
                    Drawable drawable = new BitmapDrawable(mContext.getResources(),
                            bitmapWeakReference.get());
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    urlDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    urlDrawable.setDrawable(drawable);
                    TextView textView = (TextView) MyImageGetter.this.mContainer;
                    textView.setText(textView.getText());
                }
            });
//            new ImageGetterTask(urlDrawable).execute(source);
            return urlDrawable;
        }

//        private class ImageGetterTask extends AsyncTask<String, Void, Drawable> {
//
//            UrlDrawable mDrawable;
//
//            public ImageGetterTask(UrlDrawable drawable) {
//                mDrawable = drawable;
//            }
//
//            @Override
//            protected Drawable doInBackground(String... params) {
//                String source = params[0];
//                try {
//                    InputStream is = fetch(source);
//                    Drawable drawable = Drawable.createFromStream(is, "src");
//                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
//                            drawable.getIntrinsicHeight());
//                    return drawable;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            private InputStream fetch(String urlString) throws IOException {
//                DefaultHttpClient httpClient = new DefaultHttpClient();
//                HttpGet request = new HttpGet(urlString);
//                HttpResponse response = httpClient.execute(request);
//                return response.getEntity().getContent();
//            }
//
//            @Override
//            protected void onPostExecute(Drawable drawable) {
//                mDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
//                        drawable.getIntrinsicHeight());
//                mDrawable.setDrawable(drawable);
//                TextView textView = (TextView) MyImageGetter.this.mContainer;
//                textView.setText(textView.getText());
//            }
//        }
    }
}
