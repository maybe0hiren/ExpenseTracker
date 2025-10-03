package com.example.app;

import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
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
        geminiResult = findViewById(R.id.textGeminiResult);
        buttonSendToGemini = findViewById(R.id.buttonSendToGemini);
        buttonSendToGemini.setVisibility(View.GONE); // Hide send button initially
        Button addTransaction = findViewById(R.id.buttonAddTransaction);
        addTransaction.setOnClickListener(v -> imageSelector());

        buttonSendToGemini.setOnClickListener(v -> {
            if (imageUri != null) {
                sendImageToGemini();
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void imageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            buttonSendToGemini.setVisibility(View.VISIBLE);
        }
    }


    private void sendImageToGemini() {
        new Transaction(this, imageUri, transaction -> {
            geminiResult.setText(
                    "Receiver: " + transaction.getReceiver() + "\n" +
                            "Date: " + transaction.getDate() + "\n" +
                            "Amount: " + transaction.getAmount()
            );
        });
    }
}
