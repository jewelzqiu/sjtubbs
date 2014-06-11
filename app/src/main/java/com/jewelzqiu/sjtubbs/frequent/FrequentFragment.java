package com.jewelzqiu.sjtubbs.frequent;



import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.DatabaseHelper;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class FrequentFragment extends Fragment {

    private Context mContext;

    private ListView mFrequentListView;

    private FrequentListAdapter mAdapter;

    public FrequentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequent, container, false);
        mFrequentListView = (ListView) view.findViewById(R.id.frequent_list);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        mAdapter = new FrequentListAdapter(mContext, dbHelper.query(), true);
        mFrequentListView.setAdapter(mAdapter);
    }
}
