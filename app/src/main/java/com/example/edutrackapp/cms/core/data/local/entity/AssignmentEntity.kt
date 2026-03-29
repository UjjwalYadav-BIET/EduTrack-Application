package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "assignments")
data class AssignmentEntity(
@PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val subjectId: Int,
    val teacherId: Int,
    val semester: Int,
    val section: String,
    val branch: String,
    val createdDate: Long,
    val dueDate: Long,
    val attachmentUri: String? = null
)
