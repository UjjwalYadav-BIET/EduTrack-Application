package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.Domain.Model.StudentResultUi
import com.example.edutrackapp.Domain.Model.StudentWithMarks
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity
import com.example.edutrackapp.data.local.Attendence.StudentEntity
import kotlinx.coroutines.flow.Flow

interface ResultRepository {

    // Test
    suspend fun createTest(test: TestEntity): Long
    suspend fun getAllTests(): List<TestEntity>

    // Students
    suspend fun getStudentsByClass(
        branch: String,
        semester: Int,
        section: String
    ): List<StudentEntity>

    // Students + Marks (JOIN)
    suspend fun getStudentsWithMarks(
        testId: Int
    ): List<StudentWithMarks>

    // Save / Update
    suspend fun saveOrUpdateResult(
        testId: Int,
        studentId: Int,
        marks: String
    )
    suspend fun getTestsByTeacher(teacherId: Int): List<TestEntity>

    suspend fun getTestById(testId: Int): TestEntity?



    fun getStudentResults(rollNo: Int): Flow<List<StudentResultUi>>
}