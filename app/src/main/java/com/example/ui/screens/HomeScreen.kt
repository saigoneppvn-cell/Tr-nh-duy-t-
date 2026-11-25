package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.preferences.BrowserSettings
import com.example.data.strings.AppStrings
import com.example.ui.AppScreen
import com.example.ui.BrowserTab
import com.example.ui.components.MeoMeoBackgroundWaves
import com.example.ui.components.MeoMeoBottomNavBar
import com.example.ui.components.MeoMeoCatLogo
import com.example.ui.theme.LocalMeoMeoPalette
import com.example.ui.theme.PlusJakartaSansFontFamily

@Composable
fun HomeScreen(
    activeTab: BrowserTab,
    tabCount: Int,
    settings: BrowserSettings,
    strings: AppStrings,
    bookmarks: List<BookmarkEntity>,
    suggestions: List<String>,
    onQueryChanged: (String) -> Unit,
    onSubmitQuery: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onOpenQuickMenu: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeoMeoPalette.current
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Mesh wave background art matching HTML
        MeoMeoBackgroundWaves()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Hamburger Menu + Status pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenQuickMenu,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("home_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = strings.quickMenuTitle,
                        tint = palette.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeTab.isPrivate) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(palette.accent.copy(alpha = 0.18f))
                                .border(1.dp, palette.accentGlow.copy(alpha = 0.45f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = strings.privateTabLabel,
                                tint = palette.accentBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = strings.privateTabLabel,
                                color = palette.accentBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (settings.trackersBlockedTotal > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(palette.glassFill)
                                .border(1.dp, palette.glassBorder, RoundedCornerShape(50))
                                .clickable { onNavigate(AppScreen.SETTINGS) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = strings.privacyTitle,
                                tint = palette.accentBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${settings.trackersBlockedTotal} ${strings.trackersBlockedLabel}",
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Centerpiece: Cat Logo + Title + Glass Pill Search Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 480.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MeoMeoCatLogo(width = 108.dp, height = 92.dp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = strings.brandTitle,
                        color = palette.textPrimary,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = strings.brandSubtitle,
                        color = palette.textSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Glassmorphism Pill Search Bar
                    val borderBrush = if (isFocused) {
                        SolidColor(palette.accentGlow.copy(alpha = 0.65f))
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                palette.glassTopBorder,
                                palette.glassBorder
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isFocused) palette.glassFill.copy(alpha = 0.14f)
                                else palette.glassFill
                            )
                            .border(1.dp, borderBrush, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = strings.searchTitle,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = strings.searchPlaceholder,
                                    color = palette.textSecondary,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    onQueryChanged(it)
                                },
                                singleLine = true,
                                cursorBrush = SolidColor(palette.accentBright),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    color = palette.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Go,
                                    keyboardType = KeyboardType.Uri
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        onSubmitQuery(query)
                                        query = ""
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .testTag("home_search_input")
                            )
                        }

                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    query = ""
                                    onQueryChanged("")
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = strings.close,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onVoiceSearchClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .testTag("voice_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Voice Search",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Real Live Autocomplete Suggestions
                    AnimatedVisibility(visible = suggestions.isNotEmpty() && query.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(palette.cardGradientTop.copy(alpha = 0.96f))
                                .border(1.dp, palette.cardBorder, RoundedCornerShape(20.dp))
                        ) {
                            suggestions.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            query = ""
                                            onSubmitQuery(item)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                        text = item,
                                        color = palette.textPrimary,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (idx < suggestions.lastIndex) {
                                    HorizontalDivider(color = palette.cardBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    // Quick Access Bookmarks (only shown when enabled & bookmarks exist, or subtle search engine pill)
                    if (settings.showQuickBookmarksOnHome && bookmarks.isNotEmpty() && suggestions.isEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bookmarks.take(6).forEach { bookmark ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(palette.glassFill)
                                        .border(1.dp, palette.cardBorder, RoundedCornerShape(50))
                                        .clickable { onSubmitQuery(bookmark.url) }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = palette.accentBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = bookmark.title,
                                        color = palette.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 120.dp)
                                    )
                                }
                            }
                        }
                    } else if (suggestions.isEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onNavigate(AppScreen.SETTINGS) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = null,
                                tint = palette.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "GeckoView • ${settings.searchEngine.displayName}",
                                color = palette.textSecondary.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Bar matching HTML
            MeoMeoBottomNavBar(
                currentScreen = AppScreen.HOME,
                tabCount = tabCount,
                strings = strings,
                onNavigate = onNavigate
            )
        }
    }
}
