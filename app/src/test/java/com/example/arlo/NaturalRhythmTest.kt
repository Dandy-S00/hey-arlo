package com.example.arlo

import com.example.arlo.data.Chronotype
import com.example.arlo.data.NaturalRhythmProfile
import com.example.arlo.model.CuratedResource
import org.junit.Assert.*
import org.junit.Test

class NaturalRhythmTest {

    @Test
    fun testNaturalRhythmProfileDefaults() {
        val defaultProfile = NaturalRhythmProfile()
        assertTrue(defaultProfile.isAutoTuneEnabled)
        assertEquals(Chronotype.STEADY_RHYTHM, defaultProfile.chronotype)
        assertTrue(defaultProfile.adaptiveHeartbeatHours in 18..48)
        assertTrue(defaultProfile.activeWindowText.isNotBlank())
        assertTrue(defaultProfile.peakHourText.isNotBlank())
    }

    @Test
    fun testCuratedResourceJsonSerialization() {
        val resource = CuratedResource(
            title = "Ultradian 90-Minute Focus",
            sourceName = "Chronobiology Research",
            url = "https://hubermanlab.com",
            summary = "90 minute focus blocks",
            whyRelevantToYourPhase = "Helps maintain deep momentum without burnout",
            targetGoalCategory = "Habits & Routine",
            tags = listOf("Focus", "Biology"),
            isHiddenGem = true,
            recommendedPhase = "Active Execution"
        )

        val json = resource.toJsonObject()
        val parsed = CuratedResource.fromJsonObject(json)

        assertEquals(resource.title, parsed.title)
        assertEquals(resource.sourceName, parsed.sourceName)
        assertEquals(resource.url, parsed.url)
        assertEquals(resource.whyRelevantToYourPhase, parsed.whyRelevantToYourPhase)
        assertTrue(parsed.isHiddenGem)
    }

    @Test
    fun testChronotypesHaveDescriptionsAndIcons() {
        Chronotype.entries.forEach { c ->
            assertTrue(c.title.isNotBlank())
            assertTrue(c.icon.isNotBlank())
            assertTrue(c.description.isNotBlank())
        }
    }
}
