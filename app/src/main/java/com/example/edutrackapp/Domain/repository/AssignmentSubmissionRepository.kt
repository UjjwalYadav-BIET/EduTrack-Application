package com.example.edutrackapp.Domain.repository



import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity
import kotlinx.coroutines.flow.Flow

interface AssignmentSubmissionRepository {

    suspend fun getSubmissionById(
        id: Int
    ): AssignmentSubmissionEntity?

    suspend fun submitAssignment(submission: AssignmentSubmissionEntity)

    suspend fun updateSubmission(submission: AssignmentSubmissionEntity)

    suspend fun deleteSubmission(submission: AssignmentSubmissionEntity)

    fun getSubmissionsForAssignment(
        assignmentId: Int
    ): Flow<List<AssignmentSubmissionEntity>>

    fun getSubmissionsByStudent(
        rollNo: String
    ): Flow<List<AssignmentSubmissionEntity>>

    suspend fun getSubmissionByStudent(
        assignmentId: Int,
        rollNo: String
    ): AssignmentSubmissionEntity?

    suspend fun evaluateSubmission(
        submissionId: Int,
        marks: Int,
        feedback: String
    )

//    suspend fun updateFeedback(
//        submissionId: Int,
//        feedback: String
//    )
}