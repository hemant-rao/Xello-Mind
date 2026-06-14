package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ActivePracticeScreen
import com.example.ui.DashboardScreen
import com.example.ui.LeaderboardScreen
import com.example.ui.PracticeListScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MemoryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MemoryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val allItems by viewModel.allItems.collectAsState()
            val allLogs by viewModel.allLogs.collectAsState()
            val leaderboard by viewModel.leaderboard.collectAsState()
            val challenges by viewModel.challenges.collectAsState()

            val currentTab by viewModel.currentTab.collectAsState()
            val activeItem by viewModel.activeItem.collectAsState()

            // Safe reactive theme evaluation: defaults to true (dark) if profile is loading
            val useDarkTheme = userProfile?.isDarkMode ?: true

            MyApplicationTheme(darkTheme = useDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        "dashboard" -> "NeuroDash Analytics"
                                        "practice_list" -> "Memory Gym"
                                        "active_practice" -> "Practice Session"
                                        "leaderboard" -> "Social Leaderboard"
                                        else -> "User Preferences"
                                    },
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        // Sticky bottom navigation bar (edge-to-edge safe)
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            val tabs = listOf(
                                Triple("dashboard", "Dashboard", Icons.Default.Analytics),
                                Triple("practice_list", "Practice", Icons.Default.Psychology),
                                Triple("leaderboard", "League", Icons.Default.EmojiEvents),
                                Triple("settings", "Settings", Icons.Default.Settings)
                            )

                            tabs.forEach { (tabId, label, icon) ->
                                val isSelected = currentTab == tabId || (tabId == "practice_list" && currentTab == "active_practice")
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tabId) },
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(text = label, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("nav_item_$tabId"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transit"
                        ) { targetTab ->
                            when (targetTab) {
                                "dashboard" -> {
                                    userProfile?.let { prof ->
                                        DashboardScreen(
                                            viewModel = viewModel,
                                            profile = prof,
                                            logs = allLogs
                                        )
                                    } ?: Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                "practice_list" -> {
                                    PracticeListScreen(
                                        viewModel = viewModel,
                                        items = allItems
                                    )
                                }
                                "active_practice" -> {
                                    activeItem?.let { item ->
                                        ActivePracticeScreen(
                                            viewModel = viewModel,
                                            item = item
                                        )
                                    } ?: viewModel.setTab("practice_list")
                                }
                                "leaderboard" -> {
                                    LeaderboardScreen(
                                        viewModel = viewModel,
                                        entries = leaderboard,
                                        challenges = challenges
                                    )
                                }
                                "settings" -> {
                                    userProfile?.let { prof ->
                                        SettingsScreen(
                                            viewModel = viewModel,
                                            profile = prof
                                        )
                                    } ?: Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
