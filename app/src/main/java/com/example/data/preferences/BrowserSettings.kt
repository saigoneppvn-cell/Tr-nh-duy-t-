package com.example.data.preferences

enum class ThemePreset(val id: String) {
    OBSIDIAN("OBSIDIAN"),
    MIDNIGHT("MIDNIGHT"),
    AMOLED("AMOLED"),
    LIGHT("LIGHT");

    companion object {
        fun fromId(id: String?): ThemePreset = entries.firstOrNull { it.id == id } ?: OBSIDIAN
    }
}

enum class TrackingProtectionLevel(val id: String) {
    STRICT("STRICT"),
    STANDARD("STANDARD"),
    OFF("OFF");

    companion object {
        fun fromId(id: String?): TrackingProtectionLevel = entries.firstOrNull { it.id == id } ?: STRICT
    }
}

enum class SearchEngineId(
    val id: String,
    val displayName: String,
    val searchUrlTemplate: String,
    val homeUrl: String
) {
    GOOGLE("GOOGLE", "Google", "https://www.google.com/search?q=%s", "https://www.google.com"),
    DUCKDUCKGO("DUCKDUCKGO", "DuckDuckGo", "https://duckduckgo.com/?q=%s", "https://duckduckgo.com"),
    BING("BING", "Bing", "https://www.bing.com/search?q=%s", "https://www.bing.com"),
    BRAVE("BRAVE", "Brave Search", "https://search.brave.com/search?q=%s", "https://search.brave.com"),
    COCCOC("COCCOC", "Cốc Cốc", "https://coccoc.com/search?query=%s", "https://coccoc.com");

    companion object {
        fun fromId(id: String?): SearchEngineId = entries.firstOrNull { it.id == id } ?: GOOGLE
    }
}

enum class AppLanguage(val code: String, val displayName: String) {
    VI("vi", "Tiếng Việt"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: VI
    }
}

data class BrowserSettings(
    val themePreset: ThemePreset = ThemePreset.OBSIDIAN,
    val forceDarkWeb: Boolean = true,
    val showQuickBookmarksOnHome: Boolean = true,
    val bottomAddressBar: Boolean = false,
    val trackingProtection: TrackingProtectionLevel = TrackingProtectionLevel.STRICT,
    val javascriptEnabled: Boolean = true,
    val httpsOnly: Boolean = true,
    val trackersBlockedTotal: Long = 0L,
    val defaultDesktopMode: Boolean = false,
    val textZoomPercent: Int = 100,
    val askBeforeDownload: Boolean = false,
    val searchEngine: SearchEngineId = SearchEngineId.GOOGLE,
    val searchSuggestionsEnabled: Boolean = true,
    val openLinksInNewTab: Boolean = true,
    val restoreTabsOnLaunch: Boolean = true,
    val language: AppLanguage = AppLanguage.VI
)
