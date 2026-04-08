package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

data class TimeTableSlot(
    val id: String,
    val subject: String,      // e.g. "Data Structures"
    val startTime: String,    // e.g. "09:00 AM"   ← split from combined "time" field
    val endTime: String,      // e.g. "10:00 AM"
    val duration: String,     // e.g. "1 hr"       ← NEW: shown as a pill below start/end
    val room: String,         // e.g. "Lab 2"
    val batch: String,        // e.g. "CS-A"
    val classType: String,    // e.g. "Lecture" / "Lab" / "Seminar" ← NEW
    val colorHex: Long        // Distinct color per subject
)