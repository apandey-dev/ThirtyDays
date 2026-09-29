package com.minimal.thirtydays.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.DayCardShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily

/**
 * List View Card:
 * - Single click toggles completion (represented by solid fill square, NO strikethrough, clean dulled text).
 * - Long press opens the custom detail bottom sheet.
 * - Displays Time Range (e.g. "9:30 - 11:30") and sub-details.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoCard(
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
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "todo_press_scale"
    )

    val fillBgColor by animateColorAsState(
        targetValue = if (todo.isCompleted) extendedColors.textPrimary else MaterialTheme.colorScheme.surface,
        label = "fill_indicator_color"
    )

    val cardAlpha = if (todo.isCompleted) 0.6f else 1f
    val indicatorShape = RoundedCornerShape(3.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
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
        border = BorderStroke(
            width = 1.dp,
            color = if (todo.isCompleted) extendedColors.textTertiary.copy(alpha = 0.35f) else extendedColors.border
        ),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            // Top Row: Indicator + Title + Time Range Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Minimal Solid-Fill Square Box (no tick icon)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(indicatorShape)
                            .background(fillBgColor)
                            .border(
                                width = 1.dp,
                                color = if (todo.isCompleted) extendedColors.textPrimary else extendedColors.borderFocused,
                                shape = indicatorShape
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = todo.title,
                        fontFamily = MaliFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (todo.isCompleted) extendedColors.textTertiary else extendedColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Time Range Badge (e.g. "9:30 - 11:30")
                Box(
                    modifier = Modifier
                        .clip(DayBadgeShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = todo.timeRange,
                        fontFamily = PlaypenFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = extendedColors.textSecondary,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            // Sub-details below in Playpen Sans font
            if (todo.subDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = todo.subDetails,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (todo.isCompleted) extendedColors.textTertiary else extendedColors.textSecondary,
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }
    }
}
