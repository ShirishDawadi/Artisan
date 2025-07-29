package com.example.artisan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private EditText mFullname, mEmail, mPassword, mRepassword, mPhone;
    private Button mSignup;
    private RadioGroup userTypeRadioGroup;
    private TextView mPasswordError, mEmailError, mPhoneError, mUsertypeError;
    private TextView mNameCounter, mPasswordCounter, mRepasswordCounter, mPhoneCounter;
    private ProgressBar progressBar;
    private TextInputLayout passwordInputLayout, repasswordInputLayout;
    private boolean passwordVisible = false, repasswordVisible = false;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userId;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }

        setupPasswordVisibility();

        setupTextWatchers();

        userTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBuyer) {
                userType = "Buyer";
            } else if (checkedId == R.id.radioSeller) {
                userType = "Seller";
            }
        });

        mSignup.setOnClickListener(v -> registerUser());
    }

    private void initializeViews() {
        mFullname = findViewById(R.id.fullname);
        mEmail = findViewById(R.id.emailsignup);
        mPassword = findViewById(R.id.passwordsignup);
        mRepassword = findViewById(R.id.repasswordsignup);
        mPhone = findViewById(R.id.phone);
        mSignup = findViewById(R.id.signupbutton);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        mEmailError = findViewById(R.id.emailError);
        mPasswordError = findViewById(R.id.passwordError);
        mPhoneError = findViewById(R.id.phoneError);
        mUsertypeError = findViewById(R.id.usertypeError);
        mNameCounter = findViewById(R.id.nameCounter);
        mPasswordCounter = findViewById(R.id.passCounter);
        mRepasswordCounter = findViewById(R.id.rePassCounter);
        mPhoneCounter = findViewById(R.id.phoneCounter);
        progressBar = findViewById(R.id.progressBar);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        repasswordInputLayout = findViewById(R.id.repasswordInputLayout);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void setupPasswordVisibility() {
        passwordInputLayout.setEndIconOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordInputLayout.setEndIconDrawable(R.drawable.visible_password);
            } else {
                mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordInputLayout.setEndIconDrawable(R.drawable.invisible_password);
            }
            mPassword.setSelection(mPassword.getText().length());
        });

        repasswordInputLayout.setEndIconOnClickListener(v -> {
            repasswordVisible = !repasswordVisible;
            if (repasswordVisible) {
                mRepassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                repasswordInputLayout.setEndIconDrawable(R.drawable.visible_password);
            } else {
                mRepassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                repasswordInputLayout.setEndIconDrawable(R.drawable.invisible_password);
            }
            mRepassword.setSelection(mRepassword.getText().length());
        });
    }

    private void setupTextWatchers() {
        mFullname.addTextChangedListener(new CustomTextWatcher(mFullname, mNameCounter, 20));
        mPassword.addTextChangedListener(new CustomTextWatcher(mPassword, mPasswordCounter, 12));
        mRepassword.addTextChangedListener(new CustomTextWatcher(mRepassword, mRepasswordCounter, 12));
        mPhone.addTextChangedListener(new CustomTextWatcher(mPhone, mPhoneCounter, 10));
    }

    private void registerUser() {
        String fullname = mFullname.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String repassword = mRepassword.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        mEmailError.setVisibility(View.GONE);
        mPasswordError.setVisibility(View.GONE);
        mUsertypeError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(fullname)) {
            mFullname.setHint("Enter name");
            mFullname.setHintTextColor(Color.RED);
            mFullname.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setHint("Enter email");
            mEmail.setHintTextColor(Color.RED);
            mEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailError.setText("Invalid email address");
            mEmailError.setVisibility(View.VISIBLE);
            mEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setHint("Enter password");
            mPassword.setHintTextColor(Color.RED);
            mPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mPasswordError.setText("Password must be >= 6 characters");
            mPasswordError.setVisibility(View.VISIBLE);
            mPassword.requestFocus();
            return;
        }

        if (!password.equals(repassword)) {
            mPasswordError.setText("Passwords do not match");
            mPasswordError.setVisibility(View.VISIBLE);
            mRepassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            mPhone.setHint("Enter phone number");
            mPhone.setHintTextColor(Color.RED);
            mPhone.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            mPhoneError.setText("Phone should be 10 digits long");
            mPhoneError.setVisibility(View.VISIBLE);
            mPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userType)) {
            mUsertypeError.setText("Select a user type");
            mUsertypeError.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        userId = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
                        saveUserData(fullname, email, phone);
                        startActivity(new Intent(getApplicationContext(), Home.class));
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveUserData(String fullname, String email, String phone) {
        DocumentReference documentReference = fStore.collection("users").document(userId);
        Map<String, Object> user = new HashMap<>();
        user.put("fullname", fullname);
        user.put("email", email);
        user.put("phone", phone);
        user.put("userType", userType);
        user.put("bio", "");

        if (userType.equals("Seller")) {
            user.put("rating", 0.0);
            user.put("totalLikes", "0");
        }

        documentReference.set(user).addOnSuccessListener(unused ->
                Toast.makeText(SignUp.this, "Account Created", Toast.LENGTH_SHORT).show());
    }

    public void backHome(View view) {
        Intent intent = new Intent(SignUp.this, LoginSignup.class);
        startActivity(intent);
    }
}
