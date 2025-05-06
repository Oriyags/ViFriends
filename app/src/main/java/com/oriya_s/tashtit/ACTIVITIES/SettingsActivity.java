package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

public class SettingsActivity extends AppCompatActivity {

    private Button btnToggleTheme, btnAbout, btnDeleteAccount, btnInvite;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToggleTheme = findViewById(R.id.btn_toggle_theme);
        btnAbout = findViewById(R.id.btn_about);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);
        btnInvite = findViewById(R.id.btn_invite);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();

        btnToggleTheme.setOnClickListener(v -> toggleTheme());
        btnAbout.setOnClickListener(v -> showAboutDialog());
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        btnInvite.setOnClickListener(v -> inviteFriend());
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About ViFriends")
                .setMessage("ViFriends is your go-to app for staying connected with friends.\n\nVersion 1.0\nDeveloped by Oriya S.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        // Delete profile picture
        StorageReference profileRef = storage.getReference("profile_images/" + uid + ".jpg");
        profileRef.delete(); // Optional: ignore failure silently

        // Delete subcollections
        deleteSubcollection("users", uid, "chats");
        deleteSubcollection("users", uid, "friends");
        deleteSubcollection("users", uid, "events");

        // Delete user document
        db.collection("users").document(uid).delete()
                .addOnSuccessListener(aVoid -> {
                    currentUser.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LogInActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                });
    }

    private void deleteSubcollection(String parentCollection, String docId, String subcollection) {
        CollectionReference subRef = db.collection(parentCollection).document(docId).collection(subcollection);
        subRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    doc.getReference().delete();
                }
            }
        });
    }

    private void inviteFriend() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Join ViFriends!");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out ViFriends - the app that lets us stay in touch and share moments. Download it now!");
        startActivity(Intent.createChooser(intent, "Invite via"));
    }
}