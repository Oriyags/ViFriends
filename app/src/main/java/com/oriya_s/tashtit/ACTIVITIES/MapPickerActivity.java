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

    // Request code to identify which result is returned in onActivityResult
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Google Places SDK with your API key (if not already initialized)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Define the fields you want to retrieve from the selected place
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,    // Required to get coordinates
                Place.Field.ADDRESS     // Human-readable address
        );

        // Create an intent to launch the Google Places Autocomplete UI
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
        ).build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    // Handle the result returned by the Autocomplete activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ensure handling the correct request
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the selected place from the intent
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Make sure coordinates exist before continuing
                if (place.getLatLng() != null) {
                    // Create a new intent to send back the result
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", place.getLatLng().latitude);
                    resultIntent.putExtra("lng", place.getLatLng().longitude);
                    resultIntent.putExtra("address", place.getAddress());

                    // Return the location info back to the calling activity
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            } else {
                // User canceled or no place selected
                Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}