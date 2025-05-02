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
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.ACTIVITIES.AddEventActivity;
import com.oriya_s.tashtit.ACTIVITIES.HomeActivity;
import com.oriya_s.tashtit.R;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final Context context;
    private final ArrayList<Event> eventList;

    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
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

        // Show image preview using Glide
        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(event.getImageUri()).into(holder.eventImage);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        // Show video label if available
        if (event.getVideoUri() != null && !event.getVideoUri().isEmpty()) {
            holder.eventVideoLabel.setVisibility(View.VISIBLE);
        } else {
            holder.eventVideoLabel.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (context instanceof HomeActivity) {
                ((HomeActivity) context).deleteEvent(event, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEventActivity.class);
            intent.putExtra("edit_event", event); // requires Serializable or Parcelable
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 2); // EDIT_EVENT_REQUEST
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDescription, eventVisibility, eventDate, eventVideoLabel;
        ImageView eventImage;
        ImageButton deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDescription = itemView.findViewById(R.id.event_description);
            eventVisibility = itemView.findViewById(R.id.event_visibility);
            eventDate = itemView.findViewById(R.id.event_date);
            eventImage = itemView.findViewById(R.id.event_image_preview);
            eventVideoLabel = itemView.findViewById(R.id.event_video_label);
            deleteButton = itemView.findViewById(R.id.event_delete_button);
        }
    }
}