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

    private RecyclerView pendingRequestsRecyclerView;
    private PendingRequestsAdapter adapter;
    private ArrayList<Friend> pendingRequests;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        pendingRequestsRecyclerView = findViewById(R.id.recycler_pending_requests);
        pendingRequests = new ArrayList<>();
        adapter = new PendingRequestsAdapter(this, pendingRequests, db, currentUser);
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRequestsRecyclerView.setAdapter(adapter);

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingRequests.clear();
                    snapshot.forEach(doc -> pendingRequests.add(doc.toObject(Friend.class)));
                    adapter.notifyDataSetChanged();
                });
    }
}
