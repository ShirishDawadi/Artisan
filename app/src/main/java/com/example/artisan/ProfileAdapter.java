package com.example.artisan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<Profile> profileList;
    private Context context;

    public ProfileAdapter(List<Profile> profileList,Context context){
        this.profileList=profileList;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_display, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile=profileList.get(position);

        if (profile.getSellerProfileUrl() != null && !profile.getSellerProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(profile.getSellerProfileUrl())
                    .into(holder.sellerProfilePicture);
        } else {
            holder.sellerProfilePicture.setImageResource(R.drawable.man);
        }

        holder.sellerName.setText(profile.getSellerName());
        holder.likes.setText(String.valueOf( profile.getTotalLikes()));
        holder.rating.setText(String.valueOf(profile.getRating()));
        holder.itemView.setOnClickListener(v->{
            Intent intent= new Intent(context,SellerProfile.class);
            intent.putExtra("sellerId",profile.getSellerId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView sellerProfilePicture;
        private TextView sellerName,likes,rating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sellerProfilePicture=itemView.findViewById(R.id.sellerProfilePicture);
            sellerName=itemView.findViewById(R.id.sellerNameTextView);
            likes=itemView.findViewById(R.id.likesTextView);
            rating=itemView.findViewById(R.id.ratingTextView);
        }
    }
    public void setProfileList(List<Profile> newList) {
        this.profileList = newList;
    }
}
