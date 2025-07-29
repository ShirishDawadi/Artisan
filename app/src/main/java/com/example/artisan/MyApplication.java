package com.example.artisan;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyApplication extends Application {

    private String usertype;
    private String name;
    public String getUsertype() {
        return usertype;
    }
    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MediaManager.init(this);
    }

    public void sendFCMToUser(String userId, HashMap<String, Object> notifData, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken == null) return;

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        try {
                            String accessToken = AccessTokenGenerator.getAccessToken(context);

                            JSONObject notification = new JSONObject();
                            notification.put("title", notifData.get("title"));
                            notification.put("body", notifData.get("message"));

                            JSONObject message = new JSONObject();
                            message.put("token", fcmToken);
                            message.put("notification", notification);

                            JSONObject json = new JSONObject();
                            json.put("message", message);

                            sendFCMRequest(json, accessToken);
                        } catch (Exception e) {
                            Log.e("FCM", "Failed to build JSON", e);
                        }
                    });
                });
    }

    private void sendFCMRequest(JSONObject jsonBody, String accessToken) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/artisan-c6819/messages:send")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FCM", "Failed to send", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("FCM", "FCM Notification sent successfully");
                } else {
                    Log.e("FCM", "Error sending FCM: " + response.code() + " - " + response.body().string());
                }
            }
        });
    }

}