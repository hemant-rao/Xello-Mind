package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Alignment
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
        // §778 — config-driven OdioBook ads (admin-controlled). Inits MobileAds +
        // fetches this app's slice; no-op until ads are enabled for "xellomind".
        com.example.ads.OdioBookAds.init(applicationContext, "xellomind")

        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val allItems by viewModel.visibleItems.collectAsState()
            val allLogs by viewModel.allLogs.collectAsState()
            val leaderboard by viewModel.leaderboard.collectAsState()
            val challenges by viewModel.challenges.collectAsState()

            val currentTab by viewModel.currentTab.collectAsState()
            val activeItem by viewModel.activeItem.collectAsState()

            // Theme customization parameters
            val themeAccent by viewModel.themeAccent.collectAsState()
            val themeTypography by viewModel.themeTypography.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()

            // Safe reactive theme evaluation: defaults to true (dark) if profile is loading
            val useDarkTheme = userProfile?.isDarkMode ?: true

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp >= 600

            MyApplicationTheme(
                darkTheme = useDarkTheme,
                accent = themeAccent,
                typographyStyle = themeTypography,
                fontScale = fontScale
            ) {
                val canGoBack by viewModel.canGoBack.collectAsState()
                BackHandler(enabled = canGoBack) {
                    viewModel.navigateBack()
                }

                if (isWideScreen) {
                    // Modern side-by-side adaptive layout for tablets & wide screens
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical))
                                .testTag("side_nav_rail"),
                            containerColor = MaterialTheme.colorScheme.background,
                            header = {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Xello Mind Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(vertical = 24.dp)
                                        .size(36.dp)
                                )
                            }
                        ) {
                            val railTabs = listOf(
                                Triple("practice_list", "Practice", Icons.Default.Psychology),
                                Triple("dashboard", "Analytics", Icons.Default.Analytics),
                                Triple("leaderboard", "League", Icons.Default.EmojiEvents),
                                Triple("settings", "Settings", Icons.Default.Settings)
                            )
                            Spacer(Modifier.height(16.dp))
                            railTabs.forEach { (tabId, label, icon) ->
                                val isSelected = currentTab == tabId || (tabId == "practice_list" && currentTab == "active_practice")
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tabId) },
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(text = label, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("rail_item_$tabId"),
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        Scaffold(
                            modifier = Modifier.weight(1f),
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            text = when (currentTab) {
                                                "dashboard" -> "Performance Analytics"
                                                "practice_list" -> "Practice"
                                                "active_practice" -> "Vocal Practice"
                                                "leaderboard" -> "Leaderboard"
                                                else -> "Settings"
                                            },
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
                                    navigationIcon = {
                                        if (canGoBack && currentTab == "active_practice") {
                                            IconButton(
                                                onClick = { viewModel.navigateBack() },
                                                modifier = Modifier.testTag("top_bar_back_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        titleContentColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            },
                            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 960.dp)
                                        .fillMaxWidth()
                                ) {
                                    ScreenContent(
                                        currentTab = currentTab,
                                        viewModel = viewModel,
                                        userProfile = userProfile,
                                        allItems = allItems,
                                        allLogs = allLogs,
                                        leaderboard = leaderboard,
                                        challenges = challenges,
                                        activeItem = activeItem
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Traditional single column layout with bottom bar for mobile portrait
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = when (currentTab) {
                                            "dashboard" -> "Performance Analytics"
                                            "practice_list" -> "Practice"
                                            "active_practice" -> "Vocal Practice"
                                            "leaderboard" -> "Leaderboard"
                                            else -> "Settings"
                                        },
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                navigationIcon = {
                                    if (canGoBack && currentTab == "active_practice") {
                                        IconButton(
                                            onClick = { viewModel.navigateBack() },
                                            modifier = Modifier.testTag("top_bar_back_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (currentTab == "practice_list" || currentTab == "dashboard") {
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier.testTag("top_bar_menu_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "More options",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Practice Logs", fontWeight = FontWeight.SemiBold) },
                                                    onClick = {
                                                        showMenu = false
                                                        viewModel.setTab("dashboard")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.History,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("menu_history_btn")
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Leaderboard", fontWeight = FontWeight.SemiBold) },
                                                    onClick = {
                                                        showMenu = false
                                                        viewModel.setTab("leaderboard")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.EmojiEvents,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("menu_league_btn")
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                                                    onClick = {
                                                        showMenu = false
                                                        viewModel.setTab("settings")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Settings,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("menu_settings_btn")
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("bottom_nav_bar")
                            ) {
                                val tabs = listOf(
                                    Triple("practice_list", "Practice", Icons.Default.Psychology),
                                    Triple("dashboard", "Analytics", Icons.Default.Analytics)
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
                            ScreenContent(
                                currentTab = currentTab,
                                viewModel = viewModel,
                                userProfile = userProfile,
                                allItems = allItems,
                                allLogs = allLogs,
                                leaderboard = leaderboard,
                                challenges = challenges,
                                activeItem = activeItem
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ScreenContent(
        currentTab: String,
        viewModel: MemoryViewModel,
        userProfile: com.example.data.UserProfile?,
        allItems: List<com.example.data.PracticeItem>,
        allLogs: List<com.example.data.PracticeLog>,
        leaderboard: List<com.example.data.LeaderboardEntry>,
        challenges: List<com.example.viewmodel.SocialChallenge>,
        activeItem: com.example.data.PracticeItem?
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
                        contentAlignment = Alignment.Center
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
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
