package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashSet;

public class ReplyDetailActivity extends Activity {

    public static final String REPLY_USER = "reply_user";

    public static final String REPLY_TIME = "reply_time";

    public static final String REPLY_CONTENT = "reply_url";

    public static final String REPLY_TITLE = "reply_title";

    private static final String IMG_AUTO_ZOOM = "javascript:"
            + "var elements = document.getElementsByTag('img').onload = "
            + "function(){"
            + "if(this.width > screen.width) {"
            + "this.width = screen.width"
            + "}"
            + "}";

    private WebView mWebView;

    private HashSet<String> imgFormatSet = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setClipToPadding(false);

        BBSApplication.imgUrlMap.clear();
        BBSApplication.imgUrlList.clear();
        new PrepareContentTask(mWebView).execute(getIntent().getStringExtra(REPLY_CONTENT));

        setTitle(getIntent().getStringExtra(REPLY_TITLE));

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View layout = findViewById(R.id.parent_layout);
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            layout.setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());
        }

        imgFormatSet.clear();
        imgFormatSet.add("jpg");
        imgFormatSet.add("jpeg");
        imgFormatSet.add("gif");
        imgFormatSet.add("png");
        imgFormatSet.add("bmp");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BBSApplication.imgUrlMap.isEmpty()) {
            return true;
        }
        getMenuInflater().inflate(R.menu.post_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_pic) {
            if (BBSApplication.imgUrlMap.isEmpty()) {
                Toast.makeText(this, "此贴没有图片！", Toast.LENGTH_SHORT).show();
                return true;
            }
            startActivity(new Intent(this, PicViewPagerActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl(IMG_AUTO_ZOOM);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            System.out.println("clicked url: " + url);
            if (BBSApplication.imgUrlMap.containsKey(url)) {
                Intent intent = new Intent(ReplyDetailActivity.this,
                        PicViewPagerActivity.class);
                intent.putExtra(PicViewPagerActivity.PHOTO_POSITION,
                        BBSApplication.imgUrlMap.get(url));
                startActivity(intent);
            } else if (imgFormatSet
                    .contains(url.substring(url.lastIndexOf('.') + 1).toLowerCase())) {
                Intent intent = new Intent(ReplyDetailActivity.this, SinglePicActivity.class);
                intent.putExtra(SinglePicActivity.PIC_URL, url);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
            return true;
        }
    }

    private class PrepareContentTask extends AsyncTask<String, Void, Boolean> {

        private String html;

        private WebView mWebView;

        private String content;

        public PrepareContentTask(WebView webView) {
            mWebView = webView;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            html = params[0];
            boolean success = true;

            System.out.println(html);
            Document doc = Jsoup.parse(html);

            Elements links = doc.select("a");
            for (Element link : links) {
                String url = link.attr("href");
                if (url.startsWith("/")) {
                    link.attr("href", Utils.BBS_BASE_URL + url);
                }
            }

            Elements imgs = doc.select("img");
            for (Element img : imgs) {
                img.wrap("<a href='" + img.attr("src") + "'></a>");
                int pos = BBSApplication.imgUrlMap.size();
                String url = img.attr("src");
                BBSApplication.imgUrlMap.put(url, pos);
                BBSApplication.imgUrlList.add(url);
            }
            content = doc.outerHtml();

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mWebView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
            } else {
                mWebView.loadUrl(null);
            }
            invalidateOptionsMenu();
        }
    }
}
