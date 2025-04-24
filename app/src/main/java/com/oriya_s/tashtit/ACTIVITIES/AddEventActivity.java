package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.oriya_s.tashtit.R;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDescriptionInput;
    private Button pickDateButton, chooseFriendsButton, saveButton;
    private TextView dateText;
    private RadioGroup visibilityGroup;
    private ImageView selectedImage;

    private String imageUri = "";

    private static final int IMAGE_PICK_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventNameInput = findViewById(R.id.event_name);
        eventDescriptionInput = findViewById(R.id.event_description);
        pickDateButton = findViewById(R.id.pick_date_button);
        dateText = findViewById(R.id.date_text);
        visibilityGroup = findViewById(R.id.visibility_group);
        chooseFriendsButton = findViewById(R.id.choose_friends_button);
        saveButton = findViewById(R.id.save_event_button);
        selectedImage = findViewById(R.id.selected_image);

        pickDateButton.setOnClickListener(v -> showDatePicker());
        selectedImage.setOnClickListener(v -> pickImageFromGallery());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri selected = data.getData();
            imageUri = selected.toString();
            selectedImage.setImageURI(selected);
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

        Intent resultIntent = new Intent();
        resultIntent.putExtra("event_name", name);
        resultIntent.putExtra("event_description", description);
        resultIntent.putExtra("event_date", date);
        resultIntent.putExtra("event_visibility", visibility);
        resultIntent.putExtra("event_image", imageUri);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}