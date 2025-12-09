package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val database: EduTrackDatabase
) : ViewModel() {

    var title = mutableStateOf("")
    var subject = mutableStateOf("")
    var description = mutableStateOf("")
    var dueDate = mutableStateOf("")

    // Defaulting to "CS-A" since we haven't built a Batch Selector yet
    var batch = mutableStateOf("CS-A")

    var selectedFileUri = mutableStateOf<Uri?>(null)

    fun onTitleChange(newText: String) { title.value = newText }
    fun onSubjectChange(newText: String) { subject.value = newText }
    fun onDescChange(newText: String) { description.value = newText }
    fun onDateChange(newText: String) { dueDate.value = newText }
    fun onFileSelected(uri: Uri?) { selectedFileUri.value = uri }

    fun createAssignment(onSuccess: () -> Unit) {
        if (title.value.isNotEmpty() && subject.value.isNotEmpty() && description.value.isNotEmpty()) {
            viewModelScope.launch {
                val assignment = AssignmentEntity(
                    title = title.value,
                    subject = subject.value,
                    description = description.value,
                    dueDate = dueDate.value,
                    batch = batch.value, // <--- FIXED: Added the missing batch parameter
                    attachmentUri = selectedFileUri.value?.toString()
                )
                database.assignmentDao.insertAssignment(assignment)
                onSuccess()
            }
        }
    }
}