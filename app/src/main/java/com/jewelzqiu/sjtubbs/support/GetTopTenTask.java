package com.jewelzqiu.sjtubbs.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class GetTopTenTask extends AsyncTask<String, Void, Boolean> {

    private static final String REQUEST_URL = Utils.BBS_BASE_URL + "/php/bbsindex.html";

    private OnPostsGetListener mListener;

    private ArrayList<Post> mPosts = new ArrayList<>();

    public GetTopTenTask(OnPostsGetListener listener) {
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = true;
        try {
            Document doc = Jsoup.connect(REQUEST_URL).get();
            Elements elementsTopTen = doc.select(params[0]).first().select("td[align=center]")
                    .select("td[bgcolor=#f6f6f6]").select("tr");
            for (Element element : elementsTopTen) {
                mPosts.add(parsePost(element));
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private Post parsePost(Element element) {
        String title, board, author, url;
        Elements columns = element.select("td");

        Element data = columns.get(0);
        board = data.text();

        data = columns.get(1);
        title = data.text();
        url = Utils.BBS_BASE_URL + data.select("a").attr("href").replace("bbstcon", "bbswaptcon");

        data = columns.get(2);
        author = data.text();

        return new Post(url, title, board, author);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onPostsGet(result ? mPosts : null, null);
    }
}
