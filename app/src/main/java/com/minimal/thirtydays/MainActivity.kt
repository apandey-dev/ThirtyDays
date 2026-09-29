package com.minimal.thirtydays

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.minimal.thirtydays.data.AppDataManager
import com.minimal.thirtydays.model.DayItem
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.ui.screens.AddTaskScreen
import com.minimal.thirtydays.ui.screens.DayDetailScreen
import com.minimal.thirtydays.ui.screens.DayTimetableScreen
import com.minimal.thirtydays.ui.screens.HomeScreen
import com.minimal.thirtydays.ui.theme.ThirtyDaysTheme

sealed interface ScreenState {
    data object Home : ScreenState
    data class DayDetail(val dayItem: DayItem) : ScreenState
    data class DayTimetable(val dayItem: DayItem) : ScreenState
    data class AddTask(val dayItem: DayItem, val editTodo: TodoItem? = null) : ScreenState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val isSystemDark = isSystemInDarkTheme()
            var isDarkMode by remember {
                mutableStateOf(AppDataManager.isDarkMode(context, isSystemDark))
            }

            ThirtyDaysTheme(darkTheme = isDarkMode) {
                var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Home) }
                var dataVersion by remember { mutableStateOf(0) }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "main_screen_transition"
                    ) { state ->
                        when (state) {
                            is ScreenState.Home -> {
                                key(dataVersion) {
                                    HomeScreen(
                                        isDarkMode = isDarkMode,
                                        onToggleTheme = {
                                            val newMode = !isDarkMode
                                            isDarkMode = newMode
                                            AppDataManager.setDarkMode(context, newMode)
                                        },
                                        onDayClick = { day ->
                                            currentScreen = ScreenState.DayDetail(day)
                                        },
                                        onDayLongClick = { day ->
                                            currentScreen = ScreenState.DayTimetable(day)
                                        }
                                    )
                                }
                            }
                            is ScreenState.DayDetail -> {
                                DayDetailScreen(
                                    dayItem = state.dayItem,
                                    onBack = {
                                        currentScreen = ScreenState.Home
                                    }
                                )
                            }
                            is ScreenState.DayTimetable -> {
                                key(dataVersion) {
                                    DayTimetableScreen(
                                        dayItem = state.dayItem,
                                        onBack = {
                                            dataVersion++
                                            currentScreen = ScreenState.Home
                                        },
                                        onNavigateToAddTask = { editItem ->
                                            currentScreen = ScreenState.AddTask(state.dayItem, editItem)
                                        }
                                    )
                                }
                            }
                            is ScreenState.AddTask -> {
                                AddTaskScreen(
                                    dayItem = state.dayItem,
                                    editingTodo = state.editTodo,
                                    onBack = {
                                        currentScreen = ScreenState.DayTimetable(state.dayItem)
                                    },
                                    onTaskSaved = {
                                        dataVersion++
                                        currentScreen = ScreenState.DayTimetable(state.dayItem)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
