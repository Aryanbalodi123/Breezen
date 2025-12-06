###############################################
##  BREEZEN – COMPLETE PROGUARD CONFIG (FINAL)
###############################################

########## KEEP JNI KEYS ##########
-keep class com.example.breezen.core.network.Keys { *; }
-dontwarn com.example.breezen.core.network.Keys

# Keep native method signatures
-keepclassmembers class * {
    native <methods>;
}

###############################################
## COMPOSE (required or release crashes)
###############################################
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class kotlin.coroutines.** { *; }

###############################################
## VIEWMODELS & LIFECYCLE
###############################################
-keep class androidx.lifecycle.** { *; }
-keep class com.example.breezen.**ViewModel { *; }
-keepclassmembers class com.example.breezen.**ViewModel { *; }

###############################################
## RETROFIT + OKHTTP
###############################################
# Retrofit
-dontwarn retrofit2.Platform$Java8
-dontwarn javax.annotation.**
-keep class retrofit2.** { *; }

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okhttp3.internal.publicsuffix.PublicSuffixDatabase

###############################################
## GSON – Keep data classes for JSON parsing
###############################################
-keep class com.google.gson.** { *; }
-keep class com.example.breezen.** { *; }

# Keep SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

###############################################
## COROUTINES
###############################################
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

###############################################
## KEEP ANNOTATIONS (Retrofit, Gson, Compose require)
###############################################
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

###############################################
## OTHER COMMON SAFETY RULES
###############################################
# Prevent minification issues with reflection
-dontwarn java.lang.invoke.*
-keep class kotlin.Metadata { *; }
