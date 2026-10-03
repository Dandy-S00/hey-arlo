package com.example.arlo

import com.example.arlo.ui.components.CatSayings
import org.junit.Assert.*
import org.junit.Test

class CatSayingsTest {

    @Test
    fun testWorkingSayingsQuality() {
        val working = CatSayings.WORKING_SAYINGS
        assertTrue("Working sayings should have multiple options", working.size >= 8)
        working.forEach { saying ->
            assertTrue("Saying should not be blank", saying.isNotBlank())
            assertTrue("Saying should have reasonable length", saying.length in 10..100)
        }
    }

    @Test
    fun testDoneSayingsQuality() {
        val done = CatSayings.DONE_SAYINGS
        assertTrue("Done sayings should have multiple options", done.size >= 5)
        done.forEach { saying ->
            assertTrue("Saying should not be blank", saying.isNotBlank())
            assertTrue("Saying should have reasonable length", saying.length in 10..100)
        }
    }
}
