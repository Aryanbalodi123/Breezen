import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.breezen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.breezen"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    // Load keystore secrets from local.properties
    val keystorePropertiesFile = rootProject.file("local.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            // Ensure breezen-release-key.jks is inside the 'app' folder
            storeFile = file("breezen-release-key.jks")
            storePassword = keystoreProperties["storePassword"] as String? ?: ""
            keyAlias = "breezen"
            keyPassword = keystoreProperties["keyPassword"] as String? ?: ""
        }
    }

    buildTypes {
        debug {
            // MAGIC IS HERE: Use the Release Key for Debugging
            // This ensures the SHA-256 matches your C++ check while you dev.
            signingConfig = signingConfigs.getByName("release")

            isDebuggable = true
            externalNativeBuild {
                cmake {
                    cppFlags("-DDEBUG=1")
                }
            }
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            externalNativeBuild {
                cmake {
                    cppFlags("-DDEBUG=0")
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // BOMs
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Core & Jetpack
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.5.0-alpha04")
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Coroutines & Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Supabase-kt
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-android:3.0.1")

    // Firebase
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Visuals & Media
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.airbnb.android:lottie-compose:6.5.2")

    // Blur library
    implementation(libs.haze)

    // Media3
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-common:1.5.0")

    // Networking (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.places)
    implementation(libs.androidx.compose.ui.text)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}