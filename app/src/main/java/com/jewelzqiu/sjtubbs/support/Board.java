package com.jewelzqiu.sjtubbs.support;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class Board implements Parcelable {

    public String title;

    public String name;

    public String url;

    public boolean hasSubBoard;

    public ArrayList<Board> subBoardList;

    public Board(String boardTitle, String boardName, String boardUrl) {
        title = boardTitle;
        name = boardName;
        url = boardUrl;
    }

    public Board(String boardTitle, String boardName, String boardUrl, ArrayList<Board> boardList) {
        title = boardTitle;
        name = boardName;
        url = boardUrl;
        hasSubBoard = true;
        subBoardList = boardList;
    }

    public void setSubBoardList(ArrayList<Board> boardList) {
        hasSubBoard = true;
        subBoardList = boardList;
    }

    public Board(Parcel parcel) {
        title = parcel.readString();
        name = parcel.readString();
        url = parcel.readString();
        boolean[] booleans = new boolean[1];
        parcel.readBooleanArray(booleans);
        hasSubBoard = booleans[0];
        subBoardList = new ArrayList<Board>();
        parcel.readTypedList(subBoardList, null);
    }

    @Override
    public String toString() {
        return name + (hasSubBoard ? ": " + subBoardList : " ");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(name);
        parcel.writeString(url);
        boolean[] booleans = new boolean[1];
        booleans[0] = hasSubBoard;
        parcel.writeBooleanArray(booleans);
        parcel.writeTypedList(subBoardList);
    }

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel parcel) {
            return new Board(parcel);
        }

        @Override
        public Board[] newArray(int i) {
            return new Board[i];
        }
    };
}
