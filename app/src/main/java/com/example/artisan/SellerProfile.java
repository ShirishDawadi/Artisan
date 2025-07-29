package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerProfile extends AppCompatActivity {

    private TextView profilename, description, rating, likes, report, block;
    private ImageView threeLine,back;
    private ShapeableImageView homeSellerProfilePicture;
    private Button seeRating;
    private LinearLayout options;
    private FrameLayout productFrameLayout;
    private String sellerId, userType;
    private TextView reportTitle,inappropriateContent,misleadingInfo, scamFraud, others;
    private TextView submit,cancel;
    private EditText reason;
    private FirebaseFirestore db ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seller_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent= getIntent();
        sellerId=intent.getStringExtra("sellerId");

        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadInformation();

        MyApplication app = (MyApplication) getApplicationContext();
        userType = app.getUsertype();
        if("Admin".equals(userType)) {
            block.setVisibility(View.VISIBLE);
            report.setVisibility(View.GONE);
        }

        ProductFragment productFragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sellerId", sellerId);
        productFragment.setArguments(bundle);
        loadFragment(productFragment);

        threeLine.setOnClickListener(v->{
            options.setVisibility(options.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
        seeRating.setOnClickListener(v->{
            Intent intent1=new Intent(SellerProfile.this,SellerRating.class);
            intent1.putExtra("SellerId",sellerId);
            startActivity(intent1);
        });
        back.setOnClickListener( v -> finish());

        block.setOnClickListener(v->blockProfile());

        report.setOnClickListener(v->reportSellerDialog());
    }
    private void blockProfile() {
        db.collection("users").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("users").document(sellerId)
                                .update("isblocked", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "User blocked successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to block user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeViews() {
        threeLine = findViewById(R.id.threeLine);
        homeSellerProfilePicture = findViewById(R.id.homeSellerProfilePicture);
        profilename = findViewById(R.id.profilename);
        seeRating = findViewById(R.id.seeRating);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        likes = findViewById(R.id.likes);
        options = findViewById(R.id.options);
        report = findViewById(R.id.report);
        block = findViewById(R.id.block);
        productFrameLayout = findViewById(R.id.productFrameLayout);
        back=findViewById(R.id.back);
    }
    private void loadFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.productFrameLayout, fragment)
                .commit();
    }

    private void loadInformation() {
        DocumentReference sellerRef = db.collection("users").document(sellerId);

        sellerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("fullname");
                String bio = documentSnapshot.getString("bio");
                String profileUrl = documentSnapshot.getString("profilePictureUrl");
                Double ratingValue = documentSnapshot.getDouble("rating");
                Long likeCount = documentSnapshot.getLong("totalLikes");

                profilename.setText(name != null ? name : "");
                description.setText(bio != null ? bio : "");
                rating.setText(ratingValue != null ? String.valueOf(ratingValue) : "0.0");
                likes.setText(likeCount != null ? String.valueOf(likeCount) : "0");

                if (profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profileUrl)
                            .placeholder(R.drawable.man)
                            .into(homeSellerProfilePicture);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load seller info", Toast.LENGTH_SHORT).show();
        });
    }
    private void reportSellerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.report_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        reportTitle = dialogView.findViewById(R.id.reportText);
        inappropriateContent = dialogView.findViewById(R.id.inappropriateContent);
        misleadingInfo = dialogView.findViewById(R.id.misleadingInformation);
        scamFraud = dialogView.findViewById(R.id.scamOrFraud);
        others = dialogView.findViewById(R.id.others);
        reason = dialogView.findViewById(R.id.reason);
        submit = dialogView.findViewById(R.id.submit);
        cancel = dialogView.findViewById(R.id.cancel);

        reportTitle.setText("Report Profile");

        View.OnClickListener quickReport = v -> {
            String selectedReason = ((TextView) v).getText().toString();
            reportSeller(selectedReason);
            dialog.dismiss();
        };

        inappropriateContent.setOnClickListener(quickReport);
        misleadingInfo.setOnClickListener(quickReport);
        scamFraud.setOnClickListener(quickReport);

        others.setOnClickListener(v -> {
            reason.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);
        });

        submit.setOnClickListener(v -> {
            String otherText = reason.getText().toString().trim();
            reportSeller(otherText.isEmpty()?"other":otherText);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void reportSeller(String reason) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String reporterId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> report = new HashMap<>();
        report.put("reportType","profile");
        report.put("reportedProfileId", sellerId);
        report.put("reportedBy", reporterId);
        report.put("reason", reason);
        report.put("status","pending");
        report.put("timestamp", Timestamp.now());

        db.collection("reports")
                .add(report)
                .addOnSuccessListener(doc -> Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show());
    }
}