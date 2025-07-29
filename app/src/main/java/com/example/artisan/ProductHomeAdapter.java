package com.example.artisan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;


public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private boolean isSingleLineView = true;
    private FirebaseDatabase db;

    public ProductHomeAdapter(List<Product> productList, Context context) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductHomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_product_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_product_grid_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHomeAdapter.ViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product.getSellerProfileUrl() != null && !product.getSellerProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getSellerProfileUrl())
                    .into(holder.sellerProfilePicture);
        } else {
            holder.sellerProfilePicture.setImageResource(R.drawable.man);
        }
        holder.sellerName.setText(product.getSellerName());
        holder.sellerRating.setText(String.valueOf(product.getSellerRating()));
        holder.productTitle.setText(product.getProductName());
        holder.likes.setText(String.valueOf(product.getProductLikes()));
        holder.comments.setText(String.valueOf(product.getComments()));
        holder.productPrice.setText("Rs." + product.getProductPrice());

        holder.sellerName.setOnClickListener(v->openSellerAccount(product.getSellerId()));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db = FirebaseDatabase.getInstance("https://artisan-c6819-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference likeRef = db.getReference("Likes")
                .child(product.getProductID()).child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (isLiked) {
                    holder.likebutton.setColorFilter(ContextCompat.getColor(context, R.color.darkred));
                    holder.likebutton.setTag("liked");
                } else {
                    holder.likebutton.setColorFilter(ContextCompat.getColor(context, R.color.white));
                    holder.likebutton.setTag("unliked");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.likebutton.setColorFilter(ContextCompat.getColor(context, R.color.white));
            }
        });

        holder.likebutton.setOnClickListener(v -> toggleLike(holder.likebutton, holder.likes, product.getProductID()));

        holder.imageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        ImageHomeAdapter imageHomeAdapter = new ImageHomeAdapter(product.getProductImageUrls(), product.getAspectRatio(), context);
        holder.imageRecyclerView.setAdapter(imageHomeAdapter);

        holder.productTitle.setOnClickListener(v->openSellerAccount(product.getSellerId()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductInfo.class);
            intent.putExtra("productId", product.getProductID());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isSingleLineView ? 0 : 1;
    }

    public void setLayoutMode(boolean isSingleLineView) {
        this.isSingleLineView = isSingleLineView;
        notifyDataSetChanged();
    }

    public void setProductList(List<Product> newList) {
        this.productList = newList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView sellerProfilePicture, likebutton;
        private TextView sellerName, sellerRating, productTitle, likes, comments, productPrice;
        private RecyclerView imageRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            sellerProfilePicture = itemView.findViewById(R.id.homeSellerProfilePicture);
            sellerName = itemView.findViewById(R.id.homeSellerName);
            sellerRating = itemView.findViewById(R.id.homeSellerRating);
            imageRecyclerView = itemView.findViewById(R.id.homeImageRecycler);
            productTitle = itemView.findViewById(R.id.homeProductTitle);
            likebutton = itemView.findViewById(R.id.homeLikeButton);
            likes = itemView.findViewById(R.id.homeProductLikes);
            comments = itemView.findViewById(R.id.homeProductComments);
            productPrice = itemView.findViewById(R.id.homeProductPrice);
        }
    }

    private void openSellerAccount(String sellerId){
        Intent intent = new Intent(context, SellerProfile.class);
        intent.putExtra("sellerId", sellerId);
        context.startActivity(intent);
    }
    private void toggleLike(ImageView likeButton, TextView likesView, String productID) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference productLikeRef = db.getReference("Likes").child(productID).child(userId);

        Object tag = likeButton.getTag();
        boolean isCurrentlyLiked = tag != null && tag.equals("liked");

        boolean willLike = !isCurrentlyLiked;
        int currentLikes = Integer.parseInt(likesView.getText().toString());
        int updatedLikes = willLike ? currentLikes + 1 : Math.max(currentLikes - 1, 0);

        likesView.setText(String.valueOf(updatedLikes));
        likeButton.setColorFilter(ContextCompat.getColor(context, willLike ? R.color.darkred : R.color.white));
        likeButton.setTag(willLike ? "liked" : "unliked");

        if (willLike) {
            productLikeRef.setValue(true).addOnSuccessListener(aVoid ->
                    updateProductLikes(productID, 1));
                    updateUserRecommendedTags(productID, true);
        } else {
            productLikeRef.removeValue().addOnSuccessListener(aVoid ->
                    updateProductLikes(productID, -1));
                    updateUserRecommendedTags(productID, false);
        }
    }
    private void updateProductLikes(String productID, int change) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productID);

        productRef.update("likes", FieldValue.increment(change))
                .addOnSuccessListener(aVoid -> {
                    productRef.get().addOnSuccessListener(snapshot -> {
                        String sellerId = snapshot.getString("userId");
                        if (sellerId != null) {
                            DocumentReference sellerRef = db.collection("users").document(sellerId);
                            sellerRef.update("totalLikes", FieldValue.increment(change));
                        }
                    });
                });
    }

    private void updateUserRecommendedTags(String productID, boolean isLiking) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("products").document(productID).get().addOnSuccessListener(snapshot -> {
            List<String> productTags = (List<String>) snapshot.get("tags");
            if (productTags == null) return;

            DocumentReference userRef = firestore.collection("users").document(userId);

            userRef.get().addOnSuccessListener(userSnap -> {
                if (!userSnap.exists()) return;

                Map<String, Long> currentTags = (Map<String, Long>) userSnap.get("recommended.tags");
                if (currentTags == null) currentTags = new HashMap<>();

                for (String tag : productTags) {
                    long currentValue = currentTags.containsKey(tag) ? currentTags.get(tag) : 0;

                    if (isLiking) {
                        currentValue = Math.min(currentValue + 1, 5);
                    } else {
                        currentValue--;
                    }

                    if (currentValue > 0) {
                        currentTags.put(tag, currentValue);
                    } else {
                        currentTags.remove(tag);
                    }
                }
                userRef.update("recommended.tags", currentTags);
            });
        }).addOnFailureListener(e -> Log.e("TAG_UPDATE", "Failed to get product tags", e));
    }
}
