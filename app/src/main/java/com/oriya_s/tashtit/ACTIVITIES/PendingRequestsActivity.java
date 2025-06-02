package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.ADPTERS.PendingRequestsAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class PendingRequestsActivity extends AppCompatActivity {

    // RecyclerView for displaying pending friend requests
    private RecyclerView           pendingRequestsRecyclerView;
    // Adapter to bind friend request data to the RecyclerView
    private PendingRequestsAdapter adapter;
    // List to hold pending friend request objects
    private ArrayList<Friend>      pendingRequests;

    // Firebase instances for Firestore and authentication
    private FirebaseFirestore db;
    private FirebaseUser      currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        // Initialize Firebase Firestore and current authenticated user
        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize the RecyclerView and its adapter
        pendingRequestsRecyclerView = findViewById(R.id.recycler_pending_requests);
        pendingRequests             = new ArrayList<>();
        adapter                     = new PendingRequestsAdapter(this, pendingRequests, db, currentUser);

        // Set layout manager and adapter to the RecyclerView
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRequestsRecyclerView.setAdapter(adapter);

        // Load pending friend requests from Firebase
        loadPendingRequests();
    }

    // Method to load all pending friend requests of the current user from Firestore
    private void loadPendingRequests() {
        // Exit if user is not logged in
        if (currentUser == null) return;

        // Query the "friends" subcollection for documents with status "pending"
        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Clear current list and populate it with new data
                    pendingRequests.clear();
                    snapshot.forEach(doc -> pendingRequests.add(doc.toObject(Friend.class)));

                    // Notify adapter to refresh the UI
                    adapter.notifyDataSetChanged();
                });
    }
}