package com.pwspray.trinitasrooster.Data;


public class Day {
    Period[] periods = new Period[8];

    public Day(){

    }

    public Period getPeriod(int hour){
        return periods[hour];
    }
}
