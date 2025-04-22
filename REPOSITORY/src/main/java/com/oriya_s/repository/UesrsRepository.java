package com.oriya_s.repository;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.User;
import com.oriya_s.model.UserProfiles;
import com.oriya_s.repository.BASE.BaseRepository;

public class UesrsRepository extends BaseRepository<User, UserProfiles> {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UesrsRepository(Application application) {
        super(User.class, UserProfiles.class, application);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Save user to Firebase Authentication & Firestore
    public void saveUser(@NonNull User user, @NonNull OnUserSaveCallback callback) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> callback.onResult(true))
                                .addOnFailureListener(e -> {
                                    // Firestore failed — delete the user from FirebaseAuth
                                    auth.getCurrentUser().delete();
                                    callback.onResult(false);
                                });
                    } else {
                        callback.onResult(false);  // For example: Email already used
                    }
                });
    }

    // We no longer need Firestore-based email checking before registration
    @Override
    protected com.google.firebase.firestore.Query getQueryForExist(User entity) {
        return null;
    }

    public interface OnUserSaveCallback {
        void onResult(boolean success);
    }
}