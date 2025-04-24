package com.oriya_s.tashtit.ACTIVITIES;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.oriya_s.tashtit.R;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDescriptionInput;
    private Button pickDateButton, chooseFriendsButton, saveButton;
    private TextView dateText;
    private RadioGroup visibilityGroup;

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

        pickDateButton.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            dateText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEvent() {
        String name = eventNameInput.getText().toString();
        String description = eventDescriptionInput.getText().toString();
        String date = dateText.getText().toString();

        int selectedId = visibilityGroup.getCheckedRadioButtonId();
        String visibility = (selectedId == R.id.visible_selected)
                ? "Selected Friends"
                : "All Friends";

        Intent resultIntent = new Intent();
        resultIntent.putExtra("event_name", name);
        resultIntent.putExtra("event_description", description);
        resultIntent.putExtra("event_date", date);
        resultIntent.putExtra("event_visibility", visibility);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}