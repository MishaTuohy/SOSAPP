package com.michael.helper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button loginBtn;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailAddressInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String emailAddress = email.getText().toString();
        String pass = password.getText().toString();

        if (!emailAddress.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
            if (!pass.isEmpty()){
                firebaseAuth.signInWithEmailAndPassword(emailAddress , pass)
                        .addOnSuccessListener(authResult -> {
                            Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                            intent.putExtra("email", emailAddress);
                            startActivity(intent);
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Login Failed !!", Toast.LENGTH_SHORT).show());
            }else{
                password.setError("Empty Fields Are not Allowed");
            }
        }else if(emailAddress.isEmpty()){
            email.setError("Empty Fields Are not Allowed");
        }else{
            email.setError("Pleas Enter Correct Email");
        }
    }
}