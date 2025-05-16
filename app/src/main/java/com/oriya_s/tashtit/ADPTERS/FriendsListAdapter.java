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

    private final List<Friend> friends;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    public interface OnFriendClickListener {
        void onFriendClicked(View view, Friend friend);
    }

    public interface OnFriendRemovedListener {
        void onFriendRemoved();
    }

    private OnFriendClickListener clickListener;
    private OnFriendRemovedListener removeListener;

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnFriendRemovedListener(OnFriendRemovedListener listener) {
        this.removeListener = listener;
    }

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
        if (position >= friends.size()) return;
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
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < friends.size()) {
                new AlertDialog.Builder(context)
                        .setTitle("Remove Friend")
                        .setMessage("Are you sure you want to remove " + friend.getName() + "?")
                        .setPositiveButton("Remove", (dialog, which) -> removeFriend(adapterPosition, friend))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFriendClicked(v, friend);
            }
        });
    }

    private void removeFriend(int position, Friend friend) {
        if (currentUser == null || position == RecyclerView.NO_POSITION || position >= friends.size()) return;

        String myId = currentUser.getUid();
        String friendId = friend.getFriendID();

        db.collection("users").document(myId)
                .collection("friends").document(friendId)
                .delete()
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(friendId)
                            .collection("friends").document(myId)
                            .delete();

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