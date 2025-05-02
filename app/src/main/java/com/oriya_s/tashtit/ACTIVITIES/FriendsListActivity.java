package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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

        friends = new ArrayList<>();
        adapter = new FriendsListAdapter(this, friends); // ✅ Fixed constructor
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(adapter);

        btnAddFriend.setOnClickListener(v -> showSearchDialog());

        loadFriends();
    }

    private void showSearchDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter username");

        new AlertDialog.Builder(this)
                .setTitle("Add Friend")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> searchUserByUsername(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void searchUserByUsername(String username) {
        db.collection("UserProfiles")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String id = query.getDocuments().get(0).getId();
                        String name = query.getDocuments().get(0).getString("username");
                        String avatar = query.getDocuments().get(0).getString("profileImageUrl");

                        Friend friend = new Friend(id, name, avatar);
                        addFriend(friend);
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error searching user", Toast.LENGTH_SHORT).show());
    }

    private void addFriend(Friend friend) {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .document(friend.getFriendID()) // ✅ Fixed here
                .set(friend)
                .addOnSuccessListener(unused -> {
                    friends.add(friend);
                    adapter.notifyItemInserted(friends.size() - 1);
                    Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriends() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
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