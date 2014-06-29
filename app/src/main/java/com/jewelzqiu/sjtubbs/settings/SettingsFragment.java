package com.jewelzqiu.sjtubbs.settings;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ListView;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        Preference preference = findPreference(getString(R.string.key_github));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        (String) preference.getSummary()));
                startActivity(intent);
                return true;
            }
        });

        preference = findPreference(getString(R.string.key_pic_path));
        preference.setSummary(Utils.PIC_STORE_PATH);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Utils.PIC_STORE_PATH);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
//        listView.setFitsSystemWindows(true);
        listView.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            listView.setPadding(
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    config.getPixelInsetTop(true),
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    config.getPixelInsetBottom());
        }
    }
}
