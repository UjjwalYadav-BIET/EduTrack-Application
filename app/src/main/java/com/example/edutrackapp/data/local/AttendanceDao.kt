package com.example.edutrackapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.data.local.Attendence.AttendanceEntity
import com.example.edutrackapp.data.local.Attendence.StudentEntity
import com.example.edutrackapp.data.local.Attendence.SubjectEntity

@Dao
interface AttendanceDao {

    // --- Students ---
    @Query("""
        SELECT * FROM students
        WHERE semester = :semester AND section = :section
    """)
    suspend fun getStudents(
        semester: Int,
        section: String
    ): List<StudentEntity>

    // --- Subjects ---
    @Query("""
        SELECT s.* FROM subjects s
        INNER JOIN faculty_subjects fs
        ON s.subjectId = fs.subjectId
        WHERE fs.facultyId = :facultyId
    """)
    suspend fun getSubjects(facultyId: Int): List<SubjectEntity>



    // --- Attendance ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: List<AttendanceEntity>)

    @Query("""
        SELECT * FROM attendance
        WHERE studentId = :studentId AND subjectId = :subjectId
    """)
    suspend fun getAttendanceForStudent(studentId: Int, subjectId: Int): List<AttendanceEntity>


    @Query("""
    SELECT COUNT(*) FROM attendance
    WHERE subjectId = :subjectId
    AND facultyId = :facultyId
    AND date = :date
    AND lecturePeriod = :lecturePeriod
""")
    suspend fun isAttendanceAlreadyMarked(
        facultyId: Int,
        subjectId: Int,
        date: Long,
        lecturePeriod: Int
    ): Int

    @Query("""
    SELECT createdAt FROM attendance
    WHERE facultyId = :facultyId
    ORDER BY createdAt DESC
    LIMIT 1
""")
    suspend fun getLastAttendanceTime(facultyId: Int): Long?


    @Query("""
        SELECT (SUM(CASE WHEN isPresent = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(*)
        FROM attendance
        WHERE studentId = :studentId AND subjectId = :subjectId
    """)
    suspend fun getAttendancePercentage(studentId: Int, subjectId: Int): Double
}
