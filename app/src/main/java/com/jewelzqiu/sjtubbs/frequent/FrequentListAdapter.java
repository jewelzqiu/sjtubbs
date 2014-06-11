package com.jewelzqiu.sjtubbs.frequent;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.sections.BoardActivity;
import com.jewelzqiu.sjtubbs.support.Board;
import com.jewelzqiu.sjtubbs.support.DatabaseHelper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by jewelzqiu on 6/11/14.
 */
public class FrequentListAdapter extends CursorAdapter {

    public FrequentListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.frequent_list_item, null);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        final String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_TITLE));
        final String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_NAME));
        final String url = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_URL));
        textView.setText(title + " " + name);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BoardActivity.class);
                intent.putExtra(BoardActivity.BOARD_TITLE, title);
                intent.putExtra(BoardActivity.BOARD_NAME, name);
                intent.putExtra(BoardActivity.BOARD_URL, url);
                context.startActivity(intent);
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                Board board = new Board(title, name, url);
                dbHelper.insert(board);
            }
        });
    }
}
