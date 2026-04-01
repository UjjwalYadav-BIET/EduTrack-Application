package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.AssignmentRepository
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val repository: AssignmentRepository
) : ViewModel() {

    var title = mutableStateOf("")
    var subject = mutableStateOf("")
    var description = mutableStateOf("")
    var semester = mutableStateOf("")
    var section = mutableStateOf("")
    var branch = mutableStateOf("")

    fun onSemesterChange(value: String) {
        semester.value = value
    }

    fun onSectionChange(value: String) {
        section.value = value
    }

    fun onBranchChange(value: String) {
        branch.value = value
    }
    var dueDate = mutableStateOf("")

    var selectedFileUri = mutableStateOf<Uri?>(null)

    fun onTitleChange(newText: String) { title.value = newText }
    fun onSubjectChange(newText: String) { subject.value = newText }
    fun onDescChange(newText: String) { description.value = newText }
    fun onDateChange(newText: String) { dueDate.value = newText }
    fun onFileSelected(uri: Uri?) { selectedFileUri.value = uri }

    fun createAssignment(
        semester: Int,
        dueDate: Long,
        subjectId: Int,
        onSuccess: () -> Unit
    ) {

        if (
            title.value.isNotEmpty() &&
            description.value.isNotEmpty() &&
            semester != 0 &&
            section.value.isNotEmpty() &&
            branch.value.isNotEmpty()
        ) {

            viewModelScope.launch {

                val assignment = AssignmentEntity(
                    title = title.value,
                    description = description.value,
                    subjectId = subjectId,
                    semester = semester,
                    section = section.value,
                    branch = branch.value,
                    teacherId = 1,
                    createdDate = System.currentTimeMillis(),
                    dueDate = dueDate,
                    attachmentUri = selectedFileUri.value?.toString()
                )
                repository.insertAssignment(assignment)
                clearFields()
                onSuccess()
            }
        }
    }

    private fun clearFields() {
        title.value = ""
        subject.value = ""
        description.value = ""
        dueDate.value = ""
        selectedFileUri.value = null
    }
}