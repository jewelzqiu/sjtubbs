package com.jewelzqiu.sjtubbs.topten;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.OnPostsGetListener;
import com.jewelzqiu.sjtubbs.support.Post;
import com.jewelzqiu.sjtubbs.support.PostListAdapter;
import com.jewelzqiu.sjtubbs.support.Utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class TopTenFragment extends Fragment implements OnPostsGetListener {

    private static final String TOP_TEN_DATA = "top_ten_data";

    private Context mContext;

    private PullToRefreshLayout mPullToRefreshLayout;

    private ListView mTopTenListView;

    private ProgressBar mProgressBar;

    private PostListAdapter mAdapter;

    private ArrayList<Post> mPosts;

    public TopTenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        new GetTopTenTask(TopTenFragment.this).execute(Utils.TYPE_TOP_TEN);
                    }
                })
                .setup(mPullToRefreshLayout);
        mTopTenListView = (ListView) view.findViewById(R.id.post_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        if (savedInstanceState != null) {
            mPosts = savedInstanceState.getParcelableArrayList(TOP_TEN_DATA);
            onPostsGet(mPosts, null);
        } else if (BBSApplication.topTenList != null){
            onPostsGet(BBSApplication.topTenList, null);
        } else {
            new GetTopTenTask(this).execute(Utils.TYPE_TOP_TEN);
        }
        return view;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TOP_TEN_DATA, mPosts);
    }

    @Override
    public void onPostsGet(ArrayList<Post> posts, String nextUrl) {
        mPullToRefreshLayout.setRefreshComplete();
        mProgressBar.setVisibility(View.GONE);
        mTopTenListView.setVisibility(View.VISIBLE);
        BBSApplication.topTenList = posts;
        mPosts = posts;
        if (posts == null) {
            Toast.makeText(mContext, "Network error", Toast.LENGTH_SHORT).show();
            return;
        }
        mAdapter = new PostListAdapter(posts, mContext);
        mTopTenListView.setAdapter(mAdapter);
        mTopTenListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.onItemClick(i, mContext);
            }
        });
    }

}
