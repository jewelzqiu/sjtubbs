package com.jewelzqiu.sjtubbs.sections;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Board;
import com.jewelzqiu.sjtubbs.support.DatabaseHelper;
import com.jewelzqiu.sjtubbs.support.Section;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class SectionsAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    private ArrayList<Section> mSections;

    private LayoutInflater mInflater;

    private SubboardsAdapter mAdapter;

    private Dialog mDialog;

    public SectionsAdapter(ArrayList<Section> list, Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSections = list;
    }

    @Override
    public int getGroupCount() {
        return mSections.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mSections.get(i).boardList.size();
    }

    @Override
    public Object getGroup(int i) {
        return mSections.get(i);
    }

    @Override
    public Object getChild(int i, int i2) {
        return mSections.get(i).boardList.get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i + 100 + i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.section_list_section, null);
        }
        TextView sectionView = (TextView) view;
        sectionView.setText(mSections.get(groupPosition).name);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.section_list_board, null);
        }
        TextView boardView = (TextView) view;
        Board board = mSections.get(groupPosition).boardList.get(childPosition);
        boardView.setText(board.title + " " + board.name);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void onChildClick(int groupPos, int childPos, Context context) {
        Board board = mSections.get(groupPos).boardList.get(childPos);
        if (board.hasSubBoard) {
            showSubBoardsDialog(context, board.subBoardList);
        } else {
            Intent intent = new Intent(context, BoardActivity.class);
            intent.putExtra(BoardActivity.BOARD_TITLE, board.title);
            intent.putExtra(BoardActivity.BOARD_NAME, board.name);
            intent.putExtra(BoardActivity.BOARD_URL, board.url);
            context.startActivity(intent);
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.insert(board);
        }
    }

    private void showSubBoardsDialog(Context context, ArrayList<Board> boards) {
        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.dialog_select_board);
        mDialog.setTitle("请选择版面");
        ListView listView = (ListView) mDialog.findViewById(R.id.board_list);
        mAdapter = new SubboardsAdapter(context, boards);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.onItemClick(mContext, i);
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }
}
