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

    private RecyclerView recyclerView;
    private Button btnDone;
    private FriendSelectAdapter adapter;
    private final List<String> friendNames = new ArrayList<>();
    private final List<String> friendIds = new ArrayList<>();
    private final HashSet<String> selectedIds = new HashSet<>();

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_selector);

        recyclerView = findViewById(R.id.recycler_friend_selector);
        btnDone = findViewById(R.id.btn_select_done);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new FriendSelectAdapter(friendNames, selectedIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnDone.setOnClickListener(v -> returnSelectedFriends());

        loadAcceptedFriends();
    }

    private void loadAcceptedFriends() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ids.add(doc.getId());
                    }

                    if (ids.isEmpty()) {
                        Toast.makeText(this, "No friends to show", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("users")
                            .whereIn(FieldPath.documentId(), ids)
                            .get()
                            .addOnSuccessListener(usersSnapshot -> {
                                friendNames.clear();
                                friendIds.clear();
                                selectedIds.clear();

                                for (DocumentSnapshot doc : usersSnapshot.getDocuments()) {
                                    String id = doc.getId();
                                    Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
                                    String name = profile != null ? (String) profile.get("username") : "Unknown";
                                    friendIds.add(id);
                                    friendNames.add(name != null ? name : "Unnamed");
                                }

                                adapter.setFriendIds(friendIds);
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    private void returnSelectedFriends() {
        Intent result = new Intent();
        result.putStringArrayListExtra("selectedFriendUIDs", new ArrayList<>(selectedIds));
        setResult(RESULT_OK, result);
        finish();
    }
}