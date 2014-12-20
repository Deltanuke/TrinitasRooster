package com.pwspray.trinitasrooster.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.pwspray.trinitasrooster.MainActivity;
import com.pwspray.trinitasrooster.Services.BackgroundSync;

public class BootReceiver extends BroadcastReceiver{
    public static BackgroundSync backgroundSync = new BackgroundSync();
    private static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(LOG_TAG, "BootReceiver onReceive()");
            SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_FILE_NAME, 0);
            String username = settings.getString(MainActivity.PREFS_USERNAME, null);
            String password = settings.getString(MainActivity.PREFS_PASSWORD, null);


            if (username != null && password != null) {
                backgroundSync.cancel(context);
                backgroundSync.startSync(context, username, password);
            }
        }
    }
}
