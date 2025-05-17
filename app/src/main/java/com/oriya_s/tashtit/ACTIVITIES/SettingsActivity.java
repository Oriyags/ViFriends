package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

import java.util.List;

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

        // ✅ Remove user from all acceptedUserIds in events
        removeUserFromAllEvents(uid);

        // ✅ Remove profile image
        StorageReference profileRef = storage.getReference("profile_images/" + uid + ".jpg");
        profileRef.delete(); // Ignore errors if not found

        // ✅ Remove from other users' friends lists
        db.collectionGroup("friends")
                .whereEqualTo("friendID", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });

        // ✅ Delete from other users' chat metadata + delete chats and messages
        db.collection("Chats")
                .whereArrayContains("participants", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot chatDoc : snapshot) {
                        String chatId = chatDoc.getId();
                        List<?> participants = (List<?>) chatDoc.get("participants");

                        db.collection("Chats").document(chatId)
                                .collection("messages")
                                .get()
                                .addOnSuccessListener(messages -> {
                                    for (QueryDocumentSnapshot msg : messages) {
                                        msg.getReference().delete();
                                    }
                                });

                        db.collection("Chats").document(chatId).delete();

                        for (Object userIdObj : participants) {
                            String participantId = userIdObj.toString();
                            if (!participantId.equals(uid)) {
                                db.collection("users")
                                        .document(participantId)
                                        .collection("chats")
                                        .document(uid)
                                        .delete();
                            }
                        }
                    }
                });

        // ✅ Delete user’s subcollections
        deleteSubcollection("users", uid, "chats");
        deleteSubcollection("users", uid, "friends");
        deleteSubcollection("users", uid, "events");

        // ✅ Delete user document and Firebase Auth account
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
        subRef.get().addOnSuccessListener(snapshot -> {
            for (QueryDocumentSnapshot doc : snapshot) {
                doc.getReference().delete();
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

    // ✅ NEW METHOD: Remove user from acceptedUserIds in all events
    private void removeUserFromAllEvents(String userIdToRemove) {
        db.collectionGroup("events")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        List<String> accepted = (List<String>) doc.get("acceptedUserIds");
                        if (accepted != null && accepted.contains(userIdToRemove)) {
                            accepted.remove(userIdToRemove);
                            doc.getReference().update("acceptedUserIds", accepted);
                        }
                    }
                });
    }
}