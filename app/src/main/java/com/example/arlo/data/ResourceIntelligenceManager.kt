package com.example.arlo.data

import android.content.Context
import com.example.arlo.model.ArloState
import com.example.arlo.model.CuratedResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ResourceIntelligenceManager(context: Context) {

    private val _resourcesFlow = MutableStateFlow<List<CuratedResource>>(emptyList())
    val resourcesFlow: StateFlow<List<CuratedResource>> = _resourcesFlow.asStateFlow()

    private val _isGeneratingFlow = MutableStateFlow(false)
    val isGeneratingFlow: StateFlow<Boolean> = _isGeneratingFlow.asStateFlow()

    private val catalog = listOf(
        CuratedResource(
            title = "Ultradian 90-Minute Rhythm Protocol",
            sourceName = "Chronobiology Research / Dr. Nathaniel Kleitman",
            url = "https://hubermanlab.com/toolkit-for-focus-and-productivity",
            summary = "The human brain operates in 90-minute biological focus cycles. Pushing past 90 minutes degrades quality exponentially; taking a 15-minute defocus rest restores mental peak.",
            whyRelevantToYourPhase = "You have active focus intentions today. Structuring them in one 90-minute immersion window will prevent fatigue and guarantee deep momentum.",
            targetGoalCategory = "Habits & Routine",
            tags = listOf("Focus", "Biology", "Timing"),
            isHiddenGem = true,
            recommendedPhase = "Active Execution"
        ),
        CuratedResource(
            title = "Refactoring Guru: Visual Design Patterns",
            sourceName = "Interactive Software Architecture Guide",
            url = "https://refactoring.guru/design-patterns",
            summary = "Interactive, visual breakdown of architectural patterns (Creational, Structural, Behavioral) with real-world analogies and code examples in Kotlin/Java.",
            whyRelevantToYourPhase = "When developing features or writing software tasks, choosing the right pattern early saves hours of painful structural refactoring later.",
            targetGoalCategory = "Tech & Dev",
            tags = listOf("Engineering", "Architecture", "Clean Code"),
            isHiddenGem = true,
            recommendedPhase = "Architecture & Setup"
        ),
        CuratedResource(
            title = "The Friction Inversion Canvas",
            sourceName = "Behavioral Design / BJ Fogg & James Clear",
            url = "https://jamesclear.com/habit-guide",
            summary = "Every stalled milestone has invisible 2-minute friction barriers. By subtracting 1 friction step from good habits and adding 1 friction step to distractions, consistency becomes effortless.",
            whyRelevantToYourPhase = "For any goal milestone that has felt slow or stalled, diagnosing the physical friction points unlocks immediate movement.",
            targetGoalCategory = "Habits & Routine",
            tags = listOf("Behavior", "Momentum", "Habits"),
            isHiddenGem = true,
            recommendedPhase = "Overcoming Plateau"
        ),
        CuratedResource(
            title = "Non-Sleep Deep Rest (NSDR) Protocol",
            sourceName = "Stanford Neuroscience / Dr. Andrew Huberman",
            url = "https://www.youtube.com/watch?v=AKGrmY8OSHM",
            summary = "A 10-20 minute guided state of resting hypnosis and physiological sighs that restores striatal dopamine and cognitive focus without sleeping.",
            whyRelevantToYourPhase = "When energy is low during afternoon check-ins, NSDR provides a biological neural reset rather than reaching for stimulants.",
            targetGoalCategory = "Health & Wellness",
            tags = listOf("Recovery", "Energy", "Neuroscience"),
            isHiddenGem = true,
            recommendedPhase = "Rest & Reset"
        ),
        CuratedResource(
            title = "Feynman Learning Technique Interactive Guide",
            sourceName = "Mental Models / Richard Feynman",
            url = "https://fs.blog/feynman-technique/",
            summary = "Learn anything 4x faster by explaining it simply enough for a 12-year-old, identifying knowledge gaps, and distilling with clear mental analogies.",
            whyRelevantToYourPhase = "Ideal if you are learning new concepts, tools, or preparing for high-stakes presentations and exams.",
            targetGoalCategory = "Learning & Growth",
            tags = listOf("Learning", "Memory", "Mental Models"),
            isHiddenGem = true,
            recommendedPhase = "Learning & Mastery"
        ),
        CuratedResource(
            title = "Android Jetpack Compose Performance Checklist",
            sourceName = "Official Android Engineering Team",
            url = "https://developer.android.com/develop/ui/compose/performance",
            summary = "Comprehensive deep dive on avoiding recomposition loops, utilizing derivedStateOf, immutable models, and baseline profile optimizations.",
            whyRelevantToYourPhase = "Crucial for writing snappy, battery-efficient Android interfaces that maintain 60-120fps smoothly.",
            targetGoalCategory = "Tech & Dev",
            tags = listOf("Android", "Compose", "Performance"),
            isHiddenGem = true,
            recommendedPhase = "Optimization"
        )
    )

    init {
        _resourcesFlow.value = catalog.take(3)
    }

    fun getTop3ForState(state: ArloState): List<CuratedResource> {
        val currentList = _resourcesFlow.value
        if (currentList.isNotEmpty()) return currentList.take(3)

        // Find primary goal category
        val primaryGoal = state.goals.firstOrNull { !it.done }
        val category = primaryGoal?.category ?: "Habits & Routine"

        val matching = catalog.filter {
            it.targetGoalCategory.equals(category, ignoreCase = true) || it.tags.any { t -> category.contains(t, ignoreCase = true) }
        }

        val top3 = if (matching.size >= 3) {
            matching.take(3)
        } else {
            (matching + catalog.filterNot { matching.contains(it) }).take(3)
        }
        _resourcesFlow.value = top3
        return top3
    }

    suspend fun discoverFreshWithGemini(state: ArloState, geminiManager: GeminiManager): List<CuratedResource> {
        _isGeneratingFlow.value = true
        try {
            val goalsSummary = state.goals.filter { !it.done }.joinToString("; ") { "${it.title} (Category: ${it.category}, progress: ${it.progressPercent}%)" }
            val tasksSummary = state.tasks.filter { !it.done }.joinToString("; ") { it.title }

            val prompt = """
                You are Arlo's Resource Radar intelligence.
                Analyze the user's active goals and tasks:
                Goals: $goalsSummary
                Tasks: $tasksSummary
                
                Find exactly 3 high-yield, obscure or 'hidden gem' resources, interactive tools, research papers, frameworks, or guides that would help them succeed at their current phase, which they would NOT normally find or know about on their own.
                
                Respond in valid JSON array format with 3 objects:
                [
                  {
                    "title": "Title of resource or technique",
                    "sourceName": "Creator / Author / Publication",
                    "url": "https://valid-url-or-domain.org",
                    "summary": "2-sentence practical summary of what it does",
                    "whyRelevantToYourPhase": "Why this specifically solves their current goal or task phase",
                    "targetGoalCategory": "Habits, Tech, Health, or Creative",
                    "tags": ["Tag1", "Tag2"],
                    "isHiddenGem": true,
                    "recommendedPhase": "Active Execution"
                  }
                ]
            """.trimIndent()

            val response = geminiManager.generateResponse(prompt)
            val jsonText = response.substringAfter("[").substringBeforeLast("]")
            if (jsonText.isNotBlank()) {
                val fullJson = "[$jsonText]"
                val arr = JSONArray(fullJson)
                val newList = mutableListOf<CuratedResource>()
                for (i in 0 until arr.length()) {
                    newList.add(CuratedResource.fromJsonObject(arr.getJSONObject(i)))
                }
                if (newList.isNotEmpty()) {
                    _resourcesFlow.value = newList.take(3)
                    return newList.take(3)
                }
            }
        } catch (e: Exception) {
            // Fallback to rotating catalog
            val shuffled = catalog.shuffled().take(3)
            _resourcesFlow.value = shuffled
            return shuffled
        } finally {
            _isGeneratingFlow.value = false
        }
        return _resourcesFlow.value
    }

    fun markSaved(id: String) {
        val updated = _resourcesFlow.value.map {
            if (it.id == id) it.copy(isSavedToVault = true) else it
        }
        _resourcesFlow.value = updated
    }
}
