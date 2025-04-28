package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oriya_s.model.Event;
import com.oriya_s.tashtit.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDescriptionInput;
    private Button pickDateButton, chooseFriendsButton, saveButton, pickVideoButton;
    private TextView dateText;
    private RadioGroup visibilityGroup;
    private ImageView selectedImage;
    private VideoView selectedVideo;

    private String imageUri = "";
    private String videoUri = "";

    private static final int IMAGE_PICK_CODE = 101;
    private static final int VIDEO_PICK_CODE = 202;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eventNameInput = findViewById(R.id.event_name);
        eventDescriptionInput = findViewById(R.id.event_description);
        pickDateButton = findViewById(R.id.pick_date_button);
        dateText = findViewById(R.id.date_text);
        visibilityGroup = findViewById(R.id.visibility_group);
        chooseFriendsButton = findViewById(R.id.choose_friends_button);
        saveButton = findViewById(R.id.save_event_button);
        selectedImage = findViewById(R.id.selected_image);
        pickVideoButton = findViewById(R.id.pick_video_button);
        selectedVideo = findViewById(R.id.selected_video);

        pickDateButton.setOnClickListener(v -> showDatePicker());
        selectedImage.setOnClickListener(v -> pickImageFromGallery());
        pickVideoButton.setOnClickListener(v -> pickVideoFromGallery());
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            dateText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selected = data.getData();

            if (requestCode == IMAGE_PICK_CODE) {
                imageUri = selected.toString();
                selectedImage.setImageURI(selected);
            } else if (requestCode == VIDEO_PICK_CODE) {
                videoUri = selected.toString();
                selectedVideo.setVisibility(View.VISIBLE);
                selectedVideo.setVideoURI(selected);
                selectedVideo.seekTo(1);
            }
        }
    }

    private void saveEvent() {
        String name = eventNameInput.getText().toString().trim();
        String description = eventDescriptionInput.getText().toString().trim();
        String date = dateText.getText().toString();
        String visibility = "";

        int selectedId = visibilityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.visible_all) {
            visibility = "all";
        } else if (selectedId == R.id.visible_selected) {
            visibility = "selected";
        }

        if (name.isEmpty() || description.isEmpty() || date.equals("No date selected")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", name);
        eventMap.put("description", description);
        eventMap.put("date", date);
        eventMap.put("visibility", visibility);
        eventMap.put("imageUri", imageUri);
        eventMap.put("videoUri", videoUri);

        db.collection("users")
                .document(userId)
                .collection("events")
                .add(eventMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
                });
    }
}