import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tahlildoa.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tahlildoa.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 20
        versionName = "1.2.8"

        // Ship only the locales the app actually uses (Indonesian + English
        // fallback + Arabic). AndroidX/Material bundles ~80 locales that would
        // otherwise bloat every APK (~200-400 KB of unused strings.xml tables).
        resourceConfigurations.addAll(listOf("id", "en", "ar"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // --- Signing configuration (CI/CD-friendly) --------------------------
    // Reads keystore from env vars if present (CI); falls back to debug
    // signing for local builds.
    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("KEYSTORE_PATH")
            if (storeFilePath != null && File(storeFilePath).exists()) {
                storeFile = File(storeFilePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    // --- ABI splits for smaller per-architecture APKs --------------------
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    buildTypes {
        release {
            // R8 minification ON. The historical release-only crash was rooted
            // in installSplashScreen() being called with a non-splash theme
            // (before super.onCreate, outside any try/catch) — it has been
            // removed. The surgical keep rules in proguard-rules.pro cover
            // everything R8 cannot infer (serializers, workers, enums,
            // ViewModels). The CI pipeline installs THIS exact APK on real
            // emulators (API 29 + API 34) and fails the run if it force-closes,
            // so a broken minified build can never reach a Release again.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if available, else fall back to debug.
            signingConfig = signingConfigs
                .findByName("release")
                ?.takeIf { it.storeFile != null }
                ?: signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Compose compiler is now provided by the `org.jetbrains.kotlin.plugin.compose`
    // Gradle plugin (Kotlin 2.0+ integrated compiler) — the legacy
    // `composeOptions.kotlinCompilerExtensionVersion` is no longer used.

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            // Dead weight that never runs on Android:
            excludes += "META-INF/*.version"
            excludes += "DebugProbesKt.bin"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // AndroidX core — bumped to match Tahlil-Singkat exactly.
    // The previous versions (core-ktx 1.12.0, lifecycle 2.7.0, activity-compose 1.8.2,
    // navigation-compose 2.7.7) were all COMPILED AGAINST COMPOSE 1.6.x. Even with
    // our Compose BOM bumped to 2024.09.03, these libraries shipped bytecode that
    // still references the pre-1.7 `androidx.compose.ui.modifier.ModifierLocalProvider`
    // interface signature — and at runtime the JVM sees two conflicting default-method
    // implementations of `ModifierLocalProvider.all(...)` (one from Compose UI 1.7.x,
    // one residual from the 1.6.x-compiled transitive deps) and throws:
    //   IncompatibleClassChangeError: Conflicting default method implementations
    //   boolean androidx.compose.ui.modifier.ModifierLocalProvider.all(...)
    // on the first composed frame. The fix is to use libraries that were themselves
    // compiled against Compose 1.7.x.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Jetpack Compose (BOM-managed) — bumped to 2024.09.03 to ship Compose UI 1.7.x,
    // which fixes the `ModifierLocalProvider.all(...)` IncompatibleClassChangeError
    // that crashed every minified release build on Compose UI 1.6.x.
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    // NOTE: `androidx.compose.animation:animation` is intentionally NOT declared.
    // Singkat doesn't declare it either — it comes in transitively via material3
    // -> foundation -> animation. An explicit declaration caused R8 to retain more
    // symbols from animation, increasing the surface for the ModifierLocalProvider
    // IncompatibleClassChangeError. Let it come in transitively.

    // Navigation — 2.8.x is the first Navigation Compose release compiled
    // against Compose UI 1.7.x. Earlier 2.7.x pulls Compose 1.6.x-compiled
    // bytecode into the APK and re-introduces the ModifierLocalProvider ICE.
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlinx Serialization (compatible with Kotlin 2.0.21)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Splashscreen API

    // WorkManager — for background tasks (daily reading reminder)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Debugging tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
