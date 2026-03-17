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

    // Teacher gives marks
    @Query("""
        UPDATE assignment_submissions
        SET marks = :marks
        WHERE id = :submissionId
    """)
    suspend fun updateMarks(
        submissionId: Int,
        marks: Int
    )

    // Teacher adds feedback
    @Query("""
        UPDATE assignment_submissions
        SET feedback = :feedback
        WHERE id = :submissionId
    """)
    suspend fun updateFeedback(
        submissionId: Int,
        feedback: String
    )
}