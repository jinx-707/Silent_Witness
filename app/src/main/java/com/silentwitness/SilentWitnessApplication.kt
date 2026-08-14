package com.silentwitness

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.onesignal.OneSignal
import com.silentwitness.services.SosTriggerManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SilentWitnessApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var sosTriggerManager: SosTriggerManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // OneSignal is gated: only initialised when an App ID is configured in local.properties.
        if (BuildConfig.ONESIGNAL_APP_ID.isNotBlank()) {
            runCatching {
                OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
            }
        }
        // Global SOS monitoring: shake (and voice, if enabled) work on every screen,
        // including the disguise/calculator layer before unlock.
        sosTriggerManager.startMonitoring()
    }
}
