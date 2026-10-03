package com.example.arlo

import com.example.arlo.data.AudioStructuredNote
import com.example.arlo.data.VideoTranscriptionResult
import com.example.arlo.model.GamificationState
import com.example.arlo.model.Task
import com.example.arlo.model.Goal
import com.example.arlo.model.Note
import com.example.arlo.security.VaultCrypto
import org.junit.Assert.*
import org.junit.Test

class EliteAppValidationTest {

    @Test
    fun testAudioStructuredNoteDataModel() {
        val note = AudioStructuredNote(
            title = "Strategy Call",
            summary = "Discussed sprint milestones and release deadlines.",
            fullTranscript = "Let's align on delivering the new companion voice model by Friday.",
            actionItems = listOf("Deliver companion voice model", "Review security audit"),
            sentiment = "Focused",
            durationSeconds = 120,
            recordingType = "Phone Call & Conversation"
        )

        assertEquals("Strategy Call", note.title)
        assertEquals(2, note.actionItems.size)
        assertEquals("Focused", note.sentiment)
        assertEquals(120, note.durationSeconds)
        assertEquals("Phone Call & Conversation", note.recordingType)
    }

    @Test
    fun testVideoTranscriptionResultDataModel() {
        val video = VideoTranscriptionResult(
            videoUrl = "https://www.youtube.com/watch?v=sample123",
            videoTitle = "Mastering Productivity with Feline Focus",
            channelOrSource = "YouTube",
            summary = "A deep dive into intentional habit design and energy rhythms.",
            fullTranscript = "Welcome back to the channel. Today we explore deep focus sprints.",
            keyTakeaways = listOf("Use 90-minute focus blocks", "Rest with mindful intention"),
            actionItems = listOf("Schedule 90-minute morning sprint"),
            estimatedDuration = "14:20",
            tags = listOf("productivity", "habits", "focus")
        )

        assertEquals("Mastering Productivity with Feline Focus", video.videoTitle)
        assertEquals("YouTube", video.channelOrSource)
        assertEquals(2, video.keyTakeaways.size)
        assertEquals(1, video.actionItems.size)
        assertEquals(3, video.tags.size)
    }

    @Test
    fun testEncryptionRoundtripResilience() {
        val sampleJson = """{"version":1,"avatar":"🐾","tasks":[{"id":"1","title":"Ship Arlo","done":true}]}"""
        val passphrase = "ultra_secure_passphrase_2026".toCharArray()

        val envelope = VaultCrypto.encrypt(sampleJson, passphrase)
        assertNotNull(envelope)
        assertTrue(envelope.startsWith("arlo-vault-v1:"))

        val decrypted = VaultCrypto.decrypt(envelope, passphrase)
        assertEquals(sampleJson, decrypted)
    }

    @Test
    fun testGamificationAndPetTreatModel() {
        val gamification = GamificationState()
        val nextState = gamification.copy(
            tunaTreats = gamification.tunaTreats + 5,
            currentXp = gamification.currentXp + 50
        )
        assertEquals(gamification.tunaTreats + 5, nextState.tunaTreats)
        assertEquals(gamification.currentXp + 50, nextState.currentXp)
    }

    @Test
    fun testModelsSerializationIntegrity() {
        val task = Task(title = "Test Task", done = false)
        val taskJson = task.toJsonObject()
        val parsedTask = Task.fromJsonObject(taskJson)
        assertEquals(task.title, parsedTask.title)
        assertEquals(task.done, parsedTask.done)

        val note = Note(body = "Daily meditation note", createdAt = "2026-10-03T10:00:00Z", mood = "Reflective")
        val noteJson = note.toJsonObject()
        val parsedNote = Note.fromJsonObject(noteJson)
        assertEquals(note.body, parsedNote.body)
        assertEquals(note.mood, parsedNote.mood)
    }
}
