# MediaPipe Tasks uses reflection/native entry points.
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# Room generated implementations are discovered by name.
-keep class * extends androidx.room.RoomDatabase
