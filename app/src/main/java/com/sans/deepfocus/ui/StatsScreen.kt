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
import kotlinx.coroutines.flow.map

class StatsViewModel(private val sessionDao: SessionDao) {
    private val today = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
    
    val totalFocusTimeToday = sessionDao.getTotalFocusTimeSince(today)
        .map { it ?: 0L }

    val sessionCountToday = sessionDao.getSessionCountSince(today)

    fun formatDuration(ms: Long): String {
        val hours = ms / (1000 * 60 * 60)
        val minutes = (ms / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val focusTime by viewModel.totalFocusTimeToday.collectAsState(initial = 0L)
    val sessionCount by viewModel.sessionCountToday.collectAsState(initial = 0)

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

        // Placeholder for History or Goal progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Weekly distribution Coming soon", style = MaterialTheme.typography.bodyMedium)
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
