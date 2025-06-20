package com.oriya_s.tashtit.ADPTERS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.util.List;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // Context for UI and launching intents
    private final Context           context;
    private final ArrayList<Event>  eventList;
    // Currently logged-in user
    private final FirebaseUser      currentUser;
    // Reference to Firestore for fetching additional data (e.g. user avatars)
    private final FirebaseFirestore db;

    // Constructor initializes adapter with context and list
    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context     = context;
        this.eventList   = eventList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.db          = FirebaseFirestore.getInstance();
    }

    // Inflates the layout for each event item (converting an XML layout resource into View objects in memory)
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    // Fills this card with info about one event, wire the buttons, and make it interactive
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if (position < 0 || position >= eventList.size()) return;

        Event event = eventList.get(position);

        // Basic info of the event
        holder.eventTitle.setText(event.getName());
        holder.eventDescription.setText(event.getDescription());
        holder.eventVisibility.setText("Visible: " + event.getVisibility());
        holder.eventDate.setText(event.getDate());

        // Creator name (if available)
        if (event.getCreatorName() != null) {
            holder.eventAuthorName.setText(event.getCreatorName());
            holder.eventAuthorName.setVisibility(View.VISIBLE);
        } else {
            holder.eventAuthorName.setVisibility(View.GONE);
        }

        // Load creator avatar from Firestore
        holder.eventAuthorImage.setVisibility(View.VISIBLE);
        if (event.getCreatorId() != null && !event.getCreatorId().isEmpty()) {
            db.collection("users")
                    .document(event.getCreatorId())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists() && snapshot.contains("profile")) {
                            Map<String, Object> profile = (Map<String, Object>) snapshot.get("profile");
                            String avatarUrl = profile.get("profileImageUrl") != null
                                    ? profile.get("profileImageUrl").toString()
                                    : null;

                            if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals("null")) {
                                Glide.with(context)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.ic_default_avatar)
                                        .error(R.drawable.ic_default_avatar)
                                        .into(holder.eventAuthorImage);
                            } else {
                                holder.eventAuthorImage.setImageResource(R.drawable.ic_default_avatar);
                            }
                        } else {
                            holder.eventAuthorImage.setImageResource(R.drawable.ic_default_avatar);
                        }
                    })
                    .addOnFailureListener(e -> holder.eventAuthorImage.setImageResource(R.drawable.ic_default_avatar));
        } else {
            holder.eventAuthorImage.setImageResource(R.drawable.ic_default_avatar);
        }

        // Load image attached to event
        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(event.getImageUri()).into(holder.eventImage);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        // Show video icon if video exists
        holder.eventVideoLabel.setVisibility(
                event.getVideoUri() != null && !event.getVideoUri().isEmpty() ? View.VISIBLE : View.GONE
        );

        // Show location if available (either address or coordinates)
        if (event.getAddress() != null && !event.getAddress().isEmpty()) {
            holder.eventLocationInfo.setText("📍 " + event.getAddress());
            holder.eventLocationInfo.setVisibility(View.VISIBLE);
        } else if (event.getLatitude() != 0 && event.getLongitude() != 0) {
            String coords = String.format("📍 Lat: %.5f, Lng: %.5f", event.getLatitude(), event.getLongitude());
            holder.eventLocationInfo.setText(coords);
            holder.eventLocationInfo.setVisibility(View.VISIBLE);
        } else {
            holder.eventLocationInfo.setVisibility(View.GONE);
        }

        // Show how many friends accepted
        List<String> acceptedUserIds = event.getAcceptedUserIds();
        if (acceptedUserIds != null && !acceptedUserIds.isEmpty()) {
            holder.eventAttendanceCount.setVisibility(View.VISIBLE);
            String text = acceptedUserIds.size() == 1 ? "1 friend is going" : acceptedUserIds.size() + " friends are going";
            holder.eventAttendanceCount.setText(text);
        } else {
            holder.eventAttendanceCount.setVisibility(View.GONE);
        }

        // Show delete button if user is creator
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

        // Open attendee list
        holder.btnFriends.setOnClickListener(v -> {
            Intent intent = new Intent(context, AttendeesActivity.class);
            intent.putExtra("event", event);
            context.startActivity(intent);
        });

        // Play event video
        if (event.getVideoUri() != null && !event.getVideoUri().isEmpty()) {
            holder.btnVideo.setVisibility(View.VISIBLE);
            holder.btnVideo.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getVideoUri()));
                intent.setDataAndType(Uri.parse(event.getVideoUri()), "video/*");
                context.startActivity(intent);
            });
        } else {
            holder.btnVideo.setVisibility(View.GONE);
        }

        // Open map for event location
        if (event.getLatitude() != 0 && event.getLongitude() != 0) {
            holder.btnMap.setVisibility(View.VISIBLE);
            holder.btnMap.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:" + event.getLatitude() + "," + event.getLongitude()
                        + "?q=" + event.getLatitude() + "," + event.getLongitude() + "(Event)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
            });
        } else {
            holder.btnMap.setVisibility(View.GONE);
        }

        // If current user is not the creator, allow responding to the event
        holder.itemView.setOnClickListener(v -> {
            if (currentUser != null && !currentUser.getUid().equals(event.getCreatorId())) {
                Intent intent = new Intent(context, EventResponseActivity.class);
                intent.putExtra("event", event);
                ((Activity) context).startActivityForResult(intent, 1234);
            }
        });
    }

    // Return number of items in the list
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class: holds references to views for reuse
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView    eventTitle, eventDescription, eventVisibility, eventDate, eventVideoLabel, eventAuthorName, eventAttendanceCount, eventLocationInfo;
        ImageView   eventImage, eventAuthorImage;
        ImageButton deleteButton, btnFriends, btnVideo, btnMap;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle           = itemView.findViewById(R.id.event_title);
            eventDescription     = itemView.findViewById(R.id.event_description);
            eventVisibility      = itemView.findViewById(R.id.event_visibility);
            eventDate            = itemView.findViewById(R.id.event_date);
            eventVideoLabel      = itemView.findViewById(R.id.event_video_label);
            eventAttendanceCount = itemView.findViewById(R.id.event_attendance_count);
            eventAuthorName      = itemView.findViewById(R.id.event_author_name);
            eventAuthorImage     = itemView.findViewById(R.id.event_author_image);
            eventImage           = itemView.findViewById(R.id.event_image_preview);
            eventLocationInfo    = itemView.findViewById(R.id.event_location_info);
            deleteButton         = itemView.findViewById(R.id.event_delete_button);
            btnFriends           = itemView.findViewById(R.id.event_btn_friends);
            btnVideo             = itemView.findViewById(R.id.event_btn_video);
            btnMap               = itemView.findViewById(R.id.event_btn_map);
        }
    }
}