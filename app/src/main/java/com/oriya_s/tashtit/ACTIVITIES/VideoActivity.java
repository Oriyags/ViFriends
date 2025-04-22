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

    protected void initializeViews() {
        startVideoCall = findViewById(R.id.btn_start_call);

        startVideoCall.setOnClickListener(v -> startVideo());
    }

    private void startVideo() {
        try {
            URL serverURL = new URL("https://meet.jit.si");

            // Fixed Room Name - For All Devices
            String roomName = "TashtitRoomMainVideo123456";

            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(roomName)
                    .setFeatureFlag("welcomepage.enabled", false) // No welcome page
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            JitsiMeetActivity.launch(this, options);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected void setViewModel() {
        // If needed in the future
    }
}