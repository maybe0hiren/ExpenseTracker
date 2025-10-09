import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONHelper {

    private static final String TAG = "JSONHelper";
    private static final String JSON_FILE_NAME = "notifications.json";

    public static List<JSONObject> readAllNotifications(Context context) {
        List<JSONObject> notificationList = new ArrayList<>();

        try {
            File file = new File(context.getFilesDir(), JSON_FILE_NAME);

            if (file.exists()) {
                StringBuilder jsonString = new StringBuilder();
                FileReader fileReader = new FileReader(file);
                int character;
                while ((character = fileReader.read()) != -1) {
                    jsonString.append((char) character);
                }
                fileReader.close();

                if (jsonString.length() > 0) {
                    JSONArray notifications = new JSONArray(jsonString.toString());
                    for (int i = 0; i < notifications.length(); i++) {
                        notificationList.add(notifications.getJSONObject(i));
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading notifications from JSON", e);
        }

        return notificationList;
    }

    public static int getNotificationCount(Context context) {
        return readAllNotifications(context).size();
    }

    public static void clearAllNotifications(Context context) {
        File file = new File(context.getFilesDir(), JSON_FILE_NAME);
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "All notifications cleared from JSON file");
        }
    }
}