package com.example.artisan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<Report> reportList;
    private Context context;
    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.reason.setText("Reason: " + report.getReason());
        holder.reportedBy.setText("Reported by: " + report.getReportedBy());
        holder.reportedId.setText("Reported ID: " + report.getReportedId());
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (report.getType().equals("profile")) {
                Toast.makeText(context, "Profile opening", Toast.LENGTH_SHORT).show();
                intent = new Intent(context, SellerProfile.class);
                intent.putExtra("sellerId", report.getReportedId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Productttttttttt opening", Toast.LENGTH_SHORT).show();
                intent = new Intent(context, ProductInfo.class);
                intent.putExtra("productId", report.getReportedId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reason, reportedBy, reportedId;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reason = itemView.findViewById(R.id.reportReason);
            reportedBy = itemView.findViewById(R.id.reportReportedBy);
            reportedId = itemView.findViewById(R.id.reportReportedId);
        }
    }

    public void updateList(List<Report> newList) {
        reportList = newList;
        notifyDataSetChanged();
    }
}
