package com.example.arlo

import com.example.arlo.data.NoteImportExportManager
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import org.junit.Assert.*
import org.junit.Test

class NoteImportExportManagerTest {

    private val manager = NoteImportExportManager(null)

    @Test
    fun testParseMarkdownWithFrontmatter() {
        val markdown = """
            ---
            title: Morning Thoughts
            date: 2026-10-03
            mood: Grateful
            tags: [Mindfulness, Journal]
            ---
            # Deep reflection
            Today started with a crisp morning walk.
            - [ ] Meditate for 10 minutes
            - [x] Drink fresh water
            #focus
        """.trimIndent()

        val parsed = manager.parseContent(markdown, "Morning.md", "Obsidian")

        assertEquals("Morning Thoughts", parsed.title)
        assertEquals("Grateful", parsed.mood)
        assertEquals("2026-10-03", parsed.detectedDate)
        assertTrue(parsed.tags.contains("Mindfulness"))
        assertTrue(parsed.tags.contains("focus"))
        assertEquals(2, parsed.checklistItems.size)
        assertEquals("Meditate for 10 minutes", parsed.checklistItems[0])
        assertEquals("Drink fresh water", parsed.checklistItems[1])
        assertEquals("Obsidian", parsed.sourceApp)
    }

    @Test
    fun testExportToObsidianMarkdown() {
        val notes = listOf(
            Note(
                body = "A peaceful and productive day.",
                createdAt = "2026-10-03T10:00:00.000Z",
                prompt = "What brought you calm?",
                mood = "Calm",
                tags = listOf("Mindful")
            )
        )

        val exported = manager.exportReflectionsToObsidianMarkdown(notes)
        assertTrue(exported.contains("mood: \"Calm\""))
        assertTrue(exported.contains("A peaceful and productive day."))
        assertTrue(exported.contains("### Prompt: What brought you calm?"))
    }

    @Test
    fun testExportGoalsToMarkdown() {
        val goals = listOf(
            Goal(
                title = "Learn Kotlin Flow",
                detail = "Master reactive programming",
                milestones = listOf(Milestone(title = "Read docs", done = true)),
                done = false
            )
        )

        val exported = manager.exportGoalsToMarkdown(goals)
        assertTrue(exported.contains("Learn Kotlin Flow"))
        assertTrue(exported.contains("[x] Read docs"))
    }
}
