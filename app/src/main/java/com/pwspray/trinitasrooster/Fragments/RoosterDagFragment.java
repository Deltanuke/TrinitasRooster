package com.pwspray.trinitasrooster.Fragments;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pwspray.trinitasrooster.Data.DayCursorAdapter;
import com.pwspray.trinitasrooster.MainActivity;
import com.pwspray.trinitasrooster.R;

public class RoosterDagFragment extends ListFragment {

    private static final String LOG_TAG = "RoosterDagFragment";
    private static final boolean LOG_D = MainActivity.LOG_D;

    private DayCursorAdapter adapter;
    private RoosterDagFragCallBack mCallback;
    private ListView roosterDagView;
    private String date;

    public interface RoosterDagFragCallBack{

        public void onHourSelected(String date, int hour);
    }

    public RoosterDagFragment(){

    }

    public void setListViewAdapter(DayCursorAdapter adapter){
        this.adapter = adapter;
        if(LOG_D) Log.d(LOG_TAG,"Set the adapter for the ListView");
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (RoosterDagFragCallBack) activity;
            if(LOG_D) Log.d(LOG_TAG,"Interface is correctly implemented");
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RoosterDagFragCallBack");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if(LOG_D) Log.d(LOG_TAG,"ListView choice mode set to single");

        getListView().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view.findViewById(R.id.list_item_period_linear).setBackgroundColor(Color.argb(255,0,0,255));
                if(LOG_D) Log.d(LOG_TAG,"Selection noticed, changed background color of item: "+position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getListView().setItemChecked(0, true);
                if(LOG_D) Log.d(LOG_TAG,"Nothing is selected, selecting item 0");
            }
        });

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity
        if(LOG_D)Log.d(LOG_TAG, "Clicked on item "+position);

        getListView().setItemChecked(position, true);
        if(LOG_D)Log.d(LOG_TAG,"Item set as checked: "+ l.getCheckedItemPosition());
        mCallback.onHourSelected(date,position + 1);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCheckedItem(int position){
        getListView().setItemChecked(position, true);
    }

    public void changeToChecked(){
        int position = getListView().getCheckedItemPosition();
        Object view = getListAdapter().getItem(position);

    }


}