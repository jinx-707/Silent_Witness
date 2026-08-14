package com.silentwitness.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fires [onShake] once per deliberate shake (threshold + debounce). [register] must be paired
 * with [unregister] to avoid leaking the sensor listener.
 */
@Singleton
class ShakeDetector @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var onShake: (() -> Unit)? = null
    private var lastShakeTime = 0L

    fun register(onShake: () -> Unit) {
        this.onShake = onShake
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregister() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        onShake = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val gx = event.values[0] / SensorManager.GRAVITY_EARTH
        val gy = event.values[1] / SensorManager.GRAVITY_EARTH
        val gz = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = Math.sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
        if (gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (lastShakeTime == 0L || now - lastShakeTime > SHAKE_DEBOUNCE_MS) {
                lastShakeTime = now
                onShake?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val SHAKE_THRESHOLD = 2.7f
        const val SHAKE_DEBOUNCE_MS = 2000L
    }
}
