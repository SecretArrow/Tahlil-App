// Top-level build file where you can add configuration options common to all sub-projects/modules.
//
// Toolchain bumped to match Tahlil-Singkat (the working sister app):
//   Kotlin 1.9.22 -> 2.0.21
//   AGP     8.2.2 -> 8.7.3
//   + new integrated `org.jetbrains.kotlin.plugin.compose` (replaces the
//     legacy `composeOptions.kotlinCompilerExtensionVersion = "1.5.8"`).
//
// Why: Compose UI 1.6.x (BOM 2024.02.00) ships an `androidx.compose.ui.modifier.
// ModifierLocalProvider` interface whose default-method `all(...)` collides
// with another `ModifierLocalProvider` impl when R8 (in any mode) merges
// interfaces horizontally — producing
//   java.lang.IncompatibleClassChangeError: Conflicting default method
//   implementations boolean androidx.compose.ui.modifier.ModifierLocalProvider.all(...)
// on the FIRST composed frame of every minified release build. Compose UI 1.7.x
// (BOM 2024.09.03) refactored that interface, eliminating the collision.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
