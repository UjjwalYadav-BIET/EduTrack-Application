package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.cms.core.data.local.dao.AssignmentWithSubmissions
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.data.local.Attendence.SubjectEntity
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {

    suspend fun insertAssignment(assignment: AssignmentEntity)

    suspend fun updateAssignment(assignment: AssignmentEntity)

    suspend fun deleteAssignment(assignment: AssignmentEntity)

    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    fun getAssignmentsBySubject(subject: Int): Flow<List<AssignmentEntity>>

    fun getAssignmentsByTeacher(teacherId: Int): Flow<List<AssignmentEntity>>

    suspend fun getAssignmentById(id: Int): AssignmentEntity?

    fun getAssignmentWithSubmissions(
        assignmentId: Int
    ): Flow<AssignmentWithSubmissions>

    fun getPendingAssignments(
        rollNo: String
    ): Flow<List<AssignmentEntity>>
    fun getAllSubjects(): Flow<List<SubjectEntity>>
}