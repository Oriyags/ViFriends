package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.ADPTERS.EventAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    // Constants for activity request codes and permission
    private static final int ADD_EVENT_REQUEST                    = 1;
    private static final int FRIENDS_REQUEST_CODE                 = 2;
    private static final int EVENT_RESPONSE_REQUEST               = 3;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    // UI components
    private ImageButton    menuButton, eventsButton;
    private Button         btnViewFriends;
    private TextView       friendsSummaryText;
    private DrawerLayout   drawerLayout;
    private NavigationView navigationView;

    // RecyclerView and Adapter for events
    private RecyclerView     eventListView;
    private ArrayList<Event> eventList;
    private EventAdapter     adapter;

    // Firebase components
    private FirebaseUser         currentUser;
    private FirebaseFirestore    firestore;
    private ListenerRegistration eventListener;

    // List of friends with accepted status
    private final Set<String> acceptedFriendIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore   = FirebaseFirestore.getInstance();

        // If user is not logged in, return to login screen
        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        requestNotificationPermission();     // Ask for push notification permission (Android 13+)
        sendWelcomeNotification();           // Send welcome message in chat
        initializeViews();                   // Bind and initialize UI elements
        setListeners();
        loadAcceptedFriendsAndEvents();      // Load events visible to the user
        loadFriendsCount();                  // Show friend count in text view
    }

    // Requests push notification permission (Android 13+)
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    // Handle result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Send a welcome message to the user
    private void sendWelcomeNotification() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (token != null && currentUser != null) {
                firestore.collection("users")
                        .document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            String name = snapshot.getString("profile.username");

                            Map<String, Object> messageData = new HashMap<>();
                            messageData.put("senderId", currentUser.getUid());
                            messageData.put("senderName", name != null ? name : "Someone");
                            messageData.put("text", "Welcome to ViFriends!");

                            firestore.collection("users")
                                    .document(currentUser.getUid())
                                    .collection("chats")
                                    .document("welcome")
                                    .collection("messages")
                                    .add(messageData);
                        });
            }
        });
    }

    // Binds all views and initializes the RecyclerView
    private void initializeViews() {
        drawerLayout       = findViewById(R.id.drawer_layout);
        navigationView     = findViewById(R.id.navigation_view);
        menuButton         = findViewById(R.id.menu_button);
        eventsButton       = findViewById(R.id.events_button);
        btnViewFriends     = findViewById(R.id.btn_view_friends);
        friendsSummaryText = findViewById(R.id.friends_summary_text);

        eventListView = findViewById(R.id.event_list);
        eventList     = new ArrayList<>();
        adapter       = new EventAdapter(this, eventList);
        eventListView.setLayoutManager(new LinearLayoutManager(this));
        eventListView.setAdapter(adapter);
    }

    // Sets up button click listeners and drawer menu logic
    private void setListeners() {
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));
        eventsButton.setOnClickListener(v -> startActivityForResult(new Intent(this, AddEventActivity.class), ADD_EVENT_REQUEST));
        btnViewFriends.setOnClickListener(v -> startActivityForResult(new Intent(this, FriendsListActivity.class), FRIENDS_REQUEST_CODE));
        navigationView.setNavigationItemSelectedListener(this::handleDrawerItemClick);
    }

    // Handles item clicks in the navigation drawer
    private boolean handleDrawerItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_user) startActivity(new Intent(this, UserProfileActivity.class));
        else if (id == R.id.nav_settings) startActivity(new Intent(this, SettingsActivity.class));
        else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    // Loads all accepted friends, then loads visible events
    private void loadAcceptedFriendsAndEvents() {
        if (currentUser == null) return;

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    acceptedFriendIds.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        acceptedFriendIds.add(doc.getId());
                    }
                    loadEventsFromFirebase();
                });
    }

    // Loads events from all users (collectionGroup) and applies visibility filters
    private void loadEventsFromFirebase() {
        if (currentUser == null) return;

        eventListener = firestore.collectionGroup("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    eventList.clear();
                    long currentTime = System.currentTimeMillis();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null || event.getVisibility() == null || event.getCreatorId() == null)
                            continue;

                        // Check if event has expired
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                            sdf.setLenient(false);
                            java.util.Date eventDate = sdf.parse(event.getDate());

                            long oneDayMillis = 24 * 60 * 60 * 1000L;
                            if (eventDate != null && currentTime > (eventDate.getTime() + oneDayMillis)) {
                                firestore.collection("users")
                                        .document(event.getCreatorId())
                                        .collection("events")
                                        .document(doc.getId())
                                        .delete();
                                continue;
                            }

                            boolean isOwner = event.getCreatorId().equals(currentUser.getUid());
                            boolean isFriend = acceptedFriendIds.contains(event.getCreatorId());

                            boolean canView = isOwner ||
                                    ("all".equals(event.getVisibility()) && isFriend) ||
                                    ("selected".equals(event.getVisibility()) &&
                                            event.getVisibleTo() != null &&
                                            event.getVisibleTo().contains(currentUser.getUid()));

                            if (canView) {
                                event.setId(doc.getId());

                                if (doc.contains("latitude")) event.setLatitude(doc.getDouble("latitude"));
                                if (doc.contains("longitude")) event.setLongitude(doc.getDouble("longitude"));
                                if (doc.contains("address")) event.setAddress(doc.getString("address"));

                                eventList.add(event);
                            }
                            // If an error happens
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged(); // Refresh UI
                });
    }

    // Loads the number of accepted friends
    private void loadFriendsCount() {
        if (currentUser == null) return;

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> updateFriendCountText(snapshot.size()));
    }

    // Updates the friend count label
    private void updateFriendCountText(int count) {
        friendsSummaryText.setText(count == 1 ? "You have 1 friend." : "You have " + count + " friends.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFriendsCount();
    }

    // Handles results from Add Event and Friends screens
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FRIENDS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int updatedCount = data.getIntExtra("updatedFriendCount", -1);
            if (updatedCount != -1) updateFriendCountText(updatedCount);
        }

        if (requestCode == EVENT_RESPONSE_REQUEST && data != null) {
            Event updatedEvent = (Event) data.getSerializableExtra("event");
            boolean accepted = data.getBooleanExtra("accepted", false);

            if (updatedEvent != null) {
                for (int i = 0; i < eventList.size(); i++) {
                    Event e = eventList.get(i);
                    if (e.getId().equals(updatedEvent.getId())) {
                        eventList.set(i, updatedEvent);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }

                Toast.makeText(this, accepted ? "Event accepted" : "Event declined", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Deletes an event from Firestore and updates the UI
    public void deleteEvent(Event event, int position) {
        if (currentUser == null || event.getId() == null) return;

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("events")
                .document(event.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();

                    if (position >= 0 && position < eventList.size()) {
                        eventList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        for (int i = 0; i < eventList.size(); i++) {
                            if (eventList.get(i).getId().equals(event.getId())) {
                                eventList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    // Clean up the Firestore listener when activity is destroyed
    @Override
    protected void onDestroy() {
        if (eventListener != null) eventListener.remove();
        super.onDestroy();
    }
}