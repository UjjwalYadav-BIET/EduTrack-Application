package com.example.edutrackapp.data.repository

import com.example.edutrackapp.Domain.repository.AssignmentRepository
import com.example.edutrackapp.cms.core.data.local.dao.AssignmentDao
import com.example.edutrackapp.cms.core.data.local.dao.AssignmentWithSubmissions
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AssignmentRepositoryImpl @Inject constructor(
    private val assignmentDao: AssignmentDao
) : AssignmentRepository {

    override suspend fun insertAssignment(assignment: AssignmentEntity) {
        assignmentDao.insertAssignment(assignment)
    }

    override suspend fun updateAssignment(assignment: AssignmentEntity) {
        assignmentDao.updateAssignment(assignment)
    }

    override suspend fun deleteAssignment(assignment: AssignmentEntity) {
        assignmentDao.deleteAssignment(assignment)
    }

    override fun getAllAssignments(): Flow<List<AssignmentEntity>> {
        return assignmentDao.getAllAssignments()
    }

    override fun getAssignmentsByBatch(batch: String): Flow<List<AssignmentEntity>> {
        return assignmentDao.getAssignmentsByBatch(batch)
    }

    override fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>> {
        return assignmentDao.getAssignmentsBySubject(subject)
    }

    override fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>> {
        return assignmentDao.getAssignmentsByTeacher(teacherId)
    }

    override suspend fun getAssignmentById(id: Int): AssignmentEntity? {
        return assignmentDao.getAssignmentById(id)
    }

    override fun getAssignmentWithSubmissions(
        assignmentId: Int
    ): Flow<AssignmentWithSubmissions> {
        return assignmentDao.getAssignmentWithSubmissions(assignmentId)
    }

    override fun getPendingAssignments(
        rollNo: String
    ): Flow<List<AssignmentEntity>> {
        return assignmentDao.getPendingAssignments(rollNo)
    }
}