package com.oriya_s.tashtit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Called automatically when a new FCM message is received (foreground or background)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        // Handle data payload (custom data from Firebase Cloud Function)
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            Log.d("FCM", "Data message received: " + title + " - " + body);
        }

        // If data payload is empty, fallback to notification payload
        if ((title == null || body == null) && remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d("FCM", "Notification message received: " + title + " - " + body);
        }

        // Do nothing if there's no usable content
        if (title == null || body == null) return;

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chat_channel",                     // Channel ID
                    "Chat Notifications",                  // Channel Name
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Build the notification content
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_notification)  // App's notification icon
                .setContentTitle(title)                    // Notification title
                .setContentText(body)                      // Notification body
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        // Android 13+ requires runtime permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("FCM", "Notification permission not granted.");
                return;
            }
        }

        // Show the notification with ID 1001
        managerCompat.notify(1001, builder.build());
    }

    // Called when a new FCM token is generated (e.g., app reinstall or token refresh)
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Get the currently logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Update the user's document with the new FCM token
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(unused ->
                            Log.d("FCM", "FCM token updated for user: " + user.getUid()))
                    .addOnFailureListener(e ->
                            Log.e("FCM", "Failed to update FCM token: ", e));
        } else {
            // Log it for debugging – user may not be signed in yet
            Log.d("FCM", "Token generated but user not signed in: " + token);
        }
    }
}