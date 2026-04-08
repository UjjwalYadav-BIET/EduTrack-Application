package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.feature.notices.Notice
import com.example.edutrackapp.cms.feature.notices.NoticeAudience
import com.example.edutrackapp.cms.feature.notices.NoticePriority
import com.example.edutrackapp.cms.feature.notices.NoticeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class TeacherNoticeUiState {
    object Idle      : TeacherNoticeUiState()
    object Loading   : TeacherNoticeUiState()
    object Uploading : TeacherNoticeUiState()
    data class Success(val message: String) : TeacherNoticeUiState()
    data class Error(val message: String)   : TeacherNoticeUiState()
}

private const val MAX_FILE_SIZE = 700 * 1024L  // 700 KB

@HiltViewModel
class TeacherNoticeViewModel @Inject constructor(
    private val repository: NoticeRepository,
    private val auth      : FirebaseAuth,
    private val firestore : FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherNoticeUiState>(TeacherNoticeUiState.Idle)
    val uiState: StateFlow<TeacherNoticeUiState> = _uiState

    val notices = repository.getNoticesForRole("teacher")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form fields
    val title       = mutableStateOf("")
    val description = mutableStateOf("")
    val priority    = mutableStateOf(NoticePriority.NORMAL)

    val titleError = mutableStateOf(false)
    val descError  = mutableStateOf(false)

    // Attachment
    val attachmentUri      = mutableStateOf<Uri?>(null)
    val attachmentName     = mutableStateOf("")
    val attachmentMimeType = mutableStateOf("")

    fun onTitleChange(v: String)            { title.value       = v; titleError.value = false }
    fun onDescChange(v: String)             { description.value = v; descError.value  = false }
    fun onPriorityChange(p: NoticePriority) { priority.value    = p }

    fun onAttachmentPicked(uri: Uri?, name: String) {
        if (uri == null) {
            attachmentUri.value      = null
            attachmentName.value     = ""
            attachmentMimeType.value = ""
            return
        }
        // Reject files larger than 700 KB
        val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        if (size > MAX_FILE_SIZE) {
            _uiState.value = TeacherNoticeUiState.Error("File too large. Maximum allowed size is 700 KB.")
            return
        }
        attachmentUri.value      = uri
        attachmentName.value     = name
        attachmentMimeType.value = context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    // Fetch real teacher name from Firestore
    private val _teacherName = MutableStateFlow("Teacher")
    val teacherName: StateFlow<String> = _teacherName

    init { fetchTeacherName() }

    private fun fetchTeacherName() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                _teacherName.value = doc.getString("name") ?: auth.currentUser?.email ?: "Teacher"
            } catch (_: Exception) { }
        }
    }

    fun postNotice(onSuccess: () -> Unit) {
        titleError.value = title.value.isBlank()
        descError.value  = description.value.isBlank()
        if (titleError.value || descError.value) return

        viewModelScope.launch {
            try {
                // Step 1 — encode attachment to Base64 if present
                var base64Data = ""
                val uri = attachmentUri.value
                if (uri != null) {
                    _uiState.value = TeacherNoticeUiState.Uploading
                    base64Data = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                            ?.readBytes()
                            ?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                            ?: ""
                    }
                }

                // Step 2 — save notice to Firestore
                _uiState.value = TeacherNoticeUiState.Loading
                val result = repository.postNotice(
                    Notice(
                        title          = title.value.trim(),
                        message        = description.value.trim(),
                        priority       = priority.value.value,
                        audience       = NoticeAudience.STUDENTS.value,
                        postedBy       = _teacherName.value,
                        postedByRole   = "teacher",
                        postedByUid    = auth.currentUser?.uid ?: "",
                        attachmentUrl  = base64Data,          // Base64 string stored in Firestore
                        attachmentName = attachmentName.value,
                        attachmentMime = attachmentMimeType.value
                    )
                )

                if (result.isSuccess) {
                    _uiState.value = TeacherNoticeUiState.Success("Notice posted!")
                    clearFields()
                    onSuccess()
                } else {
                    _uiState.value = TeacherNoticeUiState.Error(result.exceptionOrNull()?.message ?: "Failed.")
                }
            } catch (e: Exception) {
                _uiState.value = TeacherNoticeUiState.Error(e.message ?: "Something went wrong.")
            }
        }
    }

    fun resetState() { _uiState.value = TeacherNoticeUiState.Idle }

    private fun clearFields() {
        title.value          = ""
        description.value    = ""
        priority.value       = NoticePriority.NORMAL
        attachmentUri.value  = null
        attachmentName.value = ""
        attachmentMimeType.value = ""
    }
}