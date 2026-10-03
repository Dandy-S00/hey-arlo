package com.example.arlo

import com.example.arlo.model.ArloState
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Task
import com.example.arlo.model.GamificationState
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class CadenceReminderTest {

    @Test
    fun testGamificationStateDefaultsAndLevelProgression() {
        val defaultState = GamificationState()
        assertFalse(defaultState.isEnabled)
        assertEquals(1, defaultState.level)
        assertTrue(defaultState.tunaTreats > 0)
        assertTrue(defaultState.activeQuests.isNotEmpty())

        val json = defaultState.toJsonObject()
        val parsed = GamificationState.fromJsonObject(json)
        assertEquals(defaultState.level, parsed.level)
        assertEquals(defaultState.currentRank, parsed.currentRank)
        assertEquals(defaultState.equippedAccessory, parsed.equippedAccessory)
    }

    @Test
    fun testQuestsRewardValues() {
        val quests = GamificationState.defaultQuests()
        assertTrue(quests.size >= 3)
        quests.forEach { q ->
            assertTrue(q.xpReward > 0)
            assertTrue(q.treatsReward >= 1)
            assertTrue(q.title.isNotBlank())
        }
    }
}
