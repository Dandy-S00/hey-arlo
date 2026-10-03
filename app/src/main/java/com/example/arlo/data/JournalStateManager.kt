package com.example.arlo.data

import com.example.arlo.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class JournalMetrics(
    val totalEntries: Int = 0,
    val uniqueDaysCount: Int = 0,
    val mostCommonMood: String = "None"
)

data class JournalFilterState(
    val searchQuery: String = "",
    val selectedMood: String? = null
)

class JournalStateManager(
    private val repository: ArloRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        val PROMPT_PRESETS = listOf(
            "What went well today?",
            "What brought you calm or joy?",
            "What is one thing you learned?",
            "What was challenging, and how did you meet it?",
            "One kind thing you did or received today?",
            "Free-form reflection"
        )

        val MOOD_TAGS = listOf(
            Pair("Grateful", "🌸"),
            Pair("Calm", "🌊"),
            Pair("Energized", "☀️"),
            Pair("Thoughtful", "🍃"),
            Pair("Restful", "🌙")
        )
    }

    private val _filterState = MutableStateFlow(JournalFilterState())
    val filterState: StateFlow<JournalFilterState> = _filterState

    val reflectionsFlow: StateFlow<List<Note>> = repository.state
        .map { it?.notes ?: emptyList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val metricsFlow: StateFlow<JournalMetrics> = reflectionsFlow.map { notes ->
        if (notes.isEmpty()) {
            JournalMetrics()
        } else {
            val total = notes.size
            val uniqueDays = notes.map { it.createdAt.take(10) }.distinct().size
            val moodCounts = notes.filter { it.mood.isNotBlank() }
                .groupingBy { it.mood }
                .eachCount()
            val topMood = moodCounts.maxByOrNull { it.value }?.key ?: "Reflective"

            JournalMetrics(
                totalEntries = total,
                uniqueDaysCount = uniqueDays,
                mostCommonMood = topMood
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, JournalMetrics())

    val filteredReflectionsFlow: StateFlow<List<Note>> = combine(reflectionsFlow, _filterState) { notes, filter ->
        notes.filter { note ->
            val matchesMood = filter.selectedMood == null || note.mood.equals(filter.selectedMood, ignoreCase = true)
            val matchesQuery = filter.searchQuery.isBlank() ||
                    note.body.contains(filter.searchQuery, ignoreCase = true) ||
                    note.prompt.contains(filter.searchQuery, ignoreCase = true) ||
                    note.mood.contains(filter.searchQuery, ignoreCase = true)
            matchesMood && matchesQuery
        }.sortedByDescending { it.createdAt }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun saveReflection(
        body: String,
        prompt: String = "",
        mood: String = "",
        tags: List<String> = emptyList()
    ) {
        repository.addReflection(
            body = body,
            prompt = prompt,
            mood = mood,
            tags = tags
        )
    }

    fun updateReflection(id: String, body: String, mood: String = "") {
        repository.updateReflection(id, body, mood)
    }

    fun deleteReflection(id: String) {
        repository.deleteNote(id)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setMoodFilter(mood: String?) {
        _filterState.value = _filterState.value.copy(selectedMood = mood)
    }

    fun clearFilters() {
        _filterState.value = JournalFilterState()
    }
}
