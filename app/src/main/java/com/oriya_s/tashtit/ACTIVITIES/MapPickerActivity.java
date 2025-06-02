package com.oriya_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.oriya_s.tashtit.R;

import java.util.Arrays;
import java.util.List;

public class MapPickerActivity extends AppCompatActivity {

    // Request code to identify the autocomplete result
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Google Places SDK if it hasn't been initialized yet
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Specify the fields from the selected place (ID, Name, Coordinates, Address)
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        );

        // Build and launch the autocomplete activity in fullscreen mode
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);

        // Start the autocomplete activity for result
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    // Handle the result of the autocomplete place selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the autocomplete request
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the selected place from the returned data
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Ensure the location has coordinates
                if (place.getLatLng() != null) {
                    // Prepare an intent with the selected location details
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", place.getLatLng().latitude);
                    resultIntent.putExtra("lng", place.getLatLng().longitude);
                    resultIntent.putExtra("address", place.getAddress());

                    // Return the result to the calling activity
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            } else {
                Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}