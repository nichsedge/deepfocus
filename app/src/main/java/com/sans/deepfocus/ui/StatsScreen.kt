package com.sans.deepfocus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sans.deepfocus.data.SessionDao
import com.sans.deepfocus.data.SessionEntity
import kotlinx.coroutines.flow.map
import java.time.*
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

class StatsViewModel(private val sessionDao: SessionDao) {
    private fun getStartOfToday(): Long {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    val totalFocusTimeToday = sessionDao.getTotalFocusTimeSince(getStartOfToday())
        .map { it ?: 0L }

    val sessionCountToday = sessionDao.getSessionCountSince(getStartOfToday())

    val weeklyDistribution = sessionDao.getSessionsSince(
        LocalDate.now().minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    ).map { sessions ->
        val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
        last7Days.map { date ->
            val dailySum = sessions.filter {
                Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate() == date
            }.sumOf { it.duration }
            DayFocus(
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                durationMs = dailySum
            )
        }
    }

    fun formatDuration(ms: Long): String {
        val hours = ms / (1000 * 60 * 60)
        val minutes = (ms / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

data class DayFocus(val label: String, val durationMs: Long)

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val focusTime by viewModel.totalFocusTimeToday.collectAsState(initial = 0L)
    val sessionCount by viewModel.sessionCountToday.collectAsState(initial = 0)
    val weeklyData by viewModel.weeklyDistribution.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Focus Time",
                    value = viewModel.formatDuration(focusTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sessions",
                    value = sessionCount.toString(),
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Distribution Chart
        item {
            WeeklyDistributionChart(weeklyData)
        }
    }
}

@Composable
fun WeeklyDistributionChart(data: List<DayFocus>) {
    val maxDuration = data.maxOfOrNull { it.durationMs }?.coerceAtLeast(1L) ?: 1L
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text(
                "Weekly Activity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { dayFocus ->
                    val barHeightProportion = remember(dayFocus.durationMs, maxDuration) {
                        dayFocus.durationMs.toFloat() / maxDuration
                    }
                    
                    val animatedHeight by animateFloatAsState(
                        targetValue = barHeightProportion,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "barHeight"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(animatedHeight.coerceAtLeast(0.02f))
                                .width(12.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (dayFocus.durationMs > 0) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            dayFocus.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(value, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
