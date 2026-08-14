package com.silentwitness.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.onesignal.OneSignal
import com.silentwitness.BuildConfig
import com.silentwitness.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signs the device into Supabase with an anonymous user the first time a repository needs to
 * reach the backend, then caches the user id in EncryptedSharedPreferences. Anonymous users are
 * device-specific: clearing app data creates a new identity (accepted limitation).
 */
@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val client: SupabaseClient,
    private val context: Context
) : AuthRepository {

    private val prefs: android.content.SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "silent_witness_auth",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun ensureSignedIn(): String {
        cachedUserId()?.let { return it }
        runCatching {
            if (client.auth.loadFromStorage()) {
                client.auth.currentUserOrNull()?.id?.let { uid ->
                    saveUserId(uid)
                    loginPush(uid)
                    return uid
                }
            }
        }
        client.auth.signInAnonymously()
        val uid = client.auth.currentUserOrNull()?.id ?: error("Anonymous sign-in failed")
        saveUserId(uid)
        loginPush(uid)
        return uid
    }

    override suspend fun getCurrentUserId(): String? = cachedUserId()

    private fun loginPush(uid: String) {
        if (BuildConfig.ONESIGNAL_APP_ID.isBlank()) return
        runCatching { OneSignal.login(uid) }
    }

    private fun cachedUserId(): String? = prefs.getString(KEY_USER_ID, null)

    private fun saveUserId(uid: String) {
        prefs.edit().putString(KEY_USER_ID, uid).apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id_v1"
    }
}
