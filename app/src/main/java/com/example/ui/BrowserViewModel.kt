package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MeoMeoApplication
import com.example.browser.UrlUtils
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.SavedTabEntity
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.BrowserSettings
import com.example.data.preferences.SearchEngineId
import com.example.data.preferences.ThemePreset
import com.example.data.preferences.TrackingProtectionLevel
import com.example.data.strings.AppStrings
import com.example.data.strings.stringsFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.WebResponse
import java.net.URLEncoder
import java.util.UUID

enum class AppScreen {
    HOME,
    BROWSER,
    SETTINGS,
    BOOKMARKS,
    DOWNLOADS,
    TABS
}

enum class SettingsCategory {
    APPEARANCE,
    PRIVACY,
    TOOLS,
    SEARCH,
    TABS,
    STORAGE,
    LANGUAGE,
    ABOUT
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "MeoMeo",
    val url: String = "",
    val isHome: Boolean = true,
    val isPrivate: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val securityHost: String = "",
    val blockedTrackersOnPage: Int = 0,
    val desktopMode: Boolean = false,
    val isFullscreen: Boolean = false
)

sealed interface WebPromptState {
    data class Alert(
        val title: String,
        val message: String,
        val prompt: GeckoSession.PromptDelegate.AlertPrompt,
        val result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) : WebPromptState

    data class Confirm(
        val title: String,
        val message: String,
        val prompt: GeckoSession.PromptDelegate.ButtonPrompt,
        val result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) : WebPromptState

    data class TextInput(
        val title: String,
        val message: String,
        val defaultValue: String,
        val prompt: GeckoSession.PromptDelegate.TextPrompt,
        val result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) : WebPromptState

    data class Choice(
        val title: String,
        val choices: Array<GeckoSession.PromptDelegate.ChoicePrompt.Choice>,
        val multiple: Boolean,
        val prompt: GeckoSession.PromptDelegate.ChoicePrompt,
        val result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) : WebPromptState
}

data class WebPermissionPrompt(
    val host: String,
    val permissionType: Int,
    val permission: GeckoSession.PermissionDelegate.ContentPermission,
    val result: GeckoResult<Int>
)

data class PendingAndroidPermission(
    val permissions: Array<String>,
    val callback: GeckoSession.PermissionDelegate.Callback
)

data class ContextMenuState(
    val linkUri: String?,
    val sourceUri: String?,
    val title: String?
)

data class PendingDownloadConfirmation(
    val url: String,
    val fileName: String,
    val mimeType: String?,
    val userAgent: String?
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MeoMeoApplication
    private val prefsRepo = app.preferencesRepository
    private val browserRepo = app.browserRepository
    private val geckoManager = app.geckoRuntimeManager
    private val downloadHelper = app.downloadHelper

    val settings: StateFlow<BrowserSettings> = prefsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, BrowserSettings())

    val strings: StateFlow<AppStrings> = settings
        .map { stringsFor(it.language) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, stringsFor(AppLanguage.VI))

    val bookmarks: StateFlow<List<BookmarkEntity>> = browserRepo.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = browserRepo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = browserRepo.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _tabs = MutableStateFlow(listOf(BrowserTab()))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _activeSettingsCategory = MutableStateFlow<SettingsCategory?>(null)
    val activeSettingsCategory: StateFlow<SettingsCategory?> = _activeSettingsCategory.asStateFlow()

    private val _showQuickMenu = MutableStateFlow(false)
    val showQuickMenu: StateFlow<Boolean> = _showQuickMenu.asStateFlow()

    private val _showFindInPage = MutableStateFlow(false)
    val showFindInPage: StateFlow<Boolean> = _showFindInPage.asStateFlow()

    private val _webPrompt = MutableStateFlow<WebPromptState?>(null)
    val webPrompt: StateFlow<WebPromptState?> = _webPrompt.asStateFlow()

    private val _webPermissionPrompt = MutableStateFlow<WebPermissionPrompt?>(null)
    val webPermissionPrompt: StateFlow<WebPermissionPrompt?> = _webPermissionPrompt.asStateFlow()

    private val _contextMenu = MutableStateFlow<ContextMenuState?>(null)
    val contextMenu: StateFlow<ContextMenuState?> = _contextMenu.asStateFlow()

    private val _pendingDownload = MutableStateFlow<PendingDownloadConfirmation?>(null)
    val pendingDownload: StateFlow<PendingDownloadConfirmation?> = _pendingDownload.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    private val _androidPermissionRequests = MutableSharedFlow<PendingAndroidPermission>(extraBufferCapacity = 4)
    val androidPermissionRequests: SharedFlow<PendingAndroidPermission> = _androidPermissionRequests.asSharedFlow()

    private val _filePickerRequests = MutableSharedFlow<Pair<GeckoSession.PromptDelegate.FilePrompt, GeckoResult<GeckoSession.PromptDelegate.PromptResponse>>>(extraBufferCapacity = 2)
    val filePickerRequests = _filePickerRequests.asSharedFlow()

    private val sessionMap = LinkedHashMap<String, GeckoSession>()
    private var suggestionsJob: Job? = null

    init {
        viewModelScope.launch {
            val initialSettings = prefsRepo.settingsFlow.first()
            geckoManager.getOrCreateRuntime(initialSettings)

            if (initialSettings.restoreTabsOnLaunch) {
                val saved = browserRepo.getSavedTabs()
                if (saved.isNotEmpty()) {
                    val restored = saved.map {
                        BrowserTab(
                            id = it.id,
                            title = it.title,
                            url = it.url,
                            isHome = it.isHome,
                            desktopMode = initialSettings.defaultDesktopMode
                        )
                    }
                    _tabs.value = restored
                    val active = saved.firstOrNull { it.isActive }?.id ?: restored.first().id
                    _activeTabId.value = active
                    val activeTab = restored.firstOrNull { it.id == active }
                    if (activeTab != null && !activeTab.isHome && activeTab.url.isNotBlank()) {
                        ensureSessionForTab(activeTab).loadUri(activeTab.url)
                        _currentScreen.value = AppScreen.BROWSER
                    }
                }
            }
        }

        viewModelScope.launch {
            settings.collect { currentSettings ->
                geckoManager.applySettings(currentSettings)
                sessionMap.forEach { (tabId, session) ->
                    val tab = _tabs.value.firstOrNull { it.id == tabId }
                    session.settings.allowJavascript = currentSettings.javascriptEnabled
                    if (tab != null) {
                        session.settings.userAgentMode =
                            if (tab.desktopMode) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                            else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
                    }
                }
            }
        }
    }

    fun activeTab(): BrowserTab {
        val currentList = _tabs.value
        return currentList.firstOrNull { it.id == _activeTabId.value } ?: currentList.first()
    }

    fun getOrCreateSession(tabId: String): GeckoSession {
        val tab = _tabs.value.firstOrNull { it.id == tabId } ?: activeTab()
        return ensureSessionForTab(tab)
    }

    private fun ensureSessionForTab(tab: BrowserTab): GeckoSession {
        sessionMap[tab.id]?.let { return it }
        val currentSettings = settings.value
        val runtime = geckoManager.getOrCreateRuntime(currentSettings)
        val session = geckoManager.createSession(
            isPrivate = tab.isPrivate,
            desktopMode = tab.desktopMode,
            javascriptEnabled = currentSettings.javascriptEnabled
        )
        attachDelegates(tab.id, session)
        session.open(runtime)
        sessionMap[tab.id] = session
        return session
    }

    private fun attachDelegates(tabId: String, session: GeckoSession) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url.isNullOrBlank()) return
                val isSecure = url.startsWith("https://", ignoreCase = true)
                val host = UrlUtils.extractHost(url)
                updateTab(tabId) {
                    it.copy(
                        url = url,
                        isHome = false,
                        isSecure = isSecure,
                        securityHost = host,
                        blockedTrackersOnPage = 0
                    )
                }
                persistTabsSnapshot()
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                updateTab(tabId) { it.copy(canGoBack = canGoBack) }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                updateTab(tabId) { it.copy(canGoForward = canGoForward) }
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny> {
                val uri = request.uri
                val lower = uri.lowercase()
                if (lower.startsWith("tel:") || lower.startsWith("mailto:") ||
                    lower.startsWith("sms:") || lower.startsWith("geo:") ||
                    lower.startsWith("intent:")
                ) {
                    launchExternalUri(uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession> {
                val isPrivate = _tabs.value.firstOrNull { it.id == tabId }?.isPrivate ?: false
                val newTab = BrowserTab(
                    title = UrlUtils.extractHost(uri),
                    url = uri,
                    isHome = false,
                    isPrivate = isPrivate,
                    desktopMode = settings.value.defaultDesktopMode
                )
                _tabs.update { it + newTab }
                _activeTabId.value = newTab.id
                val newSession = ensureSessionForTab(newTab)
                _currentScreen.value = AppScreen.BROWSER
                persistTabsSnapshot()
                return GeckoResult.fromValue(newSession)
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                updateTab(tabId) {
                    it.copy(
                        url = url,
                        isHome = false,
                        isLoading = true,
                        progress = 10
                    )
                }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                val tab = _tabs.value.firstOrNull { it.id == tabId }
                updateTab(tabId) { it.copy(isLoading = false, progress = 100) }
                if (success && tab != null && !tab.isPrivate && tab.url.startsWith("http")) {
                    viewModelScope.launch {
                        browserRepo.recordHistory(tab.title, tab.url)
                    }
                }
                persistTabsSnapshot()
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                updateTab(tabId) {
                    it.copy(progress = progress, isLoading = progress in 1..99)
                }
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) {
                updateTab(tabId) {
                    it.copy(
                        isSecure = securityInfo.isSecure,
                        securityHost = securityInfo.host ?: it.securityHost
                    )
                }
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) {
                    updateTab(tabId) { it.copy(title = title) }
                    persistTabsSnapshot()
                }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                updateTab(tabId) { it.copy(isFullscreen = fullScreen) }
            }

            override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                val uri = response.uri
                val disposition = response.headers["content-disposition"]
                val contentType = response.headers["content-type"]
                val fileName = UrlUtils.guessFileName(uri, disposition)
                val ua = response.headers["user-agent"]

                if (settings.value.askBeforeDownload) {
                    _pendingDownload.value = PendingDownloadConfirmation(
                        url = uri,
                        fileName = fileName,
                        mimeType = contentType,
                        userAgent = ua
                    )
                } else {
                    startRealDownload(uri, fileName, contentType, ua)
                }
            }

            override fun onContextMenu(
                session: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement
            ) {
                if (!element.linkUri.isNullOrBlank() || !element.srcUri.isNullOrBlank()) {
                    _contextMenu.value = ContextMenuState(
                        linkUri = element.linkUri,
                        sourceUri = element.srcUri,
                        title = element.title ?: element.altText
                    )
                }
            }
        }

        session.contentBlockingDelegate = object : ContentBlocking.Delegate {
            override fun onContentBlocked(
                session: GeckoSession,
                event: ContentBlocking.BlockEvent
            ) {
                updateTab(tabId) {
                    it.copy(blockedTrackersOnPage = it.blockedTrackersOnPage + 1)
                }
                viewModelScope.launch {
                    prefsRepo.incrementTrackersBlocked(1L)
                }
            }
        }

        session.permissionDelegate = object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                perm: GeckoSession.PermissionDelegate.ContentPermission
            ): GeckoResult<Int> {
                val result = GeckoResult<Int>()
                val host = UrlUtils.extractHost(perm.uri)
                _webPermissionPrompt.value = WebPermissionPrompt(
                    host = host,
                    permissionType = perm.permission,
                    permission = perm,
                    result = result
                )
                return result
            }

            override fun onAndroidPermissionsRequest(
                session: GeckoSession,
                permissions: Array<out String>?,
                callback: GeckoSession.PermissionDelegate.Callback
            ) {
                if (permissions.isNullOrEmpty()) {
                    callback.grant()
                    return
                }
                _androidPermissionRequests.tryEmit(
                    PendingAndroidPermission(
                        permissions = permissions.map { it }.toTypedArray(),
                        callback = callback
                    )
                )
            }
        }

        session.promptDelegate = object : GeckoSession.PromptDelegate {
            override fun onAlertPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AlertPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                _webPrompt.value = WebPromptState.Alert(
                    title = prompt.title ?: "MeoMeo",
                    message = prompt.message ?: "",
                    prompt = prompt,
                    result = result
                )
                return result
            }

            override fun onButtonPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ButtonPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                _webPrompt.value = WebPromptState.Confirm(
                    title = prompt.title ?: "MeoMeo",
                    message = prompt.message ?: "",
                    prompt = prompt,
                    result = result
                )
                return result
            }

            override fun onTextPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.TextPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                _webPrompt.value = WebPromptState.TextInput(
                    title = prompt.title ?: "MeoMeo",
                    message = prompt.message ?: "",
                    defaultValue = prompt.defaultValue ?: "",
                    prompt = prompt,
                    result = result
                )
                return result
            }

            override fun onChoicePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ChoicePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                _webPrompt.value = WebPromptState.Choice(
                    title = prompt.title ?: "Chọn",
                    choices = prompt.choices,
                    multiple = prompt.type == GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE,
                    prompt = prompt,
                    result = result
                )
                return result
            }

            override fun onFilePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.FilePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                _filePickerRequests.tryEmit(prompt to result)
                return result
            }
        }
    }

    private fun launchExternalUri(uri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(intent)
        } catch (_: Exception) {
            showToast("Không thể mở liên kết ngoài")
        }
    }

    private fun updateTab(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _tabs.update { list ->
            list.map { if (it.id == tabId) transform(it) else it }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.HOME) {
            val current = activeTab()
            if (_currentScreen.value == AppScreen.BROWSER && !current.isHome) {
                // Switch current tab back to Home view or stay on Home screen
                updateTab(current.id) { it.copy(isHome = true, isLoading = false) }
            }
        }
        _currentScreen.value = screen
    }

    fun submitInput(rawInput: String, openInNewTabOverride: Boolean = false) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            showToast(strings.value.enterQueryPrompt)
            return
        }
        _searchSuggestions.value = emptyList()
        val resolvedUrl = UrlUtils.resolveInputToUri(trimmed, settings.value.searchEngine)
        openUrl(resolvedUrl, forceNewTab = openInNewTabOverride)
    }

    fun openUrl(url: String, forceNewTab: Boolean = false, privateTab: Boolean = false) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return

        val current = activeTab()
        val shouldCreateTab = forceNewTab || (settings.value.openLinksInNewTab && !current.isHome && _currentScreen.value != AppScreen.BROWSER)

        val targetTab = if (shouldCreateTab) {
            val created = BrowserTab(
                title = UrlUtils.extractHost(cleanUrl),
                url = cleanUrl,
                isHome = false,
                isPrivate = privateTab || current.isPrivate,
                desktopMode = settings.value.defaultDesktopMode
            )
            _tabs.update { it + created }
            _activeTabId.value = created.id
            created
        } else {
            val updated = current.copy(
                url = cleanUrl,
                title = UrlUtils.extractHost(cleanUrl),
                isHome = false
            )
            updateTab(current.id) { updated }
            updated
        }

        val session = ensureSessionForTab(targetTab)
        session.loadUri(cleanUrl)
        _currentScreen.value = AppScreen.BROWSER
        persistTabsSnapshot()
    }

    fun updateSearchQueryForSuggestions(query: String) {
        suggestionsJob?.cancel()
        if (!settings.value.searchSuggestionsEnabled || query.trim().length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }
        suggestionsJob = viewModelScope.launch {
            delay(180)
            _searchSuggestions.value = UrlUtils.fetchSearchSuggestions(query)
        }
    }

    fun clearSuggestions() {
        suggestionsJob?.cancel()
        _searchSuggestions.value = emptyList()
    }

    fun goBack(): Boolean {
        val current = activeTab()
        if (_currentScreen.value == AppScreen.BROWSER) {
            if (current.isFullscreen) {
                sessionMap[current.id]?.exitFullScreen()
                return true
            }
            if (current.canGoBack) {
                sessionMap[current.id]?.goBack()
                return true
            }
            updateTab(current.id) { it.copy(isHome = true) }
            _currentScreen.value = AppScreen.HOME
            return true
        }
        if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = if (current.isHome) AppScreen.HOME else AppScreen.BROWSER
            return true
        }
        return false
    }

    fun goForward() {
        val current = activeTab()
        if (current.canGoForward) {
            sessionMap[current.id]?.goForward()
        }
    }

    fun reloadOrStop() {
        val current = activeTab()
        val session = sessionMap[current.id] ?: return
        if (current.isLoading) {
            session.stop()
        } else {
            session.reload()
        }
    }

    fun toggleDesktopMode() {
        val current = activeTab()
        val nextDesktop = !current.desktopMode
        updateTab(current.id) { it.copy(desktopMode = nextDesktop) }
        sessionMap[current.id]?.let { session ->
            session.settings.userAgentMode =
                if (nextDesktop) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            session.settings.viewportMode =
                if (nextDesktop) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                else GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            session.reload()
        }
        showToast(if (nextDesktop) "Desktop Mode: ON" else "Mobile Mode: ON")
    }

    fun toggleCurrentBookmark() {
        val current = activeTab()
        if (current.url.isBlank() || current.isHome) return
        viewModelScope.launch {
            val added = browserRepo.toggleBookmark(current.title, current.url)
            showToast(if (added) strings.value.bookmarkAdded else strings.value.bookmarkRemoved)
        }
    }

    fun addBookmarkManual(title: String, url: String) {
        val resolved = UrlUtils.resolveInputToUri(url, settings.value.searchEngine)
        if (resolved.isBlank()) return
        viewModelScope.launch {
            browserRepo.addBookmark(title.ifBlank { UrlUtils.extractHost(resolved) }, resolved)
            showToast(strings.value.bookmarkAdded)
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { browserRepo.removeBookmark(id) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { browserRepo.deleteHistoryItem(id) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            browserRepo.clearHistory()
            showToast("Đã xóa lịch sử duyệt web")
        }
    }

    fun createNewTab(privateMode: Boolean = false) {
        val newTab = BrowserTab(
            title = if (privateMode) strings.value.newPrivateTab else strings.value.newTab,
            isHome = true,
            isPrivate = privateMode,
            desktopMode = settings.value.defaultDesktopMode
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _currentScreen.value = AppScreen.HOME
        persistTabsSnapshot()
    }

    fun selectTab(tabId: String) {
        val target = _tabs.value.firstOrNull { it.id == tabId } ?: return
        _activeTabId.value = tabId
        if (target.isHome || target.url.isBlank()) {
            _currentScreen.value = AppScreen.HOME
        } else {
            val session = ensureSessionForTab(target)
            if (target.canGoBack.not() && target.title == UrlUtils.extractHost(target.url)) {
                session.loadUri(target.url)
            }
            _currentScreen.value = AppScreen.BROWSER
        }
        persistTabsSnapshot()
    }

    fun closeTab(tabId: String) {
        sessionMap.remove(tabId)?.close()
        val remaining = _tabs.value.filterNot { it.id == tabId }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab(desktopMode = settings.value.defaultDesktopMode)
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
            _currentScreen.value = AppScreen.HOME
        } else {
            _tabs.value = remaining
            if (_activeTabId.value == tabId) {
                val next = remaining.last()
                _activeTabId.value = next.id
                _currentScreen.value = if (next.isHome || next.url.isBlank()) AppScreen.HOME else AppScreen.BROWSER
            }
        }
        persistTabsSnapshot()
    }

    fun closeAllTabs() {
        sessionMap.values.forEach { it.close() }
        sessionMap.clear()
        val fresh = BrowserTab(desktopMode = settings.value.defaultDesktopMode)
        _tabs.value = listOf(fresh)
        _activeTabId.value = fresh.id
        _currentScreen.value = AppScreen.HOME
        persistTabsSnapshot()
    }

    fun findInPage(query: String, forward: Boolean = true) {
        val current = activeTab()
        val session = sessionMap[current.id] ?: return
        if (query.isBlank()) {
            session.finder.clear()
            return
        }
        val flags = if (forward) 0 else GeckoSession.FINDER_FIND_BACKWARDS
        session.finder.find(query, flags)
    }

    fun setShowFindInPage(show: Boolean) {
        _showFindInPage.value = show
        if (!show) {
            sessionMap[activeTab().id]?.finder?.clear()
        }
    }

    fun translateCurrentPage() {
        val current = activeTab()
        if (current.url.isBlank() || current.isHome) return
        val tl = settings.value.language.code
        val encoded = URLEncoder.encode(current.url, "UTF-8")
        openUrl("https://translate.google.com/translate?sl=auto&tl=$tl&u=$encoded", forceNewTab = true)
    }

    fun shareCurrentUrl() {
        val current = activeTab()
        if (current.url.isBlank() || current.isHome) return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, current.title)
            putExtra(Intent.EXTRA_TEXT, current.url)
        }
        val chooser = Intent.createChooser(shareIntent, strings.value.shareUrl).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(chooser)
    }

    fun startRealDownload(url: String, fileName: String? = null, mimeType: String? = null, userAgent: String? = null) {
        try {
            downloadHelper.enqueueDownload(url, fileName, mimeType, userAgent)
            showToast("${strings.value.downloadStarted}: ${fileName ?: UrlUtils.guessFileName(url, null)}")
        } catch (_: Exception) {
            showToast("Không thể bắt đầu tải xuống")
        }
    }

    fun confirmPendingDownload(accepted: Boolean) {
        val item = _pendingDownload.value ?: return
        _pendingDownload.value = null
        if (accepted) {
            startRealDownload(item.url, item.fileName, item.mimeType, item.userAgent)
        }
    }

    fun openDownload(item: DownloadEntity) {
        if (!downloadHelper.openDownloadedFile(item)) {
            showToast("Không tìm thấy ứng dụng phù hợp để mở tệp")
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch { browserRepo.deleteDownload(id) }
    }

    fun clearDownloads() {
        viewModelScope.launch { browserRepo.clearDownloads() }
    }

    fun clearWebPromptOnly() {
        _webPrompt.value = null
    }

    fun dismissWebPrompt() {
        when (val p = _webPrompt.value) {
            is WebPromptState.Alert -> p.result.complete(p.prompt.dismiss())
            is WebPromptState.Confirm -> p.result.complete(p.prompt.dismiss())
            is WebPromptState.TextInput -> p.result.complete(p.prompt.dismiss())
            is WebPromptState.Choice -> p.result.complete(p.prompt.dismiss())
            null -> Unit
        }
        _webPrompt.value = null
    }

    fun resolveWebPermission(allow: Boolean) {
        val req = _webPermissionPrompt.value ?: return
        _webPermissionPrompt.value = null
        req.result.complete(
            if (allow) GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
            else GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
        )
    }

    fun dismissContextMenu() {
        _contextMenu.value = null
    }

    fun setShowQuickMenu(show: Boolean) {
        _showQuickMenu.value = show
    }

    fun openSettingsCategory(category: SettingsCategory?) {
        _activeSettingsCategory.value = category
    }

    // Settings updates
    fun setThemePreset(preset: ThemePreset) = viewModelScope.launch { prefsRepo.setThemePreset(preset) }
    fun setForceDarkWeb(enabled: Boolean) = viewModelScope.launch { prefsRepo.setForceDarkWeb(enabled) }
    fun setShowQuickBookmarks(enabled: Boolean) = viewModelScope.launch { prefsRepo.setShowQuickBookmarks(enabled) }
    fun setBottomAddressBar(enabled: Boolean) = viewModelScope.launch { prefsRepo.setBottomAddressBar(enabled) }
    fun setTrackingProtection(level: TrackingProtectionLevel) = viewModelScope.launch { prefsRepo.setTrackingProtection(level) }
    fun setJavascriptEnabled(enabled: Boolean) = viewModelScope.launch { prefsRepo.setJavascriptEnabled(enabled) }
    fun setHttpsOnly(enabled: Boolean) = viewModelScope.launch { prefsRepo.setHttpsOnly(enabled) }
    fun setDefaultDesktopMode(enabled: Boolean) = viewModelScope.launch { prefsRepo.setDefaultDesktopMode(enabled) }
    fun setTextZoomPercent(percent: Int) = viewModelScope.launch { prefsRepo.setTextZoomPercent(percent) }
    fun setAskBeforeDownload(enabled: Boolean) = viewModelScope.launch { prefsRepo.setAskBeforeDownload(enabled) }
    fun setSearchEngine(engine: SearchEngineId) = viewModelScope.launch { prefsRepo.setSearchEngine(engine) }
    fun setSearchSuggestions(enabled: Boolean) = viewModelScope.launch { prefsRepo.setSearchSuggestions(enabled) }
    fun setOpenLinksInNewTab(enabled: Boolean) = viewModelScope.launch { prefsRepo.setOpenLinksInNewTab(enabled) }
    fun setRestoreTabsOnLaunch(enabled: Boolean) = viewModelScope.launch { prefsRepo.setRestoreTabsOnLaunch(enabled) }
    fun setLanguage(language: AppLanguage) = viewModelScope.launch { prefsRepo.setLanguage(language) }

    fun clearCache() {
        geckoManager.clearCache()
        app.cacheDir.deleteRecursively()
        showToast("Đã xóa bộ nhớ đệm (Cache)")
    }

    fun clearSiteData() {
        geckoManager.clearSiteData()
        showToast("Đã xóa Cookie & dữ liệu trang web")
    }

    fun clearAllBrowserData() {
        closeAllTabs()
        geckoManager.clearAllBrowserData()
        viewModelScope.launch {
            browserRepo.clearHistory()
            browserRepo.clearSavedTabs()
            showToast("Đã dọn sạch toàn bộ dữ liệu duyệt web")
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            prefsRepo.resetAll()
            showToast("Đã khôi phục cài đặt mặc định")
        }
    }

    fun showToast(message: String) {
        _toastEvents.tryEmit(message)
    }

    private fun persistTabsSnapshot() {
        if (!settings.value.restoreTabsOnLaunch) return
        viewModelScope.launch {
            val nonPrivate = _tabs.value.filter { !it.isPrivate }
            val activeId = _activeTabId.value
            val entities = nonPrivate.mapIndexed { idx, tab ->
                SavedTabEntity(
                    id = tab.id,
                    title = tab.title,
                    url = tab.url,
                    isHome = tab.isHome,
                    position = idx,
                    isActive = tab.id == activeId
                )
            }
            browserRepo.saveTabsSnapshot(entities)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionMap.values.forEach { it.close() }
        sessionMap.clear()
    }
}
