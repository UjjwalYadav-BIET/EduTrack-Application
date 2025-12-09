package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor() : ViewModel() {

    // Helper map to hold data for each day
    private val fullSchedule = mapOf(
        "Mon" to listOf(
            TimeTableSlot("1", "Data Structures", "09:00 - 10:00", "Room 301", "CS-A", 0xFFE57373),
            TimeTableSlot("2", "Operating Sys", "11:00 - 12:00", "Lab 1", "CS-B", 0xFF64B5F6)
        ),
        "Tue" to listOf(
            TimeTableSlot("3", "Algorithms", "10:00 - 11:30", "Room 302", "CS-A", 0xFF81C784),
            TimeTableSlot("4", "Data Structures", "02:00 - 03:00", "Room 301", "CS-A", 0xFFE57373)
        ),
        "Wed" to listOf(
            TimeTableSlot("5", "Database Mgmt", "09:00 - 10:00", "Lab 2", "CS-A", 0xFFFFD54F),
            TimeTableSlot("6", "Operating Sys", "12:00 - 01:00", "Room 101", "CS-B", 0xFF64B5F6)
        ),
        "Thu" to listOf(
            TimeTableSlot("7", "Project Lab", "09:00 - 12:00", "Main Lab", "CS-Final", 0xFFBA68C8)
        ),
        "Fri" to listOf(
            TimeTableSlot("8", "Mentoring", "04:00 - 05:00", "Staff Room", "All", 0xFF90A4AE)
        )
    )

    // State for the currently selected day
    var selectedDay = mutableStateOf("Mon")

    // State for the list of classes to show
    var currentClasses = mutableStateOf(fullSchedule["Mon"] ?: emptyList())

    fun selectDay(day: String) {
        selectedDay.value = day
        currentClasses.value = fullSchedule[day] ?: emptyList()
    }
}