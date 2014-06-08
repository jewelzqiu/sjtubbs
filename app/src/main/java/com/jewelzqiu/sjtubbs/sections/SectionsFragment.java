package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.GetSectionsTask;
import com.jewelzqiu.sjtubbs.support.OnSectionsGetListener;
import com.jewelzqiu.sjtubbs.support.Section;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * A fragment representing a list of Sections.
 */
public class SectionsFragment extends Fragment implements OnSectionsGetListener {

    private static final String SECTION_DATA = "section_data";

    private Context mContext;

    private PullToRefreshLayout mPullToRefreshLayout;

    private ExpandableListView mSectionListView;

    private ProgressBar mProgressBar;

    private ArrayList<Section> mSections;

    private SectionsAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SectionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_list, null);
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        new GetSectionsTask(SectionsFragment.this).execute();
                    }
                })
                .setup(mPullToRefreshLayout);
        mSectionListView = (ExpandableListView) view.findViewById(R.id.section_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        if (savedInstanceState != null) {
            mSections = savedInstanceState.getParcelableArrayList(SECTION_DATA);
            onSectionsGet(BBSApplication.sectionList);
        } else if(BBSApplication.sectionList != null) {
            onSectionsGet(BBSApplication.sectionList);
        } else {
            new GetSectionsTask(this).execute();
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SECTION_DATA, mSections);
    }

    @Override
    public void onSectionsGet(ArrayList<Section> list) {
        mPullToRefreshLayout.setRefreshComplete();
        mSections = list;
        BBSApplication.sectionList = list;
        mProgressBar.setVisibility(View.GONE);
        mSectionListView.setVisibility(View.VISIBLE);
        if (list == null) {
            Toast.makeText(mContext, "Network error", Toast.LENGTH_SHORT).show();
            return;
        }
        mAdapter = new SectionsAdapter(list, mContext);
        mSectionListView.setAdapter(mAdapter);
        mSectionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view,
                    int groupPos, int childPos, long l) {
                mAdapter.onChildClick(groupPos, childPos, mContext);
                return false;
            }
        });
    }
}
