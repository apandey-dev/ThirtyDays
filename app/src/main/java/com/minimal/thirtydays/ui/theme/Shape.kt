package com.minimal.thirtydays.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Sleek, minimal rounded rectangular shapes without complete circles or pills
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

// Specific card shape
val DayCardShape = RoundedCornerShape(12.dp)
val DayBadgeShape = RoundedCornerShape(6.dp)
