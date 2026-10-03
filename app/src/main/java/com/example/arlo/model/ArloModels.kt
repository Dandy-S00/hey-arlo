package com.example.arlo.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val detail: String = "",
    val done: Boolean = false
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("detail", detail)
        put("done", done)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): Goal = Goal(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            detail = json.optString("detail", ""),
            done = json.optBoolean("done", false)
        )
    }
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val done: Boolean = false
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("done", done)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): Task = Task(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            done = json.optBoolean("done", false)
        )
    }
}

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val body: String,
    val createdAt: String
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("body", body)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): Note = Note(
            id = json.optString("id", UUID.randomUUID().toString()),
            body = json.optString("body", ""),
            createdAt = json.optString("createdAt", "")
        )
    }
}

data class Permissions(
    val calendar: Boolean = false,
    val location: Boolean = false,
    val notifications: Boolean = true,
    val files: Boolean = false,
    val microphone: Boolean = false,
    val camera: Boolean = false,
    val accessibility: Boolean = false,
    val cloudAI: Boolean = false
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("calendar", calendar)
        put("location", location)
        put("notifications", notifications)
        put("files", files)
        put("microphone", microphone)
        put("camera", camera)
        put("accessibility", accessibility)
        put("cloudAI", cloudAI)
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): Permissions {
            if (json == null) return Permissions()
            return Permissions(
                calendar = json.optBoolean("calendar", false),
                location = json.optBoolean("location", false),
                notifications = json.optBoolean("notifications", true),
                files = json.optBoolean("files", false),
                microphone = json.optBoolean("microphone", false),
                camera = json.optBoolean("camera", false),
                accessibility = json.optBoolean("accessibility", false),
                cloudAI = json.optBoolean("cloudAI", false)
            )
        }
    }
}

data class AuditEntry(
    val action: String,
    val at: String
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("action", action)
        put("at", at)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): AuditEntry = AuditEntry(
            action = json.optString("action", ""),
            at = json.optString("at", "")
        )
    }
}

data class MemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val summary: String,
    val sourceType: String = "user_text",
    val sourceLabel: String = "User input",
    val confidence: String = "user-confirmed",
    val createdAt: String,
    val status: String = "active" // active, revoked, pending-review
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("summary", summary)
        put("sourceType", sourceType)
        put("sourceLabel", sourceLabel)
        put("confidence", confidence)
        put("createdAt", createdAt)
        put("status", status)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): MemoryEntry = MemoryEntry(
            id = json.optString("id", UUID.randomUUID().toString()),
            summary = json.optString("summary", ""),
            sourceType = json.optString("sourceType", "user_text"),
            sourceLabel = json.optString("sourceLabel", "User input"),
            confidence = json.optString("confidence", "user-confirmed"),
            createdAt = json.optString("createdAt", ""),
            status = json.optString("status", "active")
        )
    }
}

data class AdaptiveTrial(
    val id: String = UUID.randomUUID().toString(),
    val proposedChange: String,
    val status: String = "trial", // trial, approved, rejected
    val startedAt: String,
    val expiresAt: String = "",
    val userFeedback: Boolean? = null
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("proposedChange", proposedChange)
        put("status", status)
        put("startedAt", startedAt)
        put("expiresAt", expiresAt)
        if (userFeedback != null) put("userFeedback", userFeedback)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): AdaptiveTrial = AdaptiveTrial(
            id = json.optString("id", UUID.randomUUID().toString()),
            proposedChange = json.optString("proposedChange", ""),
            status = json.optString("status", "trial"),
            startedAt = json.optString("startedAt", ""),
            expiresAt = json.optString("expiresAt", ""),
            userFeedback = if (json.has("userFeedback")) json.optBoolean("userFeedback") else null
        )
    }
}

data class MemoryPreferences(
    val tone: String = "warm, direct, and encouraging",
    val communicationStyle: String = "ask before assuming",
    val feedbackPreference: String = "specific next steps",
    val sensitiveTraits: List<String> = emptyList(),
    val sourcesAllowed: List<String> = listOf("user_text")
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("tone", tone)
        put("communicationStyle", communicationStyle)
        put("feedbackPreference", feedbackPreference)
        put("sensitiveTraits", JSONArray(sensitiveTraits))
        put("sourcesAllowed", JSONArray(sourcesAllowed))
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): MemoryPreferences {
            if (json == null) return MemoryPreferences()
            val sensitive = mutableListOf<String>()
            val sources = mutableListOf<String>()
            json.optJSONArray("sensitiveTraits")?.let { arr ->
                for (i in 0 until arr.length()) sensitive.add(arr.getString(i))
            }
            json.optJSONArray("sourcesAllowed")?.let { arr ->
                for (i in 0 until arr.length()) sources.add(arr.getString(i))
            }
            return MemoryPreferences(
                tone = json.optString("tone", "warm, direct, and encouraging"),
                communicationStyle = json.optString("communicationStyle", "ask before assuming"),
                feedbackPreference = json.optString("feedbackPreference", "specific next steps"),
                sensitiveTraits = sensitive,
                sourcesAllowed = if (sources.isEmpty()) listOf("user_text") else sources
            )
        }
    }
}

data class MemoryLearning(
    val rawRetentionDays: Int = 7,
    val automaticLowRiskTrials: Boolean = true,
    val requireApprovalForPermanentChanges: Boolean = true,
    val pendingTrials: List<AdaptiveTrial> = emptyList()
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("rawRetentionDays", rawRetentionDays)
        put("automaticLowRiskTrials", automaticLowRiskTrials)
        put("requireApprovalForPermanentChanges", requireApprovalForPermanentChanges)
        val trialsArr = JSONArray()
        pendingTrials.forEach { trialsArr.put(it.toJsonObject()) }
        put("pendingTrials", trialsArr)
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): MemoryLearning {
            if (json == null) return MemoryLearning()
            val trials = mutableListOf<AdaptiveTrial>()
            json.optJSONArray("pendingTrials")?.let { arr ->
                for (i in 0 until arr.length()) {
                    trials.add(AdaptiveTrial.fromJsonObject(arr.getJSONObject(i)))
                }
            }
            return MemoryLearning(
                rawRetentionDays = json.optInt("rawRetentionDays", 7),
                automaticLowRiskTrials = json.optBoolean("automaticLowRiskTrials", true),
                requireApprovalForPermanentChanges = json.optBoolean("requireApprovalForPermanentChanges", true),
                pendingTrials = trials
            )
        }
    }
}

data class Memory(
    val version: Int = 2,
    val entries: List<MemoryEntry> = emptyList(),
    val preferences: MemoryPreferences = MemoryPreferences(),
    val learning: MemoryLearning = MemoryLearning()
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("version", version)
        val entriesArr = JSONArray()
        entries.forEach { entriesArr.put(it.toJsonObject()) }
        put("entries", entriesArr)
        put("preferences", preferences.toJsonObject())
        put("learning", learning.toJsonObject())
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): Memory {
            if (json == null) return Memory()
            val entries = mutableListOf<MemoryEntry>()
            json.optJSONArray("entries")?.let { arr ->
                for (i in 0 until arr.length()) {
                    entries.add(MemoryEntry.fromJsonObject(arr.getJSONObject(i)))
                }
            }
            return Memory(
                version = json.optInt("version", 2),
                entries = entries,
                preferences = MemoryPreferences.fromJsonObject(json.optJSONObject("preferences")),
                learning = MemoryLearning.fromJsonObject(json.optJSONObject("learning"))
            )
        }
    }
}

data class ArloState(
    val goals: List<Goal> = listOf(
        Goal(
            id = UUID.randomUUID().toString(),
            title = "Make today count",
            detail = "Choose one meaningful next step.",
            done = false
        )
    ),
    val tasks: List<Task> = listOf(
        Task(
            id = UUID.randomUUID().toString(),
            title = "Write down the one thing that matters today",
            done = false
        )
    ),
    val notes: List<Note> = emptyList(),
    val permissions: Permissions = Permissions(),
    val lastCheckIn: String? = null,
    val lastMoodIndex: Int? = null,
    val audit: List<AuditEntry> = emptyList(),
    val memory: Memory = Memory(),
    val avatar: String = "✦"
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        val goalsArr = JSONArray()
        goals.forEach { goalsArr.put(it.toJsonObject()) }
        put("goals", goalsArr)

        val tasksArr = JSONArray()
        tasks.forEach { tasksArr.put(it.toJsonObject()) }
        put("tasks", tasksArr)

        val notesArr = JSONArray()
        notes.forEach { notesArr.put(it.toJsonObject()) }
        put("notes", notesArr)

        put("permissions", permissions.toJsonObject())
        if (lastCheckIn != null) put("lastCheckIn", lastCheckIn)
        if (lastMoodIndex != null) put("lastMoodIndex", lastMoodIndex)

        val auditArr = JSONArray()
        audit.forEach { auditArr.put(it.toJsonObject()) }
        put("audit", auditArr)

        put("memory", memory.toJsonObject())
        put("avatar", avatar)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): ArloState {
            val goals = mutableListOf<Goal>()
            json.optJSONArray("goals")?.let { arr ->
                for (i in 0 until arr.length()) goals.add(Goal.fromJsonObject(arr.getJSONObject(i)))
            }

            val tasks = mutableListOf<Task>()
            json.optJSONArray("tasks")?.let { arr ->
                for (i in 0 until arr.length()) tasks.add(Task.fromJsonObject(arr.getJSONObject(i)))
            }

            val notes = mutableListOf<Note>()
            json.optJSONArray("notes")?.let { arr ->
                for (i in 0 until arr.length()) notes.add(Note.fromJsonObject(arr.getJSONObject(i)))
            }

            val audit = mutableListOf<AuditEntry>()
            json.optJSONArray("audit")?.let { arr ->
                for (i in 0 until arr.length()) audit.add(AuditEntry.fromJsonObject(arr.getJSONObject(i)))
            }

            return ArloState(
                goals = if (goals.isEmpty()) defaultStarter().goals else goals,
                tasks = tasks,
                notes = notes,
                permissions = Permissions.fromJsonObject(json.optJSONObject("permissions")),
                lastCheckIn = if (json.has("lastCheckIn")) json.optString("lastCheckIn") else null,
                lastMoodIndex = if (json.has("lastMoodIndex")) json.optInt("lastMoodIndex") else null,
                audit = audit,
                memory = Memory.fromJsonObject(json.optJSONObject("memory")),
                avatar = json.optString("avatar", "✦")
            )
        }

        fun defaultStarter(): ArloState = ArloState()
    }
}
