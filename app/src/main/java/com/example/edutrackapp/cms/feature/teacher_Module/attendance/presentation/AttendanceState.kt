package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

data class StudentUiModel(
    val id: String,
    val name: String,
    val rollNo: String,
    var isPresent: Boolean = true // Default is Present
)