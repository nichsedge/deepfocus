package com.sans.deepfocus.ui

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sans.deepfocus.domain.SessionMode
import com.sans.deepfocus.domain.SessionState
import com.sans.deepfocus.ui.theme.Emerald500
import com.sans.deepfocus.ui.theme.Indigo500
import com.sans.deepfocus.ui.theme.Indigo600

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val displayTime by viewModel.displayTime.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val sessionMode by viewModel.sessionMode.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val pomodoroDuration by viewModel.pomodoroDuration.collectAsState()
    val progress by viewModel.progress.collectAsState()

    val isMuted by viewModel.isMuted.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    var showSoundSelector by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF04060A)
                    )
                )
            )
    ) {
        // Soft glowing ambient light at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode & Tag Switchers
            if (sessionState == SessionState.IDLE) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Custom Sliding Tab Switcher
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .fillMaxWidth(0.85f)
                            .background(
                                color = Color.White.copy(alpha = 0.04f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.07f),
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Pomodoro", "Stopwatch").forEach { text ->
                            val isSelected = (text == "Pomodoro" && sessionMode == SessionMode.POMODORO) ||
                                             (text == "Stopwatch" && sessionMode == SessionMode.STOPWATCH)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.setMode(if (text == "Pomodoro") SessionMode.POMODORO else SessionMode.STOPWATCH)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (sessionMode == SessionMode.POMODORO) {
                        DurationSelector(
                            currentMinutes = (pomodoroDuration / 60000).toInt(),
                            onDurationChange = { viewModel.setPomodoroDuration(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TagSelector(
                        availableTags = availableTags,
                        selectedTag = selectedTag,
                        onTagSelect = { viewModel.selectTag(it) },
                        onAddTag = { showTagDialog = true },
                        onDeleteTag = { viewModel.deleteTag(it) }
                    )
                }
            } else {
                // Focus Active Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = sessionMode.name,
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    selectedTag?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = it.uppercase(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Glowing Premium Circular Timer Display
            TimerDisplay(displayTime, progress, sessionState)

            // Dynamic Sleek Focus Controls
            FocusControls(sessionState, viewModel)

            // Sound Indicator
            SoundIndicator(
                selectedSound = selectedSound,
                isMuted = isMuted,
                onMuteToggle = { viewModel.toggleMute() }
            ) {
                showSoundSelector = true
            }

            if (showSoundSelector) {
                SoundSelectorDialog(viewModel) {
                    showSoundSelector = false
                }
            }

            if (showTagDialog) {
                AlertDialog(
                    onDismissRequest = { showTagDialog = false },
                    title = { Text("New Tag") },
                    text = {
                        OutlinedTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            label = { Text("Tag Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTagName.isNotBlank()) {
                                    viewModel.addTag(newTagName)
                                    newTagName = ""
                                    showTagDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTagDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TagSelector(
    availableTags: List<com.sans.deepfocus.data.TagEntity>,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    onAddTag: () -> Unit,
    onDeleteTag: (com.sans.deepfocus.data.TagEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                GlassChip(
                    selected = selectedTag == null,
                    onClick = { onTagSelect(null) },
                    text = "None"
                )
            }

            items(availableTags.size) { index ->
                val tag = availableTags[index]
                GlassChip(
                    selected = selectedTag == tag.name,
                    onClick = { onTagSelect(tag.name) },
                    text = tag.name,
                    onDelete = { onDeleteTag(tag) }
                )
            }

            item {
                GlassChip(
                    selected = false,
                    onClick = onAddTag,
                    text = "Add Tag",
                    isAdd = true
                )
            }
        }
    }
}

@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    onDelete: (() -> Unit)? = null,
    isAdd: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else Color.White.copy(alpha = 0.08f),
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAdd) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (selected && onDelete != null) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SoundIndicator(
    selectedSound: com.sans.deepfocus.data.SoundEntity?,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.07f), shape = CircleShape)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Waves,
                    contentDescription = "Sound Settings",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedSound?.name ?: "No Sound",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.07f), shape = CircleShape)
                .clickable { onMuteToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun DurationSelector(currentMinutes: Int, onDurationChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(15, 25, 45, 60).forEach { mins ->
            val isSelected = currentMinutes == mins
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.04f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
                    .clickable { onDurationChange(mins) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${mins}m",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TimerDisplay(time: String, progress: Float, state: SessionState) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.LinearOutSlowInEasing
        ),
        label = "timerProgress"
    )

    // Glowing Breathing Animation when session is actively running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (state == SessionState.RUNNING) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Box(
        modifier = Modifier
            .size(310.dp)
            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Deep background glow blur
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Sleek frosted glass timer ring
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // High-fidelity progress sweep
            androidx.compose.foundation.Canvas(modifier = Modifier.size(250.dp)) {
                val strokeWidthPx = 8.dp.toPx()
                // Track background
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.minDimension / 2 - strokeWidthPx / 2,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
                )

                // Neon active sweeping indicator
                if (progress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Indigo500,
                                Emerald500,
                                Indigo500
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidthPx,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
            }

            // Elegant high-end clocks text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time,
                    fontSize = if (time.length > 5) 54.sp else 76.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state) {
                        SessionState.RUNNING -> "FOCUSING"
                        SessionState.PAUSED -> "PAUSED"
                        else -> "READY"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (state == SessionState.RUNNING) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun FocusControls(state: SessionState, viewModel: TimerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            SessionState.IDLE -> {
                LargeStartButton {
                    viewModel.start()
                }
            }

            SessionState.RUNNING -> {
                ControlIconButton(icon = Icons.Default.Pause, isPrimary = true, onClick = { viewModel.pause() })
                Spacer(Modifier.width(28.dp))
                ControlIconButton(icon = Icons.Default.Stop, onClick = { viewModel.stop() })
            }

            SessionState.PAUSED -> {
                ControlIconButton(icon = Icons.Default.PlayArrow, isPrimary = true, onClick = { viewModel.resume() })
                Spacer(Modifier.width(28.dp))
                ControlIconButton(icon = Icons.Default.Stop, onClick = { viewModel.stop() })
            }
        }
    }
}

@Composable
fun ControlIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                if (isPrimary) Brush.linearGradient(colors = listOf(Indigo500, Indigo600))
                else Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
            )
            .border(
                width = 1.dp,
                color = if (isPrimary) Color.Transparent else Color.White.copy(alpha = 0.08f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = Color.White
        )
    }
}

@Composable
fun LargeStartButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(58.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Indigo500,
                        Indigo600
                    )
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "START FOCUS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
        }
    }
}
