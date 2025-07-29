package com.example.artisan;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context context;
    private boolean hideDeleteButton;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public CommentAdapter(List<Comment> commentList,Context context,boolean hideDeleteButton,String currentUserId){
        this.commentList=commentList;
        this.context=context;
        this.hideDeleteButton=hideDeleteButton;
        this.currentUserId=currentUserId;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment= commentList.get(position);

        if (comment.getCommentProfileUrl() != null && !comment.getCommentProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(comment.getCommentProfileUrl())
                    .into(holder.commentProfile);
        } else {
            holder.commentProfile.setImageResource(R.drawable.man);
        }

        holder.commentName.setText(comment.getCommentName());
        holder.commentText.setText(comment.getCommentText());

        if (!hideDeleteButton||currentUserId != null && currentUserId.equals(comment.getCommentUserId())) {
            holder.commentDeleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.commentDeleteButton.setVisibility(View.GONE);
        }
        holder.commentDeleteButton.setOnClickListener(v -> {
            String commentId = comment.getCommentId();
            String productId= comment.getProductId();
            if (commentId != null && !commentId.isEmpty()) {
                deleteComment(commentId, position, productId);
            } else {
                Toast.makeText(context, "Comment ID not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView commentProfile;
        private TextView commentName;
        private TextView commentText;
        private ImageView commentDeleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentProfile=itemView.findViewById(R.id.commentProfilePicture);
            commentName=itemView.findViewById(R.id.commentName);
            commentText=itemView.findViewById(R.id.commentText);
            commentDeleteButton=itemView.findViewById(R.id.commentDeleteButton);
        }
    }
    private void deleteComment(String commentId, int position,String productId) {
        db.collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                    decrementCommentCount(productId);
                    commentList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, commentList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                });
    }
    private void decrementCommentCount(String productId) {
        DocumentReference productRef = db.collection("products").document(productId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(productRef);
            Long currentCount = snapshot.getLong("comments");
            long newCount = 0;
            if (currentCount != null && currentCount > 0) {
                newCount = currentCount - 1;
            }
            transaction.update(productRef, "comments", newCount);
            return null;
        });
    }
}
