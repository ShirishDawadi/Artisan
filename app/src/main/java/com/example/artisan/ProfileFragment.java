package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    TextView name;
    LinearLayout icon;
    ImageView profile;
    TextView description,rating,likes;
    Button editprofile,dashboard;
    BottomNavigationView productNavbar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView profileBottomNav = view.findViewById(R.id.productCartNavigation);

        if (profileBottomNav != null) {
            ViewCompat.setOnApplyWindowInsetsListener(profileBottomNav, (v, insets) -> {
                v.setPadding(0, 0, 0, 0);
                return insets;
            });
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        icon=view.findViewById(R.id.icons);
        profile=view.findViewById(R.id.homeSellerProfilePicture);
        description=view.findViewById(R.id.description);
        rating=view.findViewById(R.id.rating);
        likes=view.findViewById(R.id.likes);

        dashboard=view.findViewById(R.id.dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        productNavbar=view.findViewById(R.id.productCartNavigation);

        Menu menu=productNavbar.getMenu();
        MenuItem productitem=menu.findItem(R.id.nav_product);
        MenuItem cartItem= menu.findItem(R.id.nav_wish_list);

        MyApplication app = (MyApplication) requireActivity().getApplication();
        String userType=app.getUsertype();
        if(userType.equals("Seller")) {
            dashboard.setVisibility(View.VISIBLE);
            icon.setVisibility(View.VISIBLE);
            cartItem.setVisible(false);
            loadChildFragment(new ProductFragment());
       }else{
            productitem.setVisible(false);
            productNavbar.setSelectedItemId(R.id.nav_wish_list);
            loadChildFragment(new CartFragment());
        }
        productCartNavbar();
        loadUserData();

        name=view.findViewById(R.id.profilename);
        String userName=app.getName();
        name.setText(userName);

        editprofile=view.findViewById(R.id.editProfile);
        editprofile.setOnClickListener(v->editProfileActivity());

        ImageView opt=view.findViewById(R.id.threeLine);
        LinearLayout option=view.findViewById(R.id.options);
        opt.setOnClickListener(v->showOptions(option));

        TextView logout2=view.findViewById(R.id.logout);
        logout2.setOnClickListener(v -> logout());

        dashboard.setOnClickListener(v->{
            Intent i= new Intent(getContext(),SellerDashboard.class);
            startActivity(i);
        });

        return view;
    }
    private void productCartNavbar() {
        productNavbar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_wish_list && item.isVisible()) {
                loadChildFragment(new CartFragment());
                return true;
            } else if (itemId == R.id.nav_order && item.isVisible()) {
                loadChildFragment(new OrderedFragment());
                return true;
            } else if(itemId == R.id.nav_product && item.isVisible()){
                loadChildFragment(new ProductFragment());
                return true;
            }
            return false;
        });
    }
    private void logout() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .update("fcmToken", null)
                .addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), LoginSignup.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed logout try again", Toast.LENGTH_SHORT).show();
                });
    }

    public void showOptions(LinearLayout option){
        if(option.getVisibility()==View.GONE){
            option.setVisibility(View.VISIBLE);
        }else{
            option.setVisibility(View.GONE);
        }
    }
    public void editProfileActivity(){
        Intent intent = new Intent(getActivity(), EditProfile.class);
        startActivity(intent);
    }
    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                description.setText(documentSnapshot.getString("bio"));
                rating.setText(String.valueOf(documentSnapshot.getDouble("rating")));
                likes.setText(String.valueOf(documentSnapshot.getLong("totalLikes")));
                String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Glide.with(this).load(profilePictureUrl).into(profile);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error loading data", Toast.LENGTH_SHORT).show();
        });
    }
    private void loadChildFragment(Fragment fragment){
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        transaction.replace(R.id.cartFrameLayout, fragment);
        transaction.commit();
    }
}