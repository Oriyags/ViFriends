package com.oriya_s.tashtit.ADPTERS;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oriya_s.model.Event;
import com.oriya_s.tashtit.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final Context context;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onDelete(int position);
    }

    public EventAdapter(Context context, List<Event> eventList, OnEventActionListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
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
        holder.name.setText(event.getName() + " (" + event.getDate() + ")");
        holder.description.setText(event.getDescription());
        holder.visibility.setText("Visible to: " + event.getVisibility());

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageURI(Uri.parse(event.getImageUri()));
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, visibility;
        ImageButton deleteButton;
        ImageView image;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            visibility = itemView.findViewById(R.id.event_visibility);
            image = itemView.findViewById(R.id.event_image);
            deleteButton = itemView.findViewById(R.id.event_delete_button);
        }
    }
}