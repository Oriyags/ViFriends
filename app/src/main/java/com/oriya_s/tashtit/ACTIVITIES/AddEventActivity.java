package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDescriptionInput;
    private Button pickDateButton, chooseFriendsButton, saveButton, pickVideoButton;
    private TextView dateText;
    private RadioGroup visibilityGroup;
    private ImageView selectedImage, selectedVideoThumb;

    private Uri imageUri = null;
    private Uri videoUri = null;
    private String imageDownloadUrl = "";
    private String videoDownloadUrl = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        eventNameInput = findViewById(R.id.event_name);
        eventDescriptionInput = findViewById(R.id.event_description);
        pickDateButton = findViewById(R.id.pick_date_button);
        dateText = findViewById(R.id.date_text);
        visibilityGroup = findViewById(R.id.visibility_group);
        chooseFriendsButton = findViewById(R.id.choose_friends_button);
        saveButton = findViewById(R.id.save_event_button);
        selectedImage = findViewById(R.id.selected_image);
        pickVideoButton = findViewById(R.id.pick_video_button);
        selectedVideoThumb = findViewById(R.id.selected_video_thumb); // You need to add this in the layout

        initializePickers();
        setListeners();
    }

    private void initializePickers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(this).load(imageUri).into(selectedImage);
                    }
                });

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();
                        try {
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
                                    videoUri.getPath(),
                                    MediaStore.Images.Thumbnails.MINI_KIND
                            );
                            selectedVideoThumb.setImageBitmap(thumb);
                            selectedVideoThumb.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void setListeners() {
        pickDateButton.setOnClickListener(v -> showDatePicker());
        selectedImage.setOnClickListener(v -> pickImageFromGallery());
        pickVideoButton.setOnClickListener(v -> pickVideoFromGallery());
        saveButton.setOnClickListener(v -> uploadMediaAndSaveEvent());
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
        imagePickerLauncher.launch(intent);
    }

    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }

    private void uploadMediaAndSaveEvent() {
        saveButton.setEnabled(false);
        if (imageUri != null) {
            uploadImage();
        } else if (videoUri != null) {
            uploadVideo();
        } else {
            saveEvent();
        }
    }

    private void uploadImage() {
        StorageReference ref = storage.getReference("event_images/" + System.currentTimeMillis() + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageDownloadUrl = uri.toString();
                    if (videoUri != null) {
                        uploadVideo();
                    } else {
                        saveEvent();
                    }
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed uploading image", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private void uploadVideo() {
        StorageReference ref = storage.getReference("event_videos/" + System.currentTimeMillis() + ".mp4");
        ref.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    videoDownloadUrl = uri.toString();
                    saveEvent();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed uploading video", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
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
            saveButton.setEnabled(true);
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            return;
        }

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", name);
        eventMap.put("description", description);
        eventMap.put("date", date);
        eventMap.put("visibility", visibility);
        eventMap.put("imageUri", imageDownloadUrl);
        eventMap.put("videoUri", videoDownloadUrl);

        db.collection("users")
                .document(userId)
                .collection("events")
                .add(eventMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> saveButton.setEnabled(true));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageUri = null;
        videoUri = null;
    }
}