package com.sans.deepfocus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sans.deepfocus.domain.SessionMode
import com.sans.deepfocus.domain.SessionState

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val displayTime by viewModel.displayTime.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val sessionMode by viewModel.sessionMode.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val pomodoroDuration by viewModel.pomodoroDuration.collectAsState()

    val isMuted by viewModel.isMuted.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    var showSoundSelector by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    val backgroundColor = when (sessionMode) {
        SessionMode.POMODORO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
        SessionMode.STOPWATCH -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
    }

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

                TagSelector(
                    availableTags = availableTags,
                    selectedTag = selectedTag,
                    onTagSelect = { viewModel.selectTag(it) },
                    onAddTag = { showTagDialog = true },
                    onDeleteTag = { viewModel.deleteTag(it) }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sessionMode.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    selectedTag?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { onTagSelect(null) },
                    label = { Text("None") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            items(availableTags.size) { index ->
                val tag = availableTags[index]
                FilterChip(
                    selected = selectedTag == tag.name,
                    onClick = { onTagSelect(tag.name) },
                    label = { Text(tag.name) },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (selectedTag == tag.name) {
                            IconButton(
                                onClick = { onDeleteTag(tag) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                )
            }

            item {
                InputChip(
                    selected = false,
                    onClick = onAddTag,
                    label = { Text("Add Tag") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
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
                    if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
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
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                ),
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
    Box(
        modifier = Modifier
            .size(280.dp),
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
            fontSize = if (time.length > 5) 64.sp else 96.sp,
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
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { viewModel.stop() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            SessionState.PAUSED -> {
                IconButton(
                    onClick = { viewModel.resume() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { viewModel.stop() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
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
