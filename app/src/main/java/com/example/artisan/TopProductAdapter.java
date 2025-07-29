package com.example.artisan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private int selectedTabPosition = 0;
    public TopProductAdapter(List<Product> productList,Context context){
        this.productList=productList;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_product_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getProductName());
        Glide.with(context)
                .load(product.getProductImageurl())
                .into(holder.productImage);

        if (selectedTabPosition == 0) {
            holder.productRevenue.setVisibility(View.VISIBLE);
            holder.productOrderCount.setVisibility(View.GONE);
            holder.productLikesLinear.setVisibility(View.GONE);
            holder.productRevenue.setText("Rs." + product.getRevenue());
        } else if (selectedTabPosition == 1) {
            holder.productRevenue.setVisibility(View.GONE);
            holder.productOrderCount.setVisibility(View.VISIBLE);
            holder.productLikesLinear.setVisibility(View.GONE);
            holder.productOrderCount.setText(product.getOrderCount() + " orders");
        } else if (selectedTabPosition == 2) {
            holder.productRevenue.setVisibility(View.GONE);
            holder.productOrderCount.setVisibility(View.GONE);
            holder.productLikesLinear.setVisibility(View.VISIBLE);
            holder.productLikes.setText(String.valueOf(product.getProductLikes()));
        }
    }
    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private LinearLayout productLikesLinear;
        private TextView productName,productLikes,productOrderCount,productRevenue;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage= itemView.findViewById(R.id.productImage);
            productLikesLinear= itemView.findViewById(R.id.likesLinear);
            productName=itemView.findViewById(R.id.productName);
            productLikes=itemView.findViewById(R.id.productLikes);
            productOrderCount=itemView.findViewById(R.id.productOrders);
            productRevenue=itemView.findViewById(R.id.productRevenue);
        }
    }
    public void setSelectedTabPosition(int position) {
        this.selectedTabPosition = position;
        notifyDataSetChanged();
    }
}
