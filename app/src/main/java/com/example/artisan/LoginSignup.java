package com.example.artisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginSignup extends AppCompatActivity {

    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fAuth= FirebaseAuth.getInstance();
        String userId= fAuth.getUid();

        if(fAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }
    }
    public void signupPage(View v){

        Intent intent= new Intent(LoginSignup.this,SignUp.class);
        startActivity(intent);
    }
    public void loginPage(View v){

        Intent intent= new Intent(LoginSignup.this,Login.class);
        startActivity(intent);
    }
}