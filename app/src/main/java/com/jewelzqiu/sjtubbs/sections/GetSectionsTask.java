package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.support.Board;
import com.jewelzqiu.sjtubbs.support.OnSectionsGetListener;
import com.jewelzqiu.sjtubbs.support.Section;
import com.jewelzqiu.sjtubbs.support.Utils;

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
public class GetSectionsTask extends AsyncTask<Void, Void, Boolean> {

    private static final String url = Utils.BBS_BASE_URL + "/bbssec";

    ArrayList<Section> sectionList = new ArrayList<Section>();

    OnSectionsGetListener mListener;

    public GetSectionsTask(OnSectionsGetListener listener) {
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean success = true;
        try {
            Document doc = Jsoup.connect(url).get();
            parseSections(doc.getElementsByTag("tbody").select("tr"));
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    private void parseSections(Elements elements) throws IOException {
        String sectionUrl, name;
        ArrayList<Board> boardList;

        for (int i = 1; i < elements.size(); i++) {
            Element element = elements.get(i);
            Element section = element.getElementsByTag("td").get(1).select("a").first();
            sectionUrl = Utils.BBS_BASE_URL + "/" + section.attr("href");
            name = section.text();
            boardList = parseBoards(sectionUrl);
            sectionList.add(new Section(name, sectionUrl, boardList));
        }
    }

    private ArrayList<Board> parseBoards(String sectionUrl) throws IOException {
        ArrayList<Board> result = new ArrayList<Board>();
        ArrayList<Board> subBoards;
        String title, name, boardUrl;

        Document doc = Jsoup.connect(sectionUrl).get();
        Elements elements = doc.getElementsByTag("tbody").select("tr");
        for (int i = 1; i < elements.size(); i++) {
            Element element = elements.get(i);
            Element board = element.getElementsByTag("td").get(2).select("a").first();
            boardUrl = Utils.BBS_BASE_URL + "/" + board.attr("href");
            name = board.text();
            title = element.getElementsByTag("td").get(5).select("a").first().text();
            String flag = element.getElementsByTag("td").get(1).text();
            if (flag.equals("＋")) {
                subBoards = parseBoards(boardUrl);
                result.add(new Board(title, name, boardUrl, subBoards));
            } else {
                result.add(new Board(title, name, boardUrl.replace("bbsdoc", "bbstdoc")));
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        mListener.onSectionsGet(success ? sectionList : null);
    }
}
