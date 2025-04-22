package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.oriya_s.helper.inputValidators.EntryValidation;
import com.oriya_s.model.User;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.oriya_s.tashtit.R;
import com.oriya_s.viewmodel.UsersViewmodel;

public class CreateAccountActivity extends BaseActivity implements EntryValidation {

    private EditText etUserName, etPassword, etEmail, etPhoneNumber, etLocation, etDOB;
    private Button btnCreateAccount;
    private UsersViewmodel usersViewmodel;

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

        initializeViews();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etLocation = findViewById(R.id.etLocation);
        etDOB = findViewById(R.id.etDOB);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        setListeners();
    }

    @Override
    protected void setListeners() {
        btnCreateAccount.setOnClickListener(view -> {
            if (validate()) {
                User userProfile = new User();
                userProfile.setUsername(etUserName.getText().toString());
                userProfile.setPassword(etPassword.getText().toString());
                userProfile.setEmail(etEmail.getText().toString());
                userProfile.setPhoneNumber(etPhoneNumber.getText().toString());
                userProfile.setLocationID(etLocation.getText().toString());

                // Avoid crash if DOB is empty
                if (!etDOB.getText().toString().isEmpty()) {
                    userProfile.setDOB(Long.parseLong(etDOB.getText().toString()));
                } else {
                    userProfile.setDOB(0);
                }

                usersViewmodel.save(userProfile);
            }
        });
    }

    protected void setViewModel() {
        usersViewmodel = new ViewModelProvider(this).get(UsersViewmodel.class);

        usersViewmodel.getLiveDataSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showToast("User registered successfully!");
                    Intent intent = new Intent(CreateAccountActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showToast("Registration failed: Email may already be in use.");
                }
            }
        });
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setValidation() {
        // Optional
    }

    @Override
    public boolean validate() {
        if (etUserName.getText().toString().isEmpty()) {
            etUserName.setError("Enter username");
            return false;
        }

        if (etEmail.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Enter a valid email");
            return false;
        }

        if (etPassword.getText().toString().length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            return false;
        }

        return true;
    }
}