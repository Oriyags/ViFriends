package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.ADPTERS.EventAdapter;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final int ADD_EVENT_REQUEST = 1;

    private ImageButton menuButton, eventsButton;
    private Button btnViewFriends;
    private TextView friendsSummaryText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView eventListView;
    private ArrayList<Event> eventList;
    private EventAdapter adapter;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private ListenerRegistration eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setListeners();
        loadEventsFromFirebase();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuButton = findViewById(R.id.menu_button);
        eventsButton = findViewById(R.id.events_button);
        btnViewFriends = findViewById(R.id.btn_view_friends);
        friendsSummaryText = findViewById(R.id.friends_summary_text);

        eventListView = findViewById(R.id.event_list);
        eventList = new ArrayList<>();
        adapter = new EventAdapter(this, eventList);
        eventListView.setLayoutManager(new LinearLayoutManager(this));
        eventListView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        }
    }

    private void setListeners() {
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        eventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });

        btnViewFriends.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FriendsListActivity.class);
            startActivity(intent);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            handleDrawerItemClick(item);
            return true;
        });
    }

    private void handleDrawerItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_user) {
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class)); // ✅ Open settings screen
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(navigationView);
    }

    private void loadEventsFromFirebase() {
        if (currentUser == null) return;

        eventListener = firestore.collection("users")
                .document(currentUser.getUid())
                .collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    eventList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    public void deleteEvent(Event event, int position) {
        if (currentUser == null || event.getId() == null) return;

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("events")
                .document(event.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    eventList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        if (eventListener != null) {
            eventListener.remove();
        }
        super.onDestroy();
    }
}