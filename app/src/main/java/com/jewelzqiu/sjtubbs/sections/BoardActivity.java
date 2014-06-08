package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.GetPostsTask;
import com.jewelzqiu.sjtubbs.support.GetTopTenTask;
import com.jewelzqiu.sjtubbs.support.OnPostsGetListener;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.PostListAdapter;
import com.jewelzqiu.sjtubbs.support.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class BoardActivity extends Activity implements OnPostsGetListener,
        AbsListView.OnScrollListener {

    public static final String BOARD_NAME = "board_name";

    public static final String BOARD_URL = "board_url";

    private PullToRefreshLayout mPullToRefreshLayout;

    private ProgressBar mProgressBar;

    private ListView mPostListView;

    private PostListAdapter mAdapter;

    private int visibleLastIndex;

    private int lastPostId = -1;

    private String nextPageUrl = null;

    boolean isRefresh = false;

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
                        isRefresh = true;
                        lastPostId = -1;
                        new GetTopTenTask(BoardActivity.this).execute(Utils.TYPE_TOP_TEN);
                    }
                })
                .setup(mPullToRefreshLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPostListView = (ListView) findViewById(R.id.post_list);
        mPostListView.setOnScrollListener(this);
        ProgressBar loadingView = (ProgressBar) getLayoutInflater()
                .inflate(R.layout.progressbar_loading, null);
        mPostListView.addFooterView(loadingView);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra(BOARD_NAME));

        new GetPostsTask(this).execute(intent.getStringExtra(BOARD_URL));
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

    @Override
    public void onPostsGet(ArrayList<Post> posts, String nextUrl) {
        mPullToRefreshLayout.setRefreshComplete();
        nextPageUrl = nextUrl;
        System.out.println(nextUrl);
        mProgressBar.setVisibility(View.GONE);
        mPostListView.setVisibility(View.VISIBLE);
        if (posts == null) {
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastPostId >= 0) {
            for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext();) {
                try {
                    int temp = Integer.parseInt(iterator.next().board);
                    if (temp >= lastPostId) {
                        iterator.remove();
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
        lastPostId = Integer.parseInt(posts.get(posts.size() - 1).board);

        if (mAdapter == null || isRefresh) {
            isRefresh = false;
            mAdapter = new PostListAdapter(posts, this);
            mPostListView.setAdapter(mAdapter);
        } else {
            mAdapter.appendData(posts);
        }
        mPostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.onItemClick(i, BoardActivity.this);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == mAdapter.getCount()) {
            new GetPostsTask(this).execute(nextPageUrl);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }
}
