package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.AssignmentSubmissionRepository
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherEvaluateViewModel @Inject constructor(
    private val repository: AssignmentSubmissionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val submissionId: Int =
        checkNotNull(savedStateHandle["submissionId"])

    private val _submission = MutableStateFlow<AssignmentSubmissionEntity?>(null)
    val submission = _submission.asStateFlow()

    init {
        loadSubmission()
    }

    private fun loadSubmission() {
        viewModelScope.launch {
            _submission.value = repository.getSubmissionById(submissionId)
        }
    }

    fun evaluateSubmission(marks: Int, feedback: String) {
        viewModelScope.launch {
            repository.evaluateSubmission(submissionId, marks, feedback)
        }
    }
}