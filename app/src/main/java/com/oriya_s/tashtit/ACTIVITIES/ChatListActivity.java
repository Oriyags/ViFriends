package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oriya_s.tashtit.ADPTERS.FriendsListAdapter;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    // RecyclerView & Adapter for showing list of chat partners
    private RecyclerView       recyclerView;
    private FriendsListAdapter adapter;
    private List<Friend>       chatPreviews;

    // Firebase references
    private FirebaseUser      currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Set up RecyclerView and layout
        recyclerView = findViewById(R.id.recycler_chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize chat preview list and adapter
        chatPreviews = new ArrayList<>();
        adapter = new FriendsListAdapter(
                this,
                chatPreviews,
                FirebaseFirestore.getInstance(),
                FirebaseAuth.getInstance().getCurrentUser()
        );
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // When a friend in the list is clicked → open chat with them
        adapter.setOnFriendClickListener((view, friend) -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("userId", friend.getFriendID());
            startActivity(intent);
        });

        // Load chat list from Firestore
        loadChatList();
    }

    // Loads all chat partners of the current user
    private void loadChatList() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .get()
                .addOnSuccessListener(query -> {
                    chatPreviews.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        // Each chat document contains a "withUser" field (the ID of the other participant)
                        Map<String, Object> data = doc.getData();
                        String withUserId = (String) data.get("withUser");

                        // Fetch profile details of the other user
                        db.collection("users")
                                .document(withUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    DocumentSnapshot profileDoc = userDoc;
                                    // Get the username and profile image from the embedded "profile" map
                                    String name = profileDoc.get("profile.username", String.class);
                                    String avatar = profileDoc.get("profile.profileImageUrl", String.class);

                                    // Create a Friend object for the adapter
                                    Friend f = new Friend(
                                            withUserId,
                                            name,
                                            avatar,
                                            currentUser.getUid(),
                                            "chat"
                                    );

                                    // Add to the list and notify the adapter
                                    chatPreviews.add(f);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Show error if loading chats failed
                    Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show();
                });
    }
}