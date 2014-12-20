package com.pwspray.trinitasrooster.Data;

public class PeriodObject {
    public String teacherShort;
    public String classroom;
    public String className;
    public String homework;
    public int state;

    public PeriodObject(String teacherShort, String classroom, String className, String homework, int state){
        this.teacherShort = teacherShort;
        this.classroom = classroom;
        this.className = className;
        this.homework = homework;
        this.state = state;
    }
}