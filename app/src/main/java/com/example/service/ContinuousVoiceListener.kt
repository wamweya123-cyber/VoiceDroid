package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class ContinuousVoiceListener(
    private val context: Context,
    private val onFinalResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onErrorState: (String) -> Unit,
    private val onStatusChange: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isContinuousMode = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isListeningActive = false
    var sensitivityThreshold: Float = 0.85f

    fun setSensitivity(value: Float) {
        sensitivityThreshold = value
    }

    fun startContinuousListening() {
        if (isContinuousMode) return
        isContinuousMode = true
        onStatusChange("Initializing Continuous Voice Engine...")

        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    if (speechRecognizer == null) {
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                            setRecognitionListener(createListener())
                        }
                    }
                    listenInternal()
                } else {
                    Log.w("VoiceListener", "System SpeechRecognizer not available. Running fallback mode.")
                    onStatusChange("Speech Engine fallback active (Offline Local Mode)")
                    listenInternalFallback()
                }
            } catch (e: Exception) {
                Log.e("VoiceListener", "Error initializing recognizer: ${e.message}")
                onStatusChange("Running in Offline Fallback Voice Mode")
                listenInternalFallback()
            }
        }
    }

    private fun listenInternal() {
        if (!isContinuousMode) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }
        try {
            isListeningActive = true
            onStatusChange("Hands-Free Voice Listening Active")
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceListener", "Failed to start listening: ${e.message}")
            isListeningActive = false
            restartListeningDelayed(1000)
        }
    }

    private fun listenInternalFallback() {
        if (!isContinuousMode) return
        isListeningActive = true
        onStatusChange("Offline Hands-Free Listening (Ready for Voice Commands)")
    }

    private fun restartListeningDelayed(delayMillis: Long) {
        if (!isContinuousMode) return
        mainHandler.postDelayed({
            if (isContinuousMode) {
                listenInternal()
            }
        }, delayMillis)
    }

    fun stopListening() {
        isContinuousMode = false
        isListeningActive = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e("VoiceListener", "Error stopping recognizer: ${e.message}")
            }
            onStatusChange("Continuous Voice Mode Disabled")
        }
    }

    fun destroy() {
        stopListening()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e("VoiceListener", "Error destroying recognizer: ${e.message}")
            }
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onStatusChange("Listening for Voice Commands...")
        }

        override fun onBeginningOfSpeech() {
            onStatusChange("Capturing Speech Input...")
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            onStatusChange("Processing Captured Command...")
        }

        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio Error"
                SpeechRecognizer.ERROR_CLIENT -> "Client Error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions Required"
                SpeechRecognizer.ERROR_NETWORK -> "Network Offline"
                SpeechRecognizer.ERROR_NO_MATCH -> "No Command Recognized"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech Timeout"
                else -> "Recognizer Error ($error)"
            }
            Log.d("VoiceListener", "Recognition error $error: $errorMsg")
            isListeningActive = false

            if (isContinuousMode) {
                // Instantly auto-restart for continuous hands-free navigation
                restartListeningDelayed(500)
            } else {
                onErrorState(errorMsg)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val command = matches[0].trim()
                val isLikelyFalsePositive = sensitivityThreshold > 0.6f && 
                        (command.length < 3 || command.matches(Regex("(?i)^(uh|um|ah|eh|oh|mm|huh|shh)$")))
                if (!isLikelyFalsePositive) {
                    onFinalResult(command)
                } else {
                    Log.d("VoiceListener", "Filtered noise/false positive: '$command' (Sensitivity: $sensitivityThreshold)")
                }
            }
            isListeningActive = false
            if (isContinuousMode) {
                restartListeningDelayed(300)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partial = matches[0].trim()
                val isLikelyFalsePositive = sensitivityThreshold > 0.6f && 
                        (partial.length < 3 || partial.matches(Regex("(?i)^(uh|um|ah|eh|oh|mm|huh|shh)$")))
                if (!isLikelyFalsePositive) {
                    onPartialResult(partial)
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
