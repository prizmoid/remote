package com.prizmoid.setup_ble.ui.btdevicecontrol;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.prizmoid.setup_ble.R;
import com.prizmoid.setup_ble.ui.fragments.AboutFragment;
import com.prizmoid.setup_ble.ui.fragments.ConfigFragment;
import com.prizmoid.setup_ble.ui.fragments.DashFragment;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]
            {R.string.section_dash, R.string.section_config,R.string.section_about};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;

    }



    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch (position) {
            case 0:
                return DashFragment.newInstance(position);
            case 1:
                return ConfigFragment.newInstance(position);
            case 2:
                return AboutFragment.newInstance(position);
        }
        //return PlaceholderFragment.newInstance(position + 1);
        return AboutFragment.newInstance(2);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}