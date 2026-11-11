package my.tahlil.app

import android.app.Application
import android.util.Log
import my.tahlil.app.data.SettingsRepository
import my.tahlil.app.work.BackgroundScheduler

/**
 * Application class — holds the singleton [SettingsRepository] so all
 * screens share the same DataStore instance, and schedules the daily
 * tahlil reading reminder.
 *
 * No Hilt/Dagger — kept simple per the project's "keep simple if too complex"
 * constraint. The repository is created lazily and exposed via the
 * application context.
 *
 * Launch-time work is ordered defensively so that no optional subsystem
 * can force-close the app:
 *
 *  1. [TahlilCrashHandler] is installed FIRST — before anything else can
 *     throw — so every subsequent crash is logged to logcat (tag
 *     `TahlilCrash`) and persisted to `cache/last_crash.txt` for
 *     inspection on the next launch.
 *  2. The daily reminder is scheduled via WorkManager's
 *     `enqueueUniquePeriodicWork` with `KEEP` policy (idempotent across
 *     launches) inside a try/catch — a WorkManager initialization failure
 *     is logged but never fatal. The reminder is a nice-to-have, not
 *     critical functionality.
 */
class TahlilDoaApp : Application() {

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Install the crash handler FIRST so any subsequent crash is logged.
        try {
            TahlilCrashHandler.install(cacheDir)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install crash handler", e)
        }

        // Schedule the daily reading reminder — idempotent (KEEP policy).
        try {
            BackgroundScheduler.scheduleDailyReminder(this)
        } catch (e: Exception) {
            // WorkManager init failure should NOT crash the app —
            // the reminder is a nice-to-have, not critical functionality.
            Log.e(TAG, "Failed to schedule reminder", e)
        }
    }

    companion object {
        private const val TAG = "TahlilDoaApp"

        @Volatile
        private var instance: TahlilDoaApp? = null

        fun getInstance(): TahlilDoaApp =
            instance ?: throw IllegalStateException("TahlilDoaApp not yet initialized")
    }
}
