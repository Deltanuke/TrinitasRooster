package com.pwspray.trinitasrooster.Data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pwspray.trinitasrooster.R;
import com.pwspray.trinitasrooster.Util;

public class DayCursorAdapter extends CursorAdapter{
    private LayoutInflater inflater;

    public static class ViewHolder {
        public final TextView hourView;
        public final TextView subjectView;
        public final TextView teacherView;
        public final TextView classroomView;
        public final TextView homeworkView;
        public final TextView teacherShortView;
        public final ImageView homeworkImageView;


        public ViewHolder(View view) {
            hourView = (TextView) view.findViewById(R.id.period_hour);
            subjectView = (TextView) view.findViewById(R.id.period_subject);
            teacherView = (TextView) view.findViewById(R.id.period_teacher);
            classroomView = (TextView) view.findViewById(R.id.period_classroom);
            homeworkView = (TextView) view.findViewById(R.id.period_homework);
            teacherShortView = (TextView) view.findViewById(R.id.period_teacher_short);
            homeworkImageView = (ImageView) view.findViewById(R.id.period_homework_image);
        }
    }

    public DayCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        //inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.list_item_period;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int hour = cursor.getInt(1);
        String subject = cursor.getString(2);
        String teacher = cursor.getString(3);
        String classroom = cursor.getString(4);
        String teacherShort = cursor.getString(5);
        String homework = cursor.getString(6);
        if(subject != null)
            subject = Util.getFullSubject(subject);

        viewHolder.hourView.setText(String.valueOf(hour));
        viewHolder.subjectView.setText(subject);
        viewHolder.teacherShortView.setText(teacherShort);
        viewHolder.classroomView.setText(classroom);
        viewHolder.homeworkView.setText(homework);
        viewHolder.teacherView.setText(teacher);

        if(homework != null && !homework.isEmpty()){
            viewHolder.homeworkImageView.setVisibility(View.VISIBLE);
        }
    }
}
