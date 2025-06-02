package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oriya_s.tashtit.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseStorage   storage;
    private FirebaseUser      currentUser;
    private String            viewedUserId;

    // UI components
    private ImageView profileImage;
    private EditText  etUsername, etBio, etCity, etPhone, etDOB;
    private TextView  tvAge;
    private Button    btnSaveChanges;
    private Uri       selectedImageUri;
    private long      dobMillis = 0;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db          = FirebaseFirestore.getInstance();
        storage     = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get the user ID to display — defaults to current user if not provided
        viewedUserId = getIntent().getStringExtra("userId");
        if (viewedUserId == null || viewedUserId.isEmpty()) {
            viewedUserId = currentUser != null ? currentUser.getUid() : null;
        }

        // Setup views and listeners
        initializeViews();
        loadUserData();
    }

    // Binds views and sets click listeners
    private void initializeViews() {
        profileImage   = findViewById(R.id.profile_image);
        etUsername     = findViewById(R.id.et_username);
        etCity         = findViewById(R.id.tv_city);
        etPhone        = findViewById(R.id.tv_phone);
        etDOB          = findViewById(R.id.tv_dob);
        tvAge          = findViewById(R.id.tv_age);
        etBio          = findViewById(R.id.et_bio);
        btnSaveChanges = findViewById(R.id.btn_save_changes);

        // Sets up launcher to handle image selection result
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(this).load(selectedImageUri).into(profileImage);
                            uploadProfileImage(selectedImageUri); // Uploads image to Firebase
                        }
                    }
                });

        etDOB.setOnClickListener(v -> showDatePicker());
        profileImage.setOnClickListener(v -> openImagePicker());
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    // Loads user data from Firestore and populates the fields
    private void loadUserData() {
        if (viewedUserId == null) return;

        db.collection("users")
                .document(viewedUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Map<String, Object> profile = (Map<String, Object>) document.get("profile");
                        if (profile == null) return;

                        String username = (String) profile.get("username");
                        String phone = (String) profile.get("phoneNumber");
                        String city = (String) profile.get("locationID");
                        String bio = (String) profile.get("bio");
                        String profileImageUrl = (String) profile.get("profileImageUrl");
                        dobMillis = profile.get("dob") instanceof Number ? ((Number) profile.get("dob")).longValue() : 0;

                        etUsername.setText(username != null ? username : "");
                        etCity.setText(city != null ? city : "");
                        etPhone.setText(phone != null ? phone : "");
                        etBio.setText(bio != null ? bio : "");
                        etDOB.setText(dobMillis > 0 ? formatDate(dobMillis) : "Not set");
                        tvAge.setText(dobMillis > 0 ? calculateAge(dobMillis) + " years old" : "");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImage);
                        }

                        // If viewing someone else's profile — disable editing
                        if (!viewedUserId.equals(currentUser.getUid())) {
                            disableEditing();
                        }
                    }
                });
    }

    // Disables all editing features when not viewing own profile
    private void disableEditing() {
        etUsername.setEnabled(false);
        etBio.setEnabled(false);
        etCity.setEnabled(false);
        etPhone.setEnabled(false);
        etDOB.setEnabled(false);
        profileImage.setClickable(false);
        btnSaveChanges.setVisibility(View.GONE);
    }

    // Opens a date picker dialog to set date of birth
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            dobMillis = calendar.getTimeInMillis();
            etDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            tvAge.setText(calculateAge(dobMillis) + " years old");
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Saves all changed fields to Firestore
    private void saveProfileChanges() {
        if (currentUser == null || !currentUser.getUid().equals(viewedUserId)) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("profile.username", etUsername.getText().toString().trim());
        updates.put("profile.bio", etBio.getText().toString().trim());
        updates.put("profile.locationID", etCity.getText().toString().trim());
        updates.put("profile.phoneNumber", etPhone.getText().toString().trim());
        updates.put("profile.dob", dobMillis);

        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Opens image picker for profile picture
    private void openImagePicker() {
        if (currentUser == null || !currentUser.getUid().equals(viewedUserId)) return;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Uploads selected profile image to Firebase Storage
    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null || imageUri == null || !currentUser.getUid().equals(viewedUserId)) return;

        StorageReference ref = storage.getReference("profile_images/" + currentUser.getUid() + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("users")
                            .document(currentUser.getUid())
                            .update("profile.profileImageUrl", uri.toString())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show());
                }));
    }

    // Formats a date in dd/MM/yyyy from milliseconds
    private String formatDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);
    }

    // Calculates age in years from date of birth
    private int calculateAge(long dobMillis) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(dobMillis);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}