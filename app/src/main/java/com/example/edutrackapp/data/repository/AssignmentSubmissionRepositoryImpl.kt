package com.example.edutrackapp.data.repository


import com.example.edutrackapp.Domain.repository.AssignmentSubmissionRepository
import com.example.edutrackapp.cms.core.data.local.dao.AssignmentSubmissionDao
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AssignmentSubmissionRepositoryImpl @Inject constructor(
    private val submissionDao: AssignmentSubmissionDao
) : AssignmentSubmissionRepository {
    override suspend fun getSubmissionById(
        id: Int
    ): AssignmentSubmissionEntity? {
        return submissionDao.getSubmissionById(id)
    }

    override suspend fun submitAssignment(submission: AssignmentSubmissionEntity) {
        submissionDao.submitAssignment(submission)
    }

    override suspend fun updateSubmission(submission: AssignmentSubmissionEntity) {
        submissionDao.updateSubmission(submission)
    }

    override suspend fun deleteSubmission(submission: AssignmentSubmissionEntity) {
        submissionDao.deleteSubmission(submission)
    }

    override fun getSubmissionsForAssignment(
        assignmentId: Int
    ): Flow<List<AssignmentSubmissionEntity>> {
        return submissionDao.getSubmissionsForAssignment(assignmentId)
    }

    override fun getSubmissionsByStudent(
        rollNo: String
    ): Flow<List<AssignmentSubmissionEntity>> {
        return submissionDao.getSubmissionsByStudent(rollNo)
    }

    override suspend fun getSubmissionByStudent(
        assignmentId: Int,
        rollNo: String
    ): AssignmentSubmissionEntity? {
        return submissionDao.getSubmissionByStudent(assignmentId, rollNo)
    }

    override suspend fun evaluateSubmission(
        submissionId: Int,
        marks: Int,
        feedback: String
    ) {
        submissionDao.evaluateSubmission(submissionId, marks, feedback)
    }


}