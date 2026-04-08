package com.sans.deepfocus.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sans.deepfocus.domain.SessionMode
import com.sans.deepfocus.domain.SessionState

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val remainingTime by viewModel.remainingTime.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val sessionMode by viewModel.sessionMode.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val pomodoroDuration by viewModel.pomodoroDuration.collectAsState()
    
    val isMuted by viewModel.isMuted.collectAsState()
    
    var showSoundSelector by remember { mutableStateOf(false) }

    val displayTime = if (sessionMode == SessionMode.POMODORO) {
        viewModel.formatTime(remainingTime)
    } else {
        viewModel.formatTime(elapsedTime)
    }

    val backgroundColor by animateColorAsState(
        when (sessionMode) {
            SessionMode.POMODORO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
            SessionMode.STOPWATCH -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mode Selector
            if (sessionState == SessionState.IDLE) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton(
                        text = "Pomodoro",
                        isSelected = sessionMode == SessionMode.POMODORO,
                        onClick = { viewModel.setMode(SessionMode.POMODORO) } 
                    )
                    ModeButton(
                        text = "Stopwatch",
                        isSelected = sessionMode == SessionMode.STOPWATCH,
                        onClick = { viewModel.setMode(SessionMode.STOPWATCH) } 
                    )
                }
                
                if (sessionMode == SessionMode.POMODORO) {
                    DurationSelector(
                        currentMinutes = (pomodoroDuration / 60000).toInt(),
                        onDurationChange = { viewModel.setPomodoroDuration(it) }
                    )
                }
            } else {
                Text(
                    text = sessionMode.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            // Timer Display
            TimerDisplay(displayTime, sessionState)

            // Controls
            FocusControls(sessionState, viewModel)

            // Sound Selector Button
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
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp, 8.dp, 8.dp, 24.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Waves,
                    contentDescription = "Sound Settings",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedSound?.name ?: "No Sound",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        Spacer(Modifier.width(2.dp))
        
        Surface(
            onClick = onMuteToggle,
            shape = RoundedCornerShape(8.dp, 24.dp, 24.dp, 8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Icon(
                    if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text)
    }
}

@Composable
fun DurationSelector(currentMinutes: Int, onDurationChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(15, 25, 45, 60).forEach { mins ->
            val isSelected = currentMinutes == mins
            Surface(
                onClick = { onDurationChange(mins) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "${mins}m",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimerDisplay(time: String, state: SessionState) {
    val scale by animateFloatAsState(if (state == SessionState.RUNNING) 1.05f else 1f)
    
    Box(
        modifier = Modifier
            .size(280.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 8.dp
        )
        Text(
            text = time,
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
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
                IconButton(
                    onClick = { viewModel.pause() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { viewModel.stop() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(32.dp))
                }
            }
            SessionState.PAUSED -> {
                IconButton(
                    onClick = { viewModel.resume() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { viewModel.stop() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun LargeStartButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(80.dp)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(40.dp))
    }
}
