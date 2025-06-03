package com.oriya_s.tashtit.ADPTERS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.oriya_s.model.UserProfile;
import com.oriya_s.tashtit.R;

import java.util.List;

/**
 * Adapter to display a list of event attendees in a RecyclerView.
 * Each attendee shows their name and profile picture.
 */
public class AttendeesAdapter extends RecyclerView.Adapter<AttendeesAdapter.AttendeeViewHolder> {

    // List of attendees to display
    private final List<UserProfile> attendees;

    // Context is needed for layout inflation and Glide
    private final Context context;

    // Constructor to initialize the adapter with context and data
    public AttendeesAdapter(Context context, List<UserProfile> attendees) {
        this.context = context;
        this.attendees = attendees;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type.
     * Inflates the layout for a single attendee item.
     */
    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the attendee layout (item_attendee.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendee, parent, false);
        return new AttendeeViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at the specified position.
     * Binds data from the UserProfile object to the view holder.
     */
    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        // Get the attendee at the current position
        UserProfile attendee = attendees.get(position);

        // Set the attendee's name
        holder.attendeeName.setText(attendee.getUsername());

        // Load profile image using Glide
        String avatarUrl = attendee.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(context).load(avatarUrl)                 // Load the image from URL
                    .placeholder(R.drawable.ic_default_avatar)  // Show default while loading
                    .error(R.drawable.ic_default_avatar)        // Show default if failed
                    .into(holder.attendeeImage);                // Load into ImageView
        } else {
            // If no avatar URL, use a default image
            holder.attendeeImage.setImageResource(R.drawable.ic_default_avatar);
        }
    }

     // Returns the total number of attendees in the list.
    @Override
    public int getItemCount() {
        return attendees.size();
    }

    // ViewHolder class to hold the views for each attendee item.
    static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        TextView  attendeeName;
        ImageView attendeeImage;

        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            attendeeName  = itemView.findViewById(R.id.attendee_name);
            attendeeImage = itemView.findViewById(R.id.attendee_image);
        }
    }
}