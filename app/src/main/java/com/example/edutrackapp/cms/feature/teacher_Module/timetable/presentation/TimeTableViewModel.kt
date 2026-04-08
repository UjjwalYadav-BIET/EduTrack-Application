package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor() : ViewModel() {

    // ── "Today" mapped to a day label ────────────────────────────────────────
    val todayLabel: String = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY    -> "Mon"
        Calendar.TUESDAY   -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY  -> "Thu"
        Calendar.FRIDAY    -> "Fri"
        Calendar.SATURDAY  -> "Sat"
        else               -> "Mon"
    }

    // ── Full schedule data ────────────────────────────────────────────────────
    private val fullSchedule = mapOf(
        "Mon" to listOf(
            TimeTableSlot("1",  "Data Structures", "09:00 AM", "10:00 AM", "1 hr",  "Room 301", "CS-A",    "Lecture", 0xFFE57373),
            TimeTableSlot("2",  "Operating Sys",   "11:00 AM", "12:00 PM", "1 hr",  "Lab 1",    "CS-B",    "Lab",     0xFF64B5F6),
            TimeTableSlot("3",  "Seminar",         "02:00 PM", "03:00 PM", "1 hr",  "Hall A",   "CS-All",  "Seminar", 0xFFFFB74D)
        ),
        "Tue" to listOf(
            TimeTableSlot("4",  "Algorithms",      "10:00 AM", "11:30 AM", "1.5 hr","Room 302", "CS-A",    "Lecture", 0xFF81C784),
            TimeTableSlot("5",  "Data Structures", "02:00 PM", "03:00 PM", "1 hr",  "Room 301", "CS-A",    "Lecture", 0xFFE57373)
        ),
        "Wed" to listOf(
            TimeTableSlot("6",  "Database Mgmt",   "09:00 AM", "10:00 AM", "1 hr",  "Lab 2",    "CS-A",    "Lab",     0xFFFFD54F),
            TimeTableSlot("7",  "Operating Sys",   "12:00 PM", "01:00 PM", "1 hr",  "Room 101", "CS-B",    "Lecture", 0xFF64B5F6),
            TimeTableSlot("8",  "Algorithms",      "03:00 PM", "04:30 PM", "1.5 hr","Room 302", "CS-B",    "Lecture", 0xFF81C784)
        ),
        "Thu" to listOf(
            TimeTableSlot("9",  "Project Lab",     "09:00 AM", "12:00 PM", "3 hrs", "Main Lab", "CS-Final","Lab",     0xFFBA68C8)
        ),
        "Fri" to listOf(
            TimeTableSlot("10", "Data Structures", "09:00 AM", "10:00 AM", "1 hr",  "Room 301", "CS-A",    "Lecture", 0xFFE57373),
            TimeTableSlot("11", "Mentoring",       "04:00 PM", "05:00 PM", "1 hr",  "Staff Room","All",    "Seminar", 0xFF90A4AE)
        ),
        "Sat" to emptyList()   // ← Triggers empty-state UI
    )

    // ── Reactive state ────────────────────────────────────────────────────────
    var selectedDay    = mutableStateOf(todayLabel)
    var currentClasses = mutableStateOf(fullSchedule[todayLabel] ?: emptyList())

    fun selectDay(day: String) {
        selectedDay.value    = day
        currentClasses.value = fullSchedule[day] ?: emptyList()
    }

    // ── Total teaching hours for a given day ─────────────────────────────────
    fun totalHoursForDay(day: String): String {
        val slots = fullSchedule[day] ?: return "0"
        // Parse duration strings like "1 hr", "1.5 hr", "3 hrs"
        val total = slots.sumOf { slot ->
            slot.duration.replace("hrs", "").replace("hr", "").trim().toDoubleOrNull() ?: 0.0
        }
        return if (total % 1.0 == 0.0) total.toInt().toString() else total.toString()
    }

    // ── Detect if a slot is currently live (device time falls within window) ──
    fun isSlotLive(slot: TimeTableSlot): Boolean {
        return try {
            val fmt  = DateTimeFormatter.ofPattern("hh:mm a")
            val now  = LocalTime.now()
            val from = LocalTime.parse(slot.startTime, fmt)
            val to   = LocalTime.parse(slot.endTime, fmt)
            now.isAfter(from) && now.isBefore(to) && selectedDay.value == todayLabel
        } catch (e: Exception) {
            false
        }
    }
}