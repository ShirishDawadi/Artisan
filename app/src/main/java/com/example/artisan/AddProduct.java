package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

public class AddProduct extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private RadioGroup aspect;
    private EditText tagInput;
    private ChipGroup chipGroup;
    private List<String> tags;
    private int chipCount=0;
    private String aspectRatio="Square";
    private FrameLayout layout;
    private FrameLayout imageOverlayFrame;
    private List<Uri> imageUris = new ArrayList<>();
    private TextView meow;
    private EditText title;
    private EditText price;
    private EditText description;
    private Button addProduct;
    private ImageView closeAddProduct;
    private List<Variation> variationList = new ArrayList<>();
    private VariationAdapter variationAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> cloudinaryImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //image recycler view finding
        imageOverlayFrame=findViewById(R.id.imageOverlayFrame);
        layout=findViewById(R.id.frameLayout);
        aspect=findViewById(R.id.aspectRatio);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);
        setAspectRatio();

        //chip being created for tags and keyword
        tagInput = findViewById(R.id.tagInput);
        tagInput.addTextChangedListener(new CustomTextWatcher(tagInput,10));
        chipGroup = findViewById(R.id.chipGroup);
        tags = new ArrayList<>();

        tagInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith(",") || s.toString().endsWith("\n")) {
                    String tag = s.toString().trim().replace(",", "");
                    if (!tag.isEmpty()) {
                        addTag(tag);
                    }
                    tagInput.setText("");
                }
            }
        });

        //initializing recycler view for images
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(layoutManager);
        imageAdapter = new ImageAdapter(imageUris, aspectRatio,this);
        imageRecyclerView.setAdapter(imageAdapter);
        imageOverlayFrame.setOnClickListener(v->selectImage());

        //drag-and-drop
        ItemTouchHelper.Callback callback = new ImageMoveCallback(imageAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(imageRecyclerView);

        //for variation recycler view
        TextView addVariationButton=findViewById(R.id.addVariationButton);
        RecyclerView variationRecyclerView = findViewById(R.id.variationRecyclerView);
        variationAdapter = new VariationAdapter(variationList);
        variationRecyclerView.setAdapter(variationAdapter);
        variationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        variationList.add(new Variation("Normal", 0));
        variationAdapter.notifyItemInserted(variationList.size() - 1);

        addVariationButton.setOnClickListener(v -> {
            variationList.add(new Variation("", 0));
            variationAdapter.notifyItemInserted(variationList.size() - 1);
        });

        //for inserting in database
        title=findViewById(R.id.productTitle);
        title.addTextChangedListener(new CustomTextWatcher(title,40));
        price=findViewById(R.id.productPrice);

        price.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    String result = dest.subSequence(0, dstart)
                            + source.subSequence(start, end).toString()
                            + dest.subSequence(dend, dest.length());
                    if (result.isEmpty()) return null;
                    if (result.contains(".")) {
                        String[] parts = result.split("\\.");
                        String integerPart = parts[0];
                        String decimalPart = parts.length > 1 ? parts[1] : "";

                        if (integerPart.length() > 8) {
                            if (dstart <= result.indexOf(".")) {
                                return "";
                            }
                        }
                        if (decimalPart.length() > 2) {
                            if (dstart > result.indexOf(".")) {
                                return "";
                            }
                        }
                    } else {
                        if (result.length() > 8) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        description=findViewById(R.id.productDescription);
        addProduct=findViewById(R.id.addProduct);
        addProduct.setOnClickListener(v->addProductFirebaseAndCloudinary());
        meow=findViewById(R.id.meow);

        Intent intent = getIntent();
        String productId = intent.getStringExtra("productId");
        if (productId != null) {
            meow.setText("Edit Product");
            loadProductInfo(productId);
            addProduct.setText("Update Product");
        }
        closeAddProduct=findViewById(R.id.closeAddProduct);
        closeAddProduct.setOnClickListener(v->showDialog());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showDialog();
            }
        });
    }
    class ImageMoveCallback extends ItemTouchHelper.Callback {
        private final ImageAdapter adapter;
        public ImageMoveCallback(ImageAdapter adapter) {
            this.adapter = adapter;
        }
        @Override
        public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.3f;
        }
        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.3f;
        }
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(dragFlags, 0);
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    }
    private void backHome(){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
        finish();
    }
    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder
                (AddProduct.this,R.style.MyAlertDialog);
        builder.setMessage("Do you want to exit?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        backHome();
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.red));
        }
    }
    private void addTag(String tag) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, chipGroup, false);
        chip.setText(tag);
        chipCount++;
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            tags.remove(tag);
            chipCount--;
            if(chipCount<10){
                tagInput.setVisibility(View.VISIBLE);
            }
        });
        chipGroup.addView(chip);
        tags.add(tag);
        if(chipCount>=10){
            tagInput.setVisibility(View.GONE);
        }
    }
    private void setAspectRatio(){
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                360,
                getResources().getDisplayMetrics()
        );
        int heightPotrait = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                450,
                getResources().getDisplayMetrics()
        );
        int heightLandscape = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                270,
                getResources().getDisplayMetrics()
        );
        aspect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                ViewGroup.LayoutParams params = layout.getLayoutParams();
                if (checkedId == R.id.potrait) {
                    aspectRatio = "Portrait";
                    params.width = width;
                    params.height = heightPotrait;
                } else if (checkedId == R.id.landscape) {
                    aspectRatio = "Landscape";
                    params.width = width;
                    params.height = heightLandscape;
                }else if(checkedId==R.id.square){
                    aspectRatio = "Square";
                    params.width = width;
                    params.height = width;
                }
                layout.setLayoutParams(params);
                layout.requestLayout();

                imageAdapter = new ImageAdapter(imageUris,aspectRatio,getBaseContext());
                imageRecyclerView.setAdapter(imageAdapter);
            }
        });
    }
    private void selectImage(){
        imageOverlayFrame.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null && imageUris.isEmpty()) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if(count==0){
                    imageOverlayFrame.setVisibility(View.VISIBLE);
                    return;
                }
                if (count > 10) {
                    Toast.makeText(this, "You can select a maximum of 10 images", Toast.LENGTH_SHORT).show();
                    imageOverlayFrame.setVisibility(View.VISIBLE);
                    return;
                }
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            if (imageAdapter != null) {
                imageAdapter.notifyDataSetChanged();
            }
        }
    }
    private void addProductFirebaseAndCloudinary() {
        String productTitle = title.getText().toString().trim();
        String productPrice = price.getText().toString().trim();
        String productDescription = description.getText().toString().trim();

        if (productTitle.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty() || imageUris.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select images", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Variation> updatedVariations = variationAdapter.getVariations();
        for (Variation variation : updatedVariations) {
            if (variation.getName().isEmpty()) {
                Toast.makeText(AddProduct.this, "Variation name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            } 
        }
        uploadImagesToCloudinary();
    }
    private void uploadImagesToCloudinary() {
        cloudinaryImageUrls = new ArrayList<>(Collections.nCopies(imageUris.size(), null));
        int imageCount = imageUris.size();
        final int[] uploadedCount = {0};

        for (int i = 0; i < imageUris.size(); i++) {
            final int index = i;
            Uri imageUri = imageUris.get(i);

            MediaManager.get().upload(imageUri)
                    .unsigned("artisan_upload")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("url").toString();
                            if(imageUrl.startsWith("http://")){
                                imageUrl=imageUrl.replace("http://", "https://");
                            }
                            cloudinaryImageUrls.set(index, imageUrl);
                            uploadedCount[0]++;

                            if (uploadedCount[0] == imageCount) {
                                if (!cloudinaryImageUrls.contains(null)) {
                                    saveProductToFirestore();
                                }
                            }
                        }
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(AddProduct.this, "Error uploading image: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    .dispatch();
        }
    }
    private void saveProductToFirestore() {
        String productTitle = title.getText().toString().trim();
        String productPrice = price.getText().toString().trim();
        String productDescription = description.getText().toString().trim();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> product = new HashMap<>();
        product.put("title", productTitle);
        product.put("price", Double.parseDouble(productPrice));
        product.put("description", productDescription);
        product.put("tags", tags);
        product.put("aspectRatio", aspectRatio);
        product.put("imageUrls", cloudinaryImageUrls);
        product.put("userId",userId);
        product.put("createdAt", FieldValue.serverTimestamp());
        product.put("likes",0);
        product.put("comments",0);
        product.put("orderCount",0);

        List<Map<String, Object>> variationsData = new ArrayList<>();
        List<Variation> updatedVariations = variationAdapter.getVariations();
        for (Variation variation : updatedVariations) {
            Map<String, Object> variationData = new HashMap<>();
            variationData.put("name", variation.getName());
            variationData.put("stock", variation.getStock());
            variationsData.add(variationData);
        }
        product.put("variations", variationsData);

        db.collection("products")
                .add(product)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddProduct.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProduct.this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadProductInfo(String productId) {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        title.setText(document.getString("title"));
                        price.setText(String.valueOf(document.getLong("price")));
                        description.setText(document.getString("description"));
                        aspectRatio = document.getString("aspectRatio");

                        if (aspectRatio.equals("Portrait")) {
                            aspect.check(R.id.potrait);
                        } else if (aspectRatio.equals("Landscape")) {
                            aspect.check(R.id.landscape);
                        } else {
                            aspect.check(R.id.square);
                        }

                        List<String> existingTags = (List<String>) document.get("tags");
                        if (existingTags != null) {
                            for (String tag : existingTags) {
                                addTag(tag);
                            }
                        }

                        List<Map<String, Object>> variations = (List<Map<String, Object>>) document.get("variations");
                        if (variations != null) {
                            variationList.clear();
                            for (Map<String, Object> var : variations) {
                                String name = (String) var.get("name");
                                long stock = (long) var.get("stock");
                                variationList.add(new Variation(name, (int) stock));
                            }
                            variationAdapter.notifyDataSetChanged();
                        }

                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        if (imageUrls != null) {
                            imageUris.clear();
                            cloudinaryImageUrls.clear();

                            for (String url : imageUrls) {
                                imageUris.add(Uri.parse(url));
                                cloudinaryImageUrls.add(url);
                            }
                            imageAdapter.notifyDataSetChanged();
                            imageOverlayFrame.setVisibility(View.GONE);
                        }

                        addProduct.setOnClickListener(v -> updateProduct(productId));
                    }
                });
    }
    private void updateProduct(String productId) {
        String productTitle = title.getText().toString().trim();
        String productPrice = price.getText().toString().trim();
        String productDescription = description.getText().toString().trim();

        if (productTitle.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", productTitle);
        updatedData.put("price", Double.parseDouble(productPrice));
        updatedData.put("description", productDescription);
        updatedData.put("tags", tags);
        updatedData.put("aspectRatio", aspectRatio);
        updatedData.put("imageUrls", cloudinaryImageUrls);
        updatedData.put("updatedAt", FieldValue.serverTimestamp());

        List<Map<String, Object>> variationsData = new ArrayList<>();
        List<Variation> updatedVariations = variationAdapter.getVariations();
        for (Variation variation : updatedVariations) {
            Map<String, Object> variationData = new HashMap<>();
            variationData.put("name", variation.getName());
            variationData.put("stock", variation.getStock());
            variationsData.add(variationData);
        }
        updatedData.put("variations", variationsData);

        db.collection("products").document(productId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
