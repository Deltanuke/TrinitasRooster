package com.pwspray.trinitasrooster.Data;

  /*
    Available states for state:
    0 = normaal
    1 = notified
    */

public class Period {
    private int state;
    private int hour;
    private long start;
    private long end;
    private String classroom;
    private String homework;
    private String subject;
    private String teacherFull;
    private String teacherShort;
    private String classname;
    private String date;

    public Period(int _hour, String _classname, String _classroom, String _subject, String _teacherFull, String _teacherShort, String _date, String _homework, long _start, long _end, int _state){
        hour = _hour;
        classname = _classname;
        classroom = _classroom;
        subject = _subject;
        teacherFull = _teacherFull;
        teacherShort = _teacherShort;
        date = _date;
        homework = _homework;
        start = _start;
        end = _end;
        state = _state;
    }

    public Period(int _hour){
        hour = _hour;
        classname = null;
        classroom = null;
        subject = null;
        teacherFull = null;
        teacherShort = null;
        date = null;
        homework = null;
        start = 0;
        end = 0;
        state = 15;
    }

    public String getTeacherFull(){
        return teacherFull;
    }
    public String getTeacherShort(){
        return teacherShort;
    }
    public String getSubject(){
        return subject;
    }
    public String getClassName(){
        return classname;
    }
    public String getClassroom(){
        return classroom;
    }
    public int getState(){
        return state;
    }
    public int getHour(){
        return hour;
    }
    public long getStart(){
        return start;
    }
    public long getEnd(){
        return end;
    }
    public String getHomework(){
        return homework;
    }
    public String getDate(){
        return date;
    }

}
