package com.example.artisan;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderInfo extends AppCompatActivity {

    private ImageView back, image ,addRatingImage, trackingImage;
    private TextView productName, variationName, quantity, totalPrice, payment, status;
    private TextView buyerName, buyerNumber, buyerAddress, landmarkInfo, deliveryInfo;
    private EditText ratingComment;
    private RecyclerView ratingImageRecycler;
    private LinearLayout ratingLinear;
    private Button outForDeliveryStatus,receivedStatus, cancelOrder, rateSeller, refund, delivered;
    private Boolean isOrdered=false,isCOD=false, isSeller=false ,isDelivered=false, isReceived=false;
    private String orderId, userType, userId, productNameText, variation,productId ,sellerId, trackingImageUrl;
    private Double totalAmount;
    private int quantityOrdered;
    private RatingBar ratingBar;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private static final int PICK_IMAGES_REQUEST = 1;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private List<String> cloudinaryImageUrls;
    private ImageView currentTrackingImageView;
    private TextView currentAddImageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getStringExtra("OrderId");
        userType = ((MyApplication) getApplication()).getUsertype();
        isSeller = userType.equals("Seller");

        db=FirebaseFirestore.getInstance();
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();
        loadInformation();

        MyApplication app = (MyApplication) getApplicationContext();
        userType = app.getUsertype();
        if("Admin".equals(userType)) {
            refund.setVisibility(View.VISIBLE);
            delivered.setVisibility(View.VISIBLE);
        }else{
            refund.setVisibility(View.GONE);
            delivered.setVisibility(View.GONE);
        }

        trackingImage.setOnClickListener(v->{
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(this).load(trackingImageUrl).into(dialogImage);
            dialog.show();
        });
        back.setOnClickListener(v->finish());
        outForDeliveryStatus.setOnClickListener(v-> outForDeliveryDialog());
        receivedStatus.setOnClickListener(v-> receivedStatusDialog());
        cancelOrder.setOnClickListener(v-> cancelOrderDialog());
        addRatingImage.setOnClickListener(v-> addImagesFromMobile());
        rateSeller.setOnClickListener(v->settingRating());
        refund.setOnClickListener(v->changeStatus("Refunded"));
        delivered.setOnClickListener(v->changeStatus("Delivered"));
    }
    private void initializeViews() {
        back = findViewById(R.id.back);
        image = findViewById(R.id.image);

        productName = findViewById(R.id.productName);
        variationName = findViewById(R.id.variationName);
        quantity = findViewById(R.id.quantity);
        totalPrice = findViewById(R.id.totalPrice);

        buyerName = findViewById(R.id.buyerName);
        buyerNumber = findViewById(R.id.buyerNumber);
        buyerAddress = findViewById(R.id.buyerAddress);
        landmarkInfo = findViewById(R.id.landmarkInfo);
        deliveryInfo = findViewById(R.id.deliveryInfo);

        payment = findViewById(R.id.paymentTextView);
        status = findViewById(R.id.statusTextView);
        trackingImage = findViewById(R.id.trackingImage);

        outForDeliveryStatus = findViewById(R.id.markAsOD);
        receivedStatus = findViewById(R.id.markAsReceived);
        cancelOrder = findViewById(R.id.cancelOrder);

        rateSeller = findViewById(R.id.rateSeller);

        ratingLinear=findViewById(R.id.ratingLinearLayout);
        ratingBar = findViewById(R.id.ratingBar);
        ratingComment = findViewById(R.id.ratingComment);
        addRatingImage = findViewById(R.id.addRatingImage);
        ratingImageRecycler =findViewById(R.id.ratingImageRecycler);

        refund= findViewById(R.id.refund);
        delivered= findViewById(R.id.delivered);
    }
    private void addImagesFromMobile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            int remainingSlots = 5 - selectedImageUris.size();

            if (remainingSlots <= 0) {
                Toast.makeText(this, "You can only add up to 5 photos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), remainingSlots);
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null && remainingSlots > 0) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
            }

            if (imageAdapter == null) {
                imageAdapter = new ImageAdapter(selectedImageUris, "Square", this);
                ratingImageRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                ratingImageRecycler.setAdapter(imageAdapter);
                ratingImageRecycler.setVisibility(View.VISIBLE);
            } else {
                imageAdapter.notifyDataSetChanged();
            }
        }
    }
    private void settingRating() {
        float stars = ratingBar.getRating();
        String comment = ratingComment.getText().toString().trim();

        if (stars == 0) {
            Toast.makeText(this, "Please provide a star rating", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUris.size() > 0) {
            cloudinaryImageUrls = new ArrayList<>(Collections.nCopies(selectedImageUris.size(), null));
            int imageCount = selectedImageUris.size();
            final int[] uploadedCount = {0};

            for (int i = 0; i < selectedImageUris.size(); i++) {
                final int index = i;
                Uri imageUri = selectedImageUris.get(i);

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
                                if (imageUrl.startsWith("http://")) {
                                    imageUrl = imageUrl.replace("http://", "https://");
                                }
                                cloudinaryImageUrls.set(index, imageUrl);
                                uploadedCount[0]++;

                                if (uploadedCount[0] == imageCount && !cloudinaryImageUrls.contains(null)) {
                                    saveRatingToFirestore(stars, comment, cloudinaryImageUrls);
                                }
                            }
                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Toast.makeText(OrderInfo.this, "Error uploading image: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {}
                        })
                        .dispatch();
            }
        } else {
            saveRatingToFirestore(stars, comment, new ArrayList<>());
        }
    }
    private void saveRatingToFirestore(float stars, String comment, List<String> imageUrls) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("stars", stars);
        ratingMap.put("comment", comment);
        ratingMap.put("ratingImageUrls", imageUrls);
        ratingMap.put("ratingTimestamp", FieldValue.serverTimestamp());

        db.collection("orders").document(orderId)
                .update("rating", ratingMap)
                .addOnSuccessListener(unused -> {
                    updateSellerRating(stars);
                    Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateSellerRating(float stars) {
        if (sellerId == null || sellerId.isEmpty()) {
            return;
        }

        DocumentReference sellerRef = db.collection("users").document(sellerId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(sellerRef);

            if (!snapshot.exists()) {
                return null;
            }

            double totalStars = snapshot.contains("totalStars") && snapshot.getDouble("totalStars") != null
                    ? snapshot.getDouble("totalStars") : 0.0;
            long ratingCount = snapshot.contains("ratingCount") && snapshot.getLong("ratingCount") != null
                    ? snapshot.getLong("ratingCount") : 0;

            totalStars += stars;
            ratingCount++;

            double average = totalStars / ratingCount;

            double rating = Math.round(average * 10.0) / 10.0;

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("totalStars", totalStars);
            updateMap.put("ratingCount", ratingCount);
            updateMap.put("rating", rating);

            transaction.update(sellerRef, updateMap);
            return null;
        }).addOnSuccessListener(aVoid -> {
            finish();
        }).addOnFailureListener(e -> {
            Log.d("RatingSeller","Update failed");
        });
    }
    private void receivedStatusDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder
                (OrderInfo.this,R.style.MyAlertDialog);
        builder.setMessage("Are you sure you want to mark order as\n\"Received\"");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.collection("orders").document(orderId)
                                .update(
                                        "status", "Received",
                                        "receivedTimestamp", FieldValue.serverTimestamp()
                                )
                                .addOnSuccessListener(aVoid -> {
                                    productRevenue();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(OrderInfo.this, "Failed to update order", Toast.LENGTH_SHORT).show();
                                });
                        dialog.dismiss();
                        finish();
                    }
                });
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

//        Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
//        if (positiveButton != null) {
//            positiveButton.setTextColor(getResources().getColor(R.color.red));
//        }
    }
    private void cancelOrderDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder
                (OrderInfo.this,R.style.MyAlertDialog);
        builder.setMessage("Are you sure you want to cancel order?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes,Cancel order",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isCOD) changeStatus("Cancelled");
                        else changeStatus("Pending refund");
                        cancelOrder();
                        dialog.dismiss();
                        finish();
                    }
                });
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.red));
        }
    }
    private void outForDeliveryDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.change_status_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        currentTrackingImageView = dialogView.findViewById(R.id.trackingImage);
        currentAddImageTextView = dialogView.findViewById(R.id.addPhoto);

        TextView yes=dialogView.findViewById(R.id.yes);
        TextView cancel=dialogView.findViewById(R.id.cancel);

        currentAddImageTextView.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        yes.setOnClickListener(v->{
            if (selectedImageUri != null) {
                MediaManager.get().upload(selectedImageUri)
                        .unsigned("artisan_upload")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String uploadedUrl = resultData.get("url").toString();
                                if (uploadedUrl.startsWith("http://")) {
                                    uploadedUrl = uploadedUrl.replace("http://", "https://");
                                }

                                saveTrackingImageUrlToFirestore(uploadedUrl);
                                changeStatus("Out for Delivery");
                                Toast.makeText(OrderInfo.this, "Order marked as Out for Delivery!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Toast.makeText(OrderInfo.this, "Upload error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                            }
                        })
                        .dispatch();
                dialog.dismiss();
                finish();
            } else {
                Toast.makeText(this, "Please add a tracking photo first!", Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(v-> {
            selectedImageUri=null;
            dialog.dismiss();
        });
    }
    private void changeStatus(String status) {
        db.collection("orders").document(orderId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                            sendNotification("Cancelled".equals(status) || "Pending refund".equals(status));
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
                );
    }
    private void cancelOrder(){
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("orderId",orderId).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()){
                      queryDocumentSnapshots.getDocuments().get(0).getReference().delete();
                    }

                }).addOnFailureListener(v->{
                    Log.d("Notification","Notification not found");
                });
    }
    private void sendNotification(boolean isOrderCancelled) {
        db.collection("orders").document(orderId).get().addOnSuccessListener(orderSnapshot -> {
            if (!orderSnapshot.exists()) return;

            String buyerId = orderSnapshot.getString("buyerId");
            String sellerId = orderSnapshot.getString("sellerId");
            String variation = orderSnapshot.getString("variationName");
            String qty = String.valueOf(orderSnapshot.getLong("quantity"));

            String message,title,receiverId;

            if (isOrderCancelled) {
                title="Order Cancelled";
                if (userType.equals("Buyer")) {
                    receiverId = sellerId;
                    message = "Order for \"" + productNameText + "\" (" + variation + ", " + qty + " pcs) was cancelled by the buyer.";
                } else {
                    receiverId = buyerId;
                    message = "Your order for \"" + productNameText + "\" (" + variation + ", " + qty + " pcs) was cancelled by the seller.";
                }
            } else {
                title = "Order being Delivered";
                receiverId = buyerId;
                message = "Your order for \"" + productNameText + "\" (" + variation + ", " + qty + " pcs) is out for delivery.";
            }
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("orderId",orderId);
            notificationData.put("message", message);
            notificationData.put("timestamp", FieldValue.serverTimestamp());
            notificationData.put("title", title);

            db.collection("users").document(receiverId)
                    .collection("notifications")
                    .add(notificationData)
                    .addOnSuccessListener(v -> {
                        if(isOrderCancelled){
                            updateProductStockAndOrderCount();
                        }
                        ((MyApplication) getApplication()).sendFCMToUser(receiverId, notificationData, this);
                    });
        });
    }
    private void updateProductStockAndOrderCount(){
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                List<Map<String, Object>> variations = (List<Map<String, Object>>) snapshot.get("variations");
                Long orderCount=snapshot.getLong("orderCount");
                orderCount--;
                if (variations != null) {
                    for (Map<String, Object> var : variations) {
                        String name = (String) var.get("name");
                        if (name != null && name.equals(variation)) {
                            Number stockNumber = (Number) var.get("stock");
                            long currentStock = stockNumber != null ? stockNumber.longValue() : 0;
                            var.put("stock", currentStock + quantityOrdered);
                            break;
                        }
                    }
                    productRef.update("variations", variations);
                    productRef.update("orderCount", orderCount);
                }
            }
        });
    }
    private void productRevenue(){
        db.collection("products").document(productId).update("totalRevenue",FieldValue.increment(totalAmount))
                .addOnSuccessListener(v->{
                    Toast.makeText(this, "Order marked as received", Toast.LENGTH_SHORT).show();
                });
    }
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && currentTrackingImageView != null && currentAddImageTextView != null) {
                    currentTrackingImageView.setImageURI(uri);
                    currentAddImageTextView.setVisibility(View.GONE);
                    selectedImageUri = uri;
                }
            }
    );
    private void saveTrackingImageUrlToFirestore(String trackingUrl) {

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("trackingImageUrl", trackingUrl);
        updateData.put("OFDtimestamp",FieldValue.serverTimestamp());

        db.collection("orders").document(orderId).update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tracking info uploaded successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update tracking info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadInformation() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(orderSnapshot -> {
            if (orderSnapshot.exists()) {
                productId = orderSnapshot.getString("productId");
                String buyerId = orderSnapshot.getString("buyerId");

                sellerId=orderSnapshot.getString("sellerId");
                variationName.setText(orderSnapshot.getString("variationName"));
                variation=orderSnapshot.getString("variationName");

                quantity.setText(orderSnapshot.getLong("quantity") + " pcs");
                quantityOrdered = orderSnapshot.getLong("quantity").intValue();

                totalAmount=orderSnapshot.getDouble("totalAmount");
                totalPrice.setText(String.valueOf( totalAmount));
                payment.setText(orderSnapshot.getString("paymentOption"));
                if(orderSnapshot.getString("paymentOption").equals("COD")) isCOD=true;
                status.setText(orderSnapshot.getString("status"));
                if(orderSnapshot.getString("status").equals("Ordered")) isOrdered=true;
                if(orderSnapshot.getString("status").equals("Received")) isReceived=true;
                if(orderSnapshot.getString("status").equals("Out for Delivery") && !isSeller) receivedStatus.setVisibility(View.VISIBLE);
                if(orderSnapshot.getString("status").equals("Pending refund"))delivered.setVisibility(View.GONE);
                trackingImageUrl= orderSnapshot.getString("trackingImageUrl");
                Glide.with(this).load(trackingImageUrl).into(trackingImage);

                if(orderSnapshot.get("rating")==null && !isSeller && isReceived) ratingLinear.setVisibility(View.VISIBLE);

                if(isOrdered){
                    if(isSeller) outForDeliveryStatus.setVisibility(View.VISIBLE);
                    cancelOrder.setVisibility(View.VISIBLE);
                }

                Map<String, Object> address = (Map<String, Object>) orderSnapshot.get("buyerAddress");
                if (address != null) {
                    buyerNumber.setText(String.valueOf(address.get("mobile")));
                    String province = (String) address.get("province");
                    String district = (String) address.get("district");
                    String city = (String) address.get("city");
                    String ward = String.valueOf(address.get("ward"));
                    String tole = (String) address.get("tole");

                    String formattedAddress = (province != null ? province : "") + ", " +
                            (district != null ? district : "") + ", " +
                            (city != null ? city : "") + (ward != null && !ward.equals("null") && !ward.isEmpty() ? "-" + ward : "") +
                            (tole != null && !tole.equals("null") && !tole.isEmpty() ? ", " + tole : "");

                    buyerAddress.setText(formattedAddress.trim()
                            .replaceAll("^,+|(?<=,),+|,+$", "")
                            .replaceAll(", ,", ","));

                    landmarkInfo.setText(String.valueOf(address.get("landmark")));
                    deliveryInfo.setText(String.valueOf(address.get("instructions")));
                }

                db.collection("products").document(productId).get().addOnSuccessListener(productSnapshot -> {
                    if (productSnapshot.exists()) {
                        productName.setText(productSnapshot.getString("title"));
                        productNameText=productSnapshot.getString("title");
                        List<String> imageUrls = (List<String>) productSnapshot.get("imageUrls");
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            Glide.with(this).load(imageUrls.get(0)).into(image);
                        }
                    }
                });

                db.collection("users").document(buyerId).get().addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        buyerName.setText(userSnapshot.getString("fullname"));
                    }
                });

            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load order info", Toast.LENGTH_SHORT).show();
        });
    }
}