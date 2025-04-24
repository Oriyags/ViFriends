package com.oriya_s.tashtit.ADPTERS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oriya_s.model.Event;
import com.oriya_s.tashtit.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public interface OnEventActionListener {
        void onDelete(int position);
    }

    private OnEventActionListener listener;

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
        holder.name.setText(event.name + " (" + event.date + ")");
        holder.description.setText(event.description);
        holder.visibility.setText("Visible to: " + event.visibility);

        holder.deleteButton.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, visibility;
        ImageButton deleteButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            visibility = itemView.findViewById(R.id.event_visibility);
            deleteButton = itemView.findViewById(R.id.event_delete_button);
        }
    }
}