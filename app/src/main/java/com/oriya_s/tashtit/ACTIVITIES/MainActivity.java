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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
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

        // Get and store Firebase Cloud Messaging (FCM) token if user is logged in
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            // Save the token in the Firestore document for this user
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.getUid())
                                    .update("fcmToken", token);
                        }
                    }
                });

        // Show splash screen for 2 seconds, then redirect to Home or Login screen
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // If user is already logged in, go to the home screen
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                // If user is not logged in, go to the login screen
                intent = new Intent(MainActivity.this, LogInActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000);
    }

    // These override methods are required by BaseActivity but are not used in MainActivity

    @Override
    protected void initializeViews() {
        // No views to initialize in this splash screen
    }

    @Override
    protected void setListeners() {
        // No listeners needed in splash screen
    }

    @Override
    protected void setViewModel() {
        // No ViewModel setup needed here
    }
}