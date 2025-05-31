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
    private List<String> friendIds;
    private final HashSet<String> selectedIds;

    public FriendSelectAdapter(List<String> friendNames, HashSet<String> selectedIds) {
        this.friendNames = friendNames;
        this.selectedIds = selectedIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_selector, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        String name = friendNames.get(position);
        String uid = friendIds.get(position);

        holder.nameText.setText(name);
        holder.checkBox.setChecked(selectedIds.contains(uid));

        holder.itemView.setOnClickListener(v -> {
            boolean currentlyChecked = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!currentlyChecked);
            toggleSelection(uid);
        });

        holder.checkBox.setOnClickListener(v -> toggleSelection(uid));
    }

    private void toggleSelection(String uid) {
        if (selectedIds.contains(uid)) {
            selectedIds.remove(uid);
        } else {
            selectedIds.add(uid);
        }
    }

    @Override
    public int getItemCount() {
        return friendNames != null ? friendNames.size() : 0;
    }

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