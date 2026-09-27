package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.LatencyScienceScreen
import com.example.ui.screens.ReflexArenaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    ARENA("Reflex", Icons.Filled.Bolt, Icons.Outlined.Bolt, "tab_arena"),
    ANALYTICS("Analytics", Icons.Filled.InsertChart, Icons.Outlined.InsertChart, "tab_analytics"),
    SCIENCE("Science", Icons.Filled.Psychology, Icons.Outlined.Psychology, "tab_science"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

@Composable
fun MainAppScreen(
    viewModel: ReflexViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.ARENA) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val personalBest by viewModel.personalBest.collectAsStateWithLifecycle()
    val bestHardwareRecord by viewModel.bestHardwareRecord.collectAsStateWithLifecycle()
    val bestTouchRecord by viewModel.bestTouchRecord.collectAsStateWithLifecycle()
    val avgTime by viewModel.averageReactionTime.collectAsStateWithLifecycle()
    val avgHardwareTime by viewModel.averageHardwareReactionTime.collectAsStateWithLifecycle()
    val avgTouchTime by viewModel.averageTouchReactionTime.collectAsStateWithLifecycle()
    val totalTrials by viewModel.totalTrials.collectAsStateWithLifecycle()
    val totalFalseStarts by viewModel.totalFalseStarts.collectAsStateWithLifecycle()

    // BackHandler: Navigate back to Arena screen from any secondary tab
    BackHandler(enabled = currentTab != AppTab.ARENA) {
        currentTab = AppTab.ARENA
    }

    // When Pitch Black OLED is active during ARMED state, hide the navigation bar to prevent any OLED light bleed
    val hideNav = uiState.screenMode == ScreenMode.PITCH_BLACK_OLED && uiState.phase == TrialPhase.ARMED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!hideNav) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.ARENA -> {
                ReflexArenaScreen(
                    state = uiState,
                    onArmTrial = { viewModel.armTrial() },
                    onScreenTap = { viewModel.onScreenTap() },
                    onResetTrial = { viewModel.resetTrial() },
                    onSetStimulusType = { viewModel.setStimulusType(it) },
                    onSetSessionMode = { viewModel.setSessionMode(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.ANALYTICS -> {
                AnalyticsScreen(
                    records = allRecords,
                    personalBest = personalBest,
                    bestHardwareRecord = bestHardwareRecord,
                    bestTouchRecord = bestTouchRecord,
                    averageTime = avgTime,
                    averageHardwareTime = avgHardwareTime,
                    averageTouchTime = avgTouchTime,
                    totalTrials = totalTrials,
                    totalFalseStarts = totalFalseStarts,
                    onDeleteRecord = { viewModel.deleteRecord(it) },
                    onClearAll = { viewModel.clearAllHistory() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.SCIENCE -> {
                LatencyScienceScreen(
                    state = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.SETTINGS -> {
                SettingsScreen(
                    state = uiState,
                    onSetScreenMode = { viewModel.setScreenMode(it) },
                    onSetVibrationDuration = { viewModel.setVibrationDuration(it) },
                    onSetVibrationIntensity = { viewModel.setVibrationIntensity(it) },
                    onSetDelayRange = { min, max -> viewModel.setDelayRange(min, max) },
                    onClearAllHistory = { viewModel.clearAllHistory() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
