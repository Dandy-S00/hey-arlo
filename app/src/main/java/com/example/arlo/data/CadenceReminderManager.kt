package com.example.arlo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.arlo.model.ArloState
import com.example.arlo.model.CatchUpBriefing
import com.example.arlo.model.GamificationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class CadenceReminderManager(
    context: Context,
    private val rhythmManager: NaturalRhythmManager = NaturalRhythmManager(context)
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_cadence_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CADENCE_HOURS = 32
        const val CADENCE_INTERVAL_MS = CADENCE_HOURS * 60 * 60 * 1000L
    }

    private val _gamificationFlow = MutableStateFlow(loadGamificationState())
    val gamificationFlow: StateFlow<GamificationState> = _gamificationFlow.asStateFlow()

    private val _showCheckpointDialog = MutableStateFlow(false)
    val showCheckpointDialog: StateFlow<Boolean> = _showCheckpointDialog.asStateFlow()

    private var cachedBriefing: CatchUpBriefing? = null

    init {
        checkIfCheckpointDue()
    }

    private fun loadGamificationState(): GamificationState {
        val raw = prefs.getString("gamification_state", null) ?: return GamificationState()
        return try {
            GamificationState.fromJsonObject(JSONObject(raw))
        } catch (e: Exception) {
            GamificationState()
        }
    }

    private fun saveGamificationState(state: GamificationState) {
        prefs.edit().putString("gamification_state", state.toJsonObject().toString()).apply()
        _gamificationFlow.value = state
    }

    fun setGamificationEnabled(enabled: Boolean) {
        val updated = _gamificationFlow.value.copy(isEnabled = enabled)
        saveGamificationState(updated)
    }

    fun awardProgress(xp: Int, treats: Int = 0): String? {
        val current = _gamificationFlow.value
        if (!current.isEnabled) return null

        var newXp = current.currentXp + xp
        var newLevel = current.level
        var newXpThreshold = current.xpForNextLevel
        var leveledUp = false

        while (newXp >= newXpThreshold) {
            newXp -= newXpThreshold
            newLevel += 1
            newXpThreshold = (newXpThreshold * 1.35).toInt()
            leveledUp = true
        }

        val newRank = when (newLevel) {
            1 -> "Prowling Apprentice"
            2 -> "Curious Stalker"
            3 -> "Shadow Pouncer"
            4 -> "Master of Focus"
            else -> "Grand Feline Sovereign"
        }

        val updated = current.copy(
            level = newLevel,
            currentXp = newXp,
            xpForNextLevel = newXpThreshold,
            tunaTreats = current.tunaTreats + treats,
            currentRank = newRank
        )
        saveGamificationState(updated)

        return if (leveledUp) {
            "🎉 Level Up! You reached Level $newLevel: $newRank!"
        } else {
            "+$xp XP earned! 🐾"
        }
    }

    fun equipAccessory(accessory: String) {
        val current = _gamificationFlow.value
        val updated = current.copy(equippedAccessory = accessory)
        saveGamificationState(updated)
    }

    fun unlockAccessory(accessory: String, costTreats: Int): Boolean {
        val current = _gamificationFlow.value
        if (current.tunaTreats >= costTreats && !current.unlockedAccessories.contains(accessory)) {
            val updated = current.copy(
                tunaTreats = current.tunaTreats - costTreats,
                unlockedAccessories = current.unlockedAccessories + accessory,
                equippedAccessory = accessory
            )
            saveGamificationState(updated)
            return true
        }
        return false
    }

    fun checkIfCheckpointDue(): Boolean {
        val lastTimestamp = prefs.getLong("last_catchup_checkpoint_ts", 0L)
        val now = System.currentTimeMillis()
        val interval = rhythmManager.getAdaptiveCadenceIntervalMs()
        val isDue = (now - lastTimestamp) >= interval
        if (isDue) {
            _showCheckpointDialog.value = true
        }
        return isDue
    }

    fun openCheckpointManual() {
        _showCheckpointDialog.value = true
    }

    fun dismissCheckpoint() {
        prefs.edit().putLong("last_catchup_checkpoint_ts", System.currentTimeMillis()).apply()
        _showCheckpointDialog.value = false
    }

    fun generateBriefing(state: ArloState): CatchUpBriefing {
        val pendingTasks = state.tasks.filter { !it.done }
        val stalledGoals = state.goals.filter { !it.done && it.progressPercent < 80 }
        val pendingMilestonesCount = state.goals.sumOf { g -> g.milestones.count { !it.done } }

        val steps = mutableListOf<String>()
        if (pendingTasks.isNotEmpty()) {
            steps.add("Pick just ONE pending task ('${pendingTasks.first().title.take(28)}') and knock it out in 5 minutes.")
        } else {
            steps.add("Draft one quick focus intention for the next 4 hours.")
        }

        if (stalledGoals.isNotEmpty()) {
            steps.add("Advance a single milestone on '${stalledGoals.first().title.take(24)}' to regain forward momentum.")
        } else {
            steps.add("Celebrate your clean slate! Maintain your gentle momentum with an evening stretch.")
        }

        steps.add("Breathe: Remember that paws step one at a time. Zero guilt, only forward motion.")

        val briefing = CatchUpBriefing(
            overdueTasksCount = pendingTasks.size,
            pendingMilestonesCount = pendingMilestonesCount,
            stalledGoals = stalledGoals.map { it.title },
            recoverySteps = steps,
            hoursSinceLastCheckIn = CADENCE_HOURS,
            motivationalQuote = "Cats never worry about yesterday's spilled milk. Shake your tail and stretch forward."
        )
        cachedBriefing = briefing
        return briefing
    }
}
