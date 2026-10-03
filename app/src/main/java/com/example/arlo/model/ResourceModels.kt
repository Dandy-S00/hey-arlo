package com.example.arlo.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CuratedResource(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceName: String,
    val url: String,
    val summary: String,
    val whyRelevantToYourPhase: String,
    val targetGoalCategory: String = "General",
    val tags: List<String> = emptyList(),
    val isHiddenGem: Boolean = true,
    val recommendedPhase: String = "Active Execution",
    val isSavedToVault: Boolean = false
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("sourceName", sourceName)
        put("url", url)
        put("summary", summary)
        put("whyRelevantToYourPhase", whyRelevantToYourPhase)
        put("targetGoalCategory", targetGoalCategory)
        val tagsArr = JSONArray()
        tags.forEach { tagsArr.put(it) }
        put("tags", tagsArr)
        put("isHiddenGem", isHiddenGem)
        put("recommendedPhase", recommendedPhase)
        put("isSavedToVault", isSavedToVault)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): CuratedResource {
            val tagList = mutableListOf<String>()
            json.optJSONArray("tags")?.let { arr ->
                for (i in 0 until arr.length()) tagList.add(arr.getString(i))
            }
            return CuratedResource(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", ""),
                sourceName = json.optString("sourceName", "Curated Guide"),
                url = json.optString("url", ""),
                summary = json.optString("summary", ""),
                whyRelevantToYourPhase = json.optString("whyRelevantToYourPhase", ""),
                targetGoalCategory = json.optString("targetGoalCategory", "General"),
                tags = tagList,
                isHiddenGem = json.optBoolean("isHiddenGem", true),
                recommendedPhase = json.optString("recommendedPhase", "Active Execution"),
                isSavedToVault = json.optBoolean("isSavedToVault", false)
            )
        }
    }
}
