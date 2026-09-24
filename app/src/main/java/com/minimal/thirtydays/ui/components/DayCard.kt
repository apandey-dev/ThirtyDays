package com.minimal.thirtydays.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.model.DayItem
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.DayCardShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily

/**
 * Minimalist, compact DayCard designed with precise typography,
 * non-circular sleek rounded geometry, and responsive layout.
 */
@Composable
fun DayCard(
    item: DayItem,
    onClick: (DayItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "card_press_scale"
    )

    val borderColor = if (item.isToday) {
        extendedColors.borderFocused
    } else {
        extendedColors.border
    }

    val borderWidth = if (item.isToday) 1.5.dp else 1.dp

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick(item)
                }
            ),
        shape = DayCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Day Identifier (Mali Bold) + Today Indicator (if applicable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.dayHeader,
                    fontFamily = MaliFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = extendedColors.textPrimary
                )

                if (item.isToday) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = extendedColors.todayBadgeBg,
                                shape = DayBadgeShape
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TODAY",
                            fontFamily = PlaypenFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = extendedColors.todayBadgeText,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Weekday (Playpen SemiBold) & Formatted Date (Playpen Regular)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.dayOfWeekShort,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary,
                    letterSpacing = 0.3.sp
                )

                Text(
                    text = item.formattedDate,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = extendedColors.textTertiary,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
