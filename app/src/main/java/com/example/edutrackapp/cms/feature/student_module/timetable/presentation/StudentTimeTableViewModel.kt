package com.example.edutrackapp.cms.feature.student_module.timetable.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

data class StudentClassSlot(
    val subject: String,
    val time: String,
    val room: String,
    val teacher: String,
    val type: String, // "Lecture" or "Lab"
    val startTime: Int // Hour of day (e.g., 9 for 9:00 AM) to check "Live" status
)

@HiltViewModel
class StudentTimeTableViewModel @Inject constructor() : ViewModel() {

    // Mock Data for "CS-A"
    private val fullSchedule = mapOf(
        "Mon" to listOf(
            StudentClassSlot("Data Structures", "09:00 - 10:00", "Room 301", "Prof. Salman", "Lecture", 9),
            StudentClassSlot("Physics Lab", "10:00 - 12:00", "Phy Lab", "Prof. HC Verma", "Lab", 10),
            StudentClassSlot("Lunch Break", "12:00 - 01:00", "Canteen", "-", "Break", 12),
            StudentClassSlot("Mathematics", "01:00 - 02:00", "Room 301", "Prof. Ramanujan", "Lecture", 13)
        ),
        "Tue" to listOf(
            StudentClassSlot("Algorithms", "09:00 - 10:30", "Room 302", "Prof. Knuth", "Lecture", 9),
            StudentClassSlot("Chemistry", "10:30 - 11:30", "Room 302", "Prof. Curie", "Lecture", 10),
            StudentClassSlot("Library", "11:30 - 12:30", "Central Lib", "-", "Self Study", 11)
        ),
        "Wed" to listOf(
            StudentClassSlot("Computer Networks", "09:00 - 10:00", "Room 301", "Prof. Tanenbaum", "Lecture", 9),
            StudentClassSlot("OS Lab", "10:00 - 01:00", "Comp Lab 2", "Prof. Torvalds", "Lab", 10)
        ),
        "Thu" to listOf(
            StudentClassSlot("Web Dev", "10:00 - 12:00", "Lab 1", "Prof. Zuckerberg", "Lab", 10),
            StudentClassSlot("Sports", "04:00 - 05:00", "Ground", "Coach", "Activity", 16)
        ),
        "Fri" to listOf(
            StudentClassSlot("Mentoring", "09:00 - 10:00", "Staff Room", "HOD", "Meeting", 9),
            StudentClassSlot("Project Work", "10:00 - 01:00", "Lab 3", "Guide", "Lab", 10)
        )
    )

    var selectedDay = mutableStateOf("Mon")
    var currentClasses = mutableStateOf(fullSchedule["Mon"] ?: emptyList())

    fun selectDay(day: String) {
        selectedDay.value = day
        currentClasses.value = fullSchedule[day] ?: emptyList()
    }

    // Helper to check if a class is "Live" (Current Hour matches Class Hour)
    fun isClassLive(slotHour: Int): Boolean {
        // Simple logic: If current hour matches slot start hour
        val currentHour = LocalTime.now().hour
        return currentHour == slotHour
    }
}