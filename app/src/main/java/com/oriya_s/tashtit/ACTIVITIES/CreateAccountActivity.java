package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.tashtit.R;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends BaseActivity {

    private EditText etUserName, etPassword, etConfirmPassword, etEmail, etPhoneNumber, etLocation, etDOB;
    private Button btnCreateAccount, btnCancel;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
    }

    @Override
    protected void initializeViews() {
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etLocation = findViewById(R.id.etLocation);
        etDOB = findViewById(R.id.etDOB);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCancel = findViewById(R.id.btnCancel);

        setListeners();
    }

    @Override
    protected void setListeners() {
        btnCreateAccount.setOnClickListener(v -> {
            if (validate()) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                saveUserProfile(firebaseUser.getUid());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveUserProfile(String uid) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", etUserName.getText().toString());
        userProfile.put("email", etEmail.getText().toString());
        userProfile.put("phoneNumber", etPhoneNumber.getText().toString());
        userProfile.put("locationID", etLocation.getText().toString());

        if (!etDOB.getText().toString().isEmpty()) {
            userProfile.put("dob", Long.parseLong(etDOB.getText().toString()));
        } else {
            userProfile.put("dob", 0);
        }

        db.collection("UserProfiles")
                .document(uid)
                .set(userProfile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateAccountActivity.this, LogInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void setViewModel() {
        // Not needed here
    }

    private boolean validate() {
        if (etUserName.getText().toString().isEmpty()) {
            etUserName.setError("Enter username");
            return false;
        }

        String email = etEmail.getText().toString();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return false;
        }

        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}