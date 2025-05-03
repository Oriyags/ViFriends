package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.ADPTERS.FriendsListAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView friendsRecyclerView;
    private ImageButton btnAddFriend;
    private Button btnPendingRequests;
    private FriendsListAdapter adapter;
    private ArrayList<Friend> friends;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        friendsRecyclerView = findViewById(R.id.recycler_friends);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        btnPendingRequests = findViewById(R.id.btn_pending_requests);

        friends = new ArrayList<>();
        adapter = new FriendsListAdapter(this, friends, db, currentUser);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(adapter);

        btnAddFriend.setOnClickListener(v -> showSearchDialog());

        btnPendingRequests.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsListActivity.this, PendingRequestsActivity.class);
            startActivity(intent);
        });

        loadFriends();
    }

    private void showSearchDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter username");

        new AlertDialog.Builder(this)
                .setTitle("Send Friend Request")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> searchUserByUsername(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void searchUserByUsername(String username) {
        if (currentUser == null || username.isEmpty()) return;

        if (username.equals(currentUser.getDisplayName())) {
            Toast.makeText(this, "You can't send a friend request to yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("UserProfiles")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String receiverId = query.getDocuments().get(0).getId();

                        if (receiverId.equals(currentUser.getUid())) {
                            Toast.makeText(this, "You can't send a friend request to yourself", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if request already exists
                        db.collection("users").document(receiverId)
                                .collection("friends").document(currentUser.getUid())
                                .get()
                                .addOnSuccessListener(existing -> {
                                    if (existing.exists()) {
                                        Toast.makeText(this, "Friend request already sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Get current user's profile info
                                        db.collection("UserProfiles")
                                                .document(currentUser.getUid())
                                                .get()
                                                .addOnSuccessListener(currentProfile -> {
                                                    String senderName = currentProfile.getString("username");
                                                    String senderAvatar = currentProfile.getString("profileImageUrl");

                                                    Friend sentRequest = new Friend(
                                                            currentUser.getUid(),
                                                            senderName,
                                                            senderAvatar,
                                                            receiverId,
                                                            "pending"
                                                    );

                                                    // Save request to receiver's friend list
                                                    db.collection("users")
                                                            .document(receiverId)
                                                            .collection("friends")
                                                            .document(currentUser.getUid())
                                                            .set(sentRequest)
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                            });

                                                    // (Optional) Add placeholder to sender's friend list
                                                    Friend placeholder = new Friend(
                                                            receiverId,
                                                            username,
                                                            query.getDocuments().get(0).getString("profileImageUrl"),
                                                            currentUser.getUid(),
                                                            "requested"
                                                    );

                                                    db.collection("users")
                                                            .document(currentUser.getUid())
                                                            .collection("friends")
                                                            .document(receiverId)
                                                            .set(placeholder);
                                                });
                                    }
                                });
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error searching user", Toast.LENGTH_SHORT).show());
    }

    private void loadFriends() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    friends.clear();
                    snapshot.forEach(doc -> {
                        Friend f = doc.toObject(Friend.class);
                        friends.add(f);
                    });
                    adapter.notifyDataSetChanged();
                });
    }
}