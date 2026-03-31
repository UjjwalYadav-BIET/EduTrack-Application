package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TeacherAssignmentListViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    private val assignmentFlow = assignmentRepository.getAllAssignments()
    private val subjectFlow = assignmentRepository.getAllSubjects()

    val assignments = combine(
        assignmentFlow,
        subjectFlow
    ) { assignments, subjects ->

        assignments.map { assignment ->

            val subjectName = subjects.find {
                it.subjectId == assignment.subjectId
            }?.subjectName ?: "Unknown"

            TeacherAssignmentUi(
                assignment = assignment,
                subjectName = subjectName
            )
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}