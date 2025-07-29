package com.example.artisan;

import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private List<Product> productList, recommendedList;
    private RecyclerView homeProductRecycler,recommendedRecycler;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProductHomeAdapter productHomeAdapter, recommendedAdapter;
    private ImageView singleLineIcon;
    private ImageView gridViewIcon;
    private TextView noRecommendation;
    private boolean isSingleLine = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        homeProductRecycler = view.findViewById(R.id.homeProductRecycler);
        recommendedRecycler=view.findViewById(R.id.recommendedRecycleView);

        homeProductRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        singleLineIcon = view.findViewById(R.id.singleGrid);
        gridViewIcon = view.findViewById(R.id.doubleGrid);
        noRecommendation=view.findViewById(R.id.noRecommendation);

        recommendedList=new ArrayList<>();

        StaggeredGridLayoutManager layoutManager= new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recommendedRecycler.setLayoutManager(layoutManager);
        recommendedRecycler.setNestedScrollingEnabled(false);
        recommendedAdapter = new ProductHomeAdapter(recommendedList, getContext());
        recommendedAdapter.setLayoutMode(false);
        recommendedRecycler.setAdapter(recommendedAdapter);

        recommendedRecycler.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        homeProductRecycler.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        updateIconColors(isSingleLine);

        singleLineIcon.setOnClickListener(v -> {
            homeProductRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            productHomeAdapter.setLayoutMode(true);
            isSingleLine = true;
            updateIconColors(isSingleLine);
        });

        gridViewIcon.setOnClickListener(v -> {
            homeProductRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            productHomeAdapter.setLayoutMode(false);
            isSingleLine = false;
            updateIconColors(isSingleLine);
        });

        productList = new ArrayList<>();
        getProducts();
        productHomeAdapter = new ProductHomeAdapter(productList, getContext());
        homeProductRecycler.setAdapter(productHomeAdapter);

        loadRecommendedProducts();
        return view;
    }
    private void updateIconColors(boolean isSingleLine) {
        Context context = getContext();

        if (isSingleLine) {
            singleLineIcon.setColorFilter(ContextCompat.getColor(context, R.color.darkred));
            gridViewIcon.setColorFilter(ContextCompat.getColor(context, R.color.white));
        } else {
            singleLineIcon.setColorFilter(ContextCompat.getColor(context, R.color.white));
            gridViewIcon.setColorFilter(ContextCompat.getColor(context, R.color.darkred));
        }
    }

    private void getProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(v -> {
                    List<Product> tempProductList = new ArrayList<>();
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();

                    for (DocumentSnapshot document : v) {
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        String name = document.getString("title");
                        Double price = document.getDouble("price");
                        String aspectRatio = document.getString("aspectRatio");
                        String productID = document.getId();
                        Long comments = document.getLong("comments");
                        Long likes = document.getLong("likes");
                        String sellerID = document.getString("userId");
                        Timestamp createdAt = document.getTimestamp("createdAt");
                        userTasks.add(db.collection("users").document(sellerID).get());

                        Product product = new Product(productID, null, null, null, imageUrls, name, likes, comments, price, aspectRatio,sellerID);
                        product.setCreatedAt(createdAt);
                        tempProductList.add(product);
                    }
                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(results -> {
                                for (int i = 0; i < results.size(); i++) {
                                    DocumentSnapshot userDocument = (DocumentSnapshot) results.get(i);
                                    String sellerName = userDocument.getString("fullname");
                                    Double sellerRating = userDocument.getDouble("rating");
                                    String profilePictureUrl = userDocument.getString("profilePictureUrl");

                                    Product updatedProduct = new Product(
                                            tempProductList.get(i).getProductID(),
                                            profilePictureUrl,
                                            sellerName,
                                            sellerRating,
                                            tempProductList.get(i).getProductImageUrls(),
                                            tempProductList.get(i).getProductName(),
                                            tempProductList.get(i).getProductLikes(),
                                            tempProductList.get(i).getComments(),
                                            tempProductList.get(i).getProductPrice(),
                                            tempProductList.get(i).getAspectRatio(),
                                            tempProductList.get(i).getSellerId()
                                    );
                                    updatedProduct.setCreatedAt(tempProductList.get(i).getCreatedAt());
                                    tempProductList.set(i, updatedProduct);
                                }
                                Collections.sort(tempProductList, (p1, p2) -> {
                                    if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) {
                                        return 0;
                                    }
                                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                                });

                                productList.addAll(tempProductList);
                                productHomeAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(v -> {
                    Toast.makeText(getContext(), "Failed reopen the app", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecommendedProducts() {
        String currentUserId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(currentUserId);
        DocumentReference searchRef = userRef.collection("recommended").document("searchedProducts");

        Tasks.whenAllSuccess(userRef.get(), searchRef.get()).addOnSuccessListener(results -> {
            DocumentSnapshot tagsSnap = (DocumentSnapshot) results.get(0);
            DocumentSnapshot searchSnap = (DocumentSnapshot) results.get(1);

            Map<String, Long> tagMap = (Map<String, Long>) tagsSnap.get("recommended.tags");
            Map<String, Integer> keywordScores = new HashMap<>();

            if (tagMap != null) {
                for (Map.Entry<String, Long> entry : tagMap.entrySet()) {
                    keywordScores.put(entry.getKey().toLowerCase(), entry.getValue().intValue());
                }
            }

            if (searchSnap.exists() && searchSnap.getData() != null) {
                for (int i = 0; i < 10; i++) {
                    String term = (String) searchSnap.get(String.valueOf(i));
                    if (term != null && !term.trim().isEmpty()) {
                        String keyword = term.toLowerCase();
                        keywordScores.put(keyword, keywordScores.getOrDefault(keyword, 0) + 3);
                    }
                }
            }

            if (keywordScores.isEmpty()) {
                noRecommendation.setVisibility(View.VISIBLE);
                return;
            }

            db.collection("products").get().addOnSuccessListener(snapshot -> {
                List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                List<Product> tempRecommended = new ArrayList<>();
                List<Integer> scores = new ArrayList<>();

                for (DocumentSnapshot doc : snapshot) {
                    List<String> tags = (List<String>) doc.get("tags");
                    if (tags == null) continue;

                    int score = 0;
                    for (String tag : tags) {
                        score += keywordScores.getOrDefault(tag.toLowerCase(), 0);
                    }

                    if (score > 0) {
                        List<String> imageUrls = (List<String>) doc.get("imageUrls");
                        String name = doc.getString("title");
                        Double price = doc.getDouble("price");
                        String aspectRatio = doc.getString("aspectRatio");
                        String productID = doc.getId();
                        Long comments = doc.getLong("comments");
                        Long likes = doc.getLong("likes");
                        String sellerID = doc.getString("userId");
                        Timestamp createdAt = doc.getTimestamp("createdAt");

                        userTasks.add(db.collection("users").document(sellerID).get());

                        Product product = new Product(productID, null, null, null, imageUrls, name, likes, comments, price, aspectRatio,sellerID);
                        product.setCreatedAt(createdAt);
                        tempRecommended.add(product);
                        scores.add(score);
                    }
                }

                Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userDocs -> {
                    for (int i = 0; i < userDocs.size(); i++) {
                        DocumentSnapshot userDoc = (DocumentSnapshot) userDocs.get(i);
                        String sellerName = userDoc.getString("fullname");
                        Double sellerRating = userDoc.getDouble("rating");
                        String profilePictureUrl = userDoc.getString("profilePictureUrl");

                        Product original = tempRecommended.get(i);

                        Product updated = new Product(
                                original.getProductID(),
                                profilePictureUrl,
                                sellerName,
                                sellerRating,
                                original.getProductImageUrls(),
                                original.getProductName(),
                                original.getProductLikes(),
                                original.getComments(),
                                original.getProductPrice(),
                                original.getAspectRatio(),
                                original.getSellerId()
                        );
                        tempRecommended.set(i, updated);
                    }

                    List<Map.Entry<Product, Integer>> sorted = new ArrayList<>();
                    for (int i = 0; i < tempRecommended.size(); i++) {
                        sorted.add(new AbstractMap.SimpleEntry<>(tempRecommended.get(i), scores.get(i)));
                    }
                    sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

                    recommendedList.clear();
                    for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                        recommendedList.add(sorted.get(i).getKey());
                    }
                    recommendedAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch seller info", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to fetch products", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error getting recommendation info", Toast.LENGTH_SHORT).show();
        });
    }
}