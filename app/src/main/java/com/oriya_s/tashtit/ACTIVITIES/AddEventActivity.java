package com.oriya_s.tashtit.ACTIVITIES;

import android.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.model.Friend;
import com.oriya_s.tashtit.R;

import java.util.*;

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
    private FirebaseUser currentUser;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    private List<Friend> allFriends = new ArrayList<>();
    private List<String> selectedFriendIds = new ArrayList<>();

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
        chooseFriendsButton = findViewById(R.id.choose_friends_button);
        saveButton = findViewById(R.id.save_event_button);
        selectedImage = findViewById(R.id.selected_image);
        pickVideoButton = findViewById(R.id.pick_video_button);
        selectedVideoThumb = findViewById(R.id.selected_video_thumb);

        chooseFriendsButton.setEnabled(false);

        initializePickers();
        setListeners();
    }

    private void fetchFriends(Runnable onComplete) {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allFriends.clear();
                    List<Friend> tempFriends = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Friend f = doc.toObject(Friend.class);
                        tempFriends.add(f);
                    }

                    if (tempFriends.isEmpty()) {
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    for (Friend f : tempFriends) {
                        db.collection("users").document(f.getFriendID())
                                .get()
                                .addOnSuccessListener(profileDoc -> {
                                    if (profileDoc.exists() && profileDoc.contains("profile")) {
                                        Map<String, Object> profile = (Map<String, Object>) profileDoc.get("profile");
                                        if (profile != null) {
                                            f.setName((String) profile.get("username"));
                                            f.setAvatarUrl((String) profile.get("avatarUrl"));
                                        }
                                    }
                                    allFriends.add(f);
                                    if (allFriends.size() == tempFriends.size() && onComplete != null) {
                                        onComplete.run();
                                    }
                                });
                    }
                });
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

        chooseFriendsButton.setOnClickListener(v -> {
            if (allFriends.isEmpty()) {
                fetchFriends(this::showFriendSelectionDialog);
            } else {
                showFriendSelectionDialog();
            }
        });

        visibilityGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.visible_selected) {
                chooseFriendsButton.setEnabled(true);
                if (allFriends.isEmpty()) {
                    fetchFriends(this::showFriendSelectionDialog);
                } else {
                    showFriendSelectionDialog();
                }
            } else {
                chooseFriendsButton.setEnabled(false);
                selectedFriendIds.clear();
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

    private void showFriendSelectionDialog() {
        if (allFriends.isEmpty()) {
            Toast.makeText(this, "No friends to show", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[allFriends.size()];
        boolean[] checkedItems = new boolean[allFriends.size()];

        for (int i = 0; i < allFriends.size(); i++) {
            names[i] = allFriends.get(i).getName();
            checkedItems[i] = selectedFriendIds.contains(allFriends.get(i).getFriendID());
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Friends")
                .setMultiChoiceItems(names, checkedItems, (dialog, indexSelected, isChecked) -> {
                    String friendId = allFriends.get(indexSelected).getFriendID();
                    if (isChecked) {
                        if (!selectedFriendIds.contains(friendId)) {
                            selectedFriendIds.add(friendId);
                        }
                    } else {
                        selectedFriendIds.remove(friendId);
                    }
                })
                .setPositiveButton("OK", null)
                .show();
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

        final String visibility;
        int selectedId = visibilityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.visible_all) {
            visibility = "all";
        } else if (selectedId == R.id.visible_selected) {
            visibility = "selected";
        } else {
            Toast.makeText(this, "Please select visibility", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || description.isEmpty() || date.equals("No date selected")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    String creatorName = snapshot.getString("profile.username");
                    if (creatorName == null || creatorName.isEmpty()) {
                        creatorName = currentUser.getEmail();
                    }
                    String creatorAvatar = snapshot.getString("profile.avatarUrl");

                    List<String> visibleFriendIds = new ArrayList<>();
                    if ("all".equals(visibility)) {
                        for (Friend f : allFriends) {
                            visibleFriendIds.add(f.getFriendID());
                        }
                    } else if ("selected".equals(visibility)) {
                        visibleFriendIds.addAll(selectedFriendIds);
                    }

                    Map<String, Object> eventMap = new HashMap<>();
                    eventMap.put("name", name);
                    eventMap.put("description", description);
                    eventMap.put("date", date);
                    eventMap.put("visibility", visibility);
                    eventMap.put("imageUri", imageDownloadUrl);
                    eventMap.put("videoUri", videoDownloadUrl);
                    eventMap.put("creatorName", creatorName);
                    eventMap.put("creatorAvatar", creatorAvatar);
                    eventMap.put("creatorId", userId);
                    eventMap.put("visibleTo", visibleFriendIds);

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
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageUri = null;
        videoUri = null;
    }
}