package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.R;

import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by jewelzqiu on 7/7/14.
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    Context mContext;

    public MyExceptionHandler(Context context) {
        mContext = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        ex.printStackTrace();
        String log = stringWriter.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, log);
        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.send_log)));
    }
}
