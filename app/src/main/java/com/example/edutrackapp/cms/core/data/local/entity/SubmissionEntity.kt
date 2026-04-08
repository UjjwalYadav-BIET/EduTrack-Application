package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreId: String = "",
    val assignmentId: String = "",   // Firestore doc ID (String now)
    val studentRollNo: String = "",
    val studentName: String = "",
    val studentUid: String = "",
    val fileUri: String = "",
    val submissionDate: String = "",
    val marks: String = "",
    val status: String = "submitted"
)