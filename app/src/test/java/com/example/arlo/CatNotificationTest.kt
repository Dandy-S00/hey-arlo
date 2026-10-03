package com.example.arlo

import com.example.arlo.notification.CatTone
import com.example.arlo.notification.ScheduledCatReminder
import org.junit.Assert.*
import org.junit.Test

class CatNotificationTest {

    @Test
    fun testCatToneQuipsAndIcons() {
        val tones = CatTone.values()
        assertEquals(4, tones.size)

        val gentle = CatTone.GENTLE_MEOW
        assertEquals("🐾", gentle.icon)
        assertTrue(gentle.quipPrefix.contains("Meow"))

        val biscuit = CatTone.BISCUIT_KNEADER
        assertEquals("🍪", biscuit.icon)
        assertTrue(biscuit.quipPrefix.contains("biscuits"))

        val zoomies = CatTone.FELINE_ZOOMIES
        assertEquals("⚡", zoomies.icon)
        assertTrue(zoomies.quipPrefix.contains("Zoomies"))

        val zen = CatTone.ZEN_LOAF
        assertEquals("🧘", zen.icon)
        assertTrue(zen.quipPrefix.contains("Loaf"))
    }

    @Test
    fun testScheduledCatReminderModel() {
        val reminder = ScheduledCatReminder(
            id = "test_rem_1",
            title = "🐾 Task Reminder: Review Vault",
            message = "Arlo is baking biscuits! Time to focus on: Review Vault",
            triggerTimeEpochMs = 1700000000000L,
            category = "Task",
            tone = CatTone.BISCUIT_KNEADER,
            playSound = true
        )

        assertEquals("test_rem_1", reminder.id)
        assertTrue(reminder.title.contains("Task Reminder"))
        assertTrue(reminder.message.contains("biscuits"))
        assertEquals("Task", reminder.category)
        assertEquals(CatTone.BISCUIT_KNEADER, reminder.tone)
        assertTrue(reminder.playSound)
    }
}
