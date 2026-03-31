package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long,
    val teacherId: Int,
    val targetYear:Int,
    val targetBranch:String,
    val targetSection: String,
    val attachmentUrl: String?,
    val isActive: Boolean = true
)