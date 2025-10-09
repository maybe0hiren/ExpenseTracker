package com.example.app2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStartReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoStartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            Log.d(TAG, "Starting notification service on boot/update");

            // Start the foreground service
            Intent serviceIntent = new Intent(context, ForegroundNotificationService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}