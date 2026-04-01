package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
    private val repository: ResultRepository
) : ViewModel() {

    var tests = mutableStateListOf<TestEntity>()
        private set
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadTests() {
        viewModelScope.launch {
            tests.clear()
            tests.addAll(repository.getAllTests())
        }
    }

    fun loadStudents(
        testId: Int,
        branch: String,
        semester: Int,
        section: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)

            try {
                val data = repository.getStudentsWithMarks(
                    testId, branch, semester, section
                )

                _uiState.value = UiState(students = data)
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