package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.ADPTERS.FriendsListAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity {

    // UI components
    private RecyclerView friendsRecyclerView;
    private ImageButton  btnAddFriend;
    private Button       btnPendingRequests;

    // Adapter and data list
    private FriendsListAdapter adapter;
    private ArrayList<Friend>  friends;

    // Firebase services
    private FirebaseFirestore db;
    private FirebaseUser      currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        // Firebase instances
        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI components
        friendsRecyclerView = findViewById(R.id.recycler_friends);
        btnAddFriend        = findViewById(R.id.btn_add_friend);
        btnPendingRequests  = findViewById(R.id.btn_pending_requests);

        // Setup RecyclerView with adapter
        friends = new ArrayList<>();
        adapter = new FriendsListAdapter(this, friends, db, currentUser);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(adapter);

        // Handle click events on friend list items
        adapter.setOnFriendClickListener(this::showFriendOptions);

        // Update result when a friend is removed
        adapter.setOnFriendRemovedListener(() -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedFriendCount", friends.size());
            setResult(RESULT_OK, resultIntent);
        });

        // Search for a new friend
        btnAddFriend.setOnClickListener(v -> showSearchDialog());

        // View pending friend requests
        btnPendingRequests.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsListActivity.this, PendingRequestsActivity.class);
            startActivity(intent);
        });

        // Load accepted friends from Firestore
        loadFriends();
    }

    // Show an input dialog to search for a friend by username
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

    // Search Firestore for a user by their username
    private void searchUserByUsername(String username) {
        if (currentUser == null || username.isEmpty()) return;

        db.collection("users")
                .whereEqualTo("profile.username", username)
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
                                        // Fetch current user profile info
                                        db.collection("users")
                                                .document(currentUser.getUid())
                                                .get()
                                                .addOnSuccessListener(currentDoc -> {
                                                    String senderName = currentDoc.get("profile.username", String.class);
                                                    String senderAvatar = currentDoc.get("profile.profileImageUrl", String.class);

                                                    // Send request to the receiver
                                                    Friend sentRequest = new Friend(
                                                            currentUser.getUid(),
                                                            senderName,
                                                            senderAvatar,
                                                            receiverId,
                                                            "pending"
                                                    );

                                                    db.collection("users")
                                                            .document(receiverId)
                                                            .collection("friends")
                                                            .document(currentUser.getUid())
                                                            .set(sentRequest)
                                                            .addOnSuccessListener(unused ->
                                                                    Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show()
                                                            );

                                                    // Save placeholder for receiver in sender's list
                                                    String receiverAvatar = query.getDocuments().get(0).get("profile.profileImageUrl", String.class);

                                                    Friend placeholder = new Friend(
                                                            receiverId,
                                                            username,
                                                            receiverAvatar,
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

    // Load accepted friends from Firestore and update the RecyclerView
    private void loadFriends() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    friends.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Friend f = doc.toObject(Friend.class);
                        String friendId = f.getFriendID();

                        db.collection("users").document(friendId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                                        String friendName = profile != null ? (String) profile.get("username") : "Unknown";
                                        String avatarUrl = profile != null ? (String) profile.get("profileImageUrl") : null;

                                        f.setName(friendName);
                                        f.setAvatarUrl(avatarUrl);

                                        boolean exists = false;
                                        for (Friend existing : friends) {
                                            if (existing.getFriendID().equals(friendId)) {
                                                exists = true;
                                                break;
                                            }
                                        }

                                        if (!exists) {
                                            friends.add(f);
                                            runOnUiThread(() -> adapter.notifyDataSetChanged());
                                        }
                                    } else {
                                        // Friend was deleted, remove from current user's list
                                        db.collection("users")
                                                .document(currentUser.getUid())
                                                .collection("friends")
                                                .document(friendId)
                                                .delete();
                                    }
                                });
                    }
                });
    }

    // Show popup menu with options when clicking a friend
    private void showFriendOptions(View anchorView, Friend friend) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add("View Profile");
        popup.getMenu().add("Make Video Call");
        popup.getMenu().add("Go to Chat");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            Intent intent = null;
            switch (title) {
                case "View Profile":
                    intent = new Intent(this, UserProfileActivity.class);
                    break;
                case "Make Video Call":
                    intent = new Intent(this, VideoActivity.class);
                    break;
                case "Go to Chat":
                    intent = new Intent(this, ChatActivity.class);
                    break;
            }
            if (intent != null) {
                intent.putExtra("userId", friend.getFriendID());
                startActivity(intent);
            }
            return true;
        });

        popup.show();
    }
}