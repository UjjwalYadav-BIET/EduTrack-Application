package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.cms.core.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    @Query("SELECT * FROM submissions WHERE studentRollNo = :rollNo")
    fun getStudentSubmissions(rollNo: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE studentRollNo = :rollNo")
    suspend fun getStudentSubmissionsOnce(rollNo: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsForAssignment(assignmentId: String): Flow<List<SubmissionEntity>>
}