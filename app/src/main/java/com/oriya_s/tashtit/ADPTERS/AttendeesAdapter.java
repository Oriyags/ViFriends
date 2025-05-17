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

public class AttendeesAdapter extends RecyclerView.Adapter<AttendeesAdapter.AttendeeViewHolder> {

    private final List<UserProfile> attendees;
    private final Context context;

    public AttendeesAdapter(Context context, List<UserProfile> attendees) {
        this.context = context;
        this.attendees = attendees;
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendee, parent, false);
        return new AttendeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        UserProfile attendee = attendees.get(position);
        holder.attendeeName.setText(attendee.getUsername());

        if (attendee.getAvatarUrl() != null && !attendee.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(attendee.getAvatarUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(holder.attendeeImage);
        } else {
            holder.attendeeImage.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        ImageView attendeeImage;
        TextView attendeeName;

        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            attendeeImage = itemView.findViewById(R.id.attendee_image);
            attendeeName = itemView.findViewById(R.id.attendee_name);
        }
    }
}