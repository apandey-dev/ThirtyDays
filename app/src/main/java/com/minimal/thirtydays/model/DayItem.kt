package com.minimal.thirtydays.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Immutable data model representing a single day card in the 30-day grid.
 * Zero-leakage, immutable and stable for Jetpack Compose optimizations.
 */
data class DayItem(
    val dayNumber: Int,
    val date: LocalDate,
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

        /**
         * Generates the 30-day items starting from the current date.
         */
        fun generate30Days(startDate: LocalDate = LocalDate.now()): List<DayItem> {
            val today = LocalDate.now()
            return (1..30).map { dayIndex ->
                val date = startDate.plusDays((dayIndex - 1).toLong())
                DayItem(
                    dayNumber = dayIndex,
                    date = date,
                    isToday = date.isEqual(today),
                    isCompleted = false
                )
            }
        }
    }
}
