package com.silentwitness.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object DownloadHelper {
    private val client = OkHttpClient()

    @Throws(IOException::class)
    suspend fun downloadFile(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to download: ${response.code}")
                response.body?.bytes() ?: throw IOException("Empty response")
            }
        }
    }
}
