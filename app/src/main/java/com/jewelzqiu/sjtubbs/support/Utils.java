package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by jewelzqiu on 6/7/14.
 */
public class Utils {

    public static final String TYPE_TOP_TEN = "table.Bg_Color_Midium table:contains(十大热门话题)";

    public static final String BBS_BASE_URL = "https://bbs.sjtu.edu.cn";

    public static String PIC_STORE_PATH;

    public static String PIC_CACHE_PATH;

    public static String CURRENT_BOARD;

    public static final String COOKIE_UTMPKEY = "utmpkey";

    public static final String COOKIE_UTMPNUM = "utmpnum";

    public static final String COOKIE_UTMPUSERID = "utmpuserid";

    public static String USER_ID = null;

    public static HashMap<String, String> cookies = new HashMap<String, String>(3);

    public static String getCookies() {
        return COOKIE_UTMPNUM + "=" + cookies.get(COOKIE_UTMPNUM) + " ;" +
                COOKIE_UTMPKEY + "=" + cookies.get(COOKIE_UTMPKEY) + " ;" +
                COOKIE_UTMPUSERID + "=" + cookies.get(COOKIE_UTMPUSERID);
    }

    public static void setInsets(Activity activity, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        view.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                config.getPixelInsetBottom());
    }

    public static boolean isMarkReadEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences == null) {
            return true;
        }
        return preferences.getBoolean(context.getString(R.string.key_mark_read), true);
    }

    public static void setSexColor(TextView view, String userId) {
        if (userId == null || userId.equals("")) {
            return;
        }
        new SetSexTask(view).execute(userId);
    }

    private static class SetSexTask extends AsyncTask<String, Void, Integer> {

        TextView view;

        public SetSexTask(TextView textView) {
            view = textView;
        }

        @Override
        protected Integer doInBackground(String... params) {
            DatabaseHelper dbHelper = new DatabaseHelper(view.getContext());
            try {
                return dbHelper.getUserSexColor(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return android.R.color.black;
            }
        }

        @Override
        protected void onPostExecute(Integer color) {
            view.setTextColor(view.getResources().getColor(color));
        }
    }

    public static void login(final Context context, final OnLoginLogoutListener listener) {
        View loginView = LayoutInflater.from(context).inflate(R.layout.dialog_login, null);

        final EditText usernameView = (EditText) loginView.findViewById(R.id.text_username);
        final EditText passwdView = (EditText) loginView.findViewById(R.id.text_password);
        final Switch rememberSwitch = (Switch) loginView.findViewById(R.id.switch_remember);

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean remember = preferences.getBoolean(context.getString(R.string.key_remember), true);
        rememberSwitch.setChecked(remember);
        if (remember) {
            String username = preferences.getString(context.getString(R.string.key_username), "");
            String password = preferences.getString(context.getString(R.string.key_password), "");
            usernameView.setText(username);
            passwdView.setText(password);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(loginView);
        builder.setTitle(R.string.login);
        builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                Editable text = usernameView.getText();
                if (text == null || text.length() == 0) {
                    Toast.makeText(context, R.string.username_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = text.toString();

                text = passwdView.getText();
                if (text == null || text.length() == 0) {
                    Toast.makeText(context, R.string.password_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = text.toString();

                new LoginTask(listener).execute(BBS_BASE_URL + "/bbswaplogin", username, password);

                boolean remember = rememberSwitch.isChecked();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(context.getString(R.string.key_remember), remember);
                editor.putString(context.getString(R.string.key_username), username);
                editor.putString(context.getString(R.string.key_password), password);
                editor.apply();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void logout(OnLoginLogoutListener listener) {
        USER_ID = null;
        cookies.clear();
        listener.onLoginLogout();
    }

    public static void autoLogin(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(context.getString(R.string.key_username), "");
        String password = preferences.getString(context.getString(R.string.key_password), "");
        if (username == null || username.length() == 0 || password == null
                || password.length() == 0) {
            return;
        }
        new LoginTask(null).execute(BBS_BASE_URL + "/bbswaplogin", username, password);
    }

    private static class LoginTask extends AsyncTask<String, Void, Void> {

        OnLoginLogoutListener mListener;

        public LoginTask(OnLoginLogoutListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Connection.Response response = Jsoup.connect(params[0])
                        .data("id", params[1], "pw", params[2])
                        .method(Connection.Method.POST)
                        .execute();

                USER_ID = response.cookie(COOKIE_UTMPUSERID);
                cookies.clear();
                cookies.put(COOKIE_UTMPKEY, response.cookie(COOKIE_UTMPKEY));
                cookies.put(COOKIE_UTMPNUM, response.cookie(COOKIE_UTMPNUM));
                cookies.put(COOKIE_UTMPUSERID, USER_ID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mListener != null) {
                mListener.onLoginLogout();
            }
        }
    }

    public static boolean isAutoLoginEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.key_auto_login), true);
    }

    public interface OnLoginLogoutListener {

        public void onLoginLogout();
    }
}
