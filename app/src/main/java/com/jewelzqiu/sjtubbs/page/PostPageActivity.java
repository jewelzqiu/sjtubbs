package com.jewelzqiu.sjtubbs.page;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.Utils;

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
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class PostPageActivity extends Activity implements AbsListView.OnScrollListener {

    public static final String POST_URL = "post_url";

    public static final String PAGE_TITLE = "page_title";

    private static final String IMG_AUTO_ZOOM = "javascript:"
            + "var elements = document.getElementsByTag('img').onload = "
            + "function(){"
            + "if(this.width > screen.width) {"
            + "this.width = screen.width"
            + "}"
            + "}";

    private static final int FLAG_OK = 0;

    private static final int FLAG_ERROR = -1;

    private static final int FLAG_NO_MORE = 1;

    private PullToRefreshLayout mPullToRefreshLayout;

    private ProgressBar mProgressBar;

    private ListView mPostListView;

    private ProgressBar mFooterView;

    private PostPageAdapter mAdapter;

    private String originalUrl, nextPageUrl;

    private int visibleLastIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_list);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        new PrepareContentTask(true).execute(originalUrl);
                        mFooterView.setVisibility(View.VISIBLE);
                    }
                })
                .setup(mPullToRefreshLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPostListView = (ListView) findViewById(R.id.post_list);
        mPostListView.setOnScrollListener(this);
        mFooterView = (ProgressBar) getLayoutInflater()
                .inflate(R.layout.progressbar_loading, null);
        mPostListView.addFooterView(mFooterView);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getIntent().getStringExtra(PAGE_TITLE));

        originalUrl = getIntent().getStringExtra(POST_URL);

        BBSApplication.imgUrlMap.clear();
        BBSApplication.imgUrlList.clear();
        new PrepareContentTask(true).execute(originalUrl);
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
            startActivity(new Intent(this, PicActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onPostListGet(ArrayList<String> postList, int result, boolean clear) {
        mPullToRefreshLayout.setRefreshComplete();
        mProgressBar.setVisibility(View.GONE);
        mPostListView.setVisibility(View.VISIBLE);
        switch (result) {
            case FLAG_ERROR:

                break;

            case FLAG_OK:
                if (clear || mAdapter == null) {
                    mAdapter = new PostPageAdapter(this, postList);
                    mPostListView.setAdapter(mAdapter);
                } else {
                    mAdapter.appendPosts(postList);
                }
                if (nextPageUrl == null || postList.isEmpty()) {
                    mFooterView.setVisibility(View.INVISIBLE);
                } else {
                    mFooterView.setVisibility(View.VISIBLE);
                }
                break;

            case FLAG_NO_MORE:
                mFooterView.setVisibility(View.INVISIBLE);
                break;
        }
//        mPostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mAdapter.onItemClick(PostPageActivity.this, position);
//            }
//        });

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == mAdapter.getCount()) {
            new PrepareContentTask(false).execute(nextPageUrl);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private class PrepareContentTask extends AsyncTask<String, Void, Integer> {

        private String url;

        private ArrayList<String> postList = new ArrayList<String>();

        private boolean clear;

        public PrepareContentTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected Integer doInBackground(String... params) {
            url = params[0];

            if (url == null) {
                return FLAG_NO_MORE;
            }

            try {
                Document doc = Jsoup.connect(url).get();

                Elements imgs = doc.select("img");
                for (Element img : imgs) {
                    String src = img.attr("src");
                    src = Utils.BBS_BASE_URL + src;
                    img.attr("src", src);
                    int pos = BBSApplication.imgUrlMap.size();
                    BBSApplication.imgUrlMap.put(src, pos);
                    BBSApplication.imgUrlList.add(src);
                }

                Elements posts = doc.getElementsByTag("pre");
                for (Element post : posts) {
                    String postContent = post.html().replace("\n", "<br />");
                    postContent = postContent.substring(postContent.indexOf(']') + 2);
                    postList.add(postContent);
                }

                Elements links = doc.select("body > a");
                nextPageUrl = null;
                if (links.size() >= 4) {
                    Element nextLink = links.get(links.size() - 4);
                    if (nextLink.text().equals("下一页")) {
                        nextPageUrl = Utils.BBS_BASE_URL + "/" + nextLink.attr("href");
                    }
                }
                return FLAG_OK;
            } catch (IOException e) {
                e.printStackTrace();
                return FLAG_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            invalidateOptionsMenu();
            onPostListGet(postList, result, clear);
        }
    }
}
