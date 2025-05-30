plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.oriya_s.tashtit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oriya_s.tashtit"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ Firebase dependencies
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1") // ✅ Added FCM support

    // ✅ Jitsi Meet SDK for Video Calls
    implementation("org.jitsi.react:jitsi-meet-sdk:8.1.2")

    // ✅ Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // ✅ UI components
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ✅ Google Maps & Places (only the full SDK, v2.7.0)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.places)

    // ✅ Internal modules
    implementation(project(":VIEWMODEL"))
    implementation(project(":MODEL"))
    implementation(project(":HELPER"))

    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// ✅ Required for Firebase services (must be last)
apply(plugin = "com.google.gms.google-services")