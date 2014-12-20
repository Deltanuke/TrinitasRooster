package com.pwspray.trinitasrooster.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.pwspray.trinitasrooster.MainActivity;
import com.pwspray.trinitasrooster.Services.BackgroundSync;
import com.pwspray.trinitasrooster.Services.LoginService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "BackgroundSync";

    @Override
    public void onReceive(Context context, Intent intent) {
        String username = intent.getStringExtra(BackgroundSync.PARAM_USERNAME);
        String password = intent.getStringExtra(BackgroundSync.PARAM_PASSWORD);
        boolean doDebug = intent.getBooleanExtra(BackgroundSync.PARAM_DEBUG, false);

        Log.d(LOG_TAG, "AlarmReceiver onReceive()");
        //setUpdateTimeDebug(context);

        IntentFilter filter = new IntentFilter(LoginService.ACTION_RETURN_LOGIN);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        LoginReceiver loginReceiver = new LoginReceiver();
        context.getApplicationContext().registerReceiver(loginReceiver, filter);

        LoginService.startActionLogin(context, username, password, doDebug);
    }

    public static void setUpdateTimeDebug(Context context) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf.format(calendar.getTime());

        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("Debug_" + String.valueOf(calendar.getTimeInMillis()), date);
        editor.commit();
    }

}