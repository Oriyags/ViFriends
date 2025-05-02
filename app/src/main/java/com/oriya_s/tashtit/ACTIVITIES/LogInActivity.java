package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.oriya_s.tashtit.R;

public class LogInActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

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

        firebaseAuth = FirebaseAuth.getInstance();
        initializeViews();
    }

    protected void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        setListeners();
    }

    protected void setListeners() {
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(LogInActivity.this, CreateAccountActivity.class))
        );

        btnLogin.setOnClickListener(v -> {
            hideKeyboard();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (!validateInputs(email, password)) return;

            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showStyledToast(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null);
        TextView textView = layout.findViewById(R.id.toastText);
        textView.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Override
    protected void setViewModel() {}
}