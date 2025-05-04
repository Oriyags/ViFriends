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

    private RecyclerView recyclerView;
    private FriendsListAdapter adapter;
    private List<Friend> chatPreviews;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recycler_chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatPreviews = new ArrayList<>();
        adapter = new FriendsListAdapter(this, chatPreviews, FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter.setOnFriendClickListener((view, friend) -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("userId", friend.getFriendID());
            startActivity(intent);
        });

        loadChatList();
    }

    private void loadChatList() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .get()
                .addOnSuccessListener(query -> {
                    chatPreviews.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Map<String, Object> data = doc.getData();
                        String withUserId = (String) data.get("withUser");
                        db.collection("users")
                                .document(withUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    DocumentSnapshot profileDoc = userDoc;
                                    String name = profileDoc.get("profile.username", String.class);
                                    String avatar = profileDoc.get("profile.profileImageUrl", String.class);
                                    Friend f = new Friend(withUserId, name, avatar, currentUser.getUid(), "chat");
                                    chatPreviews.add(f);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show());
    }
}