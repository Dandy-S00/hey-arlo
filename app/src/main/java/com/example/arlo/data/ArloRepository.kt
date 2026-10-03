package com.example.arlo.data

import android.content.Context
import com.example.arlo.model.*
import com.example.arlo.security.VaultCrypto
import com.example.arlo.security.VaultStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ArloRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val storage = VaultStorage(context)
    private var activePassphrase: CharArray? = null

    private val _hasVault = MutableStateFlow(storage.hasVault())
    val hasVault: StateFlow<Boolean> = _hasVault.asStateFlow()

    private val _hasLegacyData = MutableStateFlow(storage.hasLegacyData())
    val hasLegacyData: StateFlow<Boolean> = _hasLegacyData.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _state = MutableStateFlow<ArloState?>(null)
    val state: StateFlow<ArloState?> = _state.asStateFlow()

    private val _connections = MutableStateFlow<Map<String, ConnectionConsent>>(emptyMap())
    val connections: StateFlow<Map<String, ConnectionConsent>> = _connections.asStateFlow()

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun nowIso(): String = isoFormat.format(Date())

    fun unlock(passphrase: String): Result<Unit> {
        if (!storage.hasVault()) {
            return Result.failure(IllegalStateException("No vault exists yet."))
        }
        val envelope = storage.readVaultEnvelope()
            ?: return Result.failure(IllegalStateException("Failed to read vault file."))

        return try {
            val passChars = passphrase.toCharArray()
            val decryptedJsonStr = VaultCrypto.decrypt(envelope, passChars)
            val json = JSONObject(decryptedJsonStr)
            val loadedState = ArloState.fromJsonObject(json)

            activePassphrase = passChars
            _state.value = loadedState
            _isLocked.value = false
            audit("vault unlocked")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Unable to open the vault. Check the passphrase and try again.", e))
        }
    }

    fun createVault(passphrase: String, importLegacy: Boolean): Result<Unit> {
        if (passphrase.length < 12) {
            return Result.failure(IllegalArgumentException("Passphrase must be at least 12 characters."))
        }

        return try {
            val passChars = passphrase.toCharArray()
            val initialState: ArloState = if (importLegacy && storage.hasLegacyData()) {
                val legacyStr = storage.readLegacyData()
                storage.clearLegacyData()
                _hasLegacyData.value = false
                try {
                    if (legacyStr != null) ArloState.fromJsonObject(JSONObject(legacyStr)) else ArloState.defaultStarter()
                } catch (_: Exception) {
                    ArloState.defaultStarter()
                }
            } else {
                ArloState.defaultStarter()
            }

            val envelope = VaultCrypto.encrypt(initialState.toJsonObject().toString(), passChars)
            storage.writeVaultEnvelope(envelope)

            activePassphrase = passChars
            _state.value = initialState
            _isLocked.value = false
            _hasVault.value = true
            audit("encrypted vault created")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun lock() {
        activePassphrase?.fill('\u0000')
        activePassphrase = null
        _state.value = null
        _isLocked.value = true
    }

    fun deleteVault(): Boolean {
        lock()
        val deleted = storage.deleteVault()
        _hasVault.value = storage.hasVault()
        return deleted
    }

    fun changePassphrase(oldPass: String, newPass: String): Result<Unit> {
        if (newPass.length < 12) {
            return Result.failure(IllegalArgumentException("New passphrase must be at least 12 characters."))
        }
        val current = _state.value ?: return Result.failure(IllegalStateException("Vault is locked."))
        val envelope = storage.readVaultEnvelope() ?: return Result.failure(IllegalStateException("No vault found."))

        return try {
            // Verify old passphrase
            VaultCrypto.decrypt(envelope, oldPass.toCharArray())

            // Re-encrypt with new passphrase
            val newPassChars = newPass.toCharArray()
            val newEnvelope = VaultCrypto.encrypt(current.toJsonObject().toString(), newPassChars)
            storage.writeVaultEnvelope(newEnvelope)

            activePassphrase?.fill('\u0000')
            activePassphrase = newPassChars
            audit("passphrase changed")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Current passphrase incorrect or encryption failed.", e))
        }
    }

    fun exportEncryptedBackup(): String? {
        return storage.readVaultEnvelope()
    }

    fun exportDecryptedJson(): String? {
        return _state.value?.toJsonObject()?.toString(2)
    }

    private fun persist() {
        val current = _state.value ?: return
        val pass = activePassphrase ?: return
        scope.launch {
            try {
                val envelope = VaultCrypto.encrypt(current.toJsonObject().toString(), pass)
                storage.writeVaultEnvelope(envelope)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun audit(action: String) {
        val current = _state.value ?: return
        val newEntry = AuditEntry(action = action, at = nowIso())
        val updatedAudit = (listOf(newEntry) + current.audit).take(50)
        _state.value = current.copy(audit = updatedAudit)
        persist()
    }

    // Task actions
    fun addTask(title: String) {
        if (title.isBlank()) return
        val current = _state.value ?: return
        val newTask = Task(title = title.trim(), done = false)
        _state.value = current.copy(tasks = listOf(newTask) + current.tasks)
        audit("task created")
    }

    fun toggleTask(id: String) {
        val current = _state.value ?: return
        val updated = current.tasks.map { if (it.id == id) it.copy(done = !it.done) else it }
        _state.value = current.copy(tasks = updated)
        audit("task updated")
    }

    fun deleteTask(id: String) {
        val current = _state.value ?: return
        val updated = current.tasks.filter { it.id != id }
        _state.value = current.copy(tasks = updated)
        audit("task deleted")
    }

    // Goal actions
    fun addGoal(title: String, detail: String) {
        if (title.isBlank()) return
        val current = _state.value ?: return
        val newGoal = Goal(title = title.trim(), detail = detail.trim(), done = false)
        _state.value = current.copy(goals = listOf(newGoal) + current.goals)
        audit("goal created")
    }

    fun toggleGoal(id: String) {
        val current = _state.value ?: return
        val updated = current.goals.map { if (it.id == id) it.copy(done = !it.done) else it }
        _state.value = current.copy(goals = updated)
        audit("goal updated")
    }

    fun deleteGoal(id: String) {
        val current = _state.value ?: return
        val updated = current.goals.filter { it.id != id }
        _state.value = current.copy(goals = updated)
        audit("goal deleted")
    }

    // Note actions
    fun addNote(body: String) {
        if (body.isBlank()) return
        val current = _state.value ?: return
        val newNote = Note(body = body.trim(), createdAt = nowIso())
        _state.value = current.copy(notes = listOf(newNote) + current.notes)
        audit("journal note created")
    }

    fun deleteNote(id: String) {
        val current = _state.value ?: return
        val updated = current.notes.filter { it.id != id }
        _state.value = current.copy(notes = updated)
        audit("journal note deleted")
    }

    // Check-in
    fun checkIn(moodIndex: Int) {
        val current = _state.value ?: return
        _state.value = current.copy(
            lastCheckIn = nowIso(),
            lastMoodIndex = moodIndex
        )
        audit("daily check-in (mood $moodIndex)")
    }

    // Permissions
    fun updatePermission(key: String, enabled: Boolean) {
        val current = _state.value ?: return
        val p = current.permissions
        val updated = when (key) {
            "calendar" -> p.copy(calendar = enabled)
            "location" -> p.copy(location = enabled)
            "notifications" -> p.copy(notifications = enabled)
            "files" -> p.copy(files = enabled)
            "microphone" -> p.copy(microphone = enabled)
            "camera" -> p.copy(camera = enabled)
            "accessibility" -> p.copy(accessibility = enabled)
            "cloudAI" -> p.copy(cloudAI = enabled)
            else -> p
        }
        _state.value = current.copy(permissions = updated)
        audit("permission $key ${if (enabled) "enabled" else "disabled"}")
    }

    fun pauseAllPermissions() {
        val current = _state.value ?: return
        _state.value = current.copy(
            permissions = Permissions(
                calendar = false,
                location = false,
                notifications = false,
                files = false,
                microphone = false,
                camera = false,
                accessibility = false,
                cloudAI = false
            )
        )
        audit("all permissions paused")
    }

    // Memory & Adaptive Learning
    fun startAdaptiveTrial(change: String) {
        if (change.isBlank()) return
        val current = _state.value ?: return
        val newTrial = AdaptiveTrial(
            proposedChange = change.trim(),
            status = "trial",
            startedAt = nowIso(),
            expiresAt = isoFormat.format(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
        )
        val learning = current.memory.learning
        val updatedLearning = learning.copy(pendingTrials = listOf(newTrial) + learning.pendingTrials)
        _state.value = current.copy(memory = current.memory.copy(learning = updatedLearning))
        audit("adaptive trial started")
    }

    fun recordTrialFeedback(trialId: String, keep: Boolean) {
        val current = _state.value ?: return
        val learning = current.memory.learning
        var approvedStyle: String? = null

        val updatedTrials = learning.pendingTrials.map { trial ->
            if (trial.id == trialId) {
                val newStatus = if (keep) "approved" else "rejected"
                if (keep) approvedStyle = trial.proposedChange
                trial.copy(status = newStatus, userFeedback = keep)
            } else trial
        }

        val updatedPreferences = if (approvedStyle != null) {
            current.memory.preferences.copy(communicationStyle = approvedStyle!!)
        } else current.memory.preferences

        _state.value = current.copy(
            memory = current.memory.copy(
                preferences = updatedPreferences,
                learning = learning.copy(pendingTrials = updatedTrials)
            )
        )
        audit("adaptive trial ${if (keep) "approved" else "rejected"}")
    }

    fun addMemoryEntry(summary: String, sourceType: String = "user_text", sourceLabel: String = "User input") {
        if (summary.isBlank()) return
        val current = _state.value ?: return
        val entry = MemoryEntry(
            summary = summary.trim(),
            sourceType = sourceType,
            sourceLabel = sourceLabel,
            confidence = "user-confirmed",
            createdAt = nowIso()
        )
        _state.value = current.copy(
            memory = current.memory.copy(entries = listOf(entry) + current.memory.entries)
        )
        audit("memory entry added")
    }

    fun revokeSource(sourceType: String) {
        val current = _state.value ?: return
        val prefs = current.memory.preferences
        val updatedSources = prefs.sourcesAllowed.filter { it != sourceType }
        val updatedEntries = current.memory.entries.map {
            if (it.sourceType == sourceType) it.copy(status = "revoked") else it
        }
        _state.value = current.copy(
            memory = current.memory.copy(
                preferences = prefs.copy(sourcesAllowed = updatedSources),
                entries = updatedEntries
            )
        )
        audit("source revoked: $sourceType")
    }

    fun deleteMemoryEntry(id: String) {
        val current = _state.value ?: return
        val updatedEntries = current.memory.entries.filter { it.id != id }
        _state.value = current.copy(
            memory = current.memory.copy(entries = updatedEntries)
        )
        audit("memory entry deleted")
    }

    // Avatar
    fun setAvatar(avatarSymbol: String) {
        val current = _state.value ?: return
        _state.value = current.copy(avatar = avatarSymbol)
        audit("avatar updated")
    }

    // Connectors
    fun requestConnection(provider: ApiProvider) {
        val map = _connections.value.toMutableMap()
        if (map[provider.id]?.status == "awaiting-provider-auth") {
            map.remove(provider.id)
            audit("disconnected connector ${provider.name}")
        } else {
            map[provider.id] = ConnectionConsent(
                providerId = provider.id,
                providerName = provider.name,
                status = "awaiting-provider-auth",
                requestedAt = nowIso()
            )
            audit("requested connector ${provider.name}")
        }
        _connections.value = map
    }
}
