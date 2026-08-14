package com.silentwitness.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import com.silentwitness.utils.ShakeDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class SosTriggerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shakeDetector: ShakeDetector,
    private val sosService: SosService
) : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isMonitoring = false

    private val _shakeEnabled = MutableStateFlow(prefs.getBoolean(KEY_SHAKE_ENABLED, true))
    val shakeEnabled: StateFlow<Boolean> = _shakeEnabled.asStateFlow()

    private val _voiceEnabled = MutableStateFlow(prefs.getBoolean(KEY_VOICE_ENABLED, false))
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()

    private val keywords = listOf("help", "sos", "save me", "emergency", "help me")

    fun isShakeEnabled(): Boolean = prefs.getBoolean(KEY_SHAKE_ENABLED, true)
    fun isVoiceEnabled(): Boolean = prefs.getBoolean(KEY_VOICE_ENABLED, false)

    fun setShakeEnabled(enable: Boolean) {
        _shakeEnabled.value = enable
        prefs.edit().putBoolean(KEY_SHAKE_ENABLED, enable).apply()
        if (enable) {
            startShakeDetection()
        } else {
            stopShakeDetection()
        }
    }

    fun setVoiceEnabled(enable: Boolean) {
        _voiceEnabled.value = enable
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enable).apply()
        if (enable) {
            startVoiceDetection()
        } else {
            stopVoiceDetection()
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        _shakeEnabled.value = prefs.getBoolean(KEY_SHAKE_ENABLED, true)
        _voiceEnabled.value = prefs.getBoolean(KEY_VOICE_ENABLED, false)
        if (_shakeEnabled.value) {
            startShakeDetection()
        }
        if (_voiceEnabled.value) {
            startVoiceDetection()
        }
    }

    private fun startShakeDetection() {
        shakeDetector.register { triggerSos() }
    }

    private fun stopShakeDetection() {
        shakeDetector.unregister()
    }

    fun startVoiceDetection() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "startVoiceDetection skipped: RECORD_AUDIO not granted")
            return
        }
        if (speechRecognizer == null) {
            Log.d(TAG, "Creating SpeechRecognizer")
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            if (speechRecognizer == null) {
                Log.w(TAG, "SpeechRecognizer is null (recognition service unavailable)")
                return
            }
            speechRecognizer?.setRecognitionListener(voiceListener)
        }
        isListening = true
        startListening()
    }

    fun stopVoiceDetection() {
        Log.d(TAG, "stopVoiceDetection")
        isListening = false
        speechRecognizer?.stopListening()
    }

    private fun startListening() {
        if (!isListening || speechRecognizer == null) {
            Log.d(
                TAG,
                "startListening skipped: isListening=$isListening, recognizer=${speechRecognizer != null}"
            )
            return
        }
        Log.d(TAG, "startListening called")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private val voiceListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            Log.e(TAG, "SpeechRecognizer error: $error (${errorCode(error)})")
            launch {
                delay(1000)
                if (!isListening) return@launch
                if (error == SpeechRecognizer.ERROR_CLIENT ||
                    error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                ) {
                    recreateRecognizer()
                }
                startListening()
            }
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "onResults: $matches")
            matches?.forEach { text ->
                val lower = text.lowercase()
                if (keywords.any { lower.contains(it) }) {
                    Log.i(TAG, "Keyword matched in \"$text\" -> triggering SOS")
                    triggerSos()
                }
            }
            launch {
                delay(500)
                if (isListening) startListening()
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun recreateRecognizer() {
        Log.d(TAG, "Recreating SpeechRecognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
        if (!_voiceEnabled.value) return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(voiceListener)
    }

    private fun triggerSos() {
        Log.i(TAG, "triggerSos fired")
        launch { sosService.triggerSos() }
    }

    fun stopMonitoring() {
        Log.d(TAG, "stopMonitoring")
        isListening = false
        isMonitoring = false
        stopShakeDetection()
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun errorCode(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
        SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
        SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
        SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
        SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
        else -> "UNKNOWN"
    }

    private companion object {
        const val TAG = "SosTriggerManager"
        const val PREFS_NAME = "sos_prefs"
        const val KEY_SHAKE_ENABLED = "shake_enabled"
        const val KEY_VOICE_ENABLED = "voice_enabled"
    }
}
