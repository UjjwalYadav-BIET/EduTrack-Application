package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.NoticeRepository
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    var title = mutableStateOf("")
    var description = mutableStateOf("")
    var attachmentUri = mutableStateOf<String?>(null)

    var targetYear = mutableStateOf(2)
    var targetBranch = mutableStateOf("CSE")
    var targetSection = mutableStateOf("A")

    fun onTitleChange(text: String) { title.value = text }
    fun onDescChange(text: String) { description.value = text }

    fun postNotice(onSuccess: () -> Unit) {
        if (title.value.isNotEmpty() && description.value.isNotEmpty()) {
            viewModelScope.launch {

                val notice = NoticeEntity(
                    title = title.value,
                    description = description.value,
                    createdAt = System.currentTimeMillis(),
                    teacherId = 1, // 🔥 Replace with logged-in teacher ID
                    targetYear = targetYear.value,
                    targetBranch = targetBranch.value,
                    targetSection = targetSection.value,
                    attachmentUrl = attachmentUri.value,
                    isActive = true
                )

                repository.insertNotice(notice)
                onSuccess()
            }
        }
    }
}