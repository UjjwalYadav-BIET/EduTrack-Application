package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.Model.StudentWithMarks
import com.example.edutrackapp.Domain.repository.ResultRepository
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



data class UiState(
    val students: List<StudentWithMarks> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: ResultRepository,

) : ViewModel() {

    var tests = mutableStateListOf<TestEntity>()
        private set
    var selectedTest by mutableStateOf<TestEntity?>(null)
        private set

    fun loadTestById(testId: Int) {
        viewModelScope.launch {
            selectedTest = repository.getTestById(testId)
        }
    }
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState


    fun loadTestsByTeacher(teacherId: Int) {
        viewModelScope.launch {
            tests.clear()
            tests.addAll(repository.getTestsByTeacher(teacherId))
        }
    }
    fun loadStudents(
        testId: Int,
    ) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)

            try {
                val students = repository.getStudentsWithMarks(testId)
                _uiState.value = UiState(students = students)

            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message)
            }
        }
    }

    fun createTest(test: TestEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.createTest(test)
            onDone()
        }
    }

    fun onMarksChange(index: Int, marks: String) {
        val updated = _uiState.value.students.toMutableList()
        updated[index] = updated[index].copy(marks = marks)
        _uiState.value = _uiState.value.copy(students = updated)
    }

    fun saveResults(testId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value.students.forEach { student ->
                    if (!student.marks.isNullOrBlank()) {
                        repository.saveOrUpdateResult(
                            testId,
                            student.studentId,
                            student.marks!!
                        )
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}