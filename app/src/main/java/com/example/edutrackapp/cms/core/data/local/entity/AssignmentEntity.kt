package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class AssignmentEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val subject: String,

    val batch: String,      // e.g. "CS-A"

    val teacherId: String,  // Teacher identifier

    val createdDate: String, // When assignment was created

    val dueDate: String,    // Assignment deadline

    val attachmentUri: String? = null // Optional PDF attachment
)