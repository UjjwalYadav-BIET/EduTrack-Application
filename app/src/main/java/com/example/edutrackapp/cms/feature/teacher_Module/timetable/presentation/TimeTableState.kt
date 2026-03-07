package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

data class TimeTableSlot(
    val id: String,
    val subject: String,
    val time: String,
    val room: String,
    val batch: String,
    val colorHex: Long
)