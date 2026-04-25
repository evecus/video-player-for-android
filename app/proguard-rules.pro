# VLC
-keep class org.videolan.** { *; }
-keep class org.videolan.libvlc.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Coil
-keep class coil.** { *; }

# Data classes used with Gson / serialization
-keepclassmembers class com.yinghua.player.data.model.** { *; }

# Suppress warnings
-dontwarn java.lang.invoke.*
-dontwarn javax.annotation.**
