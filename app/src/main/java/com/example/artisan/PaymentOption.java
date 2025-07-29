package com.example.artisan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.f1soft.esewapaymentsdk.ui.screens.EsewaPaymentActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.f1soft.esewapaymentsdk.EsewaConfiguration;
import com.f1soft.esewapaymentsdk.EsewaPayment;


public class PaymentOption extends AppCompatActivity {

    private ImageView productImage,back;
    private TextView productNameTextView;
    private TextView variationNameTextView;
    private TextView quantityTextView;
    private TextView totalPriceTextView;
    private TextView buyerNameTextView;
    private TextView buyerNumberTextView;
    private TextView buyerAddressTextView;
    private TextView landmarkInfoTextView;
    private TextView deliveryInfoTextView;
    private String productId;
    private String name;
    private String variationName;
    private String buyerName,buyerId;
    private String sellerId;
    private Double totalPrice;
    private int quantity;
    private HashMap<String, Object> shippingAddress;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CardView eSewaPay,cod;
    private boolean isPaid=false;
    private static final int REQUEST_CODE_PAYMENT = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_option);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productImage = findViewById(R.id.image);
        productNameTextView = findViewById(R.id.productName);
        variationNameTextView = findViewById(R.id.variationName);
        quantityTextView = findViewById(R.id.quantity);
        totalPriceTextView = findViewById(R.id.totalPrice);
        buyerNameTextView = findViewById(R.id.buyerName);
        buyerNumberTextView = findViewById(R.id.buyerNumber);
        buyerAddressTextView = findViewById(R.id.buyerAddress);
        landmarkInfoTextView = findViewById(R.id.landmarkInfo);
        deliveryInfoTextView = findViewById(R.id.deliveryInfo);

        buyerId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        variationName = intent.getStringExtra("variationName");
        quantity = intent.getIntExtra("quantity", 0);
        buyerName=intent.getStringExtra("buyerName");
        shippingAddress = (HashMap<String, Object>) intent.getSerializableExtra("shippingAddress");

        variationNameTextView.setText(variationName);
        quantityTextView.setText(String.valueOf(quantity));
        buyerNameTextView.setText(buyerName);

        if (productId != null) {
            fetchProductDetails(productId);
        }
        fillBuyerDetails();

        EsewaConfiguration eSewaConfiguration = new EsewaConfiguration("JB0BBQ4aD0UqIThFJwAKBgAXEUkEGQUBBAwdOgABHD4DChwUAB0R","BhwIWQQADhIYSxILExMcAgFXFhcOBwAKBgAXEQ==",EsewaConfiguration.ENVIRONMENT_TEST);

        eSewaPay=findViewById(R.id.eSewa);
        eSewaPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount=totalPrice.toString();
                String productName=productNameTextView.getText().toString();
                HashMap<String,String> meow= new HashMap<>();
                String callbackUrl = "https://698a-103-207-83-169.ngrok-free.app";

                EsewaPayment eSewaPayment = new EsewaPayment(amount,productName,productId,callbackUrl,meow);

                Intent intent = new Intent(PaymentOption.this, EsewaPaymentActivity.class);
                intent.putExtra(EsewaConfiguration.ESEWA_CONFIGURATION, eSewaConfiguration);

                intent.putExtra(EsewaPayment.ESEWA_PAYMENT, eSewaPayment);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        });

        back=findViewById(R.id.back);
        back.setOnClickListener(v->finish());
        cod=findViewById(R.id.cod);
        cod.setOnClickListener(v -> handleOrderAndNotification(totalPrice));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) return;
                isPaid=true;
                Toast.makeText(this, "SUCCESSFUL PAYMENT", Toast.LENGTH_SHORT).show();
                handleOrderAndNotification(totalPrice);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Canceled By User", Toast.LENGTH_SHORT).show();
            } else if (resultCode == EsewaPayment.RESULT_EXTRAS_INVALID) {
                if (data == null) return;
                String message = data.getStringExtra(EsewaPayment.EXTRA_RESULT_MESSAGE);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void fetchProductDetails(String productId) {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name = documentSnapshot.getString("title");
                        Double price = documentSnapshot.getDouble("price");
                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                        sellerId=documentSnapshot.getString("userId");
                        if (name != null) {
                            productNameTextView.setText(name);
                        }
                        if (price != null) {
                            totalPrice = price * quantity;
                            totalPriceTextView.setText(String.format("%.2f", totalPrice));
                        }
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            String firstImageUrl = imageUrls.get(0);
                            Glide.with(this)
                                    .load(firstImageUrl)
                                    .placeholder(R.drawable.add_photos)
                                    .error(R.drawable.add_photos)
                                    .into(productImage);
                        } else {
                            productImage.setImageResource(R.drawable.add_photos);
                        }
                    } else {
                        productNameTextView.setText("Product Not Found");
                    }
                })
                .addOnFailureListener(e -> {
                    productNameTextView.setText("Error Loading Product");
                });
    }
    private void fillBuyerDetails() {
        if (shippingAddress != null) {
            buyerNumberTextView.setText(String.valueOf(shippingAddress.get("mobile")));
            String province = String.valueOf(shippingAddress.get("province"));
            String district = String.valueOf(shippingAddress.get("district"));
            String city = String.valueOf(shippingAddress.get("city"));
            String ward = String.valueOf(shippingAddress.get("ward"));
            String tole = String.valueOf(shippingAddress.get("tole"));

            String formattedAddress = (province != null ? province : "") + ", " +
                    (district != null ? district : "") + ", " +
                    (city != null ? city : "") + (ward != null && !ward.equals("null") && !ward.isEmpty() ? "-" + ward : "") +
                    (tole != null && !tole.equals("null") && !tole.isEmpty() ? ", " + tole : "");

            buyerAddressTextView.setText(formattedAddress.trim().replaceAll("^,+|(?<=,),+|,+ $", "").replaceAll(", ,", ","));
            landmarkInfoTextView.setText(String.valueOf(shippingAddress.get("landmark")));
            deliveryInfoTextView.setText(String.valueOf(shippingAddress.get("instructions")));
        } else {
            Toast.makeText(this, "Error: Missing shipping address. Please add it again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handleOrderAndNotification(Double totalAmount) {
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("buyerId", buyerId);
        orderData.put("sellerId", sellerId);
        orderData.put("buyerName", buyerName);
        orderData.put("buyerAddress", shippingAddress);
        orderData.put("productId", productId);
        orderData.put("variationName", variationName);
        orderData.put("quantity", quantity);
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", "Ordered");
        orderData.put("paymentOption",isPaid?"Paid":"COD");
        orderData.put("timestamp", Timestamp.now());

        db.collection("orders").add(orderData)
                .addOnSuccessListener(orderRef -> {

                    HashMap<String, Object> sellerNotif = new HashMap<>();
                    sellerNotif.put("title", "New Order");
                    String sellerMessage = buyerName + " ordered " + quantity + " x " + variationName + " of \"" + name + "\".";
                    sellerNotif.put("message", sellerMessage);
                    sellerNotif.put("timestamp", Timestamp.now());
                    sellerNotif.put("orderId", orderRef.getId());

                    db.collection("users").document(sellerId)
                            .collection("notifications").add(sellerNotif)
                            .addOnSuccessListener(notifRef -> {
                                Toast.makeText(this, "Ordered successfully.", Toast.LENGTH_SHORT).show();

                                ((MyApplication) getApplication()).sendFCMToUser(sellerId, sellerNotif, this);
                                updateProductStock(productId, variationName, quantity);
                                updateProductOrderCount(productId);
                                runOnUiThread(() -> finish());
                            });

                });
    }
    private void updateProductStock(String productId, String variationName, int quantityOrdered) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                List<Map<String, Object>> variations = (List<Map<String, Object>>) snapshot.get("variations");
                if (variations != null) {
                    for (Map<String, Object> variation : variations) {
                        String name = (String) variation.get("name");
                        if (name != null && name.equals(variationName)) {
                            Number stockNumber = (Number) variation.get("stock");
                            long currentStock = stockNumber != null ? stockNumber.longValue() : 0;
                            variation.put("stock", currentStock - quantityOrdered);
                            break;
                        }
                    }
                    productRef.update("variations", variations);
                }
            }
        });
    }
    private void updateProductOrderCount(String productId){
        db.collection("products")
                .document(productId)
                .update("orderCount", FieldValue.increment(1))
                .addOnFailureListener(v->{
                    Log.d("Order Count", "updateProductOrderCount failed ");
                });
    }
}