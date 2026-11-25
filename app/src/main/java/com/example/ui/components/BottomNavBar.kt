package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.strings.AppStrings
import com.example.ui.AppScreen
import com.example.ui.theme.LocalMeoMeoPalette

@Composable
fun MeoMeoBottomNavBar(
    currentScreen: AppScreen,
    tabCount: Int,
    strings: AppStrings,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeoMeoPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.navBarBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = palette.cardBorder, thickness = 1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Download
                NavItemButton(
                    label = strings.navDownload,
                    isActive = currentScreen == AppScreen.DOWNLOADS,
                    testTag = "nav_download",
                    onClick = { onNavigate(AppScreen.DOWNLOADS) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = strings.navDownload,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                }

                // 2. Bookmarks
                NavItemButton(
                    label = strings.navBookmarks,
                    isActive = currentScreen == AppScreen.BOOKMARKS,
                    testTag = "nav_bookmarks",
                    onClick = { onNavigate(AppScreen.BOOKMARKS) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Icon(
                        imageVector = if (currentScreen == AppScreen.BOOKMARKS) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = strings.navBookmarks,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                }

                // 3. Home (Active glowing purple pill + bottom glowing dot)
                val homeActive = currentScreen == AppScreen.HOME
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (homeActive) {
                                    Modifier
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    palette.accent.copy(alpha = 0.24f),
                                                    palette.accent.copy(alpha = 0.08f)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            palette.accentGlow.copy(alpha = 0.35f),
                                            RoundedCornerShape(14.dp)
                                        )
                                } else Modifier
                            )
                            .clickable { onNavigate(AppScreen.HOME) }
                            .testTag("nav_home")
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = strings.navHome,
                            tint = if (homeActive) palette.textPrimary else palette.textSecondary,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = strings.navHome,
                            color = if (homeActive) palette.textPrimary else palette.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (homeActive) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (homeActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 1.dp)
                                .size(6.dp)
                                .shadow(8.dp, CircleShape, ambientColor = palette.accentGlow, spotColor = palette.accentGlow)
                                .clip(CircleShape)
                                .background(palette.accentBright)
                        )
                    }
                }

                // 4. Tabs (Square counter badge)
                NavItemButton(
                    label = strings.navTabs,
                    isActive = currentScreen == AppScreen.TABS,
                    testTag = "nav_tabs",
                    onClick = { onNavigate(AppScreen.TABS) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(1.75.dp, tint, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabCount.coerceAtMost(99).toString(),
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 10.sp
                        )
                    }
                }

                // 5. Settings
                NavItemButton(
                    label = strings.navSettings,
                    isActive = currentScreen == AppScreen.SETTINGS,
                    testTag = "nav_settings",
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = strings.navSettings,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItemButton(
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable (Color) -> Unit
) {
    val palette = LocalMeoMeoPalette.current
    val tint = if (isActive) palette.accentBright else palette.textSecondary

    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            iconContent(tint)
        }
        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
