package com.jewelzqiu.sjtubbs.support;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class Post implements Parcelable {

    public String board;

    public String author;

    public String title;

    public String url;

    public Post(String url, String title, String board, String author) {
        this.url = url;
        this.title = title;
        this.board = board;
        this.author = author;
    }

    public Post(Parcel parcel) {
        board = parcel.readString();
        author = parcel.readString();
        title = parcel.readString();
        url = parcel.readString();
    }

    @Override
    public String toString() {
        return title + "\n" + board + ", " + author + "\n" + url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(board);
        parcel.writeString(author);
        parcel.writeString(title);
        parcel.writeString(url);
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel parcel) {
            return new Post(parcel);
        }

        @Override
        public Post[] newArray(int i) {
            return new Post[i];
        }
    };
}
