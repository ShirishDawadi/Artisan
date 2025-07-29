package com.example.artisan;

import android.app.Dialog;
import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {
    private List<Rating> ratingList;
    private Context context;

    public RatingAdapter(Context context, List<Rating> ratingList){
        this.context=context;
        this.ratingList=ratingList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rating rating=ratingList.get(position);
        holder.name.setText(rating.getBuyerName());
        holder.rating.setText(String.valueOf(rating.getStars()));
        holder.comment.setText(rating.getRatingComment());
        Glide.with(context).load(rating.getProfileUrl()).into(holder.profile);

        List<String> imageUrls = rating.getRatingImages();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageView[] imageViews = {holder.image1, holder.image2, holder.image3, holder.image4, holder.image5};
            for (int i = 0; i < Math.min(imageUrls.size(), 5); i++) {
                imageViews[i].setVisibility(View.VISIBLE);
                Glide.with(context).load(imageUrls.get(i)).into(imageViews[i]);
            }
        }
        holder.image1.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(context).load(rating.getRatingImages().get(0)).into(dialogImage);
            dialog.show();
        });
        holder.image2.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(context).load(rating.getRatingImages().get(1)).into(dialogImage);
            dialog.show();
        });
        holder.image3.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(context).load(rating.getRatingImages().get(2)).into(dialogImage);
            dialog.show();
        });
        holder.image4.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(context).load(rating.getRatingImages().get(3)).into(dialogImage);
            dialog.show();
        });
        holder.image5.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
            Glide.with(context).load(rating.getRatingImages().get(4)).into(dialogImage);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView profile,image1,image2,image3,image4,image5;
        private TextView name,rating,comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.buyerProfile);
            image1=itemView.findViewById(R.id.ratingImage1);
            image2=itemView.findViewById(R.id.ratingImage2);
            image3=itemView.findViewById(R.id.ratingImage3);
            image4=itemView.findViewById(R.id.ratingImage4);
            image5=itemView.findViewById(R.id.ratingImage5);

            name=itemView.findViewById(R.id.ratingName);
            rating=itemView.findViewById(R.id.ratingText);
            comment=itemView.findViewById(R.id.ratingComment);
        }
    }
}
