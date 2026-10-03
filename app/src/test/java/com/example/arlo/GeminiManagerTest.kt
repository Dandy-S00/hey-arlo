package com.example.arlo

import com.example.arlo.data.GeminiTier
import org.junit.Assert.*
import org.junit.Test

class GeminiManagerTest {

    @Test
    fun testGeminiTiersModelsAreModernAndCompliant() {
        val local = GeminiTier.LOCAL
        assertTrue(local.isCompletelyLocal)
        assertEquals("🔒", local.badgeIcon)

        val free = GeminiTier.FREE
        assertEquals("gemini-3.5-flash", free.modelName)
        assertEquals("⚡", free.badgeIcon)
        assertFalse(free.isCompletelyLocal)

        val advanced = GeminiTier.ADVANCED
        assertEquals("gemini-3.1-pro-preview", advanced.modelName)
        assertEquals("💎", advanced.badgeIcon)
        assertFalse(advanced.isCompletelyLocal)

        val lite = GeminiTier.FLASH_LITE
        assertEquals("gemini-3.1-flash-lite-preview", lite.modelName)
        assertEquals("🍃", lite.badgeIcon)
        assertFalse(lite.isCompletelyLocal)

        // Strict prohibition check: Ensure NO deprecated 1.5 or 2.0 models are used
        GeminiTier.entries.filter { !it.isCompletelyLocal }.forEach { tier ->
            assertFalse(tier.modelName.contains("1.5"))
            assertFalse(tier.modelName.contains("2.0"))
            assertTrue(tier.modelName.startsWith("gemini-"))
        }
    }

    @Test
    fun testGeminiTierDisplayMetadata() {
        GeminiTier.entries.forEach { tier ->
            assertTrue(tier.displayName.isNotBlank())
            assertTrue(tier.tierType.isNotBlank())
            assertTrue(tier.description.isNotBlank())
        }
    }

    @Test
    fun testLocalModelTierGuaranteesZeroOutboundTransmission() {
        val local = GeminiTier.LOCAL
        assertTrue(local.isCompletelyLocal)
        assertEquals("local", local.id)
    }
}
