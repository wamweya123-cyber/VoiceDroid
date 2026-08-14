package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun parseVoiceCommand(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun analyzeVoiceCommand(spokenText: String, preferOffline: Boolean = false): String = withContext(Dispatchers.IO) {
        if (preferOffline) {
            return@withContext fallbackParse(spokenText)
        }
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext fallbackParse(spokenText)
        }

        val systemPrompt = """
            You are the Voice Remote Control Engine for an Android phone.
            Analyze the user's voice command and extract structured JSON output with:
            - "action": One of ["OPEN_APP", "CHANGE_SETTING", "SYSTEM_NAVIGATION", "ACCESSIBILITY_READ", "SECURITY_LOCK", "LAUNCH_CAMERA", "MEDIA_CONTROL", "CUSTOM_MACRO", "NAVIGATE_TAB"]
            - "targetApp": package name or human readable app name (e.g. "YouTube", "Settings", "Camera", "Console", "Apps", "Macros", "Security", "Settings")
            - "parameter": value or detail (e.g. "Volume 30%", "Wi-Fi ON", "Scroll Down", "Brightness 80%", "Tab: CONSOLE")
            - "securityLevel": "NORMAL" or "HIGH_SECURITY" (e.g. Banking, System Admin, Password changes require HIGH_SECURITY)
            - "ttsFeedback": Brief friendly voice feedback string to speak back to the user hands-free.

            Respond strictly with valid JSON only.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = "Voice command: \"$spokenText\"")))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        try {
            val response = api.parseVoiceCommand(key, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                resultText
            } else {
                fallbackParse(spokenText)
            }
        } catch (e: Exception) {
            fallbackParse(spokenText)
        }
    }

    fun fallbackParse(text: String): String {
        val lower = text.lowercase()
        val isHighSec = lower.contains("bank") || lower.contains("pay") || lower.contains("password") || lower.contains("reset")
        val secLevel = if (isHighSec) "HIGH_SECURITY" else "NORMAL"

        return when {
            lower.contains("tab console") || lower.contains("open console") || lower.contains("go to console") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Console","parameter":"CONSOLE","securityLevel":"NORMAL","ttsFeedback":"Navigating hands-free to Console tab."}"""
            }
            lower.contains("tab apps") || lower.contains("open apps") || lower.contains("go to apps") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Apps Control","parameter":"APPS_CONTROL","securityLevel":"NORMAL","ttsFeedback":"Navigating to Apps Control."}"""
            }
            lower.contains("tab macros") || lower.contains("open macros") || lower.contains("go to macros") || lower.contains("routines") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Voice Macros","parameter":"MACROS","securityLevel":"NORMAL","ttsFeedback":"Navigating to Voice Macros."}"""
            }
            lower.contains("tab security") || lower.contains("open security") || lower.contains("go to security") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Security Shield","parameter":"SECURITY_SHIELD","securityLevel":"NORMAL","ttsFeedback":"Navigating to Security Shield."}"""
            }
            lower.contains("tab settings") || lower.contains("open settings tab") || lower.contains("go to settings") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Voice Settings","parameter":"SETTINGS","securityLevel":"NORMAL","ttsFeedback":"Navigating to Voice Settings."}"""
            }
            lower.contains("next tab") || lower.contains("next screen") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Navigation","parameter":"NEXT_TAB","securityLevel":"NORMAL","ttsFeedback":"Switched to next screen."}"""
            }
            lower.contains("previous tab") || lower.contains("go back") -> {
                """{"action":"NAVIGATE_TAB","targetApp":"Navigation","parameter":"PREV_TAB","securityLevel":"NORMAL","ttsFeedback":"Returned to previous screen."}"""
            }
            lower.contains("scroll down") || lower.contains("page down") -> {
                """{"action":"SYSTEM_NAVIGATION","targetApp":"System Screen","parameter":"Scroll Down","securityLevel":"NORMAL","ttsFeedback":"Scrolling page down."}"""
            }
            lower.contains("scroll up") || lower.contains("page up") -> {
                """{"action":"SYSTEM_NAVIGATION","targetApp":"System Screen","parameter":"Scroll Up","securityLevel":"NORMAL","ttsFeedback":"Scrolling page up."}"""
            }
            lower.contains("open") || lower.contains("launch") -> {
                val app = text.replace(Regex("(?i)open|launch"), "").trim().ifEmpty { "Settings" }
                """{"action":"OPEN_APP","targetApp":"$app","parameter":"Foreground","securityLevel":"$secLevel","ttsFeedback":"Opening $app now."}"""
            }
            lower.contains("volume") || lower.contains("mute") || lower.contains("unmute") -> {
                """{"action":"CHANGE_SETTING","targetApp":"System Audio","parameter":"Volume Adjust","securityLevel":"NORMAL","ttsFeedback":"Adjusted system volume hands-free."}"""
            }
            lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("bluetooth") -> {
                """{"action":"CHANGE_SETTING","targetApp":"Connectivity","parameter":"Toggle Switch","securityLevel":"NORMAL","ttsFeedback":"Toggled connection settings."}"""
            }
            lower.contains("read") || lower.contains("screen") -> {
                """{"action":"ACCESSIBILITY_READ","targetApp":"Screen Reader","parameter":"Full Screen","securityLevel":"NORMAL","ttsFeedback":"Reading screen contents aloud."}"""
            }
            lower.contains("lock") || lower.contains("secure") -> {
                """{"action":"SECURITY_LOCK","targetApp":"System Lock","parameter":"Lock Screen","securityLevel":"HIGH_SECURITY","ttsFeedback":"Securing and locking phone immediately."}"""
            }
            else -> {
                """{"action":"OPEN_APP","targetApp":"Voice Assistant","parameter":"Parsed: $text","securityLevel":"$secLevel","ttsFeedback":"Executing offline command for $text."}"""
            }
        }
    }
}
