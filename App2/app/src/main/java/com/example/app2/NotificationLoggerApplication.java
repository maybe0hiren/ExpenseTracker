package com.example.app2;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class NotificationLoggerApplication extends Application {

    private static final String TAG = "NotificationLoggerApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application started");

        // Start the foreground service when app is created
        // This ensures the service runs even if the app is not in foreground
        startNotificationService();
    }

    private void startNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, ForegroundNotificationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "Notification service started from Application");
        } catch (Exception e) {
            Log.e(TAG, "Error starting notification service from Application", e);
        }
    }
}
