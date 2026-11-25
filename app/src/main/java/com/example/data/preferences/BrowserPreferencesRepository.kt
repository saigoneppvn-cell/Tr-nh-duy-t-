package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.browserDataStore: DataStore<Preferences> by preferencesDataStore(name = "meomeo_settings")

class BrowserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val FORCE_DARK_WEB = booleanPreferencesKey("force_dark_web")
        val SHOW_QUICK_BOOKMARKS = booleanPreferencesKey("show_quick_bookmarks")
        val BOTTOM_ADDRESS_BAR = booleanPreferencesKey("bottom_address_bar")
        val TRACKING_PROTECTION = stringPreferencesKey("tracking_protection")
        val JAVASCRIPT_ENABLED = booleanPreferencesKey("javascript_enabled")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val TRACKERS_BLOCKED_TOTAL = longPreferencesKey("trackers_blocked_total")
        val DEFAULT_DESKTOP_MODE = booleanPreferencesKey("default_desktop_mode")
        val TEXT_ZOOM_PERCENT = intPreferencesKey("text_zoom_percent")
        val ASK_BEFORE_DOWNLOAD = booleanPreferencesKey("ask_before_download")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val SEARCH_SUGGESTIONS = booleanPreferencesKey("search_suggestions")
        val OPEN_LINKS_NEW_TAB = booleanPreferencesKey("open_links_new_tab")
        val RESTORE_TABS_ON_LAUNCH = booleanPreferencesKey("restore_tabs_on_launch")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val settingsFlow: Flow<BrowserSettings> = context.browserDataStore.data.map { prefs ->
        BrowserSettings(
            themePreset = ThemePreset.fromId(prefs[Keys.THEME_PRESET]),
            forceDarkWeb = prefs[Keys.FORCE_DARK_WEB] ?: true,
            showQuickBookmarksOnHome = prefs[Keys.SHOW_QUICK_BOOKMARKS] ?: true,
            bottomAddressBar = prefs[Keys.BOTTOM_ADDRESS_BAR] ?: false,
            trackingProtection = TrackingProtectionLevel.fromId(prefs[Keys.TRACKING_PROTECTION]),
            javascriptEnabled = prefs[Keys.JAVASCRIPT_ENABLED] ?: true,
            httpsOnly = prefs[Keys.HTTPS_ONLY] ?: true,
            trackersBlockedTotal = prefs[Keys.TRACKERS_BLOCKED_TOTAL] ?: 0L,
            defaultDesktopMode = prefs[Keys.DEFAULT_DESKTOP_MODE] ?: false,
            textZoomPercent = (prefs[Keys.TEXT_ZOOM_PERCENT] ?: 100).coerceIn(80, 150),
            askBeforeDownload = prefs[Keys.ASK_BEFORE_DOWNLOAD] ?: false,
            searchEngine = SearchEngineId.fromId(prefs[Keys.SEARCH_ENGINE]),
            searchSuggestionsEnabled = prefs[Keys.SEARCH_SUGGESTIONS] ?: true,
            openLinksInNewTab = prefs[Keys.OPEN_LINKS_NEW_TAB] ?: true,
            restoreTabsOnLaunch = prefs[Keys.RESTORE_TABS_ON_LAUNCH] ?: true,
            language = AppLanguage.fromCode(prefs[Keys.APP_LANGUAGE])
        )
    }

    suspend fun setThemePreset(preset: ThemePreset) {
        context.browserDataStore.edit { it[Keys.THEME_PRESET] = preset.id }
    }

    suspend fun setForceDarkWeb(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.FORCE_DARK_WEB] = enabled }
    }

    suspend fun setShowQuickBookmarks(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.SHOW_QUICK_BOOKMARKS] = enabled }
    }

    suspend fun setBottomAddressBar(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.BOTTOM_ADDRESS_BAR] = enabled }
    }

    suspend fun setTrackingProtection(level: TrackingProtectionLevel) {
        context.browserDataStore.edit { it[Keys.TRACKING_PROTECTION] = level.id }
    }

    suspend fun setJavascriptEnabled(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.JAVASCRIPT_ENABLED] = enabled }
    }

    suspend fun setHttpsOnly(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.HTTPS_ONLY] = enabled }
    }

    suspend fun incrementTrackersBlocked(delta: Long = 1L) {
        context.browserDataStore.edit {
            val current = it[Keys.TRACKERS_BLOCKED_TOTAL] ?: 0L
            it[Keys.TRACKERS_BLOCKED_TOTAL] = current + delta
        }
    }

    suspend fun setDefaultDesktopMode(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.DEFAULT_DESKTOP_MODE] = enabled }
    }

    suspend fun setTextZoomPercent(percent: Int) {
        context.browserDataStore.edit { it[Keys.TEXT_ZOOM_PERCENT] = percent.coerceIn(80, 150) }
    }

    suspend fun setAskBeforeDownload(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.ASK_BEFORE_DOWNLOAD] = enabled }
    }

    suspend fun setSearchEngine(engine: SearchEngineId) {
        context.browserDataStore.edit { it[Keys.SEARCH_ENGINE] = engine.id }
    }

    suspend fun setSearchSuggestions(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.SEARCH_SUGGESTIONS] = enabled }
    }

    suspend fun setOpenLinksInNewTab(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.OPEN_LINKS_NEW_TAB] = enabled }
    }

    suspend fun setRestoreTabsOnLaunch(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.RESTORE_TABS_ON_LAUNCH] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.browserDataStore.edit { it[Keys.APP_LANGUAGE] = language.code }
    }

    suspend fun resetAll() {
        context.browserDataStore.edit { it.clear() }
    }
}
