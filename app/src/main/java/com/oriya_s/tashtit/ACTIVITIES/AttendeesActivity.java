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

    // UI components
    private RecyclerView     recyclerView;
    private AttendeesAdapter adapter;

    // Data sources
    private final List<UserProfile> attendees = new ArrayList<>();
    private       FirebaseFirestore db;
    private       Event             event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendees);

        // Handle window insets (like status bar and navigation bar) by adding padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the RecyclerView and its adapter
        recyclerView = findViewById(R.id.attendees_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Vertical list layout
        adapter      = new AttendeesAdapter(this, attendees); // Initialize the adapter
        recyclerView.setAdapter(adapter); // Attach the adapter to the RecyclerView

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
        // Retrieve the event object passed from the previous screen
        event = (Event) getIntent().getSerializableExtra("event");

        // If event or list of accepted user IDs is missing, show a message and return
        if (event == null || event.getAcceptedUserIds() == null || event.getAcceptedUserIds().isEmpty()) {
            Toast.makeText(this, "No attendees yet", Toast.LENGTH_SHORT).show();
            return;
        }
        // Load the attendee user profiles from Firestore
        loadAttendeeProfiles();
    }

    // Fetch profile details of each attendee from Firestore
    private void loadAttendeeProfiles() {
        // Loop through all user IDs that accepted the event
        for (String userId : event.getAcceptedUserIds()) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return; // Skip if user doesn't exist

                        UserProfile profile;

                        if (snapshot.contains("profile")) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.get("profile");

                            // Get username or fallback to "(unknown)"
                            String username = map.get("username") != null ? map.get("username").toString() : "(unknown)";
                            // Get avatar URL or fallback to empty string
                            String avatarUrl = map.get("profileImageUrl") != null ? map.get("profileImageUrl").toString() : "";

                            // Create a new UserProfile object manually
                            profile = new UserProfile(username, avatarUrl);
                        } else {
                            // For backward compatibility: convert document directly to UserProfile class
                            profile = snapshot.toObject(UserProfile.class);
                        }

                        // If profile loaded successfully, add it to the list and update the adapter
                        if (profile != null) {
                            attendees.add(profile);
                            adapter.notifyItemInserted(attendees.size() - 1);
                        }
                    })
                    // Handle any errors that occurred while fetching the profile
                    .addOnFailureListener(e -> Log.e("AttendeesActivity", "Failed to fetch user: " + userId, e));
        }
    }
}