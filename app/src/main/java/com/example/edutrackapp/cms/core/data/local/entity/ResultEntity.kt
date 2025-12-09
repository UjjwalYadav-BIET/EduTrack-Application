package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val rollNo: String,
    val subject: String,      // e.g. "Computer Science"
    val examType: String,     // e.g. "Mid-Term"
    val marksObtained: String, // String to handle "AB" (Absent) or numbers
    val maxMarks: String      // e.g. "100"
)