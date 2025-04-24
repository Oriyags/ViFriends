package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.oriya_s.model.User;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.oriya_s.tashtit.R;
import com.oriya_s.viewmodel.UsersViewmodel;

public class LogInActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private SwitchCompat swSaveUser;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;

    private UsersViewmodel loginViewModel;

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

        initializeViews();
        setViewModel();
    }

    protected void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        swSaveUser = findViewById(R.id.swSaveUser);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        setListeners();
    }

    protected void setListeners() {
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LogInActivity.this, CreateAccountActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                showStyledToast("Please enter Email and Password");
                return;
            }

            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(email, password);
        });
    }

    protected void setViewModel() {
        loginViewModel = new ViewModelProvider(this).get(UsersViewmodel.class);

        loginViewModel.getLiveDataEntity().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Toast.makeText(LogInActivity.this, (user != null) ? "Login success" : "Login failure", Toast.LENGTH_LONG).show();
                startActivity(new Intent(LogInActivity.this, HomeActivity.class));
            }
        });

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
}