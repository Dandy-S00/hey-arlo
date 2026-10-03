package com.example.arlo.data

import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class GoalFilterType(val label: String) {
    ALL("All Intentions"),
    IN_PROGRESS("In Motion"),
    COMPLETED("Celebrated Steps")
}

data class GoalProgressMetrics(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val overallProgressPercent: Int = 0
)

data class GoalFilterState(
    val filterType: GoalFilterType = GoalFilterType.ALL,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

class GoalStateManager(
    private val repository: ArloRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        val PRESET_CATEGORIES = listOf(
            "Habits & Routine",
            "Health & Energy",
            "Mindset",
            "Deep Work",
            "Personal Growth"
        )
    }

    private val _filterState = MutableStateFlow(GoalFilterState())
    val filterState: StateFlow<GoalFilterState> = _filterState

    // Raw stream of goals from repository
    val goalsFlow: StateFlow<List<Goal>> = repository.state
        .map { it?.goals ?: emptyList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Progress metrics calculated reactively
    val metricsFlow: StateFlow<GoalProgressMetrics> = goalsFlow.map { goals ->
        if (goals.isEmpty()) {
            GoalProgressMetrics()
        } else {
            val total = goals.size
            val completed = goals.count { it.done }
            val inProgress = total - completed
            val avgProgress = (goals.sumOf { it.progressPercent.toDouble() } / total).toInt()
            GoalProgressMetrics(
                totalCount = total,
                completedCount = completed,
                inProgressCount = inProgress,
                overallProgressPercent = avgProgress
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, GoalProgressMetrics())

    // Filtered and sorted goals stream
    val filteredGoalsFlow: StateFlow<List<Goal>> = combine(goalsFlow, _filterState) { goals, filter ->
        goals.filter { goal ->
            val matchesType = when (filter.filterType) {
                GoalFilterType.ALL -> true
                GoalFilterType.IN_PROGRESS -> !goal.done
                GoalFilterType.COMPLETED -> goal.done
            }
            val matchesCategory = filter.selectedCategory == null || goal.category == filter.selectedCategory
            val matchesQuery = filter.searchQuery.isBlank() ||
                    goal.title.contains(filter.searchQuery, ignoreCase = true) ||
                    goal.detail.contains(filter.searchQuery, ignoreCase = true) ||
                    goal.category.contains(filter.searchQuery, ignoreCase = true)

            matchesType && matchesCategory && matchesQuery
        }.sortedWith(
            compareBy<Goal> { it.done } // In-progress first, completed at the bottom
                .thenByDescending { it.createdAt }
        )
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Actions
    fun defineGoal(
        title: String,
        detail: String = "",
        category: String = "Habits & Routine",
        targetValue: Int = 1,
        unit: String = "step",
        milestoneTitles: List<String> = emptyList()
    ) {
        if (title.isBlank()) return
        val milestones = milestoneTitles
            .filter { it.isNotBlank() }
            .map { Milestone(title = it.trim(), done = false) }

        val newGoal = Goal(
            title = title.trim(),
            detail = detail.trim(),
            category = category,
            targetValue = if (milestones.isNotEmpty()) milestones.size else targetValue.coerceAtLeast(1),
            currentValue = 0,
            unit = if (milestones.isNotEmpty()) "milestone" else unit,
            milestones = milestones,
            done = false
        )
        repository.addGoal(newGoal)
    }

    fun markComplete(goalId: String) {
        repository.markGoalComplete(goalId, true)
    }

    fun markIncomplete(goalId: String) {
        repository.markGoalComplete(goalId, false)
    }

    fun toggleComplete(goalId: String) {
        repository.toggleGoal(goalId)
    }

    fun incrementProgress(goalId: String, amount: Int = 1) {
        repository.incrementGoalProgress(goalId, amount)
    }

    fun updateProgress(goalId: String, newValue: Int) {
        repository.updateGoalProgressValue(goalId, newValue)
    }

    fun addMilestone(goalId: String, milestoneTitle: String) {
        repository.addMilestone(goalId, milestoneTitle)
    }

    fun toggleMilestone(goalId: String, milestoneId: String) {
        repository.toggleMilestone(goalId, milestoneId)
    }

    fun deleteMilestone(goalId: String, milestoneId: String) {
        repository.deleteMilestone(goalId, milestoneId)
    }

    fun deleteGoal(goalId: String) {
        repository.deleteGoal(goalId)
    }

    fun setFilterType(type: GoalFilterType) {
        _filterState.value = _filterState.value.copy(filterType = type)
    }

    fun setCategoryFilter(category: String?) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun clearFilters() {
        _filterState.value = GoalFilterState()
    }
}
