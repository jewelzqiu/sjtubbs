package com.jewelzqiu.sjtubbs.support;

/**
 * Created by jewelzqiu on 7/2/14.
 */
public class Reply {

    public String userId;

    public String time;

    public String title;

    public String content;

    public Reply(String userId, String time, String title, String content) {
        this.userId = userId;
        this.time = time;
        this.title = title;
        this.content = content;
    }

}
