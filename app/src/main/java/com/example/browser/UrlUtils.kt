package com.example.browser

import com.example.data.preferences.SearchEngineId
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UrlUtils {

    private val urlPattern = Regex(
        "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$|^https?://localhost(:\\d+)?(/.*)?$|^https?://\\d{1,3}(\\.\\d{1,3}){3}(:\\d+)?(/.*)?$|^about:.*$"
    )

    fun isLikelyUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) return false
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("about:", ignoreCase = true)
        ) {
            return true
        }
        return urlPattern.matches(trimmed)
    }

    fun resolveInputToUri(input: String, engine: SearchEngineId): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        return if (isLikelyUrl(trimmed)) {
            if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("about:", ignoreCase = true)
            ) {
                trimmed
            } else {
                "https://$trimmed"
            }
        } else {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            engine.searchUrlTemplate.replace("%s", encoded)
        }
    }

    fun extractHost(url: String): String {
        return try {
            val uri = URL(if (url.startsWith("http")) url else "https://$url")
            uri.host.removePrefix("www.")
        } catch (_: Exception) {
            url.take(32)
        }
    }

    fun guessFileName(url: String, contentDisposition: String?): String {
        if (!contentDisposition.isNullOrBlank()) {
            val match = Regex("filename\\*?=(?:UTF-8''|\"?)([^\";]+)\"?", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        val pathSegment = url.substringBefore("?").substringAfterLast("/", "")
        return if (pathSegment.isNotBlank() && pathSegment.contains(".")) {
            pathSegment
        } else {
            "meomeo_download_${System.currentTimeMillis() % 10000}.bin"
        }
    }

    suspend fun fetchSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2 || isLikelyUrl(trimmed)) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val endpoint = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=$encoded")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2500
                readTimeout = 2500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONArray(body)
                if (root.length() > 1) {
                    val items = root.getJSONArray(1)
                    buildList {
                        for (i in 0 until minOf(items.length(), 5)) {
                            add(items.getString(i))
                        }
                    }
                } else emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
