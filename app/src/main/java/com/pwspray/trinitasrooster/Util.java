package com.pwspray.trinitasrooster;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {
    private static final String LOG_TAG = "UtilClass";

    public static String getDateToDownload(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar today = Calendar.getInstance();
        Calendar downloadDate = Calendar.getInstance();

        if(today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            downloadDate.setTime(getMonday(downloadDate, 1).getTime());
        } else{
            downloadDate.setTime(getMonday(downloadDate).getTime());
        }

        Log.d(LOG_TAG, "getDateToDownload(): " + sdf.format(downloadDate.getTime()));

        return sdf.format(downloadDate.getTime());
    }

    public static String getDateToShow(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar today = Calendar.getInstance();
        Calendar showDate = Calendar.getInstance();

        if(today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            showDate.setTime(getMonday(showDate, 1).getTime());

        Log.d(LOG_TAG, "getDateToShow(): " + sdf.format(showDate.getTime()));

        return sdf.format(showDate.getTime());
    }

    public static String getDateString(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(calendar.getTime());
    }

    public static Calendar getMonday(Calendar calendar){
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            calendar.add(Calendar.DATE, -1);
        }

        return calendar;
    }

    public static Calendar getMonday(Calendar calendar, int weekCorrection){
        if(weekCorrection >0 ) {
            for (int i = 0; i < weekCorrection; i++) {
                calendar.add(Calendar.DATE, 7);
            }
        } else if(weekCorrection < 0){
            for (int i = 0; i > weekCorrection; i--) {
                calendar.add(Calendar.DATE, -7);
            }
        }

        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            calendar.add(Calendar.DATE, -1);
        }

        return calendar;
    }

    public static Calendar getTuesday(Calendar calendar, int weekCorrection){
        if(weekCorrection >0 ) {
            for (int i = 0; i < weekCorrection; i++) {
                calendar.add(Calendar.DATE, 7);
            }
        } else if(weekCorrection < 0){
            for (int i = 0; i > weekCorrection; i--) {
                calendar.add(Calendar.DATE, -7);
            }
        }
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY){
            calendar.add(Calendar.DATE, 1);
        }

        return calendar;
    }

    public static Calendar getWednesday(Calendar calendar, int weekCorrection){
        if(weekCorrection >0 ) {
            for (int i = 0; i < weekCorrection; i++) {
                calendar.add(Calendar.DATE, 7);
            }
        } else if(weekCorrection < 0){
            for (int i = 0; i > weekCorrection; i--) {
                calendar.add(Calendar.DATE, -7);
            }
        }

        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY){
            calendar.add(Calendar.DATE, 1);
        }

        return calendar;
    }

    public static Calendar getThursday(Calendar calendar, int weekCorrection){
        if(weekCorrection >0 ) {
            for (int i = 0; i < weekCorrection; i++) {
                calendar.add(Calendar.DATE, 7);
            }
        } else if(weekCorrection < 0){
            for (int i = 0; i > weekCorrection; i--) {
                calendar.add(Calendar.DATE, -7);
            }
        }

        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY){
            calendar.add(Calendar.DATE, 1);
        }

        return calendar;
    }

    public static Calendar getFriday(Calendar calendar, int weekCorrection){
        if(weekCorrection >0 ) {
            for (int i = 0; i < weekCorrection; i++) {
                calendar.add(Calendar.DATE, 7);
            }
        } else if(weekCorrection < 0){
            for (int i = 0; i > weekCorrection; i--) {
                calendar.add(Calendar.DATE, -7);
            }
        }

        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY){
            calendar.add(Calendar.DATE, 1);
        }

        return calendar;
    }

    public static String getFullSubject(String s){
        if(s == null || s.isEmpty())
            return null;

        if(s.equals("wisB"))return "Wiskunde B";
        else if(s.equals("wisD"))return "Wiskunde D";
        else if(s.equals("wisA"))return "Wiskunde A";
        else if(s.equals("wisC"))return "Wiskunde C";
        else if(s.equals("nat"))return "Natuurkunde";
        else if(s.equals("entl"))return "Engels";
        else if(s.equals("schk"))return "Scheikunde";
        else if(s.equals("dutl"))return "Duits";
        else if(s.equals("netl"))return "Nederlands";
        else if(s.equals("in"))return "Informatica";
        else if(s.equals("lo"))return "Gym";
        else if(s.equals("mu"))return "Muziek";
        else if(s.equals("ak"))return "Aardrijkskunde";
        else if(s.equals("bi"))return "Biologie";
        else if(s.equals("ne"))return "Nederlands";
        else if(s.equals("du"))return "Duits";
        else if(s.equals("fa"))return "Frans";
        else if(s.equals("tn"))return "Techniek";
        else if(s.equals("wi"))return "Wiskunde";
        else if(s.equals("na"))return "Natuurkunde";
        else if(s.equals("en"))return "Engels";
        else if(s.equals("hv"))return "Handvaardigheid";
        else if(s.equals("gs"))return "Geschiedenis";
        else if(s.equals("lv"))return "Levo";
        else if(s.equals("mur"))return "Mentoruur";
        else return s;
    }
}
