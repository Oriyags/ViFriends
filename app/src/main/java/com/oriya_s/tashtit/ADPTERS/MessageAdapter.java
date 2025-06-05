package com.oriya_s.tashtit.ADPTERS;

import android.speech.tts.TextToSpeech;       // *** TTS import
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

    private final List<Message>   messageList;
    private final String          currentUserId;
    private final TextToSpeech    tts;         // *** TTS instance ***

    // Modified constructor to accept TextToSpeech
    public MessageAdapter(List<Message> messageList, TextToSpeech tts) {
        this.messageList   = messageList;
        this.tts           = tts;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Determines the type of view for each message (sent = 1, received = 0)
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderID().equals(currentUserId) ? 1 : 0;
    }

    // Inflates the appropriate layout (sent or received) based on viewType
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_message_sent
                : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    // Binds each message's text and time; also sets up click-to-speak
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.messageText.setText(msg.getText());

        // Format timestamp to "HH:mm"
        long timestamp = msg.getTimestamp();
        String timeFormatted = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
        holder.messageTime.setText(timeFormatted);

        // *** Set click‐to‐speak on the message TextView ***
        holder.messageText.setOnClickListener(v -> {
            String toSpeak = msg.getText();
            if (toSpeak != null && !toSpeak.isEmpty() && tts != null) {
                // QUEUE_FLUSH ensures any current speech is replaced
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "MSG_ID_" + position);
            }
        });
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