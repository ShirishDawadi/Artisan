package com.example.artisan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {
    private TextView totalUsers, totalOrders, totalProducts, totalRevenue, reportedProfiles, reportedProducts, reportedOrders;
    private TextView overdueDeliveries, pendingRefund;
    private FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db= FirebaseFirestore.getInstance();

        initializeView(view);
        overviewInfo();
        reportSummary();
        ordersIssue();

        return view;
    }
    private void initializeView(View view){
        totalUsers=view.findViewById(R.id.totalUsers);
        totalOrders= view.findViewById(R.id.totalOrders);
        totalProducts= view.findViewById(R.id.totalProducts);
        totalRevenue = view.findViewById(R.id.totalRevenue);

        reportedProfiles = view.findViewById(R.id.reportedProfiles);
        reportedProducts = view.findViewById(R.id.reportedProducts);
        reportedOrders = view.findViewById(R.id.reportedOrders);

        overdueDeliveries = view.findViewById(R.id.overdueDeliveries);
        pendingRefund = view.findViewById(R.id.pendingRefund);
    }

    private void overviewInfo(){
        db.collection("users").whereNotEqualTo("userType","Admin")
                .get().addOnSuccessListener(v->{
                    int users=v.size();
                    totalUsers.setText(String.valueOf(users));
                });
        db.collection("products").get()
                .addOnSuccessListener(v->{
                    int products = v.size();
                    totalProducts.setText(String.valueOf(products));
                });
        db.collection("orders").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int orders = queryDocumentSnapshots.size();
                    totalOrders.setText(String.valueOf(orders));
                    double revenue = 0;
                    for(DocumentSnapshot doc:queryDocumentSnapshots){
                        revenue += doc.getDouble("totalAmount");
                    }
                    totalRevenue.setText("Rs."+revenue);
                });
    }

    private void reportSummary(){
        db.collection("reports").whereEqualTo("reportType","profile")
                .whereEqualTo("status","pending")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    int proRep=queryDocumentSnapshots.size();
                    reportedProfiles.setText(String.valueOf(proRep));
                });
        db.collection("reports").whereEqualTo("reportType","product")
                .whereEqualTo("status","pending")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    int prodRep=queryDocumentSnapshots.size();
                    reportedProducts.setText(String.valueOf(prodRep));
                });
        db.collection("reports").whereEqualTo("reportType","order")
                .whereEqualTo("status","pending")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    int ordRep=queryDocumentSnapshots.size();
                    reportedOrders.setText(String.valueOf(ordRep));
                });
    }
    private void ordersIssue() {

        db.collection("orders")
                .whereEqualTo("status", "Pending refund")
                .get()
                .addOnSuccessListener(v ->
                        pendingRefund.setText(String.valueOf(v.size()))
                );

        db.collection("orders")
                .whereEqualTo("status", "Out for delivery")
                .get()
                .addOnSuccessListener(snapshots -> {
                    int overdue = 0;
                    long now = System.currentTimeMillis();
                    for (DocumentSnapshot doc : snapshots) {
                        Timestamp ofdTime = doc.getTimestamp("OFDtimestamp");
                        if (ofdTime != null) {
                            long diff = now - ofdTime.toDate().getTime();
                            if (diff > 7L * 24 * 60 * 60 * 1000) {
                                overdue++;
                            }
                        }
                    }
                    overdueDeliveries.setText(String.valueOf(overdue));
                });
    }

}