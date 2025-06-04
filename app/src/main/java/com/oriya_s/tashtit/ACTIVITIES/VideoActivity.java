package com.oriya_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oriya_s.tashtit.R;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoActivity extends AppCompatActivity {

    Button startVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        setViewModel();
    }

    // Binds the start button and sets its click behavior
    protected void initializeViews() {
        startVideoCall = findViewById(R.id.btn_start_call);

        // When the button is clicked, start the Jitsi video call
        startVideoCall.setOnClickListener(v -> startVideo());
    }

    // Starts a video call using Jitsi Meet
    private void startVideo() {
        try {
            // Define the Jitsi server URL
            URL serverURL = new URL("https://meet.jit.si");

            // Set a fixed room name so all users join the same video room
            String roomName = "TashtitRoomMainVideo123456";

            // Configure the video meeting options
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(roomName)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            // Launch the Jitsi Meet activity with these options
            JitsiMeetActivity.launch(this, options);

        } catch (MalformedURLException e) {
            // Catch and print error if server URL is malformed
            e.printStackTrace();
        }
    }

    // Placeholder for future ViewModel setup (currently unused)
    protected void setViewModel() {}
}