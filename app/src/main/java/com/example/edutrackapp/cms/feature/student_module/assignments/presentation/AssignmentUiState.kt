package com.example.edutrackapp.cms.feature.student_module.assignments.presentation

import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity

// Helper Data Class
data class AssignmentUiState(
    val assignment: AssignmentEntity,
    val subjectName: String,
    val isSubmitted: Boolean
)