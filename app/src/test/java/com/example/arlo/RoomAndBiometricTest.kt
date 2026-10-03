package com.example.arlo

import com.example.arlo.data.EmailMessageItem
import com.example.arlo.data.SmsMessageItem
import com.example.arlo.data.db.GoalEntity
import com.example.arlo.data.db.ReflectionEntity
import com.example.arlo.data.db.TaskEntity
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import com.example.arlo.model.Task
import com.example.arlo.ui.components.ChartTimeWindow
import com.example.arlo.ui.components.SearchCategoryFilter
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class RoomAndBiometricTest {

    @Test
    fun testTaskEntityDomainConversion() {
        val task = Task(id = UUID.randomUUID().toString(), title = "Meditate for 10 minutes", done = true)
        val entity = TaskEntity.fromDomain(task)

        assertEquals(task.id, entity.id)
        assertEquals(task.title, entity.title)
        assertTrue(entity.done)

        val backToDomain = entity.toDomain()
        assertEquals(task.id, backToDomain.id)
        assertEquals(task.title, backToDomain.title)
        assertEquals(task.done, backToDomain.done)
    }

    @Test
    fun testGoalEntityDomainConversion() {
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            title = "Morning Sunlight Protocol",
            detail = "Walk outside within 30 mins",
            done = false,
            milestones = listOf(
                Milestone(title = "Step outside", done = true),
                Milestone(title = "10 min view", done = false)
            )
        )

        val entity = GoalEntity.fromDomain(goal)
        assertEquals(goal.id, entity.id)
        assertEquals(goal.title, entity.title)
        assertTrue(entity.milestonesJson.contains("Step outside"))

        val domain = entity.toDomain()
        assertEquals(2, domain.milestones.size)
        assertTrue(domain.milestones[0].done)
        assertFalse(domain.milestones[1].done)
    }

    @Test
    fun testReflectionEntityDomainConversion() {
        val note = Note(
            id = UUID.randomUUID().toString(),
            prompt = "Evening Wind-Down",
            body = "Calm evening with tea and reading.",
            mood = "Steady",
            createdAt = "2026-10-03T04:30:00Z"
        )

        val entity = ReflectionEntity.fromDomain(note)
        assertEquals(note.id, entity.id)
        assertEquals(note.body, entity.body)
        assertEquals(note.mood, entity.mood)

        val domain = entity.toDomain()
        assertEquals(note.id, domain.id)
        assertEquals(note.body, domain.body)
        assertEquals(note.mood, domain.mood)
        assertEquals(note.createdAt, domain.createdAt)
    }

    @Test
    fun testChartTimeWindowProperties() {
        assertEquals(7, ChartTimeWindow.PAST_7_DAYS.days)
        assertEquals(14, ChartTimeWindow.PAST_14_DAYS.days)
        assertEquals(30, ChartTimeWindow.ALL_TIME.days)
    }

    @Test
    fun testSearchCategoryFilters() {
        assertEquals(6, SearchCategoryFilter.entries.size)
        assertTrue(SearchCategoryFilter.entries.contains(SearchCategoryFilter.ALL))
        assertTrue(SearchCategoryFilter.entries.contains(SearchCategoryFilter.TASKS))
        assertTrue(SearchCategoryFilter.entries.contains(SearchCategoryFilter.GOALS))
        assertTrue(SearchCategoryFilter.entries.contains(SearchCategoryFilter.REFLECTIONS))
        assertTrue(SearchCategoryFilter.entries.contains(SearchCategoryFilter.MESSAGES))
    }

    @Test
    fun testSmsAndEmailModels() {
        val sms = SmsMessageItem(
            id = "sms-101",
            sender = "+1234567890",
            body = "Can you review the design sprint deck?",
            dateMs = 1700000000000L,
            dateFormatted = "Nov 14, 2:30 PM",
            isRead = false
        )
        assertEquals("sms-101", sms.id)
        assertEquals("+1234567890", sms.sender)
        assertFalse(sms.isRead)

        val email = EmailMessageItem(
            id = "em-101",
            sender = "mentor@stanford.edu",
            subject = "Research on Habit Architecture",
            snippet = "Great draft on friction reduction.",
            dateMs = 1700000000000L,
            dateFormatted = "Nov 14, 3:00 PM",
            isRead = true
        )
        assertEquals("em-101", email.id)
        assertTrue(email.isRead)
    }
}
