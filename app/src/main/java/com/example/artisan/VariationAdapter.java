package com.example.artisan;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class VariationAdapter extends RecyclerView.Adapter<VariationAdapter.VariationViewHolder> {
    private List<Variation> variationList;
    public VariationAdapter(List<Variation> variationList) {
        this.variationList = variationList;
    }
    @NonNull
    @Override
    public VariationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.variation_item, parent, false);
        return new VariationViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull VariationViewHolder holder, int position) {
        Variation variation = variationList.get(position);
        holder.variationName.setText(variation.getName());
        holder.stockValue.setText(String.valueOf(variation.getStock()));
        holder.variationName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                variation.setName(s.toString());
            }
        });

        holder.stockValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    variation.setStock(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    variation.setStock(0);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return variationList.size();
    }

    public List<Variation> getVariations() {
        return variationList;
    }
    public class VariationViewHolder extends RecyclerView.ViewHolder {
        public EditText variationName;
        public EditText stockValue;
        public ImageButton removeVariationButton;

        public VariationViewHolder(View itemView) {
            super(itemView);
            variationName = itemView.findViewById(R.id.variationName);
            variationName.addTextChangedListener(new CustomTextWatcher(variationName,10));
            stockValue = itemView.findViewById(R.id.stockValue);
            stockValue.addTextChangedListener(new CustomTextWatcher(stockValue,3));
            removeVariationButton = itemView.findViewById(R.id.removeVariationButton);

            removeVariationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    variationList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }
}