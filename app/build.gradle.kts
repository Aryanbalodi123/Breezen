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
    // ============ CORE ANDROID & JETPACK ============
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ============ COMPOSE BOM (Dependency Management) ============
    implementation(platform(libs.androidx.compose.bom.v20240500))
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))

    // ============ JETPACK COMPOSE UI ============
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.ui:ui-graphics:1.6.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")

    // ============ MATERIAL DESIGN 3 ============
    implementation(libs.material3)
    implementation("androidx.compose.material3:material3:1.5.0-alpha04")

    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.4")

    // ============ GRAPHICS SHAPES (FOR COOKIE SHAPE) ============

    // ============ NAVIGATION ============
    implementation(libs.navigation.compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ============ FOUNDATION & LAYOUT ============
    implementation(libs.androidx.foundation)
    implementation("androidx.compose.foundation:foundation:1.6.4")

    // ============ LIFECYCLE & VIEWMODEL ============
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ============ ANIMATION ============
    implementation(libs.androidx.animation.core.android)
    implementation("androidx.compose.animation:animation:1.6.4")
    implementation("androidx.compose.animation:animation-core:1.6.4")

    // ============ LOTTIE (Optional - for animations) ============
    implementation(libs.lottie.compose)
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // ============ VISUAL EFFECTS ============
    implementation(libs.haze)
    implementation("dev.chrisbanes.haze:haze:0.4.0")
    implementation(libs.blurview)
    implementation("androidx.palette:palette-ktx:1.0.0")

    // ============ IMAGE LOADING & MEDIA ============
    implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")

    // ============ NETWORKING & SERIALIZATION ============
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ============ JSON SERIALIZATION ============
    implementation(libs.json)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ============ COROUTINES ============
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ============ FIREBASE ============
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation(libs.androidx.compose.material3.material32)
    implementation(libs.androidx.compose.foundation.foundation)

    // ============ TESTING ============
    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250800))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")

    // ============ DEBUG TOOLING ============
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")

    // ============ ACCOMPANIST (Pager - for page navigation) ============
    implementation(libs.accompanist.pager)
    implementation("com.google.accompanist:accompanist-pager:0.33.2-alpha")
    implementation(libs.accompanist.pager.indicators)
    implementation("com.google.accompanist:accompanist-pager-indicators:0.33.2-alpha")
}