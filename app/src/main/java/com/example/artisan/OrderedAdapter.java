package com.example.artisan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class OrderedAdapter extends RecyclerView.Adapter<OrderedAdapter.ViewHolder> {

    private List<Ordered> orderedList;
    private Context context;

    public OrderedAdapter(List<Ordered> orderedList, Context context){
        this.context=context;
        this.orderedList=orderedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.ordered_product,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ordered ordered= orderedList.get(position);
        holder.title.setText(ordered.getTitle());
        holder.variationX.setText(ordered.getVariationX());
        holder.total.setText("Rs."+ordered.getTotalPrice());
        holder.paymentOption.setText(ordered.getPaymentOption());
        holder.status.setText(ordered.getStatus());

        Glide.with(context)
                .load(ordered.getImageUrl())
                .into(holder.orderedImage);

        holder.itemView.setOnClickListener(v->{
            Intent intent= new Intent(context,OrderInfo.class);
            intent.putExtra("OrderId",ordered.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderedList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView orderedImage;
        private TextView title,variationX,total,paymentOption,status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderedImage=itemView.findViewById(R.id.orderedImage);
            title=itemView.findViewById(R.id.orderedTitle);
            variationX=itemView.findViewById(R.id.orderedVariation);
            total=itemView.findViewById(R.id.orderedPrice);
            paymentOption=itemView.findViewById(R.id.orderedPayment);
            status=itemView.findViewById(R.id.orderedStatus);
        }
    }
}
