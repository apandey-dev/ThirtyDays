package com.minimal.thirtydays.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Data model for a single Weekly Target / Goal.
 */
data class WeeklyGoalItem(
    val id: String,
    val title: String,
    val category: String = "General",
    val target: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false
)

/**
 * Data model for Weekly Batches.
 */
data class BatchPhase(
    val phaseNumber: Int,
    val title: String = "",
    val goals: List<WeeklyGoalItem> = emptyList(),
    val startDay: Int,
    val endDay: Int
) {
    val phaseHeader: String
        get() = "WEEK $phaseNumber"

    val dayRangeLabel: String
        get() = "DAYS $startDay–$endDay"

    val goalsSummary: String
        get() = if (goals.isNotEmpty()) goals.joinToString(" • ") { it.title } else title

    companion object {
        fun getDefaultBatches(): List<BatchPhase> = listOf(
            BatchPhase(phaseNumber = 1, startDay = 1, endDay = 7),
            BatchPhase(phaseNumber = 2, startDay = 8, endDay = 14),
            BatchPhase(phaseNumber = 3, startDay = 15, endDay = 21),
            BatchPhase(phaseNumber = 4, startDay = 22, endDay = 30)
        )
    }
}

/**
 * Immutable data model representing a single day card in the 30-day grid.
 */
data class DayItem(
    val dayNumber: Int,
    val date: LocalDate,
    val phaseNumber: Int = ((dayNumber - 1) / 7) + 1,
    val isToday: Boolean = false,
    val isCompleted: Boolean = false
) {
    val formattedDayNumber: String
        get() = String.format(Locale.getDefault(), "%02d", dayNumber)

    val dayHeader: String
        get() = "DAY $formattedDayNumber"

    val dayOfWeekShort: String
        get() = date.dayOfWeek.name.take(3).uppercase(Locale.getDefault())

    val formattedDate: String
        get() = date.format(DATE_FORMATTER).uppercase(Locale.getDefault())

    val fullDateLabel: String
        get() = date.format(FULL_DATE_FORMATTER)

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        private val FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

        fun generate30Days(
            startDate: LocalDate = LocalDate.of(2026, 10, 1),
            today: LocalDate = LocalDate.now()
        ): List<DayItem> {
            return (1..30).map { dayIndex ->
                val date = startDate.plusDays((dayIndex - 1).toLong())
                val phase = when {
                    dayIndex in 1..7 -> 1
                    dayIndex in 8..14 -> 2
                    dayIndex in 15..21 -> 3
                    else -> 4
                }
                DayItem(
                    dayNumber = dayIndex,
                    date = date,
                    phaseNumber = phase,
                    isToday = date.isEqual(today),
                    isCompleted = false
                )
            }
        }
    }
}
