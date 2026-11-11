# ============================================================
# ProGuard / R8 rules for Tahlil & Doa — MINIMAL, Singkat-style
# ------------------------------------------------------------
# History: this file once carried aggressive keeps for
# @Stable/@Immutable/@Composable classes, ViewModels, enums and
# utils. Those rules PIN Compose classes so R8 cannot merge them
# freely — and the half-pinned class graph produced:
#   IncompatibleClassChangeError: Conflicting default method
#   implementations ModifierLocalProvider.all(...)
# on the first composed frame of every minified release build
# (confirmed across 6 CI runs). The working sister app
# Tahlil-Singkat ships with only TWO rules and its minified APK
# runs fine. This file now matches that philosophy: keep only
# what genuinely needs keeping (serialization + workers), let R8
# do everything else.
# ============================================================

# === Attributes (stack traces + runtime annotations) ===
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable

# === kotlinx.serialization (official recommended rules) ===
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class my.tahlil.app.**$$serializer { *; }
-keepclassmembers class my.tahlil.app.** {
    *** Companion;
}
-keepclasseswithmembers class my.tahlil.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class my.tahlil.app.** { *; }

# === WorkManager ===
# Workers are instantiated via reflection by WorkManager's factory.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# === General Android ===
-dontwarn android.support.**
