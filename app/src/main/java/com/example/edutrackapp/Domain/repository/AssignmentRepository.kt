package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.cms.core.data.local.dao.AssignmentWithSubmissions
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {

    suspend fun insertAssignment(assignment: AssignmentEntity)

    suspend fun updateAssignment(assignment: AssignmentEntity)

    suspend fun deleteAssignment(assignment: AssignmentEntity)

    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    fun getAssignmentsByBatch(batch: String): Flow<List<AssignmentEntity>>

    fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>>

    fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>>

    suspend fun getAssignmentById(id: Int): AssignmentEntity?

    fun getAssignmentWithSubmissions(
        assignmentId: Int
    ): Flow<AssignmentWithSubmissions>

    fun getPendingAssignments(
        rollNo: String
    ): Flow<List<AssignmentEntity>>
}