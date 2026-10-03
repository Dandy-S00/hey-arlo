package com.example.arlo

import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import org.junit.Assert.*
import org.junit.Test

class GoalStateManagerTest {

    @Test
    fun testSimpleGoalProgress() {
        val incompleteGoal = Goal(title = "Drink 2L water", done = false)
        assertEquals(0f, incompleteGoal.progressFraction, 0.001f)
        assertEquals(0, incompleteGoal.progressPercent)

        val completedGoal = incompleteGoal.copy(done = true)
        assertEquals(1f, completedGoal.progressFraction, 0.001f)
        assertEquals(100, completedGoal.progressPercent)
    }

    @Test
    fun testNumericTargetProgress() {
        val goal = Goal(
            title = "Morning Meditation",
            targetValue = 10,
            currentValue = 4,
            unit = "session",
            done = false
        )

        assertEquals(0.4f, goal.progressFraction, 0.001f)
        assertEquals(40, goal.progressPercent)

        val finishedGoal = goal.copy(currentValue = 10, done = true)
        assertEquals(1.0f, finishedGoal.progressFraction, 0.001f)
        assertEquals(100, finishedGoal.progressPercent)
    }

    @Test
    fun testMilestoneGoalProgress() {
        val milestones = listOf(
            Milestone(title = "Outline chapters", done = true),
            Milestone(title = "Draft manuscript", done = true),
            Milestone(title = "First revision", done = false),
            Milestone(title = "Final review", done = false)
        )
        val goal = Goal(
            title = "Write Guidebook",
            milestones = milestones,
            done = false
        )

        // 2 out of 4 is 50%
        assertEquals(0.5f, goal.progressFraction, 0.001f)
        assertEquals(50, goal.progressPercent)

        val allDoneMilestones = milestones.map { it.copy(done = true) }
        val completedGoal = goal.copy(milestones = allDoneMilestones, done = true)
        assertEquals(1.0f, completedGoal.progressFraction, 0.001f)
        assertEquals(100, completedGoal.progressPercent)
    }

    @Test
    fun testJsonSerializationWithProgress() {
        val goal = Goal(
            title = "Read 5 Books",
            detail = "Deepen knowledge",
            category = "Personal Growth",
            targetValue = 5,
            currentValue = 2,
            unit = "book",
            milestones = listOf(Milestone(title = "Book 1", done = true)),
            createdAt = "2026-10-03T10:00:00.000Z",
            done = false
        )

        val json = goal.toJsonObject()
        val restored = Goal.fromJsonObject(json)

        assertEquals(goal.title, restored.title)
        assertEquals(goal.detail, restored.detail)
        assertEquals(goal.category, restored.category)
        assertEquals(goal.targetValue, restored.targetValue)
        assertEquals(goal.currentValue, restored.currentValue)
        assertEquals(goal.unit, restored.unit)
        assertEquals(1, restored.milestones.size)
        assertEquals("Book 1", restored.milestones[0].title)
        assertTrue(restored.milestones[0].done)
    }
}
