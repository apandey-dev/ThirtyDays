package com.minimal.thirtydays.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.DayCardShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily

/**
 * Grid View Card:
 * - Constant compact height (118dp) ensuring Title is completely visible without truncation.
 * - Sub-details cleanly ellipsized.
 * - Light surfaceVariant category tags.
 * - Colored/highlighted border, no checkbox.
 * - Single click toggles completion (dulls card), Long press opens bottom sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoGridCard(
    todo: TodoItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "grid_todo_scale"
    )

    // Dynamic colored border based on state
    val borderColor by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            extendedColors.textTertiary.copy(alpha = 0.25f)
        } else {
            extendedColors.borderFocused
        },
        label = "grid_border_color"
    )

    val borderWidth = if (todo.isCompleted) 1.dp else 1.2.dp
    val cardAlpha = if (todo.isCompleted) 0.55f else 1f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp) // Constant height fitting full multi-line title
            .scale(scale)
            .alpha(cardAlpha)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = DayCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(width = borderWidth, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Light Category Tag + Time Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(DayBadgeShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = todo.category.uppercase(),
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = extendedColors.textSecondary,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = todo.timeRange,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.5.sp,
                    color = if (todo.isCompleted) extendedColors.textTertiary else extendedColors.textSecondary
                )
            }

            // Bottom Column: Title (Full text, no ellipse) + Sub-details (ellipsized)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = todo.title,
                    fontFamily = MaliFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 3,
                    lineHeight = 15.sp,
                    color = if (todo.isCompleted) extendedColors.textTertiary else extendedColors.textPrimary
                )

                if (todo.subDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todo.subDetails,
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp,
                        color = if (todo.isCompleted) extendedColors.textTertiary else extendedColors.textSecondary
                    )
                }
            }
        }
    }
}
