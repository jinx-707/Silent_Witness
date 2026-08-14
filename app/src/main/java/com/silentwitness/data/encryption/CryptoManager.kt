package com.silentwitness.data.encryption

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Client-side zero-knowledge encryption for anything uploaded to the backend.
 *
 * A random 256-bit master key is generated once and sealed in the Android Keystore via
 * [EncryptedSharedPreferences]. Every record gets its own HKDF-SHA256 key derived from the
 * master key + a per-record salt, then encrypted with AES-256-GCM. A plaintext SHA-256 hash is
 * stored alongside the ciphertext so tampering with the DB row is detectable on decrypt.
 */
@Singleton
class CryptoManager @Inject constructor(@ApplicationContext private val context: Context) {

    data class EncryptedBlob(
        val ciphertextB64: String,
        val ivB64: String,
        val saltB64: String,
        val hash: String
    )

    private val prefs: android.content.SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "silent_witness_crypto",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val masterKey: ByteArray = run {
        val existing = prefs.getString(KEY_MASTER, null)
        if (existing != null) {
            Base64.decode(existing, Base64.NO_WRAP)
        } else {
            val bytes = ByteArray(KEY_BYTES).also { SecureRandom().nextBytes(it) }
            prefs.edit()
                .putString(KEY_MASTER, Base64.encodeToString(bytes, Base64.NO_WRAP))
                .apply()
            bytes
        }
    }

    fun encryptJson(json: String): EncryptedBlob {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = hkdf(masterKey, salt, KEY_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        return EncryptedBlob(
            ciphertextB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP),
            ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            hash = sha256(json)
        )
    }

    /**
     * Decrypts a blob. Throws [IllegalArgumentException] if the integrity hash does not match
     * the recovered plaintext (tamper detection).
     */
    fun decryptJson(blob: EncryptedBlob): String {
        val salt = Base64.decode(blob.saltB64, Base64.NO_WRAP)
        val iv = Base64.decode(blob.ivB64, Base64.NO_WRAP)
        val ciphertext = Base64.decode(blob.ciphertextB64, Base64.NO_WRAP)
        val key = hkdf(masterKey, salt, KEY_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        val plaintext = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        check(blob.hash == sha256(plaintext)) { "Integrity check failed for encrypted record" }
        return plaintext
    }

    /**
     * Encrypts arbitrary bytes (used for the SOS evidence export) with a fresh salt + IV, derived
     * from the same master key as [encryptJson]. Returns salt || iv || ciphertext concatenated so
     * the payload is self-describing — reversible via [decryptBytes].
     */
    fun encryptBytes(data: ByteArray): ByteArray {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = hkdf(masterKey, salt, KEY_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(data)
        return salt + iv + ciphertext
    }

    /**
     * Inverse of [encryptBytes]. GCM authenticates the ciphertext, so a corrupted payload throws
     * rather than returning garbage.
     */
    fun decryptBytes(encrypted: ByteArray): ByteArray {
        require(encrypted.size > SALT_BYTES + GCM_IV_LENGTH) { "Malformed encrypted payload" }
        val salt = encrypted.copyOfRange(0, SALT_BYTES)
        val iv = encrypted.copyOfRange(SALT_BYTES, SALT_BYTES + GCM_IV_LENGTH)
        val ciphertext = encrypted.copyOfRange(SALT_BYTES + GCM_IV_LENGTH, encrypted.size)
        val key = hkdf(masterKey, salt, KEY_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    /**
     * Persists the app PIN (encrypted at rest by EncryptedSharedPreferences). Uses [commit] so
     * the write survives process death even if it happens right after the user sets the PIN.
     */
    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).commit()
    }

    fun loadPin(): String? = prefs.getString(KEY_PIN, null)

    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_MASTER = "master_key_v1"
        const val KEY_PIN = "app_pin_v1"
        const val KEY_BYTES = 32
        const val SALT_BYTES = 16
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_BITS = 128
        const val HMAC_SHA256 = "HmacSHA256"

        private fun hkdf(ikm: ByteArray, salt: ByteArray, length: Int): ByteArray {
            val extract = Mac.getInstance(HMAC_SHA256)
            extract.init(SecretKeySpec(salt, HMAC_SHA256))
            val prk = extract.doFinal(ikm)
            val expand = Mac.getInstance(HMAC_SHA256)
            expand.init(SecretKeySpec(prk, HMAC_SHA256))
            var t = ByteArray(0)
            val out = ByteArray(length)
            var block = 0
            var offset = 0
            while (offset < length) {
                block++
                t = expand.doFinal(t + byteArrayOf(block.toByte()))
                val copy = minOf(t.size, length - offset)
                System.arraycopy(t, 0, out, offset, copy)
                offset += copy
            }
            return out
        }
    }
}
