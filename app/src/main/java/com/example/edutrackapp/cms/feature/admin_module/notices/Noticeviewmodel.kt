package com.example.edutrackapp.cms.feature.admin_module.notices

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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class NoticeUiState {
    object Idle      : NoticeUiState()
    object Loading   : NoticeUiState()
    object Uploading : NoticeUiState()
    data class Success(val message: String) : NoticeUiState()
    data class Error(val message: String)   : NoticeUiState()
}

private const val MAX_FILE_SIZE = 700 * 1024L  // 700 KB

@HiltViewModel
class AdminNoticeViewModel @Inject constructor(
    private val repository  : NoticeRepository,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeUiState>(NoticeUiState.Idle)
    val uiState: StateFlow<NoticeUiState> = _uiState

    val notices = repository.getNoticesForRole("admin")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form fields
    var noticeTitle    = mutableStateOf("")
    var noticeMessage  = mutableStateOf("")
    var noticeTarget   = mutableStateOf(NoticeAudience.ALL)
    var noticePriority = mutableStateOf(NoticePriority.NORMAL)

    // Attachment
    var attachmentUri      = mutableStateOf<Uri?>(null)
    var attachmentName     = mutableStateOf("")
    var attachmentMimeType = mutableStateOf("")

    fun onTitleChange(v: String)            { noticeTitle.value    = v }
    fun onMessageChange(v: String)          { noticeMessage.value  = v }
    fun onTargetChange(t: NoticeAudience)   { noticeTarget.value   = t }
    fun onPriorityChange(p: NoticePriority) { noticePriority.value = p }

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
            _uiState.value = NoticeUiState.Error("File too large. Maximum allowed size is 700 KB.")
            return
        }
        attachmentUri.value      = uri
        attachmentName.value     = name
        attachmentMimeType.value = context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    fun postNotice(onSuccess: () -> Unit) {
        if (noticeTitle.value.isBlank()) {
            _uiState.value = NoticeUiState.Error("Notice title cannot be empty."); return
        }
        if (noticeMessage.value.isBlank()) {
            _uiState.value = NoticeUiState.Error("Notice message cannot be empty."); return
        }

        viewModelScope.launch {
            try {
                // Step 1 — encode attachment to Base64 if present
                var base64Data = ""
                val uri = attachmentUri.value
                if (uri != null) {
                    _uiState.value = NoticeUiState.Uploading
                    base64Data = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                            ?.readBytes()
                            ?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                            ?: ""
                    }
                }

                // Step 2 — save notice to Firestore
                _uiState.value = NoticeUiState.Loading
                val result = repository.postNotice(
                    Notice(
                        title          = noticeTitle.value.trim(),
                        message        = noticeMessage.value.trim(),
                        priority       = noticePriority.value.value,
                        audience       = noticeTarget.value.value,
                        postedBy       = firebaseAuth.currentUser?.email ?: "Admin",
                        postedByRole   = "admin",
                        postedByUid    = firebaseAuth.currentUser?.uid ?: "",
                        attachmentUrl  = base64Data,          // Base64 string stored in Firestore
                        attachmentName = attachmentName.value,
                        attachmentMime = attachmentMimeType.value
                    )
                )

                if (result.isSuccess) {
                    _uiState.value = NoticeUiState.Success("Notice posted!")
                    clearFields()
                    onSuccess()
                } else {
                    _uiState.value = NoticeUiState.Error(result.exceptionOrNull()?.message ?: "Failed.")
                }
            } catch (e: Exception) {
                _uiState.value = NoticeUiState.Error(e.message ?: "Something went wrong.")
            }
        }
    }

    fun deleteNotice(noticeId: String) {
        viewModelScope.launch {
            if (repository.deleteNotice(noticeId).isFailure)
                _uiState.value = NoticeUiState.Error("Delete failed.")
        }
    }

    fun resetState() { _uiState.value = NoticeUiState.Idle }

    private fun clearFields() {
        noticeTitle.value        = ""
        noticeMessage.value      = ""
        noticeTarget.value       = NoticeAudience.ALL
        noticePriority.value     = NoticePriority.NORMAL
        attachmentUri.value      = null
        attachmentName.value     = ""
        attachmentMimeType.value = ""
    }
}