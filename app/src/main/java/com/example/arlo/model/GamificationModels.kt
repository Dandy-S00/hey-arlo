package com.example.arlo.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CatQuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val xpReward: Int,
    val treatsReward: Int,
    val isCompleted: Boolean = false,
    val category: String = "Focus"
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("xpReward", xpReward)
        put("treatsReward", treatsReward)
        put("isCompleted", isCompleted)
        put("category", category)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): CatQuest = CatQuest(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", ""),
            description = json.optString("description", ""),
            xpReward = json.optInt("xpReward", 30),
            treatsReward = json.optInt("treatsReward", 1),
            isCompleted = json.optBoolean("isCompleted", false),
            category = json.optString("category", "Focus")
        )
    }
}

data class GamificationState(
    val isEnabled: Boolean = false,
    val level: Int = 1,
    val currentXp: Int = 20,
    val xpForNextLevel: Int = 100,
    val tunaTreats: Int = 5,
    val currentRank: String = "Prowling Apprentice",
    val equippedAccessory: String = "🧣 Cozy Knit Scarf",
    val unlockedAccessories: List<String> = listOf("🧣 Cozy Knit Scarf", "🎀 Red Ribbon Collar"),
    val activeQuests: List<CatQuest> = defaultQuests()
) {
    val progressFraction: Float
        get() = (currentXp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("isEnabled", isEnabled)
        put("level", level)
        put("currentXp", currentXp)
        put("xpForNextLevel", xpForNextLevel)
        put("tunaTreats", tunaTreats)
        put("currentRank", currentRank)
        put("equippedAccessory", equippedAccessory)
        val accArr = JSONArray()
        unlockedAccessories.forEach { accArr.put(it) }
        put("unlockedAccessories", accArr)
        val qArr = JSONArray()
        activeQuests.forEach { qArr.put(it.toJsonObject()) }
        put("activeQuests", qArr)
    }

    companion object {
        fun defaultQuests(): List<CatQuest> = listOf(
            CatQuest(
                title = "Knock Out 1 Stalled Step",
                description = "Complete one milestone or task you have put off.",
                xpReward = 50,
                treatsReward = 2,
                category = "Momentum"
            ),
            CatQuest(
                title = "Feline Brain Dump",
                description = "Jot down your current thoughts in the Journal.",
                xpReward = 35,
                treatsReward = 1,
                category = "Mindset"
            ),
            CatQuest(
                title = "Vault Knowledge Snack",
                description = "Read & review one link from your Linksi Vault.",
                xpReward = 30,
                treatsReward = 1,
                category = "Curiosity"
            )
        )

        fun fromJsonObject(json: JSONObject?): GamificationState {
            if (json == null) return GamificationState()
            val accList = mutableListOf<String>()
            json.optJSONArray("unlockedAccessories")?.let { arr ->
                for (i in 0 until arr.length()) accList.add(arr.getString(i))
            }

            val questList = mutableListOf<CatQuest>()
            json.optJSONArray("activeQuests")?.let { arr ->
                for (i in 0 until arr.length()) questList.add(CatQuest.fromJsonObject(arr.getJSONObject(i)))
            }

            return GamificationState(
                isEnabled = json.optBoolean("isEnabled", false),
                level = json.optInt("level", 1),
                currentXp = json.optInt("currentXp", 20),
                xpForNextLevel = json.optInt("xpForNextLevel", 100),
                tunaTreats = json.optInt("tunaTreats", 5),
                currentRank = json.optString("currentRank", "Prowling Apprentice"),
                equippedAccessory = json.optString("equippedAccessory", "🧣 Cozy Knit Scarf"),
                unlockedAccessories = if (accList.isEmpty()) listOf("🧣 Cozy Knit Scarf", "🎀 Red Ribbon Collar") else accList,
                activeQuests = if (questList.isEmpty()) defaultQuests() else questList
            )
        }
    }
}

data class CatchUpBriefing(
    val overdueTasksCount: Int,
    val pendingMilestonesCount: Int,
    val stalledGoals: List<String>,
    val recoverySteps: List<String>,
    val hoursSinceLastCheckIn: Int,
    val motivationalQuote: String
)
