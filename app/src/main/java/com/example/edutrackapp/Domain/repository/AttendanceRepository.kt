package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.Domain.Model.attendance.Attendance
import com.example.edutrackapp.Domain.Model.attendance.Student
import com.example.edutrackapp.Domain.Model.attendance.Subject



interface AttendanceRepository {
    suspend fun getStudentsForFaculty(facultyId: Int, semester: Int, section: String): List<Student>
    suspend fun getSubjectsForFaculty(facultyId: Int): List<Subject>
    suspend fun insertAttendance(attendance: List<Attendance>)
    suspend fun getAttendanceForStudent(studentId: Int, subjectId: Int): List<Attendance>
    suspend fun getAttendancePercentage(studentId: Int, subjectId: Int): Double

    suspend fun isAttendanceAlreadyTaken(
        facultyId: Int,
        subjectId: Int,
        date: Long,
        lecturePeriod: Int
    ): Boolean

    suspend fun canTakeAttendance(
        facultyId: Int
    ): Boolean
}
