package com.example.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FreqTransAdapter extends RecyclerView.Adapter<FreqTransAdapter.ViewHolder> {

    private ArrayList<ReceiverModel> list;
    private Context context;
    private DatabaseHelper dbHelper;

    public FreqTransAdapter(Context context, ArrayList<ReceiverModel> list, DatabaseHelper dbHelper) {
        this.context = context;
        this.list = list;
        this.dbHelper = dbHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReceiverModel item = list.get(position);
        holder.textReceiver.setText(item.getReceiver());
        holder.textGroup.setText(item.getGroup());

        // Apply transparent tint color
        try {
            int baseColor = Color.parseColor(item.getColour());
            int tintedColor = Color.argb(40, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
            holder.cardView.setCardBackgroundColor(tintedColor);
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.argb(30, 0, 0, 0)); // fallback
        }

        // Delete button logic
        holder.btnAction.setOnClickListener(v -> {
            dbHelper.removeRow(item.getReceiver());
            list.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textReceiver, textGroup;
        Button btnAction;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            textReceiver = itemView.findViewById(R.id.textReceiver);
            textGroup = itemView.findViewById(R.id.textGroup);
            btnAction = itemView.findViewById(R.id.btnAction);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
