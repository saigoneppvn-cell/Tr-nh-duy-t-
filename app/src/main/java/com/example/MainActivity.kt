package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.BrowserViewModel
import com.example.ui.PendingAndroidPermission
import com.example.ui.components.DownloadConfirmDialog
import com.example.ui.components.LinkContextMenuDialog
import com.example.ui.components.MeoMeoToast
import com.example.ui.components.QuickMenuBottomSheet
import com.example.ui.components.WebPermissionDialog
import com.example.ui.components.WebPromptDialog
import com.example.ui.screens.BookmarksHistoryScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsCategoryDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TabsScreen
import com.example.ui.theme.MeoMeoTheme
import kotlinx.coroutines.delay
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        handleIncomingIntent(intent)

        setContent {
            MeoMeoBrowserRoot(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.dataString
            if (!uri.isNullOrBlank()) {
                viewModel.openUrl(uri, forceNewTab = true)
            }
        }
    }
}

@Composable
private fun MeoMeoBrowserRoot(viewModel: BrowserViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val strings by viewModel.strings.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val suggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val activeSettingsCategory by viewModel.activeSettingsCategory.collectAsStateWithLifecycle()
    val showQuickMenu by viewModel.showQuickMenu.collectAsStateWithLifecycle()
    val showFindInPage by viewModel.showFindInPage.collectAsStateWithLifecycle()
    val webPrompt by viewModel.webPrompt.collectAsStateWithLifecycle()
    val webPermissionPrompt by viewModel.webPermissionPrompt.collectAsStateWithLifecycle()
    val contextMenu by viewModel.contextMenu.collectAsStateWithLifecycle()
    val pendingDownload by viewModel.pendingDownload.collectAsStateWithLifecycle()

    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()
    val isCurrentBookmarked = bookmarks.any { it.url == activeTab.url }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { msg ->
            toastMessage = msg
            delay(2200)
            if (toastMessage == msg) toastMessage = null
        }
    }

    // Voice Search Launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.submitInput(spoken)
            }
        }
    }

    // Android Runtime Permissions Bridge for GeckoView (Camera, Mic, Location)
    var pendingAndroidPerm by remember { mutableStateOf<PendingAndroidPermission?>(null) }
    val androidPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        if (granted) pendingAndroidPerm?.callback?.grant()
        else pendingAndroidPerm?.callback?.reject()
        pendingAndroidPerm = null
    }
    LaunchedEffect(Unit) {
        viewModel.androidPermissionRequests.collect { req ->
            pendingAndroidPerm = req
            androidPermLauncher.launch(req.permissions)
        }
    }

    // File Upload Picker Bridge for GeckoView <input type="file">
    var pendingFilePrompt by remember {
        mutableStateOf<Pair<GeckoSession.PromptDelegate.FilePrompt, GeckoResult<GeckoSession.PromptDelegate.PromptResponse>>?>(null)
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val current = pendingFilePrompt
        pendingFilePrompt = null
        if (current != null) {
            val (prompt, result) = current
            if (uris.isNotEmpty()) {
                result.complete(prompt.confirm(context, uris.toTypedArray()))
            } else {
                result.complete(prompt.dismiss())
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.filePickerRequests.collect { req ->
            pendingFilePrompt = req
            val mimeTypes = req.first.mimeTypes?.takeIf { it.isNotEmpty() } ?: arrayOf("*/*")
            filePickerLauncher.launch(mimeTypes.map { it }.toTypedArray())
        }
    }

    // Hardware Back button inside Browser screen
    BackHandler(enabled = currentScreen == AppScreen.BROWSER) {
        viewModel.goBack()
    }

    MeoMeoTheme(themePreset = settings.themePreset) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = currentScreen, label = "meomeo_screen") { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            activeTab = activeTab,
                            tabCount = tabs.size,
                            settings = settings,
                            strings = strings,
                            bookmarks = bookmarks,
                            suggestions = suggestions,
                            onQueryChanged = viewModel::updateSearchQueryForSuggestions,
                            onSubmitQuery = viewModel::submitInput,
                            onVoiceSearchClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                                        )
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE,
                                            if (settings.language.code == "vi") "vi-VN" else "en-US"
                                        )
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, strings.searchPlaceholder)
                                    }
                                    voiceLauncher.launch(intent)
                                } catch (_: Exception) {
                                    viewModel.showToast(strings.voiceUnavailable)
                                }
                            },
                            onOpenQuickMenu = { viewModel.setShowQuickMenu(true) },
                            onNavigate = viewModel::navigateTo
                        )
                    }

                    AppScreen.BROWSER -> {
                        val session = remember(activeTab.id) {
                            viewModel.getOrCreateSession(activeTab.id)
                        }
                        BrowserScreen(
                            activeTab = activeTab,
                            tabCount = tabs.size,
                            session = session,
                            isBookmarked = isCurrentBookmarked,
                            settings = settings,
                            strings = strings,
                            suggestions = suggestions,
                            showFindInPage = showFindInPage,
                            onQueryChanged = viewModel::updateSearchQueryForSuggestions,
                            onSubmitAddress = viewModel::submitInput,
                            onBack = { viewModel.goBack() },
                            onForward = viewModel::goForward,
                            onReloadOrStop = viewModel::reloadOrStop,
                            onToggleBookmark = viewModel::toggleCurrentBookmark,
                            onGoHome = { viewModel.navigateTo(AppScreen.HOME) },
                            onOpenTabs = { viewModel.navigateTo(AppScreen.TABS) },
                            onOpenMenu = { viewModel.setShowQuickMenu(true) },
                            onFindInPage = viewModel::findInPage,
                            onCloseFindInPage = { viewModel.setShowFindInPage(false) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            settings = settings,
                            strings = strings,
                            onBack = {
                                viewModel.navigateTo(
                                    if (activeTab.isHome || activeTab.url.isBlank()) AppScreen.HOME
                                    else AppScreen.BROWSER
                                )
                            },
                            onOpenCategory = viewModel::openSettingsCategory
                        )
                    }

                    AppScreen.BOOKMARKS -> {
                        BookmarksHistoryScreen(
                            bookmarks = bookmarks,
                            history = history,
                            strings = strings,
                            onBack = {
                                viewModel.navigateTo(
                                    if (activeTab.isHome || activeTab.url.isBlank()) AppScreen.HOME
                                    else AppScreen.BROWSER
                                )
                            },
                            onOpenUrl = { url -> viewModel.openUrl(url) },
                            onAddBookmark = viewModel::addBookmarkManual,
                            onDeleteBookmark = viewModel::deleteBookmark,
                            onDeleteHistoryItem = viewModel::deleteHistoryItem,
                            onClearHistory = viewModel::clearHistory
                        )
                    }

                    AppScreen.DOWNLOADS -> {
                        DownloadsScreen(
                            downloads = downloads,
                            strings = strings,
                            onBack = {
                                viewModel.navigateTo(
                                    if (activeTab.isHome || activeTab.url.isBlank()) AppScreen.HOME
                                    else AppScreen.BROWSER
                                )
                            },
                            onOpenDownload = viewModel::openDownload,
                            onDeleteDownload = viewModel::deleteDownload,
                            onClearDownloads = viewModel::clearDownloads
                        )
                    }

                    AppScreen.TABS -> {
                        TabsScreen(
                            tabs = tabs,
                            activeTabId = activeTabId,
                            strings = strings,
                            onBack = {
                                viewModel.navigateTo(
                                    if (activeTab.isHome || activeTab.url.isBlank()) AppScreen.HOME
                                    else AppScreen.BROWSER
                                )
                            },
                            onSelectTab = viewModel::selectTab,
                            onCloseTab = viewModel::closeTab,
                            onNewTab = viewModel::createNewTab,
                            onCloseAllTabs = viewModel::closeAllTabs
                        )
                    }
                }
            }

            // Quick Menu Sheet
            if (showQuickMenu) {
                QuickMenuBottomSheet(
                    activeTab = activeTab,
                    isBrowserActive = currentScreen == AppScreen.BROWSER,
                    isBookmarked = isCurrentBookmarked,
                    strings = strings,
                    onDismiss = { viewModel.setShowQuickMenu(false) },
                    onNewTab = { viewModel.createNewTab(privateMode = false) },
                    onNewPrivateTab = { viewModel.createNewTab(privateMode = true) },
                    onToggleDesktop = viewModel::toggleDesktopMode,
                    onToggleBookmark = viewModel::toggleCurrentBookmark,
                    onFindInPage = { viewModel.setShowFindInPage(true) },
                    onTranslatePage = viewModel::translateCurrentPage,
                    onShare = viewModel::shareCurrentUrl,
                    onNavigate = viewModel::navigateTo
                )
            }

            // Settings Sub-Category Dialog
            activeSettingsCategory?.let { category ->
                SettingsCategoryDialog(
                    category = category,
                    settings = settings,
                    strings = strings,
                    historyCount = history.size,
                    bookmarksCount = bookmarks.size,
                    downloadsCount = downloads.size,
                    viewModel = viewModel,
                    onDismiss = { viewModel.openSettingsCategory(null) }
                )
            }

            // GeckoView Web Prompts (alert / confirm / prompt / choice)
            webPrompt?.let { promptState ->
                WebPromptDialog(
                    state = promptState,
                    strings = strings,
                    onResolved = viewModel::clearWebPromptOnly,
                    onDismiss = viewModel::dismissWebPrompt
                )
            }

            // GeckoView Content Permissions Dialog
            webPermissionPrompt?.let { perm ->
                WebPermissionDialog(
                    request = perm,
                    strings = strings,
                    onAllow = { viewModel.resolveWebPermission(true) },
                    onDeny = { viewModel.resolveWebPermission(false) }
                )
            }

            // GeckoView Link/Image Long-Press Context Menu
            contextMenu?.let { cm ->
                LinkContextMenuDialog(
                    state = cm,
                    strings = strings,
                    onOpenNewTab = { uri -> viewModel.openUrl(uri, forceNewTab = true) },
                    onDownload = { uri -> viewModel.startRealDownload(uri) },
                    onCopied = { viewModel.showToast(strings.linkCopied) },
                    onDismiss = viewModel::dismissContextMenu
                )
            }

            // Download Confirmation Dialog
            pendingDownload?.let { dl ->
                DownloadConfirmDialog(
                    pending = dl,
                    strings = strings,
                    onConfirm = { viewModel.confirmPendingDownload(true) },
                    onCancel = { viewModel.confirmPendingDownload(false) }
                )
            }

            // Floating MeoMeo Toast Pill
            MeoMeoToast(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp)
            )
        }
    }
}
