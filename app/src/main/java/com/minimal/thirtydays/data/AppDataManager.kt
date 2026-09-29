package com.minimal.thirtydays.data

import android.content.Context
import com.minimal.thirtydays.model.BatchPhase
import com.minimal.thirtydays.model.TodoItem
import com.minimal.thirtydays.model.WeeklyGoalItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/**
 * Lightweight, zero-leak DataManager handling JSON import, export, and persistent storage.
 */
object AppDataManager {
    private const val CONFIG_FILE_NAME = "thirty_days_config.json"
    private const val PREFS_NAME = "thirty_days_prefs"
    private const val KEY_START_DATE = "challenge_start_date"
    private val DEFAULT_START_DATE: LocalDate = LocalDate.of(2026, 10, 1)

    fun getStartDate(context: Context): LocalDate {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_START_DATE, null)
        if (!saved.isNullOrBlank()) {
            try {
                val parsed = LocalDate.parse(saved)
                if (parsed == LocalDate.of(2026, 9, 25)) {
                    prefs.edit().putString(KEY_START_DATE, DEFAULT_START_DATE.toString()).apply()
                    return DEFAULT_START_DATE
                }
                return parsed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val initialDate = DEFAULT_START_DATE
        prefs.edit().putString(KEY_START_DATE, initialDate.toString()).apply()
        return initialDate
    }

    fun setStartDate(context: Context, date: LocalDate) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_START_DATE, date.toString()).apply()
    }

    private const val KEY_DARK_MODE = "is_dark_mode"

    fun isDarkMode(context: Context, isSystemDark: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_DARK_MODE)) {
            return isSystemDark
        }
        return prefs.getBoolean(KEY_DARK_MODE, isSystemDark)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    fun getSampleJsonStructure(): String {
        val root = JSONObject()

        // 1. Batches / Collective Weekly Goals Array
        val batchesArray = JSONArray()
        val defaultBatches = BatchPhase.getDefaultBatches()
        for (batch in defaultBatches) {
            val bObj = JSONObject()
            bObj.put("phaseNumber", batch.phaseNumber)
            bObj.put("title", batch.title)
            val goalsArray = JSONArray()
            batch.goals.forEach { goal ->
                val gObj = JSONObject()
                gObj.put("id", goal.id)
                gObj.put("title", goal.title)
                gObj.put("category", goal.category)
                gObj.put("target", goal.target)
                gObj.put("notes", goal.notes)
                gObj.put("isCompleted", goal.isCompleted)
                goalsArray.put(gObj)
            }
            bObj.put("goals", goalsArray)
            bObj.put("startDay", batch.startDay)
            bObj.put("endDay", batch.endDay)
            batchesArray.put(bObj)
        }
        root.put("batches", batchesArray)

        // 2. Days Array with Daily Timetable Blocks
        val daysArray = JSONArray()
        for (dayIndex in 1..30) {
            val dayObj = JSONObject()
            dayObj.put("dayNumber", dayIndex)

            val todosArray = JSONArray()
            val sampleTodos = TodoItem.getSampleTodosForDay(dayIndex)
            for (todo in sampleTodos) {
                val tObj = JSONObject()
                tObj.put("id", todo.id)
                tObj.put("title", todo.title)
                tObj.put("timeRange", todo.timeRange)
                tObj.put("subDetails", todo.subDetails)
                tObj.put("category", todo.category)
                tObj.put("isCompleted", todo.isCompleted)
                todosArray.put(tObj)
            }
            dayObj.put("todos", todosArray)
            daysArray.put(dayObj)
        }
        root.put("days", daysArray)

        return root.toString(2)
    }

    fun loadBatches(context: Context): List<BatchPhase> {
        val jsonStr = getConfigFile(context).takeIf { it.exists() }?.readText()
        if (jsonStr.isNullOrBlank()) {
            return BatchPhase.getDefaultBatches()
        }

        return try {
            val root = JSONObject(jsonStr)
            val array = root.getJSONArray("batches")
            val list = mutableListOf<BatchPhase>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val goals = mutableListOf<WeeklyGoalItem>()
                if (obj.has("goals")) {
                    val gArr = obj.getJSONArray("goals")
                    for (k in 0 until gArr.length()) {
                        val item = gArr.opt(k)
                        if (item is JSONObject) {
                            goals.add(
                                WeeklyGoalItem(
                                    id = item.optString("id", "${obj.getInt("phaseNumber")}-$k"),
                                    title = item.optString("title", ""),
                                    category = item.optString("category", "General"),
                                    target = item.optString("target", ""),
                                    notes = item.optString("notes", ""),
                                    isCompleted = item.optBoolean("isCompleted", false)
                                )
                            )
                        } else if (item is String && item.isNotBlank()) {
                            goals.add(
                                WeeklyGoalItem(
                                    id = "${obj.getInt("phaseNumber")}-$k",
                                    title = item,
                                    category = "General"
                                )
                            )
                        }
                    }
                } else if (obj.has("weeklyGoals")) {
                    val gArr = obj.getJSONArray("weeklyGoals")
                    for (k in 0 until gArr.length()) {
                        val str = gArr.optString(k, "")
                        if (str.isNotBlank()) {
                            goals.add(
                                WeeklyGoalItem(
                                    id = "${obj.getInt("phaseNumber")}-$k",
                                    title = str,
                                    category = "General"
                                )
                            )
                        }
                    }
                }
                list.add(
                    BatchPhase(
                        phaseNumber = obj.getInt("phaseNumber"),
                        title = obj.optString("title", ""),
                        goals = goals,
                        startDay = obj.getInt("startDay"),
                        endDay = obj.getInt("endDay")
                    )
                )
            }
            if (list.isEmpty()) BatchPhase.getDefaultBatches() else list
        } catch (e: Exception) {
            BatchPhase.getDefaultBatches()
        }
    }

    fun saveBatches(context: Context, updatedBatches: List<BatchPhase>) {
        try {
            val file = getConfigFile(context)
            val jsonStr = if (file.exists()) file.readText() else getSampleJsonStructure()
            val root = JSONObject(jsonStr)

            val batchesArray = JSONArray()
            for (batch in updatedBatches) {
                val bObj = JSONObject()
                bObj.put("phaseNumber", batch.phaseNumber)
                bObj.put("title", batch.title)
                val goalsArray = JSONArray()
                batch.goals.forEach { goal ->
                    val gObj = JSONObject()
                    gObj.put("id", goal.id)
                    gObj.put("title", goal.title)
                    gObj.put("category", goal.category)
                    gObj.put("target", goal.target)
                    gObj.put("notes", goal.notes)
                    gObj.put("isCompleted", goal.isCompleted)
                    goalsArray.put(gObj)
                }
                bObj.put("goals", goalsArray)
                bObj.put("startDay", batch.startDay)
                bObj.put("endDay", batch.endDay)
                batchesArray.put(bObj)
            }
            root.put("batches", batchesArray)
            file.writeText(root.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadTodosForDay(context: Context, dayNumber: Int): List<TodoItem> {
        val jsonStr = getConfigFile(context).takeIf { it.exists() }?.readText()
        if (jsonStr.isNullOrBlank()) {
            return TodoItem.getSampleTodosForDay(dayNumber)
        }

        return try {
            val root = JSONObject(jsonStr)
            val daysArray = root.getJSONArray("days")
            for (i in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(i)
                if (dayObj.getInt("dayNumber") == dayNumber) {
                    val todosArray = dayObj.getJSONArray("todos")
                    val list = mutableListOf<TodoItem>()
                    for (j in 0 until todosArray.length()) {
                        val tObj = todosArray.getJSONObject(j)
                        list.add(
                            TodoItem(
                                id = tObj.optString("id", "$dayNumber-$j"),
                                title = tObj.getString("title"),
                                timeRange = tObj.optString("timeRange", "09:00 - 10:00"),
                                subDetails = tObj.optString("subDetails", ""),
                                category = tObj.optString("category", "General"),
                                isCompleted = tObj.optBoolean("isCompleted", false)
                            )
                        )
                    }
                    return list
                }
            }
            TodoItem.getSampleTodosForDay(dayNumber)
        } catch (e: Exception) {
            TodoItem.getSampleTodosForDay(dayNumber)
        }
    }

    fun saveTodosForDay(context: Context, dayNumber: Int, updatedTodos: List<TodoItem>) {
        try {
            val file = getConfigFile(context)
            val jsonStr = if (file.exists()) file.readText() else getSampleJsonStructure()
            val root = JSONObject(jsonStr)
            val daysArray = root.optJSONArray("days") ?: JSONArray()
            var found = false

            for (i in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(i)
                if (dayObj.getInt("dayNumber") == dayNumber) {
                    val newTodosArray = JSONArray()
                    for (todo in updatedTodos) {
                        val tObj = JSONObject()
                        tObj.put("id", todo.id)
                        tObj.put("title", todo.title)
                        tObj.put("timeRange", todo.timeRange)
                        tObj.put("subDetails", todo.subDetails)
                        tObj.put("category", todo.category)
                        tObj.put("isCompleted", todo.isCompleted)
                        newTodosArray.put(tObj)
                    }
                    dayObj.put("todos", newTodosArray)
                    found = true
                    break
                }
            }

            if (!found) {
                val dayObj = JSONObject()
                dayObj.put("dayNumber", dayNumber)
                val newTodosArray = JSONArray()
                for (todo in updatedTodos) {
                    val tObj = JSONObject()
                    tObj.put("id", todo.id)
                    tObj.put("title", todo.title)
                    tObj.put("timeRange", todo.timeRange)
                    tObj.put("subDetails", todo.subDetails)
                    tObj.put("category", todo.category)
                    tObj.put("isCompleted", todo.isCompleted)
                    newTodosArray.put(tObj)
                }
                dayObj.put("todos", newTodosArray)
                daysArray.put(dayObj)
                root.put("days", daysArray)
            }

            file.writeText(root.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importJson(context: Context, jsonString: String): Result<Unit> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("batches") && !root.has("days")) {
                return Result.failure(IllegalArgumentException("JSON must contain 'batches' or 'days'."))
            }
            getConfigFile(context).writeText(root.toString(2))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resetToDefault(context: Context) {
        val file = getConfigFile(context)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getCurrentJson(context: Context): String {
        val file = getConfigFile(context)
        return if (file.exists()) file.readText() else getSampleJsonStructure()
    }

    private fun getConfigFile(context: Context): File {
        return File(context.filesDir, CONFIG_FILE_NAME)
    }
}
