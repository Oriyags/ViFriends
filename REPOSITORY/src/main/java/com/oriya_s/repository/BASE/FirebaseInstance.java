package com.oriya_s.repository.BASE;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseInstance {
    private static volatile FirebaseInstance _instance = null;
    public static FirebaseApp app;

    private FirebaseInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("vifriends-6e4f2") // from project_id
                .setApplicationId("1:549158463756:android:12f54c525bafd83e658129") // updated mobilesdk_app_id
                .setApiKey("AIzaSyCOTTgX3R9P5NLBxRcPRCVwOpzCk7lfhjc") // from api_key
                .setStorageBucket("vifriends-6e4f2.firebasestorage.app") // from storage_bucket
                .build();

        app = FirebaseApp.initializeApp(context, options);
    }

    public static FirebaseInstance instance(Context context) {
        if (_instance == null) {
            synchronized (FirebaseInstance.class) {
                if (_instance == null) {
                    _instance = new FirebaseInstance(context);
                }
            }
        }

        return _instance;
    }
}