package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.OnPostsGetListener;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.PostListAdapter;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    public static final String BOARD_TITLE = "board_title";

    public static final String BOARD_NAME = "board_name";

    public static final String BOARD_URL = "board_url";

    private PullToRefreshLayout mPullToRefreshLayout;

    private ProgressBar mProgressBar;

    private ListView mPostListView;

    private ProgressBar mFooterView;

    private PostListAdapter mAdapter;

    private int visibleLastIndex;

    private int lastPostId = -1;

    private String nextPageUrl = null;

    private boolean isRefresh = false;

    private String mBoardName;

    private String boardUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mBoardName = getIntent().getStringExtra(BOARD_NAME);
        Utils.CURRENT_BOARD = mBoardName;

        setContentView(R.layout.fragment_post_list);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        isRefresh = true;
                        lastPostId = -1;
                        new GetPostsTask(BoardActivity.this).execute(boardUrl);
                    }
                })
                .setup(mPullToRefreshLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPostListView = (ListView) findViewById(R.id.post_list);
        mPostListView.setOnScrollListener(this);
        mPostListView.setOnItemClickListener(this);
        mFooterView = (ProgressBar) getLayoutInflater().inflate(R.layout.progressbar_loading, null);
        mPostListView.addFooterView(mFooterView);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(intent.getStringExtra(BOARD_TITLE));

        boardUrl = intent.getStringExtra(BOARD_URL);
        new GetPostsTask(this).execute(boardUrl);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_uploaded) {
            Intent intent = new Intent(this, UploadedPicsActivity.class);
            intent.putExtra(UploadedPicsActivity.UPLOADED_URL,
                    Utils.BBS_BASE_URL + "/bbsfdoc2?board=" + mBoardName);
            intent.putExtra(BOARD_TITLE, getTitle());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostsGet(ArrayList<Post> posts, String nextUrl) {
        mPullToRefreshLayout.setRefreshComplete();
        nextPageUrl = nextUrl;
        mProgressBar.setVisibility(View.GONE);
        mPostListView.setVisibility(View.VISIBLE);
        if (posts == null) {
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            mFooterView.setVisibility(View.INVISIBLE);
            return;
        }

        if (posts.isEmpty()) {
            return;
        }

        if (lastPostId >= 0) {
            for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext(); ) {
                try {
                    int temp = Integer.parseInt(iterator.next().desc);
                    if (temp >= lastPostId) {
                        iterator.remove();
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
        lastPostId = Integer.parseInt(posts.get(posts.size() - 1).desc);

        if (mAdapter == null || isRefresh) {
            isRefresh = false;
            mAdapter = new PostListAdapter(posts, this);
            mPostListView.setAdapter(mAdapter);
        } else {
            mAdapter.appendData(posts);
        }
        if (posts.isEmpty()) {
            mFooterView.setVisibility(View.INVISIBLE);
        } else {
            mFooterView.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.onItemClick(position, this);
    }
}
