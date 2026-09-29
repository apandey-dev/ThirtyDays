package com.minimal.thirtydays.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.ui.theme.DayBadgeShape
import com.minimal.thirtydays.ui.theme.LocalExtendedColors
import com.minimal.thirtydays.ui.theme.MaliFontFamily
import com.minimal.thirtydays.ui.theme.PlaypenFontFamily

/**
 * Clean Minimalist Bottom Sheet displaying purely the task details:
 * - Category
 * - Title in Mali Bold
 * - Schedule Time Range
 * - Notes & Details in Playpen font
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailBottomSheet(
    todo: TodoItem?,
    onDismiss: () -> Unit
) {
    if (todo == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extendedColors = LocalExtendedColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = extendedColors.textPrimary,
        tonalElevation = 0.dp,
        dragHandle = {
            // Sleek rectangular drag handle (non-circular)
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 12.dp)
                    .size(width = 36.dp, height = 3.5.dp)
                    .clip(DayBadgeShape)
                    .background(extendedColors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Category Badge
            Box(
                modifier = Modifier
                    .clip(DayBadgeShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "#" + todo.category.lowercase(),
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = extendedColors.textSecondary,
                    letterSpacing = 0.4.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title in Mali Bold
            Text(
                text = todo.title,
                fontFamily = MaliFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                lineHeight = 25.sp,
                color = extendedColors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Schedule Time Range
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DayBadgeShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "TIME:",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = extendedColors.textTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = todo.timeRange,
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = extendedColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes / Sub-Details Section
            Text(
                text = "NOTES & DETAILS",
                fontFamily = PlaypenFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = extendedColors.textTertiary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = DayBadgeShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extendedColors.border)
            ) {
                Text(
                    text = if (todo.subDetails.isNotBlank()) todo.subDetails else "No additional notes provided.",
                    fontFamily = PlaypenFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = extendedColors.textSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
