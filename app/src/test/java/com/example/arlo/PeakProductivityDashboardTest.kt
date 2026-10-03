package com.example.arlo

import com.example.arlo.data.Chronotype
import com.example.arlo.data.NaturalRhythmProfile
import com.example.arlo.model.ArloState
import com.example.arlo.model.Task
import com.example.arlo.ui.components.CurrentRhythmPhase
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class PeakProductivityDashboardTest {

    @Test
    fun testRhythmPhaseEnumProperties() {
        val peak = CurrentRhythmPhase.PEAK_FOCUS
        assertTrue(peak.isPeak)
        assertTrue(peak.badgeIcon.isNotBlank())
        assertTrue(peak.title.contains("PEAK FOCUS"))

        val windDown = CurrentRhythmPhase.WIND_DOWN_REST
        assertFalse(windDown.isPeak)
        assertTrue(windDown.title.contains("WIND-DOWN"))

        val warmup = CurrentRhythmPhase.PRE_PEAK_WARMUP
        assertFalse(warmup.isPeak)

        val steady = CurrentRhythmPhase.STEADY_PACING
        assertFalse(steady.isPeak)
    }

    @Test
    fun testUrgentTaskSurfacing() {
        val tasks = listOf(
            Task(id = "1", title = "Write critical module", done = false),
            Task(id = "2", title = "Completed email", done = true),
            Task(id = "3", title = "Review PR", done = false)
        )
        val state = ArloState(tasks = tasks)
        val urgent = state.tasks.filter { !it.done }

        assertEquals(2, urgent.size)
        assertEquals("Write critical module", urgent.first().title)
    }

    @Test
    fun testNaturalRhythmProfileActiveWindowCalculations() {
        val profile = NaturalRhythmProfile(
            learnedWakeHour = 7,
            learnedSleepHour = 23,
            peakFocusHour = 9,
            chronotype = Chronotype.EARLY_LARK
        )

        assertEquals("7:00 AM – 11:00 PM", profile.activeWindowText)
        assertEquals("9:00 AM – 11:00 AM", profile.peakHourText)
    }
}
