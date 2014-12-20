package com.pwspray.trinitasrooster.Fragments;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.pwspray.trinitasrooster.Data.DayCursorAdapter;
import com.pwspray.trinitasrooster.Fragments.RoosterDagFragment;
import com.pwspray.trinitasrooster.R;

/**
 * Created by Gebruiker on 13-12-2014.
 */
public class RoosterPageAdapter extends FragmentPagerAdapter {

    private final static int ROOSTER_DAGEN = 5;
    private DayCursorAdapter mondayAdapter;
    private DayCursorAdapter tuesdayAdapter;
    private DayCursorAdapter wednesdayAdapter;
    private DayCursorAdapter thursdayAdapter;
    private DayCursorAdapter fridayAdapter;
    private RoosterDagFragment mondayFragment;
    private RoosterDagFragment tuesdayFragment;
    private RoosterDagFragment wednesdayFragment;
    private RoosterDagFragment thursdayFragment;
    private RoosterDagFragment fridayFragment;

    private Context context;

    public RoosterPageAdapter(FragmentManager fm, Context c) {
        super(fm);
        mondayFragment = new RoosterDagFragment();
        tuesdayFragment = new RoosterDagFragment();
        wednesdayFragment = new RoosterDagFragment();
        thursdayFragment = new RoosterDagFragment();
        fridayFragment = new RoosterDagFragment();

        context = c;
    }

    @Override
    public int getCount() {
        return ROOSTER_DAGEN;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return mondayFragment;
            case 1: return tuesdayFragment;
            case 2: return wednesdayFragment;
            case 3: return thursdayFragment;
            case 4: return fridayFragment;
            default: return mondayFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Resources resources = context.getResources();
        return resources.getStringArray(R.array.week_dagen)[position].toUpperCase();
    }

    public void setAdapters(DayCursorAdapter d1, DayCursorAdapter d2, DayCursorAdapter d3, DayCursorAdapter d4, DayCursorAdapter d5){
        mondayFragment.setListViewAdapter(d1);
        tuesdayFragment.setListViewAdapter(d2);
        wednesdayFragment.setListViewAdapter(d3);
        thursdayFragment.setListViewAdapter(d4);
        fridayFragment.setListViewAdapter(d5);
        mondayAdapter = d1;
        tuesdayAdapter = d2;
        wednesdayAdapter = d3;
        thursdayAdapter = d4;
        fridayAdapter = d5;
    }

    public void setFragmentDates(String d1, String d2, String d3, String d4, String d5){
        mondayFragment.setDate(d1);
        tuesdayFragment.setDate(d2);
        wednesdayFragment.setDate(d3);
        thursdayFragment.setDate(d4);
        fridayFragment.setDate(d5);
    }
}
