package com.jewelzqiu.sjtubbs.newpost;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 7/7/14.
 */
public class NewPostActivity extends Activity {

    public static final String FLAG_IS_REPLY = "is_reply";

    public static final String REPLY_URL = "url";

    public static final String REPLY_TO = "reply_to_user";

    public static final String BOARD_NAME = "board";

    private boolean isReply;

    private String replyToUser;

    private String boardName;

    private ArrayList<NameValuePair> postValues = new ArrayList<NameValuePair>();

    private EditText titleText;

    private EditText contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        titleText = (EditText) findViewById(R.id.edittext_title);
        contentText = (EditText) findViewById(R.id.edittext_content);

        postValues.clear();
        Intent intent = getIntent();
        isReply = intent.getBooleanExtra(FLAG_IS_REPLY, false);
        replyToUser = intent.getStringExtra(REPLY_TO);
        boardName = intent.getStringExtra(BOARD_NAME);
        new GetPostValuesTask().execute(intent.getStringExtra(REPLY_URL));

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_send:
                new PostTask().execute();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetPostValuesTask extends AsyncTask<String, Void, Void> {

        private String title;

        private String content;

        @Override
        protected Void doInBackground(String... params) {
            postValues.add(new BasicNameValuePair("signature", "1"));
            postValues.add(new BasicNameValuePair("autocr", "on"));
            postValues.add(new BasicNameValuePair("up", ""));
            postValues.add(new BasicNameValuePair("MAX_FILE_SIZE", "1048577"));
            postValues.add(new BasicNameValuePair("level", "0"));
            postValues.add(new BasicNameValuePair("live", "180"));
            postValues.add(new BasicNameValuePair("exp", "0"));
            postValues.add(new BasicNameValuePair("board", boardName));

            if (isReply) {
                postValues.add(new BasicNameValuePair(REPLY_TO, replyToUser));

                try {
                    Document document = Jsoup.connect(params[0]).cookies(Utils.cookies).get();
                    Elements elements = document.getElementsByTag("input");
                    for (Element element : elements) {
                        if (element.attr("name").equals("file")) {
                            postValues.add(new BasicNameValuePair("file", element.attr("value")));
                        } else if (element.attr("name").equals("reidstr")) {
                            postValues.add(new BasicNameValuePair("reidstr", element.attr("value")));
                        } else if (element.attr("name").equals("title")) {
                            title = element.attr("value");
                        }
                    }

                    Element textArea = document.select("#text").first();
                    content = textArea.text();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                postValues.add(new BasicNameValuePair("file", ""));
                postValues.add(new BasicNameValuePair("reidstr", ""));
                postValues.add(new BasicNameValuePair(REPLY_TO, ""));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            titleText.setText(title);
            contentText.setText(content);
        }
    }

    private class PostTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            postValues.add(new BasicNameValuePair("title", titleText.getText().toString()));
            postValues.add(new BasicNameValuePair("text", contentText.getText().toString()));

            try {
                HttpPost httpPost = new HttpPost(Utils.BBS_BASE_URL + "/bbssnd");
                DefaultHttpClient client = new DefaultHttpClient();
                httpPost.addHeader("Cookie", Utils.getCookies());
                httpPost.addHeader("Connection", "keep-live");
                httpPost.setEntity(new UrlEncodedFormEntity(postValues, "GB2312"));
                HttpResponse httpResponse = client.execute(httpPost);
                return EntityUtils.toString(httpResponse.getEntity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.contains("ERROR")) {
                Toast.makeText(getApplicationContext(), getString(R.string.post_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.post_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
