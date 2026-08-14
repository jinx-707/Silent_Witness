package com.silentwitness.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules/cancels the periodic [CheckInWorker]. Gated stub — the real OneSignal push flow is
 * wired later.
 */
object CheckInScheduler {

    private const val WORK_NAME = "silent_witness_check_in"
    private const val MIN_INTERVAL_MINUTES = 15L

    fun schedule(context: Context, intervalHours: Int) {
        val minutes = maxOf(MIN_INTERVAL_MINUTES, intervalHours.toLong() * 60)
        val request = PeriodicWorkRequestBuilder<CheckInWorker>(minutes, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
