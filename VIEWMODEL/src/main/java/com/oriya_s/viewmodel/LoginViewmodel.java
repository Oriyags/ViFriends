package com.oriya_s.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginViewmodel extends ViewModel {

    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();

    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    // Real Login - Connected to Firebase Authentication
    public void login(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Load User Data from Firestore:
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // You can get user data here:
                                        String username = documentSnapshot.getString("username");
                                        String phone = documentSnapshot.getString("phoneNumber");
                                        String location = documentSnapshot.getString("locationID");

                                        // Save this info for later (SharedPreferences or ViewModel)
                                    }

                                    loginResult.setValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    loginResult.setValue(false);
                                });
                    } else {
                        loginResult.setValue(false);
                    }
                });
    }
}