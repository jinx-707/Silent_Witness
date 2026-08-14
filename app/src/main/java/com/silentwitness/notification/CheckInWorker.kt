package com.silentwitness.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.silentwitness.R
import com.silentwitness.domain.repository.CheckInRepository
import com.silentwitness.services.SosService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that fires a local "you're overdue" notification once the last confirmed
 * check-in is older than the configured [CheckInSettings.intervalHours]. If the user enabled
 * "release on missed", a grace period is observed and an SOS is then sent automatically.
 */
@HiltWorker
class CheckInWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val checkInRepository: CheckInRepository,
    private val sosService: SosService
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = checkInRepository.getCheckInSettings().first()
            if (!settings.enabled) return Result.success()

            val lastCheckIn = getLastCheckInTime()
            val hoursSince = (System.currentTimeMillis() - lastCheckIn) / (1000L * 60 * 60)

            if (hoursSince >= settings.intervalHours) {
                notifyContactsAboutMissedCheckIn()

                if (settings.releaseOnMissed) {
                    val graceMs = TimeUnit.MINUTES.toMillis(
                        minOf(settings.gracePeriodMinutes, MAX_GRACE_MINUTES).toLong()
                    )
                    delay(graceMs)

                    // Give the user one last chance to check in before the SOS goes out.
                    val lastCheckInAfterGrace = getLastCheckInTime()
                    if (lastCheckInAfterGrace <= lastCheckIn) {
                        sosService.triggerSos()
                    }
                }
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    private fun getLastCheckInTime(): Long =
        applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_CHECK_IN, System.currentTimeMillis())

    private fun notifyContactsAboutMissedCheckIn() {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(nm)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Silent Witness check-in")
            .setContentText("Your regular check-in is overdue. Open the app to confirm you are safe.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Safety check-ins",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val PREFS = "check_in_prefs"
        private const val KEY_LAST_CHECK_IN = "last_check_in_epoch_ms"
        private const val MAX_GRACE_MINUTES = 10
        private const val CHANNEL_ID = "silent_witness_check_in"
        private const val NOTIFICATION_ID = 9001

        fun markCheckedIn(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_CHECK_IN, Instant.now().toEpochMilli())
                .apply()
        }
    }
}
