package com.minimal.thirtydays.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Dedicated Timetable Screen:
 * - Top header with day title ("today" / "day XX") & three-dots menu matching two.png.
 * - Minimal "add task" button navigating to the new task page (one.png).
 * - Clean List Tiles matching two.png (Title, Time Range, Italic Notes).
 * - Interactive Swipe Actions matching three.png ("done" green reveal) & four.png ("undone" gray reveal).
 * - Clear All Tasks with iOS-style slide to confirm ModalBottomSheet.
 * - Long-press opens custom action bottom sheet with Edit, Delete & Toggle options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTimetableScreen(
    dayItem: DayItem,
    onBack: () -> Unit,
    onNavigateToAddTask: (TodoItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val scope = rememberCoroutineScope()

    var todos by remember(dayItem.dayNumber) {
        mutableStateOf(AppDataManager.loadTodosForDay(context, dayItem.dayNumber))
    }

    var selectedTodoForAction by remember { mutableStateOf<TodoItem?>(null) }
    var isClearAllBottomSheetOpen by remember { mutableStateOf(false) }
    val actionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clearSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }

    val updateTodoStatus: (String, Boolean) -> Unit = { todoId, isCompleted ->
        val updated = todos.map {
            if (it.id == todoId) it.copy(isCompleted = isCompleted) else it
        }
        todos = updated
        AppDataManager.saveTodosForDay(context, dayItem.dayNumber, updated)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Toast.makeText(context, if (isCompleted) "Marked done" else "Marked undone", Toast.LENGTH_SHORT).show()
    }

    val deleteBlock: (String) -> Unit = { todoId ->
        val updated = todos.filter { it.id != todoId }
        todos = updated
        AppDataManager.saveTodosForDay(context, dayItem.dayNumber, updated)
        Toast.makeText(context, "Task removed", Toast.LENGTH_SHORT).show()
    }

    val clearAllTasks: () -> Unit = {
        todos = emptyList()
        AppDataManager.saveTodosForDay(context, dayItem.dayNumber, emptyList())
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "All tasks cleared", Toast.LENGTH_SHORT).show()
    }

    // Button press animation state
    val buttonInteraction = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteraction.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "btn_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = safeInsets.calculateTopPadding() + 4.dp,
                bottom = safeInsets.calculateBottomPadding() + 8.dp
            )
    ) {
        // Top Bar: "today" or Day Title on Left | Three dots menu on Right (matching two.png)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (dayItem.isToday) "today" else "day ${dayItem.formattedDayNumber}",
                fontFamily = PlaypenFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = extendedColors.textPrimary
            )

            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = extendedColors.textPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Clear All Tasks",
                                fontFamily = PlaypenFontFamily,
                                color = Color(0xFFD32F2F)
                            )
                        },
                        onClick = {
                            showMenu = false
                            if (todos.isEmpty()) {
                                Toast.makeText(context, "No tasks to clear", Toast.LENGTH_SHORT).show()
                            } else {
                                isClearAllBottomSheetOpen = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Minimal "add task" Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .scale(buttonScale)
                .clickable(
                    interactionSource = buttonInteraction,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToAddTask(null)
                    }
                ),
            shape = RoundedCornerShape(12.dp),
            color = extendedColors.todayBadgeBg,
            border = BorderStroke(1.dp, extendedColors.borderFocused)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "add task",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = extendedColors.todayBadgeText,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Task List with Swipe Actions
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks yet\nTap 'add task' above to get started",
                    fontFamily = PlaypenFontFamily,
                    fontSize = 13.sp,
                    color = extendedColors.textTertiary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = todos,
                    key = { it.id }
                ) { todo ->
                    SwipeableTaskCardItem(
                        todo = todo,
                        onSwipeDone = { updateTodoStatus(todo.id, true) },
                        onSwipeUndone = { updateTodoStatus(todo.id, false) },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTodoForAction = todo
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTodoForAction = todo
                        }
                    )
                }
            }
        }
    }

    // Custom-Made Action BottomSheet (Edit, Delete, Mark Status)
    if (selectedTodoForAction != null) {
        val currentTodo = selectedTodoForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedTodoForAction = null },
            sheetState = actionSheetState,
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
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 28.dp)
                    .navigationBarsPadding()
            ) {
                // Header Task Details Preview
                Text(
                    text = currentTodo.title,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = extendedColors.textPrimary,
                    lineHeight = 22.sp
                )

                if (currentTodo.timeRange.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = currentTodo.timeRange,
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = extendedColors.textSecondary
                    )
                }

                if (currentTodo.subDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = currentTodo.subDetails,
                        fontFamily = PlaypenFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = extendedColors.textTertiary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: EDIT TASK
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val itemToEdit = currentTodo
                            scope.launch {
                                actionSheetState.hide()
                                selectedTodoForAction = null
                                onNavigateToAddTask(itemToEdit)
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, extendedColors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                tint = extendedColors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Edit Task",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = extendedColors.textPrimary
                            )
                            Text(
                                text = "Modify task name, schedule, or notes",
                                fontFamily = PlaypenFontFamily,
                                fontSize = 10.5.sp,
                                color = extendedColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 2: TOGGLE STATUS
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newStatus = !currentTodo.isCompleted
                            val targetId = currentTodo.id
                            scope.launch {
                                actionSheetState.hide()
                                selectedTodoForAction = null
                                updateTodoStatus(targetId, newStatus)
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, extendedColors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentTodo.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentTodo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = "Toggle Status",
                                tint = if (currentTodo.isCompleted) Color(0xFF2E7D32) else extendedColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentTodo.isCompleted) "Mark as Undone" else "Mark as Done",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = extendedColors.textPrimary
                            )
                            Text(
                                text = if (currentTodo.isCompleted) "Move back to active tasks" else "Complete this task",
                                fontFamily = PlaypenFontFamily,
                                fontSize = 10.5.sp,
                                color = extendedColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 3: DELETE TASK
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val idToDelete = currentTodo.id
                            scope.launch {
                                actionSheetState.hide()
                                selectedTodoForAction = null
                                deleteBlock(idToDelete)
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE57373).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Task",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delete Task",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "Remove this task permanently",
                                fontFamily = PlaypenFontFamily,
                                fontSize = 10.5.sp,
                                color = extendedColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Clear All Tasks Confirmation BottomSheet with iOS Slide-to-Confirm
    if (isClearAllBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isClearAllBottomSheetOpen = false },
            sheetState = clearSheetState,
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
                    text = "clear all tasks",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFD32F2F)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Are you sure you want to remove all tasks for this day? This action cannot be undone.",
                    fontFamily = PlaypenFontFamily,
                    fontSize = 12.5.sp,
                    color = extendedColors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // iOS-Style Slide to Clear Slider
                IOSSlideToConfirmButton(
                    text = "slide to clear all",
                    trackBgColor = Color(0xFFFFEBEE),
                    thumbArrowTint = Color(0xFFD32F2F),
                    textColor = Color(0xFFD32F2F),
                    onConfirmed = {
                        scope.launch {
                            clearSheetState.hide()
                            isClearAllBottomSheetOpen = false
                            clearAllTasks()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        scope.launch {
                            clearSheetState.hide()
                            isClearAllBottomSheetOpen = false
                        }
                    }
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = extendedColors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * iOS Style Slider to Confirm Action (e.g. Slide to Clear All)
 */
@Composable
fun IOSSlideToConfirmButton(
    text: String,
    trackBgColor: Color,
    thumbArrowTint: Color,
    textColor: Color,
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
            .background(trackBgColor)
            .border(1.dp, extendedColors.border.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        maxDragWidthPx = (totalWidthPx - thumbSizePx - with(density) { 8.dp.toPx() }).coerceAtLeast(0f)

        // Center Prompt Text
        Text(
            text = text,
            fontFamily = PlaypenFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = textColor,
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                tint = thumbArrowTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Swipeable Task Card matching two.png, three.png (done green), and four.png (undone gray).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableTaskCardItem(
    todo: TodoItem,
    onSwipeDone: () -> Unit,
    onSwipeUndone: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val maxSwipePx = with(density) { 90.dp.toPx() }
    val triggerThresholdPx = with(density) { 55.dp.toPx() }

    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
    ) {
        // Background Actions Revealed on Swipe
        val currentOffset = offsetX.value
        if (currentOffset > 0f) {
            // Left Background (revealed when swiped right -> "done" matching three.png)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(density) { currentOffset.coerceAtLeast(0f).toDp() })
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "done",
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        } else if (currentOffset < 0f) {
            // Right Background (revealed when swiped left -> "undone" matching four.png)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(density) { (-currentOffset).coerceAtLeast(0f).toDp() })
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "undone",
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF616161)
                    )
                }
            }
        }

        // Foreground Card (matching two.png)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(todo.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val dragDistance = offsetX.value
                            if (dragDistance >= triggerThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwipeDone()
                            } else if (dragDistance <= -triggerThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwipeUndone()
                            }
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-maxSwipePx * 1.3f, maxSwipePx * 1.3f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (todo.isCompleted) Color(0xFFC8E6C9) else extendedColors.border.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Title (e.g. "hii")
                Text(
                    text = todo.title,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = if (todo.isCompleted) extendedColors.textSecondary else extendedColors.textPrimary
                )

                // Time Range (e.g. "12:52 PM - 03:52 PM" or "14:30 - 16:00")
                if (todo.timeRange.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.timeRange,
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = extendedColors.textSecondary
                    )
                }

                // SubDetails / Notes (e.g. "Hello User" in italics)
                if (todo.subDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.subDetails,
                        fontFamily = PlaypenFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = extendedColors.textTertiary
                    )
                }
            }
        }
    }
}
