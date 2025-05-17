package com.oriya_s.tashtit.ADPTERS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.ACTIVITIES.AttendeesActivity;
import com.oriya_s.tashtit.ACTIVITIES.EventResponseActivity;
import com.oriya_s.tashtit.ACTIVITIES.HomeActivity;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final Context context;
    private final ArrayList<Event> eventList;
    private final FirebaseUser currentUser;
    private final FirebaseFirestore db;

    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventTitle.setText(event.getName());
        holder.eventDescription.setText(event.getDescription());
        holder.eventVisibility.setText("Visible: " + event.getVisibility());
        holder.eventDate.setText(event.getDate());

        // Author name
        if (event.getCreatorName() != null) {
            holder.eventAuthorName.setText(event.getCreatorName());
            holder.eventAuthorName.setVisibility(View.VISIBLE);
        } else {
            holder.eventAuthorName.setVisibility(View.GONE);
        }

        // Author avatar
        holder.eventAuthorImage.setVisibility(View.VISIBLE);
        if (event.getCreatorAvatar() != null && !event.getCreatorAvatar().isEmpty()) {
            Glide.with(context)
                    .load(event.getCreatorAvatar())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(holder.eventAuthorImage);
        } else {
            holder.eventAuthorImage.setImageResource(R.drawable.ic_default_avatar);
        }

        // Event image
        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(event.getImageUri()).into(holder.eventImage);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        // Video label
        if (event.getVideoUri() != null && !event.getVideoUri().isEmpty()) {
            holder.eventVideoLabel.setVisibility(View.VISIBLE);
        } else {
            holder.eventVideoLabel.setVisibility(View.GONE);
        }

        // Clean up deleted users from acceptedUserIds
        List<String> acceptedUserIds = event.getAcceptedUserIds();
        if (acceptedUserIds != null && !acceptedUserIds.isEmpty()) {
            Iterator<String> iterator = acceptedUserIds.iterator();
            final List<String> toRemove = new ArrayList<>();

            while (iterator.hasNext()) {
                String userId = iterator.next();
                db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        toRemove.add(userId);
                    }

                    if (userId.equals(acceptedUserIds.get(acceptedUserIds.size() - 1))) {
                        // When last check completes, update UI
                        acceptedUserIds.removeAll(toRemove);
                        event.setAcceptedUserIds(acceptedUserIds);

                        if (!acceptedUserIds.isEmpty()) {
                            holder.eventAttendanceCount.setVisibility(View.VISIBLE);
                            int count = acceptedUserIds.size();
                            String text = count == 1 ? "1 friend is going" : count + " friends are going";
                            holder.eventAttendanceCount.setText(text);
                        } else {
                            holder.eventAttendanceCount.setVisibility(View.GONE);
                        }

                        // Update in Firestore as well
                        db.collection("users")
                                .document(event.getCreatorId())
                                .collection("events")
                                .document(event.getId())
                                .update("acceptedUserIds", acceptedUserIds);
                    }
                });
            }
        } else {
            holder.eventAttendanceCount.setVisibility(View.GONE);
        }

        // Delete button
        if (currentUser != null && currentUser.getUid().equals(event.getCreatorId())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).deleteEvent(event, holder.getAdapterPosition());
                }
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Click handling
        holder.itemView.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getUid().equals(event.getCreatorId())) {
                Intent intent = new Intent(context, AttendeesActivity.class);
                intent.putExtra("event", event);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, EventResponseActivity.class);
                intent.putExtra("event", event);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 3);
                } else {
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDescription, eventVisibility, eventDate, eventVideoLabel, eventAuthorName, eventAttendanceCount;
        ImageView eventImage, eventAuthorImage;
        ImageButton deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDescription = itemView.findViewById(R.id.event_description);
            eventVisibility = itemView.findViewById(R.id.event_visibility);
            eventDate = itemView.findViewById(R.id.event_date);
            eventVideoLabel = itemView.findViewById(R.id.event_video_label);
            eventAttendanceCount = itemView.findViewById(R.id.event_attendance_count);
            eventAuthorName = itemView.findViewById(R.id.event_author_name);
            eventAuthorImage = itemView.findViewById(R.id.event_author_image);
            eventImage = itemView.findViewById(R.id.event_image_preview);
            deleteButton = itemView.findViewById(R.id.event_delete_button);
        }
    }
}