plugins {
    // Keep your existing plugins
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bantaybakuna" // Make sure this is your actual namespace
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bantaybakuna" // Make sure this is your actual ID
        minSdk = 23
        targetSdk = 35
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
        // Keep using Java 11 or higher if needed
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    // Your credentials/googleid lines (keep them)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // **** ADD THIS LINE FOR WORKMANAGER ****
    // Replace "2.9.0" with the latest stable version if needed
    // Check latest version here: https://developer.android.com/jetpack/androidx/releases/work
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("com.google.guava:guava:33.0.0-android")

    // **** END OF ADDED LINE ****

    // Test dependencies (keep them)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}