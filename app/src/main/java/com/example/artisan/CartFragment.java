package com.example.artisan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView cartRecycler;
    private CartAdapter cartAdapter;
    private List<CartProduct> cartProductList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecycler=view.findViewById(R.id.cartRecyclerView);
        cartRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        cartAdapter = new CartAdapter(getContext(), cartProductList);
        cartRecycler.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCartProducts();
        return view;
    }
    private void loadCartProducts() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<String> cartProductIds = (List<String>) snapshot.get("cart");
                        if (cartProductIds == null || cartProductIds.isEmpty()) return;

                        cartProductList.clear();
                        int total = cartProductIds.size();
                        final int[] fetchedCount = {0};

                        for (String productId : cartProductIds) {
                            db.collection("products").document(productId).get()
                                    .addOnSuccessListener(productSnap -> {
                                        if (productSnap.exists()) {
                                            String name = productSnap.getString("title");
                                            Double price = productSnap.getDouble("price");
                                            List<String> imageUrls = (List<String>) productSnap.get("imageUrls");
                                            String imageUrl = imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : "";

                                            cartProductList.add(new CartProduct(name, price, imageUrl, productSnap.getId()));
                                        }
                                        fetchedCount[0]++;
                                        if (fetchedCount[0] == total) {
                                            Collections.reverse(cartProductList);
                                            cartAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }
}