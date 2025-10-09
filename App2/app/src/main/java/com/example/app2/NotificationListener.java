import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;
import android.os.Bundle;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";
    private static final String JSON_FILE_NAME = "notifications.json";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationListener Service Created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Posted: " + sbn.getPackageName());

        // Extract notification details
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();

        // Get notification content
        String title = "";
        String text = "";

        if (notification.extras != null) {
            title = notification.extras.getString(Notification.EXTRA_TITLE, "");
            text = notification.extras.getString(Notification.EXTRA_TEXT, "");
        }

        // Create notification data object
        JSONObject notificationData = new JSONObject();
        try {
            notificationData.put("packageName", packageName);
            notificationData.put("title", title);
            notificationData.put("text", text);
            notificationData.put("timestamp", getCurrentTimestamp());
            notificationData.put("id", sbn.getId());
            notificationData.put("postTime", sbn.getPostTime());

            // Save to JSON file
            saveNotificationToJson(notificationData);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object", e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Removed: " + sbn.getPackageName());
        // You can also log removed notifications if needed
    }

    private void saveNotificationToJson(JSONObject newNotification) {
        try {
            File file = new File(getFilesDir(), JSON_FILE_NAME);
            JSONArray notifications;

            // Read existing notifications from file
            if (file.exists()) {
                StringBuilder jsonString = new StringBuilder();
                FileReader fileReader = new FileReader(file);
                int character;
                while ((character = fileReader.read()) != -1) {
                    jsonString.append((char) character);
                }
                fileReader.close();

                if (jsonString.length() > 0) {
                    notifications = new JSONArray(jsonString.toString());
                } else {
                    notifications = new JSONArray();
                }
            } else {
                notifications = new JSONArray();
            }

            // Add new notification to array
            notifications.put(newNotification);

            // Write updated array back to file
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(notifications.toString());
            fileWriter.close();

            Log.d(TAG, "Notification saved to JSON file. Total notifications: " + notifications.length());

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error saving notification to JSON", e);
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NotificationListener Service Destroyed");
    }
}