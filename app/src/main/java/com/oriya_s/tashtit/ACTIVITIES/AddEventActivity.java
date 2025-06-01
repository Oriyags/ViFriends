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
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

import java.util.*;

public class AddEventActivity extends AppCompatActivity {

    // Request code used for starting the Friend Selector activity
    // (A unique constant used to distinguish between different startActivityForResult)
    private static final int REQUEST_SELECT_FRIENDS = 2001;

    // UI elements for user input and actions
    private EditText   eventNameInput, eventDescriptionInput;
    private Button     pickDateButton, saveButton, pickVideoButton, pickLocationButton;
    private TextView   dateText, selectedLocationText;
    private RadioGroup visibilityGroup;
    private ImageView  selectedImage, selectedVideoThumb;

    // URIs to hold selected image and video files
    private Uri imageUri = null;
    private Uri videoUri = null;

    // URLs to be retrieved after uploading media to Firebase Storage
    private String imageDownloadUrl = "";
    private String videoDownloadUrl = "";

    // Variables to store location data
    private double selectedLat     = 0;
    private double selectedLng     = 0;
    private String selectedAddress = "";

    // Firestore database instance for saving and loading event data
    private FirebaseFirestore db;
    // Firebase Authentication instance to manage user login
    private FirebaseAuth      auth;
    // Firebase Storage instance for uploading images/videos
    private FirebaseStorage   storage;
    // Currently logged-in user
    private FirebaseUser      currentUser;

    // Launchers to handle results from image and video pickers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    // Event visibility and selected friends list
    private String       selectedVisibility = null;
    private List<String> selectedFriendUIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialize Firebase services
        auth        = FirebaseAuth.getInstance();
        db          = FirebaseFirestore.getInstance();
        storage     = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();

        // Bind UI elements
        eventNameInput        = findViewById(R.id.event_name);
        eventDescriptionInput = findViewById(R.id.event_description);
        pickDateButton        = findViewById(R.id.pick_date_button);
        dateText              = findViewById(R.id.date_text);
        visibilityGroup       = findViewById(R.id.visibility_group);
        saveButton            = findViewById(R.id.save_event_button);
        selectedImage         = findViewById(R.id.selected_image);
        pickVideoButton       = findViewById(R.id.pick_video_button);
        selectedVideoThumb    = findViewById(R.id.selected_video_thumb);
        pickLocationButton    = findViewById(R.id.pick_location_button);
        selectedLocationText  = findViewById(R.id.selected_location_text);

        initializePickers();
        setListeners();
    }

    // Initializes image and video picker launchers
    private void initializePickers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();  // Get image URI
                        Glide.with(this).load(imageUri).into(selectedImage);  // Load into ImageView
                    }
                });

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();  // Get video URI
                        try {
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
                                    videoUri.getPath(),
                                    MediaStore.Images.Thumbnails.MINI_KIND
                            );
                            selectedVideoThumb.setImageBitmap(thumb);  // Set video thumbnail
                            selectedVideoThumb.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // Attach listeners for all interactive UI elements
    private void setListeners() {
        pickDateButton.setOnClickListener(v -> showDatePicker());
        selectedImage.setOnClickListener(v -> pickImageFromGallery());
        pickVideoButton.setOnClickListener(v -> pickVideoFromGallery());
        pickLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, 1001);
        });

        // Handle radio button changes for visibility
        visibilityGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.visible_selected) {
                showFriendSelectionDialog();
            }
        });

        // Save event logic when user clicks save
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false); // Disable to prevent double submission

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

    // Opens a date picker dialog and sets selected date
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            dateText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Launch gallery to pick an image
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Launch gallery to pick a video
    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }

    // Decide which media (if any) to upload before saving event
    private void uploadMediaAndSaveEvent() {
        if (imageUri != null) {
            uploadImage();
        } else if (videoUri != null) {
            uploadVideo();
        } else {
            saveEvent();
        }
    }

    // Upload selected image to Firebase Storage
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

    // Upload selected video to Firebase Storage
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

    // Final step: Save event details in Firestore
    private void saveEvent() {
        String name        = eventNameInput.getText().toString().trim();
        String description = eventDescriptionInput.getText().toString().trim();
        String date        = dateText.getText().toString();

        // Validate inputs
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

        // Fetch current user profile to get display name and avatar
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String creatorName = snapshot.getString("profile.username");
                    if (creatorName == null || creatorName.isEmpty()) {
                        creatorName = currentUser.getEmail();
                    }
                    String creatorAvatar = snapshot.getString("profile.avatarUrl");

                    // Create map with event data
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

                    // Save event to Firestore under the current user's document
                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("events")
                            .add(eventMap)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity
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

    // Opens a screen to let user select which friends can view the event
    private void showFriendSelectionDialog() {
        Intent intent = new Intent(this, FriendSelectorActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_FRIENDS);
    }

    // Handle results from activities like friend selection and map picking
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

            // Update the UI with the selected location
            if (selectedLocationText != null) {
                if (selectedAddress != null && !selectedAddress.isEmpty()) {
                    selectedLocationText.setText(selectedAddress);
                } else {
                    selectedLocationText.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
                }
            } else {
                Toast.makeText(this, "Location selected but not displayed (UI error)", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SELECT_FRIENDS && resultCode == RESULT_OK && data != null) {
            selectedFriendUIDs = data.getStringArrayListExtra("selectedFriendUIDs");
            if (selectedFriendUIDs == null) selectedFriendUIDs = new ArrayList<>();
            Toast.makeText(this, selectedFriendUIDs.size() + " friend(s) selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Reset image and video URIs to release memory
    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageUri = null;
        videoUri = null;
    }
}