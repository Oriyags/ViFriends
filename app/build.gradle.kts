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

    // ✅ Jitsi Meet SDK for Video Calls (Latest Stable Version)
    implementation("org.jitsi.react:jitsi-meet-sdk:8.1.2")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(project(":VIEWMODEL"))
    implementation(project(":MODEL"))
    implementation(project(":HELPER"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// ✅ Required for Firebase services (must be last)
apply(plugin = "com.google.gms.google-services")
