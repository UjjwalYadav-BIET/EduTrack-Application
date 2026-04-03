package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.edutrackapp.Domain.Model.StudentResultUi
import com.example.edutrackapp.Domain.Model.StudentWithMarks
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {

    // Insert single result
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    // Insert multiple
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultEntity>)

    // Get all results for a test
    @Query("SELECT * FROM results WHERE testId = :testId")
    suspend fun getResultsByTest(testId: Int): List<ResultEntity>

    // Get single result (for duplicate check)
    @Query("""
        SELECT * FROM results 
        WHERE testId = :testId AND studentId = :studentId
    """)
    suspend fun getResult(
        testId: Int,
        studentId: Int
    ): ResultEntity?

    // Update marks
    @Update
    suspend fun updateResult(result: ResultEntity)

    // Delete results of a test
    @Query("DELETE FROM results WHERE testId = :testId")
    suspend fun deleteResultsByTest(testId: Int)



    @Query("""
        SELECT 
            s.studentId,
            s.name,
            s.rollNo,
            r.marksObtained AS marks
        FROM students s
        LEFT JOIN results r
        ON s.studentId = r.studentId AND r.testId = :testId
        WHERE s.branch = :branch 
        AND s.semester = :semester 
        AND s.section = :section
    """)
    suspend fun getStudentsWithMarks(
        testId: Int,
        branch: String,
        semester: Int,
        section: String
    ): List<StudentWithMarks>

    @Query("""
    SELECT 
        t.subject AS subject,
        t.testName AS testName,
        r.marksObtained AS marksObtained
    FROM results r
    INNER JOIN tests t ON r.testId = t.testId
    INNER JOIN students s ON r.studentId = s.studentId
    WHERE s.rollNo = :rollNo
""")
    fun getResultsForStudent(rollNo: Int): Flow<List<StudentResultUi>>
}