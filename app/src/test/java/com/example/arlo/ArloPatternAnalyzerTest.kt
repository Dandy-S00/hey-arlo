package com.example.arlo

import com.example.arlo.data.ArloPatternAnalyzer
import com.example.arlo.model.ArloState
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import com.example.arlo.model.Task
import org.junit.Assert.*
import org.junit.Test

class ArloPatternAnalyzerTest {

    @Test
    fun testAnalyzeExtractsInsightsAndTimeSensitiveGoal() {
        val testGoal = Goal(
            id = "goal-1",
            title = "Master Reactive Flow",
            category = "Deep Work",
            milestones = listOf(
                Milestone(id = "m1", title = "Read docs", done = true),
                Milestone(id = "m2", title = "Build demo pipeline", done = false)
            ),
            targetValue = 2,
            currentValue = 1,
            done = false
        )

        val testNote = Note(
            body = "Had a peaceful and deeply focused morning session.",
            createdAt = "2026-10-03T09:00:00.000Z",
            mood = "Focused"
        )

        val state = ArloState(
            goals = listOf(testGoal),
            notes = listOf(testNote),
            tasks = listOf(Task(title = "Morning stretch", done = true)),
            lastMoodIndex = 4
        )

        val breakdown = ArloPatternAnalyzer.analyze(state)

        assertNotNull(breakdown.timeSensitiveGoal)
        assertEquals("Master Reactive Flow", breakdown.timeSensitiveGoal?.title)
        assertNotNull(breakdown.suggestedMilestone)
        assertEquals("Build demo pipeline", breakdown.suggestedMilestone?.title)

        assertTrue(breakdown.hiddenPatterns.isNotEmpty())
        assertTrue(breakdown.attentionItems.isNotEmpty())
        assertTrue(breakdown.learningProgress.isNotEmpty())
        assertTrue(breakdown.nextStepSuggestion.contains("Build demo pipeline"))
    }

    @Test
    fun testInteractiveResponses() {
        val state = ArloState(
            goals = listOf(Goal(title = "Kotlin Learning", done = false)),
            notes = emptyList(),
            tasks = emptyList()
        )
        val breakdown = ArloPatternAnalyzer.analyze(state)

        val patternResp = ArloPatternAnalyzer.generateInteractiveResponse("Tell me my patterns", breakdown)
        assertTrue(patternResp.contains("pattern", ignoreCase = true))

        val attentionResp = ArloPatternAnalyzer.generateInteractiveResponse("What should I pay attention to?", breakdown)
        assertTrue(attentionResp.contains("attention", ignoreCase = true))

        val learningResp = ArloPatternAnalyzer.generateInteractiveResponse("What did I learn?", breakdown)
        assertTrue(learningResp.contains("learn", ignoreCase = true) || learningResp.contains("progress", ignoreCase = true))

        val goalResp = ArloPatternAnalyzer.generateInteractiveResponse("What is my next goal step?", breakdown)
        assertTrue(goalResp.contains("goal", ignoreCase = true) || goalResp.contains("step", ignoreCase = true))
    }
}
