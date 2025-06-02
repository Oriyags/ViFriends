package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.tashtit.ADPTERS.FriendSelectAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FriendSelectorActivity extends AppCompatActivity {

    // UI components
    private RecyclerView recyclerView;
    private Button       btnDone;

    // Adapter for displaying selectable friends
    private FriendSelectAdapter adapter;

    // Data structures to manage friend info
    private final List<String>    friendNames = new ArrayList<>();
    private final List<String>    friendIds   = new ArrayList<>();
    private final HashSet<String> selectedIds = new HashSet<>();

    // Firebase references
    private FirebaseFirestore db;
    private FirebaseUser      currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_selector);

        recyclerView = findViewById(R.id.recycler_friend_selector);
        btnDone      = findViewById(R.id.btn_select_done);

        // Initialize Firebase
        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Set up adapter and recycler view
        adapter = new FriendSelectAdapter(friendNames, selectedIds); // Provide adapter with names and selection set
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Vertical list
        recyclerView.setAdapter(adapter); // Bind adapter to RecyclerView

        // Set button click listener to return selected friends when done
        btnDone.setOnClickListener(v -> returnSelectedFriends());

        // Load accepted friends from Firestore
        loadAcceptedFriends();
    }

    /**
     * Loads friends from Firestore that have an 'accepted' friendship status.
     * Then fetches their usernames and displays them in the list.
     */
    private void loadAcceptedFriends() {
        if (currentUser == null) return;

        // First get list of accepted friend document IDs
        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted") // Filter by accepted status
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ids.add(doc.getId()); // Collect friend UIDs
                    }

                    if (ids.isEmpty()) {
                        Toast.makeText(this, "No friends to show", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Fetch friend profiles by IDs
                    db.collection("users")
                            .whereIn(FieldPath.documentId(), ids)
                            .get()
                            .addOnSuccessListener(usersSnapshot -> {
                                // Clear old data
                                friendNames.clear();
                                friendIds.clear();
                                selectedIds.clear();

                                // Extract friend names from "profile.username"
                                for (DocumentSnapshot doc : usersSnapshot.getDocuments()) {
                                    String id = doc.getId();
                                    Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
                                    String name = profile != null ? (String) profile.get("username") : "Unknown";

                                    friendIds.add(id);
                                    friendNames.add(name != null ? name : "Unnamed");
                                }

                                // Update adapter with latest friend list
                                adapter.setFriendIds(friendIds);
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    //Prepares and returns the selected friend IDs back to the calling activity.
    private void returnSelectedFriends() {
        Intent result = new Intent();
        result.putStringArrayListExtra("selectedFriendUIDs", new ArrayList<>(selectedIds)); // Put selected IDs
        setResult(RESULT_OK, result); // Set result code and intent
        finish();
    }
}