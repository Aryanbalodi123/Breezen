plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // For Kotlin 2.0+ Compose compiler integration
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.askquestion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.askquestion"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM - upgrade to at least 2024.05.00 (supports RuntimeShader APIs)
    implementation(platform(libs.androidx.compose.bom.v20240500))

    // Compose UI dependencies without hardcoded versions
    implementation(libs.ui)               // Compose UI core
    implementation(libs.ui.graphics)      // Needed for asComposeShader()
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3) // Material 3
    implementation(libs.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.json)

    // Modern Foundation Pager (1.7.8)
    implementation(libs.androidx.foundation)

    // Accompanist Pager (deprecated, keep if you still use it)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // Visual Effects libraries
    implementation(libs.haze)
    implementation(libs.blurview)
    implementation(libs.androidx.palette.ktx)
    implementation (libs.gson)


    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.gson)

    // Firebase Firestore
    implementation(libs.firebase.firestore)

    // JSON Serialization
    implementation(libs.kotlinx.serialization.json)

    // Animation Core
    implementation(libs.androidx.animation.core.android)
    implementation(libs.lottie.compose)

    // Play Services Base
    implementation(libs.play.services.base)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.compose.material3.material3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250800))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.retrofit.v290)
    implementation(libs.converter.gson.v290)
}
