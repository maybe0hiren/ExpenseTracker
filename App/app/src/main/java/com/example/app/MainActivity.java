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

        transactionSet = findViewById(R.id.scrollTransactionSet);
        transactionSet.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardAdapter(transactionsSetList);
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
    private void showGroupManager(View anchorView){
        View popupView = LayoutInflater.from(this).inflate(R.layout.groups_window, null);
        LinearLayout formLayout = popupView.findViewById(R.id.formLayout);

        // Create PopupWindow
        groupManager = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // focusable to handle clicks
        );

        groupManager.setAnimationStyle(R.style.PopupAnimation);

        // Allow outside touch to dismiss (optional)
        groupManager.setOutsideTouchable(true);
        groupManager.setBackgroundDrawable(getDrawable(android.R.color.transparent));
        groupManager.setOnDismissListener(() -> {
            transactionSet.setVisibility(View.VISIBLE);
        });

        // Show popup at center
        groupManager.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // Setup list data inside popup
        ListView listView = popupView.findViewById(R.id.popupList);
        List<String> data = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        // Handle list clicks
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "Clicked: " + data.get(position), Toast.LENGTH_SHORT).show();
        });

        Spinner colorSelector = popupView.findViewById(R.id.colorSelector);
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Red", "Green", "Blue", "Yellow"});
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSelector.setAdapter(colorAdapter);

        // Button inside popup
        Button buttonAddTransaction = popupView.findViewById(R.id.buttonAddTransaction);
        buttonAddTransaction.setOnClickListener(v -> {
            listView.setVisibility(View.GONE);
            formLayout.setVisibility(View.VISIBLE);
            buttonAddTransaction.setVisibility((View.GONE));
        });
        Button buttonDone = popupView.findViewById(R.id.buttonDone);
        EditText inputGroup = popupView.findViewById(R.id.inputGroup);
        EditText inputReceiver = popupView.findViewById(R.id.inputReceiver);

        buttonDone.setOnClickListener(v -> {
            String groupName = inputGroup.getText().toString();
            String receiverName = inputReceiver.getText().toString();
            String color = colorSelector.getSelectedItem().toString();
            Toast.makeText(this, "Group: " + groupName + "\nReceiver: " + receiverName + "\nColor: " + color, Toast.LENGTH_LONG).show();
            formLayout.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            buttonAddTransaction.setVisibility(View.VISIBLE);
        });
    }
}
