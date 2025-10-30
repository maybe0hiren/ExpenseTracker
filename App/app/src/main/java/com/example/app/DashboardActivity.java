package com.example.app;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView dashboardRecyclerView;
    private DashboardAdapter adapter;
    private List<GroupSpending> groupSpendingList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        dashboardRecyclerView = findViewById(R.id.dashboardRecyclerView);
        dashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DashboardAdapter(groupSpendingList);
        dashboardRecyclerView.setAdapter(adapter);

        Button buttonBackToMain = findViewById(R.id.buttonBackToMain);
        buttonBackToMain.setOnClickListener(v -> finish());

        calculateGroupSpending();
    }

    private void calculateGroupSpending() {
        Map<String, GroupSpending> groupSpendingMap = new HashMap<>();
        Map<String, String> keywordToGroupMap = new HashMap<>();

        // Load all groups and their keywords from the database.
        Cursor groupCursor = dbHelper.readAllRows();
        if (groupCursor != null && groupCursor.moveToFirst()) {
            do {
                String groupName = groupCursor.getString(groupCursor.getColumnIndexOrThrow("GroupName"));
                String receiverKeyword = groupCursor.getString(groupCursor.getColumnIndexOrThrow("Receiver")).toLowerCase();
                String color = groupCursor.getString(groupCursor.getColumnIndexOrThrow("Colour"));

                if (!groupSpendingMap.containsKey(groupName)) {
                    groupSpendingMap.put(groupName, new GroupSpending(groupName, 0, color));
                }
                keywordToGroupMap.put(receiverKeyword, groupName);
            } while (groupCursor.moveToNext());
        }
        if (groupCursor != null) {
            groupCursor.close();
        }

        // Read all SMS messages, including the date
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE},
                null, null,
                Telephony.Sms.DEFAULT_SORT_ORDER
        );

        if (smsCursor != null && smsCursor.moveToFirst()) {
            do {
                String address = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                long dateMillis = smsCursor.getLong(smsCursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                String date = dateFormat.format(new java.util.Date(dateMillis));
                String time = timeFormat.format(new java.util.Date(dateMillis));

                Transaction transaction = new Transaction(address, body, date, time);

                if (transaction.getAmount() > 0 && !transaction.getReceiver().equals("XXX")) {
                    // **THE CRITICAL FIX IS HERE**
                    // We must use the receiver parsed from the SMS body, not the sender's address.
                    String parsedReceiver = transaction.getReceiver().toLowerCase();

                    for (Map.Entry<String, String> entry : keywordToGroupMap.entrySet()) {
                        String dbKeyword = entry.getKey();
                        boolean matchFound = false;

                        String[] keywordParts = dbKeyword.split("\\s+");
                        for (String part : keywordParts) {
                            // Check if the PARSED RECEIVER contains the keyword part.
                            if (!part.isEmpty() && parsedReceiver.contains(part)) {
                                String groupName = entry.getValue();
                                GroupSpending spending = groupSpendingMap.get(groupName);
                                if (spending != null) {
                                    spending.addSpending(transaction.getAmount());
                                }
                                matchFound = true;
                                break;
                            }
                        }
                        if (matchFound) {
                            break;
                        }
                    }
                }
            } while (smsCursor.moveToNext());
        }
        if (smsCursor != null) {
            smsCursor.close();
        }

        groupSpendingList.clear();
        groupSpendingList.addAll(groupSpendingMap.values());
        adapter.notifyDataSetChanged();
    }
}
