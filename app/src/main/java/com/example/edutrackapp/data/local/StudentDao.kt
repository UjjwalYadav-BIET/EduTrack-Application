package com.example.edutrackapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.data.local.Attendence.StudentEntity

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<StudentEntity>

    @Query("""
        SELECT * FROM students 
        WHERE branch = :branch AND semester = :semester AND section = :section
    """)
    suspend fun getStudentsByClass(
        branch: String,
        semester: Int,
        section: String
    ): List<StudentEntity>

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}