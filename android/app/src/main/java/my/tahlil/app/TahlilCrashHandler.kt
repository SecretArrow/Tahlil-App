package my.tahlil.app

import android.util.Log

/**
 * Global uncaught exception handler.
 *
 * Wraps the system default handler to:
 *  1. Log the full stack trace with a recognizable tag so the user can
 *     share it from `adb logcat` or a crash-reporting tool.
 *  2. Write the stack trace to a file in the app's cache directory so
 *     it survives the crash and can be inspected on next launch.
 *
 * This does NOT swallow exceptions — after logging, it delegates to the
 * system default handler which shows the standard "app crashed" dialog.
 * The purpose is purely diagnostic: if the app crashes again, we'll have
 * a record of WHY.
 *
 * The CI release-verification step greps logcat for the [TAG]
 * ("TahlilCrash") when a release APK fails to launch, so any future
 * force-close produces an actionable stack trace in the job log.
 */
class TahlilCrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val cacheDir: java.io.File,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val stackTrace = Log.getStackTraceString(throwable)

        // Log with a distinctive tag
        Log.e(TAG, "══════════════════════════════════════════════")
        Log.e(TAG, "APP CRASHED — thread: ${thread.name}")
        Log.e(TAG, "Exception: ${throwable.javaClass.name}: ${throwable.message}")
        Log.e(TAG, stackTrace)
        Log.e(TAG, "══════════════════════════════════════════════")

        // Persist to cache for inspection on next launch
        try {
            val logFile = java.io.File(cacheDir, "last_crash.txt")
            logFile.writeText(
                buildString {
                    appendLine("Timestamp: ${System.currentTimeMillis()}")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Exception: ${throwable.javaClass.name}: ${throwable.message}")
                    appendLine()
                    appendLine(stackTrace)
                }
            )
            // Keep only the last 5 crash logs
            val crashFiles = cacheDir.listFiles { f -> f.name.startsWith("crash_") }
                ?.sortedBy { it.lastModified() } ?: emptyList()
            if (crashFiles.size > 5) {
                crashFiles.take(crashFiles.size - 5).forEach { it.delete() }
            }
        } catch (e: Exception) {
            // Swallow — we're already crashing, don't crash the crash handler
        }

        // Delegate to the system default handler (shows the crash dialog)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        private const val TAG = "TahlilCrash"

        /** Install the handler. Call from Application.onCreate(). */
        fun install(cacheDir: java.io.File) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(
                TahlilCrashHandler(current, cacheDir)
            )
        }
    }
}
