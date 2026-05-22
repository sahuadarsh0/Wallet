# ── CardVault R8 / ProGuard Rules ───────────────────────────────────────

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room ────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ── Hilt ────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── kotlinx.serialization ──────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.technitedminds.wallet.**$$serializer { *; }
-keepclassmembers class com.technitedminds.wallet.** {
    *** Companion;
}
-keepclasseswithmembers class com.technitedminds.wallet.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Google Tink ─────────────────────────────────────────────────────────
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ── ML Kit ──────────────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ── Coil ────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── CameraX ─────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }

# ── AndroidX Biometric ──────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }

# ── Compose ─────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# ── Domain models (used in type converters via reflection-like JSON) ───
-keep class com.technitedminds.wallet.domain.model.** { *; }
-keep class com.technitedminds.wallet.data.local.database.entities.** { *; }
-keep class com.technitedminds.wallet.data.local.database.converters.** { *; }

# ── Enums ───────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ──────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# ── DataStore ───────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── NFC / IsoDep ────────────────────────────────────────────────────────
-keep class com.technitedminds.wallet.data.nfc.** { *; }

# ── Strip verbose / debug logging in release ───────────────────────────
# Keeps Log.w and Log.e for genuine production failures, so user-reported
# crashes can still be diagnosed via ADB. Removes Log.d / Log.v / Log.i,
# which are wired up only for camera lifecycle tracing during development.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
