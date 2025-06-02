package com.oriya_s.tashtit.ACTIVITIES;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class EventResponseActivity extends AppCompatActivity {

    // Firebase and event-related variables
    private FirebaseFirestore db;
    private String            currentUserId;
    private Event             event;

    // UI components
    private ImageView eventImage;
    private TextView  eventTitle, eventDate, eventDescription;
    private Button    btnAccept, btnDecline;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_response);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Firestore and get current user ID
        db            = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // Bind UI views to variables
        eventImage       = findViewById(R.id.response_event_image);
        eventTitle       = findViewById(R.id.response_event_title);
        eventDate        = findViewById(R.id.response_event_date);
        eventDescription = findViewById(R.id.response_event_description);
        btnAccept        = findViewById(R.id.btn_accept_event);
        btnDecline       = findViewById(R.id.btn_decline_event);

        // Get the event object passed from the previous screen
        event = (Event) getIntent().getSerializableExtra("event");

        // Validate event and user ID
        if (event == null || currentUserId == null) {
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Prevent the event creator from responding to their own event
        if (currentUserId.equals(event.getCreatorId())) {
            Toast.makeText(this, "You cannot respond to your own event.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show event details and set up buttons
        showEventData();
        setButtonListeners();
    }

    // Displays the event's title, date, description, and image (if available)
    private void showEventData() {
        eventTitle.setText(event.getName());
        eventDate.setText(event.getDate());
        eventDescription.setText(event.getDescription());

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            eventImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(event.getImageUri()).into(eventImage);
        } else {
            eventImage.setVisibility(View.GONE);
        }
    }

    // Sets up the "Accept" and "Decline" button functionality
    private void setButtonListeners() {

        // When "Accept" is clicked
        btnAccept.setOnClickListener(v -> {
            if (currentUserId == null) return;

            db.collection("users")
                    .document(event.getCreatorId())
                    .collection("events")
                    .document(event.getId())
                    .update("acceptedUserIds", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                    .addOnSuccessListener(unused -> {
                        // Update local event object
                        if (event.getAcceptedUserIds() == null) {
                            event.setAcceptedUserIds(new ArrayList<>());
                        }
                        if (!event.getAcceptedUserIds().contains(currentUserId)) {
                            event.getAcceptedUserIds().add(currentUserId);
                        }

                        // Return result back to calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("accepted", true);
                        resultIntent.putExtra("event", event);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving response", Toast.LENGTH_SHORT).show();
                    });
        });

        // When "Decline" is clicked
        btnDecline.setOnClickListener(v -> {
            if (currentUserId == null) return;

            db.collection("users")
                    .document(event.getCreatorId())
                    .collection("events")
                    .document(event.getId())
                    .update("acceptedUserIds", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
                    .addOnSuccessListener(unused -> {
                        // Update local event object
                        if (event.getAcceptedUserIds() != null) {
                            event.getAcceptedUserIds().remove(currentUserId);
                        }

                        // Return result back to calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("accepted", false);
                        resultIntent.putExtra("event", event);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating response", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}