package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private String viewedUserId;

    private ImageView profileImage;
    private EditText etBio;
    private Button btnSaveChanges;
    private TextView tvUsername, tvCity, tvPhone, tvDOB, tvAge;

    private Uri selectedImageUri;
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

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        viewedUserId = getIntent().getStringExtra("userId");
        if (viewedUserId == null || viewedUserId.isEmpty()) {
            viewedUserId = currentUser != null ? currentUser.getUid() : null;
        }

        initializeViews();
        loadUserData();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvCity = findViewById(R.id.tv_city);
        tvPhone = findViewById(R.id.tv_phone);
        tvDOB = findViewById(R.id.tv_dob);
        tvAge = findViewById(R.id.tv_age);
        etBio = findViewById(R.id.et_bio);
        btnSaveChanges = findViewById(R.id.btn_save_changes);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(this).load(selectedImageUri).into(profileImage);
                            uploadProfileImage(selectedImageUri);
                        } else {
                            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        profileImage.setOnClickListener(v -> openImagePicker());
        btnSaveChanges.setOnClickListener(v -> saveBioToFirestore());
    }

    private void loadUserData() {
        if (viewedUserId == null) return;

        db.collection("users")
                .document(viewedUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Object profileObj = document.get("profile");
                        if (profileObj instanceof Map) {
                            Map<String, Object> profile = (Map<String, Object>) profileObj;
                            String username = (String) profile.get("username");
                            String phone = (String) profile.get("phoneNumber");
                            String city = (String) profile.get("locationID");
                            String bio = (String) profile.get("bio");
                            String profileImageUrl = (String) profile.get("profileImageUrl");
                            long dobMillis = profile.get("dob") instanceof Number ? ((Number) profile.get("dob")).longValue() : 0;

                            tvUsername.setText(username != null ? username : "");
                            tvCity.setText(city != null ? city : "");
                            tvPhone.setText(phone != null ? phone : "");
                            etBio.setText(bio != null ? bio : "");
                            tvDOB.setText(dobMillis > 0 ? formatDate(dobMillis) : "Not set");
                            tvAge.setText(dobMillis > 0 ? calculateAge(dobMillis) + " years old" : "");

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).into(profileImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveBioToFirestore() {
        String updatedBio = etBio.getText().toString().trim();
        if (currentUser == null || !currentUser.getUid().equals(viewedUserId)) return;

        db.collection("users")
                .document(currentUser.getUid())
                .update("profile.bio", updatedBio)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Bio updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update bio", Toast.LENGTH_SHORT).show()
                );
    }

    private void openImagePicker() {
        if (currentUser == null || !currentUser.getUid().equals(viewedUserId)) return;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null || imageUri == null || !currentUser.getUid().equals(viewedUserId)) {
            Toast.makeText(this, "Image not selected or unauthorized.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference ref = storage.getReference("profile_images/" + currentUser.getUid() + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("users")
                            .document(currentUser.getUid())
                            .update("profile.profileImageUrl", uri.toString())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                            );
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private String formatDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return day + "/" + month + "/" + year;
    }

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