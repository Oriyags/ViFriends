package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oriya_s.tashtit.ADPTERS.FriendsListAdapter;
import com.oriya_s.tashtit.R;

import java.util.Arrays;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView friendsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        friendsRecyclerView = findViewById(R.id.recycler_friends);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data
        List<String> friends = Arrays.asList("Alice", "Bob", "Charlie", "Dana", "Eli");

        FriendsListAdapter adapter = new FriendsListAdapter(friends);
        friendsRecyclerView.setAdapter(adapter);

        Toast.makeText(this, "Showing Friends List", Toast.LENGTH_SHORT).show();
    }
}
