package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val batch: String,      // e.g., "CS-A"
    val dueDate: String,    // e.g., "12/12/2025"
    val attachmentUri: String? = null, // URI string of the PDF teacher uploaded
    val subject: String

)