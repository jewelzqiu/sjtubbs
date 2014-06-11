package com.jewelzqiu.sjtubbs.support;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jewelzqiu on 6/11/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int CURRENT_VERSION = 1;

    private static final String DB_NAME = "db_bbs";

    private static final String TB_FREQUENT = "frequent";

    public static final String COL_TITLE = "title";

    public static final String COL_NAME = "_id";

    public static final String COL_URL = "url";

    public static final String COL_COUNT = "count";

    public static final String COL_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TB_FREQUENT + "("
                + COL_NAME + " VARCHAR PRIMARY KEY, "
                + COL_TITLE + " VARCHAR, "
                + COL_URL + " VARCHAR, "
                + COL_COUNT + " INTEGER, "
                + COL_TIME + " LONG"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(Board board) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + TB_FREQUENT + " WHERE "
                + COL_NAME + "=\"" + board.name + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() == 0) {
            sql = "INSERT INTO " + TB_FREQUENT + " VALUES (\""
                    + board.name + "\", \"" + board.title + "\", \"" + board.url + "\", "
                    + 1 + ", " + System.currentTimeMillis() + ")";
        } else {
            sql = "UPDATE " + TB_FREQUENT + " SET "
                    + COL_COUNT + "=" + COL_COUNT + "+1, "
                    + COL_TIME + "=" + System.currentTimeMillis()
                    + " WHERE " + COL_NAME + "=\"" + board.name + "\"";
        }
        db.execSQL(sql);
        db.close();
    }

    public Cursor query() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TB_FREQUENT + " ORDER BY "
                + COL_COUNT + " DESC, " + COL_TIME + " ASC LIMIT 10";
        return db.rawQuery(sql, null);
    }
}
