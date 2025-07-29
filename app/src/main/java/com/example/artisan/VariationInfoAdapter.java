package com.example.artisan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artisan.Variation;

import java.util.List;

public class VariationInfoAdapter extends RecyclerView.Adapter<VariationInfoAdapter.ViewHolder> {

    private List<Variation> variations;
    private Context context;

    public VariationInfoAdapter(List<Variation> variations, Context context) {
        this.variations = variations;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.variation_item_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Variation variation = variations.get(position);
        holder.variationName.setText(variation.getName());
        holder.variationStock.setText(String.valueOf(variation.getStock()));
    }

    @Override
    public int getItemCount() {
        return variations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView variationName;
        TextView variationStock;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            variationName = itemView.findViewById(R.id.variationName);
            variationStock = itemView.findViewById(R.id.stockValue);
        }
    }
}