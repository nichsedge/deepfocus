package com.sans.deepfocus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sans.deepfocus.analytics.AnalyticsProvider
import com.sans.deepfocus.data.AppDatabase
import com.sans.deepfocus.data.SoundEntity
import com.sans.deepfocus.domain.TimerManager
import com.sans.deepfocus.ui.StatsScreen
import com.sans.deepfocus.ui.StatsViewModel
import com.sans.deepfocus.ui.TimerScreen
import com.sans.deepfocus.ui.TimerViewModel
import com.sans.deepfocus.ui.theme.DeepfocusTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var timerManager: TimerManager
    private val analytics = AnalyticsProvider.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getInstance(this)
        timerManager = TimerManager.getInstance(database.sessionDao())

        initializeData()

        enableEdgeToEdge()
        setContent {
            val timerViewModel = remember {
                TimerViewModel(
                    application,
                    timerManager,
                    database.soundDao(),
                    database.tagDao()
                )
            }
            val statsViewModel = remember {
                StatsViewModel(database.sessionDao(), database.tagDao())
            }

            DeepfocusTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Simple screen tracking
                LaunchedEffect(currentRoute) {
                    currentRoute?.let {
                        analytics.trackScreen(it)
                    }
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                                label = { Text("Timer") },
                                selected = currentRoute == "timer",
                                onClick = {
                                    navController.navigate("timer") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Analytics,
                                        contentDescription = "Stats"
                                    )
                                },
                                label = { Text("Stats") },
                                selected = currentRoute == "stats",
                                onClick = {
                                    navController.navigate("stats") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "timer",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("timer") {
                            TimerScreen(timerViewModel)
                        }
                        composable("stats") {
                            StatsScreen(statsViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun initializeData() {
        lifecycleScope.launch {
            // Initialize Sounds
            val sounds = database.soundDao().getAllSounds().first()
            if (sounds.isEmpty()) {
                database.soundDao().insertSound(
                    SoundEntity(name = "No Sound", uri = "", isSelected = true)
                )
            }

            // Initialize Tags
            val tags = database.tagDao().getAllTags().first()
            if (tags.isEmpty()) {
                listOf("Work", "Study", "Exercise", "Other").forEach {
                    database.tagDao().insertTag(com.sans.deepfocus.data.TagEntity(it))
                }
            }
        }
    }
}