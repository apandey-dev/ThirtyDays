package com.minimal.thirtydays.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Minimalist Day Detail Screen (Opened on single click of any Day Card):
 * - Header: Date & Day name.
 * - List Tiles matching two.png style (Title, Time range, Italic notes).
 * - Interactive Swipe Actions matching three.png ("done" green reveal) and four.png ("undone" gray reveal).
 * - Persistent storage with AppDataManager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dayItem: DayItem,
    onBack: () -> Unit,
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

    var activeDetailTodo by remember { mutableStateOf<TodoItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fullDayName = remember(dayItem.date) {
        dayItem.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(Locale.getDefault())
    }

    val updateTodoStatus: (String, Boolean) -> Unit = { id, isCompleted ->
        val updated = todos.map { item ->
            if (item.id == id) item.copy(isCompleted = isCompleted) else item
        }
        todos = updated
        AppDataManager.saveTodosForDay(context, dayItem.dayNumber, updated)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Toast.makeText(context, if (isCompleted) "Marked done" else "Marked undone", Toast.LENGTH_SHORT).show()
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
                    top = safeInsets.calculateTopPadding() + 4.dp,
                    bottom = safeInsets.calculateBottomPadding() + 8.dp
                )
        ) {
            // Header: Date & Day Title (Clean & Minimal)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = dayItem.formattedDate,
                    fontFamily = MaliFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = extendedColors.textPrimary,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${dayItem.dayHeader} • $fullDayName",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary,
                    letterSpacing = 0.4.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks for today\nLong-press the day card on home to add tasks",
                        fontFamily = PlaypenFontFamily,
                        fontSize = 13.sp,
                        color = extendedColors.textTertiary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // Task List with Swipe-to-Done and Swipe-to-Undone matching two.png, three.png & four.png
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
                        DetailSwipeableCard(
                            todo = todo,
                            onSwipeDone = { updateTodoStatus(todo.id, true) },
                            onSwipeUndone = { updateTodoStatus(todo.id, false) },
                            onClick = { activeDetailTodo = todo },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeDetailTodo = todo
                            }
                        )
                    }
                }
            }
        }

        // Custom Detail & Action Bottom Sheet
        if (activeDetailTodo != null) {
            val todo = activeDetailTodo!!
            ModalBottomSheet(
                onDismissRequest = { activeDetailTodo = null },
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
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = todo.title,
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = extendedColors.textPrimary
                    )

                    if (todo.timeRange.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TIME: ${todo.timeRange}",
                            fontFamily = PlaypenFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary
                        )
                    }

                    if (todo.subDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, extendedColors.border)
                        ) {
                            Text(
                                text = todo.subDetails,
                                fontFamily = PlaypenFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = extendedColors.textSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Toggle Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newStatus = !todo.isCompleted
                                val targetId = todo.id
                                scope.launch {
                                    sheetState.hide()
                                    activeDetailTodo = null
                                    updateTodoStatus(targetId, newStatus)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (todo.isCompleted) MaterialTheme.colorScheme.surfaceVariant else extendedColors.todayBadgeBg,
                        border = BorderStroke(1.dp, extendedColors.borderFocused)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (todo.isCompleted) Icons.Outlined.RadioButtonUnchecked else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (todo.isCompleted) extendedColors.textPrimary else extendedColors.todayBadgeText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (todo.isCompleted) "MARK AS UNDONE" else "MARK AS DONE",
                                fontFamily = PlaypenFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (todo.isCompleted) extendedColors.textPrimary else extendedColors.todayBadgeText,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipeable Card in DayDetailScreen matching two.png, three.png (done green), and four.png (undone gray).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailSwipeableCard(
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
