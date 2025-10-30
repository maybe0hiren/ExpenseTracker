package com.example.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private final List<GroupSpending> groupSpendingList;

    public DashboardAdapter(List<GroupSpending> groupSpendingList) {
        this.groupSpendingList = groupSpendingList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textGroupName, textTotalSpending;
        MaterialCardView groupCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupName = itemView.findViewById(R.id.textGroupName);
            textTotalSpending = itemView.findViewById(R.id.textTotalSpending);
            groupCardView = itemView.findViewById(R.id.groupCardView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_spending_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupSpending item = groupSpendingList.get(position);
        holder.textGroupName.setText(item.getGroupName());
        holder.textTotalSpending.setText("Total Spending: ₹" + String.format("%.2f", item.getTotalSpending()));

        if (holder.groupCardView != null) {
            try {
                String colorHex = item.getColor();
                if (colorHex != null && !colorHex.isEmpty()) {
                    int baseColor = Color.parseColor(colorHex);
                    int tintedColor = Color.argb(40, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
                    holder.groupCardView.setCardBackgroundColor(tintedColor);
                } else {
                    holder.groupCardView.setCardBackgroundColor(Color.argb(30, 0, 0, 0)); // fallback
                }
            } catch (Exception e) {
                holder.groupCardView.setCardBackgroundColor(Color.argb(30, 0, 0, 0)); // fallback
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupSpendingList.size();
    }
}
