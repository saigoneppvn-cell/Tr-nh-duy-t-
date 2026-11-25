package com.example

import com.example.browser.UrlUtils
import com.example.data.preferences.SearchEngineId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun isLikelyUrl_recognizesDomainsAndUrls() {
        assertTrue(UrlUtils.isLikelyUrl("wikipedia.org"))
        assertTrue(UrlUtils.isLikelyUrl("https://mozilla.org/en-US/"))
        assertTrue(UrlUtils.isLikelyUrl("http://localhost:8080"))
        assertFalse(UrlUtils.isLikelyUrl("meo meo browser"))
        assertFalse(UrlUtils.isLikelyUrl("geckoview"))
    }

    @Test
    fun resolveInputToUri_normalizesUrlsAndBuildsSearchQueries() {
        assertEquals(
            "https://example.com",
            UrlUtils.resolveInputToUri("example.com", SearchEngineId.GOOGLE)
        )
        assertEquals(
            "https://duckduckgo.com/?q=kotlin+compose",
            UrlUtils.resolveInputToUri("kotlin compose", SearchEngineId.DUCKDUCKGO)
        )
    }

    @Test
    fun extractHost_stripsWwwAndScheme() {
        assertEquals("mozilla.org", UrlUtils.extractHost("https://www.mozilla.org/firefox"))
    }

    @Test
    fun guessFileName_usesContentDispositionOrUrlPath() {
        assertEquals(
            "report.pdf",
            UrlUtils.guessFileName("https://example.com/dl", "attachment; filename=\"report.pdf\"")
        )
        assertEquals(
            "archive.zip",
            UrlUtils.guessFileName("https://example.com/files/archive.zip?token=123", null)
        )
    }
}
