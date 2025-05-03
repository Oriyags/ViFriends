package com.oriya_s.tashtit.ADPTERS;

import android.app.AlertDialog;
import android.content.Context;
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

    private final List<Friend> friends;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    public FriendsListAdapter(Context context, List<Friend> friends, FirebaseFirestore db, FirebaseUser currentUser) {
        this.context = context;
        this.friends = friends;
        this.db = db;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.friendName.setText(friend.getName());

        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.friendAvatar);
        } else {
            holder.friendAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnRemove.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove " + friend.getName() + "?")
                    .setPositiveButton("Remove", (dialog, which) -> removeFriend(friend, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void removeFriend(Friend friend, int position) {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .document(friend.getFriendID())
                .delete()
                .addOnSuccessListener(unused -> {
                    friends.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Friend removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;
        ImageView friendAvatar;
        ImageButton btnRemove;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.tv_friend_name);
            friendAvatar = itemView.findViewById(R.id.iv_friend_avatar);
            btnRemove = itemView.findViewById(R.id.btn_remove_friend);
        }
    }
}
