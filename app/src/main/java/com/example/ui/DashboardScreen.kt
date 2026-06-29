package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeLog
import com.example.data.UserProfile
import com.example.viewmodel.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: MemoryViewModel,
    profile: UserProfile,
    logs: List<PracticeLog>,
    modifier: Modifier = Modifier
) {
    var rangeFilter by remember { mutableStateOf("Weekly") } // "Weekly" or "Monthly"

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp)
    ) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StreakBanner(profile)

                    // Analytical Progress Graph
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analytics_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Accuracy Trend",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Based on memory recognition scores",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Filter Pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                        .padding(2.dp)
                                ) {
                                    listOf("Weekly", "Monthly").forEach { filter ->
                                        val selected = rangeFilter == filter
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .clickable { rangeFilter = filter }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = filter,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            // Render Custom Canvas Graph
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val points = remember(logs, rangeFilter) {
                                    getAnalyticsPoints(logs, rangeFilter)
                                }

                                if (points.isEmpty()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Practice more to generate metrics",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    AnalyticsGraph(
                                        points = points,
                                        colorScheme = MaterialTheme.colorScheme
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatsSummaryRow(logs)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Practices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (logs.isNotEmpty()) {
                            Text(
                                text = "Last ${logs.size.coerceAtMost(5)} tries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    if (logs.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No practices recorded yet",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Head to the Practice tab to begin speaking!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        logs.take(5).forEach { log ->
                            PracticeHistoryRow(log)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // Welcome & Streak Banner
                item {
                    StreakBanner(profile)
                }

                // Analytical Progress Graph
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analytics_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Accuracy Trend",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Based on memory recognition scores",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Filter Pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                        .padding(2.dp)
                                ) {
                                    listOf("Weekly", "Monthly").forEach { filter ->
                                        val selected = rangeFilter == filter
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .clickable { rangeFilter = filter }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = filter,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            // Render Custom Canvas Graph
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val points = remember(logs, rangeFilter) {
                                    getAnalyticsPoints(logs, rangeFilter)
                                }

                                if (points.isEmpty()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Practice more to generate metrics",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    AnalyticsGraph(
                                        points = points,
                                        colorScheme = MaterialTheme.colorScheme
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Practices List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Practices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (logs.isNotEmpty()) {
                            Text(
                                text = "Last ${logs.size.coerceAtMost(5)} tries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (logs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No practices recorded yet",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Head to the Practice tab to begin speaking!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(logs.take(5)) { log ->
                        PracticeHistoryRow(log)
                    }
                }

                // Stats Summary Cards
                item {
                    StatsSummaryRow(logs)
                }
                // §778 — config-driven OdioBook banner (admin-controlled; no-op until
                // ads are enabled for "xellomind"). Bottom of the dashboard list.
                item {
                    com.example.ads.OdioBookAds.Banner(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun StreakBanner(profile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_banner"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back, ${profile.username}!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (profile.currentStreak > 1) {
                        "Brilliant! Keep updating your memory synapses daily."
                    } else {
                        "Practice today to build your consecutive memory habit!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Points",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${profile.totalXp} Points",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Subtle glowing badge
            MemoryStreakBadge(streakCount = profile.currentStreak)
        }
    }
}

@Composable
fun MemoryStreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val glowColor = primaryColor.copy(alpha = 0.28f)
    
    Box(
        modifier = modifier
            .padding(12.dp)
            .drawBehind {
                // Subtle primary brand glow effect using dual translucent overlay circles
                drawCircle(
                    color = glowColor,
                    radius = size.minDimension * 0.72f,
                    center = center
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.95f,
                    center = center
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = "Streak Flame",
                tint = primaryColor,
                modifier = Modifier.size(22.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$streakCount Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "MEMORY STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun StatsSummaryRow(logs: List<PracticeLog>) {
    val totalPracticed = logs.size
    val averageScore = if (totalPracticed > 0) logs.map { it.accuracyScore }.average().toInt() else 0
    val totalDurationMin = if (totalPracticed > 0) (logs.sumOf { it.durationMs } / 60000).toInt() else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Avg Accuracy",
            value = "$averageScore%",
            icon = Icons.Filled.TrendingUp,
            color = Color(0xFF0D9488),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Practiced",
            value = totalPracticed.toString(),
            icon = Icons.Filled.Mic,
            color = Color(0xFF0EA5E9),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Mind Active",
            value = "${totalDurationMin}m",
            icon = Icons.Filled.Timer,
            color = Color(0xFFF43F5E),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnalyticsGraph(
    points: List<Int>,
    colorScheme: ColorScheme
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxPoints = points.size
        val stepX = width / (if (maxPoints > 1) maxPoints - 1 else 1)

        val strokeWidth = 3.dp.toPx()
        val dotRadius = 5.dp.toPx()

        // Helper to map score to Y pixel coordinate
        fun mapY(score: Int): Float {
            // scale 0-100 to map exactly inside graph heights
            val percentage = score / 100f
            return height - (percentage * (height - 40.dp.toPx())) - 10.dp.toPx()
        }

        // Draw reference gridlines
        val gridLines = listOf(25, 50, 75, 100)
        gridLines.forEach { value ->
            val y = mapY(value)
            drawLine(
                color = colorScheme.onSurface.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, score ->
            val x = index * stepX
            val y = mapY(score)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw Filled Area Gradient under line
        val gradient = Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.35f),
                colorScheme.primary.copy(alpha = 0.0f)
            )
        )
        drawPath(path = fillPath, brush = gradient)

        // Draw Connected Line
        drawPath(
            path = path,
            color = colorScheme.primary,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw Dots and numerical badges
        points.forEachIndexed { index, score ->
            val x = index * stepX
            val y = mapY(score)

            // Outer white glow
            drawCircle(
                color = colorScheme.background,
                radius = dotRadius + 1.dp.toPx(),
                center = Offset(x, y)
            )
            // Color core
            drawCircle(
                color = colorScheme.primary,
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun PracticeLauncherCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Begin Practice Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Recite words, sentences, and paragraphs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PracticeHistoryRow(log: PracticeLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Difficulty color badge
            val (badgeColor, label) = when (log.accuracyScore) {
                in 90..100 -> Pair(Color(0xFF0D9488), "Perfect")
                in 75..89 -> Pair(Color(0xFF0EA5E9), "Good")
                else -> Pair(Color(0xFFF59E0B), "Review")
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${log.accuracyScore}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.originalText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val itemTypeNatural = when (log.itemType.uppercase()) {
                    "WORD" -> "Word"
                    "SENTENCE" -> "Sentence"
                    "PARAGRAPH" -> "Paragraph"
                    else -> log.itemType.lowercase().replaceFirstChar { it.uppercase() }
                }
                Text(
                    text = "$itemTypeNatural · ${getFormattedTime(log.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = label,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

// Maps recent history into average scores
private fun getAnalyticsPoints(logs: List<PracticeLog>, range: String): List<Int> {
    val items = logs.sortedBy { it.timestamp }
    val limit = if (range == "Weekly") 7 else 15
    val scores = items.takeLast(limit).map { it.accuracyScore }
    // If empty or only 1, pad it with a default starter point so the curve looks great
    return if (scores.isEmpty()) {
        emptyList()
    } else if (scores.size == 1) {
        listOf(60, scores[0])
    } else {
        scores
    }
}

private fun getFormattedTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
