package com.pwspray.trinitasrooster.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pwspray.trinitasrooster.MainActivity;
import com.pwspray.trinitasrooster.Services.DownloadRoosterService;
import com.pwspray.trinitasrooster.Util;

public class RoosterDownloadReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "RoosterDownloadReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean succes = intent.getBooleanExtra(DownloadRoosterService.PARAM_OUT_SUCCES, false);
        Log.d(LOG_TAG, "RoosterDownloadReceiver onReceive() succes?:" + succes);

        context.getApplicationContext().unregisterReceiver(this);

        if(succes){

        } else { //todo retry

        }
    }
}