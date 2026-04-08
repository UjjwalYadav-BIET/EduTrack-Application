package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class AssignmentPriority(val label: String) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High")
}

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val database: EduTrackDatabase,
    private val db:       FirebaseFirestore    // ← Firestore injected
) : ViewModel() {

    val title           = mutableStateOf("")
    val subject         = mutableStateOf("")
    val description     = mutableStateOf("")
    val dueDate         = mutableStateOf("")
    val batch           = mutableStateOf("CS-A")
    val totalMarks      = mutableStateOf("100")
    val priority        = mutableStateOf(AssignmentPriority.MEDIUM)
    val selectedFileUri = mutableStateOf<Uri?>(null)

    val titleError       = mutableStateOf(false)
    val subjectError     = mutableStateOf(false)
    val descriptionError = mutableStateOf(false)
    val dueDateError     = mutableStateOf(false)

    fun onTitleChange(v: String)   { title.value = v;       titleError.value = false }
    fun onSubjectChange(v: String) { subject.value = v;     subjectError.value = false }
    fun onDescChange(v: String)    { description.value = v; descriptionError.value = false }
    fun onDateChange(v: String)    { dueDate.value = v;     dueDateError.value = false }
    fun onMarksChange(v: String)   { if (v.all { it.isDigit() }) totalMarks.value = v }
    fun onPriorityChange(p: AssignmentPriority) { priority.value = p }
    fun onFileSelected(uri: Uri?)  { selectedFileUri.value = uri }

    fun createAssignment(onSuccess: () -> Unit) {
        titleError.value       = title.value.isBlank()
        subjectError.value     = subject.value.isBlank()
        descriptionError.value = description.value.isBlank()
        dueDateError.value     = dueDate.value.isBlank()

        if (titleError.value || subjectError.value ||
            descriptionError.value || dueDateError.value) return

        viewModelScope.launch {
            try {
                // 1. Existing: save to local Room
                database.assignmentDao.insertAssignment(
                    AssignmentEntity(
                        title         = title.value.trim(),
                        subject       = subject.value.trim(),
                        description   = description.value.trim(),
                        dueDate       = dueDate.value.trim(),
                        batch         = batch.value,
                        attachmentUri = selectedFileUri.value?.toString()
                    )
                )

                // 2. NEW: save to Firestore so every student sees it in real time
                db.collection("assignments").add(mapOf(
                    "title"         to title.value.trim(),
                    "subject"       to subject.value.trim(),
                    "description"   to description.value.trim(),
                    "dueDate"       to dueDate.value.trim(),
                    "batch"         to batch.value,
                    "totalMarks"    to totalMarks.value,
                    "priority"      to priority.value.name,
                    "attachmentUri" to (selectedFileUri.value?.toString() ?: ""),
                    "createdAt"     to System.currentTimeMillis()
                )).await()

                Log.d("AssignmentVM", "Firestore assignment created successfully")
            } catch (e: Exception) {
                Log.e("AssignmentVM", "Firestore write failed: ${e.message}", e)
            }
            onSuccess()
        }
    }
}