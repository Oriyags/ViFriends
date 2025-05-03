package com.oriya_s.tashtit.ADPTERS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.R;

import java.util.List;

public class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.RequestViewHolder> {

    private final List<Friend> requests;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    public PendingRequestsAdapter(Context context, List<Friend> requests, FirebaseFirestore db, FirebaseUser currentUser) {
        this.context = context;
        this.requests = requests;
        this.db = db;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Friend request = requests.get(position);
        holder.requestName.setText(request.getName());

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(request.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.requestAvatar);
        } else {
            holder.requestAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnAccept.setOnClickListener(v -> acceptRequest(request, position));
        holder.btnDecline.setOnClickListener(v -> declineRequest(request, position));
    }

    private void acceptRequest(Friend request, int position) {
        if (currentUser == null) return;

        // Update status in receiver's list (current user)
        db.collection("users").document(currentUser.getUid())
                .collection("friends").document(request.getFriendID())
                .update("status", "accepted")
                .addOnSuccessListener(unused -> {
                    // Get current user's profile to build reciprocal friend object
                    db.collection("UserProfiles").document(currentUser.getUid()).get()
                            .addOnSuccessListener(currentProfileDoc -> {
                                String currentName = currentProfileDoc.getString("username");
                                String currentAvatar = currentProfileDoc.getString("profileImageUrl");

                                Friend reciprocalFriend = new Friend(
                                        currentUser.getUid(),
                                        currentName,
                                        currentAvatar,
                                        request.getFriendID(),
                                        "accepted"
                                );

                                // Add reciprocal record to sender's friend list
                                db.collection("users").document(request.getFriendID())
                                        .collection("friends").document(currentUser.getUid())
                                        .set(reciprocalFriend)
                                        .addOnSuccessListener(aVoid -> {
                                            requests.remove(position);
                                            notifyItemRemoved(position);
                                            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                        });
                            });
                });
    }

    private void declineRequest(Friend request, int position) {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .collection("friends").document(request.getFriendID())
                .delete()
                .addOnSuccessListener(unused -> db.collection("users")
                        .document(request.getFriendID())
                        .collection("friends")
                        .document(currentUser.getUid())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            requests.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                        }));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView requestName;
        ImageView requestAvatar;
        Button btnAccept, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestName = itemView.findViewById(R.id.tv_request_name);
            requestAvatar = itemView.findViewById(R.id.iv_request_avatar);
            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnDecline = itemView.findViewById(R.id.btn_decline_request);
        }
    }
}