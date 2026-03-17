package com.example.edutrackapp.data.local.Attendence

import androidx.room.Entity

@Entity(
    tableName = "faculty_subjects",
    primaryKeys = ["facultyId", "subjectId"]
)
data class FacultySubjectEntity(
    val facultyId: Int,
    val subjectId: Int
)

