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
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.R;

import java.util.List;

public class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.RequestViewHolder> {

    private final List<Friend>      requests;
    // Context for layout inflation and UI feedback
    private final Context           context;
    // Firestore instance to interact with database
    private final FirebaseFirestore db;
    // Currently logged-in Firebase user
    private final FirebaseUser      currentUser;

    // Constructor
    public PendingRequestsAdapter(Context context, List<Friend> requests, FirebaseFirestore db, FirebaseUser currentUser) {
        this.context     = context;
        this.requests    = requests;
        this.db          = db;
        this.currentUser = currentUser;
    }

    // Inflate the layout for each pending request item
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_request, parent, false);
        return new RequestViewHolder(view);
    }

    // Bind data to the view holder at a given position
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Friend request = requests.get(position);

        // Display the requester's name
        holder.requestName.setText(request.getName());

        // Load the requester's avatar if available
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(request.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.requestAvatar);
        } else {
            holder.requestAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Handle accept and decline button clicks
        holder.btnAccept.setOnClickListener(v -> acceptRequest(request, position));
        holder.btnDecline.setOnClickListener(v -> declineRequest(request, position));
    }

    // Accept a friend request: update current user's status, and add reciprocal entry to sender
    private void acceptRequest(Friend request, int position) {
        if (currentUser == null) return;

        // Step 1: Update current user's side to "accepted"
        db.collection("users").document(currentUser.getUid())
                .collection("friends").document(request.getFriendID())
                .update("status", "accepted")
                .addOnSuccessListener(unused -> {
                    // Step 2: Get current user's profile info
                    db.collection("UserProfiles").document(currentUser.getUid()).get()
                            .addOnSuccessListener(currentProfileDoc -> {
                                String currentName = currentProfileDoc.getString("username");
                                String currentAvatar = currentProfileDoc.getString("profileImageUrl");

                                // Step 3: Create Friend object and save it
                                Friend reciprocalFriend = new Friend(
                                        currentUser.getUid(),
                                        currentName,
                                        currentAvatar,
                                        request.getFriendID(),
                                        "accepted"
                                );

                                // Step 4: Save entry to sender's "friends" collection
                                db.collection("users").document(request.getFriendID())
                                        .collection("friends").document(currentUser.getUid())
                                        .set(reciprocalFriend)
                                        .addOnSuccessListener(aVoid -> {
                                            // Step 5: Update UI
                                            requests.remove(position);
                                            notifyItemRemoved(position);
                                            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                        });
                            });
                });
    }

    // Decline a friend request: delete both references
    private void declineRequest(Friend request, int position) {
        if (currentUser == null) return;

        // Step 1: Delete request from current user's side
        db.collection("users").document(currentUser.getUid())
                .collection("friends").document(request.getFriendID())
                .delete()
                .addOnSuccessListener(unused ->
                        // Step 2: Delete reference from sender's side
                        db.collection("users").document(request.getFriendID())
                                .collection("friends").document(currentUser.getUid())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Step 3: Update UI
                                    requests.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                                })
                );
    }

    // Return the number of pending requests
    @Override
    public int getItemCount() {
        return requests.size();
    }

    // ViewHolder class for binding UI components
    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView  requestName;
        ImageView requestAvatar;
        Button    btnAccept, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestName   = itemView.findViewById(R.id.tv_request_name);
            requestAvatar = itemView.findViewById(R.id.iv_request_avatar);
            btnAccept     = itemView.findViewById(R.id.btn_accept_request);
            btnDecline    = itemView.findViewById(R.id.btn_decline_request);
        }
    }
}