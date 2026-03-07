package com.example.edutrackapp.data.repository

import com.example.edutrackapp.Domain.Model.attendance.Attendance
import com.example.edutrackapp.Domain.Model.attendance.Student
import com.example.edutrackapp.Domain.Model.attendance.Subject
import com.example.edutrackapp.Domain.repository.AttendanceRepository
import com.example.edutrackapp.data.Attendence.AttendanceEntity
import com.example.edutrackapp.data.local.AttendanceDao
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val dao: AttendanceDao
) : AttendanceRepository {

    override suspend fun getStudentsForFaculty(
        facultyId: Int,
        semester: Int,
        section: String
    ): List<Student> {
        return dao.getStudents(semester, section).map {
            Student(
                it.studentId,
                it.name,
                it.rollNo,
                it.branch,
                it.semester,
                it.section
            )
        }
    }

    override suspend fun getSubjectsForFaculty(facultyId: Int): List<Subject> {
        return dao.getSubjects(facultyId).map {
            Subject(
                it.subjectId,
                it.subjectName,
                it.semester,
                it.branch
            )
        }
    }
    override suspend fun isAttendanceAlreadyTaken(
        facultyId: Int,
        subjectId: Int,
        date: Long,
        lecturePeriod: Int
    ): Boolean {
        return dao.isAttendanceAlreadyMarked(
            facultyId, subjectId, date, lecturePeriod
        ) > 0
    }

    override suspend fun canTakeAttendance(facultyId: Int): Boolean {
        val lastTime = dao.getLastAttendanceTime(facultyId) ?: return true
        val diffMinutes = (System.currentTimeMillis() - lastTime) / (1000 * 60)
        return diffMinutes >= 60
    }


    override suspend fun insertAttendance(attendance: List<Attendance>) {
        dao.insertAttendance(
            attendance.map {
                AttendanceEntity(
                    attendanceId = it.id,
                    studentId = it.studentId,
                    subjectId = it.subjectId,
                    facultyId = it.facultyId,
                    date = it.date,
                    lecturePeriod = it.lecturePeriod,
                    isPresent = it.isPresent
                )
            }
        )
    }

    override suspend fun getAttendanceForStudent(
        studentId: Int,
        subjectId: Int
    ): List<Attendance> {
        return dao.getAttendanceForStudent(studentId, subjectId).map {
            Attendance(
                it.attendanceId,
                it.studentId,
                it.subjectId,
                it.facultyId,
                it.date,
                it.lecturePeriod,
                it.isPresent
            )
        }
    }

    override suspend fun getAttendancePercentage(
        studentId: Int,
        subjectId: Int
    ): Double {
        return dao.getAttendancePercentage(studentId, subjectId)
    }
}
