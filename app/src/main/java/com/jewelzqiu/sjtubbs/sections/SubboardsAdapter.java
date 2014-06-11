package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Board;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class SubboardsAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mInflater;

    private ArrayList<Board> mBoards;

    public SubboardsAdapter(Context context, ArrayList<Board> boards) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBoards = boards;
    }

    @Override
    public int getCount() {
        return mBoards.size();
    }

    @Override
    public Object getItem(int i) {
        return mBoards.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.dialog_list_item, null);
        }
        TextView textView = (TextView) view;
        Board board = mBoards.get(i);
        textView.setText(board.title + " " + board.name);
        return view;
    }

    public void onItemClick(Context context, int position) {
        Board board = mBoards.get(position);
        Intent intent = new Intent(mContext, BoardActivity.class);
        intent.putExtra(BoardActivity.BOARD_TITLE, board.title);
        intent.putExtra(BoardActivity.BOARD_NAME, board.name);
        intent.putExtra(BoardActivity.BOARD_URL, board.url);
        mContext.startActivity(intent);
    }
}
