package com.example.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int READ_SMS_PERMISSION_CODE = 101;
    DatabaseHelper db;

    RecyclerView transactionSet;
    private CardAdapter adapter;
    List<Transaction> transactionsSetList = new ArrayList<>();
    private EditText inputReceiver;
    private String inputReceiverValue;
    private EditText inputDate;
    private String inputDateValue;
    private EditText inputAmountLessThan;
    private double inputAmountLessThanValue;
    private EditText inputAmountMoreThan;
    private double inputAmountMoreThanValue;
    private Button getTransactions;
    private Button manageGroups;
    private PopupWindow groupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        transactionSet = findViewById(R.id.scrollTransactionSet);
        transactionSet.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardAdapter(transactionsSetList, db);
        transactionSet.setAdapter(adapter);

        manageGroups = findViewById(R.id.buttonManageGroups);
        inputReceiver = findViewById(R.id.inputReceiver);
        inputDate = findViewById(R.id.inputDate);
        inputAmountLessThan = findViewById(R.id.inputAmountLessThan);
        inputAmountMoreThan = findViewById(R.id.inputAmountMoreThan);
        getTransactions = findViewById(R.id.buttonGetTransactions);

        manageGroups.setOnClickListener(v -> {
            transactionSet.setVisibility(View.GONE);
            showGroupManager(v);
        });
        getTransactions.setOnClickListener(v -> {
            transactionsSetList.clear();

            try {
                inputReceiverValue = inputReceiver.getText().toString().trim().toLowerCase();
                if (inputReceiverValue.isEmpty()) inputReceiverValue = "ANY";

                inputDateValue = inputDate.getText().toString().trim().toLowerCase();
                if (inputDateValue.isEmpty()) inputDateValue = "ANY";

                if (inputAmountLessThan.getText().toString().isEmpty()) inputAmountLessThanValue = 100000;
                else inputAmountLessThanValue = Double.parseDouble(inputAmountLessThan.getText().toString());

                if (inputAmountMoreThan.getText().toString().isEmpty()) inputAmountMoreThanValue = 0;
                else inputAmountMoreThanValue = Double.parseDouble(inputAmountMoreThan.getText().toString());

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Invalid Inputs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_SMS},
                        READ_SMS_PERMISSION_CODE
                );
            } else {
                readAndFilterSMS();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readAndFilterSMS();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readAndFilterSMS() {
        try {
            transactionsSetList.clear();
            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE},
                    null, null,
                    Telephony.Sms.DEFAULT_SORT_ORDER
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                    String date = dateFormat.format(new java.util.Date(dateMillis));
                    String time = timeFormat.format(new java.util.Date(dateMillis));

                    Transaction transaction = new Transaction(address, body, date, time);

                    if (!transaction.getID().equals("XXX")) {
                        String receiverFull = transaction.getReceiver().toLowerCase();
                        boolean receiverMatches = inputReceiverValue.equals("ANY") ||
                                receiverFull.contains(inputReceiverValue);

                        boolean dateMatches = inputDateValue.equals("ANY") ||
                                transaction.getDate().toLowerCase().contains(inputDateValue);

                        boolean amountMatches = transaction.getAmount() >= inputAmountMoreThanValue &&
                                transaction.getAmount() <= inputAmountLessThanValue;

                        if (receiverMatches && dateMatches && amountMatches) {
                            transactionsSetList.add(transaction);
                        }
                    }

                } while (cursor.moveToNext());
            }

            if (cursor != null) cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading SMS!", Toast.LENGTH_SHORT).show();
        }
    }
    private void showGroupManager(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.groups_window, null);
        LinearLayout formLayout = popupView.findViewById(R.id.formLayout);
        Button buttonAddTransaction = popupView.findViewById(R.id.buttonAddTransaction);
        Button buttonDone = popupView.findViewById(R.id.buttonDone);
        EditText inputGroup = popupView.findViewById(R.id.inputGroup);
        EditText inputReceiver = popupView.findViewById(R.id.inputReceiver);
        Spinner colorSelector = popupView.findViewById(R.id.colorSelector);

        // Map color names to hex codes for tinting
        final String[] colorNames = {"Red", "Green", "Blue", "Yellow"};
        final String[] colorHex = {"#FFCDD2", "#C8E6C9", "#BBDEFB", "#FFF9C4"}; // light tints

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSelector.setAdapter(colorAdapter);

        // Create PopupWindow
        groupManager = new PopupWindow(
                popupView,
                1000,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        groupManager.setAnimationStyle(R.style.PopupAnimation);
        groupManager.setOutsideTouchable(true);
        groupManager.setBackgroundDrawable(getDrawable(android.R.color.transparent));
        groupManager.setOnDismissListener(() -> transactionSet.setVisibility(View.VISIBLE));
        groupManager.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // --- RecyclerView for showing database rows ---
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fixed height so popup doesn't shrink
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
        );
        recyclerView.setLayoutParams(params);

        // Load data from database
        ArrayList<ReceiverModel> receivers = new ArrayList<>();
        Cursor cursor = db.readAllRows();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String receiver = cursor.getString(cursor.getColumnIndexOrThrow("Receiver"));
                String groupName = cursor.getString(cursor.getColumnIndexOrThrow("GroupName"));
                String colour = cursor.getString(cursor.getColumnIndexOrThrow("Colour"));
                receivers.add(new ReceiverModel(receiver, groupName, colour));
            } while (cursor.moveToNext());
            cursor.close();
        }

        FreqTransAdapter freqAdapter = new FreqTransAdapter(this, receivers, db);
        recyclerView.setAdapter(freqAdapter);

        // Add RecyclerView to popup layout above form
        LinearLayout popupRoot = popupView.findViewById(R.id.popupRoot);
        popupRoot.addView(recyclerView, 1);

        // Show add form
        buttonAddTransaction.setOnClickListener(v -> {
            recyclerView.setVisibility(View.GONE);
            formLayout.setVisibility(View.VISIBLE);
            buttonAddTransaction.setVisibility(View.GONE);
        });

        // Save new entry
        buttonDone.setOnClickListener(v -> {
            String groupName = inputGroup.getText().toString().trim();
            String receiverName = inputReceiver.getText().toString().trim();
            int selectedIndex = colorSelector.getSelectedItemPosition();
            String color = colorHex[selectedIndex]; // store hex for tinting

            if (groupName.isEmpty() || receiverName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = db.addRow(receiverName, groupName, color);
            receiverName = receiverName.toLowerCase();
            groupName = groupName.toLowerCase();
            if (success) {
                receivers.add(new ReceiverModel(receiverName, groupName, color));
                freqAdapter.notifyItemInserted(receivers.size() - 1);
                Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error adding entry", Toast.LENGTH_SHORT).show();
            }

            inputGroup.setText("");
            inputReceiver.setText("");
            formLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            buttonAddTransaction.setVisibility(View.VISIBLE);
        });
    }



}
