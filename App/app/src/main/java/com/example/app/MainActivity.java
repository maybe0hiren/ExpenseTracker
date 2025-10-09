package com.example.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Telephony;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int READ_SMS_PERMISSION_CODE = 101;

    private Uri imageUri;
    private ImageView imagePreview;
    private TextView geminiResult;
    private Button buttonSendToGemini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imageTransactionScreenshot);
        geminiResult = findViewById(R.id.textTransactionDetails);
        buttonSendToGemini = findViewById(R.id.buttonSendToGemini);
        buttonSendToGemini.setVisibility(View.GONE);
        Button addTransaction = findViewById(R.id.buttonAddTransaction);
        addTransaction.setOnClickListener(v -> imageSelector());

        buttonSendToGemini.setOnClickListener(v -> {
            if (imageUri != null) {
                sendImageToGemini();
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ask SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSION_CODE
            );
        } else {
            readLatestSMS();
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readLatestSMS();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Pick image
    private void imageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            buttonSendToGemini.setVisibility(View.VISIBLE);
        }
    }

    // Send to Gemini (existing logic)
    private void sendImageToGemini() {
        new Transaction(this, imageUri, transaction -> {
            geminiResult.setText(
                    "Receiver: " + transaction.getReceiver() + "\n" +
                            "Date: " + transaction.getDate() + "\n" +
                            "Amount: " + transaction.getAmount()
            );
        });
    }

    // ✅ Read the latest SMS (non-realtime)
    private void readLatestSMS() {
        try {
            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE},
                    null,
                    null,
                    Telephony.Sms.DEFAULT_SORT_ORDER // usually "date DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

                String formattedDate = android.text.format.DateFormat.format(
                        "dd/MM/yyyy hh:mm:ss", new java.util.Date(dateMillis)
                ).toString();

                geminiResult.setText("Latest SMS:\nFrom: " + address + "\n" +
                        "Date: " + formattedDate + "\n" +
                        "Message: " + body);
            } else {
                geminiResult.setText("No SMS found.");
            }

            if (cursor != null) cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            geminiResult.setText("Failed to read SMS: " + e.getMessage());
        }
    }
}
