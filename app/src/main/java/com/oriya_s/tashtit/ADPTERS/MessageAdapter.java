package com.oriya_s.tashtit.ADPTERS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.oriya_s.model.Message;
import com.oriya_s.tashtit.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messageList;
    // ID of the current user (used to check if a message is sent or received)
    private final String        currentUserId;

    // Constructor
    public MessageAdapter(List<Message> messageList) {
        this.messageList   = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Determines the type of view for each message (sent = 1, received = 0)
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderID().equals(currentUserId) ? 1 : 0;
    }

    // Inflates the appropriate layout (sent or received) based on view type
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    // Binds each message's text and formatted time to the views
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.messageText.setText(msg.getText());

        // Format timestamp to "HH:mm" format (e.g., "14:23")
        long timestamp = msg.getTimestamp();
        String timeFormatted = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        holder.messageTime.setText(timeFormatted);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder class holds references to the views in each message item layout
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
    }
}