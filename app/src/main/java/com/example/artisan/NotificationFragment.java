package com.example.artisan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView notificationRecycler;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList= new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId,userType;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_notification, container, false);

        notificationRecycler=view.findViewById(R.id.notificationRecycler);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationAdapter = new NotificationAdapter(getContext(),notificationList);
        notificationRecycler.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userType = ((MyApplication) requireActivity().getApplication()).getUsertype();

        loadNotification();
        return view;
    }
    private void loadNotification() {
        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();

                    for (var doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String orderId = doc.getString("orderId");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        String cancelledProfile =doc.getString("profileUrl");

                        if(cancelledProfile!=null){
                            Notification notification = new Notification(cancelledProfile, title, message);
                            notification.setTimestamp(timestamp);
                            notificationList.add(notification);

                            Collections.sort(notificationList, (a, b) -> b.getTimeStamp().compareTo(a.getTimeStamp()));
                            notificationAdapter.notifyDataSetChanged();
                        }

                        if(orderId!=null) {
                            db.collection("orders").document(orderId).get()
                                    .addOnSuccessListener(orderDoc -> {
                                        if (orderDoc.exists()) {
                                            String Id = "Buyer".equals(userType)
                                                    ? orderDoc.getString("sellerId")
                                                    : orderDoc.getString("buyerId");

                                            db.collection("users").document(Id).get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        if (userDoc.exists()) {
                                                            String profileSenderUrl = userDoc.getString("profilePictureUrl");

                                                            Notification notification = new Notification(profileSenderUrl, title, message, orderId);
                                                            notification.setTimestamp(timestamp);
                                                            notificationList.add(notification);

                                                            Collections.sort(notificationList, (a, b) -> b.getTimeStamp().compareTo(a.getTimeStamp()));
                                                            notificationAdapter.notifyDataSetChanged();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}