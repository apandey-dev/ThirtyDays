package com.minimal.thirtydays.model

/**
 * Data model for Timetable / Todo items with title, time range (e.g., "9:30 - 11:30"),
 * sub-details, category, and completion state.
 */
data class TodoItem(
    val id: String,
    val title: String,
    val timeRange: String = "09:00 - 10:00",
    val subDetails: String = "",
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false
) {
    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    companion object {
        // Zero dummy data by default - starts completely clean
        fun getSampleTodosForDay(dayNumber: Int): List<TodoItem> = emptyList()
    }
}
