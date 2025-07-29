package com.example.artisan;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextInputLayout passwordInputLayout;
    private TextView emailErr, passwordErr;
    private ProgressBar progressBar;
    private boolean passwordVisible = false;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.loginbutton);
        progressBar = findViewById(R.id.progressBar2);
        emailErr = findViewById(R.id.emailError);
        passwordErr = findViewById(R.id.passwordError);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        passwordInputLayout.setEndIconOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordInputLayout.setEndIconDrawable(R.drawable.visible_password);
            } else {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordInputLayout.setEndIconDrawable(R.drawable.invisible_password);
            }
            password.setTypeface(Typeface.DEFAULT);
            password.setSelection(password.getText().length());
        });

        fAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(v -> {

            emailErr.setVisibility(View.GONE);
            passwordErr.setVisibility(View.GONE);

            String loginemail = email.getText().toString().trim();
            String loginpassword = password.getText().toString();

            if (TextUtils.isEmpty(loginemail)) {
                email.setHint("Enter email");
                email.setHintTextColor(Color.RED);
                email.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginemail).matches()) {
                emailErr.setText("Invalid email address");
                emailErr.setVisibility(View.VISIBLE);
                email.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(loginpassword)) {
                password.setHint("Enter password");
                password.setHintTextColor(Color.RED);
                password.requestFocus();
                return;
            }

            if (loginpassword.length() < 6) {
                passwordErr.setText("Password must be >= 6 characters long");
                passwordErr.setVisibility(View.VISIBLE);
                password.requestFocus();
                return;
            }

            login.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(loginemail, loginpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            login.setEnabled(true);
                            progressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, Home.class));
                                finish();
                            } else {
                                passwordErr.setText("Invalid email or password. Please try again.");
                                passwordErr.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        });
    }

    public void back(View view) {
        Intent intent = new Intent(Login.this, LoginSignup.class);
        startActivity(intent);
        finish();
    }
}
