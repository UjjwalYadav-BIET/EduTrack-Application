package com.example.edutrackapp.cms.feature.student_module.attendance.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SubjectAttendance(
    val subjectName: String,
    val attended: Int,
    val total: Int,
    val color: Color
) {
    val percentage: Int
        get() = if (total == 0) 0 else ((attended.toFloat() / total) * 100).toInt()
}

@HiltViewModel
class StudentAttendanceViewModel @Inject constructor() : ViewModel() {

    private val _attendanceList = mutableStateListOf<SubjectAttendance>()
    val attendanceList: List<SubjectAttendance> = _attendanceList

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _attendanceList.addAll(
            listOf(
                SubjectAttendance("Data Structures", 28, 30, Color(0xFF4CAF50)), // Green (Good)
                SubjectAttendance("Operating Systems", 18, 24, Color(0xFF4CAF50)),
                SubjectAttendance("Algorithms", 12, 20, Color(0xFFFFC107)), // Yellow (Warning)
                SubjectAttendance("Mathematics", 15, 25, Color(0xFFFFC107)),
                SubjectAttendance("Computer Networks", 5, 15, Color(0xFFF44336)) // Red (Danger)
            )
        )
    }

    fun getOverallPercentage(): Int {
        if (_attendanceList.isEmpty()) return 0
        val totalClasses = _attendanceList.sumOf { it.total }
        val totalAttended = _attendanceList.sumOf { it.attended }
        return ((totalAttended.toFloat() / totalClasses) * 100).toInt()
    }
}