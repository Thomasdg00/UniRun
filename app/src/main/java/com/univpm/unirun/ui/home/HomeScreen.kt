package com.univpm.unirun.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.ui.components.KineticButton
import com.univpm.unirun.ui.theme.KineticTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    weeklyKm: Double,
    activeTimeFormatted: String,
    activeTimeDelta: String,
    intensityKcal: Int,
    streak: Int,
    recoveryStatus: String,
    recentActivities: List<ActivityEntity>,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStartActivityClick: () -> Unit,
    onActivityClick: (ActivityEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    KineticTheme {
        val listState = rememberLazyListState()

        Scaffold(
            topBar = {
                HomeTopAppBar(
                    onMenuClick = onMenuClick,
                    onSettingsClick = onSettingsClick
                )
            },
            bottomBar = {
                // Placeholder for bottom nav; will be part of MainActivity shell later
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                state = listState
            ) {
                // Weekly KM stat
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Text(
                            text = "THIS WEEK",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            letterSpacing = 0.2.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", weeklyKm),
                                style = MaterialTheme.typography.headlineLarge,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.04).sp,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = "KM",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bento grid: Active Time + Intensity
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Active Time Card
                            BentoCard(
                                modifier = Modifier.weight(1f),
                                label = "ACTIVE TIME",
                                value = activeTimeFormatted,
                                delta = activeTimeDelta
                            )
                            // Intensity Card
                            BentoCard(
                                modifier = Modifier.weight(1f),
                                label = "INTENSITY",
                                value = intensityKcal.toString(),
                                delta = "KCAl"
                            )
                        }
                    }
                }

                // Recovery Status Card
                item {
                    RecoveryCard(
                        recoveryStatus = recoveryStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                // Streak
                item {
                    Text(
                        text = "STREAK: ${streak}D",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        fontStyle = FontStyle.Italic
                    )
                }

                // START ACTIVITY Button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .clickable { onStartActivityClick() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "START ACTIVITY",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp
                            )
                            Card(
                                modifier = Modifier
                                    .size(40.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Start Activity",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    tint = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                    }
                }

                // Recent Feed Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT FEED",
                            style = MaterialTheme.typography.headlineSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "VIEW ALL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            letterSpacing = 0.18.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.clickable {
                                // Scroll to top
                            }
                        )
                    }
                }

                // Recent Activities
                items(recentActivities, key = { it.id }) { activity ->
                    ActivityItemRow(
                        activity = activity,
                        onActivityClick = { onActivityClick(activity) }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTopAppBar(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "KINETIC",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            fontSize = 24.sp,
            letterSpacing = (-0.04).sp,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BentoCard(
    label: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                letterSpacing = 0.18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.padding(top = 18.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = delta,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun RecoveryCard(
    recoveryStatus: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TARGET RECOVERY",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    letterSpacing = 0.18.sp,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    text = recoveryStatus,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Power bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            // Emoji overlay
            Text(
                text = "⚡",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 8.dp)
                    .alpha(0.1f),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActivityItemRow(
    activity: ActivityEntity,
    onActivityClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onActivityClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val calendar = Calendar.getInstance().apply { timeInMillis = activity.startTimestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val timeOfDay = when {
                hour < 12 -> "Morning"
                hour < 18 -> "Afternoon"
                else -> "Evening"
            }
            val sport = activity.sportType.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            Text(
                text = "${dateFormat.format(Date(activity.startTimestamp)).uppercase(Locale.getDefault())} · ${sport.uppercase(Locale.getDefault())}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$timeOfDay $sport",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (activity.distanceMeters >= 1000f) {
                            String.format(Locale.getDefault(), "%.1f", activity.distanceMeters / 1000f)
                        } else {
                            activity.distanceMeters.roundToInt().toString()
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activity.distanceMeters >= 1000f) "KM" else "M",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDuration(activity.durationSeconds),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatPace(activity.distanceMeters, activity.durationSeconds),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pace",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun formatPace(distanceMeters: Float, durationSeconds: Long): String {
    if (distanceMeters <= 0f || durationSeconds <= 0L) return "--:--"
    val secondsPerKm = durationSeconds / (distanceMeters / 1000f)
    if (!secondsPerKm.isFinite()) return "--:--"
    val totalSeconds = secondsPerKm.roundToInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
