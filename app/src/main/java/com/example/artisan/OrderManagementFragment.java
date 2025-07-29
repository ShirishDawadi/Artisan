package com.example.artisan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderManagementFragment extends Fragment {

    private TabLayout orderTabLayout;
    private RecyclerView orderRecyclerView;
    private OrderedAdapter adapter;
    private List<Ordered> orderedList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        orderTabLayout = view.findViewById(R.id.orderTabLayout);
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        db = FirebaseFirestore.getInstance();

        orderTabLayout.addTab(orderTabLayout.newTab().setText("Pending Refund"));
        orderTabLayout.addTab(orderTabLayout.newTab().setText("Overdue Delivery"));

        adapter = new OrderedAdapter(orderedList, requireContext());
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderRecyclerView.setAdapter(adapter);

        loadPendingRefundOrders();

        orderTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadPendingRefundOrders();
                    adapter.notifyDataSetChanged();
                } else {
                    loadOverdueDeliveries();
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void loadPendingRefundOrders() {
        orderedList.clear();
        db.collection("orders")
                .whereEqualTo("status", "Pending refund")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String orderId = doc.getId();
                        String productId = doc.getString("productId");
                        String variationX = doc.getString("variationName");
                        Double totalPrice = doc.getDouble("totalAmount");
                        String status = doc.getString("status");
                        String paymentOption = doc.getString("paymentOption");

                        if (productId != null) {
                            db.collection("products").document(productId)
                                    .get()
                                    .addOnSuccessListener(productDoc -> {
                                        List<String> imageUrls = (List<String>) productDoc.get("imageUrls");
                                        String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
                                        String title = productDoc.getString("title");

                                        Ordered ordered = new Ordered(orderId, imageUrl, title, variationX, totalPrice, status, paymentOption);
                                        orderedList.add(ordered);
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                });
    }

    private void loadOverdueDeliveries() {
        orderedList.clear();
        long now = System.currentTimeMillis();

        db.collection("orders")
                .whereEqualTo("status", "Out for delivery")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Timestamp ofdTime = doc.getTimestamp("OFDtimestamp");
                        if (ofdTime != null) {
                            long diff = now - ofdTime.toDate().getTime();
                            if (diff > 7L * 24 * 60 * 60 * 1000) {
                                String orderId = doc.getId();
                                String productId = doc.getString("productId");
                                String variationX = doc.getString("variationName");
                                Double totalPrice = doc.getDouble("totalAmount");
                                String status = doc.getString("status");
                                String paymentOption = doc.getString("paymentOption");

                                if (productId != null) {
                                    db.collection("products").document(productId)
                                            .get()
                                            .addOnSuccessListener(productDoc -> {
                                                List<String> imageUrls = (List<String>) productDoc.get("imageUrls");
                                                String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
                                                String title = productDoc.getString("title");

                                                Ordered ordered = new Ordered(orderId, imageUrl, title, variationX, totalPrice, status, paymentOption);
                                                orderedList.add(ordered);
                                                adapter.notifyDataSetChanged();
                                            });
                                }
                            }
                        }
                    }
                });
    }


}
