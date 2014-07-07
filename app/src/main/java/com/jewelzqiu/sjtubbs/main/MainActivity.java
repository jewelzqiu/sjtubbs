package com.jewelzqiu.sjtubbs.main;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.frequent.FrequentFragment;
import com.jewelzqiu.sjtubbs.sections.SectionsFragment;
import com.jewelzqiu.sjtubbs.settings.SettingsFragment;
import com.jewelzqiu.sjtubbs.support.MyExceptionHandler;
import com.jewelzqiu.sjtubbs.topten.TopTenFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks/*, OnSectionsGetListener*/ {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public static String[] drawerListTitles;

    private Fragment mFragment;

    private TopTenFragment mTopTenFragment;

    private FrequentFragment mFrequentFragment;

    private SectionsFragment mSectionsFragment;

    private SettingsFragment mSettingsFragment;

    private int mCurrentItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerListTitles = getResources().getStringArray(R.array.drawer_list_title);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //new GetSectionsTask(this).execute();

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mCurrentItem = position;
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                if (mTopTenFragment == null) {
                    mTopTenFragment = new TopTenFragment();
                }
                mFragment = mTopTenFragment;
                break;
            case 1:
                if (mFrequentFragment == null) {
                    mFrequentFragment = new FrequentFragment();
                }
                mFragment = mFrequentFragment;
                break;
            case 2:
                if (mSectionsFragment == null) {
                    mSectionsFragment = new SectionsFragment();
                }
                mFragment = mSectionsFragment;
                break;
            case 3:
                if (mSettingsFragment == null) {
                    mSettingsFragment = new SettingsFragment();
                }
                mFragment = mSettingsFragment;
                break;
        }
        mTitle = drawerListTitles[position];
        setTitle(mTitle);
        if (mFragment != null) {
            fragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        if (mFragment == mSectionsFragment) {
            if (mNavigationDrawerFragment.isDrawerOpen()) {
                actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_USE_LOGO
                                | ActionBar.DISPLAY_SHOW_TITLE
                                | ActionBar.DISPLAY_SHOW_HOME
                                | ActionBar.DISPLAY_HOME_AS_UP
                );
            } else {
                actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_SHOW_CUSTOM
                                | ActionBar.DISPLAY_USE_LOGO
                                | ActionBar.DISPLAY_SHOW_HOME
                                | ActionBar.DISPLAY_HOME_AS_UP
                );
                mSectionsFragment.resetActionBar();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mTitle = getString(R.string.app_name);
        } else {
            mTitle = drawerListTitles[mCurrentItem];
        }
        restoreActionBar();
        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public void onSectionsGet(ArrayList<Section> list) {
//        BBSApplication.sectionList = list;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mNavigationDrawerFragment.isDrawerOpen()) {
                mNavigationDrawerFragment.closeDrawer();
            } else {
                mNavigationDrawerFragment.openDrawer();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFragment == mSectionsFragment && mSectionsFragment.isSearching()) {
            mSectionsFragment.resetActionBar();
            return;
        }
        super.onBackPressed();
    }
}
