package com.pwspray.trinitasrooster.Data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pwspray.trinitasrooster.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RoosterDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "trinitasrooster.db";
    public static final String TABLE_CLASSES_NAME = "classes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SUBJECT_OF_CLASS = "subject";
    public static final String COLUMN_DATE_OF_CLASS = "date";
    public static final String COLUMN_HOUR_OF_CLASS = "hour";
    public static final String COLUMN_START_UNIX = "start";
    public static final String COLUMN_END_UNIX = "end";
    public static final String COLUMN_HOMEWORK = "homework";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_TEACHER_FULL = "teacher";
    public static final String COLUMN_TEACHER_SHORT = "teacherShort";
    public static final String COLUMN_NAME_OF_CLASS = "classname";
    public static final String COLUMN_CLASSROOM = "classroom";

    public RoosterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES_NAME + " (" +
                COLUMN_ID + " integer primary key autoincrement, " +
                COLUMN_DATE_OF_CLASS + " string not null, " +
                COLUMN_HOUR_OF_CLASS + " integer not null, " +
                COLUMN_START_UNIX + " real null, " +
                COLUMN_END_UNIX + " real null, " +
                COLUMN_SUBJECT_OF_CLASS + " string null, " +
                COLUMN_TEACHER_FULL + " string null, " +
                COLUMN_TEACHER_SHORT + " string null, " +
                COLUMN_CLASSROOM + " string null, " +
                COLUMN_NAME_OF_CLASS + " string null, " +
                COLUMN_HOMEWORK + " string null, " + //huiswerk altijd toevoegen als "" ! (GEEN NULL)
                COLUMN_STATE + " integer not null)";

        db.execSQL(SQL_CREATE_CLASSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //todo
        final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_CLASSES_NAME; //todo verwijder ook preferences van weken
        db.execSQL(SQL_DROP_TABLE);
        this.onCreate(db);
    }

    public PeriodObject getPeriodByDateAndHourObject(String date, int hour){
        SQLiteDatabase db = this.getWritableDatabase();

        final String columns[] = {
                COLUMN_TEACHER_SHORT, COLUMN_CLASSROOM, COLUMN_NAME_OF_CLASS, COLUMN_HOMEWORK, COLUMN_STATE
        };

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};

        Cursor cursor = db.query(TABLE_CLASSES_NAME, columns, whereClause, whereArgs, null, null, null, null);

        boolean fail = false;
        PeriodObject periodObject = null;

        if(cursor.moveToFirst()) {
            for (int i = 0; i <= 4; i++)
                if (cursor.isNull(i))
                    fail = true;

            if(!fail)
                periodObject = new PeriodObject(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
        }

        cursor.close();
        db.close();
        return periodObject;
    }

    public int getStateByDateAndHour(String date, int hour){
        SQLiteDatabase db = this.getWritableDatabase();

        final String columns[] = {
                COLUMN_STATE
        };

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};

        Cursor cursor = db.query(TABLE_CLASSES_NAME, columns, whereClause, whereArgs, null, null, null, null);

        if(cursor.moveToFirst()){
            cursor.close();
            db.close();
            return cursor.getInt(0);
        } else {
            cursor.close();
            db.close();
            return -1;
        }
    }

    public Cursor getPeriodsByDate(String date){
        SQLiteDatabase db = this.getWritableDatabase();

        final String columns[] = {
                COLUMN_ID, COLUMN_HOUR_OF_CLASS, COLUMN_SUBJECT_OF_CLASS, COLUMN_TEACHER_FULL, COLUMN_CLASSROOM, COLUMN_TEACHER_SHORT, COLUMN_HOMEWORK, COLUMN_DATE_OF_CLASS
        };

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ?";
        final String whereArgs[] = {date};
        final String orderBy = COLUMN_HOUR_OF_CLASS + " ASC";

        Cursor cursor = db.query(TABLE_CLASSES_NAME, columns, whereClause, whereArgs, null, null, orderBy, null);

        return cursor;
    }

    public Boolean writePeriod(int lesuur, String lesgroep, String lokaal, String docentAfkorting, String docentAchternaam, String huiswerkOmschrijving, int state, String datum, Long start, Long end){
        SQLiteDatabase db = this.getWritableDatabase();

        String vak;

        if(lesgroep != null)
            vak = lesgroep.substring(3);
        else
            vak = null;

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE_OF_CLASS, datum);
        values.put(COLUMN_HOUR_OF_CLASS, lesuur);
        values.put(COLUMN_START_UNIX, start);
        values.put(COLUMN_END_UNIX, end);
        values.put(COLUMN_SUBJECT_OF_CLASS, vak);
        values.put(COLUMN_TEACHER_FULL, docentAchternaam);
        values.put(COLUMN_TEACHER_SHORT, docentAfkorting);
        values.put(COLUMN_CLASSROOM, lokaal);
        values.put(COLUMN_NAME_OF_CLASS, lesgroep);
        values.put(COLUMN_HOMEWORK, huiswerkOmschrijving);
        values.put(COLUMN_STATE, state);

        long rowId = db.insert(TABLE_CLASSES_NAME, null, values);
        db.close();
        if(rowId == -1)
            return false;
        else
            return true;
    }

    public Boolean deletePeriod(String date, int hour){
        SQLiteDatabase db = this.getWritableDatabase();

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};
        int rowsDeleted = db.delete(TABLE_CLASSES_NAME, whereClause, whereArgs);
        db.close();
        if(rowsDeleted == 0)
            return false;
        else
            return true;
    }

    public boolean updatePeriod(String date, int hour, String teacherShort, String teacherFull, String classroom, String classname, String homework){
        SQLiteDatabase db = this.getWritableDatabase();

        String vak = classname.substring(3);

        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBJECT_OF_CLASS, vak);
        values.put(COLUMN_TEACHER_FULL, teacherFull);
        values.put(COLUMN_TEACHER_SHORT, teacherShort);
        values.put(COLUMN_CLASSROOM, classroom);
        values.put(COLUMN_NAME_OF_CLASS, classname);
        values.put(COLUMN_HOMEWORK, homework);
        //values.put(COLUMN_STATE, state);

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};

        int rowsUpdated = db.update(TABLE_CLASSES_NAME, values, whereClause, whereArgs);
        db.close();
        if(rowsUpdated == 0)
            return false;
        else
            return true;
    }

    public boolean updatePeriod(String date, int hour, String teacherShort, String teacherFull, String classroom, String classname, String homework, int state){
        SQLiteDatabase db = this.getWritableDatabase();

        String vak = classname.substring(3);

        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBJECT_OF_CLASS, vak);
        values.put(COLUMN_TEACHER_FULL, teacherFull);
        values.put(COLUMN_TEACHER_SHORT, teacherShort);
        values.put(COLUMN_CLASSROOM, classroom);
        values.put(COLUMN_NAME_OF_CLASS, classname);
        values.put(COLUMN_HOMEWORK, homework);
        values.put(COLUMN_STATE, state);

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};

        int rowsUpdated = db.update(TABLE_CLASSES_NAME, values, whereClause, whereArgs);
        db.close();
        if(rowsUpdated == 0)
            return false;
        else
            return true;
    }

    public Boolean updatePeriod(String datum, int lesuur, String lesgroep, String lokaal, String docentAfkorting, String docentAchternaam, String huiswerkOmschrijving, int state, Long start, Long end){
        SQLiteDatabase db = this.getWritableDatabase();

        String vak = lesgroep.substring(3);

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE_OF_CLASS, datum);
        values.put(COLUMN_HOUR_OF_CLASS, lesuur);
        values.put(COLUMN_START_UNIX, start);
        values.put(COLUMN_END_UNIX, end);
        values.put(COLUMN_SUBJECT_OF_CLASS, vak);
        values.put(COLUMN_TEACHER_FULL, docentAchternaam);
        values.put(COLUMN_TEACHER_SHORT, docentAfkorting);
        values.put(COLUMN_CLASSROOM, lokaal);
        values.put(COLUMN_NAME_OF_CLASS, lesgroep);
        values.put(COLUMN_HOMEWORK, huiswerkOmschrijving);
        values.put(COLUMN_STATE, state);

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {datum, String.valueOf(lesuur)};

        int rowsUpdated = db.update(TABLE_CLASSES_NAME, values, whereClause, whereArgs);
        db.close();
        if(rowsUpdated == 0)
            return false;
        else
            return true;
    }

    public boolean updatePeriodState(String date, int hour, int state){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STATE, state);

        final String whereClause = COLUMN_DATE_OF_CLASS + "= ? AND " + COLUMN_HOUR_OF_CLASS + " = ?";
        final String whereArgs[] = {date, String.valueOf(hour)};

        int rowsUpdated = db.update(TABLE_CLASSES_NAME, values, whereClause, whereArgs);
        db.close();
        if(rowsUpdated == 0)
            return false;
        else
            return true;
    }

    public boolean resetStates(){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STATE, 0);

        int rowsUpdated = db.update(TABLE_CLASSES_NAME, values, null, null);
        db.close();
        if(rowsUpdated == 0)
            return false;
        else
            return true;
    }

    public static boolean hasWeek(Context context, Calendar calendar) {
        calendar = getMonday(calendar);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(calendar.getTime());

        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_FILE_NAME, 0);
        Boolean hasWeek = settings.getBoolean("Database_" + date, false);
        return hasWeek;
    }

    public static void setHasWeek(Context context, Calendar calendar, boolean hasWeek) {
        calendar = getMonday(calendar);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(calendar.getTime());

        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        Log.d("RoosterDbHelper", "setHasWeek() - datum: " + date);

        editor.putBoolean("Database_" + date, hasWeek);
        editor.commit();
    }

    public static void setHasWeek(Context context, Calendar calendar){
        setHasWeek(context, calendar, true);
    }

    public static Calendar getMonday(Calendar calendar){
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            calendar.add(Calendar.DATE, -1);
        }

        return calendar;
    }


    public String[][] cursorToStringArray(Cursor c){
        int i =0;
        String[][] data = new String[8][6];
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            data[i][0] = String.valueOf(c.getInt(1));
            data[i][1] = c.getString(2);
            data[i][2] = c.getString(3);
            data[i][3] = c.getString(4);
            data[i][4] = c.getString(5);
            data[i][5] = c.getString(6);
            i++;
        }
        return data;
    }
}