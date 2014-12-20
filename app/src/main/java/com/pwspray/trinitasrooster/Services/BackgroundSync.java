package com.pwspray.trinitasrooster.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pwspray.trinitasrooster.Receivers.AlarmReceiver;

public class BackgroundSync {
    private static final String LOG_TAG = "BackgroundSync";

    public static final String PARAM_USERNAME = "com.pwspray.trinitasrooster.extra.USERNAME";
    public static final String PARAM_PASSWORD = "com.pwspray.trinitasrooster.extra.PASSWORD";
    public static final String PARAM_DEBUG = "com.pwspray.trinitasrooster.extra.DEBUG";
    public static final String ACTION_ALARM = "com.pwspray.trinitasrooster.action.ALARM";

    public PendingIntent pendingIntent;

    //public long lastSync = 0;
    //public long secondLastSync = 0;
    //public String lastUsername;
    //public String lastPassword;

    public BackgroundSync(){

    }

    public void startSync(Context context, String username, String password){
        Log.d(LOG_TAG, "startSync()");

        //secondLastSync = lastSync;
        //lastSync = System.currentTimeMillis();
        //lastUsername = username;
        //lastPassword = password;

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtra(PARAM_USERNAME, username);
        intent.putExtra(PARAM_PASSWORD, password);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 1, AlarmManager.INTERVAL_HOUR, pendingIntent); //setInexactRepeating , AlarmManager.INTERVAL_HOUR
    }

    /*public void onSyncFailed(Context context){
        final int SECOND = 1000;
        final int MINUTE = 60 * SECOND;
        final int HALF_HOUR = 30 * MINUTE;

        if(lastUsername != null && !lastUsername.isEmpty() && lastPassword != null && !lastPassword.isEmpty()){
            long currentTime = System.currentTimeMillis();
            long differenceLast = currentTime - lastSync;
            long differenceSecondLast = lastSync - secondLastSync;

            if(lastSync == 0 || differenceLast > HALF_HOUR){
                syncImmediately(context, lastUsername, lastPassword);
            }
        }
    }*/

    public void syncImmediately(Context context, String username, String password){
        Log.d(LOG_TAG, "syncImmediately()");

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtra(PARAM_USERNAME, username);
        intent.putExtra(PARAM_PASSWORD, password);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        try{
            pi.send();
            //secondLastSync = lastSync;
            //lastSync = System.currentTimeMillis();
        } catch (Exception e){
            Log.e(LOG_TAG, "syncImmediately() PendingIntent niet verzonden", e);
        }
    }
    public void syncImmediately(Context context, String username, String password, boolean doDebug){
        Log.d(LOG_TAG, "syncImmediately() DEBUG");

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtra(PARAM_USERNAME, username);
        intent.putExtra(PARAM_PASSWORD, password);
        intent.putExtra(PARAM_DEBUG, doDebug);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        try{
            pi.send();
            //secondLastSync = lastSync;
            //lastSync = System.currentTimeMillis();
        } catch (Exception e){
            Log.e(LOG_TAG, "syncImmediately() PendingIntent niet verzonden", e);
        }
    }


    public void cancel(Context context){
        if(pendingIntent != null){
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            try{
                alarmManager.cancel(pendingIntent);
            } catch (Exception e){
              Log.e(LOG_TAG, "cancel() exception ", e);
            }
        }
    }
}
