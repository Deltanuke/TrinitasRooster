package com.pwspray.trinitasrooster.Fragments;

import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pwspray.trinitasrooster.R;
import com.pwspray.trinitasrooster.Util;

public class RoosterDetailsFragment extends Fragment {

    private TextView hour;
    private TextView teacher;
    private TextView teacherShort;
    private TextView room;
    private TextView subject;
    private TextView homework;
    private TextView title;
    private int stateI;
    private String hourS;
    private String subjectS;
    private String teacherS;
    private String teacherShortS;
    private String roomS;
    private String homeworkS;
    private String[] data;

    public RoosterDetailsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(subjectS == null){
            preloadContentData(data);
        }

        if(data ==  null){
            View detailsView = inflater.inflate(R.layout.rooster_details_fragment_free, container, false);
            return detailsView;
        }
        if(hourS == null){
            //View detailsView = inflater.inflate(R.layout.rooster_details_fragment_out, container, false);
            //return detailsView;
        }
        View dagView = inflater.inflate(R.layout.rooster_details_fragment, container, false);

        hour = (TextView) dagView.findViewById(R.id.uurTextView);
        teacher = (TextView) dagView.findViewById(R.id.leraarTextView);
        teacherShort = (TextView) dagView.findViewById(R.id.leraarKortTextView);
        room = (TextView) dagView.findViewById(R.id.lokaalTextView);
        subject = (TextView) dagView.findViewById(R.id.vakTextView);
        homework = (TextView) dagView.findViewById(R.id.huiswerkTextView);
        title = (TextView) dagView.findViewById(R.id.titelTextView);



        if(hourS != null)
            hour.setText(hourS);
        if(subjectS != null){
            subject.setText(Util.getFullSubject(subjectS));
            title.setText(Util.getFullSubject(subjectS));
        }
        if(roomS != null)
            room.setText(roomS);
        if(teacherS != null)
            teacher.setText(teacherS);
        if(teacherShortS != null)
            teacherShort.setText(teacherShortS);
        if(homeworkS != null) {
            if(!homeworkS.isEmpty())
                homework.setText(Html.fromHtml(homeworkS));
            else
                homework.setText(homeworkS);
        }

        return dagView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //TODO code insertion data van listview
    public void setContentData(String[] data){
        this.hour.setText(data[0]);
        this.subject.setText(data[1]);
        this.teacher.setText(data[2]);
        this.room.setText(data[3]);
        this.teacherShort.setText(data[4]);
        this.homework.setText(data[5]);
    }

    public void preloadContentData(String[] data){
        this.hourS = data[0];
        this.subjectS = data[1];
        this.teacherS = data[2];
        this.roomS = data[3];
        this.teacherShortS = data[4];
        this.homeworkS = data[5];
    }

    public void preloadContentData(){
        preloadContentData(data);
    }

    public void setStringArrayData(String[] data){
        this.data = data;
    }

    public boolean isFree(){
        if(subjectS == null)
            return true;
        else
            return false;
    }
}