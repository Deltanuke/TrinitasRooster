package com.pwspray.trinitasrooster.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.pwspray.trinitasrooster.Services.DownloadRoosterService;
import com.pwspray.trinitasrooster.Services.LoginService;
import com.pwspray.trinitasrooster.Util;

public class LoginReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "LoginReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "LoginReceiver onReceive()");
        String name = intent.getStringExtra(LoginService.PARAM_OUT_NAME);
        boolean doDebug = intent.getBooleanExtra(LoginService.PARAM_OUT_DEBUG, false);

        context.getApplicationContext().unregisterReceiver(this);

        if (name != null && !name.isEmpty()) { //succes
            IntentFilter filter = new IntentFilter(DownloadRoosterService.ACTION_RETURN_DOWNLOAD_ROOSTER);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            RoosterDownloadReceiver roosterDownloadReceiver = new RoosterDownloadReceiver();
            context.getApplicationContext().registerReceiver(roosterDownloadReceiver, filter);

            DownloadRoosterService.startActionDownloadRooster(context, Util.getDateToDownload(), doDebug);
        } else { //TODO try again

        }
    }
}