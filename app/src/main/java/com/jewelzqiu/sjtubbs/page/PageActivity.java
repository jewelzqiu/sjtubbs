package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class PageActivity extends Activity {

    public static final String POST_URL = "post_url";

    public static final String PAGE_TITLE = "page_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(getIntent().getStringExtra(POST_URL));

        setTitle(getIntent().getStringExtra(PAGE_TITLE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
