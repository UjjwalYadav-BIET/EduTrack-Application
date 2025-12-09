package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,      // e.g. "12 Dec 2025"
    val postedBy: String,  // e.g. "Prof. Salman"
    val targetBatch: String // "ALL" or "CS-A"
)