package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity

data class TeacherAssignmentUi(
    val assignment: AssignmentEntity,
    val subjectName: String
)