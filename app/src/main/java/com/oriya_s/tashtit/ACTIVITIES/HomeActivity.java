package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.oriya_s.tashtit.ADPTERS.EventAdapter;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final int ADD_EVENT_REQUEST = 1;

    private ImageButton menuButton, eventsButton, callButton, cameraButton, chatButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView eventListView;
    private ArrayList<Event> eventList;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setListeners();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuButton = findViewById(R.id.menu_button);
        eventsButton = findViewById(R.id.events_button);
        callButton = findViewById(R.id.call_button);
        cameraButton = findViewById(R.id.camera_button);
        chatButton = findViewById(R.id.chat_button);

        eventListView = findViewById(R.id.event_list);
        eventList = new ArrayList<>();
        adapter = new EventAdapter(this, eventList, position -> {
            eventList.remove(position);
            adapter.notifyItemRemoved(position);
        });
        eventListView.setLayoutManager(new LinearLayoutManager(this));
        eventListView.setAdapter(adapter);
    }

    private void setListeners() {
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        eventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });

        callButton.setOnClickListener(v -> showToast("Choose Friend to Call"));
        cameraButton.setOnClickListener(v -> showToast("Choose Friend for Video Call"));
        chatButton.setOnClickListener(v -> showToast("Choose Friend to Chat With"));

        navigationView.setNavigationItemSelectedListener(item -> {
            handleDrawerItemClick(item);
            return true;
        });
    }

    private void handleDrawerItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_user) {
            showToast("User Profile");
        } else if (id == R.id.nav_settings) {
            showToast("Settings");
        } else if (id == R.id.nav_logout) {
            showToast("Logged Out");
        }

        drawerLayout.closeDrawer(navigationView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_EVENT_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra("event_name");
            String description = data.getStringExtra("event_description");
            String date = data.getStringExtra("event_date");
            String visibility = data.getStringExtra("event_visibility");

            Event event = new Event(name, description, date, visibility);
            eventList.add(event);
            adapter.notifyItemInserted(eventList.size() - 1);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}