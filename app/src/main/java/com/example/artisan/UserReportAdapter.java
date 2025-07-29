package com.example.artisan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserReportAdapter extends RecyclerView.Adapter<UserReportAdapter.ReportViewHolder> {

    private Context context;
    private List<ReportItem> reportList;

    public UserReportAdapter(Context context, List<ReportItem> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportItem item = reportList.get(position);
        holder.nameTextView.setText(item.getName());
        holder.countTextView.setText(String.valueOf(item.getCount()));

        holder.itemView.setOnClickListener(v -> {
            if (item.isProfile()) {
                Intent intent = new Intent(context, SellerProfile.class);
                intent.putExtra("sellerId", item.getId());
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, ProductInfo.class);
                intent.putExtra("productId", item.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, countTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
        }
    }
}

