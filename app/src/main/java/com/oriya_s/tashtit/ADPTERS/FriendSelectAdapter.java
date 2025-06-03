package com.oriya_s.tashtit.ADPTERS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oriya_s.tashtit.R;

import java.util.HashSet;
import java.util.List;

public class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.FriendViewHolder> {

    private final List<String> friendNames;
    // List of friend unique IDs
    private       List<String> friendIds;
    // Set of selected friend IDs
    private final HashSet<String> selectedIds;

    // Constructor
    public FriendSelectAdapter(List<String> friendNames, HashSet<String> selectedIds) {
        this.friendNames = friendNames;
        this.selectedIds = selectedIds;
    }

    // Setter method
    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    // Called when a new ViewHolder needs to be created (i.e., new item_friend_selector layout)
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_selector, parent, false);
        return new FriendViewHolder(view);
    }

    // Called to bind data to a specific ViewHolder based on its position
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        // Get name and UID of the current friend
        String name = friendNames.get(position);
        String uid  = friendIds.get(position);

        // Display friend name in TextView
        holder.nameText.setText(name);

        // Set checkbox state based on whether this UID is in the selected set
        holder.checkBox.setChecked(selectedIds.contains(uid));

        // Toggle checkbox when item row is clicked
        holder.itemView.setOnClickListener(v -> {
            boolean currentlyChecked = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!currentlyChecked); // toggle visual state
            toggleSelection(uid); // update set
        });

        // Also allow toggling from direct checkbox tap
        holder.checkBox.setOnClickListener(v -> toggleSelection(uid));
    }

    // Add or remove UID from the selected set
    private void toggleSelection(String uid) {
        if (selectedIds.contains(uid)) {
            selectedIds.remove(uid); // unselect if already selected
        } else {
            selectedIds.add(uid); // select if not selected
        }
    }

    // Total number of items to show
    @Override
    public int getItemCount() {
        return friendNames != null ? friendNames.size() : 0;
    }

    // Inner class that holds references to the UI elements for each row
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CheckBox checkBox;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.friend_name_text);
            checkBox = itemView.findViewById(R.id.friend_checkbox);
        }
    }
}