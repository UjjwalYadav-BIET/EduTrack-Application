package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.edutrackapp.cms.feature.student_module.assignments.presentation.SubmissionStatus


@Entity(
    tableName = "assignment_submissions",

    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["assignmentId"]),
        Index(value = ["studentRollNo"])
    ]
)

data class AssignmentSubmissionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val assignmentId: Int,      // Reference to AssignmentEntity.id

    val studentRollNo: String,  // Student identifier

    val submissionDate: String, // Date of submission

    val submissionTime: Long,   // Timestamp (for late submission check)

    val fileUri: String,        // Student uploaded file

    val marks: Int? = null,     // Teacher grading

    val feedback: String? = null, // Teacher comments

    val status: String = SubmissionStatus.SUBMITTED
)