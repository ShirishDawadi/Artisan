package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class EditProduct extends AppCompatActivity {

    private String productId;
    private ImageView closeAddProduct;
    private EditText productTitle, productPrice, productDescription, tagInput;
    private ChipGroup chipGroup;
    private RecyclerView imageRecyclerView, variationRecyclerView;
    private RadioGroup aspectRatioGroup;
    private RadioButton squareRadio, portraitRadio, landscapeRadio;
    private TextView addVariationButton;
    private Button addProductButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        productId= intent.getStringExtra("productId");
        initializeViews();
    }
    private void initializeViews() {
        closeAddProduct = findViewById(R.id.closeAddProduct);
        productTitle = findViewById(R.id.productTitle);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        tagInput = findViewById(R.id.tagInput);
        chipGroup = findViewById(R.id.chipGroup);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);
        variationRecyclerView = findViewById(R.id.variationRecyclerView);
        aspectRatioGroup = findViewById(R.id.aspectRatio);
        squareRadio = findViewById(R.id.square);
        portraitRadio = findViewById(R.id.potrait);
        landscapeRadio = findViewById(R.id.landscape);
        addVariationButton = findViewById(R.id.addVariationButton);
        addProductButton = findViewById(R.id.addProduct);

        closeAddProduct.setOnClickListener(v -> finish());
    }

    private void loadProductInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                productTitle.setText(document.getString("title"));
                productPrice.setText(document.getString("price"));
                productDescription.setText(document.getString("description"));

                String aspect = document.getString("aspectRatio");
                if ("square".equals(aspect)) squareRadio.setChecked(true);
                else if ("portrait".equals(aspect)) portraitRadio.setChecked(true);
                else if ("landscape".equals(aspect)) landscapeRadio.setChecked(true);

                List<String> tags = (List<String>) document.get("tags");
                chipGroup.removeAllViews();
                if (tags != null) {
                    for (String tag : tags) {
                        Chip chip = new Chip(this);
                        chip.setText(tag);
                        chip.setCloseIconVisible(true);
                        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
                        chipGroup.addView(chip);
                    }
                }

                // Load images
                List<String> imageUrls = (List<String>) document.get("images");
                if (imageUrls != null) {
//                    imageAdapter.setImageList(imageUrls); // assuming you use existing ImageAdapter
                }

                // Load variations
                List<Map<String, Object>> variationList = (List<Map<String, Object>>) document.get("variations");
                if (variationList != null) {
//                    variationAdapter.setVariationList(variationList); // assuming you have a VariationAdapter
                }

            } else {
                Toast.makeText(this, "Product not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}