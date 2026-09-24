package com.minimal.thirtydays.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minimal.thirtydays.model.DayItem
import com.minimal.thirtydays.ui.components.DayCard

/**
 * HomeScreen containing solely the responsive 30-day grid cards.
 * Stripped of any headers, footers, app titles, or floating action buttons.
 * Fully responsive across all mobile form factors and orientations.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDayClick: (DayItem) -> Unit = {}
) {
    // Generate stable 30 days list without repeated allocations
    val daysList = remember { DayItem.generate30Days() }
    val gridState = rememberLazyGridState()
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 145.dp),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = safeInsets.calculateTopPadding() + 8.dp,
            bottom = safeInsets.calculateBottomPadding() + 8.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = daysList,
            key = { it.dayNumber }
        ) { dayItem ->
            DayCard(
                item = dayItem,
                onClick = onDayClick
            )
        }
    }
}
