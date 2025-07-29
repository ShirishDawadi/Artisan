package com.example.artisan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Home extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);  //changed systemBars.bottom as 0 to fix the bottom navigation
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        checkUserType();

        FrameLayout frameLayout=findViewById(R.id.framelayout);

        loadFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddProduct.class));
                return true;
            }else if(itemId == R.id.nav_notification){
                loadFragment(new NotificationFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fcmToken = task.getResult();
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseFirestore.getInstance().collection("users")
                                .document(currentUserId)
                                .update("fcmToken", fcmToken)
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved"))
                                .addOnFailureListener(e -> Log.e("FCM", "Failed to save token", e));
                    } else {
                        Log.e("FCM", "Fetching FCM token failed", task.getException());
                    }
                });

    }
    private void loadFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
    }
    private void checkUserType() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userType = documentSnapshot.getString("userType");
                if("Admin".equals(userType)){
                    Toast.makeText(this, "Loading admin panel", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,AdminPanel.class));
//                    finish();
                }

                Boolean isBlocked = documentSnapshot.getBoolean("isblocked");
                if(isBlocked!=null && isBlocked)logggingOut();
                String userName =documentSnapshot.getString("fullname");

                Menu menu = bottomNavigationView.getMenu();
                MenuItem addItem = menu.findItem(R.id.nav_add);

                ((MyApplication) this.getApplication()).setUsertype(userType);
                ((MyApplication) this.getApplication()).setName(userName);


                if ("Buyer".equals(userType) || "Admin".equals(userType)) {
                    addItem.setVisible(false);
                } else if ("Seller".equals(userType)) {
                    addItem.setVisible(true);
                }
            }
            bottomNavigationView.setVisibility(View.VISIBLE);

        }).addOnFailureListener(e -> {
            logggingOut();
        });
    }
    private void logggingOut(){
        Toast.makeText(this, "You have been blocked.\n Contact Support", Toast.LENGTH_SHORT).show();
        auth.signOut();
        Intent intent = new Intent(this, LoginSignup.class);
        startActivity(intent);
        finish();
    }
}