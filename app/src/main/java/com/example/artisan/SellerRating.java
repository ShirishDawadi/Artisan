package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SellerRating extends AppCompatActivity {

    private ImageView back;
    private RecyclerView ratingRecycler;
    private String sellerId;
    private RatingAdapter ratingAdapter;
    private List<Rating> ratingList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seller_rating);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        sellerId = intent.getStringExtra("SellerId");

        db = FirebaseFirestore.getInstance();
        back = findViewById(R.id.backSellerRating);
        ratingRecycler = findViewById(R.id.ratingRecycler);
        back.setOnClickListener(v -> finish());

        ratingList = new ArrayList<>();
        ratingAdapter = new RatingAdapter(this, ratingList);
        ratingRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingRecycler.setAdapter(ratingAdapter);

        loadRatings();
    }

    private void loadRatings() {
        db.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    List<Task<Void>> tasks = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocuments) {
                        Map<String, Object> ratingMap = (Map<String, Object>) doc.get("rating");

                        if (ratingMap != null) {
                            String buyerId = doc.getString("buyerId");
                            String buyerName = doc.getString("buyerName");
                            String comment = ratingMap.get("comment") != null ? (String) ratingMap.get("comment") : "";
                            List<String> imageUrls = (List<String>) ratingMap.get("ratingImageUrls");
                            Number starsNumber = (Number) ratingMap.get("stars");
                            double stars = starsNumber != null ? starsNumber.doubleValue() : 0.0;
                            Timestamp timestamp = ratingMap.get("ratingTimestamp") instanceof Timestamp
                                    ? (Timestamp) ratingMap.get("ratingTimestamp")
                                    : null;

                            Task<Void> task = db.collection("users").document(buyerId).get()
                                    .continueWith(userTask -> {
                                        DocumentSnapshot userDoc = userTask.getResult();
                                        String profileUrl = userDoc.getString("profilePictureUrl");

                                        ratingList.add(new Rating(
                                                profileUrl,
                                                buyerName,
                                                comment,
                                                stars,
                                                imageUrls != null ? imageUrls : new ArrayList<>(),
                                                timestamp
                                        ));
                                        return null;
                                    });
                            tasks.add(task);
                        }
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        Collections.sort(ratingList, (r1, r2) -> {
                            Timestamp t1 = r1.getRatingTimestamp();
                            Timestamp t2 = r2.getRatingTimestamp();
                            return t2.compareTo(t1);
                        });

                        ratingAdapter.notifyDataSetChanged();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load ratings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}