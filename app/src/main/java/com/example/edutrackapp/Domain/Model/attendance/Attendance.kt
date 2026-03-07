package com.example.edutrackapp.Domain.Model.attendance

data class Attendance(
    val id: Int = 0,
    val studentId: Int,
    val subjectId: Int,
    val facultyId: Int,
    val date: Long,
    val lecturePeriod: Int,
    val isPresent: Boolean
)