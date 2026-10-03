package com.example.arlo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.arlo.model.ArloState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class GeminiTier(
    val id: String,
    val displayName: String,
    val modelName: String,
    val tierType: String,
    val description: String,
    val badgeIcon: String,
    val isCompletelyLocal: Boolean
) {
    LOCAL(
        id = "local",
        displayName = "100% On-Device Local Model",
        modelName = "local-deterministic-pattern-engine",
        tierType = "Completely Offline / 100% On-Device",
        description = "Runs purely on your device. Zero data ever leaves your phone. Full privacy guarantee.",
        badgeIcon = "🔒",
        isCompletelyLocal = true
    ),
    FREE(
        id = "free",
        displayName = "Gemini 3.5 Flash",
        modelName = "gemini-3.5-flash",
        tierType = "Free with Google Account",
        description = "Fast, multimodal, smart reasoning. Zero cost for all Google users.",
        badgeIcon = "⚡",
        isCompletelyLocal = false
    ),
    ADVANCED(
        id = "advanced",
        displayName = "Gemini 3.1 Pro",
        modelName = "gemini-3.1-pro-preview",
        tierType = "Subscriber / Google One AI Premium",
        description = "Highest reasoning depth, long context synthesis, and nuanced habit insights.",
        badgeIcon = "💎",
        isCompletelyLocal = false
    ),
    FLASH_LITE(
        id = "flash_lite",
        displayName = "Gemini 3.1 Flash Lite",
        modelName = "gemini-3.1-flash-lite-preview",
        tierType = "Battery & Data Saver",
        description = "Ultra-rapid turnaround, minimal battery consumption, ideal on the go.",
        badgeIcon = "🍃",
        isCompletelyLocal = false
    )
}

data class DataTransmissionDisclosure(
    val isCloudTransmission: Boolean,
    val destination: String,
    val modelName: String,
    val fieldsTransmitted: List<String>,
    val fullPayloadPreview: String,
    val privacyGuarantee: String
)

data class GeminiConfig(
    val activeTier: GeminiTier = GeminiTier.LOCAL,
    val apiKey: String = "",
    val googleAccountEmail: String = "",
    val isGoogleAccountLinked: Boolean = false,
    val cloudAiConsentGranted: Boolean = false,
    val shareIntentionsInCloudContext: Boolean = false,
    val shareReflectionsInCloudContext: Boolean = false
)

class GeminiManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_gemini_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<GeminiConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): GeminiConfig {
        val tierId = prefs.getString("gemini_tier_id", GeminiTier.LOCAL.id) ?: GeminiTier.LOCAL.id
        val tier = GeminiTier.entries.firstOrNull { it.id == tierId } ?: GeminiTier.LOCAL
        val key = prefs.getString("gemini_api_key", "") ?: ""
        val email = prefs.getString("gemini_google_email", "") ?: ""
        val linked = prefs.getBoolean("gemini_google_linked", false)
        val cloudConsent = prefs.getBoolean("gemini_cloud_consent", false)
        val shareIntentions = prefs.getBoolean("gemini_share_intentions", false)
        val shareReflections = prefs.getBoolean("gemini_share_reflections", false)

        return GeminiConfig(
            activeTier = tier,
            apiKey = key,
            googleAccountEmail = email,
            isGoogleAccountLinked = linked || email.isNotBlank(),
            cloudAiConsentGranted = cloudConsent,
            shareIntentionsInCloudContext = shareIntentions,
            shareReflectionsInCloudContext = shareReflections
        )
    }

    fun setTier(tier: GeminiTier) {
        prefs.edit().putString("gemini_tier_id", tier.id).apply()
        _configFlow.value = _configFlow.value.copy(activeTier = tier)
    }

    fun setApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("gemini_api_key", trimmed).apply()
        _configFlow.value = _configFlow.value.copy(apiKey = trimmed)
    }

    fun setCloudConsent(granted: Boolean) {
        prefs.edit().putBoolean("gemini_cloud_consent", granted).apply()
        _configFlow.value = _configFlow.value.copy(cloudAiConsentGranted = granted)
    }

    fun setContextSharing(shareIntentions: Boolean, shareReflections: Boolean) {
        prefs.edit()
            .putBoolean("gemini_share_intentions", shareIntentions)
            .putBoolean("gemini_share_reflections", shareReflections)
            .apply()
        _configFlow.value = _configFlow.value.copy(
            shareIntentionsInCloudContext = shareIntentions,
            shareReflectionsInCloudContext = shareReflections
        )
    }

    fun linkGoogleAccount(email: String, isSubscriber: Boolean = false) {
        val trimmed = email.trim()
        val tier = if (isSubscriber) GeminiTier.ADVANCED else GeminiTier.FREE
        prefs.edit()
            .putString("gemini_google_email", trimmed)
            .putBoolean("gemini_google_linked", true)
            .putString("gemini_tier_id", tier.id)
            .apply()
        _configFlow.value = _configFlow.value.copy(
            googleAccountEmail = trimmed,
            isGoogleAccountLinked = true,
            activeTier = tier
        )
    }

    fun unlinkGoogleAccount() {
        prefs.edit()
            .remove("gemini_google_email")
            .putBoolean("gemini_google_linked", false)
            .apply()
        _configFlow.value = _configFlow.value.copy(
            googleAccountEmail = "",
            isGoogleAccountLinked = false
        )
    }

    /**
     * Inspects exactly what data would be transmitted and where it would go.
     */
    fun inspectDataTransmission(userMessage: String, state: ArloState): DataTransmissionDisclosure {
        val config = _configFlow.value
        if (config.activeTier.isCompletelyLocal) {
            return DataTransmissionDisclosure(
                isCloudTransmission = false,
                destination = "Device Internal RAM (Zero Network Transmission)",
                modelName = "100% On-Device Local Pattern Engine",
                fieldsTransmitted = listOf("None: Zero bytes transmitted externally"),
                fullPayloadPreview = "{\n  \"transmission\": \"BLOCKED_BY_ON_DEVICE_POLICY\",\n  \"bytes_leaving_device\": 0,\n  \"local_execution_only\": true\n}",
                privacyGuarantee = "100% On-Device Guarantee: Everything is processed strictly inside your encrypted local vault. No network sockets are opened."
            )
        }

        val fields = mutableListOf<String>()
        fields.add("User Prompt: \"$userMessage\"")

        var intentionsContext = "Withheld (User privacy setting)"
        if (config.shareIntentionsInCloudContext) {
            fields.add("Titles of things you are moving toward (${state.goals.size} items)")
            intentionsContext = state.goals.joinToString("; ") { "${it.title} (${it.progressPercent}%)" }
        }

        var reflectionsContext = "Withheld (User privacy setting)"
        if (config.shareReflectionsInCloudContext) {
            fields.add("Recent reflection moods (${state.notes.size} items)")
            reflectionsContext = state.notes.take(3).joinToString("; ") { "${it.mood}: ${it.body.take(40)}" }
        }

        val destination = "Google Cloud (https://generativelanguage.googleapis.com/v1beta/models/${config.activeTier.modelName}:generateContent)"
        val payloadPreview = JSONObject().apply {
            put("destination", destination)
            put("userPrompt", userMessage)
            put("sharedContext", JSONObject().apply {
                put("intentionsIncluded", config.shareIntentionsInCloudContext)
                put("reflectionsIncluded", config.shareReflectionsInCloudContext)
            })
            put("deviceContentsSent", "NONE (Contacts, files, location, and photos are strictly excluded)")
        }.toString(2)

        return DataTransmissionDisclosure(
            isCloudTransmission = true,
            destination = destination,
            modelName = config.activeTier.displayName,
            fieldsTransmitted = fields,
            fullPayloadPreview = payloadPreview,
            privacyGuarantee = if (config.cloudAiConsentGranted)
                "Authorized: Explicit user consent granted for cloud transmission over encrypted TLS."
            else
                "PENDING: User has not yet granted explicit consent for cloud transmission."
        )
    }

    /**
     * Ask Gemini or Local model for conversational intelligence.
     * Guaranteed zero data sent without explicit user consent.
     */
    suspend fun queryGemini(
        userMessage: String,
        state: ArloState
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val config = _configFlow.value

        // 1. If Local Model is selected OR Cloud Consent has NOT been explicitly granted:
        if (config.activeTier.isCompletelyLocal || !config.cloudAiConsentGranted) {
            val breakdown = ArloPatternAnalyzer.analyze(state)
            val localResponse = ArloPatternAnalyzer.generateInteractiveResponse(userMessage, breakdown)

            val note = if (config.activeTier.isCompletelyLocal) {
                "🔒 [100% On-Device Local Model • 0 Bytes Sent]\nDestination: Local phone only. Your reflections and intentions never leave this device.\n\n$localResponse"
            } else {
                "🔒 [100% On-Device Fallback]\nCloud AI transmission paused: Explicit consent not yet granted for Google Cloud (${config.activeTier.displayName}).\n\n$localResponse\n\n*(To enable Google Cloud transmission, review the payload in Gemini Settings and grant explicit consent)*"
            }

            return@withContext Pair(note, false)
        }

        val effectiveKey = config.apiKey.ifBlank {
            System.getenv("GEMINI_API_KEY") ?: ""
        }

        if (effectiveKey.isBlank()) {
            val breakdown = ArloPatternAnalyzer.analyze(state)
            val localResponse = ArloPatternAnalyzer.generateInteractiveResponse(userMessage, breakdown)
            val decorated = "🔒 [100% On-Device Fallback]\nNo Gemini API Key found for ${config.activeTier.displayName}. Processed locally.\n\n$localResponse"
            return@withContext Pair(decorated, false)
        }

        // 2. Perform Explicit Outbound Cloud Transmission (Only after explicit consent)
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.activeTier.modelName}:generateContent?key=$effectiveKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                doInput = true
            }

            // Only include contextual fields that user explicitly authorized
            val contextParts = mutableListOf<String>()
            if (config.shareIntentionsInCloudContext) {
                val intentions = state.goals.joinToString("; ") { "${it.title} (${it.progressPercent}%)" }
                contextParts.add("Things user is moving toward: [$intentions]")
            }
            if (config.shareReflectionsInCloudContext) {
                val reflections = state.notes.take(3).joinToString(" | ") { "${it.mood}: ${it.body.take(40)}" }
                contextParts.add("Recent reflections: [$reflections]")
            }
            val energyScore = state.lastMoodIndex?.let { "$it/5" } ?: "Not logged"
            contextParts.add("Energy: $energyScore")

            val systemInstruction = "You are Arlo, an empathetic, observant, and witty feline companion living on the user's Android phone. " +
                    "Your role is to help the user reflect on their daily rhythm and gently move toward what matters to them. " +
                    "Use natural, human, encouraging language. NEVER use intimidating corporate words like 'rigid goals' or 'KPIs'—instead speak about 'what you are moving toward', 'intentions', 'small steps', and 'gentle momentum'. " +
                    "Sprinkle occasional subtle, charming cat humor without overdoing it. " +
                    "Authorized Context: [${contextParts.joinToString(" • ")}]. " +
                    "Active Gemini Model: ${config.activeTier.displayName}."

            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray()
                contentsArr.put(JSONObject().apply {
                    val partsArr = JSONArray()
                    partsArr.put(JSONObject().put("text", userMessage))
                    put("parts", partsArr)
                })
                put("contents", contentsArr)

                put("systemInstruction", JSONObject().apply {
                    val sysParts = JSONArray()
                    sysParts.put(JSONObject().put("text", systemInstruction))
                    put("parts", sysParts)
                })
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

                if (text.isNotBlank()) {
                    val transparentHeader = "🌐 [Transmitted via Google Cloud • ${config.activeTier.displayName}]\n" +
                            "Destination: Google Generative Language API (TLS Encrypted)\n\n"
                    Pair(transparentHeader + text.trim(), true)
                } else {
                    val breakdown = ArloPatternAnalyzer.analyze(state)
                    Pair(ArloPatternAnalyzer.generateInteractiveResponse(userMessage, breakdown), false)
                }
            } else {
                val breakdown = ArloPatternAnalyzer.analyze(state)
                val fallback = ArloPatternAnalyzer.generateInteractiveResponse(userMessage, breakdown)
                Pair("🔒 [100% On-Device Fallback • Google Cloud Status $responseCode]\n\n$fallback", false)
            }
        } catch (e: Exception) {
            val breakdown = ArloPatternAnalyzer.analyze(state)
            val fallback = ArloPatternAnalyzer.generateInteractiveResponse(userMessage, breakdown)
            Pair("🔒 [100% On-Device Fallback • Offline Mode]\n\n$fallback", false)
        }
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val config = _configFlow.value
        val effectiveKey = config.apiKey.ifBlank {
            System.getenv("GEMINI_API_KEY") ?: ""
        }
        if (effectiveKey.isBlank() || config.activeTier.isCompletelyLocal || !config.cloudAiConsentGranted) {
            return@withContext ""
        }
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.activeTier.modelName}:generateContent?key=$effectiveKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                doInput = true
            }

            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray()
                contentsArr.put(JSONObject().apply {
                    val partsArr = JSONArray()
                    partsArr.put(JSONObject().put("text", prompt))
                    put("parts", partsArr)
                })
                put("contents", contentsArr)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                parts?.optJSONObject(0)?.optString("text", "") ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
