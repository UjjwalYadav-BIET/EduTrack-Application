package com.example.edutrackapp.Domain.Model

data class StudentWithMarks(
    val studentId: Int,
    val name: String,
    val rollNo: Int,
    val marks: String?
)