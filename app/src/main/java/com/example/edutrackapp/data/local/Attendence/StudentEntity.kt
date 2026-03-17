package com.example.edutrackapp.data.local.Attendence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val studentId: Int = 0,
    val name: String,
    val rollNo: Int,
    val branch: String,
    val semester: Int,
    val section: String
)