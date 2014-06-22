package com.jewelzqiu.sjtubbs.sections;

import com.etsy.android.grid.StaggeredGridView;
import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class UploadedPicsActivity extends Activity implements AbsListView.OnScrollListener,
        AdapterView.OnItemClickListener {

    public static final String UPLOADED_URL = "uploaded_url";

    private String mUploadedUrl;

    private String mNextUrl;

    private ProgressBar mProgressBar;

    private PullToRefreshLayout mPullToRefreshLayout;

    private StaggeredGridView mGridView;

    private ProgressBar mFooterView;

    private UploadedPicsAdapter mAdapter;

    private int visibleLastIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_pics);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        mGridView = (StaggeredGridView) findViewById(R.id.grid_view);
        mFooterView = (ProgressBar) getLayoutInflater()
                .inflate(R.layout.progressbar_loading, null);

        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        new GetUploadedPicsTask(true).execute(mUploadedUrl);
                    }
                })
                .setup(mPullToRefreshLayout);

        mGridView.addFooterView(mFooterView);
        mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(this);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mUploadedUrl = getIntent().getStringExtra(UPLOADED_URL);
        setTitle(getString(R.string.action_uploaded) + ": " + getIntent()
                .getStringExtra(BoardActivity.BOARD_TITLE));

        BBSApplication.imgUrlList.clear();
        BBSApplication.imgUrlMap.clear();

        new GetUploadedPicsTask(true).execute(mUploadedUrl);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setNavigationBarAlpha(0.7f);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.uploaded_pics, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == mAdapter.getCount()) {
            new GetUploadedPicsTask(false).execute(mNextUrl);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter != null) {
            mAdapter.onItemClick(position);
        }
    }

    private class GetUploadedPicsTask extends AsyncTask<String, Void, Boolean> {

        private ArrayList<String> picUrlList = new ArrayList<String>();

        private boolean clear;

        public GetUploadedPicsTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = true;
            try {
                Document doc = Jsoup.connect(params[0]).get();
                Elements links = doc.select("a[target=_blank]");
                for (int i = links.size() - 1; i > 0; i--) {
                    picUrlList.add(links.get(i).attr("href"));

                }

                links = doc.getElementsByTag("a");
                Element link = links.get(links.size() - 4);
                if (link.text().equals("上一页")) {
                    mNextUrl = Utils.BBS_BASE_URL + "/" + link.attr("href");
                } else {
                    mNextUrl = Utils.BBS_BASE_URL + "/" + links.get(links.size() - 3).attr("href");
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } catch (NullPointerException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mPullToRefreshLayout.setRefreshComplete();
            mProgressBar.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            if (clear || mAdapter == null) {
                mAdapter = new UploadedPicsAdapter(UploadedPicsActivity.this, picUrlList);
                mGridView.setAdapter(mAdapter);
            } else {
                mAdapter.appendUrlList(picUrlList);
            }
            if (picUrlList.isEmpty()) {
                mFooterView.setVisibility(View.INVISIBLE);
            } else {
                mFooterView.setVisibility(View.VISIBLE);
            }
        }
    }
}
