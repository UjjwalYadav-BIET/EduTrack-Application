package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.data.local.Attendence.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    // ✅ All assignments
    @Query("SELECT * FROM assignments ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    // ✅ By subjectId (FIXED)
    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId ORDER BY dueDate ASC")
    fun getAssignmentsBySubject(subjectId: Int): Flow<List<AssignmentEntity>>

    // ✅ By teacherId (FIXED type)
    @Query("SELECT * FROM assignments WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getAssignmentsByTeacher(teacherId: Int): Flow<List<AssignmentEntity>>

    // ✅ By class (NEW - replaces batch)
    @Query("""
        SELECT * FROM assignments 
        WHERE semester = :semester 
        AND section = :section 
        AND branch = :branch
        ORDER BY dueDate ASC
    """)
    fun getAssignmentsByClass(
        semester: Int,
        section: String,
        branch: String
    ): Flow<List<AssignmentEntity>>

    // ✅ Single assignment
    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    suspend fun getAssignmentById(assignmentId: Int): AssignmentEntity?

    @Transaction
    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    fun getAssignmentWithSubmissions(
        assignmentId: Int
    ): Flow<AssignmentWithSubmissions>

    // ✅ Pending assignments
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

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>
}