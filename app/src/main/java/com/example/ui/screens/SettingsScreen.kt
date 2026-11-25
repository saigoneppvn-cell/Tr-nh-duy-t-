package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tab
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.BrowserSettings
import com.example.data.strings.AppStrings
import com.example.ui.SettingsCategory
import com.example.ui.theme.LocalMeoMeoPalette
import com.example.ui.theme.PlusJakartaSansFontFamily

private data class SettingsCardItem(
    val category: SettingsCategory,
    val groupLabel: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    strings: AppStrings,
    onBack: () -> Unit,
    onOpenCategory: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val palette = LocalMeoMeoPalette.current
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val items = remember(settings, strings) {
        listOf(
            SettingsCardItem(
                category = SettingsCategory.APPEARANCE,
                groupLabel = strings.groupAppearance,
                title = strings.appearanceTitle,
                subtitle = "${strings.appearanceSubtitle} (${settings.themePreset.id})",
                icon = Icons.Outlined.Palette,
                testTag = "settings_appearance"
            ),
            SettingsCardItem(
                category = SettingsCategory.PRIVACY,
                groupLabel = strings.groupPrivacy,
                title = strings.privacyTitle,
                subtitle = "${strings.privacySubtitle} • ${settings.trackingProtection.id}",
                icon = Icons.Outlined.Shield,
                testTag = "settings_privacy"
            ),
            SettingsCardItem(
                category = SettingsCategory.TOOLS,
                groupLabel = strings.groupTools,
                title = strings.toolsTitle,
                subtitle = "${strings.toolsSubtitle} (${settings.textZoomPercent}%)",
                icon = Icons.Outlined.Build,
                testTag = "settings_tools"
            ),
            SettingsCardItem(
                category = SettingsCategory.SEARCH,
                groupLabel = strings.groupSearch,
                title = strings.searchTitle,
                subtitle = settings.searchEngine.displayName,
                icon = Icons.Outlined.Search,
                testTag = "settings_search"
            ),
            SettingsCardItem(
                category = SettingsCategory.TABS,
                groupLabel = strings.groupTab,
                title = strings.tabTitle,
                subtitle = strings.tabSubtitle,
                icon = Icons.Outlined.Tab,
                testTag = "settings_tabs"
            ),
            SettingsCardItem(
                category = SettingsCategory.STORAGE,
                groupLabel = strings.groupStorage,
                title = strings.storageTitle,
                subtitle = strings.storageSubtitle,
                icon = Icons.Outlined.Storage,
                testTag = "settings_storage"
            ),
            SettingsCardItem(
                category = SettingsCategory.LANGUAGE,
                groupLabel = strings.groupLanguage,
                title = strings.languageTitle,
                subtitle = settings.language.displayName,
                icon = Icons.Outlined.Language,
                testTag = "settings_language"
            ),
            SettingsCardItem(
                category = SettingsCategory.ABOUT,
                groupLabel = strings.groupAbout,
                title = strings.aboutTitle,
                subtitle = "MeoMeo Browser v1.0.0 • GeckoView",
                icon = Icons.Outlined.Info,
                testTag = "settings_about"
            )
        )
    }

    val filteredItems = remember(items, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) items
        else items.filter {
            it.groupLabel.lowercase().contains(q) ||
                it.title.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header matching HTML Screen 2
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.back,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = strings.settingsTitle,
                            color = palette.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    }

                    IconButton(
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) searchQuery = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("settings_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (showSearch) Icons.Outlined.Close else Icons.Outlined.Search,
                            contentDescription = strings.searchTitle,
                            tint = palette.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.glassFill)
                            .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = palette.accentBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = strings.settingsSearchPlaceholder,
                                    color = palette.textSecondary,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                cursorBrush = SolidColor(palette.accentBright),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    color = palette.textPrimary,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Scrollable list of 8 settings cards
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredItems, key = { it.category.name }) { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.groupLabel,
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(palette.cardGradientTop, palette.cardGradientBottom)
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(palette.cardTopBorder, palette.cardBorder)
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { onOpenCategory(item.category) }
                                .testTag(item.testTag)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.iconBadgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = palette.accent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = palette.textPrimary,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.subtitle,
                                        color = palette.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
