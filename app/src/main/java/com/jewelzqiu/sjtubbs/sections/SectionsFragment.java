package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.main.BBSApplication;
import com.jewelzqiu.sjtubbs.support.Board;
import com.jewelzqiu.sjtubbs.support.OnSectionsGetListener;
import com.jewelzqiu.sjtubbs.support.Section;
import com.jewelzqiu.sjtubbs.support.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    private View normalView;

    private ImageButton searchButton;

    private View searchView;

    private AutoCompleteTextView searchTextView;

    private ImageButton clearButton;

    private ArrayAdapter<String> mSearchAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SectionsFragment() {
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
        } else if (BBSApplication.sectionList != null) {
            onSectionsGet(BBSApplication.sectionList);
        } else {
            new GetSectionsTask(this).execute();
        }

        getActivity().getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM |
                        ActionBar.DISPLAY_USE_LOGO |
                        ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_HOME_AS_UP
        );

        searchView = inflater.inflate(R.layout.actionbar_search, null);
        searchTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_view);
        clearButton = (ImageButton) searchView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextView.setText("");
            }
        });
        mSearchAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_dropdown_item_1line, BBSApplication.boardNameList);
        searchTextView.setAdapter(mSearchAdapter);
        searchTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String boardName = (String) ((TextView) view).getText();
                Board board = BBSApplication.boardMap.get(boardName);
                Intent intent = new Intent(mContext, BoardActivity.class);
                intent.putExtra(BoardActivity.BOARD_TITLE, board.title);
                intent.putExtra(BoardActivity.BOARD_NAME, board.name);
                intent.putExtra(BoardActivity.BOARD_URL, board.url);
                mContext.startActivity(intent);
                resetActionBar();
            }
        });

        normalView = inflater.inflate(R.layout.actionbar_normal, null);
        searchButton = (ImageButton) normalView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getActionBar().setCustomView(searchView);
                searchTextView.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchTextView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        getActivity().getActionBar().setCustomView(normalView);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSectionListView.setFitsSystemWindows(true);
        mSectionListView.setClipToPadding(false);
        Utils.setInsets(getActivity(), mSectionListView);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetActionBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_HOME_AS_UP
        );
        getActivity().getActionBar().setCustomView(normalView);
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

        mSearchAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_dropdown_item_1line, BBSApplication.boardNameList);
        if (searchTextView != null) {
            searchTextView.setAdapter(mSearchAdapter);
        }
    }

    public void resetActionBar() {
        getActivity().getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_CUSTOM
                        | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_HOME_AS_UP
        );
        searchTextView.setText("");
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchTextView.getWindowToken(), 0);
        getActivity().getActionBar().setCustomView(normalView);
    }

    public boolean isSearching() {
        return getActivity().getActionBar().getCustomView() == searchView;
    }
}
