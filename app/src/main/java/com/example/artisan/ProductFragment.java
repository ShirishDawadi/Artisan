package com.example.artisan;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String productUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        if (getArguments() != null) {
            productUserId = getArguments().getString("sellerId");
        }

        recyclerView = view.findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        fetchProducts();
        return view;
    }

    private void fetchProducts() {
        if(productUserId==null) {
            productUserId = auth.getCurrentUser().getUid();
        }
        db.collection("products")
                .whereEqualTo("userId", productUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    List<Product> tempProductList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        String imageUrl = null;
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            imageUrl = imageUrls.get(0);
                        }
                        String name = document.getString("title");

                        Double price=document.getDouble("price");

                        Long likes=document.getLong("likes");
                        String productID=document.getId();

                        Product product = new Product(imageUrl, name, price,likes,productID);
                        product.setCreatedAt(document.getTimestamp("createdAt"));
                        tempProductList.add(product);
                    }
                    Collections.sort(tempProductList, (p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) {
                            return 0;
                        }
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    });
                    productList.addAll(tempProductList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}