package com.jewelzqiu.sjtubbs.topten;

import com.jewelzqiu.sjtubbs.support.OnPostsGetListener;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class GetTopTenTask extends AsyncTask<String, Void, Boolean> {

    private static final String REQUEST_URL = Utils.BBS_BASE_URL + "/file/bbs/mobile/top100.html";

    private OnPostsGetListener mListener;

    private ArrayList<Post> mPosts = new ArrayList<>();

    public GetTopTenTask(OnPostsGetListener listener) {
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = true;
        try {
            Document doc = Jsoup.parse(new URL(REQUEST_URL).openStream(), "gb18030",REQUEST_URL);
            Elements links = doc.getElementsByTag("a");
            for (int i = 0; i < links.size(); i += 2) {
                Element boardLink = links.get(i);
                Element postLink = links.get(i + 1);
                String board = boardLink.text();
                String title = postLink.text();
                String url = Utils.BBS_BASE_URL + postLink.attr("href");
                String author = postLink.nextSibling().outerHtml();
                String id = url.substring(url.lastIndexOf('=') + 1);
                mPosts.add(new Post(url, title, board, author, null, id));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onPostsGet(result ? mPosts : null, null);
    }
}
