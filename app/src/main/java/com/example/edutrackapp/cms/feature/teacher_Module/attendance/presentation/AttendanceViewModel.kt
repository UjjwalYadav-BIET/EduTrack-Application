package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor() : ViewModel() {

    // We use mutableStateListOf so the UI updates instantly when we click a checkbox
    private val _students = mutableStateListOf<StudentUiModel>()
    val students: List<StudentUiModel> = _students

    init {
        loadDummyStudents()
    }

    private fun loadDummyStudents() {
        // Create 20 fake students for testing
        val dummyList = List(20) { index ->
            StudentUiModel(
                id = "ST_$index",
                name = "Student Name ${index + 1}",
                rollNo = "CS-${100 + index}"
            )
        }
        _students.addAll(dummyList)
    }

    fun toggleAttendance(studentId: String) {
        val index = _students.indexOfFirst { it.id == studentId }
        if (index != -1) {
            // Flip the boolean (True -> False, False -> True)
            val student = _students[index]
            _students[index] = student.copy(isPresent = !student.isPresent)
        }
    }

    // ... inside AttendanceViewModel

    fun markStudentsBasedOnCount(count: Int) {
        // Reset everyone to Absent first (optional)
        // _students.replaceAll { it.copy(isPresent = false) }

        // Mark the first 'count' students as Present
        for (i in 0 until _students.size) {
            if (i < count) {
                _students[i] = _students[i].copy(isPresent = true)
            } else {
                _students[i] = _students[i].copy(isPresent = false)
            }
        }
    }
}