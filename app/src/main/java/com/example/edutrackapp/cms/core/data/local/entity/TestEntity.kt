package com.example.edutrackapp.cms.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val testId: Int = 0,
    val testName: String,
    val subject: Int,
    val teacherId:Int,
    val maxMarks: Int,
    val date: String,
    val year: Int,
    val branch: String,
    val section: String,
    val semester: Int
)