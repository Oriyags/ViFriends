package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Event;
import com.oriya_s.model.UserProfile;
import com.oriya_s.tashtit.ADPTERS.AttendeesAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendeesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendeesAdapter adapter;
    private final List<UserProfile> attendees = new ArrayList<>();
    private FirebaseFirestore db;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendees);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.attendees_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendeesAdapter(this, attendees);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        event = (Event) getIntent().getSerializableExtra("event");

        if (event == null || event.getAcceptedUserIds() == null || event.getAcceptedUserIds().isEmpty()) {
            Toast.makeText(this, "No attendees yet", Toast.LENGTH_SHORT).show();
            return;
        }

        loadAttendeeProfiles();
    }

    private void loadAttendeeProfiles() {
        for (String userId : event.getAcceptedUserIds()) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return;

                        UserProfile profile;

                        // Case 1: User data inside "profile" field
                        if (snapshot.contains("profile")) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.get("profile");
                            String username = map.get("username") != null ? map.get("username").toString() : "(unknown)";
                            String avatarUrl = map.get("avatarUrl") != null ? map.get("avatarUrl").toString() : "";
                            profile = new UserProfile(username, avatarUrl);
                        }
                        // Case 2: Flat structure
                        else {
                            profile = snapshot.toObject(UserProfile.class);
                        }

                        if (profile != null) {
                            attendees.add(profile);
                            adapter.notifyItemInserted(attendees.size() - 1);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("AttendeesActivity", "Failed to fetch user: " + userId, e));
        }
    }
}