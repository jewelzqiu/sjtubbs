package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.UrlDrawable;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import android.app.Activity;
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

    private ArrayList<String> mPostList;

    public PostPageAdapter(Context context, ArrayList<String> postList) {
        mContext = context;
        mPostList = postList;
    }

    public void appendPosts(ArrayList<String> postList) {
        mPostList.addAll(postList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPostList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPostList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.post_detail_list_item, null);
        }
        TextView textView = (TextView) convertView;
        String url = mPostList.get(position);
        textView.setText(Html.fromHtml(url, new MyImageGetter(mContext, textView), null));
        return convertView;
    }

    public void onItemClick(Context context, int position) {
        Intent intent = new Intent(context, PageActivity.class);
        intent.putExtra(PageActivity.PAGE_TITLE, ((Activity) context).getTitle());
        intent.putExtra(PageActivity.POST_CONTENT, mPostList.get(position));
        mContext.startActivity(intent);
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
