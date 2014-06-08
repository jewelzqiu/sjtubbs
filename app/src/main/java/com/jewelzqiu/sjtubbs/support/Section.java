package com.jewelzqiu.sjtubbs.support;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class Section implements Parcelable {

    public String name;

    public String url;

    public ArrayList<Board> boardList;

    public Section(String secName, String secUrl, ArrayList<Board> list) {
        name = secName;
        url = secUrl;
        boardList = list;
    }

    public Section(Parcel parcel) {
        name = parcel.readString();
        url = parcel.readString();
        boardList = new ArrayList<Board>();
        parcel.readTypedList(boardList, null);
    }

    @Override
    public String toString() {
        return name + ":\n" + boardList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(url);
        parcel.writeTypedList(boardList);
    }

    public static final Creator<Section> CREATOR = new Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel parcel) {
            return new Section(parcel);
        }

        @Override
        public Section[] newArray(int i) {
            return new Section[i];
        }
    };
}
