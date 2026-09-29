package com.minimal.thirtydays.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.data.AppDataManager
import com.minimal.thirtydays.model.DayItem
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Dedicated New Task / Edit Task Screen matching image one.png:
 * - Clean "new task" header with NO back arrow icon.
 * - "task" input field with "what needs to be done?" placeholder.
 * - "schedule (optional)" section with 24-hour TimePicker BottomSheet for start & end times.
 * - "notes (optional)" section with rounded card and "add notes..." placeholder.
 * - "slide to add to today" bottom slider button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    dayItem: DayItem,
    editingTodo: TodoItem? = null,
    onBack: () -> Unit,
    onTaskSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()

    var taskTitle by remember { mutableStateOf(editingTodo?.title ?: "") }
    var notesText by remember { mutableStateOf(editingTodo?.subDetails ?: "") }

    var startTime by remember {
        mutableStateOf(
            editingTodo?.timeRange?.split("-")?.getOrNull(0)?.trim() ?: ""
        )
    }
    var endTime by remember {
        mutableStateOf(
            editingTodo?.timeRange?.split("-")?.getOrNull(1)?.trim() ?: ""
        )
    }

    var isPickingStartTime by remember { mutableStateOf(false) }
    var isPickingEndTime by remember { mutableStateOf(false) }

    val saveTaskAction: () -> Unit = {
        if (taskTitle.trim().isBlank()) {
            Toast.makeText(context, "Please enter what needs to be done", Toast.LENGTH_SHORT).show()
        } else {
            val currentTodos = AppDataManager.loadTodosForDay(context, dayItem.dayNumber)
            val computedTimeRange = if (startTime.isNotBlank() && endTime.isNotBlank()) {
                "$startTime - $endTime"
            } else if (startTime.isNotBlank()) {
                startTime
            } else if (editingTodo != null) {
                editingTodo.timeRange
            } else {
                "09:00 - 10:00"
            }

            val updatedList = if (editingTodo != null) {
                currentTodos.map {
                    if (it.id == editingTodo.id) {
                        it.copy(
                            title = taskTitle.trim(),
                            timeRange = computedTimeRange,
                            subDetails = notesText.trim(),
                            category = it.category.ifBlank { "General" }
                        )
                    } else it
                }
            } else {
                currentTodos + TodoItem(
                    id = "${dayItem.dayNumber}-${UUID.randomUUID().toString().take(6)}",
                    title = taskTitle.trim(),
                    timeRange = computedTimeRange,
                    subDetails = notesText.trim(),
                    category = "General",
                    isCompleted = false
                )
            }

            AppDataManager.saveTodosForDay(context, dayItem.dayNumber, updatedList)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, if (editingTodo != null) "Task updated!" else "Task added!", Toast.LENGTH_SHORT).show()
            onTaskSaved()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = safeInsets.calculateTopPadding() + 8.dp,
                    bottom = safeInsets.calculateBottomPadding() + 16.dp
                )
        ) {
            // Scrollable Form Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .imePadding()
            ) {
                // Header (No back icon as requested)
                Text(
                    text = if (editingTodo != null) "edit task" else "new task",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = extendedColors.textPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // 1. Task Label & Input
                Text(
                    text = "task",
                    fontFamily = PlaypenFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = extendedColors.textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                BasicTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    textStyle = TextStyle(
                        fontFamily = PlaypenFontFamily,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.textPrimary
                    ),
                    cursorBrush = SolidColor(extendedColors.textPrimary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                                if (taskTitle.isEmpty()) {
                                    Text(
                                        text = "what needs to be done?",
                                        fontFamily = PlaypenFontFamily,
                                        fontSize = 16.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = extendedColors.textTertiary.copy(alpha = 0.8f)
                                    )
                                }
                                innerTextField()
                            }
                            // Thin bottom underline
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(extendedColors.border)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 2. Schedule (optional) Label & Rows
                Text(
                    text = "schedule (optional)",
                    fontFamily = PlaypenFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = extendedColors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule Rounded Container (Start and End rows)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Time Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isPickingStartTime = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, extendedColors.border.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "start",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = extendedColors.textPrimary
                            )
                            Text(
                                text = if (startTime.isNotBlank()) startTime else "set time",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = if (startTime.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (startTime.isNotBlank()) extendedColors.textPrimary else extendedColors.textTertiary
                            )
                        }
                    }

                    // End Time Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isPickingEndTime = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, extendedColors.border.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "end",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = extendedColors.textPrimary
                            )
                            Text(
                                text = if (endTime.isNotBlank()) endTime else "set time",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = if (endTime.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (endTime.isNotBlank()) extendedColors.textPrimary else extendedColors.textTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 3. Notes (optional) Label & Box
                Text(
                    text = "notes (optional)",
                    fontFamily = PlaypenFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = extendedColors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, extendedColors.border.copy(alpha = 0.6f))
                ) {
                    BasicTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        textStyle = TextStyle(
                            fontFamily = PlaypenFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = extendedColors.textPrimary,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(extendedColors.textPrimary),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (notesText.isEmpty()) {
                                    Text(
                                        text = "add notes...",
                                        fontFamily = PlaypenFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = extendedColors.textTertiary.copy(alpha = 0.8f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom "Slide to add to today" Button Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                SlideToConfirmButton(
                    text = if (editingTodo != null) "slide to update task" else "slide to add to today",
                    onConfirmed = saveTaskAction
                )
            }
        }

        // 24-Hour TimePicker BottomSheets
        if (isPickingStartTime) {
            TimePickerBottomSheet(
                title = "set start time (24h)",
                initialTimeString = startTime,
                onTimeSelected = { formatted ->
                    startTime = formatted
                    isPickingStartTime = false
                },
                onDismiss = { isPickingStartTime = false }
            )
        }

        if (isPickingEndTime) {
            TimePickerBottomSheet(
                title = "set end time (24h)",
                initialTimeString = endTime,
                onTimeSelected = { formatted ->
                    endTime = formatted
                    isPickingEndTime = false
                },
                onDismiss = { isPickingEndTime = false }
            )
        }
    }
}

/**
 * Interactive Slide-to-Confirm Button matching the pill slider in one.png
 */
@Composable
private fun SlideToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val thumbOffset = remember { Animatable(0f) }
    var maxDragWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbSizeDp = 48.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, extendedColors.border.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        maxDragWidthPx = (totalWidthPx - thumbSizePx - with(density) { 8.dp.toPx() }).coerceAtLeast(0f)

        // Center Prompt Text
        Text(
            text = text,
            fontFamily = PlaypenFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            color = extendedColors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )

        // Draggable Circular Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.value.roundToInt() + with(density) { 4.dp.toPx().roundToInt() }, 0) }
                .size(thumbSizeDp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, extendedColors.border, CircleShape)
                .clickable {
                    // Tap to auto-slide & confirm
                    scope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        thumbOffset.animateTo(maxDragWidthPx, animationSpec = tween(250))
                        onConfirmed()
                        thumbOffset.snapTo(0f)
                    }
                }
                .pointerInput(maxDragWidthPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (thumbOffset.value >= maxDragWidthPx * 0.65f) {
                                scope.launch {
                                    thumbOffset.animateTo(maxDragWidthPx, animationSpec = tween(120))
                                    onConfirmed()
                                    thumbOffset.animateTo(0f, animationSpec = tween(200))
                                }
                            } else {
                                scope.launch {
                                    thumbOffset.animateTo(0f, animationSpec = tween(200))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { thumbOffset.animateTo(0f, animationSpec = tween(200)) }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newTarget = (thumbOffset.value + dragAmount).coerceIn(0f, maxDragWidthPx)
                            scope.launch { thumbOffset.snapTo(newTarget) }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Slide to confirm",
                tint = extendedColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 24-Hour TimePicker ModalBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerBottomSheet(
    title: String,
    initialTimeString: String = "",
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Parse initial hour and minute if available
    val (parsedHour, parsedMinute) = remember(initialTimeString) {
        try {
            if (initialTimeString.contains(":")) {
                val parts = initialTimeString.split(":")
                val h = parts[0].trim().toIntOrNull() ?: 12
                val m = parts[1].trim().take(2).toIntOrNull() ?: 0
                Pair(h, m)
            } else {
                val now = LocalTime.now()
                Pair(now.hour, now.minute)
            }
        } catch (e: Exception) {
            val now = LocalTime.now()
            Pair(now.hour, now.minute)
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = parsedHour,
        initialMinute = parsedMinute,
        is24Hour = true // 24-Hour Format requested
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 10.dp)
                    .size(width = 36.dp, height = 3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extendedColors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = PlaypenFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = extendedColors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectorColor = extendedColors.todayBadgeBg,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val formattedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(formattedTime)
                    },
                shape = RoundedCornerShape(12.dp),
                color = extendedColors.todayBadgeBg,
                border = BorderStroke(1.dp, extendedColors.borderFocused)
            ) {
                Text(
                    text = "CONFIRM TIME",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = extendedColors.todayBadgeText,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}
