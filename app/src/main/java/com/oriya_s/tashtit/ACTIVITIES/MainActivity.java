package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.oriya_s.tashtit.R;
import com.oriya_s.tashtit.ACTIVITIES.BASE.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // User is already logged in
                intent = new Intent(MainActivity.this, LogInActivity.class);
            } else {
                // User not logged in
                intent = new Intent(MainActivity.this, LogInActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000); // 2 seconds splash
    }

    @Override
    protected void initializeViews() {}

    @Override
    protected void setListeners() {}

    @Override
    protected void setViewModel() {}
}