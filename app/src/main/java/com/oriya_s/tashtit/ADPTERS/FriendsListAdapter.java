package com.oriya_s.tashtit.ADPTERS;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.R;

import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> {

    private final List<Friend>      friends;
    // Context for inflating views and using Glide/Dialogs
    private final Context           context;
    // Firebase instances for removing friends from Firestore
    private final FirebaseFirestore db;
    private final FirebaseUser      currentUser;

    // Listener interface for when a friend is clicked
    public interface OnFriendClickListener {
        void onFriendClicked(View view, Friend friend);
    }

    // Listener interface for when a friend is removed
    public interface OnFriendRemovedListener {
        void onFriendRemoved();
    }

    private OnFriendClickListener clickListener;
    private OnFriendRemovedListener removeListener;

    // Allow external setting of click listener
    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.clickListener = listener;
    }

    // Allow external setting of removal listener
    public void setOnFriendRemovedListener(OnFriendRemovedListener listener) {
        this.removeListener = listener;
    }

    // Constructor to initialize data and Firebase references
    public FriendsListAdapter(Context context, List<Friend> friends, FirebaseFirestore db, FirebaseUser currentUser) {
        this.context     = context;
        this.friends     = friends;
        this.db          = db;
        this.currentUser = currentUser;
    }

    // Inflate the layout for each friend item
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    // Bind friend data to the view for a given position
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        if (position >= friends.size()) return;

        Friend friend = friends.get(position);
        holder.friendName.setText(friend.getName());

        // Load avatar using Glide, or use default if missing
        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.friendAvatar);
        } else {
            holder.friendAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Handle friend removal button
        holder.btnRemove.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < friends.size()) {
                // Show confirmation dialog before removing
                new AlertDialog.Builder(context)
                        .setTitle("Remove Friend")
                        .setMessage("Are you sure you want to remove " + friend.getName() + "?")
                        .setPositiveButton("Remove", (dialog, which) -> removeFriend(adapterPosition, friend))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Trigger friend click listener if implemented
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFriendClicked(v, friend);
            }
        });
    }

    // Handles the actual removal of the friend from Firestore
    private void removeFriend(int position, Friend friend) {
        if (currentUser == null || position == RecyclerView.NO_POSITION || position >= friends.size()) return;

        String myId     = currentUser.getUid();
        String friendId = friend.getFriendID();

        // Remove the friend from current user's subcollection
        db.collection("users").document(myId)
                .collection("friends").document(friendId)
                .delete()
                .addOnSuccessListener(unused -> {
                    // Also remove the current user from the friend's list
                    db.collection("users").document(friendId)
                            .collection("friends").document(myId)
                            .delete();

                    // Remove from UI
                    if (position < friends.size()) {
                        friends.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Friend removed", Toast.LENGTH_SHORT).show();
                        if (removeListener != null) removeListener.onFriendRemoved();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendRemoval", "Failed to delete friend", e);
                    Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    // ViewHolder class to hold views for each friend item
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView    friendName;
        ImageView   friendAvatar;
        ImageButton btnRemove;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName   = itemView.findViewById(R.id.tv_friend_name);
            friendAvatar = itemView.findViewById(R.id.iv_friend_avatar);
            btnRemove    = itemView.findViewById(R.id.btn_remove_friend);
        }
    }
}