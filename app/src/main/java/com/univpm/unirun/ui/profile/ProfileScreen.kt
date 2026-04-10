package com.univpm.unirun.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univpm.unirun.ui.theme.KineticTheme
import com.univpm.unirun.viewmodel.HistoryItem
import java.util.Locale

@Composable
fun ProfileScreen(
    userName: String,
    totalDistanceKm: Float,
    totalActivities: Int,
    avgPaceFormatted: String,
    totalCalories: Int,
    currentMonthLabel: String,
    monthlyKm: Float,
    monthlyTimeFormatted: String,
    groupedHistory: List<HistoryItem>,
    joinedLabel: String,
    locationLabel: String,
    onEditClick: () -> Unit,
    onActivityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    KineticTheme {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 48.sp,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    Text(
                        text = userName.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = joinedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Text("EDIT PROFILE", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            // All-Time Stats
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("ALL-TIME STATS", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(label = "Distance", value = String.format(Locale.getDefault(), "%.1f km", totalDistanceKm), modifier = Modifier.weight(1f))
                        StatCard(label = "Activities", value = totalActivities.toString(), modifier = Modifier.weight(1f))
                        StatCard(label = "Avg Pace", value = avgPaceFormatted, modifier = Modifier.weight(1f))
                        StatCard(label = "Calories", value = totalCalories.toString(), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Monthly Stats
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(currentMonthLabel, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(label = "This Month", value = String.format(Locale.getDefault(), "%.1f km", monthlyKm), modifier = Modifier.weight(1f))
                        StatCard(label = "Time", value = monthlyTimeFormatted, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Activity History
            item {
                Text("ACTIVITY HISTORY", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }

            items(groupedHistory) { item ->
                when (item) {
                    is HistoryItem.MonthHeader -> {
                        HistoryMonthHeader(item = item, modifier = Modifier.fillMaxWidth())
                    }
                    is HistoryItem.ActivityRow -> {
                        HistoryActivityItem(item = item, onClick = { onActivityClick(item.id) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun HistoryMonthHeader(item: HistoryItem.MonthHeader, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(String.format(Locale.getDefault(), "%.1f KM", item.totalKm), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun HistoryActivityItem(item: HistoryItem.ActivityRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 4.dp)) {
                Text(item.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.monthAbbr, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.titleName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(item.sportType, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.getDefault(), "%.1f km", item.distanceKm), style = MaterialTheme.typography.bodySmall)
                Text(item.durationFormatted, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
