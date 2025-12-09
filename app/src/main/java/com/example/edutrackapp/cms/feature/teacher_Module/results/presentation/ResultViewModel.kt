package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentResultUi(
    val name: String,
    val rollNo: String,
    var marks: String = "" // State for text input
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val database: EduTrackDatabase
) : ViewModel() {

    // Mock List of Students
    private val _students = mutableStateListOf<StudentResultUi>()
    val students: List<StudentResultUi> = _students

    init {
        // Load Dummy Students
        val dummies = List(10) { i ->
            StudentResultUi("Student ${i + 1}", "CS-10$i")
        }
        _students.addAll(dummies)
    }

    fun onMarksChange(index: Int, newMarks: String) {
        val student = _students[index]
        _students[index] = student.copy(marks = newMarks)
    }

    fun saveResults(examType: String, subject: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _students.forEach { student ->
                if (student.marks.isNotEmpty()) {
                    val result = ResultEntity(
                        studentName = student.name,
                        rollNo = student.rollNo,
                        subject = subject,
                        examType = examType,
                        marksObtained = student.marks,
                        maxMarks = "100"
                    )
                    database.resultDao.insertResult(result)
                }
            }
            onSuccess()
        }
    }
}