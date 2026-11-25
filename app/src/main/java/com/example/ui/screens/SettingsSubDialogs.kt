package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.BrowserSettings
import com.example.data.preferences.SearchEngineId
import com.example.data.preferences.ThemePreset
import com.example.data.preferences.TrackingProtectionLevel
import com.example.data.strings.AppStrings
import com.example.ui.BrowserViewModel
import com.example.ui.SettingsCategory
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalMeoMeoPalette

@Composable
fun SettingsCategoryDialog(
    category: SettingsCategory,
    settings: BrowserSettings,
    strings: AppStrings,
    historyCount: Int,
    bookmarksCount: Int,
    downloadsCount: Int,
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    val context = LocalContext.current

    val title = when (category) {
        SettingsCategory.APPEARANCE -> strings.appearanceTitle
        SettingsCategory.PRIVACY -> strings.privacyTitle
        SettingsCategory.TOOLS -> strings.toolsTitle
        SettingsCategory.SEARCH -> strings.searchTitle
        SettingsCategory.TABS -> strings.tabTitle
        SettingsCategory.STORAGE -> strings.storageTitle
        SettingsCategory.LANGUAGE -> strings.languageTitle
        SettingsCategory.ABOUT -> strings.aboutTitle
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardGradientTop,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textSecondary,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (category) {
                    SettingsCategory.APPEARANCE -> {
                        Text(
                            text = if (settings.language == AppLanguage.VI) "Chủ đề màu sắc" else "Theme Preset",
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        ThemePreset.entries.forEach { preset ->
                            val label = when (preset) {
                                ThemePreset.OBSIDIAN -> "MeoMeo Obsidian (Mặc định)"
                                ThemePreset.MIDNIGHT -> "Midnight Violet"
                                ThemePreset.AMOLED -> "AMOLED Pure Black"
                                ThemePreset.LIGHT -> "Frosted Light"
                            }
                            RadioRow(
                                label = label,
                                selected = settings.themePreset == preset,
                                onClick = { viewModel.setThemePreset(preset) }
                            )
                        }
                        HorizontalDivider(color = palette.cardBorder)
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Chế độ tối cho trang web" else "Prefer dark mode on websites",
                            checked = settings.forceDarkWeb,
                            onCheckedChange = viewModel::setForceDarkWeb
                        )
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Hiện dấu trang nhanh ở Trang chủ" else "Show quick bookmarks on Home",
                            checked = settings.showQuickBookmarksOnHome,
                            onCheckedChange = viewModel::setShowQuickBookmarks
                        )
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Thanh địa chỉ ở cạnh dưới" else "Bottom address bar",
                            checked = settings.bottomAddressBar,
                            onCheckedChange = viewModel::setBottomAddressBar
                        )
                    }

                    SettingsCategory.PRIVACY -> {
                        Text(
                            text = if (settings.language == AppLanguage.VI)
                                "Đã chặn tổng cộng ${settings.trackersBlockedTotal} trình theo dõi"
                            else
                                "${settings.trackersBlockedTotal} trackers blocked total",
                            color = palette.accentBright,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        TrackingProtectionLevel.entries.forEach { level ->
                            val desc = when (level) {
                                TrackingProtectionLevel.STRICT -> if (settings.language == AppLanguage.VI) "Nghiêm ngặt (Chặn theo dõi & dấu vân tay)" else "Strict (Trackers + Fingerprinting)"
                                TrackingProtectionLevel.STANDARD -> if (settings.language == AppLanguage.VI) "Tiêu chuẩn (Cân bằng tương thích)" else "Standard (Balanced)"
                                TrackingProtectionLevel.OFF -> if (settings.language == AppLanguage.VI) "Tắt chặn theo dõi" else "Off"
                            }
                            RadioRow(
                                label = desc,
                                selected = settings.trackingProtection == level,
                                onClick = { viewModel.setTrackingProtection(level) }
                            )
                        }
                        HorizontalDivider(color = palette.cardBorder)
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Chế độ chỉ HTTPS (HTTPS-Only)" else "HTTPS-Only Mode",
                            checked = settings.httpsOnly,
                            onCheckedChange = viewModel::setHttpsOnly
                        )
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Cho phép JavaScript" else "Enable JavaScript",
                            checked = settings.javascriptEnabled,
                            onCheckedChange = viewModel::setJavascriptEnabled
                        )
                    }

                    SettingsCategory.TOOLS -> {
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Mặc định trang cho máy tính" else "Default to Desktop mode",
                            checked = settings.defaultDesktopMode,
                            onCheckedChange = viewModel::setDefaultDesktopMode
                        )
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Hỏi trước khi tải xuống" else "Ask before downloading",
                            checked = settings.askBeforeDownload,
                            onCheckedChange = viewModel::setAskBeforeDownload
                        )
                        HorizontalDivider(color = palette.cardBorder)
                        Text(
                            text = if (settings.language == AppLanguage.VI)
                                "Cỡ chữ trang web / Chế độ đọc: ${settings.textZoomPercent}%"
                            else
                                "Web Text Scale / Reader Zoom: ${settings.textZoomPercent}%",
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Slider(
                            value = settings.textZoomPercent.toFloat(),
                            onValueChange = { viewModel.setTextZoomPercent(it.toInt()) },
                            valueRange = 80f..150f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accentBright,
                                activeTrackColor = palette.accent
                            )
                        )
                    }

                    SettingsCategory.SEARCH -> {
                        SearchEngineId.entries.forEach { engine ->
                            RadioRow(
                                label = engine.displayName,
                                selected = settings.searchEngine == engine,
                                onClick = { viewModel.setSearchEngine(engine) }
                            )
                        }
                        HorizontalDivider(color = palette.cardBorder)
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Gợi ý tìm kiếm trực tiếp" else "Live search suggestions",
                            checked = settings.searchSuggestionsEnabled,
                            onCheckedChange = viewModel::setSearchSuggestions
                        )
                    }

                    SettingsCategory.TABS -> {
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Mở liên kết ngoài trong tab mới" else "Open external links in new tab",
                            checked = settings.openLinksInNewTab,
                            onCheckedChange = viewModel::setOpenLinksInNewTab
                        )
                        ToggleRow(
                            title = if (settings.language == AppLanguage.VI) "Khôi phục tab khi mở lại ứng dụng" else "Restore previous tabs on launch",
                            checked = settings.restoreTabsOnLaunch,
                            onCheckedChange = viewModel::setRestoreTabsOnLaunch
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.closeAllTabs()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.closeAllTabs, color = DangerRed)
                        }
                    }

                    SettingsCategory.STORAGE -> {
                        val cacheKb = (context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum() / 1024L)
                        Text(
                            text = if (settings.language == AppLanguage.VI)
                                "Cache ứng dụng: ~${cacheKb} KB\nDấu trang: $bookmarksCount • Lịch sử: $historyCount • Tải xuống: $downloadsCount"
                            else
                                "App Cache: ~${cacheKb} KB\nBookmarks: $bookmarksCount • History: $historyCount • Downloads: $downloadsCount",
                            color = palette.textSecondary,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = viewModel::clearCache,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (settings.language == AppLanguage.VI) "Xóa bộ nhớ đệm (Cache)" else "Clear Cache")
                        }
                        OutlinedButton(
                            onClick = viewModel::clearSiteData,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (settings.language == AppLanguage.VI) "Xóa Cookie & Dữ liệu trang web" else "Clear Cookies & Site Data", color = palette.textPrimary)
                        }
                        OutlinedButton(
                            onClick = viewModel::clearHistory,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (settings.language == AppLanguage.VI) "Xóa lịch sử duyệt web" else "Clear Browsing History", color = palette.textPrimary)
                        }
                        OutlinedButton(
                            onClick = viewModel::clearAllBrowserData,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (settings.language == AppLanguage.VI) "Xóa toàn bộ dữ liệu" else "Clear All Data", color = DangerRed)
                        }
                    }

                    SettingsCategory.LANGUAGE -> {
                        AppLanguage.entries.forEach { lang ->
                            RadioRow(
                                label = lang.displayName,
                                selected = settings.language == lang,
                                onClick = { viewModel.setLanguage(lang) }
                            )
                        }
                    }

                    SettingsCategory.ABOUT -> {
                        Text(
                            text = "MeoMeo Browser v1.0.0\nEngine: Mozilla GeckoView (Gecko 139)\nArchitecture: Kotlin + Jetpack Compose + Room + DataStore",
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.resetSettings()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (settings.language == AppLanguage.VI) "Khôi phục cài đặt mặc định" else "Reset all settings",
                                color = palette.accentBright
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = palette.accentBright, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 13.5.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.textPrimary,
                checkedTrackColor = palette.accent
            )
        )
    }
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (selected) palette.accentGlow.copy(alpha = 0.55f) else palette.cardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.cardGradientBottom)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = palette.accentBright)
        )
        Text(
            text = label,
            color = palette.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
