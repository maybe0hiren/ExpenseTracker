package com.example.app;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private final List<Transaction> transactions;
    private final DatabaseHelper dbHelper;

    public CardAdapter(List<Transaction> transactions, DatabaseHelper dbHelper) {
        this.transactions = transactions;
        this.dbHelper = dbHelper;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView receiver, transactionDetails;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver = itemView.findViewById(R.id.textReceiver);
            transactionDetails = itemView.findViewById(R.id.textTransactionDetails);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    @NonNull
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.receiver.setText(t.getReceiver());
        holder.transactionDetails.setText("₹" + t.getAmount() + " • " + t.getDate());

        if (holder.cardView != null) {
            holder.cardView.setCardBackgroundColor(Color.argb(30, 0, 0, 0));

            String transactionReceiverId = t.getReceiver().toLowerCase();
            String colorHex = null;
            boolean matchFound = false;

            Cursor allReceiversCursor = dbHelper.readAllRows();
            if (allReceiversCursor != null && allReceiversCursor.moveToFirst()) {
                do {
                    String dbKeyword = allReceiversCursor.getString(allReceiversCursor.getColumnIndexOrThrow("Receiver")).toLowerCase();
                    String[] keywordParts = dbKeyword.split("\\s+");

                    for (String part : keywordParts) {
                         if (!part.isEmpty() && transactionReceiverId.contains(part)) {
                            colorHex = allReceiversCursor.getString(allReceiversCursor.getColumnIndexOrThrow("Colour"));
                            matchFound = true;
                            break;
                        }
                    }
                    if(matchFound) break;

                } while (allReceiversCursor.moveToNext());
            }
            if (allReceiversCursor != null) {
                allReceiversCursor.close();
            }
            try {
                if (colorHex != null && !colorHex.isEmpty()) {
                    int baseColor = Color.parseColor(colorHex);
                    int tintedColor = Color.argb(40, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
                    holder.cardView.setCardBackgroundColor(tintedColor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
