package com.example.arlo

import com.example.arlo.model.ArloState
import com.example.arlo.model.CuratedResource
import org.junit.Assert.*
import org.junit.Test

class ResourceIntelligenceTest {

    @Test
    fun testCuratedResourceAttributes() {
        val resource = CuratedResource(
            title = "Ultradian 90-Minute Focus Protocol",
            sourceName = "Chronobiology Research",
            url = "https://hubermanlab.com/toolkit-for-focus-and-productivity",
            summary = "Operate in 90-minute biological focus cycles.",
            whyRelevantToYourPhase = "Maximizes daily deep momentum without cognitive fatigue.",
            targetGoalCategory = "Habits & Routine",
            tags = listOf("Focus", "Biology"),
            isHiddenGem = true,
            recommendedPhase = "Active Execution"
        )

        assertEquals("Ultradian 90-Minute Focus Protocol", resource.title)
        assertTrue(resource.isHiddenGem)
        assertEquals("Active Execution", resource.recommendedPhase)
        assertFalse(resource.isSavedToVault)

        val json = resource.toJsonObject()
        val parsed = CuratedResource.fromJsonObject(json)
        assertEquals(resource.id, parsed.id)
        assertEquals(resource.title, parsed.title)
        assertEquals(resource.whyRelevantToYourPhase, parsed.whyRelevantToYourPhase)
        assertTrue(parsed.isHiddenGem)
    }

    @Test
    fun testCuratedResourceSerializationWithoutStreak() {
        val resource = CuratedResource(
            title = "The Friction Inversion Canvas",
            sourceName = "Behavioral Design",
            url = "https://jamesclear.com",
            summary = "Remove friction from key actions.",
            whyRelevantToYourPhase = "Overcomes initial inertia on stalled steps.",
            targetGoalCategory = "Habits & Routine"
        )

        val jsonString = resource.toJsonObject().toString()
        assertFalse(jsonString.contains("streak", ignoreCase = true))
    }
}
