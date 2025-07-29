package com.example.artisan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductInfo extends AppCompatActivity {
    private TextView sellerName;
    private ImageView sellerProfile;
    private TextView sellerRating;
    private TextView title;
    private TextView likes;
    private TextView price;
    private ChipGroup variationsChipGroup;
    private String selectedVariationName = "";
    private LinearLayout quantitySelector;
    private TextView quantity,increment,decrement;
    private Chip previouslySelectedChip = null;
    private TextView description;
    private EditText commentEditText;
    private ImageView addCommentButton;
    private List<Comment> commentList;
    private LinearLayout buttonHolder;
    private Button buyNow,addToCart;
    private String userId,sellerUserId, userType;
    private String productId;
    private ImageView backProductInfo;
    private ImageView menu;
    private LinearLayout menuItem;
    private TextView editProduct;
    private TextView deleteProduct;
    private TextView reportProduct;
    private boolean isHideButton =true, isInCart = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private HashMap<String, Integer> variationStockMap = new HashMap<>();
    private int maxAvailableStock = 0;
    private int currentQuantity = 1;
    private boolean isAdmin=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");


        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");
                        if ("Admin".equals(userType)) {
                            isAdmin=true;
                        }
                    }
                });

        sellerName=findViewById(R.id.sellerName);
        sellerProfile=findViewById(R.id.homeSellerProfilePicture);
        sellerRating=findViewById(R.id.ratingSeller);

        title=findViewById(R.id.productInfoTitle);
        likes=findViewById(R.id.productInfoLikes);
        price=findViewById(R.id.productInfoPrice);

        variationsChipGroup = findViewById(R.id.variationsChipGroup);
        quantitySelector=findViewById(R.id.quantitySelector);
        quantity=findViewById(R.id.quantity_text);
        increment=findViewById(R.id.increment_button);
        decrement=findViewById(R.id.decrement_button);

        description=findViewById(R.id.productInfoDescription);

        commentList=new ArrayList<>();
        commentEditText=findViewById(R.id.commentEditText);
        addCommentButton=findViewById(R.id.addCommentButton);
        addCommentButton.setOnClickListener(v->addComments());

        buttonHolder=findViewById(R.id.buttonHolder);
        buyNow=findViewById(R.id.buyNow);
        addToCart=findViewById(R.id.addToCart);
        checkIfInCartAndUpdateUI();
        addToCart.setOnClickListener(v -> {
            if (isInCart) {
                removeFromCart();
            } else {
                addToCart();
            }
        });


        MyApplication app = (MyApplication) getApplicationContext();
        userType = app.getUsertype();
        if ("Seller".equals(userType)|| isAdmin ||"Admin".equals(userType)) {
            buttonHolder.setVisibility(View.GONE);
            variationsChipGroup.setVisibility(View.GONE);
            quantitySelector.setVisibility(View.GONE);
        }

        backProductInfo=findViewById(R.id.backInfoButton);
        backProductInfo.setOnClickListener(v->finish());

        increment.setOnClickListener(v -> updateQuantity(1));
        decrement.setOnClickListener(v -> updateQuantity(-1));
        loadProductInfo();
        loadComment();

        menu=findViewById(R.id.threeDotButton);
        menuItem=findViewById(R.id.menuProduct);
        editProduct=findViewById(R.id.editProduct);
        editProduct.setOnClickListener(v->{
            Intent in = new Intent(this, AddProduct.class);
            in.putExtra("productId", productId);
            startActivity(in);
        });

        deleteProduct=findViewById(R.id.deleteProduct);
        deleteProduct.setOnClickListener(v->{
            if (productId != null && !productId.isEmpty()) {
                deleteProductFromFirestore(productId);
            } else {
                Toast.makeText(this, "Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        reportProduct=findViewById(R.id.reportProduct);
        reportProduct.setOnClickListener(v-> reportProductDialog());

        menu.setOnClickListener(v->{
            if(menuItem.getVisibility()==View.GONE) menuItem.setVisibility(View.VISIBLE);
            else menuItem.setVisibility(View.GONE);
        });

        buyNow.setOnClickListener(v->buyNowProduct());
    }
    private void checkIfInCartAndUpdateUI() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userType =documentSnapshot.getString("userType");
                    List<String> cart = (List<String>) documentSnapshot.get("cart");
                    if (cart != null && cart.contains(productId)) {
                        isInCart = true;
                        addToCart.setText("Remove from Cart");
                    } else {
                        isInCart = false;
                        addToCart.setText("Add to Cart");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Cart", "Failed to fetch cart", e);
                });
    }
    private void addToCart() {
        db.collection("users").document(userId)
                .update("cart", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(aVoid -> {
                    isInCart = true;
                    addToCart.setText("Remove from Cart");
                    Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Cart", "Error adding to cart", e);
                });
    }
    private void removeFromCart() {
        db.collection("users").document(userId)
                .update("cart", FieldValue.arrayRemove(productId))
                .addOnSuccessListener(aVoid -> {
                    isInCart = false;
                    addToCart.setText("Add to Cart");
                    Toast.makeText(this, "Removed from Cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Cart", "Error removing from cart", e);
                });
    }

    private void buyNowProduct(){
        Intent intent =new Intent(this,AddAddress.class);
        intent.putExtra("productId", productId);
        intent.putExtra("variationName", selectedVariationName);
        intent.putExtra("quantity", currentQuantity);
        startActivity(intent);
    }
    private void updateQuantity(int change) {
        if (!selectedVariationName.isEmpty()) {
            int currentStock = variationStockMap.getOrDefault(selectedVariationName, 0);

            if (currentStock == 0) {
                quantity.setText("Out of Stock");
                currentQuantity = 0;
            } else{
                int newQuantity = currentQuantity + change;
                if (newQuantity >= 1 && newQuantity <= currentStock) {
                    currentQuantity = newQuantity;
                    quantity.setText(String.valueOf(currentQuantity));
                } else if (newQuantity < 1) {
                    Toast.makeText(this, "Quantity cannot be less than 1.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Not enough stock for this variation.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Please select a variation first.", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteProductFromFirestore(String productIdToDelete) {
        db.collection("products").document(productIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
                });
    }
    private void addComments(){

        String commentText=commentEditText.getText().toString().trim();
        if(commentText.isEmpty()){
            return;
        }

        Map<String,Object> comment=new HashMap<>();
        comment.put("productId",productId);
        comment.put("userId",userId);
        comment.put("commentText",commentText);
        comment.put("createdAt",FieldValue.serverTimestamp());

        db.collection("comments")
                .add(comment)
                .addOnSuccessListener(v->{
                    commentEditText.setText("");
                    updateCommentCount();
                    loadComment();
                })
                .addOnFailureListener(v-> Toast.makeText(this, "Could not add comment", Toast.LENGTH_SHORT).show());
    }
    private void updateCommentCount() {
        DocumentReference productRef = db.collection("products").document(productId);
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(productRef);
            Long currentCount = snapshot.getLong("comments");
            currentCount++;
            transaction.update(productRef, "comments", currentCount);
            return null;
        });
    }
    private void loadComment() {
        db.collection("comments")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<QueryDocumentSnapshot> commentDocuments = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String commentUserId = document.getString("userId");
                        Task<DocumentSnapshot> userTask = db.collection("users").document(commentUserId).get();
                        userTasks.add(userTask);
                        commentDocuments.add(document);
                    }

                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(userDocumentSnapshots -> {
                                List<Comment> tempCommentList = new ArrayList<>();
                                for (int i = 0; i < userDocumentSnapshots.size(); i++) {
                                    DocumentSnapshot userDocument = (DocumentSnapshot) userDocumentSnapshots.get(i);
                                    QueryDocumentSnapshot commentDocument = commentDocuments.get(i);

                                    String commentName = userDocument.getString("fullname");
                                    String commentProfileUrl = userDocument.getString("profilePictureUrl");
                                    String commentTextDb = commentDocument.getString("commentText");
                                    String commentUserId= userDocument.getId();
                                    String commentId=commentDocument.getId();

                                    Comment comment = new Comment(commentProfileUrl, commentName, commentTextDb,commentId,commentUserId,productId);
                                    comment.setCreatedAt(commentDocument.getTimestamp("createdAt"));
                                    tempCommentList.add(comment);
                                }

                                Collections.sort(tempCommentList, (c1, c2) -> {
                                    if (c1.getCreatedAt() == null || c2.getCreatedAt() == null) {
                                        return 0;
                                    }
                                    return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                                });

                                commentList.addAll(tempCommentList);
                                CommentRecyclerView(commentList);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Could not show comments", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(a -> {
                    Toast.makeText(this, "Problem in comments", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProductInfo(){
        DocumentReference productRef= db.collection("products").document(productId);
        productRef.get().addOnSuccessListener(documentSnapshot -> {
            title.setText(documentSnapshot.getString("title"));
            likes.setText(String.valueOf(documentSnapshot.getLong("likes")));
            price.setText("Rs."+documentSnapshot.getDouble("price"));
            description.setText(documentSnapshot.getString("description"));
            sellerUserId=documentSnapshot.getString("userId");
            if(Objects.equals(sellerUserId, userId)|| isAdmin || "Admin".equals(userType)){
                isHideButton =false;
                reportProduct.setVisibility(View.GONE);
            }else{
                editProduct.setVisibility(View.GONE);
                deleteProduct.setVisibility(View.GONE);
            }
            String aspectRatio=documentSnapshot.getString("aspectRatio");

            List<String> imageUrls=(List<String>) documentSnapshot.get("imageUrls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                ImageRecyclerView(imageUrls,aspectRatio);
            }

            List<Map<String,Object>> variationsData=(List<Map<String,Object>>) documentSnapshot.get("variations");
            List<Variation> variations = new ArrayList<>();
            variationStockMap.clear();
            for (Map<String, Object> variationMap : variationsData) {
                String name = (String) variationMap.get("name");
                long stockLong = (long) variationMap.get("stock");
                int stock = (int) stockLong;
                variations.add(new Variation(name, stock));
                variationStockMap.put(name, stock);
            }
            VariationRecyclerView(variations);

            variationsChipGroup.removeAllViews();

            for (Variation variation : variations) {
                Chip chip = new Chip(this);
                chip.setText(variation.getName());
                chip.setClickable(true);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.grey);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                chip.setChipStrokeWidth(0);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Chip currentChip = (Chip) buttonView;
                    if (isChecked) {
                        selectedVariationName = variation.getName();
                        maxAvailableStock = variationStockMap.getOrDefault(selectedVariationName, 0);

                        if (maxAvailableStock == 0) {
                            currentQuantity= 0;
                            quantity.setText("Out of Stock");
                        } else {
                            currentQuantity=1;
                            quantity.setText(String.valueOf(currentQuantity));
                        }
                        quantity.setText(String.valueOf(currentQuantity));

                        currentChip.setChipBackgroundColorResource(R.color.darkred);
                        currentChip.setTextColor(ContextCompat.getColor(this, R.color.white));
                        if (previouslySelectedChip != null && previouslySelectedChip != currentChip) {
                            previouslySelectedChip.setChipBackgroundColorResource(R.color.grey);
                            previouslySelectedChip.setTextColor(ContextCompat.getColor(this, R.color.white));
                        }
                        previouslySelectedChip = currentChip;
                    }
                });

                variationsChipGroup.addView(chip);

                if (variations.indexOf(variation) == 0 && previouslySelectedChip == null) {
                    chip.setChecked(true);
                    previouslySelectedChip = chip;
                    selectedVariationName = variation.getName();
                    maxAvailableStock = variationStockMap.getOrDefault(selectedVariationName, 0);
                    if (maxAvailableStock == 0) {
                        quantity.setText("Out of Stock");
                        currentQuantity = 0;
                    } else {
                        quantity.setText("1");
                        currentQuantity=1;
                    }
                    quantity.setText(String.valueOf(currentQuantity));
                    chip.setChipBackgroundColorResource(R.color.darkred);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
            }

            DocumentReference sellerRef=db.collection("users").document(sellerUserId);
            sellerRef.get().addOnSuccessListener(documentSnapshot1 -> {
                sellerName.setText(documentSnapshot1.getString("fullname"));
                String profilePictureUrl = documentSnapshot1.getString("profilePictureUrl");
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Glide.with(this).load(profilePictureUrl).into(sellerProfile);
                }
                sellerRating.setText(String.valueOf(documentSnapshot1.getDouble("rating")));
            }).addOnFailureListener(e1->{
                Toast.makeText(this, "Something really is wrong", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e->{
            Toast.makeText(this, "Something is wrong i can feel it", Toast.LENGTH_SHORT).show();
        });
    }
    private void ImageRecyclerView(List<String> imageUrls, String aspectRatio) {

        List<Uri> imageUris = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            Uri uri = Uri.parse(imageUrl);
            imageUris.add(uri);
        }
        RecyclerView imageRecyclerView = findViewById(R.id.productInfoImageRecycler);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ImageAdapter imageAdapter = new ImageAdapter(imageUris, aspectRatio,this);
        imageRecyclerView.setAdapter(imageAdapter);
    }

    private void VariationRecyclerView(List<Variation> variations) {
        RecyclerView variationRecyclerView = findViewById(R.id.productInfoVariationRecycler);
        variationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        VariationInfoAdapter variationAdapter = new VariationInfoAdapter(variations,this);
        variationRecyclerView.setAdapter(variationAdapter);
    }

    private void CommentRecyclerView(List<Comment> comments){
        RecyclerView commentRecyclerView=findViewById(R.id.commentRecycler);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter commentAdapter=new CommentAdapter(comments,this,isHideButton,userId);
        commentRecyclerView.setAdapter(commentAdapter);
    }
    private void reportProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.report_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView reportTitle = dialogView.findViewById(R.id.reportText);
        TextView inappropriateContent = dialogView.findViewById(R.id.inappropriateContent);
        TextView misleadingInfo = dialogView.findViewById(R.id.misleadingInformation);
        TextView scamFraud = dialogView.findViewById(R.id.scamOrFraud);
        TextView others = dialogView.findViewById(R.id.others);
        EditText reason = dialogView.findViewById(R.id.reason);
        TextView submit = dialogView.findViewById(R.id.submit);
        TextView cancel = dialogView.findViewById(R.id.cancel);

        reportTitle.setText("Report Product");

        View.OnClickListener quickReport = v -> {
            String selectedReason = ((TextView) v).getText().toString();
            reportProduct(selectedReason);
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
            reportProduct(otherText.isEmpty()?"other":otherText);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void reportProduct(String reason) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> report = new HashMap<>();
        report.put("reportType","product");
        report.put("reportedSellerId", sellerUserId);
        report.put("reportedProductId", productId);
        report.put("reportedBy", userId);
        report.put("reason", reason);
        report.put("status","pending");
        report.put("timestamp", Timestamp.now());

        db.collection("reports")
                .add(report)
                .addOnSuccessListener(doc -> Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show());
    }
}