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

    // RecyclerView to display the list of attendees
    private       RecyclerView            recyclerView;
    // Custom adapter to bind user profiles to the RecyclerView
    private       AttendeesAdapter        adapter;
    // List to hold the attendee user profiles
    private final List<UserProfile>       attendees = new ArrayList<>();
    // Firebase Firestore instance to fetch attendee data
    private       FirebaseFirestore       db;
    // The event object passed from the previous activity
    private       Event                   event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables edge-to-edge layout styling
        EdgeToEdge.enable(this);

        // Sets the activity's layout
        setContentView(R.layout.activity_attendees);

        // Ensures proper padding for system UI (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize and configure RecyclerView
        recyclerView = findViewById(R.id.attendees_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Vertical list
        adapter      = new AttendeesAdapter(this, attendees); // Adapter with empty list
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db    = FirebaseFirestore.getInstance();
        // Get the Event object passed from previous activity via Intent
        event = (Event) getIntent().getSerializableExtra("event");

        // If the event is null or has no accepted attendees, show a message and exit
        if (event == null || event.getAcceptedUserIds() == null || event.getAcceptedUserIds().isEmpty()) {
            Toast.makeText(this, "No attendees yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load profile information of accepted attendees
        loadAttendeeProfiles();
    }

    // Fetches user profiles of attendees from Firestore and adds them to the list
    private void loadAttendeeProfiles() {
        for (String userId : event.getAcceptedUserIds()) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        // Skip if no user data was found
                        if (!snapshot.exists()) return;

                        UserProfile profile;

                        // Case 1: User profile is stored inside a nested "profile" field
                        if (snapshot.contains("profile")) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.get("profile");

                            // Extract username and avatar URL safely
                            String username = map.get("username") != null ? map.get("username").toString() : "(unknown)";
                            String avatarUrl = map.get("avatarUrl") != null ? map.get("avatarUrl").toString() : "";

                            profile = new UserProfile(username, avatarUrl);
                        }
                        // Case 2: User document is flat and matches the UserProfile model directly
                        else {
                            profile = snapshot.toObject(UserProfile.class);
                        }

                        // Add valid profile to list and update the adapter
                        if (profile != null) {
                            attendees.add(profile);
                            adapter.notifyItemInserted(attendees.size() - 1);
                        }
                    })
                    // Log errors if profile fetch fails
                    .addOnFailureListener(e -> Log.e("AttendeesActivity", "Failed to fetch user: " + userId, e));
        }
    }
}