package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.browser.UrlUtils
import com.example.data.preferences.BrowserSettings
import com.example.data.strings.AppStrings
import com.example.ui.AppScreen
import com.example.ui.BrowserTab
import com.example.ui.theme.LocalMeoMeoPalette
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserScreen(
    activeTab: BrowserTab,
    tabCount: Int,
    session: GeckoSession,
    isBookmarked: Boolean,
    settings: BrowserSettings,
    strings: AppStrings,
    suggestions: List<String>,
    showFindInPage: Boolean,
    onQueryChanged: (String) -> Unit,
    onSubmitAddress: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenMenu: () -> Unit,
    onFindInPage: (String, Boolean) -> Unit,
    onCloseFindInPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeoMeoPalette.current
    var isEditingUrl by remember { mutableStateOf(false) }
    var urlText by remember(activeTab.url) { mutableStateOf(activeTab.url) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }

    LaunchedEffect(activeTab.id, activeTab.url) {
        if (!isEditingUrl) {
            urlText = activeTab.url
        }
    }

    val addressBarContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.navBarBackground)
                .then(
                    if (!settings.bottomAddressBar) Modifier.statusBarsPadding()
                    else Modifier
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Address Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(palette.glassFill)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showSecurityDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                activeTab.isPrivate -> Icons.Outlined.VisibilityOff
                                activeTab.isSecure -> Icons.Outlined.Lock
                                else -> Icons.Outlined.LockOpen
                            },
                            contentDescription = "Site security",
                            tint = when {
                                activeTab.isPrivate -> palette.accentBright
                                activeTab.isSecure -> SuccessGreen
                                else -> WarningAmber
                            },
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (isEditingUrl) {
                        BasicTextField(
                            value = urlText,
                            onValueChange = {
                                urlText = it
                                onQueryChanged(it)
                            },
                            singleLine = true,
                            cursorBrush = SolidColor(palette.accentBright),
                            textStyle = TextStyle(
                                fontFamily = PlusJakartaSansFontFamily,
                                color = palette.textPrimary,
                                fontSize = 14.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Go,
                                keyboardType = KeyboardType.Uri
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    isEditingUrl = false
                                    onSubmitAddress(urlText)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("browser_url_input")
                        )
                        IconButton(
                            onClick = {
                                isEditingUrl = false
                                urlText = activeTab.url
                                onQueryChanged("")
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = strings.close,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    urlText = activeTab.url
                                    isEditingUrl = true
                                }
                                .padding(vertical = 4.dp)
                                .testTag("browser_url_display")
                        ) {
                            Text(
                                text = activeTab.title.ifBlank { UrlUtils.extractHost(activeTab.url) },
                                color = palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = UrlUtils.extractHost(activeTab.url),
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!isEditingUrl && activeTab.desktopMode) {
                        Icon(
                            imageVector = Icons.Outlined.DesktopWindows,
                            contentDescription = strings.desktopSite,
                            tint = palette.accentBright,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onReloadOrStop,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("browser_reload_button")
                    ) {
                        Icon(
                            imageVector = if (activeTab.isLoading) Icons.Outlined.Close else Icons.Outlined.Refresh,
                            contentDescription = if (activeTab.isLoading) strings.stop else strings.reload,
                            tint = palette.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .testTag("browser_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) strings.removeBookmark else strings.addBookmark,
                        tint = if (isBookmarked) palette.accentBright else palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (activeTab.isLoading) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress / 100f).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = palette.accentGlow,
                    trackColor = palette.cardBorder
                )
            } else {
                HorizontalDivider(color = palette.cardBorder, thickness = 1.dp)
            }

            // Autocomplete suggestions when editing address bar
            AnimatedVisibility(visible = isEditingUrl && suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.cardGradientTop)
                ) {
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isEditingUrl = false
                                    onSubmitAddress(suggestion)
                                }
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = palette.accentBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = suggestion,
                                color = palette.textPrimary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(color = palette.cardBorder, thickness = 0.5.dp)
                    }
                }
            }

            // Find in page bar
            AnimatedVisibility(visible = showFindInPage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.cardGradientTop)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = strings.findInPage,
                        tint = palette.accentBright,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = findQuery,
                        onValueChange = {
                            findQuery = it
                            onFindInPage(it, true)
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(palette.accentBright),
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSansFontFamily,
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onFindInPage(findQuery, false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "Previous match",
                            tint = palette.textPrimary
                        )
                    }
                    IconButton(
                        onClick = { onFindInPage(findQuery, true) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Next match",
                            tint = palette.textPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            findQuery = ""
                            onCloseFindInPage()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = strings.close,
                            tint = palette.textSecondary
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        if (!activeTab.isFullscreen && !settings.bottomAddressBar) {
            addressBarContent()
        }

        // Real Mozilla GeckoView rendering surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    GeckoView(ctx).apply {
                        setSession(session)
                    }
                },
                update = { view ->
                    if (view.session !== session) {
                        view.setSession(session)
                    }
                }
            )
        }

        if (!activeTab.isFullscreen && settings.bottomAddressBar) {
            addressBarContent()
        }

        // Browser bottom navigation bar
        if (!activeTab.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.navBarBackground)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = palette.cardBorder, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("browser_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back,
                            tint = palette.textPrimary
                        )
                    }

                    IconButton(
                        onClick = onForward,
                        enabled = activeTab.canGoForward,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("browser_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = strings.forward,
                            tint = if (activeTab.canGoForward) palette.textPrimary else palette.textSecondary.copy(alpha = 0.35f)
                        )
                    }

                    IconButton(
                        onClick = onGoHome,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("browser_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = strings.navHome,
                            tint = palette.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenTabs)
                            .testTag("browser_tabs_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(21.dp)
                                .border(1.75.dp, palette.textPrimary, RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabCount.coerceAtMost(99).toString(),
                                color = palette.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("browser_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = strings.quickMenuTitle,
                            tint = palette.textPrimary
                        )
                    }
                }
            }
        }
    }

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            containerColor = palette.cardGradientTop,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = palette.accentBright
                )
            },
            title = {
                Text(
                    text = activeTab.securityHost.ifBlank { UrlUtils.extractHost(activeTab.url) },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (activeTab.isSecure) "HTTPS • Kết nối được mã hóa an toàn"
                        else "HTTP • Kết nối không được mã hóa",
                        color = if (activeTab.isSecure) SuccessGreen else WarningAmber,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${activeTab.blockedTrackersOnPage} ${strings.trackersBlockedLabel} (${settings.trackingProtection.id})",
                        fontSize = 13.sp
                    )
                    Text(
                        text = activeTab.url,
                        fontSize = 11.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text(strings.close, color = palette.accentBright)
                }
            }
        )
    }
}
