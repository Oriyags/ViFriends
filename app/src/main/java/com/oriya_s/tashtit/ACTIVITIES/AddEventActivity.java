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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

import java.util.*;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDescriptionInput;
    private Button pickDateButton, saveButton, pickVideoButton, pickLocationButton;
    private TextView dateText, selectedLocationText;
    private RadioGroup visibilityGroup;
    private ImageView selectedImage, selectedVideoThumb;

    private Uri imageUri = null;
    private Uri videoUri = null;
    private String imageDownloadUrl = "";
    private String videoDownloadUrl = "";

    private double selectedLat = 0;
    private double selectedLng = 0;
    private String selectedAddress = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    private String selectedVisibility = null;
    private List<String> selectedFriendUIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();

        eventNameInput = findViewById(R.id.event_name);
        eventDescriptionInput = findViewById(R.id.event_description);
        pickDateButton = findViewById(R.id.pick_date_button);
        dateText = findViewById(R.id.date_text);
        visibilityGroup = findViewById(R.id.visibility_group);
        saveButton = findViewById(R.id.save_event_button);
        selectedImage = findViewById(R.id.selected_image);
        pickVideoButton = findViewById(R.id.pick_video_button);
        selectedVideoThumb = findViewById(R.id.selected_video_thumb);
        pickLocationButton = findViewById(R.id.pick_location_button);
        selectedLocationText = findViewById(R.id.selected_location_text);

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
        pickLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, 1001);
        });

        visibilityGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.visible_selected) {
                showFriendSelectionDialog();
            }
        });

        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);

            int selectedId = visibilityGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.visible_all) {
                selectedVisibility = "all";
                selectedFriendUIDs.clear();
                uploadMediaAndSaveEvent();
            } else if (selectedId == R.id.visible_selected) {
                selectedVisibility = "selected";
                if (selectedFriendUIDs.isEmpty()) {
                    Toast.makeText(this, "Select at least one friend", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    return;
                }
                uploadMediaAndSaveEvent();
            } else {
                Toast.makeText(this, "Please select visibility", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
            }
        });
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

        if (name.isEmpty() || description.isEmpty() || date.equals("No date selected")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String creatorName = snapshot.getString("profile.username");
                    if (creatorName == null || creatorName.isEmpty()) {
                        creatorName = currentUser.getEmail();
                    }
                    String creatorAvatar = snapshot.getString("profile.avatarUrl");

                    Map<String, Object> eventMap = new HashMap<>();
                    eventMap.put("name", name);
                    eventMap.put("description", description);
                    eventMap.put("date", date);
                    eventMap.put("visibility", selectedVisibility);
                    eventMap.put("imageUri", imageDownloadUrl);
                    eventMap.put("videoUri", videoDownloadUrl);
                    eventMap.put("creatorName", creatorName);
                    eventMap.put("creatorAvatar", creatorAvatar);
                    eventMap.put("creatorId", currentUser.getUid());
                    eventMap.put("latitude", selectedLat);
                    eventMap.put("longitude", selectedLng);
                    eventMap.put("address", selectedAddress);

                    if ("selected".equals(selectedVisibility)) {
                        eventMap.put("visibleTo", selectedFriendUIDs);
                    }

                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("events")
                            .add(eventMap)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private void showFriendSelectionDialog() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> friendIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        friendIds.add(doc.getId());
                    }

                    if (friendIds.isEmpty()) {
                        Toast.makeText(this, "No friends available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("users")
                            .whereIn(FieldPath.documentId(), friendIds)
                            .get()
                            .addOnSuccessListener(friendSnapshots -> {
                                List<String> friendNames = new ArrayList<>();
                                List<String> resolvedIds = new ArrayList<>();

                                for (DocumentSnapshot userDoc : friendSnapshots) {
                                    String id = userDoc.getId();
                                    Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                                    String name = profile != null ? (String) profile.get("username") : "Unknown";
                                    friendNames.add(name != null ? name : "Unnamed");
                                    resolvedIds.add(id);
                                }

                                showFriendSelectionDialogUI(friendNames, resolvedIds);
                            });
                });
    }

    private void showFriendSelectionDialogUI(List<String> friendNames, List<String> friendIds) {
        boolean[] checkedItems = new boolean[friendNames.size()];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Friends");

        builder.setMultiChoiceItems(
                friendNames.toArray(new String[0]),
                checkedItems,
                (dialog, which, isChecked) -> {
                    String friendId = friendIds.get(which);
                    if (isChecked) {
                        if (!selectedFriendUIDs.contains(friendId)) {
                            selectedFriendUIDs.add(friendId);
                        }
                    } else {
                        selectedFriendUIDs.remove(friendId);
                    }
                });

        builder.setPositiveButton("Done", null);
        AlertDialog dialog = builder.create();
        dialog.getListView().setVerticalScrollBarEnabled(true);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("lat") && data.hasExtra("lng")) {
                selectedLat = data.getDoubleExtra("lat", 0);
                selectedLng = data.getDoubleExtra("lng", 0);
            }

            if (data.hasExtra("address")) {
                selectedAddress = data.getStringExtra("address");
            }

            if (selectedLocationText != null) {
                if (selectedAddress != null && !selectedAddress.isEmpty()) {
                    selectedLocationText.setText(selectedAddress);
                } else {
                    selectedLocationText.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
                }
            } else {
                Toast.makeText(this, "Location selected but not displayed (UI error)", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageUri = null;
        videoUri = null;
    }
}