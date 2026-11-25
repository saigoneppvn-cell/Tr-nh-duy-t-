package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.strings.AppStrings
import com.example.ui.AppScreen
import com.example.ui.BrowserTab
import com.example.ui.ContextMenuState
import com.example.ui.PendingDownloadConfirmation
import com.example.ui.WebPermissionPrompt
import com.example.ui.WebPromptState
import com.example.ui.theme.LocalMeoMeoPalette
import org.mozilla.geckoview.GeckoSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMenuBottomSheet(
    activeTab: BrowserTab,
    isBrowserActive: Boolean,
    isBookmarked: Boolean,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onFindInPage: () -> Unit,
    onTranslatePage: () -> Unit,
    onShare: () -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    val palette = LocalMeoMeoPalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.cardGradientTop,
        contentColor = palette.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.quickMenuTitle,
                color = palette.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            QuickMenuRow(
                icon = Icons.Outlined.Add,
                label = strings.newTab,
                onClick = {
                    onDismiss()
                    onNewTab()
                }
            )
            QuickMenuRow(
                icon = Icons.Outlined.VisibilityOff,
                label = strings.newPrivateTab,
                onClick = {
                    onDismiss()
                    onNewPrivateTab()
                }
            )

            if (isBrowserActive) {
                HorizontalDivider(color = palette.cardBorder, modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onToggleDesktop()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DesktopWindows,
                            contentDescription = strings.desktopSite,
                            tint = palette.accentBright
                        )
                        Text(strings.desktopSite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(checked = activeTab.desktopMode, onCheckedChange = {
                        onDismiss()
                        onToggleDesktop()
                    })
                }
                QuickMenuRow(
                    icon = Icons.Outlined.BookmarkBorder,
                    label = if (isBookmarked) strings.removeBookmark else strings.addBookmark,
                    onClick = {
                        onDismiss()
                        onToggleBookmark()
                    }
                )
                QuickMenuRow(
                    icon = Icons.Outlined.Search,
                    label = strings.findInPage,
                    onClick = {
                        onDismiss()
                        onFindInPage()
                    }
                )
                QuickMenuRow(
                    icon = Icons.Outlined.Translate,
                    label = strings.translatePage,
                    onClick = {
                        onDismiss()
                        onTranslatePage()
                    }
                )
                QuickMenuRow(
                    icon = Icons.Outlined.Share,
                    label = strings.shareUrl,
                    onClick = {
                        onDismiss()
                        onShare()
                    }
                )
            }

            HorizontalDivider(color = palette.cardBorder, modifier = Modifier.padding(vertical = 4.dp))
            QuickMenuRow(
                icon = Icons.Outlined.BookmarkBorder,
                label = "${strings.bookmarksTitle} & ${strings.historyTitle}",
                onClick = {
                    onDismiss()
                    onNavigate(AppScreen.BOOKMARKS)
                }
            )
            QuickMenuRow(
                icon = Icons.Outlined.Download,
                label = strings.downloadsTitle,
                onClick = {
                    onDismiss()
                    onNavigate(AppScreen.DOWNLOADS)
                }
            )
            QuickMenuRow(
                icon = Icons.Outlined.Settings,
                label = strings.settingsTitle,
                onClick = {
                    onDismiss()
                    onNavigate(AppScreen.SETTINGS)
                }
            )
        }
    }
}

@Composable
private fun QuickMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = palette.accentBright,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = palette.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WebPromptDialog(
    state: WebPromptState,
    strings: AppStrings,
    onResolved: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    when (state) {
        is WebPromptState.Alert -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = palette.cardGradientTop,
                titleContentColor = palette.textPrimary,
                textContentColor = palette.textSecondary,
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = {
                        state.result.complete(state.prompt.dismiss())
                        onResolved()
                    }) {
                        Text(strings.confirm, color = palette.accentBright)
                    }
                }
            )
        }

        is WebPromptState.Confirm -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = palette.cardGradientTop,
                titleContentColor = palette.textPrimary,
                textContentColor = palette.textSecondary,
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = {
                        state.result.complete(
                            state.prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE)
                        )
                        onResolved()
                    }) {
                        Text(strings.confirm, color = palette.accentBright)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        state.result.complete(
                            state.prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE)
                        )
                        onResolved()
                    }) {
                        Text(strings.cancel, color = palette.textSecondary)
                    }
                }
            )
        }

        is WebPromptState.TextInput -> {
            var value by remember(state) { mutableStateOf(state.defaultValue) }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = palette.cardGradientTop,
                titleContentColor = palette.textPrimary,
                textContentColor = palette.textSecondary,
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.message.isNotBlank()) Text(state.message)
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        state.result.complete(state.prompt.confirm(value))
                        onResolved()
                    }) {
                        Text(strings.confirm, color = palette.accentBright)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(strings.cancel, color = palette.textSecondary)
                    }
                }
            )
        }

        is WebPromptState.Choice -> {
            val selectedIds = remember(state) {
                mutableStateListOf<String>().apply {
                    state.choices.filter { it.selected }.forEach { add(it.id) }
                }
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = palette.cardGradientTop,
                titleContentColor = palette.textPrimary,
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.choices.toList()) { choice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (state.multiple) {
                                            if (selectedIds.contains(choice.id)) selectedIds.remove(choice.id)
                                            else selectedIds.add(choice.id)
                                        } else {
                                            state.result.complete(state.prompt.confirm(choice))
                                            onResolved()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (state.multiple) {
                                    Checkbox(
                                        checked = selectedIds.contains(choice.id),
                                        onCheckedChange = null
                                    )
                                }
                                Text(
                                    text = choice.label,
                                    color = palette.textPrimary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (state.multiple) {
                        TextButton(onClick = {
                            state.result.complete(state.prompt.confirm(selectedIds.toTypedArray()))
                            onResolved()
                        }) {
                            Text(strings.confirm, color = palette.accentBright)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(strings.cancel, color = palette.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun WebPermissionDialog(
    request: WebPermissionPrompt,
    strings: AppStrings,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    val permLabel = when (request.permissionType) {
        GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION -> "Vị trí (Location)"
        GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION -> "Thông báo (Notifications)"
        GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS -> "Nội dung DRM (Protected Media)"
        GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE,
        GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE -> "Tự động phát âm thanh/video"
        else -> "Quyền truy cập trang web"
    }

    AlertDialog(
        onDismissRequest = onDeny,
        containerColor = palette.cardGradientTop,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textSecondary,
        title = { Text(request.host, fontWeight = FontWeight.Bold) },
        text = { Text("${request.host} yêu cầu quyền: $permLabel") },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(strings.confirm, color = palette.accentBright, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(strings.cancel, color = palette.textSecondary)
            }
        }
    )
}

@Composable
fun LinkContextMenuDialog(
    state: ContextMenuState,
    strings: AppStrings,
    onOpenNewTab: (String) -> Unit,
    onDownload: (String) -> Unit,
    onCopied: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    val context = LocalContext.current
    val targetUrl = state.linkUri ?: state.sourceUri ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardGradientTop,
        titleContentColor = palette.textPrimary,
        title = {
            Text(
                text = state.title?.takeIf { it.isNotBlank() } ?: targetUrl,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                QuickMenuRow(
                    icon = Icons.Outlined.OpenInNew,
                    label = strings.openInNewTab,
                    onClick = {
                        onDismiss()
                        onOpenNewTab(targetUrl)
                    }
                )
                QuickMenuRow(
                    icon = Icons.Outlined.ContentCopy,
                    label = strings.copyLink,
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("url", targetUrl))
                        onDismiss()
                        onCopied()
                    }
                )
                QuickMenuRow(
                    icon = Icons.Outlined.Download,
                    label = strings.downloadLink,
                    onClick = {
                        onDismiss()
                        onDownload(targetUrl)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = palette.textSecondary)
            }
        }
    )
}

@Composable
fun DownloadConfirmDialog(
    pending: PendingDownloadConfirmation,
    strings: AppStrings,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = palette.cardGradientTop,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textSecondary,
        title = { Text(strings.downloadsTitle, fontWeight = FontWeight.Bold) },
        text = { Text("${pending.fileName}\n\n${pending.url}") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(strings.confirm, color = palette.accentBright, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(strings.cancel, color = palette.textSecondary)
            }
        }
    )
}
