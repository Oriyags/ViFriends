package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.oriya_s.tashtit.R;

public class HomeActivity extends AppCompatActivity {

    private ImageButton menuButton, eventsButton, callButton, cameraButton, chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setListeners();
    }

    private void initializeViews() {
        menuButton = findViewById(R.id.menu_button);
        eventsButton = findViewById(R.id.events_button);
        callButton = findViewById(R.id.call_button);
        cameraButton = findViewById(R.id.camera_button);
        chatButton = findViewById(R.id.chat_button);
    }

    private void setListeners() {
        menuButton.setOnClickListener(v -> showToast("Open Settings Menu"));
        eventsButton.setOnClickListener(v -> showToast("Go to Events Activity"));
        callButton.setOnClickListener(v -> showToast("Choose Friend to Call"));
        cameraButton.setOnClickListener(v -> showToast("Choose Friend for Video Call"));
        chatButton.setOnClickListener(v -> showToast("Choose Friend to Chat With"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}