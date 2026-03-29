package com.example.edutrackapp.cms.feature.student_module.assignments.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.AssignmentRepository
import com.example.edutrackapp.Domain.repository.AssignmentSubmissionRepository
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StudentAssignmentListViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val submissionRepository: AssignmentSubmissionRepository,

    @ApplicationContext private val context: Context
) : ViewModel() {

    private val studentRollNo = "CS-101"

    val uiState = combine(
        assignmentRepository.getAllAssignments(),
        submissionRepository.getSubmissionsByStudent(studentRollNo),
        assignmentRepository.getAllSubjects()
    ) { assignments, submissions, subjects ->

        assignments.map { assignment ->

            val isSubmitted = submissions.any {
                it.assignmentId == assignment.id
            }

            val subjectName = subjects.find {
                it.subjectId == assignment.subjectId
            }?.subjectName ?: "Unknown"

            AssignmentUiState(
                assignment = assignment,
                subjectName = subjectName,
                isSubmitted = isSubmitted
            )
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )



    // ⭐ ADD THIS HERE
    val pendingAssignments = assignmentRepository
        .getPendingAssignments(studentRollNo)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun submitAssignment(
        assignmentId: Int,
        fileUri: Uri
    ) {

        viewModelScope.launch {

            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(
                    fileUri,
                    takeFlags
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val date = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())

            val assignment = assignmentRepository.getAssignmentById(assignmentId)

            val currentTime = System.currentTimeMillis()

            val status = if (assignment != null && currentTime > assignment.dueDate) {
                "LATE"
            } else {
                "SUBMITTED"
            }
            val existingSubmission = submissionRepository
                .getSubmissionByStudent(assignmentId, studentRollNo)

            if (existingSubmission != null) {
                // Already submitted → skip or update
                return@launch
            }

            val submission = AssignmentSubmissionEntity(
                assignmentId = assignmentId,
                studentRollNo = studentRollNo,
                submissionDate = date,
                submissionTime = currentTime,
                fileUri = fileUri.toString(),
                status = status
            )

            submissionRepository.submitAssignment(submission)

        }
    }
}