package com.example.edutrackapp.data.local.timeTable

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timetable",
    indices = [
        Index("day"),
        Index("facultyId"),
        Index("subjectId"),
        Index(
            value = ["day", "startTime","endTime" ,"branch", "semester", "section"],
            unique = true
        )
    ]
)
data class TimeTableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val day: String,
    val startTime: String,
    val endTime:String,
    val subjectId: Int,
    val facultyId: Int,
    val branch: String,
    val semester: Int,
    val section: String,
)
