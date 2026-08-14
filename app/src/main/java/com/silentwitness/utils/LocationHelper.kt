package com.silentwitness.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wraps the platform + Google Play location APIs used by SOS alerts and check-in reminders.
 * Returns null instead of throwing when permission is missing or a fix is unavailable.
 */
@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null
        val lastLocation = runCatching { fusedLocationClient.lastLocation.await() }.getOrNull()
        return lastLocation ?: getFreshLocation()
    }

    /**
     * Requests a fresh high-accuracy fix (not just the last known one). Suspends up to
     * [FRESH_LOCATION_TIMEOUT_MS], returning the best fix or null on timeout/permission failure.
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        var resumed = false
        fun finish(location: Location?) {
            if (!resumed) {
                resumed = true
                if (!continuation.isCancelled) continuation.resume(location)
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            FRESH_LOCATION_INTERVAL_MS
        )
            .setWaitForAccurateLocation(true)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                finish(result.lastLocation)
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    finish(null)
                }
            }
        }

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(callback)
        }
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            fusedLocationClient.removeLocationUpdates(callback)
            finish(null)
        }
        timeoutHandler.postDelayed(timeoutRunnable, FRESH_LOCATION_TIMEOUT_MS)

        runCatching {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        }.onFailure {
            timeoutHandler.removeCallbacks(timeoutRunnable)
            finish(null)
        }
    }

    fun formatLocation(location: Location): String =
        String.format(
            Locale.US,
            "%.5f, %.5f (accuracy ±%.0f m)",
            location.latitude,
            location.longitude,
            location.accuracy
        )

    fun reverseGeocode(location: Location): String {
        val address = runCatching {
            Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
        }.getOrNull()
        val line = address?.let {
            listOfNotNull(
                it.getAddressLine(0),
                it.locality,
                it.adminArea,
                it.countryName
            ).filter(String::isNotBlank).distinct().joinToString(", ")
        } ?: return formatLocation(location)
        return "$line (${formatLocation(location)})"
    }

    fun getLocationLink(location: Location): String =
        "https://maps.google.com/?q=${location.latitude},${location.longitude}"

    fun getDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun hasLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val FRESH_LOCATION_INTERVAL_MS = 5_000L
        const val FRESH_LOCATION_TIMEOUT_MS = 10_000L
    }
}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}
