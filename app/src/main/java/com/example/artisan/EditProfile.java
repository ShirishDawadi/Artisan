package com.example.artisan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText fullNameEditText;
    private EditText phoneNumberEditText;
    private EditText bioEditText;
    private TextView fullNameCounter;
    private TextView phoneNumberCounter;
    private TextView bioCounter;
    private TextView phoneNumberError;
    private Button updateProfileButton;
    private ImageView closeButton;
    private ImageView addProfile;
    private Uri imageUri;
    private ImageView profilePicture;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullNameEditText = findViewById(R.id.editName);
        phoneNumberEditText = findViewById(R.id.editPhone);
        bioEditText = findViewById(R.id.editBio);
        fullNameCounter = findViewById(R.id.fullNameCounter);
        phoneNumberCounter = findViewById(R.id.phoneNumberCounter);
        bioCounter = findViewById(R.id.bioCounter);
        phoneNumberError=findViewById(R.id.phoneNumberError);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        closeButton = findViewById(R.id.closeButton);
        addProfile=findViewById(R.id.addProfile);
        profilePicture=findViewById(R.id.homeSellerProfilePicture);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameEditText.addTextChangedListener(new CustomTextWatcher(fullNameEditText,fullNameCounter,20));
        phoneNumberEditText.addTextChangedListener(new CustomTextWatcher(phoneNumberEditText,phoneNumberCounter,10));
        bioEditText.addTextChangedListener(new CustomTextWatcher(bioEditText,bioCounter,100));

        setupClickListeners();
        loadUserData();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showDialog();
            }
        });
    }
    private void setupClickListeners() {
        updateProfileButton.setOnClickListener(v -> updateProfile());
        addProfile.setOnClickListener(v->UploadPhoto());
        closeButton.setOnClickListener(v -> showDialog());
    }
    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                fullNameEditText.setText(documentSnapshot.getString("fullname"));
                phoneNumberEditText.setText(documentSnapshot.getString("phone"));
                bioEditText.setText(documentSnapshot.getString("bio"));
                String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Glide.with(this).load(profilePictureUrl).into(profilePicture);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        if (imageUri != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String oldImageUrl = documentSnapshot.getString("profilePictureUrl");
                    uploadProfilePictureToCloudinary(imageUri, userRef, fullName, phoneNumber, bio, oldImageUrl);
                }
            });
        } else {
            saveDataToFirebase(userRef, fullName, phoneNumber, bio, null);
        }
    }

    private void saveDataToFirebase(DocumentReference userRef, String fullName, String phoneNumber, String bio,String imageUrl) {
        if(TextUtils.isEmpty(fullName)){
            fullNameEditText.setHint("Enter Name");
            return;
        }
        if(TextUtils.isEmpty(phoneNumber)){
            phoneNumberEditText.setHint("Enter Phone Number");
            return;
        }
        if(phoneNumber.length()!=10){
            phoneNumberError.setText("Should be 10 character");
            phoneNumberError.setVisibility(View.VISIBLE);
            return;
        }else{
            phoneNumberError.setVisibility(View.GONE);
        }
        if(imageUrl != null){
            if(imageUrl.startsWith("http://")){
                imageUrl=imageUrl.replace("http://", "https://");
            }
            userRef.update(
                    "fullname", fullName,
                    "phone", phoneNumber,
                    "bio", bio,
                    "profilePictureUrl", imageUrl
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
            });
        }else{
            userRef.update(
                    "fullname", fullName,
                    "phone", phoneNumber,
                    "bio", bio
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
            });
        }
        ((MyApplication) this.getApplication()).setName(fullName);
    }

    private void UploadPhoto(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri);
        }
    }
    private void uploadProfilePictureToCloudinary(Uri imageUri, DocumentReference userRef, String fullName, String phoneNumber, String bio, String oldImageUrl) {
        MediaManager.get().upload(imageUri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                // Upload started
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                // Upload progress
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String newImageUrl = (String) resultData.get("url");
                saveDataToFirebase(userRef, fullName, phoneNumber, bio, newImageUrl);
            }
            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(EditProfile.this, "Error uploading image to Cloudinary: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                saveDataToFirebase(userRef, fullName, phoneNumber, bio, null);
            }
            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                // Reschedule upload
            }
        }).dispatch();
    }
    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder
                (EditProfile.this,R.style.MyAlertDialog);
        builder.setMessage("Do you want to discard changes?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.red));
        }
    }
}