package com.jewelzqiu.sjtubbs.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class GetPostsTask extends AsyncTask<String, Void, Void> {

    private ArrayList<Post> postList = new ArrayList<Post>();

    private OnPostsGetListener mListener;

    private String nextUrl;

    public GetPostsTask(OnPostsGetListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            Document doc = Jsoup.connect(strings[0]).get();
            Elements elements = doc.getElementsByTag("tbody").select("tr");
            parsePosts(elements);

            Element nextLink = doc.select("hr ~ a").first();
            nextUrl = Utils.BBS_BASE_URL + "/" + nextLink.attr("href");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parsePosts(Elements elements) {
        for (int i = elements.size() - 1; i > 0; i--) {
            Elements data = elements.get(i).select("td");
            String desc = data.get(0).text();
            String author = data.get(2).select("a").first().text();
            Element element = data.get(4).select("a").first();
            String title = element.text();
            String url = Utils.BBS_BASE_URL + "/" + element.attr("href").replace("bbstcon",
                    "bbswaptcon");
            postList.add(new Post(url, title, desc, author));
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onPostsGet(postList, nextUrl);
    }
}
