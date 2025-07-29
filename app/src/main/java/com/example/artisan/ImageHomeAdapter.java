package com.example.artisan;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ImageHomeAdapter extends RecyclerView.Adapter<ImageHomeAdapter.ViewHolder> {

    private List<String> imageUris;
    private String aspectRatio;
    private Context context;
    public ImageHomeAdapter(List<String> imageUris,String aspectRatio,Context context) {
        this.imageUris = imageUris;
        this.aspectRatio=aspectRatio;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.image_home_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUri = imageUris.get(position);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.images.getLayoutParams();
        if (aspectRatio.equals("Square")) {
            params.dimensionRatio = "1:1";
        } else if (aspectRatio.equals("Portrait")) {
            params.dimensionRatio = "4:5";
        } else if (aspectRatio.equals("Landscape")) {
            params.dimensionRatio = "4:3";
        }

        holder.images.setLayoutParams(params);
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(holder.images);
    }
    @Override
    public int getItemCount() {
//        return imageUris.size();
        return imageUris != null ? imageUris.size() : 0;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView images;
        public ViewHolder(View view) {
            super(view);
            images = view.findViewById(R.id.productImages);
        }
    }
}