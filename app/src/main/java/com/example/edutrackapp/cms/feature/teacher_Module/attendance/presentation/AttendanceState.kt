package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

data class StudentUiModel(
    val id: String,
    val name: String,
    val rollNo: String,
    val isPresent: Boolean = false
)

data class AttendanceRecord(
    val id: String,
    val date: String,
    val time: String,
    val className: String = "CS-A",
    val presentCount: Int,
    val totalCount: Int,
    val studentSnapshots: List<StudentSnapshot>
)

data class StudentSnapshot(
    val id: String,
    val name: String,
    val rollNo: String,
    val isPresent: Boolean
)