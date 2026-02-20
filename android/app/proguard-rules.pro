# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.selfhosttinker.timestable.**$$serializer { *; }
-keepclassmembers class com.selfhosttinker.timestable.** {
    *** Companion;
}
-keepclasseswithmembers class com.selfhosttinker.timestable.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities
-keep class com.selfhosttinker.timestable.data.db.entity.** { *; }

# Hilt
-dontwarn com.google.errorprone.annotations.**
