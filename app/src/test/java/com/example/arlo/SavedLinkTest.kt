package com.example.arlo

import com.example.arlo.model.SavedLink
import org.junit.Assert.*
import org.junit.Test

class SavedLinkTest {

    @Test
    fun testDomainExtraction() {
        val link1 = SavedLink(url = "https://developer.android.com/design/tokens")
        assertEquals("developer.android.com", link1.domain)

        val link2 = SavedLink(url = "http://github.com/my-repo")
        assertEquals("github.com", link2.domain)

        val link3 = SavedLink(url = "subdomain.example.org/path?query=1")
        assertEquals("subdomain.example.org", link3.domain)
    }

    @Test
    fun testDisplayTitleFallback() {
        val linkWithTitle = SavedLink(url = "https://example.com", title = "My Bookmark")
        assertEquals("My Bookmark", linkWithTitle.displayTitle)

        val linkWithoutTitle = SavedLink(url = "https://hubermanlab.com/episode", title = "")
        assertEquals("hubermanlab.com", linkWithoutTitle.displayTitle)
    }

    @Test
    fun testSerializationAndNotes() {
        val original = SavedLink(
            url = "https://kotlinlang.org",
            title = "Kotlin Documentation",
            notes = "Key reference for coroutines and flow operators.",
            tags = listOf("Kotlin", "Android", "Dev"),
            isFavorite = true,
            isRead = false,
            createdAt = "2026-10-03T10:00:00Z"
        )

        val json = original.toJsonObject()
        val parsed = SavedLink.fromJsonObject(json)

        assertEquals(original.id, parsed.id)
        assertEquals(original.url, parsed.url)
        assertEquals(original.title, parsed.title)
        assertEquals(original.notes, parsed.notes)
        assertEquals(original.tags, parsed.tags)
        assertTrue(parsed.isFavorite)
        assertFalse(parsed.isRead)
    }
}
