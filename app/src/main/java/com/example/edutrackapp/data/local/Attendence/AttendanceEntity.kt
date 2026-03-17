package com.example.edutrackapp.data.local.Attendence

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FacultyEntity::class,
            parentColumns = ["facultyId"],
            childColumns = ["facultyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId"),
        Index("subjectId"),
        Index("facultyId"),
        Index(
            value = ["studentId", "subjectId", "facultyId", "date", "lecturePeriod"],
            unique = true
        )
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val attendanceId: Int = 0,
    val studentId: Int,
    val subjectId: Int,
    val facultyId: Int,
    val date: Long,
    val lecturePeriod: Int,
    val isPresent: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
