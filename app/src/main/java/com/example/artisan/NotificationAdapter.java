package com.example.artisan;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context,List<Notification> notificationList){
        this.context=context;
        this.notificationList=notificationList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.notification_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification= notificationList.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());

        if(notification.getProfileUrl()!=null) {
            Glide.with(context)
                    .load(notification.getProfileUrl())
                    .into(holder.notificationProfile);
        }else{
            holder.notificationProfile.setImageResource(R.drawable.man);
        }
        holder.itemView.setOnClickListener(v->{
            if(notification.getOrderId() == null) return;
            Intent intent= new Intent(context,OrderInfo.class);
            intent.putExtra("OrderId",notification.getOrderId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView notificationProfile;
        private TextView title,message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationProfile=itemView.findViewById(R.id.notificationProfile);
            title=itemView.findViewById(R.id.notificationTitle);
            message=itemView.findViewById(R.id.notificationMessage);
        }
    }

}
