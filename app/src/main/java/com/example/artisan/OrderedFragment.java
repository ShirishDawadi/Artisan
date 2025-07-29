package com.example.artisan;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderedFragment extends Fragment {

    private RecyclerView orderedRecycler;
    private OrderedAdapter orderedAdapter;
    private List<Ordered> orderedList =new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private ConstraintLayout noOrders;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordered, container, false);

        orderedRecycler = view.findViewById(R.id.orderedRecycler);
        orderedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        orderedAdapter = new OrderedAdapter(orderedList, getContext());
        orderedRecycler.setAdapter(orderedAdapter);

        noOrders=view.findViewById(R.id.noOrders);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadOrderedProducts();

        return view;
    }

    private void loadOrderedProducts() {
        String userType = ((MyApplication) requireActivity().getApplication()).getUsertype();

        db.collection("orders")
                .whereEqualTo(userType.equals("Buyer") ? "buyerId" : "sellerId", currentUserId)
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    if (orderSnapshots.isEmpty()) {
                        orderedList.clear();
                        noOrders.setVisibility(View.VISIBLE);
                        orderedAdapter.notifyDataSetChanged();
                        return;
                    }
                    List<DocumentSnapshot> orderDocs = new ArrayList<>();
                    List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();

                    for (DocumentSnapshot order : orderSnapshots) {
                        orderDocs.add(order);
                        String productId = order.getString("productId");
                        productTasks.add(db.collection("products").document(productId).get());
                    }

                    Tasks.whenAllSuccess(productTasks).addOnSuccessListener(results -> {
                        orderedList.clear();
                        for (int i = 0; i < results.size(); i++) {
                            DocumentSnapshot product = (DocumentSnapshot) results.get(i);
                            DocumentSnapshot order = orderDocs.get(i);

                            if (!product.exists()) continue;

                            List<String> images = (List<String>) product.get("imageUrls");
                            String imageUrl = (images != null && !images.isEmpty()) ? images.get(0) : "";

                            Ordered o = new Ordered(
                                    order.getId(),
                                    imageUrl,
                                    product.getString("title"),
                                    order.getString("variationName") + " X " + order.getLong("quantity"),
                                    order.getDouble("totalAmount"),
                                    order.getString("status"),
                                    order.getString("paymentOption")
                            );
                            o.setTimestamp(order.getTimestamp("timestamp"));
                            orderedList.add(o);
                        }
                        Collections.sort(orderedList, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                        orderedAdapter.notifyDataSetChanged();
                    });
                });
    }
}
