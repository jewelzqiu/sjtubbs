package com.jewelzqiu.sjtubbs.postpage;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.Reply;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

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
import android.widget.AdapterView;
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

    public static final String BOARD_NAME = "board";

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

    private String boardName;

    private ArrayList<String> mReplyUrlList = new ArrayList<>();

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
        mPostListView.setOnItemClickListener(new OnPostClickListener());
        mFooterView = (ProgressBar) getLayoutInflater()
                .inflate(R.layout.progressbar_loading, null);
        mPostListView.addFooterView(mFooterView);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getIntent().getStringExtra(PAGE_TITLE));

        originalUrl = getIntent().getStringExtra(POST_URL);
        boardName = getIntent().getStringExtra(BOARD_NAME);

        BBSApplication.imgUrlMap.clear();
        BBSApplication.imgUrlList.clear();
        new PrepareContentTask(true).execute(originalUrl);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);
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

    private void onPostListGet(ArrayList<Reply> replyList, int result, boolean clear) {
        mPullToRefreshLayout.setRefreshComplete();
        mProgressBar.setVisibility(View.GONE);
        mPostListView.setVisibility(View.VISIBLE);
        switch (result) {
            case FLAG_ERROR:

                break;

            case FLAG_OK:
                if (clear || mAdapter == null) {
                    mAdapter = new PostPageAdapter(this, replyList);
                    mPostListView.setAdapter(mAdapter);
                } else {
                    mAdapter.appendPosts(replyList);
                }
                if (nextPageUrl == null || replyList.isEmpty()) {
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

        private ArrayList<Reply> replyList = new ArrayList<Reply>();

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
                    if (!src.toLowerCase().startsWith("http")) {
                        src = Utils.BBS_BASE_URL + src;
                    }
                    img.attr("src", src);
                    int pos = BBSApplication.imgUrlMap.size();
                    BBSApplication.imgUrlMap.put(src, pos);
                    BBSApplication.imgUrlList.add(src);
                }

                boolean special = url.startsWith(Utils.BBS_BASE_URL + "/bbstopcon");

                Elements posts = doc.getElementsByTag("pre");
                for (Element post : posts) {
//                    String postContent = post.html();
                    StringBuilder builder = new StringBuilder(post.html());
                    Reply reply;
                    String id = "", time = "", title = "", content;
                    if (special) {
                        content = builder.toString().replace("\n", "<br />");
                        reply = new Reply(id, time, title, content, null);
                        replyList.add(reply);
                        continue;
                    }
                    try {
                        String replyUrl = post.getElementsByTag("a").first().attr("href");
                        if (!replyUrl.startsWith("http")) {
                            replyUrl = Utils.BBS_BASE_URL + "/" + replyUrl;
                        }

                        builder = new StringBuilder(builder.substring(builder.indexOf("]") + 2));
                        int index = builder.indexOf("\n");
                        String line = builder.substring(0, index);
                        builder = new StringBuilder(builder.substring(index + 1));

                        index = line.indexOf(' ');
                        id = line.substring(0, index);
                        time = line.substring(index + 1);

                        index = builder.indexOf("\n");
                        title = builder.substring(0, index);
                        content = builder.substring(index + 1).replace("\n", "<br />");

                        replyList.add(new Reply(id, time, title, content, replyUrl));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
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
            onPostListGet(replyList, result, clear);
        }
    }

    private class OnPostClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (Utils.USER_ID == null) {
                Utils.login(PostPageActivity.this, null);
            } else {
                mAdapter.onItemClick(PostPageActivity.this, position, boardName);
            }
        }
    }
}
