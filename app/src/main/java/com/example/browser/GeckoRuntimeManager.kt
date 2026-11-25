package com.example.browser

import android.content.Context
import com.example.data.preferences.BrowserSettings
import com.example.data.preferences.TrackingProtectionLevel
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.StorageController

class GeckoRuntimeManager(private val context: Context) {

    @Volatile
    private var runtime: GeckoRuntime? = null

    fun getOrCreateRuntime(initialSettings: BrowserSettings): GeckoRuntime {
        return runtime ?: synchronized(this) {
            runtime ?: buildRuntime(initialSettings).also { runtime = it }
        }
    }

    private fun buildRuntime(settings: BrowserSettings): GeckoRuntime {
        val cbSettings = buildContentBlocking(settings.trackingProtection)
        val runtimeSettings = GeckoRuntimeSettings.Builder()
            .contentBlocking(cbSettings)
            .preferredColorScheme(
                if (settings.forceDarkWeb) GeckoRuntimeSettings.COLOR_SCHEME_DARK
                else GeckoRuntimeSettings.COLOR_SCHEME_LIGHT
            )
            .automaticFontSizeAdjustment(false)
            .fontSizeFactor(settings.textZoomPercent / 100f)
            .allowInsecureConnections(
                if (settings.httpsOnly) GeckoRuntimeSettings.HTTPS_ONLY
                else GeckoRuntimeSettings.ALLOW_ALL
            )
            .aboutConfigEnabled(false)
            .consoleOutput(false)
            .build()

        return GeckoRuntime.create(context.applicationContext, runtimeSettings)
    }

    fun applySettings(settings: BrowserSettings) {
        val rt = runtime ?: return
        val rtSettings = rt.settings
        rtSettings.preferredColorScheme = if (settings.forceDarkWeb) {
            GeckoRuntimeSettings.COLOR_SCHEME_DARK
        } else {
            GeckoRuntimeSettings.COLOR_SCHEME_LIGHT
        }
        rtSettings.automaticFontSizeAdjustment = false
        rtSettings.fontSizeFactor = settings.textZoomPercent / 100f
        rtSettings.allowInsecureConnections = if (settings.httpsOnly) {
            GeckoRuntimeSettings.HTTPS_ONLY
        } else {
            GeckoRuntimeSettings.ALLOW_ALL
        }

        val cb = rtSettings.contentBlocking
        when (settings.trackingProtection) {
            TrackingProtectionLevel.STRICT -> {
                cb.setEnhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
                cb.setAntiTracking(
                    ContentBlocking.AntiTracking.DEFAULT or
                        ContentBlocking.AntiTracking.STRICT or
                        ContentBlocking.AntiTracking.STP
                )
                cb.setCookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
            }
            TrackingProtectionLevel.STANDARD -> {
                cb.setEnhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.DEFAULT)
                cb.setAntiTracking(ContentBlocking.AntiTracking.DEFAULT)
                cb.setCookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
            }
            TrackingProtectionLevel.OFF -> {
                cb.setEnhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.NONE)
                cb.setAntiTracking(ContentBlocking.AntiTracking.NONE)
                cb.setCookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_ALL)
            }
        }
    }

    private fun buildContentBlocking(level: TrackingProtectionLevel): ContentBlocking.Settings {
        val builder = ContentBlocking.Settings.Builder()
            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
        return when (level) {
            TrackingProtectionLevel.STRICT -> builder
                .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
                .antiTracking(
                    ContentBlocking.AntiTracking.DEFAULT or
                        ContentBlocking.AntiTracking.STRICT or
                        ContentBlocking.AntiTracking.STP
                )
                .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                .cookieBehaviorPrivateMode(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                .build()
            TrackingProtectionLevel.STANDARD -> builder
                .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.DEFAULT)
                .antiTracking(ContentBlocking.AntiTracking.DEFAULT)
                .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                .cookieBehaviorPrivateMode(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                .build()
            TrackingProtectionLevel.OFF -> builder
                .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.NONE)
                .antiTracking(ContentBlocking.AntiTracking.NONE)
                .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_ALL)
                .cookieBehaviorPrivateMode(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                .build()
        }
    }

    fun createSession(
        isPrivate: Boolean,
        desktopMode: Boolean,
        javascriptEnabled: Boolean
    ): GeckoSession {
        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(isPrivate)
            .userAgentMode(
                if (desktopMode) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            )
            .viewportMode(
                if (desktopMode) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                else GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            )
            .allowJavascript(javascriptEnabled)
            .build()
        return GeckoSession(sessionSettings)
    }

    fun clearCache(): GeckoResult<Void>? {
        return runtime?.storageController?.clearData(StorageController.ClearFlags.ALL_CACHES)
    }

    fun clearSiteData(): GeckoResult<Void>? {
        return runtime?.storageController?.clearData(
            StorageController.ClearFlags.SITE_DATA or
                StorageController.ClearFlags.COOKIES or
                StorageController.ClearFlags.AUTH_SESSIONS
        )
    }

    fun clearAllBrowserData(): GeckoResult<Void>? {
        return runtime?.storageController?.clearData(StorageController.ClearFlags.ALL)
    }
}
