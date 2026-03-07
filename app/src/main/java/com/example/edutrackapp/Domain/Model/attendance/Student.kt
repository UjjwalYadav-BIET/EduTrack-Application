package com.example.edutrackapp.Domain.Model.attendance

data class Student(
    val id: Int,
    val name: String,
    val rollNo: Int,
    val branch: String,
    val semester: Int,
    val section: String
)