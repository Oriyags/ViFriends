package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.oriya_s.tashtit.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends BaseActivity {

    // Input fields and buttons
    private EditText etUserName, etPassword, etConfirmPassword, etEmail, etPhoneNumber, etLocation, etDOB;
    private Button   btnCreateAccount, btnCancel;

    // Firebase authentication and database
    private FirebaseAuth      auth;
    private FirebaseFirestore db;

    // Date of birth stored as milliseconds
    private long dobInMillis = 0;

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

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        initializeViews();
    }

    // Set up references to UI elements
    @Override
    protected void initializeViews() {
        etUserName        = findViewById(R.id.etUserName);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail           = findViewById(R.id.etEmail);
        etPhoneNumber     = findViewById(R.id.etPhoneNumber);
        etLocation        = findViewById(R.id.etLocation);
        etDOB             = findViewById(R.id.etDOB);
        btnCreateAccount  = findViewById(R.id.btnCreateAccount);
        btnCancel         = findViewById(R.id.btnCancel);

        setListeners();
    }

    // Set up click listeners
    @Override
    protected void setListeners() {
        // Show a date picker when DOB field is clicked
        etDOB.setOnClickListener(v -> showDatePicker());

        // Handle account creation button
        btnCreateAccount.setOnClickListener(v -> {
            if (!validate()) return; // Check if all inputs are valid

            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserProfile(firebaseUser.getUid());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Cancel button returns to previous screen
        btnCancel.setOnClickListener(v -> finish());
    }

    // Display date picker dialog and store selected date
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            dobInMillis = calendar.getTimeInMillis(); // Save selected date
            etDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Save user profile info to Firestore
    private void saveUserProfile(String uid) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", etUserName.getText().toString().trim());
        userProfile.put("email", etEmail.getText().toString().trim());
        userProfile.put("phoneNumber", etPhoneNumber.getText().toString().trim());
        userProfile.put("locationID", etLocation.getText().toString().trim());
        userProfile.put("dob", dobInMillis);

        // Wrap profile under "profile" key
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("profile", userProfile);

        // Save to Firestore under "users" collection
        db.collection("users")
                .document(uid)
                .set(wrapper)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateAccountActivity.this, LogInActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Not used in this activity but required by BaseActivity
    @Override
    protected void setViewModel() {}

    // Validate user inputs before registration
    private boolean validate() {
        if (etUserName.getText().toString().trim().isEmpty()) {
            etUserName.setError("Username is required");
            return false;
        }

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return false;
        }

        String password        = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (dobInMillis == 0) {
            etDOB.setError("Please select a valid date of birth");
            return false;
        }
        return true;
    }
}