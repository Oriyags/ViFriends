package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.oriya_s.tashtit.R;

public class LogInActivity extends BaseActivity {

    // UI components
    private EditText     etEmail, etPassword;
    private Button       btnLogin, btnRegister;
    private ProgressBar  progressBar;
    private SwitchCompat swSaveUser;

    // Firebase authentication instance
    private FirebaseAuth firebaseAuth;

    // SharedPreferences keys
    private static final String PREFS_NAME   = "loginPrefs";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Connect views and load saved login details if available
        initializeViews();
        loadPreferences();
    }

    // Connects view components to their XML IDs and sets listeners
    protected void initializeViews() {
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        swSaveUser  = findViewById(R.id.swSaveUser);

        setListeners();
    }

    protected void setListeners() {
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(LogInActivity.this, CreateAccountActivity.class))
        );

        // Attempt login
        btnLogin.setOnClickListener(v -> {
            hideKeyboard(); // Hide keyboard when user presses login

            // Retrieve and trim inputs
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Validate input fields
            if (!validateInputs(email, password)) return;

            // Disable login button while processing
            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Sign in using Firebase Authentication
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Save login info if switch is checked
                            if (swSaveUser.isChecked()) {
                                savePreferences(email, password);
                            } else {
                                clearPreferences();
                            }

                            // If login succeeded, redirect to HomeActivity
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                showStyledToast("Login successful");
                                startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                                finish();
                            }
                        } else {
                            showStyledToast("Login failed: " + task.getException().getMessage());
                        }
                    });
        });
    }

    // Validates the entered email and password
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }

        return true;
    }

    // Hides the soft keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // Displays a custom-styled Toast message
    private void showStyledToast(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null);
        TextView textView = layout.findViewById(R.id.toastText);
        textView.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // Saves email, password, and remember state to SharedPreferences
    private void savePreferences(String email, String password) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_REMEMBER, true)
                .apply();
    }

    // Loads saved preferences and autofills fields if remember switch was checked
    private void loadPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean remember = preferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            etEmail.setText(preferences.getString(KEY_EMAIL, ""));
            etPassword.setText(preferences.getString(KEY_PASSWORD, ""));
            swSaveUser.setChecked(true);
        }
    }

    // Clears saved login preferences
    private void clearPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    // Overridden but unused in this activity
    @Override
    protected void setViewModel() {}
}