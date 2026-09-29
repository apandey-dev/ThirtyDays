package com.minimal.thirtydays.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.data.AppDataManager
import com.minimal.thirtydays.model.DayItem
import com.minimal.thirtydays.ui.components.DayCard
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily
import java.time.LocalDate

/**
 * HomeScreen containing the 30-day cards organized into structured weekly Batches.
 * - Week headers have no enclosing box, with a subtle brush paint highlight behind the "WEEK X" text.
 * - Week 1 header contains the sleek Theme Switcher on the top right.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onDayClick: (DayItem) -> Unit = {},
    onDayLongClick: (DayItem) -> Unit = {}
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()

    val startDate = remember { AppDataManager.getStartDate(context) }
    val today = remember { LocalDate.now() }
    val daysList = remember(startDate, today) { DayItem.generate30Days(startDate, today) }
    val batches = remember { AppDataManager.loadBatches(context) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 145.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = safeInsets.calculateTopPadding() + 6.dp,
                bottom = safeInsets.calculateBottomPadding() + 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            batches.forEach { batch ->
                // Clean unboxed header with brush stroke paint behind "WEEK X"
                item(
                    key = "batch_header_${batch.phaseNumber}",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    val brushColor = when (batch.phaseNumber) {
                        1 -> if (isDarkMode) Color(0xFFFFD54F).copy(alpha = 0.28f) else Color(0xFFFFE082).copy(alpha = 0.55f)
                        2 -> if (isDarkMode) Color(0xFF81C784).copy(alpha = 0.25f) else Color(0xFFA5D6A7).copy(alpha = 0.50f)
                        3 -> if (isDarkMode) Color(0xFF4DD0E1).copy(alpha = 0.25f) else Color(0xFF80DEEA).copy(alpha = 0.50f)
                        else -> if (isDarkMode) Color(0xFFBA68C8).copy(alpha = 0.25f) else Color(0xFFCE93D8).copy(alpha = 0.50f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 2.dp,
                                end = 2.dp,
                                top = if (batch.phaseNumber == 1) 4.dp else 16.dp,
                                bottom = 4.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            // Week Title with Painterly Brush Highlight Behind It
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 0.dp)
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    val top = h * 0.42f
                                    val bottom = h * 0.94f

                                    val path = Path().apply {
                                        moveTo(0f, top + 1f)
                                        cubicTo(w * 0.35f, top - 2.5f, w * 0.7f, top + 2f, w + 2f, top)
                                        lineTo(w + 3f, bottom - 1f)
                                        cubicTo(w * 0.65f, bottom + 2.5f, w * 0.3f, bottom - 1.5f, -2f, bottom)
                                        close()
                                    }
                                    drawPath(path = path, color = brushColor)
                                }

                                Text(
                                    text = batch.phaseHeader,
                                    fontFamily = MaliFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = extendedColors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            if (batch.goalsSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = batch.goalsSummary,
                                    fontFamily = PlaypenFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = extendedColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // Only in Week 1: Sleek Theme Switcher on the right
                        if (batch.phaseNumber == 1) {
                            Surface(
                                modifier = Modifier
                                    .clip(DayBadgeShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleTheme()
                                    },
                                shape = DayBadgeShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extendedColors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode",
                                        tint = extendedColors.textPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isDarkMode) "LIGHT" else "DARK",
                                        fontFamily = PlaypenFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = extendedColors.textPrimary,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // The day cards belonging to this batch phase
                val batchDays = daysList.filter { it.dayNumber in batch.startDay..batch.endDay }
                items(
                    items = batchDays,
                    key = { it.dayNumber }
                ) { dayItem ->
                    DayCard(
                        item = dayItem,
                        onClick = onDayClick,
                        onLongClick = onDayLongClick
                    )
                }
            }
        }
    }
}
