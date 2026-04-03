package com.example.edutrackapp.data.repository

import com.example.edutrackapp.Domain.Model.StudentResultUi
import com.example.edutrackapp.Domain.Model.StudentWithMarks
import com.example.edutrackapp.Domain.repository.ResultRepository
import com.example.edutrackapp.cms.core.data.local.dao.ResultDao
import com.example.edutrackapp.cms.core.data.local.dao.TestDao
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity
import com.example.edutrackapp.data.local.Attendence.StudentEntity
import com.example.edutrackapp.data.local.StudentDao
import kotlinx.coroutines.flow.Flow

class ResultRepositoryImpl(
    private val studentDao: StudentDao,
    private val testDao: TestDao,
    private val resultDao: ResultDao
) : ResultRepository {

    // ---------------- TEST ----------------
    override suspend fun createTest(test: TestEntity): Long {
        return testDao.insertTest(test)
    }



    override suspend fun getAllTests(): List<TestEntity> {
        return testDao.getAllTests()
    }

    // ---------------- STUDENTS ----------------
    override suspend fun getStudentsByClass(
        branch: String,
        semester: Int,
        section: String
    ): List<StudentEntity> {
        return studentDao.getStudentsByClass(branch, semester, section)
    }

    // ---------------- JOIN ----------------
    override suspend fun getStudentsWithMarks(
        testId: Int
    ): List<StudentWithMarks> {

        return testDao.getTestById(testId)?.let { test ->
            resultDao.getStudentsWithMarks(
                testId,
                test.branch,
                test.semester,
                test.section
            )
        } ?: emptyList()
    }

    override suspend fun getTestsByTeacher(teacherId: Int): List<TestEntity> {
        return testDao.getTestsByTeacher(teacherId)
    }

    override suspend fun getTestById(testId: Int): TestEntity? {
        return testDao.getTestById(testId)
    }

    // ---------------- SAVE / UPDATE ----------------
    override suspend fun saveOrUpdateResult(
        testId: Int,
        studentId: Int,
        marks: String
    ) {
        val existing = resultDao.getResult(testId, studentId)

        if (existing != null) {
            resultDao.updateResult(
                existing.copy(marksObtained = marks)
            )
        } else {
            resultDao.insertResult(
                ResultEntity(
                    testId = testId,
                    studentId = studentId,
                    marksObtained = marks
                )
            )
        }
    }
    override fun getStudentResults(rollNo: Int): Flow<List<StudentResultUi>> {
        return resultDao.getResultsForStudent(rollNo)
    }
}