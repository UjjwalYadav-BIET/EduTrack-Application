package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentSubmissionDao {

    // Student submits assignment
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun submitAssignment(submission: AssignmentSubmissionEntity)

    // Update submission (teacher grading or student resubmit)
    @Update
    suspend fun updateSubmission(submission: AssignmentSubmissionEntity)

    // Delete submission
    @Delete
    suspend fun deleteSubmission(submission: AssignmentSubmissionEntity)

    // Get submissions for a specific assignment (teacher view)
    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsForAssignment(assignmentId: Int): Flow<List<AssignmentSubmissionEntity>>

    // Get submissions by a student
    @Query("SELECT * FROM assignment_submissions WHERE studentRollNo = :rollNo")
    fun getSubmissionsByStudent(rollNo: String): Flow<List<AssignmentSubmissionEntity>>

    // Check if student already submitted assignment
    @Query("""
        SELECT * FROM assignment_submissions
        WHERE assignmentId = :assignmentId
        AND studentRollNo = :rollNo
        LIMIT 1
    """)
    suspend fun getSubmissionByStudent(
        assignmentId: Int,
        rollNo: String
    ): AssignmentSubmissionEntity?

    @Query("SELECT * FROM assignment_submissions WHERE id = :id")
    suspend fun getSubmissionById(id: Int): AssignmentSubmissionEntity?

    // Teacher gives marks
    @Query("""
    UPDATE assignment_submissions
    SET marks = :marks,
        feedback = :feedback,
        status = 'REVIEWED'
    WHERE id = :submissionId 
    AND status != 'REVIEWED'
    """)
    suspend fun evaluateSubmission(
        submissionId: Int,
        marks: Int,
        feedback: String
    )
}