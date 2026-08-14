package com.silentwitness.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.ContactsRepository
import com.silentwitness.domain.repository.LogEntryRepository
import com.silentwitness.utils.DownloadHelper
import com.silentwitness.utils.LocationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot emergency responder. Builds a short SOS message from the last known (or freshly
 * requested) location and notifies every contact: phone numbers get an automatic SMS when
 * SEND_SMS is granted (falling back to the SMS intent), email addresses get an email intent.
 * As a best-effort bonus the journal is zipped, encrypted and pushed to Supabase Storage,
 * but the URL is deliberately not included in the alert message.
 */
@Singleton
class SosService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: SupabaseClient,
    private val logEntryRepository: LogEntryRepository,
    private val contactsRepository: ContactsRepository,
    private val authRepository: AuthRepository,
    private val cryptoManager: CryptoManager,
    private val locationHelper: LocationHelper,
    private val json: Json
) {

    suspend fun triggerSos() {
        val contacts = contactsRepository.getAllContacts().first()
        if (contacts.isEmpty()) {
            showToast("No contacts found to send SOS")
            return
        }

        val location = locationHelper.getFreshLocation() ?: locationHelper.getCurrentLocation()
        val locationLink = location?.let { locationHelper.getLocationLink(it) }
            ?: "Location unavailable (enable GPS and grant location permission)"

        val message = buildString {
            append("SILENT WITNESS SOS\n")
            append("I need help.\n")
            append("Please contact me immediately.\n")
            append("Location: $locationLink")
        }

        contacts.forEach { contact ->
            val method = contact.contactMethod.trim()
            when {
                method.matches(PHONE_REGEX) -> {
                    if (hasSmsPermission()) {
                        runCatching { sendSmsAutomatic(method, message) }
                            .onFailure { sendSmsViaIntent(method, message) }
                    } else {
                        sendSmsViaIntent(method, message)
                    }
                }
                method.contains('@') -> sendEmailViaIntent(method, "SOS from Silent Witness", message)
                else -> sendSmsViaIntent(method, message)
            }
        }

        val entries = logEntryRepository.getAllEntries().first()
        runCatching { packageAndUploadEvidence(entries) }

        showToast("SOS sent to ${contacts.size} contacts")
    }

    private fun showToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Zips the journal, encrypts the zip and uploads it to the public "exports" bucket. Returns
     * the public download URL so contacts can fetch the encrypted evidence later.
     */
    private suspend fun packageAndUploadEvidence(entries: List<LogEntry>): String {
        if (entries.isEmpty()) return "No entries to upload"
        return withContext(Dispatchers.IO) {
            val payload = json.encodeToString(entries.map { EvidenceEntry.from(it) })
            val zipped = zipBytes(payload.toByteArray(Charsets.UTF_8))
            val encrypted = cryptoManager.encryptBytes(zipped)
            val uid = authRepository.ensureSignedIn()
            val path = "exports/${uid}_${System.currentTimeMillis()}.zip.enc"
            client.storage.from(BUCKET).upload(path, encrypted)
            client.storage.from(BUCKET).publicUrl(path)
        }
    }

    /**
     * Downloads a previously uploaded encrypted export from its public URL, decrypts it and
     * unzips it. Returns null if the file is missing, corrupt or not decryptable.
     */
    suspend fun downloadEvidence(publicUrl: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val encrypted = DownloadHelper.downloadFile(publicUrl)
                val zipped = cryptoManager.decryptBytes(encrypted)
                unzipFirstEntry(zipped)
            }.getOrNull()
        }
    }

    private fun hasSmsPermission(): Boolean =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private fun sendSmsAutomatic(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(message)
        if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        }
    }

    private fun sendSmsViaIntent(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$phoneNumber"))
            .putExtra("sms_body", message)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(Intent.createChooser(intent, "Send SOS via SMS")) }
    }

    private fun sendEmailViaIntent(email: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Send SOS via Email")) }
    }

    private fun zipBytes(payload: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry("evidence.json"))
            zip.write(payload)
            zip.closeEntry()
        }
        return baos.toByteArray()
    }

    private fun unzipFirstEntry(zipBytes: ByteArray): ByteArray {
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) return zip.readBytes()
            }
        }
        return ByteArray(0)
    }

    @Serializable
    private data class EvidenceEntry(
        val date: String,
        val category: String,
        val description: String,
        val location: String?,
        val injuryNotes: String?
    ) {
        companion object {
            fun from(entry: LogEntry) = EvidenceEntry(
                date = entry.date.toString(),
                category = entry.category,
                description = entry.description,
                location = entry.location,
                injuryNotes = entry.injuryNotes
            )
        }
    }

    private companion object {
        const val BUCKET = "exports"
        val PHONE_REGEX = Regex("^[0-9+]+$")
    }
}
