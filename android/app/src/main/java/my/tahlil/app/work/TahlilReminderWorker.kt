package my.tahlil.app.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import my.tahlil.app.R

/**
 * Periodic worker that reminds the user to read tahlil.
 *
 * Scheduled once a day via [BackgroundScheduler]. This is the
 * "background process" infrastructure — currently a no-op
 * notification, but it provides the foundation for:
 *  - Daily reading reminders
 *  - Syncing bookmark across devices (if backend added)
 *  - Pre-fetching content updates
 *
 * The worker survives app death and device restarts (WorkManager
 * persists scheduled work to disk) and respects system battery
 * optimizations — WorkManager will defer the work if the device
 * is in Doze mode.
 *
 * Notes on notification permission (Android 13+):
 *  - Posting a notification requires the `POST_NOTIFICATIONS`
 *    permission to be **granted** at runtime. We don't request
 *    it from this worker — the app should request it from the
 *    UI (e.g., when the user enables the reminder toggle).
 *    If permission isn't granted, `NotificationManager.notify`
 *    is a safe no-op on Android 13+.
 */
class TahlilReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            showReminderNotification()
            Result.success()
        } catch (e: Exception) {
            // Log but don't crash — WorkManager will retry per
            // the backoff policy configured on the WorkRequest.
            Result.retry()
        }
    }

    private fun showReminderNotification() {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Create channel (required API 26+). Calling this repeatedly is
        // safe — creating an existing channel is a no-op.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Tahlil",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Pengingat harian untuk membaca tahlil"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tahlil & Doa")
            .setContentText("Saatnya membaca tahlil hari ini")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "tahlil_reminder"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "tahlil_daily_reminder"
    }
}
