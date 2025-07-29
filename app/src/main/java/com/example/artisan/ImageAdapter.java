package com.example.artisan;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Collections;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Uri> imageUris;
    private String aspectRatio;
    private Context context;

    public ImageAdapter(List<Uri> imageUris,String aspectRatio,Context context) {
        this.imageUris = imageUris;
        this.aspectRatio=aspectRatio;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (aspectRatio != null && aspectRatio.equals("Portrait")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_portrait, parent, false);
        } else if (aspectRatio != null && aspectRatio.equals("Landscape")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_landscape, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(imageUris, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(imageUris, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }
    }


}