package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

data class TimeTableSlot(
    val id: String,
    val subject: String,     // e.g. "Data Structures"
    val time: String,        // e.g. "10:00 AM - 11:00 AM"
    val room: String,        // e.g. "Lab 2"
    val batch: String,       // e.g. "CS-A"
    val colorHex: Long       // To give each subject a distinct color
)