package my.tahlil.app.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules background work for the app.
 *
 * Currently schedules:
 *  - Daily tahlil reading reminder (notification)
 *
 * All work is done via WorkManager which:
 *  - Survives app death and device restarts (work is persisted to disk)
 *  - Respects system battery optimizations (Doze, App Standby)
 *  - Works without a foreground service
 *  - Persists across app updates
 *
 * The scheduler is idempotent — calling [scheduleDailyReminder] multiple
 * times uses `ExistingPeriodicWorkPolicy.KEEP` so the original schedule
 * is preserved (the next firing time stays the same).
 */
object BackgroundScheduler {

    /**
     * Schedule the daily tahlil reminder. Safe to call from
     * `Application.onCreate()` — uses `KEEP` policy so subsequent
     * invocations don't replace the existing schedule.
     */
    fun scheduleDailyReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<TahlilReminderWorker>(
            1, TimeUnit.DAYS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TahlilReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /**
     * Cancel the daily reminder. Called when the user disables
     * the reminder toggle (currently unused — the toggle isn't
     * wired in this iteration, but the infrastructure is in place).
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(TahlilReminderWorker.WORK_NAME)
    }
}
