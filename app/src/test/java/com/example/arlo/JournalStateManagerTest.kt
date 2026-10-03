package com.example.arlo

import com.example.arlo.model.Note
import org.junit.Assert.*
import org.junit.Test

class JournalStateManagerTest {

    @Test
    fun testNoteWithReflectionMetadata() {
        val note = Note(
            body = "Today I felt a deep sense of calm walking outside.",
            createdAt = "2026-10-03T08:30:00.000Z",
            prompt = "What brought you calm or joy?",
            mood = "Calm",
            tags = listOf("Mindfulness", "Nature")
        )

        val json = note.toJsonObject()
        val restored = Note.fromJsonObject(json)

        assertEquals(note.body, restored.body)
        assertEquals(note.createdAt, restored.createdAt)
        assertEquals(note.prompt, restored.prompt)
        assertEquals(note.mood, restored.mood)
        assertEquals(2, restored.tags.size)
        assertTrue(restored.tags.contains("Nature"))
    }

    @Test
    fun testReflectionPromptPresets() {
        val prompts = listOf(
            "What went well today?",
            "What brought you calm or joy?",
            "What is one thing you learned?"
        )
        assertTrue(prompts.isNotEmpty())
        assertEquals("What went well today?", prompts[0])
    }
}
