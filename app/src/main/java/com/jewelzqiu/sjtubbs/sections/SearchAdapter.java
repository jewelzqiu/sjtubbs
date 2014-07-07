package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.main.BBSApplication;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/24/14.
 */
public class SearchAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> nameList;

    private Filter mFilter;

    public SearchAdapter(Context context, int resource) {
        super(context, resource);
        nameList = new ArrayList<String>();
        mFilter = new MyFilter();
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    @Override
    public String getItem(int position) {
        return nameList.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class MyFilter extends Filter {

        private ArrayList<String> mList;

        public MyFilter() {
            mList = new ArrayList<String>(BBSApplication.boardNameList);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<String> list = new ArrayList<String>();
            if (TextUtils.isEmpty(constraint)) {
                list.addAll(mList);
            } else {
                String input = constraint.toString().toLowerCase();
                for (String s : mList) {
                    if (s.toLowerCase().contains(input)) {
                        list.add(s);
                    }
                }
            }
            results.count = list.size();
            results.values = list;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            nameList = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
