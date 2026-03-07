package com.example.edutrackapp.Domain.Model.timeTable

data class TimeTableWithDetails(
    val timetableId: Int,
    val day: String,
    val startTime: String,
    val endTime:String,
    val subjectName: String,
    val branch: String,
    val semester: Int,
    val section: String
)
