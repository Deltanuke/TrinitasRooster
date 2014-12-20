package com.pwspray.trinitasrooster.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Gebruiker on 13-12-2014.
 */
public class DetailsPageAdapter extends FragmentPagerAdapter {

    private int periods_today;
    private RoosterDetailsFragment period1;
    private RoosterDetailsFragment period2;
    private RoosterDetailsFragment period3;
    private RoosterDetailsFragment period4;
    private RoosterDetailsFragment period5;
    private RoosterDetailsFragment period6;
    private RoosterDetailsFragment period7;
    private RoosterDetailsFragment period8;

    public DetailsPageAdapter(FragmentManager fm) {
        super(fm);
        period1 = new RoosterDetailsFragment();
        period2 = new RoosterDetailsFragment();
        period3 = new RoosterDetailsFragment();
        period4 = new RoosterDetailsFragment();
        period5 = new RoosterDetailsFragment();
        period6 = new RoosterDetailsFragment();
        period7 = new RoosterDetailsFragment();
        period8 = new RoosterDetailsFragment();
        periods_today = 8;
    }

    @Override
    public int getCount() {
        return periods_today;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return period1;
            case 1: return period2;
            case 2: return period3;
            case 3: return period4;
            case 4: return period5;
            case 5: return period6;
            case 6: return period7;
            case 7: return period8;
            default: return period1;
        }
    }

    public void setDetailsContentData(String[] h1, String[] h2, String[] h3, String[] h4, String[] h5, String[] h6, String[] h7, String[] h8){
        period1.setStringArrayData(h1);
        period2.setStringArrayData(h2);
        period3.setStringArrayData(h3);
        period4.setStringArrayData(h4);
        period5.setStringArrayData(h5);
        period6.setStringArrayData(h6);
        period7.setStringArrayData(h7);
        period8.setStringArrayData(h8);
    }

    public void setDetailsContentData(Bundle data){
        period1.setStringArrayData(data.getStringArray("period0"));
        period2.setStringArrayData(data.getStringArray("period1"));
        period3.setStringArrayData(data.getStringArray("period2"));
        period4.setStringArrayData(data.getStringArray("period3"));
        period5.setStringArrayData(data.getStringArray("period4"));
        period6.setStringArrayData(data.getStringArray("period5"));
        period7.setStringArrayData(data.getStringArray("period6"));
        period8.setStringArrayData(data.getStringArray("period7"));
        period1.preloadContentData();
        period2.preloadContentData();
        period3.preloadContentData();
        period4.preloadContentData();
        period5.preloadContentData();
        period6.preloadContentData();
        period7.preloadContentData();
        period8.preloadContentData();
        if(period8.isFree()){
            periods_today--;
            if(period7.isFree()){
                periods_today--;
                if(period6.isFree()){
                    periods_today--;
                    if(period5.isFree()){
                        periods_today--;
                        if(period4.isFree()){
                            periods_today--;
                            if(period3.isFree()){
                                periods_today--;
                                if(period2.isFree()){
                                    periods_today--;
                                    if(period1.isFree()){
                                        periods_today--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



}
