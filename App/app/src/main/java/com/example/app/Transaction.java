package com.example.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.*;

public class Transaction {
    private String receiver;
    private String date;
    private double amount;

    // Set this to your Flask server URL
    private static final String FLASK_URL = "http://192.168.1.10:5000/process-image";

    public Transaction(Context context, Uri imageUri, Callback callback) {
        // Run network code on background thread
        new Thread(() -> {
            try {
                // Convert Uri -> File
                File imageFile = getFileFromUri(context, imageUri);

                // Prepare multipart request
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "image",
                                imageFile.getName(),
                                RequestBody.create(imageFile, MediaType.parse("image/jpeg"))
                        )
                        .build();

                Request request = new Request.Builder()
                        .url(FLASK_URL)
                        .post(requestBody)
                        .build();

                // Execute request
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new Exception("HTTP Error: " + response);

                String jsonResponse = response.body().string();
                JSONObject json = new JSONObject(jsonResponse);

                this.receiver = json.getString("receiver");
                this.date = json.getString("date");
                this.amount = json.getDouble("amount");

                // Notify callback on main thread
                ((android.app.Activity) context).runOnUiThread(() -> callback.onSuccess(this));

            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private File getFileFromUri(Context context, Uri uri) throws Exception {
        File file = new File(context.getCacheDir(), getFileName(context, uri));
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return file;
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            if (result == null) result = "image.jpg"; // fallback
        }
        return result;
    }

    // Getters
    public String getReceiver() { return receiver; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }

    // Callback interface to notify when done
    public interface Callback {
        void onSuccess(Transaction transaction);
    }
}
