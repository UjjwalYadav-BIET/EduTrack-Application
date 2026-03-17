package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    // Teacher posts assignment
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    // Update assignment
    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    // Delete assignment
    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    // Get all assignments
    @Query("SELECT * FROM assignments ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    // Get assignments for a specific batch
    @Query("SELECT * FROM assignments WHERE batch = :batch ORDER BY dueDate ASC")
    fun getAssignmentsByBatch(batch: String): Flow<List<AssignmentEntity>>

    // Get assignments for a subject
    @Query("SELECT * FROM assignments WHERE subject = :subject ORDER BY dueDate ASC")
    fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>>

    // Get assignments created by a teacher
    @Query("SELECT * FROM assignments WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>>

    // Get single assignment by id
    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    suspend fun getAssignmentById(assignmentId: Int): AssignmentEntity?

    // Assignment with submissions (Teacher dashboard)
    @Transaction
    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    fun getAssignmentWithSubmissions(
        assignmentId: Int
    ): Flow<AssignmentWithSubmissions>

    // ⭐ Pending assignments for student
    @Query("""
        SELECT * FROM assignments
        WHERE id NOT IN (
            SELECT assignmentId FROM assignment_submissions
            WHERE studentRollNo = :rollNo
        )
        ORDER BY dueDate ASC
    """)
    fun getPendingAssignments(
        rollNo: String
    ): Flow<List<AssignmentEntity>>
}